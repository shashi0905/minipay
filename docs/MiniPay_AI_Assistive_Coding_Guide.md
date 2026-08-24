# MiniPay — AI-Assisted Microservices Coding Project

*A hands-on Spring Boot payment-domain demo for learning microservices, security, API design, resilience, and event-driven processing.*

## 1. Purpose

MiniPay is a deliberately small payment-domain application designed to be built incrementally with an AI coding agent. It is not intended to be production-ready. The goal is to understand the code, architecture, request flow, state transitions, failure modes, and testing strategy.

## 2. Learning Objectives

- Build independently deployable Spring Boot microservices.
- Practice API Gateway routing, JWT authentication, authorization, and rate limiting.
- Implement REST APIs with DTOs, validation, exception handling, persistence, and tests.
- Implement an order → payment workflow and payment state machine.
- Practice payment concepts: idempotency, duplicate requests, provider failures, and timeouts.
- Introduce Kafka or RabbitMQ for asynchronous events.
- Practice retries, timeouts, circuit breakers, and failure handling.
- Run the system locally with Docker Compose.

## 3. Target Architecture

```text
Client / Postman
      |
      v
API Gateway
  |       |       |
  v       v       v
Auth   Order   Payment
Service Service Service
                 |
                 v
        Mock Payment Provider

Order Service and Payment Service each own their database.
Later: Kafka/RabbitMQ connects Payment events to Order Service.
```

## 4. Services

| Service | Responsibility |
|---|---|
| `api-gateway` | Routing, JWT validation, correlation ID, rate limiting |
| `auth-service` | Registration, BCrypt password hashing, login, JWT, roles |
| `order-service` | Order creation/retrieval/cancellation and order state |
| `payment-service` | Payment creation, idempotency, provider calls and payment state |
| `mock-payment-provider` | Simulates success, decline, timeout and server-error responses |

## 5. Technology Stack

- Java 21
- Spring Boot
- Spring Cloud Gateway
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Maven
- JUnit 5 + Mockito
- Docker Compose
- Resilience4j
- Kafka or RabbitMQ
- Redis (optional later)

## 6. Domain Model

### User

```text
id
username/email
passwordHash
role
createdAt
```

### Order

```text
id
userId
amount
currency
status
createdAt
updatedAt
```

Order statuses:

```text
CREATED
PAYMENT_PENDING
PAID
PAYMENT_FAILED
CANCELLED
```

### Payment

```text
id
orderId
userId
amount
currency
status
providerTransactionId
idempotencyKey
createdAt
updatedAt
```

Payment statuses:

```text
CREATED
PROCESSING
SUCCESS
FAILED
CANCELLED
```

## 7. REST API Contract

| Endpoint | Purpose |
|---|---|
| `POST /api/auth/register` | Register user |
| `POST /api/auth/login` | Authenticate and receive JWT |
| `POST /api/orders` | Create order |
| `GET /api/orders/{orderId}` | Get order |
| `GET /api/orders` | List current user's orders |
| `POST /api/orders/{orderId}/cancel` | Cancel eligible order |
| `POST /api/payments` | Initiate payment |
| `GET /api/payments/{paymentId}` | Get payment |
| `GET /api/payments/order/{orderId}` | Get payment for an order |

## 8. Example Contracts

### Create Order

```http
POST /api/orders
Authorization: Bearer <JWT>
Content-Type: application/json
```

```json
{
  "items": [
    {
      "productId": "P100",
      "quantity": 2,
      "price": 500
    }
  ],
  "currency": "INR"
}
```

Example response:

```json
{
  "orderId": "ORD-10001",
  "amount": 1000,
  "currency": "INR",
  "status": "PAYMENT_PENDING"
}
```

### Initiate Payment

```http
POST /api/payments
Authorization: Bearer <JWT>
Idempotency-Key: 8f7a9c21-1234
Content-Type: application/json
```

```json
{
  "orderId": "ORD-10001",
  "paymentMethod": "CARD"
}
```

## 9. Authentication

- Use BCrypt for passwords.
- Login returns a signed JWT containing identity and role.
- Gateway validates JWT for protected routes.
- Auth endpoints are public; order/payment endpoints require authentication.
- Customers can access only their own orders/payments.
- Add at least one ADMIN-only capability for authorization practice.

