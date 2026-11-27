<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Collections" %>
<%@ page import="edu.sjsu.team10.model.PantryItem" %>
<%@ page import="edu.sjsu.team10.model.Recipe" %>
<%@ page import="edu.sjsu.team10.util.Html" %>

<%
    String userName = (String) session.getAttribute("userName");

    if (userName == null || userName.trim().isEmpty()) {
        userName = "Consumer";
    }

    List<PantryItem> pantryItems =
        (List<PantryItem>) request.getAttribute("pantryItems");

    List<Recipe> recipes =
        (List<Recipe>) request.getAttribute("recipes");

    Integer pantryCount =
        (Integer) request.getAttribute("pantryCount");

    Integer lowPantryCount =
        (Integer) request.getAttribute("lowPantryCount");

    if (pantryItems == null) {
        pantryItems = Collections.emptyList();
    }

    if (recipes == null) {
        recipes = Collections.emptyList();
    }

    if (pantryCount == null) {
        pantryCount = pantryItems.size();
    }

    if (lowPantryCount == null) {
        lowPantryCount = 0;
    }
%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Consumer Dashboard | WhatsOnTheMenu</title>

    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/assets/css/app.css">
</head>

<body class="dashboard-page consumer-page">

<header class="appbar">

    <a class="brand"
       href="<%= request.getContextPath() %>/consumer-dashboard">

        <span class="brand-mark">W</span>
        <span>WhatsOnTheMenu</span>
    </a>

    <nav class="app-nav">

        <a class="active" href="#overview">
            Overview
        </a>

        <a href="#pantry">
            My pantry
        </a>

        <a href="<%= request.getContextPath() %>/consumer-recipes">
            Recipes
        </a>

        <a href="<%= request.getContextPath() %>/orders">
            Orders
        </a>

    </nav>

    <div class="account-menu">

        <span class="avatar">CU</span>

        <span>
            <%= Html.escape(userName) %>
        </span>

        <a href="<%= request.getContextPath() %>/change-password">
            Change password
        </a>

        <a href="<%= request.getContextPath() %>/login.jsp">
            Log out
        </a>

    </div>

</header>

<main class="dashboard-main" id="overview">

    <section class="dashboard-heading">

        <div>
            <p class="eyebrow">CONSUMER WORKSPACE</p>

            <h1>
                Good morning, <%= Html.escape(userName) %>.
            </h1>

            <p>
                See what you can make with the ingredients in your pantry.
            </p>
        </div>

        <a class="button primary"
           href="<%= request.getContextPath() %>/pantry">

            Manage pantry
            <span>→</span>
        </a>

    </section>

    <section class="metric-grid">

        <article class="metric-card">

            <span class="metric-icon green">▦</span>

            <div>
                <strong><%= pantryCount %></strong>
                <span>ingredients in pantry</span>
            </div>

            <small>
                View pantry
            </small>

        </article>

        <article class="metric-card">

            <span class="metric-icon orange">◉</span>

            <div>
                <strong>--</strong>
                <span>recipes makeable now</span>
            </div>

            <small class="positive">
                Ready to cook
            </small>

        </article>

        <article class="metric-card">

            <span class="metric-icon purple">◌</span>

            <div>
                <strong><%= lowPantryCount %></strong>
                <span>pantry items running low</span>
            </div>

            <small class="warning">
                Needs attention
            </small>

        </article>

        <article class="metric-card">

            <span class="metric-icon blue">↗</span>

            <div>
                <strong>--</strong>
                <span>active grocery orders</span>
            </div>

            <small>
                View orders
            </small>

        </article>

    </section>

    <section class="dashboard-grid">

        <!-- PANTRY SECTION -->

        <article class="panel" id="pantry">

            <div class="panel-header">

                <div>
                    <p class="eyebrow">MY PANTRY</p>
                    <h2>Pantry overview</h2>
                </div>

                <a href="<%= request.getContextPath() %>/pantry">
                    Manage →
                </a>

            </div>

            <% if (pantryItems.isEmpty()) { %>

                <p class="form-help">
                    Your pantry is empty.
                    Add ingredients to get started.
                </p>

            <% } else { %>

                <div class="inventory-list">

                    <%
                        int displayedPantryItems = 0;

                        for (PantryItem item : pantryItems) {
                            if (displayedPantryItems >= 4) {
                                break;
                            }

                            displayedPantryItems++;

                            boolean lowQuantity =
                                item.getQuantity() != null
                                && item.getQuantity().doubleValue() <= 2.0;
                    %>

                        <div class="inventory-item">

                            <span class="food-icon placeholder-icon">
                                —
                            </span>

                            <div>
                                <strong>
                                    <%= Html.escape(item.getName()) %>
                                </strong>

                                <small>
                                    Quantity:
                                    <%= item.getQuantity() %>
                                    <%= Html.escape(item.getUnit()) %>
                                </small>
                            </div>

                            <% if (lowQuantity) { %>

                                <span class="stock low">
                                    Running low
                                </span>

                            <% } else { %>

                                <span class="stock good">
                                    In stock
                                </span>

                            <% } %>

                        </div>

                    <% } %>

                </div>

            <% } %>

            <a class="button secondary full-width"
               href="<%= request.getContextPath() %>/pantry">

                Open pantry
                <span>→</span>
            </a>

        </article>

        <!-- RECIPE SECTION -->

        <article class="panel" id="recipes">

            <div class="panel-header">

                <div>
                    <p class="eyebrow">RECIPE LIBRARY</p>
                    <h2>What can I make?</h2>
                </div>

                <a href="<%= request.getContextPath() %>/consumer-recipes">
                    Browse all →
                </a>

            </div>

            <% if (recipes.isEmpty()) { %>

                <p class="form-help">
                    No recipes are available yet.
                </p>

            <% } else { %>

                <div class="recipe-list">

                    <%
                        int displayedRecipes = 0;

                        for (Recipe recipe : recipes) {
                            if (displayedRecipes >= 3) {
                                break;
                            }

                            displayedRecipes++;
                    %>

                        <a class="recipe-item"
                           href="<%= request.getContextPath() %>/consumer-recipes?id=<%= recipe.getRecipeId() %>">

                            <span class="recipe-icon recipe-placeholder">
                                ◈
                            </span>

                            <div>
                                <strong>
                                    <%= Html.escape(recipe.getName()) %>
                                </strong>

                                <small>
                                    <%= recipe.getIngredients().size() %>
                                    ingredients
                                </small>
                            </div>

                            <span class="status <%= recipe.getFeasibilityClass() %>">
                                <%= recipe.getFeasibilityLabel() %>
                            </span>

                        </a>

                    <% } %>

                </div>

            <% } %>

            <a class="button primary full-width"
               href="<%= request.getContextPath() %>/recipe-form">

                Create your recipe
                <span>+</span>
            </a>

            <a class="button secondary full-width"
               href="<%= request.getContextPath() %>/consumer-recipes">

                Explore recipes
                <span>→</span>
            </a>

        </article>

    </section>

    <!-- OGS SECTION -->

    <section class="next-step" id="orders">

        <div>
            <p class="eyebrow">GROCERY ORDERS</p>

            <h2>
                Need a few more ingredients?
            </h2>

            <p>
                Add missing pantry items to a simulated grocery order.
            </p>
        </div>

        <a class="button light"
           href="<%= request.getContextPath() %>/orders">

            Start an order
            <span>→</span>
        </a>

    </section>

</main>

<footer class="app-footer">

    <span>
        © 2026 Team 10 · WhatsOnTheMenu
    </span>

    <span>
        Consumer workspace
    </span>

</footer>

</body>
</html>