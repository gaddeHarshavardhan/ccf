package com.oneassist.ccf.enums;

public enum Option {

    SUBMIT("submit");

    private final String value;

    Option(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

