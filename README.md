# Walmart Microservice First Demo

This repository is the first-demo version of an online shopping backend inspired by the course final project. It is optimized for a stable demo first, while keeping the codebase easy to extend in the next phase.

## First Demo Scope

Completed in this version:

| Service | Port | Current role |
|---|---:|---|
| `user-service` | 8081 | Acts as the current `Account Service` for register, login, lookup, and update |
| `item-service` | 8082 | Item metadata and inventory |
| `order-service` | 8083 | Create, update, pay, cancel, and lookup orders |
| `payment-service` | 8084 | Submit payment, refund payment, lookup payment, idempotency key support |
| `frontend` | local html | Walmart-style demo storefront with cart and checkout flow |

Demo flow currently supported:

1. Register account
2. Login account
3. Create or seed items
4. Add item to cart
5. Checkout cart into order
6. Update order quantity
7. Submit payment
8. Mark order as paid
9. Cancel order
10. Refund payment
11. Lookup order and payment

## Run Locally

Open 4 terminals from the repo root and start each service:

```bash
cd user-service
mvn spring-boot:run
```

```bash
cd item-service
mvn spring-boot:run
```

```bash
cd order-service
mvn spring-boot:run
```

```bash
cd payment-service
mvn spring-boot:run
```

Then open:

```bash
frontend/index.html
```

## Swagger

Each service exposes Swagger UI:

- Account Service: `http://localhost:8081/swagger-ui.html`
- Item Service: `http://localhost:8082/swagger-ui.html`
- Order Service: `http://localhost:8083/swagger-ui.html`
- Payment Service: `http://localhost:8084/swagger-ui.html`

## Main APIs In First Demo

Account Service (`user-service` for now):

- `POST /users/register`
- `POST /users/login`
- `GET /users/{id}`
- `PUT /users/{id}`

Item Service:

- `POST /items`
- `GET /items`
- `GET /items/{id}`
- `POST /items/{id}/decrease?qty=`
- `POST /items/{id}/increase?qty=`

Order Service:

- `POST /orders?userId=&itemId=&qty=`
- `PUT /orders/{id}`
- `POST /orders/{id}/pay`
- `POST /orders/{id}/cancel`
- `GET /orders/{id}`
- `GET /orders/user/{userId}`

Payment Service:

- `POST /payments`
- `POST /payments/{id}/refund`
- `GET /payments/{id}`

## Engineering Improvements Included

- Swagger added for all services
- Global exception handling added to avoid raw stacktrace demo failures
- Service-layer unit tests added for all four services
- Minimal payment idempotency implemented through `idempotencyKey`
- Minimal cart flow implemented in frontend without adding a separate cart service yet

## Important Simplifications

To keep the first demo runnable in limited time, this version still uses:

- H2 for all services
- simple demo token instead of real auth server or Spring Security
- synchronous REST only for business flow
- log statements instead of Kafka events
- plain Maven local startup instead of Docker Compose

## Next Phase

Planned next-phase work to align with the full final project requirements:

1. Spring Security and token validation
2. Docker Compose for one-click startup
3. Kafka for event-driven async communication
4. Real databases:
   `Account -> MySQL/PostgreSQL`
   `Item -> MongoDB`
   `Order -> Cassandra`
5. OpenFeign for service-to-service calls
6. Jacoco coverage reports and stronger test coverage
7. Rename `user-service` to `account-service` at the code/module level if desired
