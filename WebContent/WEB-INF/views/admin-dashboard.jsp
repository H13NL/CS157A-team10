<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.UserAccount" %>
<%@ page import="edu.sjsu.team10.model.AuditEntry" %>
<%@ page import="edu.sjsu.team10.util.Html" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    String adminNav = "overview";
    List<UserAccount> pendingRestockers = (List<UserAccount>) request.getAttribute("pendingRestockers");
    List<AuditEntry> recentActivity = (List<AuditEntry>) request.getAttribute("recentActivity");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h:mm a");

    // result of the last approve/reject (set by AdminUsersServlet's redirect)
    String msg = request.getParameter("msg");
    String flashText = null;
    boolean flashError = false;
    if ("approved".equals(msg)) { flashText = "Restocker approved. They can now log in."; }
    else if ("rejected".equals(msg)) { flashText = "Restocker application rejected."; }
    else if ("unchanged".equals(msg)) { flashText = "Nothing changed. That restocker may already have been handled."; flashError = true; }
    else if ("invalid".equals(msg)) { flashText = "That request was not valid."; flashError = true; }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css">
</head>
<body class="dashboard-page">
<%@ include file="admin-header.jspf" %>

<main class="dashboard-main">
    <section class="dashboard-heading">
        <div>
            <p class="eyebrow">ADMINISTRATOR</p>
            <h1>Admin overview</h1>
            <p>Approve restockers, manage accounts, and review what has changed.</p>
        </div>
        <a class="button primary" href="<%= ctx %>/admin/users">Manage users <span>→</span></a>
    </section>

    <% if (flashText != null) { %>
    <p class="flash <%= flashError ? "flash-error" : "flash-success" %>"><%= flashText %></p>
    <% } %>

    <section class="metric-grid">
        <a class="metric-card" href="<%= ctx %>/admin/users">
            <span class="metric-icon green">◎</span>
            <div><strong><%= request.getAttribute("totalUsers") %></strong><span>user accounts</span></div>
        </a>
        <a class="metric-card" href="<%= ctx %>/admin/users?role=restocker&state=pending">
            <span class="metric-icon orange">◷</span>
            <div><strong><%= request.getAttribute("pendingCount") %></strong><span>restockers awaiting approval</span></div>
        </a>
        <a class="metric-card" href="<%= ctx %>/admin/users?state=disabled">
            <span class="metric-icon purple">⊘</span>
            <div><strong><%= request.getAttribute("disabledCount") %></strong><span>disabled accounts</span></div>
        </a>
        <a class="metric-card" href="<%= ctx %>/admin/audit">
            <span class="metric-icon blue">≡</span>
            <div><strong><%= request.getAttribute("eventsToday") %></strong><span>audit events in the last 24h</span></div>
        </a>
    </section>

    <div class="dashboard-grid">
        <section class="panel">
            <div class="panel-header">
                <div><p class="eyebrow">APPROVALS</p><h2>Pending restockers</h2></div>
                <a href="<%= ctx %>/admin/users?role=restocker">All restockers</a>
            </div>

            <% if (pendingRestockers.isEmpty()) { %>
            <p class="form-help">No restockers are waiting for approval.</p>
            <% } %>

            <% for (UserAccount r : pendingRestockers) { %>
            <div class="admin-row">
                <span class="food-icon orange"><%= Html.escape(r.getName().substring(0, 1).toUpperCase()) %></span>
                <div class="admin-row-name">
                    <strong><%= Html.escape(r.getName()) %></strong>
                    <small><%= Html.escape(r.getDetail()) %> · <%= Html.escape(r.getEmail()) %></small>
                    <small>Signed up <%= r.getCreatedAt() == null ? "—" : dateFormat.format(r.getCreatedAt()) %></small>
                </div>
                <div class="admin-actions">
                    <form action="<%= ctx %>/admin/users" method="post">
                        <input type="hidden" name="action" value="approve">
                        <input type="hidden" name="userId" value="<%= r.getUserId() %>">
                        <input type="hidden" name="from" value="dashboard">
                        <button class="button primary small" type="submit">Approve</button>
                    </form>
                    <form action="<%= ctx %>/admin/users" method="post"
                          onsubmit="return confirm('Reject this restocker? They will not be able to log in.');">
                        <input type="hidden" name="action" value="reject">
                        <input type="hidden" name="userId" value="<%= r.getUserId() %>">
                        <input type="hidden" name="from" value="dashboard">
                        <button class="link-button" type="submit">Reject</button>
                    </form>
                </div>
            </div>
            <% } %>
        </section>

        <section class="panel">
            <div class="panel-header">
                <div><p class="eyebrow">ACTIVITY</p><h2>Recent audit log</h2></div>
                <a href="<%= ctx %>/admin/audit">View all</a>
            </div>

            <% if (recentActivity.isEmpty()) { %>
            <p class="form-help">No activity has been logged yet.</p>
            <% } %>

            <% for (AuditEntry e : recentActivity) { %>
            <div class="admin-row">
                <span class="audit-badge <%= e.getActionTone() %>"><%= Html.escape(e.getActionLabel()) %></span>
                <div class="admin-row-name">
                    <strong><%= Html.escape(e.getUserName()) %></strong>
                    <small><%= Html.escape(e.getTargetLabel()) %></small>
                </div>
                <small class="admin-time"><%= e.getCreatedAt() == null ? "—" : dateFormat.format(e.getCreatedAt()) %></small>
            </div>
            <% } %>
        </section>
    </div>
</main>
<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span><span>Admin workspace</span></footer>
</body>
</html>
