package edu.sjsu.team10.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.sjsu.team10.dao.AuditLogDAO;
import edu.sjsu.team10.dao.RestaurantDAO;
import edu.sjsu.team10.dao.UserDAO;
import edu.sjsu.team10.model.Restaurant;
import edu.sjsu.team10.model.User;

public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserDAO userDAO = new UserDAO();
    private RestaurantDAO restaurantDAO = new RestaurantDAO();
    private AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    // receiving https request from login client for user email and password
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        // pass email and password to user data access object
        User user = userDAO.findUser(email, password);

        if (user == null) {
            redirectToLogin(request, response, "1");
            return;
        }

        // accounts an admin has disabled or rejected can't log in
        if ("disabled".equals(user.getStatus())) {
            redirectToLogin(request, response, "disabled");
            return;
        }
        if ("rejected".equals(user.getStatus())) {
            redirectToLogin(request, response, "rejected");
            return;
        }

        // figure out the user's role before creating a session
        int userId = user.getUserId();
        String role;
        String landingPage;
        Restaurant restaurant = null;

        if (userDAO.isAdmin(userId)) {
            role = "admin";
            landingPage = "/admin";
        } else if ((restaurant = restaurantDAO.findByChef(userId)) != null) {
            // This user is a chef assigned to a restaurant
            role = "chef";
            landingPage = "/chef-dashboard";
        } else if (userDAO.isConsumer(userId)) {
            role = "consumer";
            landingPage = "/consumer-dashboard";
        } else if (userDAO.isRestocker(userId)) {
            // restockers need an admin's approval first
            if (!userDAO.isApprovedRestocker(userId)) {
                redirectToLogin(request, response, "pending");
                return;
            }
            role = "restocker";
            landingPage = "/catalog";
        } else {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "This account has no valid role."
            );
            return;
        }

        // drop any old session so a new login always starts clean
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        session.setAttribute("userName", user.getName());
        session.setAttribute("role", role);
        if (restaurant != null) {
            session.setAttribute("restaurant", restaurant);
        }

        try {
            auditLogDAO.log(userId, AuditLogDAO.LOGIN, "user", userId);
        } catch (SQLException e) {
            // a failed log entry shouldn't stop the user from logging in
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + landingPage);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.sendRedirect(
            request.getContextPath() + "/login.jsp"
        );
    }

    private void redirectToLogin(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String error) throws IOException {
        response.sendRedirect(
            request.getContextPath() + "/login.jsp?error=" + error
        );
    }
}
