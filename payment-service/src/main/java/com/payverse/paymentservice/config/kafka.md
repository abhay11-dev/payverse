# Kafka — Complete HLD + Interview Guide

## 1. Kafka in One Picture

```text
                         PRODUCERS
                            |
                  +---------+---------+
                  |                   |
            Payment Service      Order Service
                  |                   |
                  +---------+---------+
                            |
                            v
                  +-------------------+
                  |   KAFKA CLUSTER   |
                  |                   |
                  | Broker 1           |
                  | Broker 2           |
                  | Broker 3           |
                  +-------------------+
                            |
                     Topic: payment-events
                            |
              +-------------+-------------+
              |             |             |
              v             v             v
           Partition 0   Partition 1   Partition 2
              |             |             |
          offsets       offsets       offsets
          0,1,2...      0,1,2...      0,1,2...
              |             |             |
              +-------------+-------------+
                            |
                    CONSUMER GROUP
                            |
              +-------------+-------------+
              |             |             |
              v             v             v
           Consumer 1    Consumer 2    Consumer 3
              |             |             |
              +-------------+-------------+
                            |
                         Service
```

---

# 2. Kafka Hierarchy

The easiest way to remember Kafka:

```text
Kafka Cluster
      |
      +---- Brokers
              |
              +---- Topics
                      |
                      +---- Partitions
                              |
                              +---- Records
                                      |
                                      +---- Offsets
```

Example:

```text
Kafka Cluster
│
├── Broker 1
├── Broker 2
└── Broker 3

Topic: payment-events
│
├── Partition 0
│     ├── Offset 0
│     ├── Offset 1
│     └── Offset 2
│
├── Partition 1
│     ├── Offset 0
│     ├── Offset 1
│     └── Offset 2
│
└── Partition 2
      ├── Offset 0
      ├── Offset 1
      └── Offset 2
```

---

# 3. Producer

A producer publishes a record/event to Kafka.

Conceptually:

```text
Producer
   |
   | key + value
   v
Kafka Topic
```

Example:

```text
Key   = PAY123
Value = PaymentCompleted
Topic = payment-events
```

A Kafka record can contain:

```text
Key
Value
Timestamp
Headers
Topic
Partition
Offset
```

The producer normally specifies the **topic**, and Kafka's partitioner determines the partition unless the producer explicitly provides a partition.

---

# 4. How Kafka Chooses a Partition

Suppose:

```text
Topic = payment-events

P0
P1
P2
```

### Case 1 — Explicit partition

Producer can explicitly send to:

```text
payment-events -> Partition 1
```

Then Kafka uses Partition 1.

---

### Case 2 — Key is provided

Suppose:

```text
Key = PAY123
```

Kafka's partitioning logic uses the key to consistently select a partition.

Conceptually:

```text
hash(PAY123) % numberOfPartitions
```

For example:

```text
hash(PAY123) % 3 = 1

PAY123 -> Partition 1
```

This is very useful for payments.

Suppose:

```text
PAY123 -> PaymentCreated
PAY123 -> PaymentProcessing
PAY123 -> PaymentCompleted
```

If they use the same partition key and remain on the same partition, their ordering is preserved **within that partition**.

---

### Case 3 — No key

If no key is provided, the producer's partitioner distributes records across available partitions according to its partitioning behavior.

Do **not** memorize:

> "No key always means round-robin."

That is too simplistic because partitioner behavior can vary by Kafka/client configuration and version.

The interview-safe statement is:

> With a key, Kafka can consistently route related records to the same partition; without a key, records are distributed across available partitions for load balancing.

---

# 5. Why Do We Need Partitions?

Partitions provide:

### 1. Scalability

A topic can be distributed across multiple brokers.

### 2. Parallelism

Different consumers can process different partitions simultaneously.

```text
P0 -> Consumer 1
P1 -> Consumer 2
P2 -> Consumer 3
```

### 3. Ordering

Kafka guarantees ordering **within a partition**.

Kafka does NOT guarantee global ordering across all partitions.

Example:

```text
Partition 0:

0 -> A
1 -> B
2 -> C

Partition 1:

0 -> X
1 -> Y
2 -> Z
```

There is no global ordering such as:

```text
A -> X -> B -> Y -> C -> Z
```

---

# 6. Offset

An offset is the position of a record inside a partition.

Example:

```text
Partition 0

Offset 0 -> Payment A
Offset 1 -> Payment B
Offset 2 -> Payment C
Offset 3 -> Payment D
```

Important:

