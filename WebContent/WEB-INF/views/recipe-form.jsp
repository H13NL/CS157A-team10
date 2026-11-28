<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="edu.sjsu.team10.model.User" %>
<%@ page import="edu.sjsu.team10.model.Recipe" %>
<%@ page import="edu.sjsu.team10.model.RecipeIngredient" %>
<%@ page import="edu.sjsu.team10.model.Ingredient" %>
<%@ page import="edu.sjsu.team10.util.Html" %>
<%@ page import="java.util.List" %>
<%
    User loggedInUser = (User) session.getAttribute("user");
    Recipe recipe = (Recipe) request.getAttribute("recipe");
    List<Ingredient> ingredients = (List<Ingredient>) request.getAttribute("ingredients");
    boolean editing = recipe != null;
    String formName = (String) request.getAttribute("formName");
    String formDescription = (String) request.getAttribute("formDescription");
    String formVisibility = (String) request.getAttribute("formVisibility");
    if (formName == null) formName = editing ? recipe.getName() : "";
    if (formDescription == null) formDescription = editing ? recipe.getDescription() : "";
    if (formVisibility == null) formVisibility = editing && recipe.isPublic() ? "public" : "private";
%>
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0"><title><%= editing ? "Edit recipe" : "Create recipe" %> | WhatsOnTheMenu</title><link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/app.css"></head>
<body class="dashboard-page">
<header class="appbar"><a class="brand" href="<%= request.getContextPath() %>/chef-dashboard"><span class="brand-mark">W</span><span>WhatsOnTheMenu</span></a><nav class="app-nav"><a href="<%= request.getContextPath() %>/chef-dashboard">Overview</a><a href="<%= request.getContextPath() %>/inventory">Inventory</a><a class="active" href="<%= request.getContextPath() %>/recipes">Recipes</a><a href="<%= request.getContextPath() %>/restock">Restock</a></nav><div class="account-menu"><span class="avatar"><%= loggedInUser.getInitials() %></span><span><%= Html.escape(loggedInUser.getName()) %></span><a href="<%= request.getContextPath() %>/login.jsp">Log out</a></div></header>
<main class="dashboard-main recipe-form-page">
    <a class="back-link" href="<%= request.getContextPath() %>/recipes">← Back to recipe library</a>
    <section class="dashboard-heading"><div><p class="eyebrow"><%= editing ? "EDIT RECIPE" : "NEW RECIPE" %></p><h1><%= editing ? "Edit your recipe." : "Create something worth sharing." %></h1><p>Build a recipe with exact quantities, units, and ingredients from your shared kitchen list.</p></div></section>
    <% if (request.getAttribute("error") != null) { %><div class="flash flash-error"><%= Html.escape(request.getAttribute("error")) %></div><% } %>
    <form class="recipe-editor" action="<%= request.getContextPath() %>/recipe-form" method="post">
        <% if (editing) { %><input type="hidden" name="recipeId" value="<%= recipe.getRecipeId() %>"><% } %>
        <section class="panel editor-basics">
            <p class="eyebrow">RECIPE BASICS</p>
            <label>Recipe name<input type="text" name="name" maxlength="150" required value="<%= Html.escape(formName) %>" placeholder="e.g. Garden vegetable ramen"></label>
            <label>Description<textarea name="description" maxlength="4000" rows="4" placeholder="What makes this recipe special?"><%= Html.escape(formDescription) %></textarea></label>
            <fieldset class="visibility-options">
                <legend>Who can view this recipe?</legend>
                <label class="visibility-choice">
                    <input type="radio" name="visibility" value="private" <%= "private".equals(formVisibility) ? "checked" : "" %>>
                    <span><strong>Private</strong><small>Only you can view and edit this recipe.</small></span>
                </label>
                <label class="visibility-choice">
                    <input type="radio" name="visibility" value="public" <%= "public".equals(formVisibility) ? "checked" : "" %>>
                    <span><strong>Public</strong><small>Every chef and consumer can view it. Only you can edit it.</small></span>
                </label>
            </fieldset>
        </section>
        <section class="panel editor-ingredients"><div class="panel-header"><div><p class="eyebrow">REQUIRED INGREDIENTS</p><h2>Build the ingredient list</h2></div><button class="button secondary small" type="button" id="add-ingredient">Add ingredient <span>+</span></button></div><p class="form-help">Choose an existing ingredient or type a new one to add it to the shared ingredient list.</p><div id="ingredient-rows">
        <% if (editing && !recipe.getIngredients().isEmpty()) { for (RecipeIngredient item : recipe.getIngredients()) { %><div class="ingredient-editor-row"><select name="ingredientId" class="ingredient-select"><option value="">New ingredient…</option><% for (Ingredient option : ingredients) { %><option value="<%= option.getIngId() %>" data-unit="<%= Html.escape(option.getUnit()) %>" <%= option.getIngId() == item.getIngredientId() ? "selected" : "" %>><%= Html.escape(option.getName()) %></option><% } %></select><input type="text" name="ingredientName" placeholder="Or new ingredient"><input type="number" name="quantity" min="0.01" step="0.01" required value="<%= item.getQuantityDisplay() %>"><input type="text" name="unit" maxlength="20" required value="<%= Html.escape(item.getUnit()) %>" placeholder="unit"><button type="button" class="remove-ingredient" aria-label="Remove ingredient">×</button></div><% } } else { %><div class="ingredient-editor-row"><select name="ingredientId" class="ingredient-select"><option value="">New ingredient…</option><% for (Ingredient option : ingredients) { %><option value="<%= option.getIngId() %>" data-unit="<%= Html.escape(option.getUnit()) %>"><%= Html.escape(option.getName()) %></option><% } %></select><input type="text" name="ingredientName" placeholder="Or new ingredient"><input type="number" name="quantity" min="0.01" step="0.01" required placeholder="0"><input type="text" name="unit" maxlength="20" required placeholder="unit"><button type="button" class="remove-ingredient" aria-label="Remove ingredient">×</button></div><% } %></div></section>
        <div class="editor-actions">
            <% if (editing) { %>
                <button class="button danger delete-recipe" type="button" id="open-delete-recipe">Delete recipe</button>
            <% } %>
            <a class="button secondary" href="<%= request.getContextPath() %>/recipes">Cancel</a>
            <button class="button primary" type="submit"><%= editing ? "Save changes" : "Create recipe" %> <span>→</span></button>
        </div>
    </form>
    <% if (editing) { %>
        <div class="modal-backdrop" id="delete-recipe-modal" hidden>
            <section class="confirmation-modal" role="dialog" aria-modal="true" aria-labelledby="delete-recipe-title" aria-describedby="delete-recipe-description">
                <span class="modal-icon" aria-hidden="true">!</span>
                <p class="eyebrow">DELETE RECIPE</p>
                <h2 id="delete-recipe-title">Delete <%= Html.escape(recipe.getName()) %>?</h2>
                <p id="delete-recipe-description">This permanently removes the recipe and its ingredient list. This action cannot be undone.</p>
                <div class="modal-actions">
                    <button class="button secondary" type="button" id="cancel-delete-recipe">Cancel</button>
                    <form action="<%= request.getContextPath() %>/recipe-delete" method="post">
                        <input type="hidden" name="recipeId" value="<%= recipe.getRecipeId() %>">
                        <button class="button danger" type="submit">Delete recipe</button>
                    </form>
                </div>
            </section>
        </div>
    <% } %>
