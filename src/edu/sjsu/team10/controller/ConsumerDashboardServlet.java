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

import edu.sjsu.team10.dao.PantryDAO;
import edu.sjsu.team10.dao.ConsumerRecipeDAO;
import edu.sjsu.team10.dao.UserDAO;
import edu.sjsu.team10.model.PantryItem;
import edu.sjsu.team10.model.Recipe;
import edu.sjsu.team10.model.User;

@WebServlet("/consumer-dashboard")
public class ConsumerDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final PantryDAO pantryDAO = new PantryDAO();
    private final ConsumerRecipeDAO recipeDAO = new ConsumerRecipeDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if (!userDAO.isConsumer(user.getUserId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Only consumers can view this page.");
            return;
        }

        try {
            List<PantryItem> pantryItems = pantryDAO.findByConsumer(user.getUserId());

            int lowCount = 0;
            for (PantryItem item : pantryItems) {
                if (item.getQuantity() != null
                        && item.getQuantity().doubleValue() <= 2.0) {
                    lowCount++;
                }
            }

            request.setAttribute("pantryItems", pantryItems);
            request.setAttribute("pantryCount", pantryItems.size());
            request.setAttribute("lowPantryCount", lowCount);

            List<Recipe> recipes =
                recipeDAO.findPublicRecipes(user.getUserId());
            request.setAttribute("recipes", recipes);

            request.getRequestDispatcher(
                    "/WEB-INF/views/consumer-dashboard.jsp")
                    .forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Could not load consumer dashboard", e);
        }
    }
}
