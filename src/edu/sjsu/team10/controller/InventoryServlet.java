package edu.sjsu.team10.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.sjsu.team10.dao.InventoryDAO;
import edu.sjsu.team10.model.Restaurant;

// View and update the logged-in chef's restaurant inventory.
// The restaurant always comes from the session, never from the form,
// so a chef can't edit another restaurant's stock.
@WebServlet("/inventory")
public class InventoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // largest value DECIMAL(10,2) can hold
    private static final BigDecimal MAX_QTY = new BigDecimal("99999999.99");

    private InventoryDAO inventoryDAO = new InventoryDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        Restaurant restaurant = requireChef(request, response);
        if (restaurant == null) {
            return;
        }

        try {
            int restaurantId = restaurant.getRestaurantId();
            request.setAttribute("inventory", inventoryDAO.findByRestaurant(restaurantId));
            request.setAttribute("availableIngredients", inventoryDAO.findNotStocked(restaurantId));
        } catch (SQLException e) {
            throw new ServletException("Could not load inventory", e);
        }

        request.getRequestDispatcher("/WEB-INF/views/inventory.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        Restaurant restaurant = requireChef(request, response);
        if (restaurant == null) {
            return;
        }

        int restaurantId = restaurant.getRestaurantId();
        String action = request.getParameter("action");
        String result;

        try {
            if ("update".equals(action)) {
                BigDecimal qty = parseQuantity(request.getParameter("qty"));
                Integer ingId = parseId(request.getParameter("ingId"));

                if (qty == null || ingId == null) {
                    result = "invalid";
                } else if (inventoryDAO.updateQuantity(restaurantId, ingId, qty)) {
                    result = "updated";
                } else {
                    result = "notfound";
                }

            } else if ("add".equals(action)) {
                BigDecimal qty = parseQuantity(request.getParameter("qty"));
                Integer ingId = parseId(request.getParameter("ingId"));
                String name = trim(request.getParameter("newName"));
                String unit = trim(request.getParameter("newUnit"));

                if (qty == null) {
                    result = "invalid";
                } else if (!name.isEmpty()) {
                    // a typed name wins over the dropdown
                    if (name.length() > 100 || unit.length() > 20) {
                        result = "invalid";
                    } else {
                        inventoryDAO.addByName(restaurantId, name, unit.isEmpty() ? null : unit, qty);
                        result = "added";
                    }
                } else if (ingId != null) {
                    inventoryDAO.addExisting(restaurantId, ingId, qty);
                    result = "added";
                } else {
                    result = "invalid";
                }

            } else if ("remove".equals(action)) {
                Integer ingId = parseId(request.getParameter("ingId"));

                if (ingId != null && inventoryDAO.remove(restaurantId, ingId)) {
                    result = "removed";
                } else {
                    result = "notfound";
                }

            } else {
                result = "invalid";
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            // primary key (restaurant_id, ing_id) already exists
            result = "duplicate";
        } catch (SQLException e) {
            throw new ServletException("Could not update inventory", e);
        }

        // redirect after POST so refreshing the page doesn't resubmit the form
        response.sendRedirect(request.getContextPath() + "/inventory?msg=" + result);
    }

    // returns the chef's restaurant, or sends a redirect/error and returns null
    private Restaurant requireChef(HttpServletRequest request,
                                   HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return null;
        }

        Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
        if (restaurant == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only chefs assigned to a restaurant can manage inventory.");
        }
        return restaurant;
    }

    // a non-negative quantity with at most 2 decimals, or null if invalid
    private BigDecimal parseQuantity(String value) {
        try {
            BigDecimal qty = new BigDecimal(trim(value)).setScale(2, RoundingMode.HALF_UP);
            if (qty.signum() < 0 || qty.compareTo(MAX_QTY) > 0) {
                return null;
            }
            return qty;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseId(String value) {
        try {
            return Integer.valueOf(trim(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
