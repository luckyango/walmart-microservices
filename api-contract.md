# API Contract

This document defines the current local API contract for the Walmart microservice project. All browser traffic should go through the local API Gateway on `http://localhost:8080`.

## Gateway Entry Points

- `http://localhost:8080/api/account/**` -> Account Service
- `http://localhost:8080/api/items/**` -> Item Service
- `http://localhost:8080/api/orders/**` -> Order Service
- `http://localhost:8080/api/payments/**` -> Payment Service

Protected routes:

- `/api/orders/**`
- `/api/payments/**`

Authentication header:

```http
Authorization: Bearer <jwt-token>
```

## Common Error Shape

Most services return:

```json
{
  "timestamp": "2026-05-01T14:02:55.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Human readable error message"
}
```

Common status codes:

- `400 BAD_REQUEST`
- `401 UNAUTHORIZED`
- `403 FORBIDDEN`
- `409 CONFLICT`
- `500 INTERNAL_SERVER_ERROR`

## Account Service

Gateway prefix: `/api/account/users`

### Register

- `POST /api/account/users/register`
- Auth: no

Request:

```json
{
  "email": "alice@test.com",
  "username": "Alice",
  "password": "secret123",
  "shippingAddress": "123 Main St",
  "billingAddress": "123 Main St",
  "paymentMethod": "Visa ending 4242"
}
```

Response:

```json
{
  "id": 1,
  "email": "alice@test.com",
  "username": "Alice",
  "shippingAddress": "123 Main St",
  "billingAddress": "123 Main St",
  "paymentMethod": "Visa ending 4242"
}
```

### Login

- `POST /api/account/users/login?email={email}&password={password}`
- Auth: no

Response:

```json
{
  "token": "<jwt-token>",
  "user": {
    "id": 1,
    "email": "alice@test.com",
    "username": "Alice",
    "shippingAddress": "123 Main St",
    "billingAddress": "123 Main St",
    "paymentMethod": "Visa ending 4242"
  }
}
```

### Get Account

- `GET /api/account/users/{id}`
- Auth: currently optional at service layer, recommended to protect later

### Update Account

- `PUT /api/account/users/{id}`
- Auth: currently optional at service layer, recommended to protect later

## Item Service

Gateway prefix: `/api/items`

### Create Item

- `POST /api/items`
- Auth: no in current local version

Request:

```json
{
  "name": "Vitamin D3",
  "upc": "UPC-2003",
  "pictureUrl": "https://example.com/vitamin.jpg",
  "price": 26.99,
  "inventory": 20
}
```

### List Items

- `GET /api/items`
- Auth: no

### Get Item

- `GET /api/items/{id}`
- Auth: no

### Decrease Inventory

- `POST /api/items/{id}/decrease?qty={qty}`
- Auth: internal service-to-service
- Used by: Order Service
- Behavior: atomic MongoDB conditional update, rejects insufficient inventory

### Increase Inventory

- `POST /api/items/{id}/increase?qty={qty}`
- Auth: internal service-to-service
- Used by: Order Service cancel/update flows

## Order Service

Gateway prefix: `/api/orders`

Protected by JWT.

### Create Order

- `POST /api/orders?userId={userId}&itemId={itemId}&qty={qty}`
- Auth: yes

Response:

```json
{
  "id": 11,
  "userId": 1,
  "itemId": "680abc123",
  "quantity": 2,
  "totalPrice": 53.98,
  "status": "CREATED",
  "createdAt": "2026-05-01T14:08:00"
}
```

### Mark Order Paid

- `POST /api/orders/{id}/pay`
- Auth: yes

### Update Order Quantity

- `PUT /api/orders/{id}`
- Auth: yes

Request:

```json
{
  "quantity": 4
}
```

### Cancel Order

- `POST /api/orders/{id}/cancel`
- Auth: yes
- Behavior:
  - if `CREATED`, restock inventory and cancel
  - if `PAID`, request refund from Payment Service, then restock inventory, then cancel

### Get Order

- `GET /api/orders/{id}`
- Auth: yes

### Get Orders By User

- `GET /api/orders/user/{userId}`
- Auth: yes

## Payment Service

Gateway prefix: `/api/payments`

Protected by JWT.

### Submit Payment

- `POST /api/payments`
- Auth: yes

Request:

```json
{
  "orderId": 11,
  "userId": 1,
  "amount": 53.98,
  "paymentMethod": "Alice - card ending 4242 - exp 05/28",
  "idempotencyKey": "pay-order-11"
}
```

Behavior:

- same `idempotencyKey` returns the existing payment record

### Refund Payment By Payment Id

- `POST /api/payments/{id}/refund`
- Auth: yes

### Get Payment By Payment Id

- `GET /api/payments/{id}`
- Auth: yes

### Refund Payment By Order Id

- `POST /api/payments/order/{orderId}/refund`
- Auth: yes
- Used by: Order Service cancel flow

### Get Payment By Order Id

- `GET /api/payments/order/{orderId}`
- Auth: yes

## Service-to-Service Synchronous Communication

The current local implementation now uses `OpenFeign` in `order-service`.

### Order Service -> Item Service

- `GET /items/{id}`
- `POST /items/{id}/decrease?qty={qty}`
- `POST /items/{id}/increase?qty={qty}`

Client:

- [ItemServiceClient.java](/D:/26spring_interview/chuwa_file/walmart-micro-demo/walmart-microservices/order-service/src/main/java/com/example/orderservice/client/ItemServiceClient.java)

### Order Service -> Payment Service

- `POST /payments/order/{orderId}/refund`

Client:

- [PaymentServiceClient.java](/D:/26spring_interview/chuwa_file/walmart-micro-demo/walmart-microservices/order-service/src/main/java/com/example/orderservice/client/PaymentServiceClient.java)

## Planned Event Contract

Kafka is not fully implemented yet, but the intended next event contract is:

- `OrderCreatedEvent`
- `PaymentSucceededEvent`
- `PaymentFailedEvent`
- `OrderCancelledEvent`
- `InventoryReleasedEvent`

Recommended minimal payload:

```json
{
  "eventId": "uuid",
  "eventType": "OrderCreatedEvent",
  "occurredAt": "2026-05-01T14:10:00Z",
  "orderId": 11,
  "userId": 1,
  "itemId": "680abc123",
  "quantity": 2
}
```
