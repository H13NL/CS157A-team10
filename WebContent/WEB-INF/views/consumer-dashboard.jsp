<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="edu.sjsu.team10.model.PantryItem" %>
<%
    String userName = (String) session.getAttribute("userName");
    if (userName == null || userName.trim().isEmpty()) {
        userName = "Consumer";
    }
%>

<%
    List<PantryItem> pantryItems =
        (List<PantryItem>) request.getAttribute("pantryItems");

    Integer pantryCount =
        (Integer) request.getAttribute("pantryCount");

    if (pantryItems == null) {
        pantryItems = new java.util.ArrayList<PantryItem>();
    }

    if (pantryCount == null) {
        pantryCount = pantryItems.size();
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Consumer Dashboard | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css">
</head>
<body class="dashboard-page consumer-page">
<header class="appbar">
    <a class="brand" href="<%= request.getContextPath() %>/consumer-dashboard">
        <span class="brand-mark">W</span>
        <span>WhatsOnTheMenu</span>
    </a>

    <nav class="app-nav">
        <a class="active" href="#overview">Overview</a>
        <a href="#pantry">My pantry</a>
        <a href="#recipes">Recipes</a>
        <a href="#orders">Orders</a>
    </nav>

    <div class="account-menu">
        <span class="avatar">CU</span>
        <span><%= userName %></span>
        <a href="<%= request.getContextPath() %>/login.jsp">Log out</a>
    </div>
</header>

<main class="dashboard-main" id="overview">
    <section class="dashboard-heading">
        <div>
            <p class="eyebrow">CONSUMER WORKSPACE</p>
            <h1>Good morning, <%= userName %>.</h1>
            <p>See what you can make with the ingredients in your pantry.</p>
        </div>
        <a class="button primary" href="<%= request.getContextPath() %>/pantry">Manage pantry <span>→</span></a>
    </section>

    <section class="metric-grid">
        <article class="metric-card">
            <span class="metric-icon green">▦</span>
            <strong><%= pantryCount %></strong>
			<span>ingredients in pantry</span>
            <small>View pantry</small>
        </article>
        <article class="metric-card">
            <span class="metric-icon orange">◉</span>
            <div><strong>--</strong><span>recipes makeable now</span></div>
            <small class="positive">Ready to cook</small>
        </article>
        <article class="metric-card">
            <span class="metric-icon purple">◌</span>
            <div><strong>--</strong><span>pantry items running low</span></div>
            <small class="warning">Needs attention</small>
        </article>
        <article class="metric-card">
            <span class="metric-icon blue">↗</span>
            <div><strong>--</strong><span>active grocery orders</span></div>
            <small>View orders</small>
        </article>
    </section>

    <section class="dashboard-grid">
        <article class="panel" id="pantry">
            <div class="panel-header">
                <div>
                    <p class="eyebrow">MY PANTRY</p>
                    <h2>Pantry overview</h2>
                </div>
                <a href="#pantry">Manage →</a>
            </div>

			<% if (pantryItems.isEmpty()) { %>
			
			    <p class="form-help">
			        Your pantry is empty. Add ingredients to get started.
			    </p>
			
			<% } else { %>
			
			    <div class="inventory-list">
			        <% for (PantryItem item : pantryItems) { %>
			
			            <div class="inventory-item">
			                <span class="food-icon placeholder-icon">—</span>
			
			                <div>
			                    <strong><%= item.getName() %></strong>
			                    <small>
			                        <%= item.getQuantity() %>
			                        <%= item.getUnit() %>
			                    </small>
			                </div>
			
			                <span class="stock good">
			                    In stock
			                </span>
			            </div>
			
			        <% } %>
			    </div>
			
			<% } %>

            <a class="button secondary full-width" href="<%= request.getContextPath() %>/pantry">Open pantry <span>→</span></a>
        </article>

        <article class="panel" id="recipes">
            <div class="panel-header">
                <div>
                    <p class="eyebrow">RECIPE LIBRARY</p>
                    <h2>What can I make?</h2>
                </div>
                <a href="#recipes">Browse all →</a>
            </div>

            <div class="recipe-list">
                <div class="recipe-item">
                    <span class="recipe-icon recipe-placeholder">—</span>
                    <div><strong>Recipe name</strong><small>Ingredient check</small></div>
                    <span class="status">Status</span>
                </div>
                <div class="recipe-item">
                    <span class="recipe-icon recipe-placeholder">—</span>
                    <div><strong>Recipe name</strong><small>Ingredient check</small></div>
                    <span class="status">Status</span>
                </div>
                <div class="recipe-item">
                    <span class="recipe-icon recipe-placeholder">—</span>
                    <div><strong>Recipe name</strong><small>Ingredient check</small></div>
                    <span class="status">Status</span>
                </div>
            </div>

            <a class="button secondary full-width" href="#recipes">Explore recipes <span>→</span></a>
        </article>
    </section>

    <section class="next-step" id="orders">
        <div>
            <p class="eyebrow">GROCERY ORDERS</p>
            <h2>Need a few more ingredients?</h2>
            <p>Add missing pantry items to a simulated grocery order.</p>
        </div>
        <a class="button light" href="#orders">Start an order <span>→</span></a>
    </section>
</main>

<footer class="app-footer">
    <span>© 2026 Team 10 · WhatsOnTheMenu</span>
    <span>Consumer workspace</span>
</footer>
</body>
</html>
