package edu.sjsu.team10.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.sjsu.team10.dao.RestockDAO;
import edu.sjsu.team10.model.User;
import edu.sjsu.team10.util.RoleGuard;

// A restocker sees the restock requests chefs have sent them (only their own).
@WebServlet("/restocker/requests")
public class RestockerRequestsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final RestockDAO restockDAO = new RestockDAO();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        User restocker = RoleGuard.require(request, response, "restocker");
        if (restocker == null) {
            return;
        }

        try {
            request.setAttribute("requests", restockDAO.findByRestocker(restocker.getUserId()));
        } catch (SQLException e) {
            throw new ServletException("Could not load restock requests", e);
        }

        request.getRequestDispatcher("/WEB-INF/views/restocker-requests.jsp").forward(request, response);
    }
}
