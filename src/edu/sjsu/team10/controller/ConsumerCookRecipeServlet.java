package edu.sjsu.team10.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.sjsu.team10.dao.PantryDAO;
import edu.sjsu.team10.dao.UserDAO;
import edu.sjsu.team10.model.User;

@WebServlet("/consumer-cook-recipe")
public class ConsumerCookRecipeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final PantryDAO pantryDAO = new PantryDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp" );
            return;
        }

        User user = (User) session.getAttribute("user");
        
        if (!userDAO.isConsumer(user.getUserId())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only consumers can cook recipes.");
            return;
        }

        String recipeIdText = request.getParameter("recipeId");

        try {
            int recipeId = Integer.parseInt(recipeIdText);
            boolean cooked = pantryDAO.consumeRecipe(user.getUserId(), recipeId);
            if (cooked) {
                response.sendRedirect(
                    request.getContextPath()
                    + "/consumer-dashboard?cook=success"
                );
            } else {
                response.sendRedirect(
                    request.getContextPath()
                    + "/consumer-dashboard?cook=insufficient"
                );
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(
                request.getContextPath()
                + "/consumer-dashboard?cook=invalid"
            );

        } catch (Exception e) {
            throw new ServletException( "Could not cook recipe", e);
        }
    }
}