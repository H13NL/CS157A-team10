<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.User" %>
<%@ page import="edu.sjsu.team10.model.Restaurant" %>
<%@ page import="edu.sjsu.team10.model.StockItem" %>
<%@ page import="edu.sjsu.team10.model.Recipe" %>
<%@ page import="edu.sjsu.team10.util.Html" %>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chef Dashboard | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css">
</head>
<body class="dashboard-page">
<header class="appbar">
    <a class="brand" href="<%= request.getContextPath() %>/chef-dashboard"><span class="brand-mark">W</span><span>WhatsOnTheMenu</span></a>
    <nav class="app-nav"><a class="active" href="#overview">Overview</a><a href="<%= request.getContextPath() %>/inventory">Inventory</a><a href="<%= request.getContextPath() %>/recipes">Recipes</a><a href="#">Restock</a></nav>
    <%
	    User loggedInUser = (User) session.getAttribute("user");
	    Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
	    List<StockItem> inventory = (List<StockItem>) request.getAttribute("inventory");
	    List<Recipe> recipes = (List<Recipe>) request.getAttribute("recipes");
	%>
	
	<div class="account-menu"> 	
	    <span class="avatar">
	        <%= loggedInUser != null ? loggedInUser.getInitials() : "?" %>
	    </span>
	
	    <span>
	        <%= loggedInUser != null ? Html.escape(loggedInUser.getName()) : "Guest" %>
	    </span>
	
	    <a href="<%= request.getContextPath() %>/login.jsp">Log out</a>
	</div>
</header>

<main class="dashboard-main" id="overview">
    <section class="dashboard-heading"><div><p class="eyebrow">CHEF WORKSPACE</p><h1>Good morning, <%= Html.escape(session.getAttribute("userName")) %>.</h1><p>Here is what is happening at <strong><%= Html.escape(restaurant.getRestaurantName()) %></strong>.</p></div>
	    <a class="button primary"
	   		href="<%= request.getContextPath() %>/inventory">
	    	Manage inventory <span>→</span>
		</a>
    </section>

    <section class="metric-grid">
        <article class="metric-card"><span class="metric-icon green">▦</span><div><strong><%= request.getAttribute("inStockCount") %></strong><span>ingredients in stock</span></div><small class="positive">+3 this week</small></article>
        <article class="metric-card"><span class="metric-icon orange">◉</span><div><strong>0</strong><span>recipes makeable now</span></div><small class="positive">Ready to cook</small></article>
        <article class="metric-card"><span class="metric-icon purple">◌</span><div><strong>0</strong><span>ingredients running low</span></div><small class="warning">Needs attention</small></article>
        <article class="metric-card"><span class="metric-icon blue">↗</span><div><strong>0</strong><span>open restock requests</span></div><small>View requests</small></article>
    </section>

    <section class="dashboard-grid">
        <article class="panel" id="inventory">
            <div class="panel-header"><div><p class="eyebrow"><%= Html.escape(restaurant.getRestaurantName().toUpperCase()) %></p><h2>Inventory overview</h2></div><a href="<%= request.getContextPath() %>/inventory">View all →</a></div>
            <div class="inventory-list">

            	<% // lists the first 4 items of the restaurant's inventory %>
            	<% if (inventory.isEmpty()) { %>
				<div class="inventory-item">
				    <span class="food-icon placeholder-icon">—</span>
				    <div>
				        <strong>No ingredients yet</strong>
				        <small>Add ingredients from the inventory page</small>
				    </div>
				</div>
            	<% } %>
            	<% for (StockItem item : inventory.subList(0, Math.min(4, inventory.size()))) { %>
				<div class="inventory-item">
				    <span class="food-icon green"><%= Html.escape(item.getName().substring(0, 1).toUpperCase()) %></span>
				    <div>
				        <strong><%= Html.escape(item.getName()) %></strong>
				        <small><%= item.getQtyDisplay() %> <%= Html.escape(item.getUnit()) %></small>
				    </div>
				    <% if (item.isInStock()) { %>
				    <span class="stock good">In stock</span>
				    <% } else { %>
				    <span class="stock not-ready">Out of stock</span>
				    <% } %>
				</div>
            	<% } %>
            </div>
        </article>

        <article class="panel" id="recipes">
            <div class="panel-header"><div><p class="eyebrow">RECIPE LIBRARY</p><h2>Tonight's options</h2></div><a href="<%= request.getContextPath() %>/recipes">Browse all →</a></div>
            <div class="recipe-list">
	            <% if (recipes == null || recipes.isEmpty()) { %>
                <div class="recipe-item">
                    <span class="recipe-icon recipe-placeholder">—</span>
                    <div><strong>No recipes yet</strong><small>Create one in the recipe library</small></div>
                </div>
                <% } else { %>
                    <% for (Recipe recipe : recipes.subList(0, Math.min(3, recipes.size()))) { %>
                    <a class="recipe-item" href="<%= request.getContextPath() %>/recipes?id=<%= recipe.getRecipeId() %>">
                        <span class="recipe-icon"><%= recipe.isPublic() ? "◈" : "◆" %></span>
                        <div>
                            <strong><%= Html.escape(recipe.getName()) %></strong>
                            <small><%= recipe.getIngredients().size() %> ingredient<%= recipe.getIngredients().size() == 1 ? "" : "s" %></small>
                        </div>
                        <span class="status <%= recipe.getFeasibilityClass() %>"><%= recipe.getFeasibilityLabel() %></span>
                    </a>
                    <% } %>
                <% } %>
            </div>
            <a class="button secondary full-width" href="<%= request.getContextPath() %>/recipes">Open recipe library <span>→</span></a>
        </article>
    </section>

    <section class="next-step"><div><p class="eyebrow">NEXT STEP</p><h2>Keep your inventory accurate.</h2><p>Update quantities after receiving deliveries or using ingredients.</p></div></section>
</main>
<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span><span>Chef workspace</span></footer>
</body>
</html>