> Offset belongs to a partition.

Therefore:

```text
Partition 0 -> offset 100
Partition 1 -> offset 50
Partition 2 -> offset 700
```

All are valid simultaneously.

There is no single global offset for the entire topic.

---

# 7. Broker

A broker is simply a **Kafka server**.

A Kafka cluster can have multiple brokers:

```text
Kafka Cluster

Broker 1
Broker 2
Broker 3
```

Partitions are distributed across these brokers.

Example:

```text
Broker 1
  └── payment-events-P0

Broker 2
  └── payment-events-P1

Broker 3
  └── payment-events-P2
```

With replication, brokers may also contain replicas of partitions.

---

# 8. Kafka Cluster

A Kafka cluster is a collection of Kafka brokers working together.

```text
                 Kafka Cluster
        +-----------------------------+
        |                             |
        | Broker 1   Broker 2   Broker 3
        |    |          |          |
        |    +----------+----------+
        |       Distributed Data
        +-----------------------------+
```

The cluster provides:

* Horizontal scalability
* Fault tolerance
* Partition distribution
* Replication
* High availability

---

# 9. Partition Replication

Suppose we have:

```text
Replication Factor = 3
```

For Partition 0:

```text
Broker 1 -> Leader
Broker 2 -> Follower
Broker 3 -> Follower
```

Conceptually:

```text
                 Partition 0

Producer
   |
   v
Broker 1
 LEADER
   |
   +----------> Broker 2
   |             FOLLOWER
   |
   +----------> Broker 3
                 FOLLOWER
```

The followers replicate the leader's partition log.

If the leader fails:

```text
Before:

Broker 1 -> Leader
Broker 2 -> Follower
Broker 3 -> Follower
```

Kafka can elect an eligible replica:

```text
After:

Broker 1 -> DOWN

Broker 2 -> NEW LEADER
Broker 3 -> FOLLOWER
```

Therefore:

> A partition has one leader at a time, while its replicas/followers maintain copies of the partition data.

---

# 10. Producer → Leader

A simplified write path:

```text
Producer
    |
    v
Partition Leader
    |
    +----> Follower
    |
    +----> Follower
```

The leader handles normal writes for that partition, while replicas follow the leader's log.

This gives Kafka fault tolerance.

---

# 11. Consumer

A consumer reads records from Kafka.

Kafka follows a **pull-based model**.

It is better to visualize it as:

```text
Consumer
    |
    | Fetch request
    v
Kafka Broker
    |
    | Records
    v
Consumer
```

The consumer controls when and how much data it fetches.

Therefore:

> Kafka is pull-based from the consumer's perspective.

---

# 12. Consumer Group

A consumer group is a logical group of consumers working together.

Example:

```text
Topic: payment-events

P0
P1
P2

Consumer Group: payment-processing

C1 -> P0
C2 -> P1
C3 -> P2
```

Within one consumer group:

> A partition is assigned to at most one consumer at a time.

This gives parallel processing without having multiple consumers in the same group independently processing the same partition.

---

# 13. Consumer Group Scaling

Suppose:

```text
3 Partitions

P0
P1
P2
```

We have:

```text
C1 -> P0
C2 -> P1
C3 -> P2
```

Now add C4:

```text
C1 -> P0
C2 -> P1
C3 -> P2
C4 -> IDLE
```

Why?

Because there are only 3 partitions.

Therefore:

> The number of partitions limits the maximum parallelism of a consumer group for that topic.

This is a very important interview point.

---

# 14. Multiple Consumer Groups

Different consumer groups can independently consume the same topic.

Example:

```text
                    payment-events
                          |
             +------------+------------+
             |            |            |
             v            v            v

       Payment Group  Notification   Analytics
                      Group          Group

       C1 C2 C3       N1 N2         A1 A2
```

The same event can therefore be processed by multiple independent applications.

For example:

```text
PaymentCompleted
       |
       +----> Ledger Service
       |
       +----> Notification Service
       |
       +----> Analytics Service
```

Each consumer group maintains its own progress.

---

# 15. Consumer Offset

Suppose:

```text
Partition 0

0 -> A
1 -> B
2 -> C
3 -> D
```

Consumer processes:

```text
A
B
```

and commits its progress.

The consumer group's committed position represents its recorded progress.

Important correction:

> A committed offset does not inherently mean "this message was successfully processed." The application/framework should commit only according to the desired processing semantics.

A common safe pattern is:

```text
Fetch
  |
Process
  |
Success
  |
Commit offset
```

