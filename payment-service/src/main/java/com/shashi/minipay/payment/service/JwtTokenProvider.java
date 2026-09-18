package com.shashi.minipay.service;

import com.shashi.minipay.entity.User;
import com.shashi.minipay.exception.JwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT Token Provider Service for token generation and validation.
 *
 * ============ JWT CONCEPTS ============
 *
 * JWT (JSON Web Token) is a stateless authentication mechanism.
 * Structure: eyJhbGc...header.eyJzdWI...payload.SflKxw...signature
 *
 * 1. HEADER:
 *    - Specifies algorithm (HS256, RS256, etc)
 *    - Type (JWT)
 *    - Base64Url encoded
 *
 * 2. PAYLOAD (Claims):
 *    - User info (sub = subject/userId, username, email)
 *    - Roles/Authorities
 *    - Expiration time (exp)
 *    - Issued at (iat)
 *    - Not before (nbf)
 *    - Custom claims
 *    - Base64Url encoded (NOT encrypted, just encoded!)
 *
 * 3. SIGNATURE:
 *    - HMACSHA256(base64(header) + "." + base64(payload), secret)
 *    - Ensures token hasn't been tampered with
 *    - Only server knows the secret key
 *
 * ============ WORKFLOW ============
 *
 * 1. LOGIN:
 *    Client sends username + password
 *    ↓
 *    Server verifies password
 *    ↓
 *    Server generates JWT token with user claims
 *    ↓
 *    Server returns token to client
 *
 * 2. SUBSEQUENT REQUESTS:
 *    Client sends: Authorization: Bearer <token>
 *    ↓
 *    Server extracts and validates token signature
 *    ↓
 *    Server extracts claims from payload
 *    ↓
 *    Server allows request if token valid and not expired
 *
 * ============ SECURITY NOTES ============
 *
 * - Secret key: Use strong random string (min 32 chars)
 * - Token expiration: Balance between security & UX
 * - Signature verification: Ensures token authenticity
 * - HTTPS: Always transmit tokens over HTTPS (payload is just Base64, not encrypted)
 * - Refresh tokens: For longer sessions, use refresh token pattern
 */
@Service
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generate JWT token from User entity.
     *
     * @param user User entity containing userId, username, email, role
     * @return JWT token string
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().toString());

        return createToken(claims, user.getId().toString());
    }

    /**
     * Create JWT token with custom claims.
     * This is the core token generation logic.
     *
     * @param claims Map of custom claims (username, email, role, etc)
     * @param subject Subject (typically userId)
     * @return Signed JWT token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Instant now = Instant.now();
        Date issuedAt = new Date(now.toEpochMilli());
        Date expiresAt = new Date(now.toEpochMilli() + jwtExpiration);

        return Jwts.builder()
                // Set claims (payload)
                .claims(claims)
                // Set subject (typically userId) - standard "sub" claim
                .subject(subject)
                // Set issued at time - standard "iat" claim
                .issuedAt(issuedAt)
                // Set expiration time - standard "exp" claim
                .expiration(expiresAt)
                // Sign with secret key using HMAC-SHA256
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                // Serialize to string
                .compact();
    }

    /**
     * Extract userId (subject) from JWT token.
     *
     * @param token JWT token
     * @return userId as String
     * @throws JwtException if token is invalid or expired
     */
    public String extractUserId(String token) {
        try {
            return extractAllClaims(token).getSubject();
        } catch (JwtException e) {
            throw new JwtException("Failed to extract userId from token: " + e.getMessage(), e);
        }
    }

    /**
     * Extract username from JWT token.
     *
     * @param token JWT token
     * @return username
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).get("username", String.class);
    }

    /**
     * Extract role from JWT token.
     *
     * @param token JWT token
     * @return role as String (CUSTOMER or ADMIN)
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Validate JWT token.
     * Checks:
     * - Signature is valid (not tampered with)
     * - Token is not expired
     * - Token structure is valid
     *
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            // Attempt to parse and verify the token
            // Use parser() for compatibility with older jjwt versions
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new JwtException("JWT token has expired: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            throw new JwtException("Unsupported JWT token: " + e.getMessage());
        } catch (MalformedJwtException e) {
            throw new JwtException("Invalid JWT token format: " + e.getMessage());
        } catch (SignatureException e) {
            throw new JwtException("JWT signature validation failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new JwtException("JWT token is empty or null: " + e.getMessage());
        }
    }

    /**
     * Extract all claims from JWT token.
     * This parses and verifies the token signature.
     *
     * @param token JWT token
     * @return Claims object (similar to Map<String, Object>)
     */
    private Claims extractAllClaims(String token) {
        try {
            // Use parser() for compatibility with older jjwt versions
            return Jwts.parser()
                    // Provide the secret key for signature verification
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    // Parse and verify the token
                    .parseSignedClaims(token)
                    // Extract claims from payload
                    .getPayload();
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            throw new JwtException("Failed to parse JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Get token expiration time in milliseconds.
     *
     * @return Expiration time
     */
    public long getExpirationTime() {
        return jwtExpiration;
    }
}
