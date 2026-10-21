package edu.sjsu.team10.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.sjsu.team10.dao.RecipeDAO;
import edu.sjsu.team10.model.Ingredient;
import edu.sjsu.team10.model.Recipe;
import edu.sjsu.team10.model.RecipeInput;
import edu.sjsu.team10.model.User;

@WebServlet("/recipe-form")
public class RecipeFormServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RecipeDAO recipeDAO = new RecipeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = loggedInUser(request, response);
        if (user == null) return;
        try {
            Integer recipeId = parseId(request.getParameter("id"));
            Recipe recipe = recipeId == null ? null : recipeDAO.findById(recipeId, restaurantId(request));
            if (recipe != null && !recipe.isOwnedBy(user.getUserId())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only your private recipes can be edited.");
                return;
            }
            request.setAttribute("recipe", recipe);
            request.setAttribute("ingredients", recipeDAO.findIngredientCatalog());
            request.getRequestDispatcher("/WEB-INF/views/recipe-form.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Could not load recipe form", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = loggedInUser(request, response);
        if (user == null) return;

        String name = trim(request.getParameter("name"));
        String description = trim(request.getParameter("description"));
        Integer recipeId = parseId(request.getParameter("recipeId"));
        List<RecipeInput> inputs = new ArrayList<>();
        String error = validateInputs(request, inputs);

        if (name.isEmpty() || name.length() > 150) error = "Give the recipe a name up to 150 characters.";
        if (description.length() > 4000) error = "Keep the description under 4,000 characters.";
        if (recipeId != null && !canEdit(user, recipeId, request)) error = "Only your private recipes can be edited.";

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("formName", name);
            request.setAttribute("formDescription", description);
            request.setAttribute("ingredients", safeIngredients());
            request.getRequestDispatcher("/WEB-INF/views/recipe-form.jsp").forward(request, response);
            return;
        }

        try {
            if (recipeId == null) {
                recipeDAO.create(user.getUserId(), name, description, inputs);
            } else {
                recipeDAO.update(recipeId, user.getUserId(), name, description, inputs);
            }
            response.sendRedirect(request.getContextPath() + "/recipes");
        } catch (SQLException e) {
            throw new ServletException("Could not save recipe", e);
        }
    }

    private String validateInputs(HttpServletRequest request, List<RecipeInput> inputs) {
        String[] ids = request.getParameterValues("ingredientId");
        String[] names = request.getParameterValues("ingredientName");
        String[] quantities = request.getParameterValues("quantity");
        String[] units = request.getParameterValues("unit");
        if (quantities == null || quantities.length == 0) return "Add at least one ingredient.";
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < quantities.length; i++) {
            String idValue = valueAt(ids, i);
            String ingredientName = trim(valueAt(names, i));
            String unit = trim(valueAt(units, i));
            BigDecimal quantity;
            try { quantity = new BigDecimal(valueAt(quantities, i)); }
            catch (NumberFormatException e) { return "Every ingredient needs a valid quantity."; }
            if (quantity.signum() <= 0 || quantity.scale() > 2) return "Quantities must be positive and use at most two decimals.";
            Integer ingredientId = parseId(idValue);
            if (ingredientId == null && ingredientName.isEmpty()) continue;
            if (unit.isEmpty() || unit.length() > 20) return "Every ingredient needs a unit up to 20 characters.";
            if (ingredientName.length() > 100) return "Ingredient names must be 100 characters or fewer.";
            String key = ingredientId == null ? "name:" + ingredientName.toLowerCase() : "id:" + ingredientId;
            if (!seen.add(key)) return "Each ingredient can appear only once in a recipe.";
            inputs.add(new RecipeInput(ingredientId, ingredientName, quantity, unit));
        }
        return inputs.isEmpty() ? "Add at least one ingredient." : null;
    }

    private boolean canEdit(User user, int recipeId, HttpServletRequest request) throws ServletException {
        try {
            Recipe recipe = recipeDAO.findById(recipeId, restaurantId(request));
            return recipe != null && recipe.isOwnedBy(user.getUserId());
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private List<Ingredient> safeIngredients() throws ServletException {
        try { return recipeDAO.findIngredientCatalog(); }
        catch (SQLException e) { throw new ServletException(e); }
    }

    private User loggedInUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) response.sendRedirect(request.getContextPath() + "/login.jsp");
        return user;
    }

    private int restaurantId(HttpServletRequest request) throws ServletException {
        Object restaurant = request.getSession(false).getAttribute("restaurant");
        if (restaurant == null) throw new ServletException("No restaurant is assigned to this chef.");
        return ((edu.sjsu.team10.model.Restaurant) restaurant).getRestaurantId();
    }

    private String valueAt(String[] values, int index) { return values != null && index < values.length && values[index] != null ? values[index] : ""; }
    private String trim(String value) { return value == null ? "" : value.trim(); }
    private Integer parseId(String value) { try { return value == null || value.trim().isEmpty() ? null : Integer.valueOf(value); } catch (NumberFormatException e) { return null; } }
}