---

# 16. Consumer Failure

Suppose:

```text
P0 -> C1
P1 -> C2
P2 -> C3
```

Now C1 fails.

Kafka detects the consumer failure and triggers a rebalance.

Before:

```text
C1 -> P0
C2 -> P1
C3 -> P2
```

After:

```text
C1 -> FAILED

C2 -> P0
C3 -> P1
```

The exact assignment depends on the consumer group's assignment strategy and current membership.

The key concept is:

> Kafka rebalances partitions among the remaining consumers in the consumer group.

The new consumer resumes according to the group's committed offset for that partition.

---

# 17. What Happens to an Uncommitted Message?

Suppose:

```text
P0

Offset 100 -> A
Offset 101 -> B
Offset 102 -> C
```

Consumer processes:

```text
100 -> success
101 -> success
102 -> processing...
```

Then consumer crashes before committing the progress for 102.

After rebalance, another consumer can read from the last committed position.

Therefore:

```text
102
 |
 +--> may be processed again
```

This is why production consumers should generally be designed to handle duplicate processing.

For payment systems, **idempotency is extremely important**.

---

# 18. Retry + DLQ

Your retry idea is correct conceptually, but one important correction:

> Kafka itself does not automatically say "retry every message 3 times and then move it to DLQ."

This is normally implemented through application/framework error handling and retry topics.

Conceptually:

```text
                 payment-events
                       |
                       v
                   Consumer
                       |
                  Process Event
                       |
              +--------+--------+
              |                 |
           SUCCESS            FAILURE
              |                 |
              v                 v
        Commit Offset       Retry Mechanism
                                |
                         +------+------+
                         |             |
                      Retry 1       Retry 2
                         |             |
                         +------+------+
                                |
                              Retry 3
                                |
                             FAILURE
                                |
                                v
                          DLQ Topic
```

For example:

```text
payment-events
payment-events-retry
payment-events-dlq
```

A DLQ is normally another Kafka topic.

---

# 19. Buggy Message Example

Suppose:

```text
Partition 0

100 -> Payment A
101 -> BUGGY EVENT
102 -> Payment C
```

Consumer reads:

```text
101 -> processing fails
```

If we simply retry forever:

```text
101 -> fail
101 -> fail
101 -> fail
101 -> fail
...
```

we can block progress for later records in that partition.

A production design may use:

```text
Main Topic
     |
     v
Consumer
     |
     +---- SUCCESS ---> Commit
     |
     +---- FAILURE ---> Retry Topic
                            |
                            v
                         Retry N
                            |
                            v
                           DLQ
```

After the retry/DLQ handling is completed, the source-topic processing position is advanced according to the chosen error-handling strategy.

The important point is:

> Do not simply "skip" a failed message without a deliberate retry/DLQ strategy.

---

# 20. ZooKeeper — Important Correction

Your original model says:

> ZooKeeper manages how brokers interact with each other.

This was true for **older Kafka architecture**, but it is not the modern model.

### Older Kafka

```text
Kafka Brokers
      |
      v
ZooKeeper
```

ZooKeeper was used for Kafka cluster metadata and coordination.

### Modern Kafka

Kafka uses **KRaft**.

```text
             Kafka Cluster
                  |
           +------+------+
           |             |
        Brokers      Controller
                      Quorum
                       |
                      KRaft
```

So the interview-safe answer is:

> Older Kafka versions used ZooKeeper for cluster coordination and metadata management. Modern Kafka uses KRaft, where Kafka's own controller quorum manages cluster metadata and leadership without ZooKeeper.

---

# 21. Consumer Offsets Are NOT Stored in ZooKeeper

This is another important correction.

Do NOT say:

```text
ZooKeeper stores:
Consumer
Group
Topic
Partition
Committed Offset
```

Modern Kafka stores consumer group offsets in Kafka's internal topic:

```text
__consumer_offsets
```

Conceptually:

```text
Consumer Group
      |
      v
Committed Offset
      |
      v
__consumer_offsets
```

So when a consumer fails:

```text
Consumer 1 fails
       |
       v
Rebalance
       |
       v
Consumer 2 gets partition
       |
       v
Read group's committed progress
       |
       v
Resume consumption
```

---

# 22. Complete Modern Kafka HLD

