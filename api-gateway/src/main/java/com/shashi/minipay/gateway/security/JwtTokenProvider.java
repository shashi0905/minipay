package com.shashi.minipay.gateway.security;

import com.shashi.minipay.gateway.exception.GatewayJwtException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * JWT Token Provider for Gateway - validates tokens from Payment Service.
 * Reusable service extracted from Payment Service.
 *
 * GATEWAY ROLE:
 * 1. Extract token from "Authorization: Bearer <token>" header
 * 2. Validate signature (ensures token is authentic)
 * 3. Check expiration (token not expired)
 * 4. Extract claims (userId, username, role)
 * 5. Add to request headers for downstream services
 */
@Service
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Validate JWT token.
     *
     * @param token JWT token string
     * @return true if valid, false otherwise
     * @throws GatewayJwtException if validation fails
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new GatewayJwtException("JWT token has expired");
        } catch (UnsupportedJwtException e) {
            throw new GatewayJwtException("Unsupported JWT token");
        } catch (MalformedJwtException e) {
            throw new GatewayJwtException("Invalid JWT token format");
        } catch (SignatureException e) {
            throw new GatewayJwtException("JWT signature validation failed");
        } catch (IllegalArgumentException e) {
            throw new GatewayJwtException("JWT token is empty or null");
        }
    }

    /**
     * Extract userId (subject) from JWT token.
     *
     * @param token JWT token
     * @return userId
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
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
     * @return role (CUSTOMER or ADMIN)
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Extract all claims from token.
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new GatewayJwtException("Failed to parse JWT token: " + e.getMessage());
        }
    }
}
