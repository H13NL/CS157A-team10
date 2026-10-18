package edu.sjsu.team10.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import edu.sjsu.team10.model.User;

public class UserDAO {

    public User findUser(String email, String password) {
    	
    	// sql query for finding login credential in db
    	String sql = "SELECT user_id, name, email FROM users " + "WHERE email = ? AND password = ?";
    	
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
                                rs.getString("email")
                            );
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}