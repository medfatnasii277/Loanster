package com.pm.borrowerservice.entity;

public enum LoanApplicationStatus {
    PENDING("Pending Review"),
    UNDER_REVIEW("Under Review"),
    DOCUMENTS_REQUIRED("Documents Required"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled"),
    FUNDED("Funded");

    private final String displayName;

    LoanApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 