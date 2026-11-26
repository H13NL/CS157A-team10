package edu.sjsu.team10.model;

public class User {

    private int userId;
    private String name;
    private String email;
    private String status = "active"; // users.status: active, disabled, or rejected

    public User(int userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public User(int userId, String name, String email, String status) {
        this(userId, name, email);
        this.status = status;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }
    
    // Initial is used for avatar display
    public String getInitials() {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }

        String[] parts = name.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return (
            parts[0].substring(0, 1)
            + parts[parts.length - 1].substring(0, 1)
        ).toUpperCase();
    }
}