# API Gateway

## Overview
Single entry point for all MiniPay microservices. Handles routing, JWT validation, rate limiting, and correlation ID propagation.

## Technology Stack
- Spring Cloud Gateway (reactive, non-blocking)
- JWT (token validation)
- Project Reactor (async I/O)

## Architecture

```
Client Request
    ↓
[API Gateway :8080]
    ↓
JwtAuthenticationFilter (validate token, extract claims)
    ↓
CorrelationIdFilter (generate/propagate correlation ID)
    ↓
RateLimitingFilter (enforce rate limits)
    ↓
[Route to Service]
    ├── /api/auth/** → Auth Service :8081
    ├── /api/payments/** → Payment Service :8082
    └── /api/orders/** → Order Service :8083
```

## Features

### 1. JWT Validation Filter
- Validates JWT tokens in Authorization header
- Extracts userId, username, role from token
- Propagates user context via headers (X-User-Id, X-User-Name, X-User-Role)
- Skips public endpoints (/api/auth/**)

### 2. Correlation ID Filter
- Generates unique correlation IDs for request tracing
- Propagates across all services for distributed tracing
- Enables searching logs by correlation ID

### 3. Rate Limiting Filter
- Token bucket algorithm
- Per-user rate limiting
- Configurable limits per endpoint:
  - /api/auth/login: 5 req/min
  - /api/auth/register: 10 req/min
  - /api/payments: 10 req/min
  - /api/orders: 20 req/min

## Configuration

### application.properties
```properties
server.port=8080
jwt.secret=<your-secret-key>
jwt.expiration=86400000

# Rate limiting
ratelimit.auth.login.requests-per-minute=5
ratelimit.auth.register.requests-per-minute=10
```

## Running

```bash
mvn spring-boot:run
```

Gateway will start on http://localhost:8080

## Integration with Services

### Auth Service (Port 8081)
- Start on port 8081
- Gateway routes `/api/auth/**` to Auth Service
- Public endpoints: no JWT required

### Payment Service (Port 8082)
- Start on port 8082
- Gateway routes `/api/payments/**` to Payment Service
- Protected endpoints: JWT required

### Order Service (Port 8083)
- Start on port 8083
- Gateway routes `/api/orders/**` to Order Service
- Protected endpoints: JWT required

## Example Requests

### Register User (public)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john",
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Login (public)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john",
    "password": "password123"
  }'
```
Response includes JWT token.

### Create Payment (protected)
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Idempotency-Key: unique-key-123" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "amount": 1000,
    "currency": "INR"
  }'
```

## Testing Locally

1. Start Auth Service (port 8081)
2. Start Payment Service (port 8082)
3. Start API Gateway (port 8080)
4. Use Postman/curl to test endpoints through gateway

## Learning Concepts

- **Gateway Pattern**: Centralized entry point for microservices
- **Reactive Programming**: Non-blocking I/O with Project Reactor
- **Token Bucket Algorithm**: Rate limiting implementation
- **Distributed Tracing**: Correlation IDs across services
- **Global Filters**: Pre/post-processing of requests
- **JWT Validation**: Stateless authentication
- **Header Propagation**: User context distribution

## Production Considerations

- Use Redis for distributed rate limiting (not in-memory)
- Implement circuit breakers for service failures
- Add API key authentication for service-to-service calls
- Configure HTTPS
- Use strong JWT secret (32+ chars)
- Implement request logging/tracing
- Add monitoring and alerting
