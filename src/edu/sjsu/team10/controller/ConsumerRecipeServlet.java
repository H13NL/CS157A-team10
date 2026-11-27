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

import edu.sjsu.team10.dao.ConsumerRecipeDAO;
import edu.sjsu.team10.dao.UserDAO;
import edu.sjsu.team10.model.Recipe;
import edu.sjsu.team10.model.User;

@WebServlet("/consumer-recipes")
public class ConsumerRecipeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ConsumerRecipeDAO recipeDAO =
        new ConsumerRecipeDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = session == null
                ? null
                : (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if (!userDAO.isConsumer(user.getUserId())) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Only consumers can view this recipe library."
            );
            return;
        }

        try {
            Integer recipeId = parseId(request.getParameter("id"));

            if (recipeId == null) {
                List<Recipe> recipes =
                    recipeDAO.findPublicRecipes(user.getUserId());

                request.setAttribute("recipes", recipes);
                request.getRequestDispatcher(
                    "/WEB-INF/views/consumer-recipe-library.jsp"
                ).forward(request, response);
                return;
            }

            Recipe recipe = recipeDAO.findPublicById(
                recipeId,
                user.getUserId()
            );

            if (recipe == null) {
                response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Public recipe not found."
                );
                return;
            }

            request.setAttribute("recipe", recipe);
            request.getRequestDispatcher(
                "/WEB-INF/views/consumer-recipe-detail.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException(
                "Could not load consumer recipes", e
            );
        }
    }

    private Integer parseId(String value) {
        try {
            return value == null || value.trim().isEmpty()
                    ? null
                    : Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
