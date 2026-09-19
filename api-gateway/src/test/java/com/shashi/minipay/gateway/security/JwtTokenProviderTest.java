package com.shashi.minipay.gateway.security;

import com.shashi.minipay.gateway.exception.GatewayJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "test-secret-key-for-jwt-token-generation-and-validation-must-be-at-least-32-characters";
    private static final long EXPIRATION = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", EXPIRATION);
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        // Given
        String token = generateValidToken();

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldThrowException_WhenTokenExpired() {
        // Given
        String token = generateExpiredToken();

        // When & Then
        assertThrows(GatewayJwtException.class, () -> jwtTokenProvider.validateToken(token));
    }

    @Test
    void validateToken_ShouldThrowException_WhenTokenInvalid() {
        // Given
        String invalidToken = "invalid.token.string";

        // When & Then
        assertThrows(GatewayJwtException.class, () -> jwtTokenProvider.validateToken(invalidToken));
    }

    @Test
    void validateToken_ShouldThrowException_WhenTokenEmpty() {
        // Given
        String emptyToken = "";

        // When & Then
        assertThrows(GatewayJwtException.class, () -> jwtTokenProvider.validateToken(emptyToken));
    }

    @Test
    void validateToken_ShouldThrowException_WhenTokenNull() {
        // When & Then
        assertThrows(GatewayJwtException.class, () -> jwtTokenProvider.validateToken(null));
    }

    @Test
    void extractUserId_ShouldReturnCorrectUserId_WhenTokenValid() {
        // Given
        String userId = "user-123";
        String token = generateTokenWithSubject(userId);

        // When
        String extractedUserId = jwtTokenProvider.extractUserId(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername_WhenTokenValid() {
        // Given
        String username = "testuser";
        String token = generateTokenWithUsername(username);

        // When
        String extractedUsername = jwtTokenProvider.extractUsername(token);

        // Then
        assertEquals(username, extractedUsername);
    }

    @Test
    void extractRole_ShouldReturnCorrectRole_WhenTokenValid() {
        // Given
        String role = "CUSTOMER";
        String token = generateTokenWithRole(role);

        // When
        String extractedRole = jwtTokenProvider.extractRole(token);

        // Then
        assertEquals(role, extractedRole);
    }

    private String generateValidToken() {
        return Jwts.builder()
                .subject("user-123")
                .claim("username", "testuser")
                .claim("role", "CUSTOMER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private String generateExpiredToken() {
        return Jwts.builder()
                .subject("user-123")
                .claim("username", "testuser")
                .claim("role", "CUSTOMER")
                .issuedAt(new Date(System.currentTimeMillis() - EXPIRATION * 2))
                .expiration(new Date(System.currentTimeMillis() - EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private String generateTokenWithSubject(String subject) {
        return Jwts.builder()
                .subject(subject)
                .claim("username", "testuser")
                .claim("role", "CUSTOMER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private String generateTokenWithUsername(String username) {
        return Jwts.builder()
                .subject("user-123")
                .claim("username", username)
                .claim("role", "CUSTOMER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private String generateTokenWithRole(String role) {
        return Jwts.builder()
                .subject("user-123")
                .claim("username", "testuser")
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
