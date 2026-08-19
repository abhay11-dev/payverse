# PayVerse

> A microservices-based digital payments platform — wallet, P2P transfers, notifications, and transaction ledger — built to mirror the architecture patterns used by production UPI-scale payment systems.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Status](https://img.shields.io/badge/status-early%20development-yellow.svg)](#project-status)

---

## Project Status

**This project is in early scaffolding — Week 1 of a 12-month build.** What's real right now: a compiling multi-module Maven project and a Docker Compose environment for local infra (MySQL, Redis, Kafka). No business logic (auth, wallet, payments) exists yet — that starts next.

This README is written honestly for where the project actually is today, not where it's headed. The [Roadmap](#roadmap) section tracks real progress with checkboxes, updated as each piece actually ships.

---

## Table of Contents
- [Project Status](#project-status)
- [Overview](#overview)
- [Planned Architecture](#planned-architecture)
- [Tech Stack](#tech-stack)
- [Planned Services](#planned-services)
- [Getting Started (Current State)](#getting-started-current-state)
- [Project Structure](#project-structure)
- [Roadmap](#roadmap)
- [License](#license)

---

## Overview

PayVerse is a self-initiated project simulating a real-world digital payments platform, being built to explore the architectural, concurrency, and reliability challenges present in systems like UPI — idempotency, optimistic locking, event sourcing, the Saga pattern, and rate limiting. The goal is to demonstrate hands-on understanding of these concepts by actually building them, not just describing them.

**Where it stands after Week 1:** repo scaffolded, local infra (MySQL, Redis, Kafka) running in Docker Compose, six-module Maven skeleton compiling clean. Auth, wallet, and payment logic have not been written yet.

---

## Planned Architecture

*(This is the target design — not yet implemented. Included here so the intended direction is clear from day one.)*

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




```
                         ┌───────────────────┐
                         │    API Gateway    │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │  Payment Service  │
                         └─────────┬─────────┘
                                   │
                            debit / credit
                                   │
                                   ▼
                         ┌───────────────────┐
                         │  Wallet Service   │
                         │                   │
                         │ Wallet            │
                         │ balance           │
                         │ @Version          │
                         │      ↑            │
                         │ optimistic lock   │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │  Wallet MySQL DB  │
                         └───────────────────┘

```



```
Payment event
     │
     ▼
┌────────────────────┐
│   Kafka            │
│ payment-events     │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│   Ledger Service   │
│                    │
│ append LedgerEntry │
└─────────┬──────────┘
          │
          ▼
┌────────────────────┐
│   Ledger MySQL DB  │
└─────────┬──────────┘
          │
          │
          │ periodically read
          ▼
┌──────────────────────────┐
│   Reconciliation Job     │  
│                          │   
│ SUM(ledger entries)      │
│          ↓               │
│ Compare with             │
│ wallet.balance           │
└───────────┬──────────────┘
            │
Auto-correcting based solely on the ledger is dangerous because a mismatch doesn't tell us which side is wrong. The ledger itself could be incomplete or contain a duplicate, so blindly changing the wallet could compound the error and destroy useful evidence. In a payments system I'd alert and investigate first, then perform a controlled, auditable correction once the root cause is established.
             ▼
       ┌───────────┐
       │  Match?   │
       └─────┬─────┘
             │
       ┌─────┴─────┐
       │           │
      YES          NO
       │           │
       ▼           ▼
     Normal      Alert /
                 investigation
```
**Patterns planned (not yet built):** idempotency via Redis keys, optimistic locking on wallet balance updates, Saga-based compensation on payment failure, append-only event-sourced ledger, token-bucket rate limiting at the gateway.

---

## Tech Stack

| Layer | Technology | Status |
|---|---|---|
| Language | Java 17 | ✅ in use |
| Build | Maven (multi-module) | ✅ in use |
| Framework | Spring Boot 3.x, Spring Security, Spring Data JPA, Spring Cloud Gateway | planned |
| Messaging | Apache Kafka | infra running, not yet integrated into any service |
| Database | MySQL 8 | infra running, no schema yet |
| Cache / Session | Redis 7 | infra running, not yet used |
| Frontend | React 18, TypeScript, TailwindCSS | planned |
| Containerization | Docker, Docker Compose | ✅ in use for local infra |
| CI/CD | GitHub Actions | planned |
| Cloud | AWS (EC2, RDS, ElastiCache, S3) | planned |
| Monitoring | Prometheus, Grafana | planned |
| Testing | JUnit 5, Mockito, Testcontainers | planned |

---

## Planned Services

| Module | Responsibility | Status |
|---|---|---|
| `payverse-api-gateway` | Single entry point — JWT auth, routing, rate limiting | scaffolded, empty |
| `payverse-user` | Registration, login, JWT issuance, refresh tokens | scaffolded, empty |
| `payverse-wallet` | Wallet balance, optimistic-locked updates | scaffolded, empty |
| `payverse-payment` | P2P transfers, idempotency, Saga orchestration | scaffolded, empty |
| `payverse-notification` | Kafka-consumer-driven push/email/websocket notifications | scaffolded, empty |
| `payverse-ledger` | Append-only transaction ledger, reconciliation | scaffolded, empty |

All six exist as Maven modules with valid (currently empty) `pom.xml` files and build successfully as part of the parent project. No controllers, services, or entities have been written yet.

---

## Getting Started (Current State)

This reflects what actually works today — not a future setup guide.

### Prerequisites
- Java 17+
- Maven 3.9+
- Docker & Docker Compose

### Run what exists

```bash
# clone the repo
git clone https://github.com/<your-username>/payverse.git
cd payverse

# bring up local infra (MySQL, Redis, Kafka, Zookeeper)
docker-compose up -d

# confirm all containers are healthy
docker-compose ps

# build all 6 (currently empty) modules
mvn clean install
```

At this stage there are no exposed API endpoints — the gateway and services have no logic yet. This will change as Week 2 adds the `user-service` auth flow.

---

## Project Structure

```
payverse/
├── payverse-api-gateway/     (empty scaffold)
├── payverse-user/            (empty scaffold)
├── payverse-wallet/          (empty scaffold)
├── payverse-payment/         (empty scaffold)
├── payverse-notification/    (empty scaffold)
├── payverse-ledger/          (empty scaffold)
├── docker-compose.yml        (MySQL, Redis, Kafka, Zookeeper)
├── pom.xml                   (parent POM, all 6 modules linked)
├── .gitignore
├── LICENSE
└── README.md
```

---

## Roadmap

**Week 1 (6–12 Jul 2026) — Foundation**
- [x] GitHub repo created, `.gitignore` and MIT license added
- [x] Multi-module Maven scaffold (6 modules) compiling clean
- [x] Docker Compose: MySQL running with persistent volume
- [ ] Docker Compose: Kafka + Zookeeper fully working (listener config in progress)
- [ ] Base packages added to `payverse-user` (`controller/`, `service/`, `repository/`, `dto/`, `exception/`, `config/`, `model/`)
- [ ] README polished, repo pushed in clean state

**Week 2 onward**
- [ ] User service: JWT auth (register/login/refresh) complete
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

*Built as a hands-on, week-by-week exploration of distributed payment systems architecture. This README is updated to reflect real progress, not aspirational scope.*