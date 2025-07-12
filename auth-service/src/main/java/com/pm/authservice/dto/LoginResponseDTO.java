package com.pm.authservice.dto;

import java.time.LocalDate;
import java.util.UUID;

public class LoginResponseDTO {

    private final String token;
    private final UserInfo user;

    public LoginResponseDTO(String token, UserInfo user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public UserInfo getUser() {
        return user;
    }

    public static class UserInfo {
        private final UUID id;
        private final String email;
        private final String fullName;
        private final String role;
        private final LocalDate dateOfBirth;

        public UserInfo(UUID id, String email, String fullName, String role, LocalDate dateOfBirth) {
            this.id = id;
            this.email = email;
            this.fullName = fullName;
            this.role = role;
            this.dateOfBirth = dateOfBirth;
        }

        public UUID getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        public String getFullName() {
            return fullName;
        }

        public String getRole() {
            return role;
        }

        public LocalDate getDateOfBirth() {
            return dateOfBirth;
        }
    }
}
