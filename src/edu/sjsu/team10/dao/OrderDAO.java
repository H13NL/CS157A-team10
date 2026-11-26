package edu.sjsu.team10.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.sjsu.team10.model.OrderIngredient;

public class OrderDAO {

    // All shared ingredients are orderable. The pantry join lets the page label
    // which items the logged-in consumer already keeps and their current amount.
    public List<OrderIngredient> findOrderableIngredients(int consumerId)
            throws SQLException {

        String sql =
            "SELECT i.ing_id, i.name, i.unit, k.qty_avai, "
          + "       CASE WHEN k.ing_id IS NULL THEN FALSE ELSE TRUE END AS in_pantry "
          + "FROM ingredient i "
          + "LEFT JOIN keeps k ON k.ing_id = i.ing_id AND k.consumer_id = ? "
          + "ORDER BY i.name";

        List<OrderIngredient> ingredients = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, consumerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ingredients.add(new OrderIngredient(
                        rs.getInt("ing_id"),
                        rs.getString("name"),
                        rs.getString("unit"),
                        rs.getBoolean("in_pantry"),
                        rs.getBigDecimal("qty_avai")
                    ));
                }
            }
        }
        return ingredients;
    }

    // Creates the order, its ingredient lines, and the resulting pantry increase
    // atomically so they can never get out of sync.
    public int create(int consumerId, Map<Integer, BigDecimal> items)
            throws SQLException {

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int orderId;
                String orderSql =
                    "INSERT INTO ogs_order (consumer_id, status) VALUES (?, 'pending')";
                try (PreparedStatement ps = con.prepareStatement(
                        orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, consumerId);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Order id was not generated");
                        }
                        orderId = keys.getInt(1);
                    }
                }

                String itemSql =
                    "INSERT INTO includes (order_id, ing_id, qty_ordered) "
                  + "SELECT ?, ing_id, ? FROM ingredient WHERE ing_id = ?";
                String updatePantrySql =
                    "UPDATE keeps SET qty_avai = qty_avai + ?, "
                  + "last_update = CURRENT_TIMESTAMP "
                  + "WHERE consumer_id = ? AND ing_id = ?";
                String insertPantrySql =
                    "INSERT INTO keeps (consumer_id, ing_id, qty_avai, unit) "
                  + "SELECT ?, ing_id, ?, unit FROM ingredient WHERE ing_id = ?";

                try (PreparedStatement itemStatement = con.prepareStatement(itemSql);
                     PreparedStatement updatePantry = con.prepareStatement(updatePantrySql);
                     PreparedStatement insertPantry = con.prepareStatement(insertPantrySql)) {
                    for (Map.Entry<Integer, BigDecimal> item : items.entrySet()) {
                        itemStatement.setInt(1, orderId);
                        itemStatement.setBigDecimal(2, item.getValue());
                        itemStatement.setInt(3, item.getKey());
                        if (itemStatement.executeUpdate() != 1) {
                            throw new SQLException("An ordered ingredient was not found");
                        }

                        updatePantry.setBigDecimal(1, item.getValue());
                        updatePantry.setInt(2, consumerId);
                        updatePantry.setInt(3, item.getKey());
                        if (updatePantry.executeUpdate() == 0) {
                            insertPantry.setInt(1, consumerId);
                            insertPantry.setBigDecimal(2, item.getValue());
                            insertPantry.setInt(3, item.getKey());
                            if (insertPantry.executeUpdate() != 1) {
                                throw new SQLException("The ordered ingredient could not be added to the pantry");
                            }
                        }
                    }
                }

                con.commit();
                return orderId;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // The connection is closing; preserve the original result.
                }
            }
        }
    }
}
