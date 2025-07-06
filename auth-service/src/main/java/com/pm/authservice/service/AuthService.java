package com.pm.authservice.service;

import com.pm.authservice.dto.LoginRequestDTO;
import com.pm.authservice.dto.RegisterRequestDTO;
import com.pm.authservice.model.User;
import com.pm.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
        Optional<String> token = userService.findByEmail(loginRequestDTO.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(),
                        u.getPassword()))
                .map(u -> jwtUtil.generateToken(u.getEmail(), u.getRole().toString()));

        return token;
    }

    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean validateTokenWithRole(String token, String requiredRole) {
        return jwtUtil.validateTokenAndCheckRole(token, requiredRole);
    }

    public Optional<User> register(RegisterRequestDTO registerRequestDTO) {
        // Check if user already exists
        if (userService.existsByEmail(registerRequestDTO.getEmail())) {
            return Optional.empty();
        }

        // Create new user with encoded password
        User user = new User(
                registerRequestDTO.getEmail(),
                passwordEncoder.encode(registerRequestDTO.getPassword()),
                registerRequestDTO.getRole(),
                registerRequestDTO.getFullName(),
                registerRequestDTO.getDateOfBirth()
        );

        User savedUser = userService.createUser(user);
        return Optional.of(savedUser);
    }
}


