# Redis and Kafka Configuration Guide

This document describes the Redis and Kafka infrastructure configuration across all services in the movie booking platform.

## Table of Contents
1. [Redis Configuration](#redis-configuration)
2. [Kafka Configuration](#kafka-configuration)
3. [Cache Strategies](#cache-strategies)
4. [Error Handling](#error-handling)

---

## Redis Configuration

### Overview
Redis is used for:
- **Distributed locking** (seat holds in booking-service)
- **Distributed caching** (show availability, theatre search)
- **Session management** (token validation in auth-service)

### Redisson Client Configuration

All services using Redis are configured with Redisson client for advanced features:

```yaml
Connection Pool:
  - Pool Size: 64 connections
  - Minimum Idle: 10 connections
  - Connect Timeout: 10 seconds
  - Operation Timeout: 3 seconds
  - Retry Attempts: 3
  - Retry Interval: 1.5 seconds

Lock Configuration:
  - Lock Watchdog Timeout: 30 seconds
```

### Service-Specific Redis Configuration

#### Booking Service
**Purpose**: Seat locking and booking hold management

**Caches**:
- `showAvailability`: TTL 60s, Max Idle 30s
- `bookingHolds`: TTL 600s (10 min), Max Idle 300s (5 min)

**Configuration File**: `booking-service/src/main/java/com/xyz/booking/config/RedisConfig.java`

#### Theatre Service
**Purpose**: Show availability and theatre metadata caching

**Dual Cache Strategy**:
1. **Redis (Distributed)**:
   - `showAvailability`: TTL 60s, Max Idle 30s
   - `theatreSearch`: TTL 60s, Max Idle 30s

2. **Caffeine (Local)**:
   - `theatreMetadata`: TTL 10s, Max Size 1000 entries

**Configuration File**: `theatre-service/src/main/java/com/xyz/theatre/config/RedisConfig.java`

#### Auth Service
**Purpose**: Token validation and session caching

**Caches**:
- `tokens`: TTL 900s (15 min), Max Idle 450s (7.5 min)
- `sessions`: TTL 900s (15 min), Max Idle 450s (7.5 min)

**Configuration File**: `auth-service/src/main/java/com/xyz/auth/config/RedisConfig.java`

---

## Kafka Configuration

### Overview
Kafka is used for asynchronous event-driven communication between services.

### Topics Configuration

| Topic | Partitions | Replicas | Min In-Sync | Retention | Producer | Consumer |
|-------|-----------|----------|-------------|-----------|----------|----------|
| `booking.confirmed` | 6 | 3 | 2 | 7 days | booking-service | notification-service |
| `booking.cancelled` | 3 | 3 | 2 | 7 days | booking-service | notification-service |
| `payment.success` | 6 | 3 | 2 | 7 days | payment-service | booking-service |
| `payment.failed` | 3 | 3 | 2 | 7 days | payment-service | booking-service |

**Partition Strategy**:
- **High-volume topics** (booking.confirmed, payment.success): 6 partitions
- **Low-volume topics** (booking.cancelled, payment.failed): 3 partitions

### Producer Configuration

All producers are configured with the following properties for reliability:

```yaml
Producer Properties:
  acks: all                    # Wait for all replicas to acknowledge
  retries: 3                   # Retry up to 3 times on failure
  enable.idempotence: true     # Exactly-once semantics
  max.in.flight.requests: 5    # Max unacknowledged requests
  compression.type: snappy     # Compress messages
  linger.ms: 10                # Batch messages for 10ms
  batch.size: 32768            # 32KB batch size
```

**Configuration Files**:
- `booking-service/src/main/java/com/xyz/booking/config/KafkaProducerConfig.java`
- `payment-service/src/main/java/com/xyz/payment/config/KafkaProducerConfig.java`

### Consumer Configuration

All consumers are configured with the following properties:

```yaml
Consumer Properties:
  auto-offset-reset: earliest       # Start from earliest message on first run
  enable-auto-commit: false         # Manual commit for reliability
  max-poll-records: 100             # Process 100 records per poll
  session.timeout.ms: 30000         # 30 seconds session timeout
  heartbeat.interval.ms: 10000      # 10 seconds heartbeat interval

Listener Properties:
  ack-mode: manual                  # Manual acknowledgment
  concurrency: 3                    # 3 concurrent consumers (2 for notification)
```

**Configuration Files**:
- `booking-service/src/main/java/com/xyz/booking/config/KafkaConsumerConfig.java`
- `payment-service/src/main/java/com/xyz/payment/config/KafkaConsumerConfig.java`
- `notification-service/src/main/java/com/xyz/notification/config/KafkaConsumerConfig.java`

---

## Cache Strategies

### 1. Cache-Aside Pattern (Show Availability)

**Implementation**: Theatre Service and Booking Service

**Flow**:
1. Check Redis cache for show availability
2. If cache miss, query database
3. Store result in cache with 60s TTL
4. Return result

**Invalidation**: On booking confirmation, cache is invalidated

```java
@Cacheable(value = "showAvailability", key = "#showId")
public ShowAvailability getShowAvailability(UUID showId) {
    // Query database if cache miss
}

// Invalidate on booking confirmation
cacheService.invalidateShowAvailability(showId);
```

### 2. Local Cache (Theatre Metadata)

**Implementation**: Theatre Service (Caffeine)

**Purpose**: Frequently accessed theatre metadata (name, address, etc.)

**Configuration**:
- TTL: 10 seconds
- Max Size: 1000 entries
- Eviction: LRU (Least Recently Used)

**Benefits**:
- Ultra-low latency (< 1ms)
- Reduces Redis load
- Suitable for rarely changing data

### 3. Cache Invalidation on Booking Confirmation

**Trigger**: When a booking is confirmed

**Actions**:
1. Invalidate `showAvailability` cache for the show
2. Invalidate `bookingHolds` cache for the hold
3. Customers immediately see updated seat availability

**Implementation**: `CacheService` in booking-service and theatre-service

---

## Error Handling

### Kafka Consumer Error Handling

All consumers implement a retry-with-DLQ (Dead Letter Queue) strategy:

**Retry Logic**:
- Max Attempts: 3 (1 initial + 2 retries)
- Retry Interval: 1 second (fixed backoff)
- Total Time: ~3 seconds

**Dead Letter Queue (DLQ)**:
- Failed messages after 3 attempts are sent to DLQ topics:
  - `booking-service.dlq`
  - `payment-service.dlq`
  - `notification-service.dlq`

**Error Handler Configuration**:
```java
DefaultErrorHandler errorHandler = new DefaultErrorHandler(
    (record, exception) -> {
        // Send to DLQ
        kafkaTemplate.send("service-name.dlq", record.key(), record.value());
    },
    new FixedBackOff(1000L, 2L)  // 1s interval, 2 retries
);
```

### Redis Connection Error Handling

**Retry Configuration**:
- Retry Attempts: 3
- Retry Interval: 1.5 seconds
- Connect Timeout: 10 seconds
- Operation Timeout: 3 seconds

**Fallback Strategy**:
- On Redis failure, services fall back to database queries
- Cache misses are logged but don't block operations

---

## Environment Variables

### Redis
```bash
REDIS_HOST=localhost          # Redis host (default: localhost)
REDIS_PORT=6379               # Redis port (default: 6379)
```

### Kafka
```bash
KAFKA_BOOTSTRAP_SERVERS=localhost:9092  # Kafka bootstrap servers
```

---

## Monitoring and Metrics

### Redis Metrics
- Connection pool utilization
- Cache hit/miss ratio
- Lock acquisition success rate
- Average operation latency

### Kafka Metrics
- Producer send rate
- Consumer lag
- Message processing time
- DLQ message count

---

## Testing

### Redis Testing
- Use Testcontainers for integration tests
- Mock RedissonClient for unit tests

### Kafka Testing
- Use Embedded Kafka for integration tests
- Mock KafkaTemplate for unit tests

---

## Troubleshooting

### Redis Issues

**Problem**: High cache miss ratio
- **Solution**: Increase TTL or pre-warm cache

**Problem**: Lock acquisition failures
- **Solution**: Check lock watchdog timeout, increase if needed

### Kafka Issues

**Problem**: High consumer lag
- **Solution**: Increase consumer concurrency or partitions

**Problem**: Messages in DLQ
- **Solution**: Check error logs, fix consumer logic, replay from DLQ

---

## References

- [Redisson Documentation](https://github.com/redisson/redisson)
- [Spring Kafka Documentation](https://spring.io/projects/spring-kafka)
- [Caffeine Cache Documentation](https://github.com/ben-manes/caffeine)
