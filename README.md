# PayVerse

A production-shaped fintech backend built with Spring Boot, Kafka, Redis, and MySQL — six microservices behind a single API Gateway, implementing the core mechanics of a UPI-style payment platform: rate limiting, idempotent payments, optimistic-locked wallets, event-driven notifications, and an append-only ledger.

Built as a month-long deep dive connecting System Design theory directly to working code — every design decision below has a real implementation behind it, not just a diagram.

---

## Architecture

```
                        ┌─────────────────────┐
                        │   API Gateway :8080  │
                        │  JWT validation      │
                        │  Rate limiting (Redis)│
                        └──────────┬───────────┘
                                   │
        ┌───────────────┬─────────┼─────────┬───────────────┬───────────────┐
        │               │         │         │               │               │
   ┌────▼────┐    ┌─────▼────┐ ┌──▼───┐ ┌───▼──────────┐ ┌──▼──────┐ ┌──────▼─────┐
   │  User   │    │  Wallet  │ │Payment│ │ Notification │ │ Ledger  │ │ (internal) │
   │  :8085  │    │  :8081   │ │:8082  │ │    :8083     │ │  :8084  │ │            │
   └─────────┘    └──────────┘ └───┬───┘ └──────▲───────┘ └────▲────┘ └────────────┘
                                    │            │              │
                                    └─────► Kafka (payment-events) ──────┘
                                                  │
                              ┌───────────────────┴───────────────────┐
                              │      MySQL · Redis · Zookeeper          │
                              └──────────────────────────────────────────┘
```

| Service | Port | Responsibility |
|---|---|---|
| **API Gateway** | 8080 | Single entry point — JWT validation, user ID propagation, Redis-backed rate limiting |
| **User Service** | 8085 | Registration, login, JWT issuance, Redis-backed refresh tokens |
| **Wallet Service** | 8081 | Wallet balance, optimistic-locked concurrent updates, idempotent add-money |
| **Payment Service** | 8082 | Payment state machine, Kafka event publishing, Saga compensation on failure |
| **Notification Service** | 8083 | Kafka consumer, WebSocket/STOMP push, notification history, persistence-over-delivery |
| **Ledger Service** | 8084 | Append-only debit/credit entries, admin statistics |

**Infrastructure:** MySQL · Redis · Kafka · Zookeeper — all orchestrated via Docker Compose.

---

## Quick Start

```bash
git clone <repo-url>
cd payverse
docker-compose up
```

That's it — one command brings up all 6 services plus MySQL, Redis, Kafka, and Zookeeper, with health checks confirming everything is ready before traffic is expected to flow.

Verify everything is healthy:

```bash
docker-compose ps
```

---

## Core Design Decisions

### Rate limiting lives at the gateway, not in individual services
Implemented with a Redis Lua script wrapping `INCR` + `EXPIRE` as one atomic operation — two separate Redis calls aren't atomic together, and a crash between them can leave a counter with no expiry. The rate limiter sits at the API Gateway specifically so a request is rejected *before* it consumes any downstream service's resources.

### Payments are idempotent by design
Every payment request carries an idempotency key, checked against Redis with `SETNX` before any state-changing work happens. A client retry after a timeout returns the cached result instead of reprocessing — this is checked before the wallet debit, not after.

### Wallet concurrency is enforced structurally, not by convention
`Wallet` uses JPA's `@Version` field for optimistic locking. Two concurrent transfers hitting the same balance can't both silently succeed — one throws `OptimisticLockException` and retries safely.

### Failed payments compensate, they don't corrupt
Payment state machine: `INITIATED → PROCESSING → SUCCESS / FAILED`. If a credit fails after a debit already succeeded, a `PAYMENT_FAILED` event triggers a compensation consumer that reverses the original debit — Saga choreography, not a distributed transaction.

### The ledger is the source of truth, the wallet balance is a derived cache
Every `LedgerEntry` is an immutable, append-only event (debit or credit). The wallet's `balance` column is a convenience value — it can be reconstructed by summing the ledger at any time, which is exactly what the reconciliation job does. This is Event Sourcing, applied specifically to keep financial state auditable.

### Notifications guarantee persistence, not delivery
A notification is saved to the database first, independent of whether the real-time WebSocket push actually reaches the user. Delivery is best-effort on top of a guarantee that's already been kept — a dropped WebSocket connection never means a lost notification.

---

## Tech Stack

- **Language / Framework:** Java, Spring Boot
- **Messaging:** Kafka (producer idempotence, manual consumer commits, DLT via `@RetryableTopic`)
- **Caching / Coordination:** Redis (rate limiting, idempotency keys, cache-aside on wallet balance reads)
- **Persistence:** MySQL, Spring Data JPA
- **Testing:** JUnit 5, Mockito, Testcontainers (real MySQL + Kafka in integration tests), JaCoCo coverage
- **Resilience:** Resilience4j Circuit Breaker on inter-service calls

---

## Testing

Each service has its own unit test suite (Mockito-based) and integration tests using Testcontainers against real MySQL and Kafka — not mocks — for anything that depends on actual message delivery or database transactions.

```bash
# Run tests for a specific service
cd payverse-payment
mvn test

# Full build + test across all services
mvn clean install
```

Coverage is tracked via JaCoCo, targeting 80%+ on the service layer across all 6 services.

---

## Demo Walkthroughs

**5-minute walkthrough:** register → login → add money → transfer → notification received → ledger entry recorded — the full user journey, through the API Gateway as the single entry point, narrating the idempotency, optimistic locking, and Kafka decoupling decisions along the way.

**15–20 minute deep dive:** Saga compensation in `payverse-payment` (what happens when a credit fails after a debit), the rate limiter's position at the gateway (and why it moved there from sitting in front of `payverse-wallet` early on), and the ledger as an Event Sourcing implementation (why the wallet balance and the ledger are two different sources of truth).

To run the full smoke test yourself:

```bash
docker-compose down -v   # clean slate
docker-compose up        # fresh start, all services healthy
# then: register → login → add money → transfer → check notification → check ledger entry
```

---

## Project Status

PayVerse is feature-complete as of this build:

- ✅ All 6 services implemented, tested, and passing a fresh `docker-compose up` with zero manual fixes
- ✅ Full user journey verified end-to-end through the API Gateway
- ✅ JaCoCo coverage target met across all services
- ✅ Rate limiting, idempotency, optimistic locking, Saga compensation, and Event Sourcing all implemented with real, tested code behind each design

This was built as a month-long learning project connecting System Design theory (Rate Limiter, UPI Payment Gateway, Distributed Cache, Digital Wallet, Notification System) to a real, working implementation — the philosophy throughout was:

**Learn → Design → Implement → Test → Understand the failure mode → Revisit the design.**

---

## License

Personal learning project — not intended for production use as-is.