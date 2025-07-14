package com.pm.authservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.RegisterRequestDTO;
import com.pm.authservice.model.UserRole;
import com.pm.authservice.repository.UserRepsitory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Authentication Integration Tests")
class AuthIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepsitory userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should complete full authentication flow: register -> login -> validate")
    void completeAuthenticationFlow_ShouldWork() throws Exception {
        // Step 1: Register a new user
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "integration@test.com",
                "password123",
                UserRole.BORROWER,
                "Integration Test User",
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        // Step 2: Login with the registered user
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("integration@test.com");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("integration@test.com"))
                .andExpect(jsonPath("$.user.role").value("BORROWER"))
                .andReturn();

        // Extract token from response
        String response = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();

        // Step 3: Validate the token
        mockMvc.perform(get("/validate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Step 4: Validate borrower-specific endpoint
        mockMvc.perform(get("/validate/borrower")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Step 5: Try officer endpoint (should fail)
        mockMvc.perform(get("/validate/officer")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should prevent duplicate user registration")
    void register_WithDuplicateEmail_ShouldFailSecondAttempt() throws Exception {
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "duplicate@test.com",
                "password123",
                UserRole.BORROWER,
                "First User",
                LocalDate.of(1990, 1, 1)
        );

        // First registration should succeed
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Second registration with same email should fail
        RegisterRequestDTO duplicateRequest = new RegisterRequestDTO(
                "duplicate@test.com",
                "differentPassword",
                UserRole.OFFICER,
                "Second User",
                LocalDate.of(1985, 12, 25)
        );

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with this email already exists"));
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    void login_WithWrongPassword_ShouldFail() throws Exception {
        // Register user first
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "wrongpass@test.com",
                "correctPassword",
                UserRole.BORROWER,
                "Wrong Pass User",
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Try login with wrong password
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("wrongpass@test.com");
        loginRequest.setPassword("wrongPassword");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should fail login with non-existent user")
    void login_WithNonExistentUser_ShouldFail() throws Exception {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("nonexistent@test.com");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should test officer role authentication flow")
    void officerAuthenticationFlow_ShouldWork() throws Exception {
        // Register officer
        RegisterRequestDTO registerRequest = new RegisterRequestDTO(
                "officer@test.com",
                "password123",
                UserRole.OFFICER,
                "Test Officer",
                LocalDate.of(1985, 5, 15)
        );

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login as officer
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("officer@test.com");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("OFFICER"))
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(response).get("token").asText();

        // Validate officer endpoint
        mockMvc.perform(get("/validate/officer")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Borrower endpoint should fail
        mockMvc.perform(get("/validate/borrower")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle invalid token gracefully")
    void validateToken_WithInvalidToken_ShouldFail() throws Exception {
        mockMvc.perform(get("/validate")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/validate/borrower")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/validate/officer")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle missing authorization header")
    void validateToken_WithoutAuthorizationHeader_ShouldFail() throws Exception {
        mockMvc.perform(get("/validate"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/validate/borrower"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/validate/officer"))
                .andExpect(status().isUnauthorized());
    }
}
