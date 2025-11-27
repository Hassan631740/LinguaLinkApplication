package com.lingualink.entity;

public enum Role {
    CLIENT("CLIENT"),
    INTERPRETER("INTERPRETER"),
    ADMINISTRATOR("ADMINISTRATOR");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Role fromString(String role) {
        if (role == null) {
            return CLIENT; // Default role
        }
        try {
            return Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle legacy role names or return default
            switch (role.toUpperCase()) {
                case "USER":
                case "ORGANIZER":
                    return CLIENT;
                case "ADMIN":
                    return ADMINISTRATOR;
                default:
                    return CLIENT;
            }
        }
    }
}

