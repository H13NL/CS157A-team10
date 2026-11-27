package edu.sjsu.team10.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.sjsu.team10.dao.UserDAO;
import edu.sjsu.team10.model.User;

@WebServlet("/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.jsp"
            );
            return;
        }

        request.getRequestDispatcher(
            "/change-password.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.jsp"
            );
            return;
        }

        User user = (User) session.getAttribute("user");

        String currentPassword =
            request.getParameter("currentPassword");

        String newPassword =
            request.getParameter("newPassword");

        String confirmPassword =
            request.getParameter("confirmPassword");

        if (currentPassword == null || newPassword == null || confirmPassword == null || currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {

            response.sendRedirect(
                request.getContextPath()
                + "/change-password?error=missing"
            );
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            response.sendRedirect(
                request.getContextPath()
                + "/change-password?error=mismatch"
            );
            return;
        }

        if (newPassword.length() < 6) {
            response.sendRedirect(
                request.getContextPath()
                + "/change-password?error=short"
            );
            return;
        }

        try {
            boolean changed = userDAO.changePassword(
                user.getUserId(),
                currentPassword,
                newPassword
            );

            if (changed) {
                response.sendRedirect(
                    request.getContextPath()
                    + "/change-password?success=1"
                );
            } else {
                response.sendRedirect(
                    request.getContextPath()
                    + "/change-password?error=incorrect"
                );
            }

        } catch (Exception e) {
            throw new ServletException(
                "Could not change password", e
            );
        }
    }
}