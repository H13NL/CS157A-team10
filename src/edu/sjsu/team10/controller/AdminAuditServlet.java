package edu.sjsu.team10.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.sjsu.team10.dao.AuditLogDAO;
import edu.sjsu.team10.model.User;
import edu.sjsu.team10.util.RoleGuard;

// View audit logs, newest first, filtered by action, entity type, or who did it.
@WebServlet("/admin/audit")
public class AdminAuditServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // the page shows at most this many entries
    static final int LIMIT = 200;

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
            List<String> actions = auditLogDAO.findDistinctActions();
            List<String> entityTypes = auditLogDAO.findDistinctEntityTypes();

            // only filter by values that actually exist in the log
            String action = request.getParameter("action");
            String entityType = request.getParameter("type");
            if (!actions.contains(action)) {
                action = "";
            }
            if (!entityTypes.contains(entityType)) {
                entityType = "";
            }
            String search = request.getParameter("q") == null ? "" : request.getParameter("q").trim();
            if (search.length() > 100) {
                search = search.substring(0, 100);
            }

            request.setAttribute("entries", auditLogDAO.find(action, entityType, search, LIMIT));
            request.setAttribute("actions", actions);
            request.setAttribute("entityTypes", entityTypes);
            request.setAttribute("actionFilter", action);
            request.setAttribute("typeFilter", entityType);
            request.setAttribute("search", search);
            request.setAttribute("limit", LIMIT);
        } catch (SQLException e) {
            throw new ServletException("Could not load audit log", e);
        }

        request.getRequestDispatcher("/WEB-INF/views/admin-audit.jsp")
               .forward(request, response);
    }
}
