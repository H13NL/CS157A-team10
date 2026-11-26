package edu.sjsu.team10.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.sjsu.team10.model.AuditEntry;

// Writes and reads audit_log (the Perform relationship: a user performs a logged action).
public class AuditLogDAO {

    // actions written by this app; kept here so every caller spells them the same way
    public static final String LOGIN = "LOGIN";
    public static final String APPROVE_RESTOCKER = "APPROVE_RESTOCKER";
    public static final String REJECT_RESTOCKER = "REJECT_RESTOCKER";
    public static final String DISABLE_USER = "DISABLE_USER";
    public static final String ENABLE_USER = "ENABLE_USER";
    public static final String CATALOG_ADD = "CATALOG_ADD";
    public static final String CATALOG_EDIT = "CATALOG_EDIT";
    public static final String CATALOG_REMOVE = "CATALOG_REMOVE";
    public static final String SUBMIT_REQUEST = "SUBMIT_REQUEST";

    private static final String INSERT_SQL =
        "INSERT INTO audit_log (user_id, action, entity_type, entity_id) VALUES (?, ?, ?, ?)";

    // entity_id is not a foreign key, so the target's name is looked up by entity_type
    private static final String SELECT_SQL =
        "SELECT l.log_id, l.user_id, actor.name AS actor_name, actor.email AS actor_email, "
      + "       l.action, l.entity_type, l.entity_id, l.created_at, "
      + "       COALESCE(tu.name, rc.name, ig.name) AS target_name "
      + "FROM audit_log l "
      + "JOIN users actor ON actor.user_id = l.user_id "
      + "LEFT JOIN users tu      ON l.entity_type IN ('user', 'restocker') AND tu.user_id = l.entity_id "
      + "LEFT JOIN recipe rc     ON l.entity_type = 'recipe' AND rc.recipe_id = l.entity_id "
      + "LEFT JOIN ingredient ig ON l.entity_type = 'ingredient' AND ig.ing_id = l.entity_id ";

    // log on its own connection (used for things like logins)
    public void log(int userId, String action, String entityType, int entityId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            log(con, userId, action, entityType, entityId);
        }
    }

    // log on the caller's connection so the entry commits or rolls back with the change it describes
    public void log(Connection con, int userId, String action, String entityType, int entityId)
            throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
            ps.setInt(1, userId);
            ps.setString(2, action);
            ps.setString(3, entityType);
            ps.setInt(4, entityId);
            ps.executeUpdate();
        }
    }

    // Runs one UPDATE/DELETE with the given parameters. If it changed a row, logs the action in the
    // same transaction and returns true; if it changed nothing, saves nothing and returns false.
    public boolean updateAndLog(int actorId, String action, String entityType, int entityId,
                                String sql, Object... params) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int changed;
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    for (int i = 0; i < params.length; i++) {
                        ps.setObject(i + 1, params[i]);
                    }
                    changed = ps.executeUpdate();
                }
                if (changed == 0) {
                    con.rollback();
                    return false;
                }
                log(con, actorId, action, entityType, entityId);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<AuditEntry> findRecent(int limit) throws SQLException {
        return find(null, null, null, limit);
    }

    // newest first; any filter left null/empty is ignored. search matches the actor's name or email.
    public List<AuditEntry> find(String action, String entityType, String search, int limit)
            throws SQLException {

        StringBuilder sql = new StringBuilder(SELECT_SQL).append("WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();

        if (action != null && !action.isEmpty()) {
            sql.append("AND l.action = ? ");
            params.add(action);
        }
        if (entityType != null && !entityType.isEmpty()) {
            sql.append("AND l.entity_type = ? ");
            params.add(entityType);
        }
        if (search != null && !search.isEmpty()) {
            sql.append("AND (actor.name LIKE ? OR actor.email LIKE ?) ");
            String like = "%" + escapeLike(search) + "%";
            params.add(like);
            params.add(like);
        }
        sql.append("ORDER BY l.created_at DESC, l.log_id DESC LIMIT ?");
        params.add(limit);

        List<AuditEntry> entries = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    entries.add(new AuditEntry(
                        rs.getInt("log_id"),
                        rs.getInt("user_id"),
                        rs.getString("actor_name"),
                        rs.getString("actor_email"),
                        rs.getString("action"),
                        rs.getString("entity_type"),
                        rs.getInt("entity_id"),
                        rs.getString("target_name"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        }
        return entries;
    }

    // values for the filter dropdowns, taken from what is actually in the log
    public List<String> findDistinctActions() throws SQLException {
        return findDistinct("SELECT DISTINCT action FROM audit_log ORDER BY action");
    }

    public List<String> findDistinctEntityTypes() throws SQLException {
        return findDistinct("SELECT DISTINCT entity_type FROM audit_log ORDER BY entity_type");
    }

    public int countSince(int hours) throws SQLException {
        String sql = "SELECT COUNT(*) FROM audit_log WHERE created_at >= NOW() - INTERVAL ? HOUR";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, hours);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    private List<String> findDistinct(String sql) throws SQLException {
        List<String> values = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                values.add(rs.getString(1));
            }
        }
        return values;
    }

    // so a search for "50%" or "a_b" matches those characters literally
    static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
