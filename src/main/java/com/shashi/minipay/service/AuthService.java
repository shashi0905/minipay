package com.shashi.minipay.service;

import com.shashi.minipay.dto.request.RegisterRequest;
import com.shashi.minipay.dto.response.AuthResponse;
import com.shashi.minipay.entity.User;
import com.shashi.minipay.entity.UserRole;
import com.shashi.minipay.exception.UserAlreadyExistsException;
import com.shashi.minipay.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * AuthService handles user registration and authentication-related business logic.
 *
 * Current step: Registration
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Register a new user. Steps:
     * 1. Check for existing username/email
     * 2. Hash password with BCrypt
     * 3. Persist user with role CUSTOMER
     * 4. Generate JWT token and return AuthResponse
     */
    public AuthResponse register(RegisterRequest request) {
        // Check duplicates
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new UserAlreadyExistsException("Username already taken: " + request.username());
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("Email already registered: " + request.email());
        }

        // Hash password
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashed = encoder.encode(request.password());

        // Create user entity
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(hashed);
        user.setRole(UserRole.CUSTOMER);

        Instant now = Instant.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User saved = userRepository.save(user);

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(saved);

        return new AuthResponse(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getRole(),
                token,
                jwtTokenProvider.getExpirationTime(),
                now
        );
    }

    /**
     * Authenticate user by username/email and password. Generate JWT token on success.
     *
     * Steps:
     * 1. Find user by username or email
     * 2. Verify password using BCrypt
     * 3. Generate JWT token using JwtTokenProvider
     * 4. Return AuthResponse
     */
    public AuthResponse login(String usernameOrEmail, String password) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new com.shashi.minipay.exception.InvalidCredentialsException("Invalid username/email or password"));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new com.shashi.minipay.exception.InvalidCredentialsException("Invalid username/email or password");
        }

        String token = jwtTokenProvider.generateToken(user);
        return new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                token,
                jwtTokenProvider.getExpirationTime(),
                Instant.now()
        );
    }
}
