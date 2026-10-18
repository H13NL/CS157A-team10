package edu.sjsu.team10.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.sjsu.team10.model.Ingredient;
import edu.sjsu.team10.model.StockItem;

// Reads and writes a restaurant's shared inventory (the stock table).
// Every method takes the restaurant id so a chef can only touch their own restaurant's stock.
public class InventoryDAO {

    // all ingredients the restaurant stocks, alphabetical
    public List<StockItem> findByRestaurant(int restaurantId) throws SQLException {

        String sql = "SELECT i.ing_id, i.name, s.qty_avai, COALESCE(s.unit, i.unit) AS unit, s.last_update "
                   + "FROM stock s JOIN ingredient i ON s.ing_id = i.ing_id "
                   + "WHERE s.restaurant_id = ? "
                   + "ORDER BY i.name";

        List<StockItem> items = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {

            preparedStatement.setInt(1, restaurantId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    items.add(new StockItem(
                            rs.getInt("ing_id"),
                            rs.getString("name"),
                            rs.getBigDecimal("qty_avai"),
                            rs.getString("unit"),
                            rs.getTimestamp("last_update")
                        ));
                }
            }
        }

        return items;
    }

    // ingredients in the global list that this restaurant does not stock yet (for the "add" dropdown)
    public List<Ingredient> findNotStocked(int restaurantId) throws SQLException {

        String sql = "SELECT ing_id, name, unit FROM ingredient "
                   + "WHERE ing_id NOT IN (SELECT ing_id FROM stock WHERE restaurant_id = ?) "
                   + "ORDER BY name";

        List<Ingredient> ingredients = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {

            preparedStatement.setInt(1, restaurantId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    ingredients.add(new Ingredient(
                            rs.getInt("ing_id"),
                            rs.getString("name"),
                            rs.getString("unit")
                        ));
                }
            }
        }

        return ingredients;
    }

    // set a new quantity; returns false if the ingredient isn't in this restaurant's stock
    public boolean updateQuantity(int restaurantId, int ingId, BigDecimal qty) throws SQLException {

        String sql = "UPDATE stock SET qty_avai = ?, last_update = CURRENT_TIMESTAMP "
                   + "WHERE restaurant_id = ? AND ing_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {

            preparedStatement.setBigDecimal(1, qty);
            preparedStatement.setInt(2, restaurantId);
            preparedStatement.setInt(3, ingId);

            return preparedStatement.executeUpdate() == 1;
        }
    }

    // stock an ingredient that already exists in the global ingredient list.
    // Throws SQLIntegrityConstraintViolationException if the restaurant already stocks it.
    public void addExisting(int restaurantId, int ingId, BigDecimal qty) throws SQLException {

        String sql = "INSERT INTO stock (restaurant_id, ing_id, qty_avai, unit) "
                   + "SELECT ?, ing_id, ?, unit FROM ingredient WHERE ing_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {

            preparedStatement.setInt(1, restaurantId);
            preparedStatement.setBigDecimal(2, qty);
            preparedStatement.setInt(3, ingId);

            preparedStatement.executeUpdate();
        }
    }

    // stock an ingredient by name, creating it in the global ingredient list if it doesn't exist.
    // Both inserts run in one transaction so a failure leaves neither row behind.
    // Throws SQLIntegrityConstraintViolationException if the restaurant already stocks it.
    public void addByName(int restaurantId, String name, String unit, BigDecimal qty) throws SQLException {

        String findSql = "SELECT ing_id, unit FROM ingredient WHERE name = ?";
        String insertIngredientSql = "INSERT INTO ingredient (name, unit) VALUES (?, ?)";
        String insertStockSql = "INSERT INTO stock (restaurant_id, ing_id, qty_avai, unit) VALUES (?, ?, ?, ?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                int ingId;
                String stockUnit;

                try (PreparedStatement find = con.prepareStatement(findSql)) {
                    find.setString(1, name);

                    try (ResultSet rs = find.executeQuery()) {
                        if (rs.next()) {
                            // reuse the existing ingredient and its unit
                            ingId = rs.getInt("ing_id");
                            stockUnit = rs.getString("unit");
                        } else {
                            ingId = -1;
                            stockUnit = unit;
                        }
                    }
                }

                if (ingId == -1) {
                    try (PreparedStatement insert = con.prepareStatement(insertIngredientSql, Statement.RETURN_GENERATED_KEYS)) {
                        insert.setString(1, name);
                        insert.setString(2, unit);
                        insert.executeUpdate();

                        try (ResultSet keys = insert.getGeneratedKeys()) {
                            keys.next();
                            ingId = keys.getInt(1);
                        }
                    }
                }

                try (PreparedStatement insert = con.prepareStatement(insertStockSql)) {
                    insert.setInt(1, restaurantId);
                    insert.setInt(2, ingId);
                    insert.setBigDecimal(3, qty);
                    insert.setString(4, stockUnit);
                    insert.executeUpdate();
                }

                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    // stop stocking an ingredient (the ingredient itself stays in the global list)
    public boolean remove(int restaurantId, int ingId) throws SQLException {

        String sql = "DELETE FROM stock WHERE restaurant_id = ? AND ing_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {

            preparedStatement.setInt(1, restaurantId);
            preparedStatement.setInt(2, ingId);

            return preparedStatement.executeUpdate() == 1;
        }
    }
}
