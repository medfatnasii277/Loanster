package com.pm.authservice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String validSecret = Base64.getEncoder().encodeToString("mySecretKeyThatIsLongEnoughForHS256Algorithm".getBytes());
    private final String testEmail = "test@example.com";
    private final String testRole = "BORROWER";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(validSecret);
    }

    @Test
    @DisplayName("Should generate valid JWT token with email and role")
    void generateToken_WithValidEmailAndRole_ShouldReturnToken() {
        // When
        String token = jwtUtil.generateToken(testEmail, testRole);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
    }

    @Test
    @DisplayName("Should validate valid token successfully")
    void validateToken_WithValidToken_ShouldNotThrowException() {
        // Given
        String token = jwtUtil.generateToken(testEmail, testRole);

        // When & Then
        jwtUtil.validateToken(token); // Should not throw exception
    }

    @Test
    @DisplayName("Should throw exception for invalid token")
    void validateToken_WithInvalidToken_ShouldThrowJwtException() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.validateToken(invalidToken))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Invalid JWT token");
    }

    @Test
    @DisplayName("Should throw exception for malformed token")
    void validateToken_WithMalformedToken_ShouldThrowJwtException() {
        // Given
        String malformedToken = "not.a.valid.jwt.structure.here";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.validateToken(malformedToken))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("Should extract role from valid token")
    void extractRole_WithValidToken_ShouldReturnRole() {
        // Given
        String token = jwtUtil.generateToken(testEmail, testRole);

        // When
        String extractedRole = jwtUtil.extractRole(token);

        // Then
        assertThat(extractedRole).isEqualTo(testRole);
    }

    @Test
    @DisplayName("Should throw exception when extracting role from invalid token")
    void extractRole_WithInvalidToken_ShouldThrowJwtException() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.extractRole(invalidToken))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("Invalid JWT token");
    }

    @Test
    @DisplayName("Should validate token and check role successfully for matching role")
    void validateTokenAndCheckRole_WithValidTokenAndMatchingRole_ShouldReturnTrue() {
        // Given
        String token = jwtUtil.generateToken(testEmail, testRole);

        // When
        boolean result = jwtUtil.validateTokenAndCheckRole(token, testRole);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when token has different role")
    void validateTokenAndCheckRole_WithValidTokenAndDifferentRole_ShouldReturnFalse() {
        // Given
        String token = jwtUtil.generateToken(testEmail, "BORROWER");
        String requiredRole = "OFFICER";

        // When
        boolean result = jwtUtil.validateTokenAndCheckRole(token, requiredRole);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when validating invalid token with role")
    void validateTokenAndCheckRole_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.jwt.token";
        String requiredRole = "BORROWER";

        // When
        boolean result = jwtUtil.validateTokenAndCheckRole(invalidToken, requiredRole);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should throw exception when initialized with invalid secret")
    void constructor_WithInvalidSecret_ShouldThrowException() {
        // Given
        String invalidSecret = "short";

        // When & Then
        assertThatThrownBy(() -> new JwtUtil(invalidSecret))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid JWT secret key");
    }

    @Test
    @DisplayName("Should generate token with correct expiration time")
    void generateToken_ShouldSetCorrectExpirationTime() {
        // Given
        String token = jwtUtil.generateToken(testEmail, testRole);
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(validSecret));

        // When
        Date expiration = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        // Then
        long expectedExpiration = System.currentTimeMillis() + (1000 * 60 * 60 * 10); // 10 hours
        long actualExpiration = expiration.getTime();
        long tolerance = 1000 * 60; // 1 minute tolerance

        assertThat(actualExpiration).isBetween(
                expectedExpiration - tolerance,
                expectedExpiration + tolerance
        );
    }

    @Test
    @DisplayName("Should generate token with correct subject")
    void generateToken_ShouldSetCorrectSubject() {
        // Given
        String token = jwtUtil.generateToken(testEmail, testRole);
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(validSecret));

        // When
        String subject = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        // Then
        assertThat(subject).isEqualTo(testEmail);
    }

    @Test
    @DisplayName("Should handle null email gracefully")
    void generateToken_WithNullEmail_ShouldGenerateToken() {
        // When & Then
        String token = jwtUtil.generateToken(null, testRole);
        assertThat(token).isNotNull();
    }

    @Test
    @DisplayName("Should handle null role gracefully")
    void generateToken_WithNullRole_ShouldGenerateToken() {
        // When & Then
        String token = jwtUtil.generateToken(testEmail, null);
        assertThat(token).isNotNull();
    }

    @Test
    @DisplayName("Should fail validation for expired token")
    void validateToken_WithExpiredToken_ShouldThrowJwtException() {
        // Given - Create an expired token manually
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(validSecret));
        String expiredToken = Jwts.builder()
                .subject(testEmail)
                .claim("role", testRole)
                .issuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)) // Yesterday
                .expiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60)) // 1 hour ago
                .signWith(key)
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtUtil.validateToken(expiredToken))
                .isInstanceOf(JwtException.class);
    }
}
