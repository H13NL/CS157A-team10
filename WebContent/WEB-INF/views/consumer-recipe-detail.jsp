<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.Recipe" %>
<%@ page import="edu.sjsu.team10.model.RecipeIngredient" %>
<%@ page import="edu.sjsu.team10.util.Html" %>

<%
    String userName = (String) session.getAttribute("userName");
    if (userName == null || userName.trim().isEmpty()) {
        userName = "Consumer";
    }

    Recipe recipe = (Recipe) request.getAttribute("recipe");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= Html.escape(recipe.getName()) %> | WhatsOnTheMenu</title>
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

<main class="dashboard-main recipe-detail-page">

    <a class="back-link"
       href="<%= request.getContextPath() %>/consumer-recipes">
        ← Back to recipe library
    </a>

    <section class="detail-hero panel">
        <div>
            <div class="detail-kicker">
                <span class="visibility-pill <%= recipe.isPublic() ? "public" : "private" %>">
                    <%= recipe.isPublic() ? "Public recipe" : "Private recipe" %>
                </span>

                <span class="detail-owner">
                    By <%= Html.escape(recipe.getCreatorName()) %>
                </span>
            </div>

            <h1><%= Html.escape(recipe.getName()) %></h1>
            <p><%= Html.escape(recipe.getDescription()) %></p>
        </div>

        <div class="detail-actions">
            <% if (!recipe.isPublic()) { %>
                <form action="<%= request.getContextPath() %>/delete-consumer-recipe"
                      method="post"
                      onsubmit="return confirm('Delete this recipe?');">

                    <input type="hidden"
                           name="recipeId"
                           value="<%= recipe.getRecipeId() %>">

                    <button class="button secondary" type="submit">
                        Remove recipe
                    </button>
                </form>
            <% } %>
        </div>
    </section>

    <section class="detail-grid">

        <article class="panel ingredient-panel">
            <div class="panel-header">
                <div>
                    <p class="eyebrow">PANTRY CHECK</p>
                    <h2>Required ingredients</h2>
                </div>

                <span class="status <%= recipe.getFeasibilityClass() %>">
                    <%= recipe.getFeasibilityLabel() %>
                </span>
            </div>

            <div class="detail-ingredients">
                <% for (RecipeIngredient ingredient : recipe.getIngredients()) { %>
                    <div class="detail-ingredient">

                        <span class="food-icon
                            <%= ingredient.isSatisfied() ? "green" : "orange" %>">
                            <%= ingredient.isSatisfied() ? "✓" : "!" %>
                        </span>

                        <div>
                            <strong>
                                <%= Html.escape(ingredient.getName()) %>
                            </strong>

                            <small>
                                Need <%= ingredient.getQuantityDisplay() %>
                                <%= Html.escape(ingredient.getUnit()) %>
                                · Have <%= ingredient.getAvailableDisplay() %>
                                <%= Html.escape(ingredient.getAvailableUnit()) %>
                            </small>
                        </div>

                        <span class="ingredient-check">
                            <%= ingredient.isSatisfied() ? "Ready" : "Short" %>
                        </span>
                    </div>
                <% } %>
            </div>
        </article>

        <aside class="panel feasibility-panel">
            <p class="eyebrow">RECIPE ACTION</p>

            <% if (recipe.getMissingCount() == 0) { %>
                <h2>You can make this now.</h2>
                <p>
                    Your pantry has enough ingredients.
                </p>

                <form action="<%= request.getContextPath() %>/consumer-cook-recipe"
      				method="post">

                    <input type="hidden"
                           name="recipeId"
                           value="<%= recipe.getRecipeId() %>">

                    <button class="button primary full-width"
                            type="submit">
                        Make this recipe
                        <span>→</span>
                    </button>
                </form>
            <% } else { %>
                <h2>More ingredients are needed.</h2>
                <p>
                    Add the missing ingredients to your pantry before cooking.
                </p>

                <a class="button secondary full-width"
                   href="<%= request.getContextPath() %>/pantry">
                    Open pantry
                    <span>→</span>
                </a>
            <% } %>
        </aside>

    </section>
</main>

<footer class="app-footer">
    <span>© 2026 Team 10 · WhatsOnTheMenu</span>
    <span>Consumer recipe library</span>
</footer>

</body>
</html>
