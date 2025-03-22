package com.oneassist.ccf.enums;

public enum ServiceEnum {
    ADLD("Accidental Damage"),
    EW("Extended Warranty"),
    AD("Accidental Damage"),
    PMS("Preventive Maintenance");

    private final String displayName;

    ServiceEnum(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}