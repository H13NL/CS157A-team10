<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.User" %>
<%@ page import="edu.sjsu.team10.model.Recipe" %>
<%@ page import="edu.sjsu.team10.model.RecipeIngredient" %>
<%@ page import="edu.sjsu.team10.util.Html" %>
<%
    User loggedInUser = (User) session.getAttribute("user");
    Recipe recipe = (Recipe) request.getAttribute("recipe");
    boolean canEdit = Boolean.TRUE.equals(request.getAttribute("canEdit"));
%>
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title><%= Html.escape(recipe.getName()) %> | WhatsOnTheMenu</title><link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css"></head>
<body class="dashboard-page">
<header class="appbar"><a class="brand" href="<%= request.getContextPath() %>/chef-dashboard"><span class="brand-mark">W</span><span>WhatsOnTheMenu</span></a><nav class="app-nav"><a href="<%= request.getContextPath() %>/chef-dashboard">Overview</a><a href="<%= request.getContextPath() %>/inventory">Inventory</a><a class="active" href="<%= request.getContextPath() %>/recipes">Recipes</a><a href="#">Restock</a></nav><div class="account-menu"><span class="avatar"><%= loggedInUser.getInitials() %></span><span><%= Html.escape(loggedInUser.getName()) %></span><a href="<%= request.getContextPath() %>/login.jsp">Log out</a></div></header>
<main class="dashboard-main recipe-detail-page">
    <a class="back-link" href="<%= request.getContextPath() %>/recipes">← Back to recipe library</a>
    <section class="detail-hero panel"><div><div class="detail-kicker"><span class="visibility-pill <%= recipe.isPublic() ? "public" : "private" %>"><%= recipe.isPublic() ? "Public recipe" : "Private recipe" %></span><span class="detail-owner">By <%= Html.escape(recipe.getCreatorName()) %></span></div><h1><%= Html.escape(recipe.getName()) %></h1><p><%= Html.escape(recipe.getDescription()) %></p></div><div class="detail-actions"><% if (canEdit) { %><a class="button primary" href="<%= request.getContextPath() %>/recipe-form?id=<%= recipe.getRecipeId() %>">Edit recipe <span>→</span></a><% } %><% if (recipe.isPublic()) { %><form action="<%= request.getContextPath() %>/recipe-copy" method="post"><input type="hidden" name="recipeId" value="<%= recipe.getRecipeId() %>"><button class="button secondary" type="submit">Save private copy <span>＋</span></button></form><% } %></div></section>
    <section class="detail-grid"><article class="panel ingredient-panel"><div class="panel-header"><div><p class="eyebrow">RECIPE INGREDIENTS</p><h2><%= recipe.getIngredients().size() %> required</h2></div><span class="status <%= recipe.getFeasibilityClass() %>"><%= recipe.getFeasibilityLabel() %></span></div><div class="detail-ingredients"><% for (RecipeIngredient ingredient : recipe.getIngredients()) { %><div class="detail-ingredient <%= ingredient.isSatisfied() ? "ingredient-ready" : "ingredient-short" %>"><span class="food-icon <%= ingredient.isSatisfied() ? "green" : "orange" %>"><%= ingredient.isSatisfied() ? "✓" : "!" %></span><div><strong><%= Html.escape(ingredient.getName()) %></strong><small>Need <%= ingredient.getQuantityDisplay() %> <%= Html.escape(ingredient.getUnit()) %> · Have <%= ingredient.getAvailableDisplay() %> <%= Html.escape(ingredient.getAvailableUnit()) %></small></div><span class="ingredient-check"><%= ingredient.isSatisfied() ? "Ready" : "Short" %></span></div><% } %></div></article><aside class="panel feasibility-panel"><p class="eyebrow">LIVE KITCHEN CHECK</p><h2><%= recipe.getMissingCount() == 0 ? "You can make this now." : "A few ingredients need attention." %></h2><p>Feasibility is checked against the current stock at your restaurant.</p><div class="feasibility-scale"><div class="scale-row"><span class="scale-dot makeable"></span><span>Makeable</span><small>0 ingredients short</small></div><div class="scale-row"><span class="scale-dot almost"></span><span>Almost Makeable</span><small>1–2 ingredients short</small></div><div class="scale-row"><span class="scale-dot not-ready"></span><span>Not Makeable</span><small>3+ ingredients short</small></div></div></aside></section>
</main>
<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span><span>Recipe detail</span></footer>
</body>
</html>
