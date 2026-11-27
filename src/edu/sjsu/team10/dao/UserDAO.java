package edu.sjsu.team10.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.sjsu.team10.model.User;
import java.sql.PreparedStatement;
import java.sql.Statement;
public class UserDAO {

    public User findUser(String email, String password) {
    	
    	// sql query for finding login credential in db
    	String sql = "SELECT user_id, name, email, status FROM users " + "WHERE email = ? AND password = ?";


        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            try (Connection con = DBConnection.getConnection();
                    PreparedStatement preparedStatement =
                        con.prepareStatement(sql)) {
            	
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
    
    public static boolean isConsumer(int userId) {

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
    
    // sign up for consumer (doesn't require approval)
    public void createConsumer(String name,
            String email,
            String password)
		throws SQLException {
		
		String userSql =
		"INSERT INTO users (name, email, password, status) "
		+ "VALUES (?, ?, ?, 'active')";
		
		String consumerSql =
		"INSERT INTO consumer (user_id) VALUES (?)";
		
		try (Connection con = DBConnection.getConnection()) {
		
		con.setAutoCommit(false);
			try {
				int userId;
				
				try (PreparedStatement userStatement = con.prepareStatement( userSql, Statement.RETURN_GENERATED_KEYS)) {
				
				 userStatement.setString(1, name);
				 userStatement.setString(2, email);
				 userStatement.setString(3, password);
				
				 userStatement.executeUpdate();
				
					 try (ResultSet keys = userStatement.getGeneratedKeys()) {
					     if (!keys.next()) {
					         throw new SQLException(
					             "Could not retrieve generated user ID."
					         );
					     }
					     userId = keys.getInt(1);
					 }
				}
		
				try (PreparedStatement consumerStatement =
				     con.prepareStatement(consumerSql)) {
				
					consumerStatement.setInt(1, userId);
					consumerStatement.executeUpdate();
				}
				
				con.commit();
		
			} catch (SQLException e) {
				con.rollback();
				throw e;
			}
		}
	}
    public boolean changePassword(int userId,
            String currentPassword,
            String newPassword)
	throws SQLException {
	
		String sql =
		"UPDATE users "
		+ "SET password = ? "
		+ "WHERE user_id = ? AND password = ?";
		
		try (Connection con = DBConnection.getConnection();
		PreparedStatement ps = con.prepareStatement(sql)) {
		
			ps.setString(1, newPassword);
			ps.setInt(2, userId);
			ps.setString(3, currentPassword);
			
			return ps.executeUpdate() == 1;
		}
	}
}
