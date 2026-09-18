package com.shashi.minipay.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * LoginRequest DTO for user authentication.
 *
 * AUTHENTICATION FLOW:
 * 1. Client sends username (or email) + password
 * 2. Server finds user by username/email
 * 3. Server verifies password using BCrypt
 * 4. If valid, server generates JWT token
 * 5. Client uses JWT in Authorization header for future requests
 */
public record LoginRequest(
        @NotBlank(message = "Username or email is required")
        String usernameOrEmail,

        @NotBlank(message = "Password is required")
        String password
) {
}
