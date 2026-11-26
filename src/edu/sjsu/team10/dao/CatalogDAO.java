package edu.sjsu.team10.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.sjsu.team10.model.CatalogItem;
import edu.sjsu.team10.model.Ingredient;

// A restocker's catalog: which ingredients they supply, at what price, and how much they have
// (the Supplies relationship / supplies table).
// Every method takes the restocker's id, so a restocker can only see and change their own catalog.
// Each change is audit-logged in the same transaction.
public class CatalogDAO {

    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    // the restocker's catalog, alphabetical
    public List<CatalogItem> findByRestocker(int restockerId) throws SQLException {
        String sql = "SELECT i.ing_id, i.name, i.unit, s.price, s.qty "
                   + "FROM supplies s JOIN ingredient i ON i.ing_id = s.ing_id "
                   + "WHERE s.restocker_id = ? ORDER BY i.name";

        List<CatalogItem> items = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, restockerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new CatalogItem(
                        rs.getInt("ing_id"),
                        rs.getString("name"),
                        rs.getString("unit"),
                        rs.getBigDecimal("price"),
                        rs.getBigDecimal("qty")
                    ));
                }
            }
        }
        return items;
    }

    // ingredients in the shared list that aren't in this catalog yet (for the "add" dropdown)
    public List<Ingredient> findNotInCatalog(int restockerId) throws SQLException {
        String sql = "SELECT ing_id, name, unit FROM ingredient "
                   + "WHERE ing_id NOT IN (SELECT ing_id FROM supplies WHERE restocker_id = ?) "
                   + "ORDER BY name";

        List<Ingredient> ingredients = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, restockerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ingredients.add(new Ingredient(rs.getInt("ing_id"), rs.getString("name"), rs.getString("unit")));
                }
            }
        }
        return ingredients;
    }

    // Add an ingredient to the catalog. If name is given, the ingredient is looked up by name
    // and created in the shared list when it doesn't exist yet; otherwise ingId is used.
    // Throws SQLIntegrityConstraintViolationException if it's already in the catalog.
    // Returns false if ingId doesn't exist.
    public boolean add(int restockerId, Integer ingId, String name, String unit,
                       BigDecimal price, BigDecimal qty) throws SQLException {

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int id;
                if (name != null && !name.isEmpty()) {
                    id = findOrCreateIngredient(con, name, unit);
                } else if (ingId != null && ingredientExists(con, ingId)) {
                    id = ingId;
                } else {
                    con.rollback();
                    return false;
                }

                String sql = "INSERT INTO supplies (restocker_id, ing_id, price, qty) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, restockerId);
                    ps.setInt(2, id);
                    ps.setBigDecimal(3, price);
                    ps.setBigDecimal(4, qty);
                    ps.executeUpdate();
                }

                auditLogDAO.log(con, restockerId, AuditLogDAO.CATALOG_ADD, "ingredient", id);
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // change price and available quantity; returns false if the item isn't in this catalog
    public boolean update(int restockerId, int ingId, BigDecimal price, BigDecimal qty) throws SQLException {
        String sql = "UPDATE supplies SET price = ?, qty = ? WHERE restocker_id = ? AND ing_id = ?";
        return auditLogDAO.updateAndLog(restockerId, AuditLogDAO.CATALOG_EDIT, "ingredient", ingId,
                                        sql, price, qty, restockerId, ingId);
    }

    // drop an item from the catalog; the ingredient itself stays in the shared list
    public boolean remove(int restockerId, int ingId) throws SQLException {
        String sql = "DELETE FROM supplies WHERE restocker_id = ? AND ing_id = ?";
        return auditLogDAO.updateAndLog(restockerId, AuditLogDAO.CATALOG_REMOVE, "ingredient", ingId,
                                        sql, restockerId, ingId);
    }

    private boolean ingredientExists(Connection con, int ingId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM ingredient WHERE ing_id = ?")) {
            ps.setInt(1, ingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ingredient names are unique, so reuse an existing one instead of adding a duplicate
    private int findOrCreateIngredient(Connection con, String name, String unit) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT ing_id FROM ingredient WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        String sql = "INSERT INTO ingredient (name, unit) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, unit == null || unit.isEmpty() ? null : unit);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }
}
