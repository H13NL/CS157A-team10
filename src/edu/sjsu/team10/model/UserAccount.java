package edu.sjsu.team10.model;

import java.sql.Timestamp;

// One row on the admin "Manage users" page: a user plus their role details.
public class UserAccount {

    private final int userId;
    private final String name;
    private final String email;
    private final String role;           // admin, chef, restocker, consumer, or none
    private final String state;          // active, pending, disabled, or rejected
    private final Timestamp createdAt;
    private final String restaurantName; // chefs only
    private final String companyName;    // restockers only
    private final String approvedByName; // restockers only, null while pending
    private final Timestamp approvedAt;  // restockers only, null while pending

    public UserAccount(int userId, String name, String email, String role, String state,
                       Timestamp createdAt, String restaurantName, String companyName,
                       String approvedByName, Timestamp approvedAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.state = state;
        this.createdAt = createdAt;
        this.restaurantName = restaurantName;
        this.companyName = companyName;
        this.approvedByName = approvedByName;
        this.approvedAt = approvedAt;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getState() { return state; }
    public Timestamp getCreatedAt() { return createdAt; }
    public String getRestaurantName() { return restaurantName; }
    public String getCompanyName() { return companyName; }
    public String getApprovedByName() { return approvedByName; }
    public Timestamp getApprovedAt() { return approvedAt; }

    public boolean isPending() { return "pending".equals(state); }
    public boolean isDisabled() { return "disabled".equals(state); }
    public boolean isRejected() { return "rejected".equals(state); }
    public boolean isAdmin() { return "admin".equals(role); }

    // short line shown under the name, e.g. the chef's restaurant or restocker's company
    public String getDetail() {
        if ("chef".equals(role) && restaurantName != null) {
            return restaurantName;
        }
        if ("restocker".equals(role)) {
            return companyName == null || companyName.isEmpty() ? "No company listed" : companyName;
        }
        return "";
    }
}
