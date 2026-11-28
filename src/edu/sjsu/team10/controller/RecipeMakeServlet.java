package edu.sjsu.team10.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.sjsu.team10.dao.RecipeDAO;
import edu.sjsu.team10.dao.RecipeDAO.MakeResult;
import edu.sjsu.team10.model.Restaurant;
import edu.sjsu.team10.model.User;
import edu.sjsu.team10.util.RoleGuard;

@WebServlet("/recipe-make")
public class RecipeMakeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RecipeDAO recipeDAO = new RecipeDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = RoleGuard.require(request, response, "chef");
        if (user == null) return;

        HttpSession session = request.getSession(false);
        Restaurant restaurant = session == null
                ? null : (Restaurant) session.getAttribute("restaurant");
        if (restaurant == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "A restaurant is required to make a recipe.");
            return;
        }

        int recipeId;
        try {
            recipeId = Integer.parseInt(request.getParameter("recipeId"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid recipe.");
            return;
        }

        try {
            MakeResult result = recipeDAO.make(recipeId, user.getUserId(),
                    restaurant.getRestaurantId());
            if (result == MakeResult.NOT_FOUND) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Recipe not found.");
                return;
            }

            String message = result == MakeResult.MADE ? "made" : "not-makeable";
            String returnTo = request.getParameter("returnTo");
            if ("detail".equals(returnTo)) {
                response.sendRedirect(request.getContextPath() + "/recipes?id="
                        + recipeId + "&msg=" + message);
            } else if ("dashboard".equals(returnTo)) {
                response.sendRedirect(request.getContextPath()
                        + "/chef-dashboard?msg=" + message);
            } else {
                response.sendRedirect(request.getContextPath() + "/recipes?msg=" + message);
            }
        } catch (SQLException e) {
            throw new ServletException("Could not make recipe", e);
        }
    }
}
