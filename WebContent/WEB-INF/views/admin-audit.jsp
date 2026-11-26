<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.AuditEntry" %>
<%@ page import="edu.sjsu.team10.util.Html" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    String adminNav = "audit";
    List<AuditEntry> entries = (List<AuditEntry>) request.getAttribute("entries");
    List<String> actions = (List<String>) request.getAttribute("actions");
    List<String> entityTypes = (List<String>) request.getAttribute("entityTypes");
    String actionFilter = (String) request.getAttribute("actionFilter");
    String typeFilter = (String) request.getAttribute("typeFilter");
    String search = (String) request.getAttribute("search");
    int limit = (Integer) request.getAttribute("limit");
    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy · h:mm:ss a");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Audit log | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css">
</head>
<body class="dashboard-page">
<%@ include file="admin-header.jspf" %>

<main class="dashboard-main">
    <section class="dashboard-heading">
        <div>
            <p class="eyebrow">ADMINISTRATOR</p>
            <h1>Audit log</h1>
            <p>Who did what, and when. Newest first.</p>
        </div>
    </section>

    <section class="panel inventory-add">
        <form class="add-form" action="<%= ctx %>/admin/audit" method="get">
            <label>
                <span>Performed by (name or email)</span>
                <input type="search" name="q" maxlength="100" value="<%= Html.escape(search) %>" placeholder="e.g. admin@test.com">
            </label>
            <label>
                <span>Action</span>
                <select name="action">
                    <option value="">All actions</option>
                    <% for (String a : actions) { %>
                    <option value="<%= Html.escape(a) %>" <%= a.equals(actionFilter) ? "selected" : "" %>><%= Html.escape(a) %></option>
                    <% } %>
                </select>
            </label>
            <label class="add-unit">
                <span>Entity</span>
                <select name="type">
                    <option value="">All</option>
                    <% for (String t : entityTypes) { %>
                    <option value="<%= Html.escape(t) %>" <%= t.equals(typeFilter) ? "selected" : "" %>><%= Html.escape(t) %></option>
                    <% } %>
                </select>
            </label>
            <button class="button secondary" type="submit">Filter</button>
            <% if (!search.isEmpty() || !actionFilter.isEmpty() || !typeFilter.isEmpty()) { %>
            <a class="admin-clear" href="<%= ctx %>/admin/audit">Clear</a>
            <% } %>
        </form>
    </section>

    <section class="panel">
        <div class="panel-header">
            <div>
                <p class="eyebrow">ENTRIES</p>
                <h2><%= entries.size() %> event<%= entries.size() == 1 ? "" : "s" %></h2>
            </div>
        </div>
        <% if (entries.size() >= limit) { %>
        <p class="form-help">Showing the newest <%= limit %>. Use the filters to narrow it down.</p>
        <% } %>

        <% if (entries.isEmpty()) { %>
        <p class="form-help">No audit entries match these filters.</p>
        <% } else { %>
        <div class="audit-table-wrap">
            <table class="audit-table">
                <thead>
                    <tr><th>When</th><th>Performed by</th><th>Action</th><th>Target</th></tr>
                </thead>
                <tbody>
                <% for (AuditEntry e : entries) { %>
                    <tr>
                        <td class="admin-time"><%= e.getCreatedAt() == null ? "—" : dateFormat.format(e.getCreatedAt()) %></td>
                        <td>
                            <strong><%= Html.escape(e.getUserName()) %></strong>
                            <small><%= Html.escape(e.getUserEmail()) %></small>
                        </td>
                        <td><span class="audit-badge <%= e.getActionTone() %>"><%= Html.escape(e.getActionLabel()) %></span></td>
                        <td><%= Html.escape(e.getTargetLabel()) %></td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>
        <% } %>
    </section>
</main>
<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span><span>Admin workspace</span></footer>
</body>
</html>
