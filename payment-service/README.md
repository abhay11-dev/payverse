# PayVerse Payment Service

The Payment Service is responsible for coordinating wallet-to-wallet payments
between PayVerse users.

## Responsibilities

- Validate payment requests
- Coordinate sender debit and receiver credit
- Generate a unique transaction ID
- Publish payment success/failure events to Kafka
- Handle receiver-credit failure using Saga-style compensation

## Payment Flow

```text
Client
  |
  v
Payment Service
  |
  |-- 1. Debit Sender Wallet
  |
  |-- 2. Credit Receiver Wallet
  |
  |-- 3. Publish PAYMENT_SUCCESS
  |
  v
Kafka
