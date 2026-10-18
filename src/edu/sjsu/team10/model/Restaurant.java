package edu.sjsu.team10.model;

public class Restaurant {

    private int restaurantId;
    private String restaurantName;

    public Restaurant(int restaurantId, String restaurantName) {
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }
}
