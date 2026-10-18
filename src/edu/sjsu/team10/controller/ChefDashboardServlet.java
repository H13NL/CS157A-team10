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

import edu.sjsu.team10.dao.InventoryDAO;
import edu.sjsu.team10.model.Restaurant;
import edu.sjsu.team10.model.StockItem;

@WebServlet("/chef-dashboard")
public class ChefDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private InventoryDAO inventoryDAO = new InventoryDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        // must be logged in
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        // must be a chef, i.e. belong to a restaurant
        Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
        if (restaurant == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only chefs assigned to a restaurant can view this page.");
            return;
        }

        try {
            List<StockItem> inventory = inventoryDAO.findByRestaurant(restaurant.getRestaurantId());

            int inStockCount = 0;
            for (StockItem item : inventory) {
                if (item.isInStock()) {
                    inStockCount++;
                }
            }

            request.setAttribute("inventory", inventory);
            request.setAttribute("inStockCount", inStockCount);
        } catch (SQLException e) {
            throw new ServletException("Could not load inventory", e);
        }

        request.getRequestDispatcher("/WEB-INF/views/chef-dashboard.jsp")
               .forward(request, response);
    }
}
