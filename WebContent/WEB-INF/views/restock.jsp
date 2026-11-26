<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.User" %>
<%@ page import="edu.sjsu.team10.model.Restaurant" %>
<%@ page import="edu.sjsu.team10.model.RestockerOption" %>
<%@ page import="edu.sjsu.team10.model.Recipe" %>
<%@ page import="edu.sjsu.team10.model.CatalogItem" %>
<%@ page import="edu.sjsu.team10.model.ShortfallLine" %>
<%@ page import="edu.sjsu.team10.model.RestockRequest" %>
<%@ page import="edu.sjsu.team10.util.Html" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    User loggedInUser = (User) session.getAttribute("user");
    Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
    String ctx = request.getContextPath();

    List<RestockerOption> restockers = (List<RestockerOption>) request.getAttribute("restockers");
    List<Recipe> recipes = (List<Recipe>) request.getAttribute("recipes");
    RestockerOption selectedRestocker = (RestockerOption) request.getAttribute("selectedRestocker");
    Recipe selectedRecipe = (Recipe) request.getAttribute("selectedRecipe");
    List<CatalogItem> catalog = (List<CatalogItem>) request.getAttribute("catalog");
    Map<Integer, String> prefill = (Map<Integer, String>) request.getAttribute("prefill");
    List<ShortfallLine> shortfall = (List<ShortfallLine>) request.getAttribute("shortfall");
    List<RestockRequest> myRequests = (List<RestockRequest>) request.getAttribute("myRequests");

    SimpleDateFormat requestDateFormat = new SimpleDateFormat("MMM d, h:mm a");
    boolean forRestocker = false;

    // result of the last submit (set by RestockServlet's redirect)
    String msg = request.getParameter("msg");
    String flashText = null;
    boolean flashError = false;
    if ("submitted".equals(msg)) { flashText = "Request #" + Html.escape(request.getParameter("id")) + " sent. It is pending until the restocker responds."; }
    else if ("empty".equals(msg)) { flashText = "Enter a quantity for at least one item."; flashError = true; }
    else if ("invalid".equals(msg)) { flashText = "Quantities must be numbers of 0 or more."; flashError = true; }
    else if ("restocker".equals(msg)) { flashText = "That restocker can't take requests right now. Choose another one."; flashError = true; }
    else if ("notcarried".equals(msg)) { flashText = "Nothing was sent: one of the items isn't in this restocker's catalog, or you asked for more than they have available."; flashError = true; }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Restock | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= ctx %>/assets/css/app.css">
</head>
<body class="dashboard-page">
<header class="appbar">
    <a class="brand" href="<%= ctx %>/chef-dashboard"><span class="brand-mark">W</span><span>WhatsOnTheMenu</span></a>
    <nav class="app-nav"><a href="<%= ctx %>/chef-dashboard">Overview</a><a href="<%= ctx %>/inventory">Inventory</a><a href="<%= ctx %>/recipes">Recipes</a><a class="active" href="<%= ctx %>/restock">Restock</a></nav>
    <div class="account-menu">
        <span class="avatar"><%= loggedInUser.getInitials() %></span>
        <span><%= Html.escape(loggedInUser.getName()) %></span>
        <a href="<%= ctx %>/login.jsp">Log out</a>
    </div>
</header>

