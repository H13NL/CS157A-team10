<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.User" %>
<%@ page import="edu.sjsu.team10.model.UserAccount" %>
<%@ page import="edu.sjsu.team10.util.Html" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    String adminNav = "users";
    List<UserAccount> accounts = (List<UserAccount>) request.getAttribute("accounts");
    String roleFilter = (String) request.getAttribute("roleFilter");
    String stateFilter = (String) request.getAttribute("stateFilter");
    String search = (String) request.getAttribute("search");
    int myId = ((User) session.getAttribute("user")).getUserId();
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");

    String[] roles = { "admin", "chef", "restocker", "consumer" };
    String[] states = { "active", "pending", "disabled", "rejected" };

    // result of the last action (set by AdminUsersServlet's redirect)
    String msg = request.getParameter("msg");
    String flashText = null;
    boolean flashError = false;
    if ("approved".equals(msg)) { flashText = "Restocker approved. They can now log in."; }
    else if ("rejected".equals(msg)) { flashText = "Restocker application rejected."; }
    else if ("disabled".equals(msg)) { flashText = "Account disabled. That user can no longer log in."; }
    else if ("enabled".equals(msg)) { flashText = "Account re-enabled."; }
    else if ("self".equals(msg)) { flashText = "You can't disable your own account."; flashError = true; }
    else if ("unchanged".equals(msg)) { flashText = "Nothing changed. The account may already be in that state, or it is an admin account."; flashError = true; }
    else if ("invalid".equals(msg)) { flashText = "That request was not valid."; flashError = true; }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Users | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css">
</head>
<body class="dashboard-page">
<%@ include file="admin-header.jspf" %>

<main class="dashboard-main">
    <section class="dashboard-heading">
        <div>
            <p class="eyebrow">ADMINISTRATOR</p>
            <h1>User accounts</h1>
            <p>Approve restockers and disable or re-enable accounts. Every change is recorded in the audit log.</p>
        </div>
    </section>

    <% if (flashText != null) { %>
    <p class="flash <%= flashError ? "flash-error" : "flash-success" %>"><%= flashText %></p>
    <% } %>

    <section class="panel inventory-add">
        <form class="add-form" action="<%= ctx %>/admin/users" method="get">
            <label>
                <span>Search name or email</span>
                <input type="search" name="q" maxlength="100" value="<%= Html.escape(search) %>" placeholder="e.g. chef@test.com">
            </label>
            <label class="add-unit">
                <span>Role</span>
                <select name="role">
                    <option value="">All roles</option>
                    <% for (String r : roles) { %>
                    <option value="<%= r %>" <%= r.equals(roleFilter) ? "selected" : "" %>><%= Character.toUpperCase(r.charAt(0)) + r.substring(1) %></option>
                    <% } %>
                </select>
            </label>
            <label class="add-unit">
                <span>Status</span>
                <select name="state">
                    <option value="">Any status</option>
                    <% for (String s : states) { %>
                    <option value="<%= s %>" <%= s.equals(stateFilter) ? "selected" : "" %>><%= Character.toUpperCase(s.charAt(0)) + s.substring(1) %></option>
                    <% } %>
                </select>
            </label>
            <button class="button secondary" type="submit">Filter</button>
            <% if (!search.isEmpty() || !roleFilter.isEmpty() || !stateFilter.isEmpty()) { %>
            <a class="admin-clear" href="<%= ctx %>/admin/users">Clear</a>
            <% } %>
        </form>
    </section>

    <section class="panel">
        <div class="panel-header"><div><p class="eyebrow">ACCOUNTS</p><h2><%= accounts.size() %> user<%= accounts.size() == 1 ? "" : "s" %></h2></div></div>

        <% if (accounts.isEmpty()) { %>
        <p class="form-help">No accounts match these filters.</p>
        <% } %>

        <% for (UserAccount a : accounts) { %>
        <div class="admin-row">
            <span class="food-icon green"><%= Html.escape(a.getName().substring(0, 1).toUpperCase()) %></span>
            <div class="admin-row-name">
                <strong><%= Html.escape(a.getName()) %><%= a.getUserId() == myId ? " (you)" : "" %></strong>
                <small><%= Html.escape(a.getEmail()) %><%= a.getDetail().isEmpty() ? "" : " · " + Html.escape(a.getDetail()) %></small>
                <small>
                    Joined <%= a.getCreatedAt() == null ? "—" : dateFormat.format(a.getCreatedAt()) %>
                    <% if (a.getApprovedByName() != null) { %>
                    · Approved by <%= Html.escape(a.getApprovedByName()) %><%= a.getApprovedAt() == null ? "" : " on " + dateFormat.format(a.getApprovedAt()) %>
                    <% } %>
                </small>
            </div>
            <span class="role-pill"><%= Html.escape(a.getRole()) %></span>
            <span class="state-pill <%= Html.escape(a.getState()) %>"><%= Html.escape(a.getState()) %></span>

            <div class="admin-actions">
                <% if (a.isPending() || a.isRejected()) { %>
                <form action="<%= ctx %>/admin/users" method="post">
                    <input type="hidden" name="action" value="approve">
                    <input type="hidden" name="userId" value="<%= a.getUserId() %>">
                    <input type="hidden" name="role" value="<%= Html.escape(roleFilter) %>">
                    <input type="hidden" name="state" value="<%= Html.escape(stateFilter) %>">
                    <input type="hidden" name="q" value="<%= Html.escape(search) %>">
                    <button class="button primary small" type="submit">Approve</button>
                </form>
                <% } %>
                <% if (a.isPending()) { %>
                <form action="<%= ctx %>/admin/users" method="post"
                      onsubmit="return confirm('Reject this restocker? They will not be able to log in.');">
                    <input type="hidden" name="action" value="reject">
                    <input type="hidden" name="userId" value="<%= a.getUserId() %>">
                    <input type="hidden" name="role" value="<%= Html.escape(roleFilter) %>">
                    <input type="hidden" name="state" value="<%= Html.escape(stateFilter) %>">
                    <input type="hidden" name="q" value="<%= Html.escape(search) %>">
                    <button class="link-button" type="submit">Reject</button>
                </form>
                <% } %>
                <% if (a.isDisabled()) { %>
                <form action="<%= ctx %>/admin/users" method="post">
                    <input type="hidden" name="action" value="enable">
                    <input type="hidden" name="userId" value="<%= a.getUserId() %>">
                    <input type="hidden" name="role" value="<%= Html.escape(roleFilter) %>">
                    <input type="hidden" name="state" value="<%= Html.escape(stateFilter) %>">
                    <input type="hidden" name="q" value="<%= Html.escape(search) %>">
                    <button class="button secondary small" type="submit">Enable</button>
                </form>
                <% } else if (!a.isAdmin() && !a.isRejected()) { %>
                <form action="<%= ctx %>/admin/users" method="post"
                      onsubmit="return confirm('Disable this account? The user will not be able to log in.');">
                    <input type="hidden" name="action" value="disable">
                    <input type="hidden" name="userId" value="<%= a.getUserId() %>">
                    <input type="hidden" name="role" value="<%= Html.escape(roleFilter) %>">
                    <input type="hidden" name="state" value="<%= Html.escape(stateFilter) %>">
                    <input type="hidden" name="q" value="<%= Html.escape(search) %>">
                    <button class="link-button" type="submit">Disable</button>
                </form>
                <% } %>
            </div>
        </div>
        <% } %>
    </section>
</main>
<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span><span>Admin workspace</span></footer>
</body>
</html>
