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
	
	public List<PantryItem> findByConsumer(int consumerId) throws SQLException {

	    String sql =
	        "SELECT i.ing_id, i.name, "
	      + "k.qty_avai, "
	      + "k.unit AS pantry_unit, "
	      + "i.unit AS ingredient_unit, "
	      + "k.last_update "
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
	                String unit = rs.getString("pantry_unit");

	                if (unit == null) {
	                    unit = rs.getString("ingredient_unit");
	                }

	                items.add(new PantryItem(
	                    rs.getInt("ing_id"),
	                    rs.getString("name"),
	                    rs.getBigDecimal("qty_avai"),
	                    unit,
	                    rs.getTimestamp("last_update")
	                ));
	            }
	        }
	    }
	    return items;
	}

    public List<Ingredient> findNotKept(int consumerId) throws SQLException {

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

    public boolean updateQuantity(int consumerId, int ingredientId, BigDecimal quantity) throws SQLException {

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

    public void addExisting(int consumerId, int ingredientId, BigDecimal quantity) throws SQLException {

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

    public boolean remove(int consumerId, int ingredientId) throws SQLException {

        String sql = "DELETE FROM keeps "
        		   + "WHERE consumer_id = ? AND ing_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, consumerId);
            ps.setInt(2, ingredientId);

            return ps.executeUpdate() == 1;
        }
    }
    
    public boolean consumeRecipe(int consumerId, int recipeId) throws SQLException {

        String requirementsSql =
            "SELECT ing_id, qty "
          + "FROM requires "
          + "WHERE recipe_id = ?";

        String decreaseSql =
            "UPDATE keeps "
          + "SET qty_avai = qty_avai - ?, "
          + "last_update = CURRENT_TIMESTAMP "
          + "WHERE consumer_id = ? "
          + "AND ing_id = ? "
          + "AND qty_avai >= ?";

        try (Connection con = DBConnection.getConnection()) {

            con.setAutoCommit(false);

            try {
                List<Integer> ingredientIds = new ArrayList<>();
                List<BigDecimal> quantities = new ArrayList<>();

                try (PreparedStatement ps = con.prepareStatement(requirementsSql)) {

                    ps.setInt(1, recipeId);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ingredientIds.add(rs.getInt("ing_id"));
                            quantities.add(rs.getBigDecimal("qty"));
                        }
                    }
                }

                // The recipe has no ingredients.
                if (ingredientIds.isEmpty()) {
                    con.rollback();
                    return false;
                }

                for (int i = 0; i < ingredientIds.size(); i++) {

                    BigDecimal requiredQuantity = quantities.get(i);
                    int ingredientId = ingredientIds.get(i);

                    try (PreparedStatement ps = con.prepareStatement(decreaseSql)) {

                        ps.setBigDecimal(1, requiredQuantity);
                        ps.setInt(2, consumerId);
                        ps.setInt(3, ingredientId);
                        ps.setBigDecimal(4, requiredQuantity);

                        // If zero rows update, the consumer lacks enough stock.
                        if (ps.executeUpdate() != 1) {
                            con.rollback();
                            return false;
                        }
                    }
                }
                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
