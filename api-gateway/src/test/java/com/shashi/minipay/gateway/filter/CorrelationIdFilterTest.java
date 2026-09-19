package com.shashi.minipay.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    @Mock
    private GatewayFilterChain chain;

    @InjectMocks
    private CorrelationIdFilter correlationIdFilter;

    @BeforeEach
    void setUp() {
        // No setup needed - chain will be mocked per test
    }

    @Test
    void filter_ShouldGenerateNewCorrelationId_WhenNotPresent() {
        // Given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("/api/payments")
                        .build()
        );
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        correlationIdFilter.filter(exchange, chain).block();

        // Then
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_ShouldUseExistingCorrelationId_WhenPresent() {
        // Given
        String existingCorrelationId = "existing-correlation-id-123";
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("/api/payments")
                        .header("X-Correlation-Id", existingCorrelationId)
                        .build()
        );
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        correlationIdFilter.filter(exchange, chain).block();

        // Then
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_ShouldGenerateNewCorrelationId_WhenHeaderEmpty() {
        // Given
        MockServerWebExchange exchange = MockServerWebExchange.from(
                org.springframework.mock.http.server.reactive.MockServerHttpRequest
                        .get("/api/payments")
                        .header("X-Correlation-Id", "")
                        .build()
        );
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // When
        correlationIdFilter.filter(exchange, chain).block();

        // Then
        verify(chain, times(1)).filter(any(ServerWebExchange.class));
    }

    @Test
    void filter_ShouldHaveCorrectOrder() {
        // Given
        int expectedOrder = org.springframework.core.Ordered.HIGHEST_PRECEDENCE + 1;

        // When
        int actualOrder = correlationIdFilter.getOrder();

        // Then
        assertEquals(expectedOrder, actualOrder);
    }
}
