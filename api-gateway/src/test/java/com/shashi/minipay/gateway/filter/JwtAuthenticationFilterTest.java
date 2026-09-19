package com.shashi.minipay.gateway.filter;

import com.shashi.minipay.gateway.security.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String SECRET = "test-secret-key-for-jwt-token-generation-and-validation-must-be-at-least-32-characters";
    private static final long EXPIRATION = 3600000;

    @BeforeEach
    void setUp() {
        // No setup needed - chain will be mocked per test
    }

    @Test
    void filter_ShouldSkipValidation_WhenPublicEndpoint() {
        // Given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .post("/api/auth/login")
                        .build()
        );
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        verify(chain, times(1)).filter(exchange);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void filter_ShouldSkipValidation_WhenSwaggerEndpoint() {
        // Given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("/swagger-ui/index.html")
                        .build()
        );
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        verify(chain, times(1)).filter(exchange);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void filter_ShouldSkipValidation_WhenHealthEndpoint() {
        // Given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("/health")
                        .build()
        );
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        jwtAuthenticationFilter.filter(exchange, chain);

        // Then
        verify(chain, times(1)).filter(exchange);
        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    void filter_ShouldReturn401_WhenAuthorizationHeaderMissing() {
        // Given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .post("/api/payments")
                        .build()
        );

        // When
        jwtAuthenticationFilter.filter(exchange, chain).block();

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_ShouldReturn401_WhenAuthorizationHeaderInvalid() {
        // Given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .post("/api/payments")
                        .header(HttpHeaders.AUTHORIZATION, "InvalidFormat token")
                        .build()
        );

        // When
        jwtAuthenticationFilter.filter(exchange, chain).block();

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_ShouldReturn401_WhenTokenInvalid() {
        // Given
        String invalidToken = "invalid.token.string";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .post("/api/payments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invalidToken)
                        .build()
        );

        when(jwtTokenProvider.validateToken(invalidToken)).thenReturn(false);

        // When
        jwtAuthenticationFilter.filter(exchange, chain).block();

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtTokenProvider, times(1)).validateToken(invalidToken);
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_ShouldAddUserHeaders_WhenTokenValid() {
        // Given
        String validToken = generateValidToken();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .post("/api/payments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                        .build()
        );

        when(jwtTokenProvider.validateToken(validToken)).thenReturn(true);
        when(jwtTokenProvider.extractUserId(validToken)).thenReturn("user-123");
        when(jwtTokenProvider.extractUsername(validToken)).thenReturn("testuser");
        when(jwtTokenProvider.extractRole(validToken)).thenReturn("CUSTOMER");
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        jwtAuthenticationFilter.filter(exchange, chain).block();

        // Then
        verify(jwtTokenProvider, times(1)).validateToken(validToken);
        verify(jwtTokenProvider, times(1)).extractUserId(validToken);
        verify(jwtTokenProvider, times(1)).extractUsername(validToken);
        verify(jwtTokenProvider, times(1)).extractRole(validToken);
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_ShouldReturn500_WhenUnexpectedError() {
        // Given
        String token = generateValidToken();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .post("/api/payments")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
        );

        when(jwtTokenProvider.validateToken(token)).thenThrow(new RuntimeException("Unexpected error"));

        // When
        jwtAuthenticationFilter.filter(exchange, chain).block();

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
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
}
