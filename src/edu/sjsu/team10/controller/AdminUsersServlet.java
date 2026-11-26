package edu.sjsu.team10.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.sjsu.team10.dao.AdminDAO;
import edu.sjsu.team10.model.User;
import edu.sjsu.team10.util.Forms;
import edu.sjsu.team10.util.RoleGuard;

// Manage user accounts: list/filter everyone, approve or reject restockers,
// and disable or re-enable accounts. Each change is audit-logged by AdminDAO.
@WebServlet("/admin/users")
public class AdminUsersServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final List<String> ROLES = Arrays.asList("admin", "chef", "restocker", "consumer");
    private static final List<String> STATES = Arrays.asList("active", "pending", "disabled", "rejected");

    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User admin = RoleGuard.require(request, response, "admin");
        if (admin == null) {
            return;
        }

        String role = allowed(request.getParameter("role"), ROLES);
        String state = allowed(request.getParameter("state"), STATES);
        String search = Forms.trim(request.getParameter("q"));
        if (search.length() > 100) {
            search = search.substring(0, 100);
        }

        try {
            request.setAttribute("accounts", adminDAO.findAccounts(role, state, search));
        } catch (SQLException e) {
            throw new ServletException("Could not load user accounts", e);
        }

        request.setAttribute("roleFilter", role == null ? "" : role);
        request.setAttribute("stateFilter", state == null ? "" : state);
        request.setAttribute("search", search);
        request.getRequestDispatcher("/WEB-INF/views/admin-users.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        User admin = RoleGuard.require(request, response, "admin");
        if (admin == null) {
            return;
        }

        String action = request.getParameter("action");
        Integer userId = Forms.parseId(request.getParameter("userId"));
        int adminId = admin.getUserId();
        String result;

        try {
            if (userId == null) {
                result = "invalid";
            } else if ("approve".equals(action)) {
                result = adminDAO.approveRestocker(adminId, userId) ? "approved" : "unchanged";
            } else if ("reject".equals(action)) {
                result = adminDAO.rejectRestocker(adminId, userId) ? "rejected" : "unchanged";
            } else if ("disable".equals(action)) {
                if (userId == adminId) {
                    result = "self";
                } else {
                    result = adminDAO.disableUser(adminId, userId) ? "disabled" : "unchanged";
                }
            } else if ("enable".equals(action)) {
                result = adminDAO.enableUser(adminId, userId) ? "enabled" : "unchanged";
            } else {
                result = "invalid";
            }
        } catch (SQLException e) {
            throw new ServletException("Could not update user account", e);
        }

        // redirect after POST, back to the page (and filters) the admin came from
        String target;
        if ("dashboard".equals(request.getParameter("from"))) {
            target = "/admin?msg=" + result;
        } else {
            target = "/admin/users?msg=" + result
                   + param("role", allowed(request.getParameter("role"), ROLES))
                   + param("state", allowed(request.getParameter("state"), STATES))
                   + param("q", Forms.trim(request.getParameter("q")));
        }
        response.sendRedirect(request.getContextPath() + target);
    }

    // only accept known filter values, so nothing unexpected reaches the query or the page
    private String allowed(String value, List<String> options) {
        return options.contains(value) ? value : null;
    }

    private String param(String name, String value) throws IOException {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return "&" + name + "=" + URLEncoder.encode(value, "UTF-8");
    }
}
