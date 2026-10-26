package edu.sjsu.team10.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.sjsu.team10.model.Restaurant;

public class RestaurantDAO {

    // Work at relationship: returns the one restaurant a chef belongs to,
    // or null if the user is not a chef
    public Restaurant findByChef(int userId) {

        String sql = "SELECT r.restaurant_id, r.restaurant_name "
                   + "FROM chef c JOIN restaurant r ON c.restaurant_id = r.restaurant_id "
                   + "WHERE c.user_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement preparedStatement = con.prepareStatement(sql)) {

            preparedStatement.setInt(1, userId);

            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return new Restaurant(
                            rs.getInt("restaurant_id"),
                            rs.getString("restaurant_name")
                        );
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
