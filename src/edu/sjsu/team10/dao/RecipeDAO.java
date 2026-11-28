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
import edu.sjsu.team10.model.Recipe;
import edu.sjsu.team10.model.RecipeIngredient;
import edu.sjsu.team10.model.RecipeInput;

public class RecipeDAO {

    public enum MakeResult {
        MADE,
        NOT_MAKEABLE,
        NOT_FOUND
    }

    public List<Recipe> findVisible(int userId, int restaurantId) throws SQLException {
        String sql = "SELECT r.recipe_id, r.name, r.description, r.is_public, "
                   + "r.created_by, r.created_at, u.name AS creator_name "
                   + "FROM recipe r JOIN users u ON u.user_id = r.created_by "
                   + "WHERE r.is_public = TRUE OR r.created_by = ? "
                   + "ORDER BY r.is_public DESC, r.created_at DESC, r.name";

        List<Recipe> recipes = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Recipe recipe = readRecipe(rs);
                    loadIngredients(con, recipe, restaurantId);
                    recipes.add(recipe);
                }
            }
        }
        return recipes;
    }

    public List<Recipe> findPublicForConsumer(int consumerId) throws SQLException {
        String sql = "SELECT r.recipe_id, r.name, r.description, r.is_public, "
                   + "r.created_by, r.created_at, u.name AS creator_name "
                   + "FROM recipe r JOIN users u ON u.user_id = r.created_by "
                   + "WHERE r.is_public = TRUE "
                   + "ORDER BY r.created_at DESC, r.name";

        List<Recipe> recipes = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Recipe recipe = readRecipe(rs);
                loadConsumerIngredients(con, recipe, consumerId);
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    public Recipe findById(int recipeId, int restaurantId) throws SQLException {
        String sql = "SELECT r.recipe_id, r.name, r.description, r.is_public, "
                   + "r.created_by, r.created_at, u.name AS creator_name "
                   + "FROM recipe r JOIN users u ON u.user_id = r.created_by "
                   + "WHERE r.recipe_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, recipeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Recipe recipe = readRecipe(rs);
                loadIngredients(con, recipe, restaurantId);
                return recipe;
            }
        }
    }

    public Recipe findPublicByIdForConsumer(int recipeId, int consumerId) throws SQLException {
        String sql = "SELECT r.recipe_id, r.name, r.description, r.is_public, "
                   + "r.created_by, r.created_at, u.name AS creator_name "
                   + "FROM recipe r JOIN users u ON u.user_id = r.created_by "
                   + "WHERE r.recipe_id = ? AND r.is_public = TRUE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, recipeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Recipe recipe = readRecipe(rs);
                loadConsumerIngredients(con, recipe, consumerId);
                return recipe;
            }
        }
    }

    public List<Ingredient> findIngredientCatalog() throws SQLException {
        String sql = "SELECT ing_id, name, unit FROM ingredient ORDER BY name";
        List<Ingredient> ingredients = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ingredients.add(new Ingredient(rs.getInt("ing_id"), rs.getString("name"), rs.getString("unit")));
            }
        }
        return ingredients;
    }

    public int create(int userId, String name, String description, boolean isPublic,
                      List<RecipeInput> inputs) throws SQLException {
        return save(null, userId, name, description, isPublic, inputs);
    }

    public void update(int recipeId, int userId, String name, String description, boolean isPublic,
                       List<RecipeInput> inputs) throws SQLException {
        save(recipeId, userId, name, description, isPublic, inputs);
    }

    public boolean deleteOwned(int recipeId, int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String ownerSql = "SELECT 1 FROM recipe "
                                + "WHERE recipe_id = ? AND created_by = ? FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(ownerSql)) {
                    ps.setInt(1, recipeId);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return false;
                        }
                    }
                }

                // Preserve old restock-request records while removing their link
                // to a recipe that will no longer exist.
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE restock_request SET recipe_id = NULL WHERE recipe_id = ?")) {
                    ps.setInt(1, recipeId);
                    ps.executeUpdate();
                }

                int deleted;
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM recipe WHERE recipe_id = ? AND created_by = ?")) {
                    ps.setInt(1, recipeId);
                    ps.setInt(2, userId);
                    deleted = ps.executeUpdate();
                }
                if (deleted != 1) throw new SQLException("Recipe could not be deleted");
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public MakeResult make(int recipeId, int userId, int restaurantId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String visibleSql = "SELECT COUNT(req.ing_id) AS ingredient_count "
                                  + "FROM recipe r "
                                  + "LEFT JOIN requires req ON req.recipe_id = r.recipe_id "
                                  + "WHERE r.recipe_id = ? AND (r.is_public = TRUE OR r.created_by = ?) "
                                  + "GROUP BY r.recipe_id";
                int ingredientCount;
                try (PreparedStatement ps = con.prepareStatement(visibleSql)) {
                    ps.setInt(1, recipeId);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            con.rollback();
                            return MakeResult.NOT_FOUND;
                        }
                        ingredientCount = rs.getInt("ingredient_count");
                    }
                }

                if (ingredientCount == 0) {
                    con.rollback();
                    return MakeResult.NOT_MAKEABLE;
                }

                String deductSql = "UPDATE stock s "
                                 + "JOIN requires req ON req.ing_id = s.ing_id "
                                 + "SET s.qty_avai = s.qty_avai - req.qty, "
                                 + "s.last_update = CURRENT_TIMESTAMP "
                                 + "WHERE s.restaurant_id = ? AND req.recipe_id = ? "
                                 + "AND s.qty_avai >= req.qty "
                                 + "AND COALESCE(req.unit, '') <> '' "
                                 + "AND LOWER(COALESCE(s.unit, '')) = LOWER(COALESCE(req.unit, ''))";
                int deducted;
                try (PreparedStatement ps = con.prepareStatement(deductSql)) {
                    ps.setInt(1, restaurantId);
                    ps.setInt(2, recipeId);
                    deducted = ps.executeUpdate();
                }

                if (deducted != ingredientCount) {
                    con.rollback();
                    return MakeResult.NOT_MAKEABLE;
                }

                con.commit();
                return MakeResult.MADE;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public int copyPrivate(int sourceRecipeId, int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String recipeSql = "SELECT name, description FROM recipe WHERE recipe_id = ? AND is_public = TRUE";
                String name;
                String description;
                try (PreparedStatement ps = con.prepareStatement(recipeSql)) {
                    ps.setInt(1, sourceRecipeId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Recipe not found");
                        name = rs.getString("name") + " (private copy)";
                        description = rs.getString("description");
                    }
                }

                int newId = insertRecipe(con, userId, name, description, false);
                String copySql = "INSERT INTO requires (recipe_id, ing_id, qty, unit) "
                               + "SELECT ?, ing_id, qty, unit FROM requires WHERE recipe_id = ?";
                try (PreparedStatement ps = con.prepareStatement(copySql)) {
                    ps.setInt(1, newId);
                    ps.setInt(2, sourceRecipeId);
                    ps.executeUpdate();
                }
                con.commit();
                return newId;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private int save(Integer recipeId, int userId, String name, String description, boolean isPublic,
                     List<RecipeInput> inputs) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int savedId;
                if (recipeId == null) {
                    savedId = insertRecipe(con, userId, name, description, isPublic);
                } else {
                    String updateSql = "UPDATE recipe SET name = ?, description = ?, is_public = ? "
                                     + "WHERE recipe_id = ? AND created_by = ?";
                    try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                        ps.setString(1, name);
                        ps.setString(2, description);
                        ps.setBoolean(3, isPublic);
                        ps.setInt(4, recipeId);
                        ps.setInt(5, userId);
                        if (ps.executeUpdate() != 1) throw new SQLException("Recipe cannot be edited");
                    }
                    savedId = recipeId;
                    try (PreparedStatement ps = con.prepareStatement("DELETE FROM requires WHERE recipe_id = ?")) {
                        ps.setInt(1, savedId);
                        ps.executeUpdate();
                    }
                }

                for (RecipeInput input : inputs) {
                    int ingredientId = resolveIngredient(con, input);
                    String unit = input.getUnit();
                    if (input.getIngredientId() != null) {
                        unit = findIngredientUnit(con, ingredientId);
                    }
                    String insertSql = "INSERT INTO requires (recipe_id, ing_id, qty, unit) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                        ps.setInt(1, savedId);
                        ps.setInt(2, ingredientId);
                        ps.setBigDecimal(3, input.getQuantity());
                        ps.setString(4, unit);
                        ps.executeUpdate();
                    }
                }
                con.commit();
                return savedId;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private int insertRecipe(Connection con, int userId, String name, String description,
                             boolean isPublic) throws SQLException {
        String sql = "INSERT INTO recipe (name, description, is_public, created_by) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setBoolean(3, isPublic);
            ps.setInt(4, userId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Recipe id was not generated");
                return keys.getInt(1);
            }
        }
    }

    private int resolveIngredient(Connection con, RecipeInput input) throws SQLException {
        if (input.getIngredientId() != null) {
            try (PreparedStatement ps = con.prepareStatement("SELECT ing_id FROM ingredient WHERE ing_id = ?")) {
                ps.setInt(1, input.getIngredientId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            throw new SQLException("Ingredient not found");
        }

        try (PreparedStatement ps = con.prepareStatement("SELECT ing_id FROM ingredient WHERE name = ?")) {
            ps.setString(1, input.getIngredientName());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ingredient (name, unit) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, input.getIngredientName());
            ps.setString(2, input.getUnit());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Ingredient id was not generated");
                return keys.getInt(1);
            }
        }
    }

    private String findIngredientUnit(Connection con, int ingredientId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT unit FROM ingredient WHERE ing_id = ?")) {
            ps.setInt(1, ingredientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("unit");
            }
        }
        throw new SQLException("Ingredient not found");
    }
    private Recipe readRecipe(ResultSet rs) throws SQLException {
        return new Recipe(rs.getInt("recipe_id"), rs.getString("name"),
                rs.getString("description"), rs.getBoolean("is_public"),
                rs.getInt("created_by"), rs.getString("creator_name"),
                rs.getTimestamp("created_at"));
    }

    private void loadIngredients(Connection con, Recipe recipe, int restaurantId) throws SQLException {
        String sql = "SELECT i.ing_id, i.name, r.qty, r.unit, "
                   + "COALESCE(s.qty_avai, 0) AS available_qty, "
                   + "COALESCE(s.unit, i.unit) AS available_unit "
                   + "FROM requires r JOIN ingredient i ON i.ing_id = r.ing_id "
                   + "LEFT JOIN stock s ON s.ing_id = r.ing_id AND s.restaurant_id = ? "
                   + "WHERE r.recipe_id = ? ORDER BY i.name";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setInt(2, recipe.getRecipeId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recipe.addIngredient(new RecipeIngredient(
                            rs.getInt("ing_id"), rs.getString("name"),
                            rs.getBigDecimal("qty"), rs.getString("unit"),
                            rs.getBigDecimal("available_qty"), rs.getString("available_unit")));
                }
            }
        }
    }

    private void loadConsumerIngredients(Connection con, Recipe recipe, int consumerId)
            throws SQLException {
        String sql = "SELECT i.ing_id, i.name, r.qty, r.unit, "
                   + "COALESCE(k.qty_avai, 0) AS available_qty, "
                   + "COALESCE(k.unit, i.unit) AS available_unit "
                   + "FROM requires r JOIN ingredient i ON i.ing_id = r.ing_id "
                   + "LEFT JOIN keeps k ON k.ing_id = r.ing_id AND k.consumer_id = ? "
                   + "WHERE r.recipe_id = ? ORDER BY i.name";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, consumerId);
            ps.setInt(2, recipe.getRecipeId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    recipe.addIngredient(new RecipeIngredient(
                            rs.getInt("ing_id"), rs.getString("name"),
                            rs.getBigDecimal("qty"), rs.getString("unit"),
                            rs.getBigDecimal("available_qty"), rs.getString("available_unit")));
                }
            }
        }
    }
}