</main>
<footer class="app-footer"><span>© 2026 Team 10 · WhatsOnTheMenu</span><span>Recipe editor</span></footer>
<script>
const rows = document.getElementById('ingredient-rows');
document.getElementById('add-ingredient').addEventListener('click', () => {
    const row = rows.firstElementChild.cloneNode(true);
    row.querySelector('select').selectedIndex = 0;
    row.querySelector('[name=ingredientName]').value = '';
    row.querySelector('[name=quantity]').value = '';
    row.querySelector('[name=unit]').value = '';
    rows.appendChild(row);
});
rows.addEventListener('click', event => {
    if (event.target.classList.contains('remove-ingredient') && rows.children.length > 1) event.target.closest('.ingredient-editor-row').remove();
});
rows.addEventListener('change', event => {
    if (event.target.classList.contains('ingredient-select')) {
        const option = event.target.selectedOptions[0];
        const unit = event.target.closest('.ingredient-editor-row').querySelector('[name=unit]');
        if (event.target.value && option.dataset.unit) {
            unit.value = option.dataset.unit;
            unit.readOnly = true;
        } else {
            unit.value = '';
            unit.readOnly = false;
        }
    }
});

const deleteModal = document.getElementById('delete-recipe-modal');
const openDelete = document.getElementById('open-delete-recipe');
const cancelDelete = document.getElementById('cancel-delete-recipe');
if (deleteModal && openDelete && cancelDelete) {
    const closeDeleteModal = () => {
        deleteModal.hidden = true;
        openDelete.focus();
    };
    openDelete.addEventListener('click', () => {
        deleteModal.hidden = false;
        cancelDelete.focus();
    });
    cancelDelete.addEventListener('click', closeDeleteModal);
    deleteModal.addEventListener('click', event => {
        if (event.target === deleteModal) closeDeleteModal();
    });
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && !deleteModal.hidden) closeDeleteModal();
    });
}
</script>
</body>
</html>