## 10. API Gateway

- Route `/api/auth/**` → Auth Service.
- Route `/api/orders/**` → Order Service.
- Route `/api/payments/**` → Payment Service.
- Generate or propagate a correlation/request ID.
- Keep business logic out of the gateway.
- Start with an in-memory rate limiter; optionally replace it with Redis later.

Suggested limits:

```text
Login API       → 5 requests/minute
Create Order    → 20 requests/minute
Payment API     → 10 requests/minute
```

## 11. Payment Workflow

### Happy Path

```text
1. Create order
        ↓
2. Order becomes PAYMENT_PENDING
        ↓
3. Initiate payment
        ↓
4. Payment Service creates payment
        ↓
5. Payment Service calls Mock Provider
        ↓
6. Provider succeeds
        ↓
7. Payment becomes SUCCESS
        ↓
8. Order becomes PAID
```

### Failure Path

```text
Provider failure
      ↓
Payment FAILED
      ↓
Order PAYMENT_FAILED
```

## 12. Idempotency

Payment creation must be idempotent. Repeating the same request with the same `Idempotency-Key` must not create a second charge.

Requirements:

- Require `Idempotency-Key` for `POST /api/payments`.
- Persist the key with the payment request/result.
- Return the previous result when the same key is replayed.
- Reject reuse of a key with materially different payment data.
- Add repeated/concurrent request tests.

## 13. Mock Payment Provider

Create a `PaymentProviderClient` abstraction and a mock implementation.

Support deterministic outcomes:

```text
SUCCESS
DECLINED
TIMEOUT
SERVER_ERROR
```

Do not use real card or banking data.

## 14. Transaction Rules

- Use database transactions for state changes inside one service.
- Do not use distributed transactions.
- Assume network calls can fail after a local transaction succeeds.
- Implement explicit legal state transitions.
- Do not allow `PAID` to silently return to `PAYMENT_PENDING`.

## 15. Incremental Build Plan

| Phase | Implementation Goal |
|---|---|
| 0 — Setup | Repository structure, Maven setup, README, Docker Compose skeleton |
| 1 — Auth | Registration, BCrypt, login, JWT, roles and tests |
| 2 — Gateway | Routing, JWT validation, correlation ID and gateway errors |
| 3 — Order | REST API, DTOs, validation, JPA/PostgreSQL, state transitions and tests |
| 4 — Payment | Payment entity, workflow, provider abstraction, mock provider and idempotency |
| 5 — End-to-End | Gateway → Order → Payment → Provider; integration tests |
| 6 — Rate Limiting | In-memory first; Redis-backed option later |
| 7 — Messaging | Kafka/RabbitMQ; `PaymentCompleted` and `PaymentFailed` events |
| 8 — Resilience | Timeouts, retries, circuit breaker and failure simulation |
| 9 — Hardening | Observability, API docs, security review, tests and cleanup |

## 16. AI Coding Agent Workflow

1. Work on one phase at a time.
2. Ask the agent to explain the design before generating substantial code.
3. Implement the smallest change that satisfies the current milestone.
4. Run tests after each meaningful change.
5. Ask why important classes, annotations and configuration exist.
6. Ask for edge cases before implementing them.
7. Do not let the agent introduce unrelated frameworks.
8. Keep Git commits small and milestone-oriented.

## 17. Master Prompt for the AI Agent

> Act as a senior Java/Spring Boot engineer and pair-programming mentor. We are building MiniPay, a learning-oriented payment microservices application. Implement only the current milestone. Before coding, explain the design, important classes, request flow, failure cases, and testing strategy. Prefer clean, conventional Spring Boot patterns. Do not add technologies that are not required. After implementation, provide the commands to run tests and explain the key code I should study.
>
> Current milestone: `<INSERT MILESTONE>`

## 18. Learning Prompts

- "Explain this class line by line before suggesting changes."
- "Why does this logic belong in the service rather than the controller?"
- "Show two approaches and explain which is preferable here."
- "What happens if the provider times out after the payment record is saved?"
- "Find race conditions in this idempotency implementation."
- "Write tests that expose the bug before changing production code."
- "Review this implementation as if it were a Java microservices interview."

