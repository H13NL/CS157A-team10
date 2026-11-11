<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="edu.sjsu.team10.model.PantryItem" %>
<%@ page import="edu.sjsu.team10.model.Ingredient" %>
<%
    String userName = (String) session.getAttribute("userName");
    if (userName == null || userName.trim().isEmpty()) {
        userName = "Consumer";
    }

    List<PantryItem> pantryItems =
        (List<PantryItem>) request.getAttribute("pantryItems");
    List<Ingredient> availableIngredients =
        (List<Ingredient>) request.getAttribute("availableIngredients");
    String message = request.getParameter("msg");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Pantry | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css">
</head>
<body class="dashboard-page">
<header class="appbar">
    <a class="brand" href="<%= request.getContextPath() %>/consumer-dashboard">
        <span class="brand-mark">W</span>
        <span>WhatsOnTheMenu</span>
    </a>
    <nav class="app-nav">
        <a href="<%= request.getContextPath() %>/consumer-dashboard">Overview</a>
        <a class="active" href="<%= request.getContextPath() %>/pantry">My pantry</a>
        <a href="#">Recipes</a>
        <a href="#">Orders</a>
    </nav>
    <div class="account-menu">
        <span class="avatar">CU</span>
        <span><%= userName %></span>
        <a href="<%= request.getContextPath() %>/login.jsp">Log out</a>
    </div>
</header>

<main class="dashboard-main">
    <section class="dashboard-heading">
        <div>
            <p class="eyebrow">CONSUMER WORKSPACE</p>
            <h1>My pantry.</h1>
            <p>Keep your ingredient quantities up to date.</p>
        </div>
    </section>

    <% if (message != null) { %>
        <p class="demo-note">
            <% if ("added".equals(message)) { %>Ingredient added.
            <% } else if ("updated".equals(message)) { %>Quantity updated.
            <% } else if ("removed".equals(message)) { %>Ingredient removed.
            <% } else if ("duplicate".equals(message)) { %>That ingredient is already in your pantry.
            <% } else { %>Please check the submitted values.
            <% } %>
        </p>
    <% } %>

    <section class="dashboard-grid">
        <article class="panel">
            <div class="panel-header">
                <div>
                    <p class="eyebrow">CURRENT PANTRY</p>
                    <h2>Ingredients</h2>
                </div>
            </div>

            <% if (pantryItems == null || pantryItems.isEmpty()) { %>
                <p class="form-help">Your pantry is empty. Add an ingredient to get started.</p>
            <% } else { %>
                <div class="inventory-list">
                    <% for (PantryItem item : pantryItems) { %>
                        <div class="inventory-item">
                            <span class="food-icon placeholder-icon">—</span>
                            <div>
                                <strong><%= item.getName() %></strong>
                                <small>Quantity: <%= item.getQuantity() %> <%= item.getUnit() %></small>
                            </div>
                            <form action="<%= request.getContextPath() %>/pantry" method="post">
                                <input type="hidden" name="action" value="update">
                                <input type="hidden" name="ingId" value="<%= item.getIngredientId() %>">
                                <input name="qty" type="number" min="0" step="0.01" value="<%= item.getQuantity() %>" required>
                                <button class="button secondary" type="submit">Update</button>
                            </form>
                            <form action="<%= request.getContextPath() %>/pantry" method="post">
                                <input type="hidden" name="action" value="remove">
                                <input type="hidden" name="ingId" value="<%= item.getIngredientId() %>">
                                <button class="button light" type="submit">Remove</button>
                            </form>
                        </div>
                    <% } %>
                </div>
            <% } %>
        </article>

        <article class="panel">
            <div class="panel-header">
                <div>
                    <p class="eyebrow">ADD INGREDIENT</p>
                    <h2>Grow your pantry</h2>
                </div>
            </div>

            <form action="<%= request.getContextPath() %>/pantry" method="post">
                <input type="hidden" name="action" value="add">

                <label for="ingId">Ingredient</label>
                <select id="ingId" name="ingId" required>
                    <option value="">Choose an ingredient</option>
                    <% if (availableIngredients != null) {
                           for (Ingredient ingredient : availableIngredients) { %>
                        <option value="<%= ingredient.getIngId() %>">
                            <%= ingredient.getName() %> (<%= ingredient.getUnit() %>)
                        </option>
                    <%     }
                       } %>
                </select>

                <label for="qty">Quantity</label>
                <input id="qty" name="qty" type="number" min="0" step="0.01" value="0" required>

                <button class="button primary full-width" type="submit">
                    Add to pantry <span>+</span>
                </button>
            </form>
        </article>
    </section>
</main>

<footer class="app-footer">
    <span>© 2026 Team 10 · WhatsOnTheMenu</span>
    <span>Consumer workspace</span>
</footer>
</body>
</html>
