package edu.sjsu.team10.controller;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.sjsu.team10.dao.UserDAO;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.getRequestDispatcher("/signup.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword =
            request.getParameter("confirmPassword");

        if (name == null || email == null || password == null || confirmPassword == null || name.trim().isEmpty() || email.trim().isEmpty()
            || password.isEmpty() || confirmPassword.isEmpty()) {

            response.sendRedirect(
                request.getContextPath()
                + "/signup.jsp?error=missing"
            );
            return;
        }

        if (!password.equals(confirmPassword)) {
            response.sendRedirect(
                request.getContextPath()
                + "/signup.jsp?error=mismatch"
            );
            return;
        }

        try {
            userDAO.createConsumer(
                name.trim(),
                email.trim().toLowerCase(),
                password
            );

            response.sendRedirect(
                request.getContextPath()
                + "/login.jsp?signup=success"
            );

        } catch (SQLIntegrityConstraintViolationException e) {
            response.sendRedirect(
                request.getContextPath()
                + "/signup.jsp?error=duplicate"
            );

        } catch (Exception e) {
            throw new ServletException(
                "Could not create consumer account", e
            );
        }
    }
}