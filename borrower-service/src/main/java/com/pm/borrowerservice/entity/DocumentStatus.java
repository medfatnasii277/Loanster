package com.pm.borrowerservice.entity;

public enum DocumentStatus {
    PENDING("Pending Review"),
    VERIFIED("Verified"),
    REJECTED("Rejected"),
    EXPIRED("Expired");

    private final String displayName;

    DocumentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 