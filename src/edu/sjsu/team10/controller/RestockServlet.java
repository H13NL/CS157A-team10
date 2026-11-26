package edu.sjsu.team10.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.sjsu.team10.dao.CatalogDAO;
import edu.sjsu.team10.dao.RecipeDAO;
import edu.sjsu.team10.dao.RestockDAO;
import edu.sjsu.team10.model.CatalogItem;
import edu.sjsu.team10.model.Recipe;
import edu.sjsu.team10.model.RecipeIngredient;
import edu.sjsu.team10.model.Restaurant;
import edu.sjsu.team10.model.RestockerOption;
import edu.sjsu.team10.model.ShortfallLine;
import edu.sjsu.team10.model.User;
import edu.sjsu.team10.util.Forms;
import edu.sjsu.team10.util.RoleGuard;

// A chef submits restock requests to an approved restocker and sees their request history.
//
// GET /restock?restocker=ID            shows that restocker's catalog with a quantity box per item
// GET /restock?restocker=ID&recipe=ID  also fills in the recipe's shortfall against the restaurant's stock
// POST /restock                        saves the request (fields qty_<ing_id>, restockerId, optional recipeId)
@WebServlet("/restock")
public class RestockServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final RestockDAO restockDAO = new RestockDAO();
    private final CatalogDAO catalogDAO = new CatalogDAO();
    private final RecipeDAO recipeDAO = new RecipeDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User chef = RoleGuard.require(request, response, "chef");
        if (chef == null) {
            return;
        }
        Restaurant restaurant = (Restaurant) request.getSession().getAttribute("restaurant");

        try {
            List<RestockerOption> restockers = restockDAO.findApprovedRestockers();
            List<Recipe> recipes = recipeDAO.findVisible(chef.getUserId(), restaurant.getRestaurantId());

            RestockerOption restocker = findRestocker(restockers, Forms.parseId(request.getParameter("restocker")));
            Recipe recipe = findRecipe(recipes, Forms.parseId(request.getParameter("recipe")));

            List<CatalogItem> catalog = new ArrayList<>();
            Map<Integer, String> prefill = new HashMap<>();
            List<ShortfallLine> shortfall = new ArrayList<>();

            if (restocker != null) {
                catalog = catalogDAO.findByRestocker(restocker.getUserId());
            }
            if (recipe != null) {
                shortfall = computeShortfall(recipe);
                matchToCatalog(shortfall, catalog, prefill);
            }

            request.setAttribute("restockers", restockers);
            request.setAttribute("recipes", recipes);
            request.setAttribute("selectedRestocker", restocker);
            request.setAttribute("selectedRecipe", recipe);
            request.setAttribute("catalog", catalog);
            request.setAttribute("prefill", prefill);
            request.setAttribute("shortfall", shortfall);
            request.setAttribute("myRequests", restockDAO.findByChef(chef.getUserId()));
        } catch (SQLException e) {
            throw new ServletException("Could not load restock page", e);
        }

        request.getRequestDispatcher("/WEB-INF/views/restock.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        User chef = RoleGuard.require(request, response, "chef");
        if (chef == null) {
            return;
        }
        Restaurant restaurant = (Restaurant) request.getSession().getAttribute("restaurant");

        Integer restockerId = Forms.parseId(request.getParameter("restockerId"));
        Integer recipeId = Forms.parseId(request.getParameter("recipeId"));
        String result;

        // collect qty_<ing_id> fields; blank or 0 means "don't order this"
        Map<Integer, BigDecimal> items = new LinkedHashMap<>();
        boolean invalid = false;
        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (!name.startsWith("qty_")) {
                continue;
            }
            String raw = Forms.trim(request.getParameter(name));
            if (raw.isEmpty()) {
                continue;
            }
            Integer ingId = Forms.parseId(name.substring(4));
            BigDecimal qty = Forms.parseAmount(raw);
            if (ingId == null || qty == null) {
                invalid = true;
            } else if (qty.signum() > 0) {
                items.put(ingId, qty);
            }
        }

        try {
            if (restockerId == null || invalid) {
                result = "invalid";
            } else if (items.isEmpty()) {
                result = "empty";
            } else {
                // only link recipes this chef is allowed to see
                if (recipeId != null) {
                    Recipe recipe = recipeDAO.findById(recipeId, restaurant.getRestaurantId());
                    if (recipe == null || !(recipe.isPublic() || recipe.isOwnedBy(chef.getUserId()))) {
                        recipeId = null;
                    }
                }
                int requestId = restockDAO.submit(chef.getUserId(), restockerId, recipeId, items);
                response.sendRedirect(request.getContextPath() + "/restock?msg=submitted&id=" + requestId + "#history");
                return;
            }
        } catch (RestockDAO.RequestRejected e) {
            result = e.getReason();
        } catch (SQLException e) {
            throw new ServletException("Could not submit restock request", e);
        }

        // send the chef back to the same restocker (and recipe) so they can fix it
        String back = "/restock?msg=" + result;
        if (restockerId != null) {
            back += "&restocker=" + restockerId;
        }
        if (recipeId != null) {
            back += "&recipe=" + recipeId;
        }
        response.sendRedirect(request.getContextPath() + back);
    }

    // how much more of each ingredient the recipe needs than the restaurant has.
    // Stock in a different unit can't be compared, so the whole amount counts as short
    // (the same rule the recipe feasibility check uses).
    private List<ShortfallLine> computeShortfall(Recipe recipe) {
        List<ShortfallLine> lines = new ArrayList<>();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            if (ing.isSatisfied()) {
                continue;
            }
            boolean sameUnit = ing.getUnit().equalsIgnoreCase(ing.getAvailableUnit());
            BigDecimal onHand = sameUnit ? ing.getAvailableQuantity() : BigDecimal.ZERO;
            BigDecimal shortBy = ing.getQuantity().subtract(onHand);
            lines.add(new ShortfallLine(ing.getIngredientId(), ing.getName(), ing.getUnit(),
                                        ing.getQuantity(), onHand, shortBy));
        }
        return lines;
    }

    // fill in quantities for the shortfall items this restocker sells in the same unit
    private void matchToCatalog(List<ShortfallLine> shortfall, List<CatalogItem> catalog,
                                Map<Integer, String> prefill) {
        if (catalog.isEmpty()) {
            return;
        }
        for (ShortfallLine line : shortfall) {
            CatalogItem item = null;
            for (CatalogItem c : catalog) {
                if (c.getIngId() == line.getIngId()) {
                    item = c;
                }
            }
            if (item == null) {
                line.setOutcome(ShortfallLine.NOT_CARRIED);
            } else if (!item.getUnit().equalsIgnoreCase(line.getUnit())) {
                line.setOutcome(ShortfallLine.UNIT_MISMATCH);
            } else {
                line.setOutcome(ShortfallLine.PREFILLED);
                prefill.put(line.getIngId(), line.getShortByDisplay());
            }
        }
    }

    private RestockerOption findRestocker(List<RestockerOption> restockers, Integer id) {
        if (id != null) {
            for (RestockerOption r : restockers) {
                if (r.getUserId() == id) {
                    return r;
                }
            }
        }
        return null;
    }

    private Recipe findRecipe(List<Recipe> recipes, Integer id) {
        if (id != null) {
            for (Recipe r : recipes) {
                if (r.getRecipeId() == id) {
                    return r;
                }
            }
        }
        return null;
    }
}
