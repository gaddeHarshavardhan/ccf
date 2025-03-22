package com.oneassist.ccf.enums;

public enum Category {
    PE("Personal Electronics"),
    HA("Home Appliances"),
    MOTOR("Motor"),
    FURNITURE("Furniture");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
