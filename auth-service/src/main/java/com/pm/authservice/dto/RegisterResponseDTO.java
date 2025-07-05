package com.pm.authservice.dto;

import java.util.UUID;

public class RegisterResponseDTO {

    private String message;
    private UUID userId;

    // Default constructor
    public RegisterResponseDTO() {}

    // Constructor with message and userId
    public RegisterResponseDTO(String message, UUID userId) {
        this.message = message;
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
} 