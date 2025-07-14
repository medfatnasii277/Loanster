package com.pm.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.dto.RegisterRequestDTO;
import com.pm.authservice.model.User;
import com.pm.authservice.model.UserRole;
import com.pm.authservice.service.AuthService;
import com.pm.authservice.config.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(TestSecurityConfig.class)
@TestPropertySource(properties = {
    "jwt.secret=dGVzdFNlY3JldEtleUZvckpXVFRlc3RpbmdUaGF0SXNMb25nRW5vdWdoRm9ySFMyNTZBbGdvcml0aG0="
})
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequestDTO loginRequest;
    private RegisterRequestDTO registerRequest;
    private LoginResponseDTO loginResponse;
    private User testUser;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        registerRequest = new RegisterRequestDTO(
                "newuser@example.com",
                "password123",
                UserRole.BORROWER,
                "Jane Doe",
                LocalDate.of(1992, 5, 15)
        );

        testUser = new User(
                "test@example.com",
                "encodedPassword",
                UserRole.BORROWER,
                "John Doe",
                LocalDate.of(1990, 1, 1)
        );
        testUser.setId(UUID.randomUUID());

        LoginResponseDTO.UserInfo userInfo = new LoginResponseDTO.UserInfo(
                testUser.getId(),
                testUser.getEmail(),
                testUser.getFullName(),
                testUser.getRole().toString(),
                testUser.getDateOfBirth()
        );
        loginResponse = new LoginResponseDTO("jwt.token.here", userInfo);
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void login_WithValidCredentials_ShouldReturnOk() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(Optional.of(loginResponse));

        // When & Then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.here"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.role").value("BORROWER"));
    }

    @Test
    @DisplayName("Should return 401 when login fails")
    void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authService.authenticate(any(LoginRequestDTO.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should register new user successfully")
    void register_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value(testUser.getId().toString()));
    }

    @Test
    @DisplayName("Should return 409 when registering with existing email")
    void register_WithExistingEmail_ShouldReturnConflict() throws Exception {
        // Given
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User with this email already exists"));
    }

    @Test
    @DisplayName("Should validate token successfully")
    void validateToken_WithValidToken_ShouldReturnOk() throws Exception {
        // Given
        when(authService.validateToken(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/validate")
                        .header("Authorization", "Bearer valid.jwt.token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 401 for invalid token")
    void validateToken_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authService.validateToken(anyString())).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/validate")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when no authorization header is provided")
    void validateToken_WithoutAuthHeader_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/validate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should validate borrower token successfully")
    void validateBorrowerToken_WithValidBorrowerToken_ShouldReturnOk() throws Exception {
        // Given
        when(authService.validateTokenWithRole(anyString(), "BORROWER")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/validate/borrower")
                        .header("Authorization", "Bearer valid.borrower.token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 401 for borrower endpoint with officer token")
    void validateBorrowerToken_WithOfficerToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authService.validateTokenWithRole(anyString(), "BORROWER")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/validate/borrower")
                        .header("Authorization", "Bearer officer.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should validate officer token successfully")
    void validateOfficerToken_WithValidOfficerToken_ShouldReturnOk() throws Exception {
        // Given
        when(authService.validateTokenWithRole(anyString(), "OFFICER")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/validate/officer")
                        .header("Authorization", "Bearer valid.officer.token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return 401 for officer endpoint with borrower token")
    void validateOfficerToken_WithBorrowerToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authService.validateTokenWithRole(anyString(), "OFFICER")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/validate/officer")
                        .header("Authorization", "Bearer borrower.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 for invalid JSON in login request")
    void login_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for invalid JSON in register request")
    void register_WithInvalidJson_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle missing request body gracefully")
    void login_WithMissingBody_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle malformed authorization header")
    void validateToken_WithMalformedAuthHeader_ShouldReturnUnauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/validate")
                        .header("Authorization", "InvalidFormat"))
                .andExpect(status().isUnauthorized());
    }
}
