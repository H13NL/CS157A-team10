package edu.sjsu.team10.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import edu.sjsu.team10.dao.PantryDAO;
import edu.sjsu.team10.model.PantryItem;
import edu.sjsu.team10.model.User;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/consumer-dashboard")
public class ConsumerDashboardServlet extends HttpServlet {
	private final PantryDAO pantryDAO = new PantryDAO();
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(
                request.getContextPath() + "/login.jsp"
            );
            return;
        }

        User user = (User) session.getAttribute("user");

        try {
            List<PantryItem> pantryItems =
                pantryDAO.findByConsumer(user.getUserId());

            request.setAttribute("pantryItems", pantryItems);
            request.setAttribute("pantryCount", pantryItems.size());

        } catch (SQLException e) {
            throw new ServletException(
                "Could not load consumer dashboard",
                e
            );
        }

        request.getRequestDispatcher(
            "/WEB-INF/views/consumer-dashboard.jsp"
        ).forward(request, response);
    }
}