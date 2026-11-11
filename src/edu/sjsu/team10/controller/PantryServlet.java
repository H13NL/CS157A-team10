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

import edu.sjsu.team10.dao.PantryDAO;
import edu.sjsu.team10.dao.UserDAO;
import edu.sjsu.team10.model.User;

@WebServlet("/pantry")
public class PantryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final BigDecimal MAX_QTY =
        new BigDecimal("99999999.99");

    private final PantryDAO pantryDAO = new PantryDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User user = requireConsumer(request, response);
        if (user == null) {
            return;
        }

        try {
            int consumerId = user.getUserId();
            request.setAttribute("pantryItems",
                pantryDAO.findByConsumer(consumerId));
            request.setAttribute("availableIngredients",
                pantryDAO.findNotKept(consumerId));

            request.getRequestDispatcher(
                "/WEB-INF/views/pantry.jsp"
            ).forward(request, response);

        } catch (SQLException e) {
            throw new ServletException("Could not load pantry", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        User user = requireConsumer(request, response);
        if (user == null) {
            return;
        }

        int consumerId = user.getUserId();
        String action = request.getParameter("action");
        String result;

        try {
            Integer ingredientId = parseId(
                request.getParameter("ingId"));

            if ("update".equals(action)) {
                BigDecimal quantity = parseQuantity(
                    request.getParameter("qty"));

                if (ingredientId == null || quantity == null) {
                    result = "invalid";
                } else if (pantryDAO.updateQuantity(
                    consumerId, ingredientId, quantity)) {
                    result = "updated";
                } else {
                    result = "notfound";
                }

            } else if ("add".equals(action)) {
                BigDecimal quantity = parseQuantity(
                    request.getParameter("qty"));

                if (ingredientId == null || quantity == null) {
                    result = "invalid";
                } else {
                    pantryDAO.addExisting(
                        consumerId, ingredientId, quantity);
                    result = "added";
                }

            } else if ("remove".equals(action)) {
                if (ingredientId != null
                    && pantryDAO.remove(consumerId, ingredientId)) {
                    result = "removed";
                } else {
                    result = "notfound";
                }

            } else {
                result = "invalid";
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            result = "duplicate";
        } catch (SQLException e) {
            throw new ServletException("Could not update pantry", e);
        }

        response.sendRedirect(
            request.getContextPath() + "/pantry?msg=" + result
        );
    }

    private User requireConsumer(HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        User user = session == null
            ? null
            : (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.jsp"
            );
            return null;
        }

        if (!userDAO.isConsumer(user.getUserId())) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Only consumers can manage a pantry."
            );
            return null;
        }

        return user;
    }

    private BigDecimal parseQuantity(String value) {
        try {
            BigDecimal quantity = new BigDecimal(value.trim())
                .setScale(2, RoundingMode.HALF_UP);

            if (quantity.signum() < 0
                || quantity.compareTo(MAX_QTY) > 0) {
                return null;
            }

            return quantity;
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseId(String value) {
        try {
            return Integer.valueOf(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
