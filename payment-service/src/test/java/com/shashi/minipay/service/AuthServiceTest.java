package com.shashi.minipay.service;

import com.shashi.minipay.dto.request.RegisterRequest;
import com.shashi.minipay.dto.response.AuthResponse;
import com.shashi.minipay.entity.User;
import com.shashi.minipay.entity.UserRole;
import com.shashi.minipay.exception.UserAlreadyExistsException;
import com.shashi.minipay.exception.InvalidCredentialsException;
import com.shashi.minipay.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "testuser",
                "test@example.com",
                "password123"
        );

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPasswordHash(encoder.encode("password123"));
        user.setRole(UserRole.CUSTOMER);
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
    }

    @Test
    void register_ShouldCreateUser_WhenUsernameAndEmailAreUnique() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        // When
        AuthResponse response = authService.register(registerRequest);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        assertEquals(UserRole.CUSTOMER, response.role());
        assertEquals("jwt-token", response.token());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtTokenProvider, times(1)).generateToken(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerRequest));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void register_ShouldHashPassword_WhenCreatingUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertNotNull(savedUser.getPasswordHash());
            assertNotEquals("password123", savedUser.getPasswordHash());
            return savedUser;
        });
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        // When
        authService.register(registerRequest);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldAssignCustomerRole_WhenCreatingUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(UserRole.CUSTOMER, savedUser.getRole());
            return savedUser;
        });
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        // When
        authService.register(registerRequest);

        // Then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void login_ShouldReturnToken_WhenValidUsernameAndPassword() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        // When
        AuthResponse response = authService.login("testuser", "password123");

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.username());
        assertEquals("test@example.com", response.email());
        assertEquals("jwt-token", response.token());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtTokenProvider, times(1)).generateToken(any(User.class));
    }

    @Test
    void login_ShouldReturnToken_WhenValidEmailAndPassword() {
        // Given
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(any(User.class))).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        // When
        AuthResponse response = authService.login("test@example.com", "password123");

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.username());
        assertEquals("jwt-token", response.token());
        verify(userRepository, times(1)).findByUsername("test@example.com");
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(jwtTokenProvider, times(1)).generateToken(any(User.class));
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> authService.login("nonexistent", "password123"));
        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(userRepository, times(1)).findByEmail("nonexistent");
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIncorrect() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> authService.login("testuser", "wrongpassword"));
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(jwtTokenProvider, never()).generateToken(any(User.class));
    }
}
