<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.User" %>
<%@ page import="edu.sjsu.team10.model.Restaurant" %>
<%@ page import="edu.sjsu.team10.model.StockItem" %>
<%@ page import="edu.sjsu.team10.model.Ingredient" %>
<%@ page import="edu.sjsu.team10.util.Html" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%
    User loggedInUser = (User) session.getAttribute("user");
    Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
    List<StockItem> inventory = (List<StockItem>) request.getAttribute("inventory");
    List<Ingredient> availableIngredients = (List<Ingredient>) request.getAttribute("availableIngredients");
    SimpleDateFormat updatedFormat = new SimpleDateFormat("MMM d, h:mm a");

    // result of the last add/update/remove (set by InventoryServlet's redirect)
    String msg = request.getParameter("msg");
    String flashText = null;
    boolean flashError = false;
    if ("updated".equals(msg)) { flashText = "Quantity updated."; }
    else if ("added".equals(msg)) { flashText = "Ingredient added to inventory."; }
    else if ("removed".equals(msg)) { flashText = "Ingredient removed from inventory."; }
    else if ("duplicate".equals(msg)) { flashText = "That ingredient is already in your inventory. Update its quantity instead."; flashError = true; }
    else if ("notfound".equals(msg)) { flashText = "That ingredient is not in your inventory."; flashError = true; }
    else if ("invalid".equals(msg)) { flashText = "Please enter a valid quantity (0 or more) and choose or name an ingredient."; flashError = true; }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inventory | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css">
</head>
<body class="dashboard-page">
<header class="appbar">
    <a class="brand" href="<%= request.getContextPath() %>/chef-dashboard"><span class="brand-mark">W</span><span>WhatsOnTheMenu</span></a>
    <nav class="app-nav"><a href="<%= request.getContextPath() %>/chef-dashboard">Overview</a><a class="active" href="<%= request.getContextPath() %>/inventory">Inventory</a><a href="<%= request.getContextPath() %>/recipes">Recipes</a><a href="#">Restock</a></nav>

	<div class="account-menu">
	    <span class="avatar"><%= loggedInUser.getInitials() %></span>
	    <span><%= Html.escape(loggedInUser.getName()) %></span>
	    <a href="<%= request.getContextPath() %>/login.jsp">Log out</a>
	</div>
</header>

<main class="dashboard-main">
    <section class="dashboard-heading">
        <div>
            <p class="eyebrow"><%= Html.escape(restaurant.getRestaurantName().toUpperCase()) %></p>
            <h1>Inventory</h1>
            <p>Shared stock for everyone working at <strong><%= Html.escape(restaurant.getRestaurantName()) %></strong>.</p>
        </div>
    </section>

    <% if (flashText != null) { %>
    <p class="flash <%= flashError ? "flash-error" : "flash-success" %>"><%= flashText %></p>
    <% } %>

    <section class="panel inventory-add">
        <div class="panel-header"><div><p class="eyebrow">ADD TO INVENTORY</p><h2>Stock an ingredient</h2></div></div>
        <form class="add-form" action="<%= request.getContextPath() %>/inventory" method="post">
            <input type="hidden" name="action" value="add">

            <label>
                <span>Existing ingredient</span>
                <select name="ingId">
                    <option value="">Choose one…</option>
                    <% for (Ingredient ing : availableIngredients) { %>
                    <option value="<%= ing.getIngId() %>"><%= Html.escape(ing.getName()) %><%= ing.getUnit().isEmpty() ? "" : " (" + Html.escape(ing.getUnit()) + ")" %></option>
                    <% } %>
                </select>
            </label>

            <span class="add-or">or</span>

            <label>
                <span>New ingredient name</span>
                <input type="text" name="newName" maxlength="100" placeholder="e.g. Basil">
            </label>

            <label class="add-unit">
                <span>Unit</span>
                <input type="text" name="newUnit" maxlength="20" placeholder="e.g. g">
            </label>

            <label class="add-qty">
                <span>Quantity</span>
                <input type="number" name="qty" min="0" step="0.01" required placeholder="0">
            </label>

            <button class="button primary" type="submit">Add <span>+</span></button>
        </form>
    </section>

    <section class="panel">
        <div class="panel-header"><div><p class="eyebrow">CURRENT STOCK</p><h2><%= inventory.size() %> ingredient<%= inventory.size() == 1 ? "" : "s" %></h2></div></div>

        <% if (inventory.isEmpty()) { %>
        <p class="form-help">No ingredients yet. Add one above to start tracking your stock.</p>
        <% } %>

        <% for (StockItem item : inventory) { %>
        <div class="stock-row">
            <span class="food-icon green"><%= Html.escape(item.getName().substring(0, 1).toUpperCase()) %></span>
            <div class="stock-name">
                <strong><%= Html.escape(item.getName()) %></strong>
                <small>Updated <%= item.getLastUpdate() == null ? "—" : updatedFormat.format(item.getLastUpdate()) %></small>
            </div>
            <% if (item.isInStock()) { %>
            <span class="stock good">In stock</span>
            <% } else { %>
            <span class="stock not-ready">Out of stock</span>
            <% } %>

            <form class="qty-form" action="<%= request.getContextPath() %>/inventory" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="ingId" value="<%= item.getIngId() %>">
                <input type="number" name="qty" min="0" step="0.01" required value="<%= item.getQtyDisplay() %>" aria-label="Quantity of <%= Html.escape(item.getName()) %>">
                <span class="unit"><%= Html.escape(item.getUnit()) %></span>
                <button class="button secondary small" type="submit">Save</button>
            </form>

            <form action="<%= request.getContextPath() %>/inventory" method="post"
                  onsubmit="return confirm('Remove this ingredient from inventory?');">
                <input type="hidden" name="action" value="remove">
                <input type="hidden" name="ingId" value="<%= item.getIngId() %>">
                <button class="link-button" type="submit">Remove</button>
            </form>
        </div>
        <% } %>
    </section>
</main>
<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span><span>Chef workspace</span></footer>
</body>
</html>
