<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="edu.sjsu.team10.model.OrderIngredient" %>
<%@ page import="edu.sjsu.team10.model.User" %>
<%@ page import="edu.sjsu.team10.util.Html" %>
<%
    User loggedInUser = (User) session.getAttribute("user");
    List<OrderIngredient> ingredients =
        (List<OrderIngredient>) request.getAttribute("orderIngredients");
    String message = request.getParameter("msg");
    String orderId = request.getParameter("id");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Start an Order | WhatsOnTheMenu</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css">
</head>
<body class="dashboard-page consumer-page">
<header class="appbar">
    <a class="brand" href="<%= request.getContextPath() %>/consumer-dashboard">
        <span class="brand-mark">W</span><span>WhatsOnTheMenu</span>
    </a>
    <nav class="app-nav">
        <a href="<%= request.getContextPath() %>/consumer-dashboard">Overview</a>
        <a href="<%= request.getContextPath() %>/pantry">My pantry</a>
        <a href="<%= request.getContextPath() %>/recipes">Recipes</a>
        <a class="active" href="<%= request.getContextPath() %>/orders">Orders</a>
    </nav>
    <div class="account-menu">
        <span class="avatar"><%= loggedInUser.getInitials() %></span>
        <span><%= Html.escape(loggedInUser.getName()) %></span>
        <a href="<%= request.getContextPath() %>/login.jsp">Log out</a>
    </div>
</header>

<main class="dashboard-main order-page">
    <p class="eyebrow">CONSUMER WORKSPACE</p>
    <section class="dashboard-heading">
        <div>
            <h1>Start a grocery order.</h1>
            <p>Select the ingredients and quantities you want to add to your pantry.</p>
        </div>
        <a class="button secondary" href="<%= request.getContextPath() %>/pantry">View pantry <span>→</span></a>
    </section>

    <% if ("created".equals(message)) { %>
        <p class="flash flash-success">Order #<%= Html.escape(orderId) %> was placed and your pantry was updated.</p>
    <% } else if ("empty".equals(message)) { %>
        <p class="flash flash-error">Add at least one ingredient before placing the order.</p>
    <% } else if ("invalid".equals(message)) { %>
        <p class="flash flash-error">Choose an ingredient and enter a quantity greater than zero.</p>
    <% } %>

    <form action="<%= request.getContextPath() %>/orders" method="post" id="order-form">
        <section class="panel inventory-add order-add">
            <div class="panel-header"><div><p class="eyebrow">ADD TO ORDER</p><h2>Select an ingredient</h2></div></div>
            <% if (ingredients == null || ingredients.isEmpty()) { %>
                <p class="form-help">There are no shared ingredients available yet.</p>
            <% } else { %>
                <div class="add-form">
                    <label>
                        <span>Order ingredient</span>
                        <select id="order-ingredient">
                            <option value="">Choose one…</option>
                            <% for (OrderIngredient ingredient : ingredients) { %>
                                <option value="<%= ingredient.getIngredientId() %>"
                                        data-name="<%= Html.escape(ingredient.getName()) %>"
                                        data-unit="<%= Html.escape(ingredient.getUnit()) %>"
                                        data-pantry="<%= ingredient.isInPantry() ? ingredient.getPantryQuantityDisplay() + " " + Html.escape(ingredient.getUnit()) : "" %>">
                                    <%= Html.escape(ingredient.getName()) %><%= ingredient.getUnit().isEmpty() ? "" : " (" + Html.escape(ingredient.getUnit()) + ")" %>
                                </option>
                            <% } %>
                        </select>
                    </label>
                    <label class="add-qty">
                        <span>Quantity</span>
                        <input id="order-add-quantity" type="number" min="0.01" max="99999999.99" step="0.01" placeholder="0">
                    </label>
                    <button class="button primary" type="button" id="add-order-item">Add <span>+</span></button>
                </div>
            <% } %>
        </section>

        <section class="panel order-cart-panel">
            <div class="panel-header"><div><p class="eyebrow">YOUR ORDER</p><h2><span id="order-count">0</span> ingredients</h2></div></div>
            <div id="order-cart" class="order-cart">
                <p class="form-help" id="empty-order">No ingredients added yet.</p>
            </div>
            <div class="editor-actions order-actions">
                <a class="button secondary" href="<%= request.getContextPath() %>/consumer-dashboard">Cancel</a>
                <button class="button primary" id="place-order" type="submit" disabled>Place order <span>→</span></button>
            </div>
        </section>
    </form>
</main>

<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span></footer>
<script>
const ingredientPicker = document.getElementById('order-ingredient');
const addQuantity = document.getElementById('order-add-quantity');
const addItemButton = document.getElementById('add-order-item');
const orderCart = document.getElementById('order-cart');
const emptyOrder = document.getElementById('empty-order');
const orderCount = document.getElementById('order-count');
const placeOrder = document.getElementById('place-order');

function refreshOrder() {
    const count = orderCart.querySelectorAll('.order-cart-row').length;
    orderCount.textContent = count;
    emptyOrder.hidden = count > 0;
    placeOrder.disabled = count === 0;
}

if (addItemButton) {
    addItemButton.addEventListener('click', () => {
        const option = ingredientPicker.selectedOptions[0];
        const amount = Number(addQuantity.value);
        if (!ingredientPicker.value) {
            ingredientPicker.focus();
            return;
        }
        if (!Number.isFinite(amount) || amount <= 0) {
            addQuantity.setCustomValidity('Enter a quantity greater than zero.');
            addQuantity.reportValidity();
            return;
        }
        addQuantity.setCustomValidity('');

        const row = document.createElement('div');
        row.className = 'stock-row order-cart-row';
        row.dataset.ingredientId = ingredientPicker.value;

        const icon = document.createElement('span');
        icon.className = 'food-icon green';
        icon.textContent = option.dataset.name.charAt(0).toUpperCase();

        const details = document.createElement('div');
        details.className = 'stock-name';
        const name = document.createElement('strong');
        name.textContent = option.dataset.name;
        const pantry = document.createElement('small');
        pantry.textContent = option.dataset.pantry
            ? 'Currently in pantry: ' + option.dataset.pantry
            : 'Not currently in your pantry';
        details.append(name, pantry);

        const quantityGroup = document.createElement('div');
        quantityGroup.className = 'qty-form';
        const idInput = document.createElement('input');
        idInput.type = 'hidden';
        idInput.name = 'ingredientId';
        idInput.value = ingredientPicker.value;
        const quantityInput = document.createElement('input');
        quantityInput.type = 'number';
        quantityInput.name = 'quantity';
        quantityInput.min = '0.01';
        quantityInput.max = '99999999.99';
        quantityInput.step = '0.01';
        quantityInput.required = true;
        quantityInput.value = addQuantity.value;
        const unit = document.createElement('span');
        unit.className = 'unit';
        unit.textContent = option.dataset.unit;
        quantityGroup.append(idInput, quantityInput, unit);

        const remove = document.createElement('button');
        remove.type = 'button';
        remove.className = 'link-button';
        remove.textContent = 'Remove';
        remove.addEventListener('click', () => {
            option.disabled = false;
            row.remove();
            refreshOrder();
        });

        row.append(icon, details, quantityGroup, remove);
        orderCart.appendChild(row);
        option.disabled = true;
        ingredientPicker.value = '';
        addQuantity.value = '';
        refreshOrder();
    });
}
</script>
</body>
</html>
