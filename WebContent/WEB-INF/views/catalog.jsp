<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.CatalogItem" %>
<%@ page import="edu.sjsu.team10.model.Ingredient" %>
<%@ page import="edu.sjsu.team10.util.Html" %>
<%@ page import="java.util.List" %>
<%
    String restockerNav = "catalog";
    List<CatalogItem> catalog = (List<CatalogItem>) request.getAttribute("catalog");
    List<Ingredient> availableIngredients = (List<Ingredient>) request.getAttribute("availableIngredients");

    // result of the last add/update/remove (set by CatalogServlet's redirect)
    String msg = request.getParameter("msg");
    String flashText = null;
    boolean flashError = false;
    if ("added".equals(msg)) { flashText = "Item added to your catalog."; }
    else if ("updated".equals(msg)) { flashText = "Price and quantity saved."; }
    else if ("removed".equals(msg)) { flashText = "Item removed from your catalog. The ingredient stays in the shared list."; }
    else if ("duplicate".equals(msg)) { flashText = "That ingredient is already in your catalog. Edit its row instead."; flashError = true; }
    else if ("notfound".equals(msg)) { flashText = "That item is not in your catalog."; flashError = true; }
    else if ("invalid".equals(msg)) { flashText = "Enter a price and quantity of 0 or more, and choose or name an ingredient."; flashError = true; }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Catalog | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css">
</head>
<body class="dashboard-page">
<%@ include file="restocker-header.jspf" %>

<main class="dashboard-main">
    <section class="dashboard-heading">
        <div>
            <p class="eyebrow">RESTOCKER</p>
            <h1>Your catalog</h1>
            <p>The ingredients you supply, your price per unit, and how much you have available. Chefs can only request what is listed here.</p>
        </div>
        <a class="button secondary" href="<%= ctx %>/restocker/requests">Incoming requests <span>→</span></a>
    </section>

    <% if (flashText != null) { %>
    <p class="flash <%= flashError ? "flash-error" : "flash-success" %>"><%= flashText %></p>
    <% } %>

    <section class="panel inventory-add">
        <div class="panel-header"><div><p class="eyebrow">ADD TO CATALOG</p><h2>List an ingredient</h2></div></div>
        <form class="add-form" action="<%= ctx %>/catalog" method="post">
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
                <span>Price / unit ($)</span>
                <input type="number" name="price" min="0" step="0.01" required placeholder="0.00">
            </label>

            <label class="add-qty">
                <span>Available</span>
                <input type="number" name="qty" min="0" step="0.01" required placeholder="0">
            </label>

            <button class="button primary" type="submit">Add <span>+</span></button>
        </form>
    </section>

    <section class="panel">
        <div class="panel-header"><div><p class="eyebrow">CATALOG</p><h2><%= catalog.size() %> item<%= catalog.size() == 1 ? "" : "s" %></h2></div></div>

        <% if (catalog.isEmpty()) { %>
        <p class="form-help">Your catalog is empty. Add an ingredient above so chefs can request it.</p>
        <% } %>

        <% for (CatalogItem item : catalog) { %>
        <div class="stock-row">
            <span class="food-icon orange"><%= Html.escape(item.getName().substring(0, 1).toUpperCase()) %></span>
            <div class="stock-name">
                <strong><%= Html.escape(item.getName()) %></strong>
                <small>Sold per <%= item.getUnit().isEmpty() ? "unit" : Html.escape(item.getUnit()) %></small>
            </div>
            <% if (item.isAvailable()) { %>
            <span class="stock good">Available</span>
            <% } else { %>
            <span class="stock not-ready">Sold out</span>
            <% } %>

            <form class="qty-form" action="<%= ctx %>/catalog" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="ingId" value="<%= item.getIngId() %>">
                <span class="unit">$</span>
                <input type="number" name="price" min="0" step="0.01" required value="<%= item.getPriceDisplay() %>" aria-label="Price of <%= Html.escape(item.getName()) %>">
                <input type="number" name="qty" min="0" step="0.01" required value="<%= item.getQtyDisplay() %>" aria-label="Available quantity of <%= Html.escape(item.getName()) %>">
                <span class="unit"><%= Html.escape(item.getUnit()) %></span>
                <button class="button secondary small" type="submit">Save</button>
            </form>

            <form action="<%= ctx %>/catalog" method="post"
                  onsubmit="return confirm('Remove this item from your catalog?');">
                <input type="hidden" name="action" value="remove">
                <input type="hidden" name="ingId" value="<%= item.getIngId() %>">
                <button class="link-button" type="submit">Remove</button>
            </form>
        </div>
        <% } %>
    </section>
</main>
<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span><span>Restocker workspace</span></footer>
</body>
</html>
