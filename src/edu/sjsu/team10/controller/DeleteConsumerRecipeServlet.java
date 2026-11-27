package edu.sjsu.team10.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.sjsu.team10.dao.ConsumerRecipeDAO;
import edu.sjsu.team10.dao.UserDAO;
import edu.sjsu.team10.model.User;

@WebServlet("/delete-consumer-recipe")
public class DeleteConsumerRecipeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ConsumerRecipeDAO recipeDAO =
        new ConsumerRecipeDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        if (!userDAO.isConsumer(user.getUserId())) {
            response.sendError(
                HttpServletResponse.SC_FORBIDDEN,
                "Only consumers can delete their recipes."
            );
            return;
        }

        try {
            int recipeId = Integer.parseInt(request.getParameter("recipeId"));
            boolean deleted = recipeDAO.deletePrivateRecipe(user.getUserId(), recipeId);
            response.sendRedirect( request.getContextPath() + "/consumer-recipes?deleted="+ (deleted ? "1" : "0"));

        } catch (NumberFormatException e) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "Invalid recipe ID."
            );
        } catch (Exception e) {
            throw new ServletException(
                "Could not delete recipe", e
            );
        }
    }
}
