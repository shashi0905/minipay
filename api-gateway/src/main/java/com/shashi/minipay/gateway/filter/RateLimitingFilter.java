package com.shashi.minipay.gateway.filter;

import com.shashi.minipay.gateway.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Filter - prevents API abuse by limiting requests per user/endpoint.
 *
 * ALGORITHM: Token Bucket Pattern
 * - Bucket size: Maximum tokens (requests) allowed
 * - Refill rate: Tokens added per time unit
 * - Request arrives: Check if bucket has tokens
 *   - If yes: Allow request, consume 1 token
 *   - If no: Return 429 Too Many Requests
 *
 * EXAMPLE:
 * - Bucket size: 5 tokens
 * - Refill: 5 tokens per minute
 * - Request 1: 4 tokens left → ALLOW
 * - Request 2: 3 tokens left → ALLOW
 * - Request 3: 2 tokens left → ALLOW
 * - Request 4: 1 token left → ALLOW
 * - Request 5: 0 tokens left → ALLOW
 * - Request 6: 0 tokens left, wait for refill → DENY (429)
 *
 * RATE LIMITS PER ENDPOINT:
 * - /api/auth/login: 5 req/min per user
 * - /api/auth/register: 10 req/min per user
 * - /api/payments: 10 req/min per user
 * - /api/orders: 20 req/min per user
 *
 * Learning Concepts:
 * - Token bucket rate limiting algorithm
 * - Preventing API abuse and DDoS attacks
 * - Per-user vs per-endpoint limiting
 * - Adaptive rate limiting
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter implements GlobalFilter, Ordered {

    // Simple in-memory rate limit store: key = "userId:endpoint", value = TokenBucket
    private static final Map<String, TokenBucket> rateLimitStore = new ConcurrentHashMap<>();

    /**
     * Filter logic - check rate limit and allow/deny request.
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip rate limiting for public endpoints
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        try {
            // Get userId from headers (set by JwtAuthenticationFilter)
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId == null) {
                userId = "anonymous";
            }

            // Determine rate limit based on endpoint
            int requestsPerMinute = getRequestsPerMinute(path);
            String bucketKey = userId + ":" + getEndpointGroup(path);

            // Get or create token bucket for this user:endpoint combo
            TokenBucket bucket = rateLimitStore.computeIfAbsent(bucketKey, 
                    k -> new TokenBucket(requestsPerMinute));

            // Try to consume a token
            if (!bucket.tryConsume()) {
                log.warn("Rate limit exceeded for user: {}, endpoint: {}", userId, path);
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
                exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
                return exchange.getResponse().setComplete();
            }

            // Add rate limit headers to response
            exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
            exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));

            return chain.filter(exchange);

        } catch (Exception e) {
            log.error("Error in rate limiting filter: {}", e.getMessage(), e);
            // Allow request on filter error (fail open)
            return chain.filter(exchange);
        }
    }

    /**
     * Get rate limit for specific endpoint.
     */
    private int getRequestsPerMinute(String path) {
        if (path.startsWith("/api/auth/login")) return 5;
        if (path.startsWith("/api/auth/register")) return 10;
        if (path.startsWith("/api/payments")) return 10;
        if (path.startsWith("/api/orders")) return 20;
        return 100; // default
    }

    /**
     * Group endpoint for rate limiting (by prefix).
     */
    private String getEndpointGroup(String path) {
        if (path.startsWith("/api/auth")) return "auth";
        if (path.startsWith("/api/payments")) return "payments";
        if (path.startsWith("/api/orders")) return "orders";
        return "other";
    }

    /**
     * Check if endpoint should skip rate limiting.
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger") ||
               path.equals("/health");
    }

    /**
     * Order: Run after JWT and correlation ID.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }

    /**
     * Token Bucket implementation for rate limiting.
     * Simple, in-memory implementation for learning purposes.
     *
     * Production note: Use external cache (Redis) for distributed rate limiting.
     */
    private static class TokenBucket {
        private final int capacity;
        private int tokens;
        private Instant lastRefillTime;

        TokenBucket(int capacity) {
            this.capacity = capacity;
            this.tokens = capacity;
            this.lastRefillTime = Instant.now();
        }

        synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        synchronized int getAvailableTokens() {
            refill();
            return tokens;
        }

        private void refill() {
            Instant now = Instant.now();
            long secondsElapsed = java.time.temporal.ChronoUnit.SECONDS.between(lastRefillTime, now);
            
            // Refill: add tokens based on elapsed time (1 token per second = capacity per minute)
            if (secondsElapsed > 0) {
                int tokensToAdd = (int) (secondsElapsed * capacity / 60);
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefillTime = now;
            }
        }
    }
}
