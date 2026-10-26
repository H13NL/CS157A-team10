package edu.sjsu.team10.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.sjsu.team10.dao.RecipeDAO;
import edu.sjsu.team10.model.User;

@WebServlet("/recipe-copy")
public class RecipeCopyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final RecipeDAO recipeDAO = new RecipeDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) { response.sendRedirect(request.getContextPath() + "/login.jsp"); return; }
        try {
            int recipeId = Integer.parseInt(request.getParameter("recipeId"));
            int copyId = recipeDAO.copyPrivate(recipeId, user.getUserId());
            response.sendRedirect(request.getContextPath() + "/recipe-form?id=" + copyId);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid recipe.");
        } catch (SQLException e) {
            throw new ServletException("Could not copy recipe", e);
        }
    }
}