<main class="dashboard-main">
    <section class="dashboard-heading">
        <div>
            <p class="eyebrow"><%= Html.escape(restaurant.getRestaurantName().toUpperCase()) %></p>
            <h1>Restock</h1>
            <p>Send a request to an approved restocker. Pick items from their catalog, or fill in what a recipe is short.</p>
        </div>
        <a class="button secondary" href="#history">My requests <span>↓</span></a>
    </section>

    <% if (flashText != null) { %>
    <p class="flash <%= flashError ? "flash-error" : "flash-success" %>"><%= flashText %></p>
    <% } %>

    <section class="panel inventory-add">
        <div class="panel-header"><div><p class="eyebrow">STEP 1</p><h2>Choose a restocker</h2></div></div>
        <% if (restockers.isEmpty()) { %>
        <p class="form-help">There are no approved restockers yet. An administrator has to approve one first.</p>
        <% } else { %>
        <form class="add-form" action="<%= ctx %>/restock" method="get">
            <label>
                <span>Restocker</span>
                <select name="restocker" required>
                    <option value="">Choose one…</option>
                    <% for (RestockerOption r : restockers) { %>
                    <option value="<%= r.getUserId() %>" <%= selectedRestocker != null && selectedRestocker.getUserId() == r.getUserId() ? "selected" : "" %>><%= Html.escape(r.getCompanyName()) %> (<%= r.getCatalogSize() %> item<%= r.getCatalogSize() == 1 ? "" : "s" %>)</option>
                    <% } %>
                </select>
            </label>
            <% if (selectedRecipe != null) { %>
            <input type="hidden" name="recipe" value="<%= selectedRecipe.getRecipeId() %>">
            <% } %>
            <button class="button secondary" type="submit">Show catalog</button>
        </form>
        <% } %>
    </section>

    <% if (selectedRestocker != null) { %>
    <section class="panel inventory-add">
        <div class="panel-header">
            <div><p class="eyebrow">STEP 2</p><h2>What do you need from <%= Html.escape(selectedRestocker.getCompanyName()) %>?</h2></div>
        </div>

        <form class="add-form recipe-fill" action="<%= ctx %>/restock" method="get">
            <input type="hidden" name="restocker" value="<%= selectedRestocker.getUserId() %>">
            <label>
                <span>Optional: fill in what a recipe is short</span>
                <select name="recipe">
                    <option value="">No recipe (simple request)</option>
                    <% for (Recipe r : recipes) { %>
                    <option value="<%= r.getRecipeId() %>" <%= selectedRecipe != null && selectedRecipe.getRecipeId() == r.getRecipeId() ? "selected" : "" %>><%= Html.escape(r.getName()) %> · <%= Html.escape(r.getFeasibilityLabel()) %></option>
                    <% } %>
                </select>
            </label>
            <button class="button secondary" type="submit">Apply</button>
        </form>

        <% if (selectedRecipe != null) { %>
            <% if (shortfall.isEmpty()) { %>
            <p class="flash flash-success">Nothing is short for <strong><%= Html.escape(selectedRecipe.getName()) %></strong>. No restock is needed for it.</p>
            <% } else { %>
            <div class="shortfall">
                <p class="eyebrow">SHORT FOR <%= Html.escape(selectedRecipe.getName().toUpperCase()) %></p>
                <% for (ShortfallLine line : shortfall) { %>
                <div class="shortfall-line">
                    <strong><%= Html.escape(line.getName()) %></strong>
                    <span>need <%= line.getNeededDisplay() %> <%= Html.escape(line.getUnit()) %>, have <%= line.getOnHandDisplay() %> → short <%= line.getShortByDisplay() %> <%= Html.escape(line.getUnit()) %></span>
                    <% if (ShortfallLine.PREFILLED.equals(line.getOutcome())) { %>
                    <span class="status makeable">Filled in below</span>
                    <% } else if (ShortfallLine.UNIT_MISMATCH.equals(line.getOutcome())) { %>
                    <span class="status almost">Sold in a different unit, enter it yourself</span>
                    <% } else { %>
                    <span class="status not-ready">Not carried by this restocker</span>
                    <% } %>
                </div>
                <% } %>
            </div>
            <% } %>
        <% } %>

        <% if (catalog.isEmpty()) { %>
        <p class="form-help">This restocker hasn't listed anything in their catalog yet.</p>
        <% } else { %>
        <form action="<%= ctx %>/restock" method="post">
            <input type="hidden" name="restockerId" value="<%= selectedRestocker.getUserId() %>">
            <% if (selectedRecipe != null && !shortfall.isEmpty()) { %>
            <input type="hidden" name="recipeId" value="<%= selectedRecipe.getRecipeId() %>">
            <% } %>

            <% for (CatalogItem item : catalog) {
                   String value = prefill.get(item.getIngId()); %>
            <div class="stock-row">
                <span class="food-icon green"><%= Html.escape(item.getName().substring(0, 1).toUpperCase()) %></span>
                <div class="stock-name">
                    <strong><%= Html.escape(item.getName()) %></strong>
                    <small>$<%= item.getPriceDisplay() %> per <%= item.getUnit().isEmpty() ? "unit" : Html.escape(item.getUnit()) %> · <%= item.getQtyDisplay() %> available</small>
                </div>
                <div class="qty-form">
                    <input type="number" name="qty_<%= item.getIngId() %>" min="0" max="<%= item.getQtyDisplay() %>" step="0.01"
                           value="<%= value == null ? "" : value %>" placeholder="0" <%= item.isAvailable() ? "" : "disabled" %>
                           aria-label="Quantity of <%= Html.escape(item.getName()) %> to request">
                    <span class="unit"><%= Html.escape(item.getUnit()) %></span>
                </div>
            </div>
            <% } %>

            <button class="button primary" type="submit">Send request <span>→</span></button>
        </form>
        <% } %>
    </section>
    <% } %>

    <section class="panel" id="history">
        <div class="panel-header"><div><p class="eyebrow">MY REQUESTS</p><h2><%= myRequests.size() %> request<%= myRequests.size() == 1 ? "" : "s" %></h2></div></div>

        <% if (myRequests.isEmpty()) { %>
        <p class="form-help">You haven't sent any restock requests yet.</p>
        <% } %>

        <div class="request-list">
        <% for (RestockRequest rq : myRequests) { %>
            <%@ include file="restock-request-card.jspf" %>
        <% } %>
        </div>
    </section>
</main>
<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span><span>Chef workspace</span></footer>
</body>
</html>
