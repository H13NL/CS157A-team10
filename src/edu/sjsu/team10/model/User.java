package edu.sjsu.team10.model;

public class User {

    private String name;
    private String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
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