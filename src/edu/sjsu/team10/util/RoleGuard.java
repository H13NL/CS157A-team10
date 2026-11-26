package edu.sjsu.team10.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.sjsu.team10.model.User;

// Shared role check for pages that belong to one role.
// LoginServlet sets the session "role" to admin, chef, consumer, or restocker.
public final class RoleGuard {

    private RoleGuard() { }

    // returns the logged-in user if they have this role, or sends a redirect/error and returns null
    public static User require(HttpServletRequest request,
                               HttpServletResponse response,
                               String role) throws IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return null;
        }

        if (!role.equals(session.getAttribute("role"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "This page is only for " + role + " accounts.");
            return null;
        }

        return (User) session.getAttribute("user");
    }
}