```text
                         PRODUCERS
                             |
                             v
                 +-----------------------+
                 |     KAFKA CLUSTER     |
                 |                       |
                 |  Broker 1             |
                 |  Broker 2             |
                 |  Broker 3             |
                 |                       |
                 |  KRaft Controller     |
                 +-----------+-----------+
                             |
                    Topic: payment-events
                             |
              +--------------+--------------+
              |              |              |
              v              v              v
          Partition 0    Partition 1    Partition 2
              |              |              |
              |              |              |
         Leader +       Leader +       Leader +
         Replicas       Replicas       Replicas
              |              |              |
              +--------------+--------------+
                             |
                    CONSUMER GROUP
                             |
             +---------------+---------------+
             |               |               |
             v               v               v
         Consumer 1      Consumer 2      Consumer 3
             |               |               |
             +---------------+---------------+
                             |
                             v
                    Process Event
                             |
                    +--------+--------+
                    |                 |
                 SUCCESS           FAILURE
                    |                 |
                    v                 v
              Commit Offset        Retry
                                      |
                                      v
                                     DLQ

Consumer Group Offset
          |
          v
 __consumer_offsets
```

---

# 23. PayVerse Example

For your PayVerse payment service:

```text
Payment Service
      |
      | Publish PaymentCompleted
      | key = paymentId
      v
Kafka
      |
      v
Topic: payment-events
      |
      +---------+---------+
      |         |         |
      v         v         v
     P0        P1        P2
      |         |         |
      +---------+---------+
                |
        +-------+-------+
        |               |
        v               v
 Payment Group     Notification Group
        |               |
    C1 C2 C3          N1 N2
        |               |
        v               v
   Ledger Service   Notification
```

Suppose:

```text
paymentId = PAY123
```

Events:

```text
PAY123 -> PaymentCreated
PAY123 -> PaymentProcessing
PAY123 -> PaymentCompleted
```

Using:

```text
key = PAY123
```

helps route related events consistently to the same partition.

That allows ordering such as:

```text
PaymentCreated
      ↓
PaymentProcessing
      ↓
PaymentCompleted
```

within that partition.

---

# 24. Why Kafka Is Good for PayVerse

Without Kafka:

```text
Payment Service
      |
      +----> Ledger
      |
      +----> Notification
      |
      +----> Analytics
```

Payment Service becomes tightly coupled to all downstream services.

With Kafka:

```text
                  Kafka
                    |
          +---------+---------+
          |         |         |
          v         v         v
       Ledger  Notification Analytics
```

Payment Service publishes an event and does not need to synchronously call every downstream consumer.

Benefits:

* Loose coupling
* Asynchronous processing
* Horizontal scalability
* Fault tolerance
* Event replay
* Independent consumer groups
* High throughput

---

# 25. Horizontal Scaling

Kafka supports horizontal scaling by distributing partitions across brokers.

Example:

```text
Before:

Broker 1
   |
All partitions
```

After:

```text
Broker 1       Broker 2       Broker 3
   |               |              |
   P0              P1             P2
```

Consumer scaling:

```text
1 Consumer:

C1 -> P0
     P1
     P2
```

versus:

```text
3 Consumers:

C1 -> P0
C2 -> P1
C3 -> P2
```

Therefore:

> Partitions provide the unit of parallelism for consumers.

---

# 26. Most Important Kafka Relationships

## Topic → Partitions

```text
Topic
 |
 +-- P0
 +-- P1
 +-- P2
```

---

## Partition → Offsets

```text
P0

0
1
2
3
4
...
```

---

## Partition → Leader + Replicas

```text
P0

Broker 1 -> Leader
Broker 2 -> Replica
Broker 3 -> Replica
```

---

## Consumer Group → Consumers

```text
Payment Group
 |
 +-- C1
 +-- C2
 +-- C3
```

---

## Consumer Group → Partitions

```text
P0 -> C1
P1 -> C2
P2 -> C3
```

---

## Consumer Group → Committed Offset

```text
Consumer Group
      |
      v
__consumer_offsets
```

---

# 27. Kafka Failure Flow

### Broker failure

```text
Leader Broker fails
        |
        v
Eligible replica becomes leader
        |
        v
Producer / Consumer reconnects
        |
        v
Continue processing
```

### Consumer failure

```text
Consumer fails
      |
      v
Consumer group rebalance
      |
      v
Another consumer gets partition
      |
      v
Resume from committed progress
```

### Message processing failure

```text
Processing fails
      |
      v
Retry
      |
      +---- Success ---> Commit
      |
      +---- Repeated Failure
                    |
                    v
                   DLQ
```

---

# 28. Kafka vs Queue Mental Model

Do not think:

```text
Kafka = simple queue
```

