package com.shashi.minipay.dto.response;

import com.shashi.minipay.entity.UserRole;

import java.time.Instant;
import java.util.UUID;

/**
 * AuthResponse DTO for authentication responses.
 *
 * KEY CONCEPTS:
 * - Used for both registration and login responses
 * - Contains user info + JWT token
 * - JWT token is used in Authorization header: "Bearer <token>"
 *
 * JWT STRUCTURE:
 * - eyJhbGc...       (header: algorithm, type)
 * - eyJzdWI...       (payload: claims like sub, exp, iat, roles)
 * - SflKxw...        (signature: ensures token integrity)
 *
 * TOKEN LIFECYCLE:
 * - Issued: server generates with expiration time
 * - Transmitted: client sends in Authorization header
 * - Validated: gateway validates signature and expiration
 * - Expired: client must login again to get new token
 */
public record AuthResponse(
        UUID userId,
        String username,
        String email,
        UserRole role,
        String token,
        long expiresIn,
        Instant createdAt
) {
}
