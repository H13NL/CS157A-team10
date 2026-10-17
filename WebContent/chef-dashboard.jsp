<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.User" %>

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
    <a class="brand" href="<%= request.getContextPath() %>/chef-dashboard.jsp"><span class="brand-mark">W</span><span>WhatsOnTheMenu</span></a>
    <nav class="app-nav"><a class="active" href="#overview">Overview</a><a href="#inventory">Inventory</a><a href="#recipes">Recipes</a><a href="#">Restock</a></nav>
    <%
	    User loggedInUser = (User) session.getAttribute("user");
	%>
	
	<div class="account-menu"> 	
	    <span class="avatar">
	        <%= loggedInUser != null ? loggedInUser.getInitials() : "?" %>
	    </span>
	
	    <span>
	        <%= loggedInUser != null ? loggedInUser.getName() : "Guest" %>
	    </span>
	
	    <a href="<%= request.getContextPath() %>/login.jsp">Log out</a>
	</div>
</header>

<main class="dashboard-main" id="overview">
    <section class="dashboard-heading"><div><p class="eyebrow">CHEF WORKSPACE</p><h1>Good morning, <%= session.getAttribute("userName") %>.</h1><p>Here is what is happening at <strong>Team 10 Kitchen</strong>.</p></div>
	    <a class="button primary"
	   		href="<%= request.getContextPath() %>/inventory.jsp">
	    	Manage inventory <span>→</span>
		</a>
    </section>

    <section class="metric-grid">
        <article class="metric-card"><span class="metric-icon green">▦</span><div><strong>0</strong><span>ingredients in stock</span></div><small class="positive">+3 this week</small></article>
        <article class="metric-card"><span class="metric-icon orange">◉</span><div><strong>0</strong><span>recipes makeable now</span></div><small class="positive">Ready to cook</small></article>
        <article class="metric-card"><span class="metric-icon purple">◌</span><div><strong>0</strong><span>ingredients running low</span></div><small class="warning">Needs attention</small></article>
        <article class="metric-card"><span class="metric-icon blue">↗</span><div><strong>0</strong><span>open restock requests</span></div><small>View requests</small></article>
    </section>

    <section class="dashboard-grid">
        <article class="panel" id="inventory">
            <div class="panel-header"><div><p class="eyebrow">TEAM 10 KITCHEN</p><h2>Inventory overview</h2></div><a href="#">View all →</a></div>
            <div class="inventory-list">
            	
            	<% // these are place holders for the inventory items (it will only list the first 4 items) %>>
				<div class="inventory-item">
				    <span class="food-icon placeholder-icon">—</span>
				    <div>
				        <strong>Ingredient name</strong>
				        <small>Category · Quantity</small>
				    </div>
				    <span class="stock">Status</span>
				</div>
				<div class="inventory-item">
				    <span class="food-icon placeholder-icon">—</span>
				    <div>
				        <strong>Ingredient name</strong>
				        <small>Category · Quantity</small>
				    </div>
				    <span class="stock">Status</span>
				</div>
				<div class="inventory-item">
				    <span class="food-icon placeholder-icon">—</span>
				    <div>
				        <strong>Ingredient name</strong>
				        <small>Category · Quantity</small>
				    </div>
				    <span class="stock">Status</span>
				</div>
				<div class="inventory-item">
				    <span class="food-icon placeholder-icon">—</span>
				    <div>
				        <strong>Ingredient name</strong>
				        <small>Category · Quantity</small>
				    </div>
				    <span class="stock">Status</span>
				</div>
            </div>
        </article>

        <article class="panel" id="recipes">
            <div class="panel-header"><div><p class="eyebrow">RECIPE LIBRARY</p><h2>Tonight's options</h2></div><a href="#">Browse all →</a></div>
            <div class="recipe-list">
            
            	<% // these are place holders for the recipe items (it will only list the first 4 items) %>>
                <div class="recipe-item">
				    <span class="recipe-icon recipe-placeholder">—</span>
				    <div>
				        <strong>Recipe name</strong>
				        <small>Number of ingredients</small>
				    </div>
				    <span class="status">Status</span>
				</div>
				<div class="recipe-item">
				    <span class="recipe-icon recipe-placeholder">—</span>
				    <div>
				        <strong>Recipe name</strong>
				        <small>Number of ingredients</small>
				    </div>
				    <span class="status">Status</span>
				</div>
				<div class="recipe-item">
				    <span class="recipe-icon recipe-placeholder">—</span>
				    <div>
				        <strong>Recipe name</strong>
				        <small>Number of ingredients</small>
				    </div>
				    <span class="status">Status</span>
				</div>
            </div>
            <a class="button secondary full-width" href="#">Create a recipe <span>+</span></a>
        </article>
    </section>

    <section class="next-step"><div><p class="eyebrow">NEXT STEP</p><h2>Keep your inventory accurate.</h2><p>Update quantities after receiving deliveries or using ingredients.</p></div></section>
</main>
<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span><span>Chef workspace</span></footer>
</body>
</html>
