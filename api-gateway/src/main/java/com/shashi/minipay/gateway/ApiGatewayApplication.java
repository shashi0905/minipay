package com.shashi.minipay.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * API Gateway Application - Single entry point for all MiniPay microservices.
 *
 * RESPONSIBILITIES:
 * 1. Route requests to appropriate microservices (auth, order, payment)
 * 2. Validate JWT tokens
 * 3. Enforce rate limiting
 * 4. Propagate correlation IDs for distributed tracing
 * 5. Handle errors centrally
 *
 * TECHNOLOGY: Spring Cloud Gateway (reactive, non-blocking)
 */
@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

	/**
	 * Define routes to backend services.
	 *
	 * GATEWAY FLOW:
	 * Client Request → Gateway Port 8080
	 *   ↓
	 * Apply Filters (JWT validation, correlation ID, rate limiting)
	 *   ↓
	 * Route to appropriate backend service (8081, 8082, 8083)
	 *   ↓
	 * Service Response → Gateway → Client
	 */
	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		return builder.routes()
				// ============ AUTH SERVICE ROUTES ============
				// All /api/auth/** requests go to Auth Service on port 8081
				.route("auth-service", r -> r
						.path("/api/auth/**")
						.uri("http://localhost:8081"))

				// ============ PAYMENT SERVICE ROUTES ============
				// All /api/payments/** requests go to Payment Service on port 8082
				.route("payment-service", r -> r
						.path("/api/payments/**")
						.uri("http://localhost:8082"))

				// ============ ORDER SERVICE ROUTES ============
				// All /api/orders/** requests go to Order Service on port 8083 (future)
				.route("order-service", r -> r
						.path("/api/orders/**")
						.uri("http://localhost:8083"))

				.build();
	}
}
