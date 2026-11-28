<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.User" %>
<%@ page import="edu.sjsu.team10.model.Restaurant" %>
<%@ page import="edu.sjsu.team10.model.Recipe" %>
<%@ page import="edu.sjsu.team10.util.Html" %>
<%@ page import="java.util.List" %>
<%
    User loggedInUser = (User) session.getAttribute("user");
    Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
    List<Recipe> recipes = (List<Recipe>) request.getAttribute("recipes");
    boolean consumerView = "consumer".equals(session.getAttribute("role"));
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recipe Library | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css">
</head>
<body class="dashboard-page">
<header class="appbar">
    <a class="brand" href="<%= request.getContextPath() %>/<%= consumerView ? "consumer-dashboard" : "chef-dashboard" %>"><span class="brand-mark">W</span><span>WhatsOnTheMenu</span></a>
    <nav class="app-nav">
        <% if (consumerView) { %>
            <a href="<%= request.getContextPath() %>/consumer-dashboard">Overview</a><a href="<%= request.getContextPath() %>/pantry">My pantry</a><a class="active" href="<%= request.getContextPath() %>/recipes">Recipes</a><a href="<%= request.getContextPath() %>/orders">Orders</a>
        <% } else { %>
            <a href="<%= request.getContextPath() %>/chef-dashboard">Overview</a><a href="<%= request.getContextPath() %>/inventory">Inventory</a><a class="active" href="<%= request.getContextPath() %>/recipes">Recipes</a><a href="<%= request.getContextPath() %>/restock">Restock</a>
        <% } %>
    </nav>
    <div class="account-menu"><span class="avatar"><%= loggedInUser.getInitials() %></span><span><%= Html.escape(loggedInUser.getName()) %></span><a href="<%= request.getContextPath() %>/login.jsp">Log out</a></div>
</header>
<main class="dashboard-main">
    <section class="dashboard-heading recipe-heading"><div><p class="eyebrow"><%= consumerView ? "CONSUMER WORKSPACE" : "CHEF WORKSPACE" %></p><h1>Recipe library</h1><p><%= consumerView ? "Browse public recipes and see what you can make from your pantry." : "Browse shared ideas, keep private recipes, and see what your kitchen can make right now." %></p></div><% if (!consumerView) { %><a class="button primary" href="<%= request.getContextPath() %>/recipe-form">Create a recipe <span>+</span></a><% } %></section>
    <% if ("made".equals(request.getParameter("msg"))) { %><p class="flash flash-success">Recipe made. The required ingredients were removed from inventory.</p>
    <% } else if ("not-makeable".equals(request.getParameter("msg"))) { %><p class="flash flash-error">This recipe cannot be made with the current inventory.</p>
    <% } else if ("deleted".equals(request.getParameter("msg"))) { %><p class="flash flash-success">Recipe deleted.</p><% } %>
    <section class="library-intro panel"><div><p class="eyebrow"><%= consumerView ? "PUBLIC RECIPES" : "YOUR COLLECTION" %></p><h2><%= consumerView ? "Recipes shared by chefs" : "Recipes for " + Html.escape(restaurant.getRestaurantName()) %></h2><p><%= consumerView ? "Only recipes that their creators marked public appear here." : "Public recipes are available to everyone. Your private recipes are visible and editable only by you." %></p></div><div class="library-legend"><span><i class="legend-dot public-dot"></i>Public</span><% if (!consumerView) { %><span><i class="legend-dot private-dot"></i>Private</span><% } %></div></section>
    <section class="recipe-library-grid">
        <% if (recipes.isEmpty()) { %><article class="panel empty-state"><span class="recipe-icon recipe-placeholder">—</span><h2><%= consumerView ? "No public recipes yet." : "Your library is ready for its first recipe." %></h2><p><%= consumerView ? "Public recipes will appear here when a chef shares one." : "Create a recipe or browse a public one from another chef." %></p><% if (!consumerView) { %><a class="button primary" href="<%= request.getContextPath() %>/recipe-form">Create a recipe <span>+</span></a><% } %></article><% } %>
        <% for (Recipe recipe : recipes) { %>
            <article class="recipe-tile">
                <a class="recipe-tile-link" href="<%= request.getContextPath() %>/recipes?id=<%= recipe.getRecipeId() %>">
                    <div class="tile-top"><span class="recipe-icon"><%= recipe.isPublic() ? "◈" : "◆" %></span><span class="visibility-pill <%= recipe.isPublic() ? "public" : "private" %>"><%= recipe.isPublic() ? "Public" : "Private" %></span></div>
                    <h2><%= Html.escape(recipe.getName()) %></h2>
                    <p class="tile-meta">By <%= Html.escape(recipe.getCreatorName()) %> · <%= recipe.getIngredients().size() %> ingredient<%= recipe.getIngredients().size() == 1 ? "" : "s" %></p>
                    <div class="tile-footer"><span class="status <%= recipe.getFeasibilityClass() %>"><%= recipe.getFeasibilityLabel() %></span><span class="tile-arrow">→</span></div>
                </a>
                <% if (!consumerView) { %>
                    <form class="recipe-make-form" action="<%= request.getContextPath() %>/recipe-make" method="post">
                        <input type="hidden" name="recipeId" value="<%= recipe.getRecipeId() %>">
                        <input type="hidden" name="returnTo" value="library">
                        <button class="button primary" type="submit">Make</button>
                    </form>
                <% } %>
            </article>
        <% } %>
    </section>
</main>
<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span><span><%= recipes.size() %> visible recipe<%= recipes.size() == 1 ? "" : "s" %></span></footer>
</body>
</html>
