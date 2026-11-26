<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.RestockRequest" %>
<%@ page import="edu.sjsu.team10.util.Html" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    String restockerNav = "requests";
    List<RestockRequest> requests = (List<RestockRequest>) request.getAttribute("requests");
    SimpleDateFormat requestDateFormat = new SimpleDateFormat("MMM d, h:mm a");
    boolean forRestocker = true;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Requests | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css">
</head>
<body class="dashboard-page">
<%@ include file="restocker-header.jspf" %>

<main class="dashboard-main">
    <section class="dashboard-heading">
        <div>
            <p class="eyebrow">RESTOCKER</p>
            <h1>Incoming requests</h1>
            <p>Restock requests chefs have sent you, newest first. Estimates use your current catalog prices.</p>
        </div>
    </section>

    <section class="panel">
        <div class="panel-header"><div><p class="eyebrow">REQUESTS</p><h2><%= requests.size() %> request<%= requests.size() == 1 ? "" : "s" %></h2></div></div>

        <% if (requests.isEmpty()) { %>
        <p class="form-help">No chef has sent you a request yet.</p>
        <% } %>

        <div class="request-list">
        <% for (RestockRequest rq : requests) { %>
            <%@ include file="restock-request-card.jspf" %>
        <% } %>
        </div>
    </section>
</main>
<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span><span>Restocker workspace</span></footer>
</body>
</html>