## 19. Testing Strategy

- Controller tests: validation, status codes and DTO mapping.
- Service unit tests: state transitions and business rules.
- Security tests: missing/invalid/expired JWT, roles and ownership.
- Gateway tests: routing and authentication filters.
- Payment tests: success, decline, timeout, duplicate request and invalid state.
- Integration tests: Order + Payment workflow.
- End-to-end smoke test through the Gateway.

## 20. Failure Scenarios

- Invalid/expired JWT
- Insufficient permissions
- Unknown order
- Order belongs to another user
- Payment attempted for cancelled order
- Duplicate payment
- Same idempotency key with different payload
- Provider decline
- Provider HTTP 500
- Provider timeout
- Database unavailable
- Delayed message
- Duplicate message event

## 21. Event-Driven Extension

After the REST version is stable, introduce Kafka or RabbitMQ.

Suggested events:

```text
OrderCreated
PaymentInitiated
PaymentCompleted
PaymentFailed
OrderCancelled
```

Replace synchronous Payment → Order status propagation with events and study:

- Eventual consistency
- Duplicate events
- Consumer idempotency
- Message retries
- Dead-letter queues
- Consumer failure
- Event ordering

## 22. Observability

- Use structured logs where practical.
- Include `correlationId` in logs.
- Log `orderId`/`paymentId`, not sensitive payment information.
- Never log card numbers, CVV, passwords, JWTs or secrets.
- Add Spring Boot Actuator health endpoints.
- Optionally add Micrometer metrics.

## 23. Security Rules

- Never store or log real card data.
- Use fake payment methods only.
- Keep secrets outside source code.
- Validate external input.
- Derive user identity from the authenticated principal rather than trusting a customer-supplied `userId`.
- Return safe API errors without internal stack traces.
- Do not commit JWT signing secrets or database passwords.

## 24. Definition of Done

- [ ] User can register and log in.
- [ ] JWT-protected requests pass through the Gateway.
- [ ] Customer can create/retrieve their own orders.
- [ ] Order can enter `PAYMENT_PENDING`.
- [ ] Payment can be initiated and processed by the mock provider.
- [ ] Successful payment updates payment/order states correctly.
- [ ] Failed payment updates payment/order states correctly.
- [ ] Duplicate payment requests do not create duplicate charges.
- [ ] Rate limiting can be demonstrated.
- [ ] At least one timeout/failure scenario is tested.
- [ ] Application runs locally with documented commands.
- [ ] Important business rules have unit/integration tests.

## 25. Suggested Repository Structure

```text
minipay/
├── api-gateway/
├── auth-service/
├── order-service/
├── payment-service/
├── mock-payment-provider/
├── docker-compose.yml
├── docs/
│   ├── architecture.md
│   ├── api-contracts.md
│   └── decisions.md
└── README.md
```

## 26. What Not to Build

Do not initially build:

- Real card processing or banking integration
- Complex product/catalog management
- Kubernetes
- Distributed transactions
- Production PCI compliance
- A large number of microservices merely to increase the service count
- A full frontend unless it helps your learning

## 27. Recommended Learning Order

1. Auth + JWT
2. Gateway + JWT validation
3. Order REST API + database
4. Payment REST API + database
5. Mock provider
6. End-to-end payment flow
7. Idempotency
8. Rate limiting
9. Messaging
10. Resilience and failure handling
11. Testing and observability

## 28. Final Learning Challenge

Once the application works, deliberately break one reliability mechanism and diagnose the behavior before asking the AI agent for help.

Try:

- Remove idempotency and submit the same payment twice.
- Introduce provider timeouts.
- Duplicate a Kafka/RabbitMQ event.
- Disable the database.
- Bypass the API Gateway.
- Send an expired JWT.
- Make the provider return HTTP 500.

For each failure, answer:

1. What failed?
2. Which service detected the failure?
3. What state was persisted?
4. Could the operation be safely retried?
5. Could the customer be charged twice?
6. What should the API return?
7. What should be logged?
8. How would you improve the design?

---

## Final Learning Objective

The objective is to understand **every request, state transition, failure mode, and design decision**—not merely to make the demo run.

The AI coding agent should accelerate implementation while you remain responsible for understanding and validating the architecture and code.
