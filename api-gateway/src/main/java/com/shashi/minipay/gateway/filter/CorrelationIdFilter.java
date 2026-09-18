 package com.shashi.minipay.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Correlation ID Filter - generates/propagates correlation IDs for distributed tracing.
 *
 * CONCEPT: Correlation ID (or Request ID) is a unique identifier that follows
 * a request across multiple services, enabling end-to-end tracing.
 *
 * FLOW:
 * 1. Check if request has "X-Correlation-Id" header
 * 2. If not present: Generate new UUID
 * 3. Add/keep header in request and response
 * 4. Forward to downstream services
 *
 * BENEFITS:
 * - Search logs for correlation ID to find all related requests
 * - Debug issues across multiple services
 * - Understand request flow through microservices
 *
 * EXAMPLE:
 * Request 1 (corr-id: abc-123)
 *   → Auth Service (logs: abc-123)
 *   → Payment Service (logs: abc-123)
 * Later: grep logs for "abc-123" finds all related processing
 *
 * Learning Concepts:
 * - Distributed tracing
 * - Request correlation across services
 * - Observability in microservices
 */
@Component
@Slf4j
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);

        // Generate new correlation ID if not present
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new correlation ID: {}", correlationId);
        } else {
            log.debug("Using existing correlation ID: {}", correlationId);
        }

        // Add correlation ID to request headers
        exchange.getRequest().mutate()
                .header(CORRELATION_ID_HEADER, correlationId)
                .build();

        // Add correlation ID to response headers
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, correlationId);

        // Store in exchange attributes for later access in other filters
        exchange.getAttributes().put("correlationId", correlationId);

        return chain.filter(exchange);
    }

    /**
     * Order: Run after JWT but before rate limiting.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
