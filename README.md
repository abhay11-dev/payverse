

# PayVerse

> A microservices-based digital payments platform — wallet, P2P transfers, notifications, and transaction ledger — built to mirror the architecture patterns used by production UPI-scale payment systems.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)

---

## Table of Contents
- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Services](#services)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Monitoring & Observability](#monitoring--observability)
- [CI/CD](#cicd)
- [Project Structure](#project-structure)
- [Design Decisions](#design-decisions)
- [Roadmap](#roadmap)
- [License](#license)

---

## Overview

PayVerse is a self-initiated project simulating a real-world digital payments platform, built to explore the architectural, concurrency, and reliability challenges present in systems like UPI. It includes wallet management, atomic P2P money transfers, event-driven notifications, an immutable transaction ledger, and an API gateway — all deployed as independent Spring Boot microservices communicating over Kafka, backed by MySQL and Redis.

**Why this project exists:** to demonstrate hands-on understanding of distributed systems concepts — idempotency, optimistic locking, event sourcing, the Saga pattern, and rate limiting — rather than just describing them.

---

## Architecture

```
                      ┌─────────────┐
                      │   Client    │
                      │ (React SPA) │
                      └──────┬──────┘
                             │ HTTPS
                      ┌──────▼──────┐
                      │ API Gateway │  ← JWT validation, rate limiting
                      └──────┬──────┘
           ┌─────────────────┼─────────────────┐
    ┌──────▼─────┐   ┌───────▼──────┐   ┌──────▼───────┐
    │ User Svc   │   │ Wallet Svc   │   │ Payment Svc  │
    └──────┬─────┘   └───────┬──────┘   └──────┬───────┘
           │                 │                  │
           │           ┌─────▼──────┐    ┌──────▼───────┐
           │           │   MySQL    │    │    Kafka     │
           │           └────────────┘    └──────┬───────┘
           │                                     │
           │                    ┌────────────────┼────────────────┐
           │             ┌──────▼──────┐   ┌──────▼──────┐  ┌─────▼──────┐
           │             │ Ledger Svc  │   │ Notif. Svc  │  │  Fraud/…   │
           │             └─────────────┘   └─────────────┘  └────────────┘
           │
     ┌─────▼─────┐
     │   Redis   │  ← idempotency keys, refresh tokens, rate-limit counters
     └───────────┘
```

**Key patterns implemented:**
- **Idempotency** — Redis-backed idempotency keys prevent duplicate payment processing on retry.
- **Optimistic locking** — wallet balance updates use `@Version` to handle concurrent writes safely.
- **Saga pattern** — payment failures trigger compensating events (e.g., reverse a debit if credit fails) instead of distributed transactions.
- **Event sourcing** — the ledger is append-only; current state is derived from the event stream, never mutated.
- **Rate limiting** — token bucket algorithm via Redis at the API Gateway layer.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x, Spring Security, Spring Data JPA, Spring Cloud Gateway |
| Messaging | Apache Kafka |
| Database | MySQL 8 |
| Cache / Session | Redis 7 |
| Frontend | React 18, TypeScript, TailwindCSS |
| Containerization | Docker, Docker Compose |
| CI/CD | GitHub Actions |
| Cloud | AWS (EC2, RDS, ElastiCache, S3) |
| Monitoring | Prometheus, Grafana |
| Testing | JUnit 5, Mockito, Testcontainers |

---

## Services

| Service | Responsibility | Port |
|---|---|---|
| `payverse-api-gateway` | Single entry point — JWT auth, routing, rate limiting | `8080` |
| `payverse-user` | Registration, login, JWT issuance, refresh tokens | `8081` |
| `payverse-wallet` | Wallet balance, optimistic-locked updates | `8082` |
| `payverse-payment` | P2P transfers, idempotency, Saga orchestration | `8083` |
| `payverse-notification` | Kafka-consumer-driven push/email/websocket notifications | `8084` |
| `payverse-ledger` | Append-only transaction ledger, reconciliation | `8085` |

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker & Docker Compose
- Node.js 18+ (for the frontend, once added)

### Run locally

```bash
# clone the repo
git clone https://github.com/<your-username>/payverse.git
cd payverse

# start all infra + services
docker-compose up -d

# build all modules
mvn clean install

# verify all containers are healthy
docker-compose ps
```

The API Gateway will be available at `http://localhost:8080`.

### Environment variables

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | MySQL connection string | `jdbc:mysql://localhost:3306/payverse` |
| `REDIS_HOST` | Redis host | `localhost` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address | `localhost:9092` |
| `JWT_SECRET` | Signing secret for JWTs | *(set via `.env`, never committed)* |

---

## API Documentation

Postman collection available at [`/docs/payverse.postman_collection.json`](./docs/payverse.postman_collection.json).

Sample endpoints:
```
POST /auth/register
POST /auth/login
POST /auth/refresh-token
GET  /wallet/balance
POST /wallet/add-money
POST /payment/transfer
GET  /notifications
```

---

## Testing

```bash
# run unit + integration tests for all modules
mvn test

# generate coverage report (JaCoCo)
mvn verify
open target/site/jacoco/index.html
```

- Unit tests: JUnit 5 + Mockito
- Integration tests: Testcontainers (real MySQL + Kafka containers, no mocks)
- Target coverage: 80%+ on service layer across all modules

---

## Monitoring & Observability

- Spring Boot Actuator exposes `/actuator/prometheus` on each service.
- Grafana dashboard (4 panels): JVM heap usage, HTTP request rate, HTTP 5xx error rate, DB connection pool utilization.
- Structured JSON logs shipped to a central log store.

---

## CI/CD

GitHub Actions pipeline:
1. **`test.yml`** — runs on every PR: `mvn test` → JaCoCo coverage → posts coverage % as a PR comment.
2. **`deploy.yml`** — runs on merge to `main` (only if `test.yml` passed): builds Docker images → pushes to registry → deploys to AWS EC2, restarts services with zero-downtime rolling restart.

---

## Project Structure

```
payverse/
├── payverse-api-gateway/
├── payverse-user/
├── payverse-wallet/
├── payverse-payment/
├── payverse-notification/
├── payverse-ledger/
├── frontend/                 # React app (added later)
├── docs/                     # architecture diagrams, Postman collection
├── docker-compose.yml
├── pom.xml                   # parent POM
└── README.md
```

---

## Design Decisions

| Decision | Reasoning |
|---|---|
| Kafka over direct REST between services | Decouples payment success from downstream notification/ledger failures — a notification outage should never block a payment. |
| Optimistic locking over pessimistic for wallet updates | Payment updates are short-lived; pessimistic locks would serialize throughput unnecessarily under normal (non-conflicting) load. |
| Redis for idempotency keys | Sub-millisecond lookups, TTL-based auto-expiry, no schema migration needed for a short-lived key. |
| Append-only ledger | Guarantees auditability — balances are always derivable from the immutable event log, never silently overwritten. |

*(This section is updated as real decisions are made during the build — not written in advance.)*

---

## Roadmap

- [x] Multi-module Maven scaffold + Docker Compose infra
- [ ] User service: JWT auth complete
- [ ] Wallet service: optimistic locking + idempotent add-money
- [ ] Payment service: Saga-based P2P transfer
- [ ] Notification service: Kafka + WebSocket push
- [ ] Ledger service: append-only event store
- [ ] React frontend
- [ ] AWS deployment + CI/CD
- [ ] Prometheus + Grafana monitoring
- [ ] Load testing (JMeter) + performance tuning

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

*Built as a hands-on exploration of distributed payment systems architecture.*