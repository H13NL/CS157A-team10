package edu.sjsu.team10.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.sjsu.team10.model.User;

public class UserDAO {

    public User findUser(String email, String password) {
    	
    	// sql query for finding login credential in db
    	String sql = "SELECT user_id, name, email, status FROM users " + "WHERE email = ? AND password = ?";
    	
    	// change to your database password (I set mine as a environment variable). 
        String dbPassword = System.getenv("WHATS_MENU_DB_PASSWORD");
        
        // change testproject into your database name
        String url = "jdbc:mysql://localhost:3306/testproject" + "?useSSL=false" + "&allowPublicKeyRetrieval=true" + "&serverTimezone=UTC";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con = DriverManager.getConnection(url,"root",dbPassword);

                PreparedStatement preparedStatement = con.prepareStatement(sql)) {
            	
            	// replace ? placeholder with actual email and password
                preparedStatement.setString(1, email);
                preparedStatement.setString(2, password);

                try (ResultSet rs = preparedStatement.executeQuery()) {
                	// if credential match 1 instance in users table then return their id, name and email
                    if (rs.next()) {
                        return new User(
                                rs.getInt("user_id"),
                                rs.getString("name"),
                                rs.getString("email"),
                                rs.getString("status")
                            );
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public boolean isConsumer(int userId) {

        String sql =
            "SELECT 1 "
          + "FROM consumer "
          + "WHERE user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement preparedStatement =
                 con.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isAdmin(int userId) {
        return existsIn("SELECT 1 FROM admin WHERE user_id = ?", userId);
    }

    public boolean isRestocker(int userId) {
        return existsIn("SELECT 1 FROM restocker WHERE user_id = ?", userId);
    }

    // a restocker can only log in after an admin has approved them (approved_by is set)
    public boolean isApprovedRestocker(int userId) {
        return existsIn("SELECT 1 FROM restocker WHERE user_id = ? AND approved_by IS NOT NULL", userId);
    }

    private boolean existsIn(String sql, int userId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
