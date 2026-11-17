package com.shashi.minipay.gateway.filter;

import com.shashi.minipay.gateway.exception.GatewayJwtException;
import com.shashi.minipay.gateway.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JWT Authentication Filter - validates JWT tokens in Authorization header.
 *
 * FLOW:
 * 1. Extract "Authorization: Bearer <token>" header
 * 2. Validate token signature and expiration
 * 3. Extract userId, username, role from token
 * 4. Add to request headers for downstream services (X-User-Id, X-User-Role)
 * 5. If invalid: return 401 Unauthorized
 *
 * PUBLIC ENDPOINTS (skipped):
 * - /api/auth/** (register, login)
 * - /swagger**, /v3/api-docs (documentation)
 *
 * PROTECTED ENDPOINTS (validated):
 * - /api/payments/** (require authentication)
 * - /api/orders/** (require authentication)
 *
 * Learning Concepts:
 * - Global Filter: runs on all requests
 * - Reactive/Async: returns Mono for non-blocking I/O
 * - Header propagation: adds user context to downstream services
 * - Order: HIGHEST_PRECEDENCE runs first
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Filter logic - validate JWT and propagate user info.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.debug("Public endpoint, skipping JWT validation: {}", path);
            return chain.filter(exchange);
        }

        try {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Extract token
            String token = authHeader.substring(7);

            // Validate token
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Invalid token for: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Extract claims
            String userId = jwtTokenProvider.extractUserId(token);
            String username = jwtTokenProvider.extractUsername(token);
            String role = jwtTokenProvider.extractRole(token);

            log.debug("JWT validated for user: {}, role: {}", username, role);

            // Add headers for downstream services
            exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Name", username)
                    .header("X-User-Role", role)
                    .build();

            // Continue filter chain
            return chain.filter(exchange);

        } catch (GatewayJwtException e) {
            log.error("JWT validation error: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        } catch (Exception e) {
            log.error("Unexpected error in JWT filter: {}", e.getMessage(), e);
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * Check if endpoint is public (no authentication required).
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger") ||
               path.startsWith("/swagger-ui") ||
               path.equals("/health");
    }

    /**
     * Order: Run this filter before rate limiting.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