Think:

```text
Kafka = distributed append-only log
```

A message is appended to a partition:

```text
Partition

0 -> Event A
1 -> Event B
2 -> Event C
3 -> Event D
```

Consumers track their position using offsets.

Different consumer groups can independently read the same events:

```text
                Topic
                  |
          +-------+-------+
          |       |       |
          v       v       v
        Group A Group B Group C
```

This is one of Kafka's major strengths.

---

# 29. Interview Traps to Avoid

### ❌ "ZooKeeper stores consumer offsets."

### ✅ Correct

> Consumer group offsets are stored in Kafka's internal `__consumer_offsets` topic.

---

### ❌ "Kafka always uses round robin when there is no key."

### ✅ Correct

> Partition selection is handled by the producer partitioner. Keys can provide consistent partitioning; without a key, records are distributed according to the producer's partitioning behavior.

---

### ❌ "Offset means message was successfully processed."

### ✅ Correct

> Offset identifies a record's position. A committed offset represents consumer-group progress, and the application controls when that progress is committed.

---

### ❌ "Kafka guarantees global ordering."

### ✅ Correct

> Kafka guarantees ordering within a partition.

---

### ❌ "DLQ is automatically provided by Kafka."

### ✅ Correct

> DLQ is generally implemented as a separate Kafka topic through application/framework error handling.

---

### ❌ "More consumers always improve performance."

### ✅ Correct

> Consumer parallelism within a group is limited by the number of partitions.

---

### ❌ "ZooKeeper is required for modern Kafka."

### ✅ Correct

> Modern Kafka uses KRaft and does not require ZooKeeper.

---

# 30. One Master Interview Answer

> **Kafka is a distributed event-streaming platform based on an append-only log. Producers publish records to topics, and each topic is divided into partitions to provide scalability and parallelism. Each partition maintains an ordered sequence of records identified by offsets, and ordering is guaranteed within a partition. Partitions are distributed across Kafka brokers and replicated using a leader-replica model for fault tolerance. Consumers pull records from Kafka and are organized into consumer groups, where partitions are distributed among consumers so multiple consumers can process events in parallel. Each consumer group maintains its own committed offsets, which Kafka stores in the internal `__consumer_offsets` topic. If a consumer fails, Kafka rebalances the group and another consumer can take over its partitions and resume from the group's committed progress. Producers can use a key such as paymentId to consistently route related events to the same partition when ordering is required. For processing failures, applications can implement retries and DLQ topics. Older Kafka used ZooKeeper, while modern Kafka uses KRaft for cluster metadata and controller management. Kafka follows a pull-based consumer model, making it highly scalable, fault-tolerant, and suitable for event-driven microservices.**

---

# 31. 30-Second Interview Version

> **Kafka is a distributed event-streaming platform where producers publish records to topics. Topics are divided into partitions, which provide scalability, parallelism, and ordering within each partition. Partitions are distributed and replicated across brokers in a Kafka cluster using a leader-replica model. Consumers read records using a pull model and are organized into consumer groups, where partitions are distributed among consumers. Each group tracks committed offsets in Kafka's internal `__consumer_offsets` topic, so after a consumer failure Kafka can rebalance the partitions and another consumer can continue from the committed progress. Keys can be used to keep related events on the same partition. Retry and DLQ handling are generally implemented at the application/framework level. Modern Kafka uses KRaft instead of ZooKeeper.**

---

# 32. Final Memory Trick

Remember:

```text
PRODUCER
    ↓
TOPIC
    ↓
PARTITIONS
    ↓
OFFSETS
    ↓
BROKERS
    ↓
CONSUMER GROUP
    ↓
CONSUMERS
    ↓
COMMITTED OFFSET
```

For scaling:

```text
PARTITIONS
     ↓
PARALLELISM
```

For availability:

```text
LEADER
  ↓
REPLICAS
  ↓
FAILURE → NEW LEADER
```

For consumer failure:

```text
CONSUMER FAILURE
       ↓
   REBALANCE
       ↓
ANOTHER CONSUMER
       ↓
COMMITTED OFFSET
```

For message failure:

```text
FAILURE
   ↓
RETRY
   ↓
RETRY LIMIT
   ↓
DLQ
```

For modern Kafka:

```text
OLD  → ZooKeeper
NEW  → KRaft
```

## The single sentence to remember

> **Kafka = Producers publish events to partitioned topics, brokers store and replicate those partitions, consumer groups process partitions in parallel, and committed offsets track each group's progress.**
