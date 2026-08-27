package com.shashi.minipay.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * RegisterRequest DTO for user registration.
 *
 * KEY CONCEPTS:
 * - DTO (Data Transfer Object): Separates API contract from entity model
 * - Record: Java 14+ feature for immutable data classes with auto-generated equals/hashCode/toString
 * - Bean Validation annotations: Validate input before reaching business logic
 *
 * VALIDATION RULES:
 * - @NotBlank: Field must not be null or empty (whitespace ignored)
 * - @Email: Must be valid email format
 * - @Size: String length constraints
 * - Violations trigger 400 Bad Request response
 *
 * WHY USE RECORDS?
 * - Immutable: Once created, cannot be changed
 * - Boilerplate-free: No getters/setters needed
 * - Thread-safe: Good for request handling
 * - Clear intent: Signals "this is just data, not an entity"
 */
public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password
) {
}
