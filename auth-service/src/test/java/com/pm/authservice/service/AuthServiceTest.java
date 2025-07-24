package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.LoginResponseDTO;
import com.pm.authservice.dto.RegisterRequestDTO;
import com.pm.authservice.model.User;
import com.pm.authservice.model.UserRole;
import com.pm.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginRequestDTO loginRequest;
    private RegisterRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        testUser = new User(
                "test@example.com",
                "encodedPassword",
                UserRole.BORROWER,
                "John Doe",
                LocalDate.of(1990, 1, 1)
        );
        testUser.setId(UUID.randomUUID());

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
    }

    @Test
    @DisplayName("Should authenticate user successfully with valid credentials")
    void authenticate_WithValidCredentials_ShouldReturnLoginResponse() {
        // Given
        String expectedToken = "jwt.token.here";
        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken(testUser.getEmail(), testUser.getRole().toString())).thenReturn(expectedToken);

        // When
        Optional<LoginResponseDTO> result = authService.authenticate(loginRequest);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(expectedToken);
        assertThat(result.get().getUser().getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.get().getUser().getRole()).isEqualTo(testUser.getRole().toString());
        
        verify(userService).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtUtil).generateToken(testUser.getEmail(), testUser.getRole().toString());
    }

    @Test
    @DisplayName("Should fail authentication when user does not exist")
    void authenticate_WithNonExistentUser_ShouldReturnEmpty() {
        // Given
        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        // When
        Optional<LoginResponseDTO> result = authService.authenticate(loginRequest);

        // Then
        assertThat(result).isEmpty();
        verify(userService).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should fail authentication with incorrect password")
    void authenticate_WithIncorrectPassword_ShouldReturnEmpty() {
        // Given
        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(loginRequest.getPassword(), testUser.getPassword())).thenReturn(false);

        // When
        Optional<LoginResponseDTO> result = authService.authenticate(loginRequest);

        // Then
        assertThat(result).isEmpty();
        verify(userService).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches(loginRequest.getPassword(), testUser.getPassword());
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    @DisplayName("Should validate token successfully when token is valid")
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String validToken = "valid.jwt.token";
        doNothing().when(jwtUtil).validateToken(validToken);

        // When
        boolean result = authService.validateToken(validToken);

        // Then
        assertThat(result).isTrue();
        verify(jwtUtil).validateToken(validToken);
    }

    @Test
    @DisplayName("Should fail token validation when token is invalid")
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.jwt.token";
        doThrow(new JwtException("Invalid token")).when(jwtUtil).validateToken(invalidToken);

        // When
        boolean result = authService.validateToken(invalidToken);

        // Then
        assertThat(result).isFalse();
        verify(jwtUtil).validateToken(invalidToken);
    }

    @Test
    @DisplayName("Should validate token with role successfully when token has required role")
    void validateTokenWithRole_WithValidTokenAndRole_ShouldReturnTrue() {
        // Given
        String validToken = "valid.jwt.token";
        String requiredRole = "BORROWER";
        when(jwtUtil.validateTokenAndCheckRole(validToken, requiredRole)).thenReturn(true);

        // When
        boolean result = authService.validateTokenWithRole(validToken, requiredRole);

        // Then
        assertThat(result).isTrue();
        verify(jwtUtil).validateTokenAndCheckRole(validToken, requiredRole);
    }

    @Test
    @DisplayName("Should fail token validation with role when token has different role")
    void validateTokenWithRole_WithDifferentRole_ShouldReturnFalse() {
        // Given
        String validToken = "valid.jwt.token";
        String requiredRole = "OFFICER";
        when(jwtUtil.validateTokenAndCheckRole(validToken, requiredRole)).thenReturn(false);

        // When
        boolean result = authService.validateTokenWithRole(validToken, requiredRole);

        // Then
        assertThat(result).isFalse();
        verify(jwtUtil).validateTokenAndCheckRole(validToken, requiredRole);
    }

    @Test
    @DisplayName("Should register new user successfully when email does not exist")
    void register_WithNewEmail_ShouldReturnUser() {
        // Given
        User savedUser = new User(
                registerRequest.getEmail(),
                "encodedPassword",
                registerRequest.getRole(),
                registerRequest.getFullName(),
                registerRequest.getDateOfBirth()
        );
        savedUser.setId(UUID.randomUUID());

        when(userService.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        // When
        Optional<User> result = authService.register(registerRequest);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(registerRequest.getEmail());
        assertThat(result.get().getRole()).isEqualTo(registerRequest.getRole());
        assertThat(result.get().getFullName()).isEqualTo(registerRequest.getFullName());
        
        verify(userService).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder).encode(registerRequest.getPassword());
        verify(userService).createUser(any(User.class));
    }

    @Test
    @DisplayName("Should fail registration when email already exists")
    void register_WithExistingEmail_ShouldReturnEmpty() {
        // Given
        when(userService.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When
        Optional<User> result = authService.register(registerRequest);

        // Then
        assertThat(result).isEmpty();
        verify(userService).existsByEmail(registerRequest.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).createUser(any(User.class));
    }

    @Test
    @DisplayName("Should handle null email in authentication gracefully")
    void authenticate_WithNullEmail_ShouldReturnEmpty() {
        // Given
        loginRequest.setEmail(null);
        when(userService.findByEmail(null)).thenReturn(Optional.empty());

        // When
        Optional<LoginResponseDTO> result = authService.authenticate(loginRequest);

        // Then
        assertThat(result).isEmpty();
        verify(userService).findByEmail(null);
    }

    @Test
    @DisplayName("Should handle empty password in authentication gracefully")
    void authenticate_WithEmptyPassword_ShouldReturnEmpty() {
        // Given
        loginRequest.setPassword("");
        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("", testUser.getPassword())).thenReturn(false);

        // When
        Optional<LoginResponseDTO> result = authService.authenticate(loginRequest);

        // Then
        assertThat(result).isEmpty();
        verify(userService).findByEmail(loginRequest.getEmail());
        verify(passwordEncoder).matches("", testUser.getPassword());
    }
}
