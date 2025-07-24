package com.pm.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.dto.RegisterRequestDTO;
import com.pm.authservice.model.User;
import com.pm.authservice.model.UserRole;
import com.pm.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Unit Tests")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private LoginRequestDTO loginRequest;
    private RegisterRequestDTO registerRequest;
    private LoginResponseDTO loginResponse;
    private User testUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

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
    @DisplayName("Should return unauthorized for invalid credentials")
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
    @DisplayName("Should register user successfully with valid data")
    void register_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(Optional.of(testUser));

        // When & Then
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    @DisplayName("Should return conflict for existing email")
    void register_WithExistingEmail_ShouldReturnConflict() throws Exception {
        // Given
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
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
    @DisplayName("Should return unauthorized for invalid token")
    void validateToken_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authService.validateToken(anyString())).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/validate")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return bad request when authorization header is missing")
    void validateToken_WithoutAuthHeader_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/validate"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate borrower token successfully")
    void validateBorrowerToken_WithValidBorrowerToken_ShouldReturnOk() throws Exception {
        // Given
        when(authService.validateTokenWithRole(anyString(), eq("BORROWER"))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/validate/borrower")
                        .header("Authorization", "Bearer valid.jwt.token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return unauthorized for borrower token with officer role")
    void validateBorrowerToken_WithOfficerToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authService.validateTokenWithRole(anyString(), eq("BORROWER"))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/validate/borrower")
                        .header("Authorization", "Bearer officer.jwt.token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should validate officer token successfully")
    void validateOfficerToken_WithValidOfficerToken_ShouldReturnOk() throws Exception {
        // Given
        when(authService.validateTokenWithRole(anyString(), eq("OFFICER"))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/validate/officer")
                        .header("Authorization", "Bearer valid.jwt.token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return unauthorized for officer token with borrower role")
    void validateOfficerToken_WithBorrowerToken_ShouldReturnUnauthorized() throws Exception {
        // Given
        when(authService.validateTokenWithRole(anyString(), eq("OFFICER"))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/validate/officer")
                        .header("Authorization", "Bearer borrower.jwt.token"))
                .andExpect(status().isUnauthorized());
    }
}
