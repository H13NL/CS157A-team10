package edu.sjsu.team10.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import edu.sjsu.team10.model.Recipe;
import edu.sjsu.team10.model.RecipeIngredient;

public class ConsumerRecipeDAO {

    public boolean deletePrivateRecipe(int consumerId, int recipeId) throws SQLException {

        String deleteIngredientsSql =
            "DELETE FROM requires WHERE recipe_id = ?";

        String deleteRecipeSql =
            "DELETE FROM recipe "
          + "WHERE recipe_id = ? "
          + "AND created_by = ? "
          + "AND is_public = FALSE";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                try (PreparedStatement ps = con.prepareStatement(deleteIngredientsSql)) {
                    ps.setInt(1, recipeId);
                    ps.executeUpdate();
                }

                int deleted;
                try (PreparedStatement ps = con.prepareStatement(deleteRecipeSql)) {
                    ps.setInt(1, recipeId);
                    ps.setInt(2, consumerId);
                    deleted = ps.executeUpdate();
                }

                if (deleted != 1) {
                    con.rollback();
                    return false;
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<Recipe> findPublicRecipes(int consumerId) throws SQLException {

    	String sql =
    		    "SELECT r.recipe_id, r.name, r.description, r.is_public, "
    		  + "r.created_by, r.created_at, u.name AS creator_name "
    		  + "FROM recipe r "
    		  + "JOIN users u ON u.user_id = r.created_by "
    		  + "WHERE r.is_public = TRUE "
    		  + "OR r.created_by = ? "
    		  + "ORDER BY r.is_public DESC, r.created_at DESC, r.name";

        List<Recipe> recipes = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
        	
        	ps.setInt(1, consumerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Recipe recipe = readRecipe(rs);
                    loadIngredients(con, recipe, consumerId);
                    recipes.add(recipe);
                }
            }
        }

        return recipes;
    }

    public Recipe findPublicById(int recipeId, int consumerId) throws SQLException {

        String sql =
              "SELECT r.recipe_id, r.name, r.description, r.is_public, "
            + "r.created_by, r.created_at, u.name AS creator_name "
            + "FROM recipe r "
            + "JOIN users u ON u.user_id = r.created_by "
            + "WHERE r.recipe_id = ? "
            + "AND (r.is_public = TRUE OR r.created_by = ?)";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, recipeId);
            ps.setInt(2, consumerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Recipe recipe = readRecipe(rs);
                loadIngredients(con, recipe, consumerId);
                return recipe;
            }
        }
    }

    private void loadIngredients(Connection con, Recipe recipe, int consumerId) throws SQLException {

    	String sql =
    		    "SELECT i.ing_id, i.name, req.qty, req.unit, "
    		  + "k.qty_avai AS available_qty, "
    		  + "k.unit AS available_unit, "
    		  + "i.unit AS ingredient_unit "
    		  + "FROM requires req "
    		  + "JOIN ingredient i ON i.ing_id = req.ing_id "
    		  + "LEFT JOIN keeps k ON k.ing_id = req.ing_id "
    		  + "AND k.consumer_id = ? "
    		  + "WHERE req.recipe_id = ? "
    		  + "ORDER BY i.name";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, consumerId);
            ps.setInt(2, recipe.getRecipeId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                	BigDecimal availableQuantity = rs.getBigDecimal("available_qty");

                	if (availableQuantity == null) {
                		availableQuantity = BigDecimal.ZERO;
                	}

                	String availableUnit = rs.getString("available_unit");

                	if (availableUnit == null) {
                		availableUnit = rs.getString("ingredient_unit");
                	}

                	recipe.addIngredient(new RecipeIngredient(
                		rs.getInt("ing_id"),
                		rs.getString("name"),
                		rs.getBigDecimal("qty"),
                		rs.getString("unit"),
                		availableQuantity,
                		availableUnit
                	));
                }
            }
        }
    }

    private Recipe readRecipe(ResultSet rs) throws SQLException {
        return new Recipe(
            rs.getInt("recipe_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getBoolean("is_public"),
            rs.getInt("created_by"),
            rs.getString("creator_name"),
            rs.getTimestamp("created_at")
        );
    }
}
