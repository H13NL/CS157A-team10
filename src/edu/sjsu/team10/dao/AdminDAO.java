package edu.sjsu.team10.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.sjsu.team10.model.UserAccount;

// Administrator actions: list accounts, approve/reject restockers, disable/enable users.
// Every change is written to audit_log in the same transaction, so a change is never
// saved without its log entry (or the other way around).
public class AdminDAO {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // Every user with their role and account state.
    // Role comes from whichever ISA subclass table holds the user.
    // A restocker with approved_by = NULL is pending until an admin approves them.
    private static final String ACCOUNTS_SQL =
        "SELECT * FROM ("
      + "  SELECT u.user_id, u.name, u.email, u.created_at, "
      + "         CASE WHEN a.user_id  IS NOT NULL THEN 'admin' "
      + "              WHEN ch.user_id IS NOT NULL THEN 'chef' "
      + "              WHEN r.user_id  IS NOT NULL THEN 'restocker' "
      + "              WHEN co.user_id IS NOT NULL THEN 'consumer' "
      + "              ELSE 'none' END AS role, "
      + "         CASE WHEN u.status = 'rejected' THEN 'rejected' "
      + "              WHEN u.status = 'disabled' THEN 'disabled' "
      + "              WHEN r.user_id IS NOT NULL AND r.approved_by IS NULL THEN 'pending' "
      + "              ELSE 'active' END AS state, "
      + "         res.restaurant_name, r.company_name, r.approved_at, "
      + "         approver.name AS approved_by_name "
      + "  FROM users u "
      + "  LEFT JOIN admin a        ON a.user_id = u.user_id "
      + "  LEFT JOIN chef ch        ON ch.user_id = u.user_id "
      + "  LEFT JOIN restaurant res ON res.restaurant_id = ch.restaurant_id "
      + "  LEFT JOIN restocker r    ON r.user_id = u.user_id "
      + "  LEFT JOIN users approver ON approver.user_id = r.approved_by "
      + "  LEFT JOIN consumer co    ON co.user_id = u.user_id "
      + ") accounts WHERE 1 = 1 ";

    // filters left null/empty are ignored; search matches name or email
    public List<UserAccount> findAccounts(String role, String state, String search) throws SQLException {
        StringBuilder sql = new StringBuilder(ACCOUNTS_SQL);
        List<String> params = new ArrayList<>();

        if (role != null && !role.isEmpty()) {
            sql.append("AND role = ? ");
            params.add(role);
        }
        if (state != null && !state.isEmpty()) {
            sql.append("AND state = ? ");
            params.add(state);
        }
        if (search != null && !search.isEmpty()) {
            sql.append("AND (name LIKE ? OR email LIKE ?) ");
            String like = "%" + AuditLogDAO.escapeLike(search) + "%";
            params.add(like);
            params.add(like);
        }
        // pending restockers first, since they need action
        sql.append("ORDER BY state = 'pending' DESC, created_at DESC, user_id DESC");

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            return readAccounts(ps);
        }
    }

    public List<UserAccount> findPendingRestockers() throws SQLException {
        return findAccounts("restocker", "pending", null);
    }

    // [total users, pending restockers, disabled accounts]
    public int[] countSummary() throws SQLException {
        String sql =
            "SELECT COUNT(*), "
          + "       SUM(r.user_id IS NOT NULL AND r.approved_by IS NULL AND u.status NOT IN ('rejected', 'disabled')), "
          + "       SUM(u.status = 'disabled') "
          + "FROM users u LEFT JOIN restocker r ON r.user_id = u.user_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return new int[] { rs.getInt(1), rs.getInt(2), rs.getInt(3) };
        }
    }

    // Approve relationship: set restocker.approved_by to this admin.
    // Also works on a rejected restocker, so an admin can change their mind.
    // Returns false if the user isn't a restocker waiting for approval.
    public boolean approveRestocker(int adminId, int restockerId) throws SQLException {
        String approveSql =
            "UPDATE restocker SET approved_by = ?, approved_at = CURRENT_TIMESTAMP "
          + "WHERE user_id = ? AND approved_by IS NULL";
        String reactivateSql =
            "UPDATE users SET status = 'active' WHERE user_id = ? AND status = 'rejected'";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int changed;
                try (PreparedStatement ps = con.prepareStatement(approveSql)) {
                    ps.setInt(1, adminId);
                    ps.setInt(2, restockerId);
                    changed = ps.executeUpdate();
                }
                if (changed == 0) {
                    con.rollback();
                    return false;
                }
                try (PreparedStatement ps = con.prepareStatement(reactivateSql)) {
                    ps.setInt(1, restockerId);
                    ps.executeUpdate();
                }
                auditLogDAO.log(con, adminId, AuditLogDAO.APPROVE_RESTOCKER, "restocker", restockerId);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // Marks a pending restocker as rejected. They stay in the table (approved_by stays NULL)
    // so the decision shows up in the user list and audit log, and they can't log in.
    public boolean rejectRestocker(int adminId, int restockerId) throws SQLException {
        String sql =
            "UPDATE users u JOIN restocker r ON r.user_id = u.user_id "
          + "SET u.status = 'rejected' "
          + "WHERE u.user_id = ? AND r.approved_by IS NULL AND u.status = 'active'";
        return auditLogDAO.updateAndLog(adminId, AuditLogDAO.REJECT_RESTOCKER, "restocker", restockerId, sql, restockerId);
    }

    // Admin accounts can't be disabled here, so the site can't be locked out of its last admin.
    public boolean disableUser(int adminId, int userId) throws SQLException {
        String sql =
            "UPDATE users SET status = 'disabled' "
          + "WHERE user_id = ? AND status = 'active' "
          + "AND user_id NOT IN (SELECT user_id FROM admin)";
        return auditLogDAO.updateAndLog(adminId, AuditLogDAO.DISABLE_USER, "user", userId, sql, userId);
    }

    public boolean enableUser(int adminId, int userId) throws SQLException {
        String sql = "UPDATE users SET status = 'active' WHERE user_id = ? AND status = 'disabled'";
        return auditLogDAO.updateAndLog(adminId, AuditLogDAO.ENABLE_USER, "user", userId, sql, userId);
    }

    private List<UserAccount> readAccounts(PreparedStatement ps) throws SQLException {
        List<UserAccount> accounts = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(new UserAccount(
                    rs.getInt("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("role"),
                    rs.getString("state"),
                    rs.getTimestamp("created_at"),
                    rs.getString("restaurant_name"),
                    rs.getString("company_name"),
                    rs.getString("approved_by_name"),
                    rs.getTimestamp("approved_at")
                ));
            }
        }
        return accounts;
    }
}
