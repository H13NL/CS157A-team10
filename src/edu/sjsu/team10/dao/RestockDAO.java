package edu.sjsu.team10.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.sjsu.team10.model.RestockRequest;
import edu.sjsu.team10.model.RestockerOption;

// Restock requests a chef sends to a restocker.
// restock_request.chef_id = Submit, restocker_id = Receives, recipe_id = BasedOn;
// the ingredients and quantities go in the contain table.
public class RestockDAO {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // why submit() refused a request
    public static class RequestRejected extends Exception {
        private static final long serialVersionUID = 1L;
        private final String reason;

        public RequestRejected(String reason) {
            super(reason);
            this.reason = reason;
        }

        // "restocker" (not approved/active) or "notcarried" (an item isn't in the catalog or exceeds what's available)
        public String getReason() { return reason; }
    }

    // only approved, active restockers can receive requests
    private static final String APPROVED_RESTOCKER =
        "r.approved_by IS NOT NULL AND u.status = 'active'";

    public List<RestockerOption> findApprovedRestockers() throws SQLException {
        String sql = "SELECT r.user_id, r.company_name, u.name, "
                   + "       (SELECT COUNT(*) FROM supplies s WHERE s.restocker_id = r.user_id) AS catalog_size "
                   + "FROM restocker r JOIN users u ON u.user_id = r.user_id "
                   + "WHERE " + APPROVED_RESTOCKER + " "
                   + "ORDER BY COALESCE(r.company_name, u.name)";

        List<RestockerOption> restockers = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                restockers.add(new RestockerOption(
                    rs.getInt("user_id"),
                    rs.getString("company_name"),
                    rs.getString("name"),
                    rs.getInt("catalog_size")
                ));
            }
        }
        return restockers;
    }

    // Saves a request and its items in one transaction and returns the new request id.
    // items maps ing_id -> quantity requested (all > 0). recipeId is null for a simple request.
    // Each item must be in the restocker's catalog with at least that much available,
    // otherwise nothing is saved and RequestRejected("notcarried") is thrown.
    public int submit(int chefId, int restockerId, Integer recipeId, Map<Integer, BigDecimal> items)
            throws SQLException, RequestRejected {

        String checkRestockerSql =
            "SELECT 1 FROM restocker r JOIN users u ON u.user_id = r.user_id "
          + "WHERE r.user_id = ? AND " + APPROVED_RESTOCKER;
        String insertRequestSql =
            "INSERT INTO restock_request (chef_id, restocker_id, recipe_id, status) VALUES (?, ?, ?, 'pending')";
        // copies the line only if this restocker carries the ingredient with enough available
        String insertItemSql =
            "INSERT INTO contain (request_id, ing_id, qty_req) "
          + "SELECT ?, ing_id, ? FROM supplies WHERE restocker_id = ? AND ing_id = ? AND qty >= ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(checkRestockerSql)) {
                    ps.setInt(1, restockerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            throw new RequestRejected("restocker");
                        }
                    }
                }

                int requestId;
                try (PreparedStatement ps = con.prepareStatement(insertRequestSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, chefId);
                    ps.setInt(2, restockerId);
                    if (recipeId == null) {
                        ps.setNull(3, Types.INTEGER);
                    } else {
                        ps.setInt(3, recipeId);
                    }
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        requestId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(insertItemSql)) {
                    for (Map.Entry<Integer, BigDecimal> item : items.entrySet()) {
                        ps.setInt(1, requestId);
                        ps.setBigDecimal(2, item.getValue());
                        ps.setInt(3, restockerId);
                        ps.setInt(4, item.getKey());
                        ps.setBigDecimal(5, item.getValue());
                        if (ps.executeUpdate() == 0) {
                            con.rollback();
                            throw new RequestRejected("notcarried");
                        }
                    }
                }

                auditLogDAO.log(con, chefId, AuditLogDAO.SUBMIT_REQUEST, "restock_request", requestId);
                con.commit();
                return requestId;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // a chef's own requests, newest first
    public List<RestockRequest> findByChef(int chefId) throws SQLException {
        return find("rr.chef_id = ?", chefId);
    }

    // requests sent to a restocker, newest first
    public List<RestockRequest> findByRestocker(int restockerId) throws SQLException {
        return find("rr.restocker_id = ?", restockerId);
    }

    private List<RestockRequest> find(String where, int id) throws SQLException {
        String requestSql =
            "SELECT rr.request_id, rr.status, rr.created_at, rr.responded_at, "
          + "       chef_user.name AS chef_name, res.restaurant_name, "
          + "       COALESCE(rk.company_name, rk_user.name) AS restocker_name, rec.name AS recipe_name "
          + "FROM restock_request rr "
          + "JOIN users chef_user ON chef_user.user_id = rr.chef_id "
          + "JOIN chef ch         ON ch.user_id = rr.chef_id "
          + "JOIN restaurant res  ON res.restaurant_id = ch.restaurant_id "
          + "JOIN restocker rk    ON rk.user_id = rr.restocker_id "
          + "JOIN users rk_user   ON rk_user.user_id = rr.restocker_id "
          + "LEFT JOIN recipe rec ON rec.recipe_id = rr.recipe_id "
          + "WHERE " + where + " "
          + "ORDER BY rr.created_at DESC, rr.request_id DESC";

        // items for the same requests, with the restocker's current price if they still carry it
        String itemSql =
            "SELECT c.request_id, i.name, i.unit, c.qty_req, s.price "
          + "FROM restock_request rr "
          + "JOIN contain c    ON c.request_id = rr.request_id "
          + "JOIN ingredient i ON i.ing_id = c.ing_id "
          + "LEFT JOIN supplies s ON s.restocker_id = rr.restocker_id AND s.ing_id = c.ing_id "
          + "WHERE " + where + " "
          + "ORDER BY i.name";

        Map<Integer, RestockRequest> requests = new LinkedHashMap<>();

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(requestSql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        requests.put(rs.getInt("request_id"), new RestockRequest(
                            rs.getInt("request_id"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at"),
                            rs.getTimestamp("responded_at"),
                            rs.getString("chef_name"),
                            rs.getString("restaurant_name"),
                            rs.getString("restocker_name"),
                            rs.getString("recipe_name")
                        ));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(itemSql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        RestockRequest request = requests.get(rs.getInt("request_id"));
                        if (request != null) {
                            request.addItem(new RestockRequest.Item(
                                rs.getString("name"),
                                rs.getString("unit"),
                                rs.getBigDecimal("qty_req"),
                                rs.getBigDecimal("price")
                            ));
                        }
                    }
                }
            }
        }
        return new ArrayList<>(requests.values());
    }
}
