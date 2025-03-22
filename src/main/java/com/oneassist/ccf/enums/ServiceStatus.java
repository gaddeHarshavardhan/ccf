package com.oneassist.ccf.enums;

public enum ServiceStatus {

    IN_PROGRESS ("In Progress"),
    COMPLETED("Completed");

    private final String status;

    ServiceStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
