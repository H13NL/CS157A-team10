package edu.sjsu.team10.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.sjsu.team10.dao.CatalogDAO;
import edu.sjsu.team10.model.User;
import edu.sjsu.team10.util.Forms;
import edu.sjsu.team10.util.RoleGuard;

// A restocker manages the ingredients they supply, with price and available quantity.
// The restocker id always comes from the session, so they can't edit another restocker's catalog.
@WebServlet("/catalog")
public class CatalogServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final CatalogDAO catalogDAO = new CatalogDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User restocker = RoleGuard.require(request, response, "restocker");
        if (restocker == null) {
            return;
        }

        try {
            int restockerId = restocker.getUserId();
            request.setAttribute("catalog", catalogDAO.findByRestocker(restockerId));
            request.setAttribute("availableIngredients", catalogDAO.findNotInCatalog(restockerId));
        } catch (SQLException e) {
            throw new ServletException("Could not load catalog", e);
        }

        request.getRequestDispatcher("/WEB-INF/views/catalog.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        User restocker = RoleGuard.require(request, response, "restocker");
        if (restocker == null) {
            return;
        }

        int restockerId = restocker.getUserId();
        String action = request.getParameter("action");
        BigDecimal price = Forms.parseAmount(request.getParameter("price"));
        BigDecimal qty = Forms.parseAmount(request.getParameter("qty"));
        Integer ingId = Forms.parseId(request.getParameter("ingId"));
        String result;

        try {
            if ("add".equals(action)) {
                String name = Forms.trim(request.getParameter("newName"));
                String unit = Forms.trim(request.getParameter("newUnit"));

                if (price == null || qty == null || name.length() > 100 || unit.length() > 20
                        || (name.isEmpty() && ingId == null)) {
                    result = "invalid";
                } else {
                    // a typed name wins over the dropdown
                    result = catalogDAO.add(restockerId, name.isEmpty() ? ingId : null, name, unit, price, qty)
                           ? "added" : "notfound";
                }

            } else if ("update".equals(action)) {
                if (price == null || qty == null || ingId == null) {
                    result = "invalid";
                } else {
                    result = catalogDAO.update(restockerId, ingId, price, qty) ? "updated" : "notfound";
                }

            } else if ("remove".equals(action)) {
                result = ingId != null && catalogDAO.remove(restockerId, ingId) ? "removed" : "notfound";

            } else {
                result = "invalid";
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            // primary key (restocker_id, ing_id) already exists
            result = "duplicate";
        } catch (SQLException e) {
            throw new ServletException("Could not update catalog", e);
        }

        // redirect after POST so refreshing doesn't resubmit the form
        response.sendRedirect(request.getContextPath() + "/catalog?msg=" + result);
    }
}
