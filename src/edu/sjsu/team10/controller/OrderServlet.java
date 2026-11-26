package edu.sjsu.team10.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.sjsu.team10.dao.OrderDAO;
import edu.sjsu.team10.model.User;
import edu.sjsu.team10.util.Forms;
import edu.sjsu.team10.util.RoleGuard;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User consumer = RoleGuard.require(request, response, "consumer");
        if (consumer == null) {
            return;
        }

        try {
            request.setAttribute("orderIngredients",
                orderDAO.findOrderableIngredients(consumer.getUserId()));
            request.getRequestDispatcher("/WEB-INF/views/orders.jsp")
                   .forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Could not load grocery order page", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        User consumer = RoleGuard.require(request, response, "consumer");
        if (consumer == null) {
            return;
        }

        Map<Integer, BigDecimal> items = new LinkedHashMap<>();
        String[] ingredientIds = request.getParameterValues("ingredientId");
        String[] quantities = request.getParameterValues("quantity");
        boolean invalid = ingredientIds == null || quantities == null
            || ingredientIds.length != quantities.length;

        if (!invalid) {
            Set<Integer> seen = new HashSet<>();
            for (int i = 0; i < ingredientIds.length; i++) {
                Integer ingredientId = Forms.parseId(ingredientIds[i]);
                BigDecimal quantity = Forms.parseAmount(quantities[i]);
                if (ingredientId == null || quantity == null
                        || quantity.signum() <= 0
                        || !seen.add(ingredientId)) {
                    invalid = true;
                    break;
                }
                items.put(ingredientId, quantity);
            }
        }

        if (invalid) {
            response.sendRedirect(request.getContextPath() + "/orders?msg=invalid");
            return;
        }
        if (items.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/orders?msg=empty");
            return;
        }

        try {
            int orderId = orderDAO.create(consumer.getUserId(), items);
            response.sendRedirect(request.getContextPath()
                + "/orders?msg=created&id=" + orderId);
        } catch (SQLException e) {
            throw new ServletException("Could not create grocery order", e);
        }
    }
}
