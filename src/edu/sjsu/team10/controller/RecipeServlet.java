package edu.sjsu.team10.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.sjsu.team10.dao.RecipeDAO;
import edu.sjsu.team10.model.Recipe;
import edu.sjsu.team10.model.Restaurant;
import edu.sjsu.team10.model.User;

@WebServlet("/recipes")
public class RecipeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RecipeDAO recipeDAO = new RecipeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        Restaurant restaurant = session == null ? null : (Restaurant) session.getAttribute("restaurant");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }
        String role = (String) session.getAttribute("role");
        boolean isChef = "chef".equals(role);
        boolean isConsumer = "consumer".equals(role);
        if (!isChef && !isConsumer) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Recipes are available to chef and consumer accounts.");
            return;
        }

        try {
            Integer recipeId = parseId(request.getParameter("id"));
            if (recipeId == null) {
                List<Recipe> recipes = isChef
                        ? recipeDAO.findVisible(user.getUserId(), restaurant.getRestaurantId())
                        : recipeDAO.findPublicForConsumer(user.getUserId());
                request.setAttribute("recipes", recipes);
                request.getRequestDispatcher("/WEB-INF/views/recipe-library.jsp").forward(request, response);
                return;
            }

            Recipe recipe = isChef
                    ? recipeDAO.findById(recipeId, restaurant.getRestaurantId())
                    : recipeDAO.findPublicByIdForConsumer(recipeId, user.getUserId());
            if (recipe == null || (!recipe.isPublic() && recipe.getCreatedBy() != user.getUserId())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Recipe not found.");
                return;
            }
            request.setAttribute("recipe", recipe);
            request.setAttribute("canEdit", isChef && recipe.isOwnedBy(user.getUserId()));
            request.setAttribute("canCopy", isChef && recipe.isPublic()
                    && !recipe.isOwnedBy(user.getUserId()));
            request.getRequestDispatcher("/WEB-INF/views/recipe-detail.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Could not load recipes", e);
        }
    }

    private Integer parseId(String value) {
        try { return value == null || value.trim().isEmpty() ? null : Integer.valueOf(value); }
        catch (NumberFormatException e) { return null; }
    }
}
