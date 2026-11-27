<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="edu.sjsu.team10.model.Recipe" %>
<%@ page import="edu.sjsu.team10.util.Html" %>

<%
    String userName = (String) session.getAttribute("userName");
    if (userName == null || userName.trim().isEmpty()) {
        userName = "Consumer";
    }

    List<Recipe> recipes =
        (List<Recipe>) request.getAttribute("recipes");

    if (recipes == null) {
        recipes = java.util.Collections.emptyList();
    }

    String deleted = request.getParameter("deleted");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recipes | WhatsOnTheMenu</title>
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/assets/css/app.css">
</head>

<body class="dashboard-page">

<header class="appbar">
    <a class="brand"
       href="<%= request.getContextPath() %>/consumer-dashboard">
        <span class="brand-mark">W</span>
        <span>WhatsOnTheMenu</span>
    </a>

    <nav class="app-nav">
        <a href="<%= request.getContextPath() %>/consumer-dashboard">
            Overview
        </a>
        <a href="<%= request.getContextPath() %>/pantry">
            My pantry
        </a>
        <a class="active"
           href="<%= request.getContextPath() %>/consumer-recipes">
            Recipes
        </a>
        <a href="<%= request.getContextPath() %>/orders">
            Orders
        </a>
    </nav>

    <div class="account-menu">
        <span class="avatar">CU</span>
        <span><%= Html.escape(userName) %></span>
        <a href="<%= request.getContextPath() %>/change-password">
            Change password
        </a>
    </div>
</header>

<main class="dashboard-main">

    <section class="dashboard-heading">
        <div>
            <p class="eyebrow">CONSUMER WORKSPACE</p>
            <h1>Recipe library.</h1>
            <p>
                Browse public recipes and see what your pantry can make.
            </p>
        </div>
    </section>

    <% if ("1".equals(deleted)) { %>
        <p class="demo-note">Your private recipe was removed.</p>
    <% } else if ("0".equals(deleted)) { %>
        <p class="demo-note">That recipe could not be removed.</p>
    <% } %>

    <section class="recipe-library-grid">

        <% if (recipes.isEmpty()) { %>
            <article class="panel empty-state">
                <h2>No public recipes yet.</h2>
                <p>
                    Public recipes will appear here when chefs publish them.
                </p>
            </article>
        <% } %>

        <% for (Recipe recipe : recipes) { %>
            <a class="recipe-tile"
               href="<%= request.getContextPath() %>/consumer-recipes?id=<%= recipe.getRecipeId() %>">

                <div class="tile-top">
                    <span class="recipe-icon">◈</span>
                    <span class="visibility-pill public">
                        Public
                    </span>
                </div>

                <h2><%= Html.escape(recipe.getName()) %></h2>

                <p class="tile-meta">
                    By <%= Html.escape(recipe.getCreatorName()) %>
                    · <%= recipe.getIngredients().size() %> ingredients
                </p>

                <div class="tile-footer">
                    <span class="status <%= recipe.getFeasibilityClass() %>">
                        <%= recipe.getFeasibilityLabel() %>
                    </span>
                    <span class="tile-arrow">→</span>
                </div>
            </a>
        <% } %>

    </section>
</main>

<footer class="app-footer">
    <span>© 2026 Team 10 · WhatsOnTheMenu</span>
    <span><%= recipes.size() %> public recipes</span>
</footer>

</body>
</html>
