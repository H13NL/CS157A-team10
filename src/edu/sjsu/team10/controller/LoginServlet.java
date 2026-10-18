package edu.sjsu.team10.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.sjsu.team10.dao.RestaurantDAO;
import edu.sjsu.team10.dao.UserDAO;
import edu.sjsu.team10.model.Restaurant;
import edu.sjsu.team10.model.User;

public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private UserDAO userDAO = new UserDAO();
    private RestaurantDAO restaurantDAO = new RestaurantDAO();

    @Override
    // receiving https request from login client for user email and password
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // pass email and password to user data access object
        User user = userDAO.findUser(email, password);
        
        // if a valid user is returned then create a session and set attribute's value with their information from database
        if (user != null) {
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("userName", user.getName());

            // the restaurant this chef works at (null if the user isn't a chef)
            Restaurant restaurant = restaurantDAO.findByChef(user.getUserId());
            session.setAttribute("restaurant", restaurant);

            // For now, all valid users go to the chef dashboard
            response.sendRedirect(
                request.getContextPath() + "/chef-dashboard"
            );
        } else {
            response.sendRedirect(
                request.getContextPath() + "/login.jsp?error=1"
            );
        }
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.sendRedirect(
            request.getContextPath() + "/login.jsp"
        );
    }
}