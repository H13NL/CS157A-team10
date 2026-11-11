package edu.sjsu.team10.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.sjsu.team10.model.Ingredient;
import edu.sjsu.team10.model.PantryItem;

public class PantryDAO {

    public List<PantryItem> findByConsumer(int consumerId)
            throws SQLException {

        String sql =
            "SELECT i.ing_id, i.name, k.qty_avai, "
          + "COALESCE(k.unit, i.unit) AS unit, k.last_update "
          + "FROM keeps k "
          + "JOIN ingredient i ON k.ing_id = i.ing_id "
          + "WHERE k.consumer_id = ? "
          + "ORDER BY i.name";

        List<PantryItem> items = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, consumerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new PantryItem(
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

    public List<Ingredient> findNotKept(int consumerId)
            throws SQLException {

        String sql =
            "SELECT i.ing_id, i.name, i.unit "
          + "FROM ingredient i "
          + "WHERE NOT EXISTS ("
          + "    SELECT 1 FROM keeps k "
          + "    WHERE k.consumer_id = ? AND k.ing_id = i.ing_id"
          + ") ORDER BY i.name";

        List<Ingredient> ingredients = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, consumerId);

            try (ResultSet rs = ps.executeQuery()) {
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

    public boolean updateQuantity(int consumerId, int ingredientId,
                                  BigDecimal quantity)
            throws SQLException {

        String sql =
            "UPDATE keeps SET qty_avai = ?, "
          + "last_update = CURRENT_TIMESTAMP "
          + "WHERE consumer_id = ? AND ing_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setBigDecimal(1, quantity);
            ps.setInt(2, consumerId);
            ps.setInt(3, ingredientId);

            return ps.executeUpdate() == 1;
        }
    }

    public void addExisting(int consumerId, int ingredientId,
                            BigDecimal quantity)
            throws SQLException {

        String sql =
            "INSERT INTO keeps (consumer_id, ing_id, qty_avai, unit) "
          + "SELECT ?, ing_id, ?, unit "
          + "FROM ingredient WHERE ing_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, consumerId);
            ps.setBigDecimal(2, quantity);
            ps.setInt(3, ingredientId);
            ps.executeUpdate();
        }
    }

    public boolean remove(int consumerId, int ingredientId)
            throws SQLException {

        String sql =
            "DELETE FROM keeps "
          + "WHERE consumer_id = ? AND ing_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, consumerId);
            ps.setInt(2, ingredientId);

            return ps.executeUpdate() == 1;
        }
    }
}
