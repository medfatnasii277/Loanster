package com.pm.authservice.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final Key secretKey;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        try {
            byte[] keyBytes = Base64.getDecoder()
                    .decode(secret.getBytes(StandardCharsets.UTF_8));
            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid JWT secret key. It must be a valid Base64-encoded string.");
        } catch (Exception e) {
            throw new RuntimeException("Error initializing JWT secret key: " + e.getMessage(), e);
        }
    }

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *10)) // 10 hours
                .signWith(secretKey)
                .compact();
    }

    public void validateToken(String token) {
            try {
                Jwts.parser().verifyWith((SecretKey)  secretKey)
                        .build()
                        .parseSignedClaims(token);
            }catch(SignatureException e) {
                throw new JwtException("Invalid JWT signature.");
            }catch (JwtException e) {
                throw new JwtException("Invalid JWT token.");
            }
    }

    public String extractRole(String token) {
        try {
            return Jwts.parser().verifyWith((SecretKey) secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT token.");
        }
    }

    public boolean validateTokenAndCheckRole(String token, String requiredRole) {
        try {
            String role = extractRole(token);
            return requiredRole.equals(role);
        } catch (JwtException e) {
            return false;
        }
    }
}
