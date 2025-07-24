package com.pm.authservice.service;

import com.pm.authservice.model.User;
import com.pm.authservice.model.UserRole;
import com.pm.authservice.repository.UserRepsitory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepsitory userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

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
    }

    @Test
    @DisplayName("Should find user by email when user exists")
    void findByEmail_WithExistingEmail_ShouldReturnUser() {
        // Given
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(email);
        assertThat(result.get().getFullName()).isEqualTo("John Doe");
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should return empty when user does not exist")
    void findByEmail_WithNonExistentEmail_ShouldReturnEmpty() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(email);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should create user successfully")
    void createUser_WithValidUser_ShouldReturnSavedUser() {
        // Given
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.createUser(testUser);

        // Then
        assertThat(result).isEqualTo(testUser);
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getRole()).isEqualTo(testUser.getRole());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should return true when user exists by email")
    void existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        // Given
        String email = "existing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        // When
        boolean result = userService.existsByEmail(email);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should return false when user does not exist by email")
    void existsByEmail_WithNonExistentEmail_ShouldReturnFalse() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        boolean result = userService.existsByEmail(email);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("Should handle null email in findByEmail")
    void findByEmail_WithNullEmail_ShouldReturnEmpty() {
        // Given
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(null);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(null);
    }

    @Test
    @DisplayName("Should handle null email in existsByEmail")
    void existsByEmail_WithNullEmail_ShouldReturnFalse() {
        // Given
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // When
        boolean result = userService.existsByEmail(null);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).findByEmail(null);
    }

    @Test
    @DisplayName("Should handle empty email string")
    void findByEmail_WithEmptyEmail_ShouldCallRepository() {
        // Given
        String emptyEmail = "";
        when(userRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(emptyEmail);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(emptyEmail);
    }

    @Test
    @DisplayName("Should propagate repository exceptions during user creation")
    void createUser_WhenRepositoryThrowsException_ShouldPropagateException() {
        // Given
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.createUser(testUser));
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should handle mixed case email correctly")
    void findByEmail_WithMixedCaseEmail_ShouldUseRepositoryAsIs() {
        // Given
        String mixedCaseEmail = "Test@Example.COM";
        when(userRepository.findByEmail(mixedCaseEmail)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail(mixedCaseEmail);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(mixedCaseEmail);
    }
}
