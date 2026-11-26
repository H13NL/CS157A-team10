package edu.sjsu.team10.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.sjsu.team10.dao.AdminDAO;
import edu.sjsu.team10.dao.AuditLogDAO;
import edu.sjsu.team10.model.User;
import edu.sjsu.team10.util.RoleGuard;

// Admin overview: account counts, restockers waiting for approval, and recent audit activity.
@WebServlet("/admin")
public class AdminDashboardServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final AdminDAO adminDAO = new AdminDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User admin = RoleGuard.require(request, response, "admin");
        if (admin == null) {
            return;
        }

        try {
            int[] summary = adminDAO.countSummary();
            request.setAttribute("totalUsers", summary[0]);
            request.setAttribute("pendingCount", summary[1]);
            request.setAttribute("disabledCount", summary[2]);
            request.setAttribute("eventsToday", auditLogDAO.countSince(24));
            request.setAttribute("pendingRestockers", adminDAO.findPendingRestockers());
            request.setAttribute("recentActivity", auditLogDAO.findRecent(8));
        } catch (SQLException e) {
            throw new ServletException("Could not load admin dashboard", e);
        }

        request.getRequestDispatcher("/WEB-INF/views/admin-dashboard.jsp")
               .forward(request, response);
    }
}
