# Service Availability & Consistency Guide

## Quick Reference

| Service | Availability | Consistency | Latency (p99) | Why This Model |
|---|---|---|---|---|
| API Gateway | 99.99% | Stateless | < 50ms | Entry point, must be always available |
| Auth Service | 99.99% | Eventual | < 100ms | Fast auth > perfect consistency |
| Theatre Service | 99.95% | Eventual | < 200ms | Search must work > perfect accuracy |
| **Booking Service** | 99.99% | **Strong** | < 500ms | **No double booking allowed** |
| Payment Service | 99.95% | Eventual | < 1000ms | Retry webhooks > immediate consistency |
| Notification Service | 99.9% | Eventual | N/A | Best effort, non-blocking |

---

## Detailed Service Analysis

### 1. API Gateway
**Availability**: 99.99% (52 minutes downtime/year)
**Consistency**: N/A (Stateless proxy)

**Implementation**:
- Multi-AZ deployment across 3 availability zones
- Application Load Balancer with health checks
- Auto-scaling: 2-10 instances based on traffic
- Circuit breaker for backend service failures

**Why High Availability**:
- Single entry point for all client requests
- Failure blocks all traffic to platform
- Must handle 10,000 RPS at peak

---

### 2. Auth Service
**Availability**: 99.99%
**Consistency**: **Eventual Consistency**

**Why Eventual**:
- JWT tokens cached in Redis (15-minute TTL)
- Token validation can use stale cache briefly
- User profile changes propagate within seconds

**Implementation**:
```java
@Cacheable(value = "tokens", key = "#token", ttl = 900)
public boolean validateToken(String token) {
    // Check blacklist first (strong consistency for revoked tokens)
    if (redisTemplate.hasKey("token:blacklist:" + token)) {
        return false; // Revoked tokens fail immediately
    }
    
    // Validate signature (can use cached result)
    return jwtService.validate(token);
}
```

**Trade-offs**:
- ✅ Fast authentication: < 50ms from cache
- ✅ Reduced database load: 80%+ cache hit ratio
- ⚠️ Revoked token might work for up to 15 minutes
- ✅ Mitigation: Blacklist in Redis provides strong consistency for revocations

**Acceptable Because**:
- Token revocation is rare (logout, security breach)
- 15-minute window is acceptable for most use cases
- Critical revocations use blacklist (immediate effect)

---

### 3. Theatre Service
**Availability**: 99.95%
**Consistency**: **Eventual Consistency**

**Why Eventual**:
- Search/browse operations are read-heavy (90% reads)
- Show availability cached for 60 seconds
- Theatre/show updates propagate within 1 minute

**Implementation**:
```java
// Search with cache
@Cacheable(value = "shows", key = "#city + #movie + #date", ttl = 60)
public List<ShowDTO> searchShows(String city, String movie, LocalDate date) {
    return showRepository.findByCityAndMovieAndDate(city, movie, date);
}

// Cache invalidation on update
@CacheEvict(value = "shows", allEntries = true)
public void updateShow(Show show) {
    showRepository.save(show);
    // Next request fetches fresh data
}
```

**Trade-offs**:
- ✅ Fast search: < 200ms with cache
- ✅ Reduced database load: Read replicas + cache
- ⚠️ Might show slightly outdated show timings
- ⚠️ Seat availability might be stale by 60 seconds

**Acceptable Because**:
- Users browse before booking (not time-critical)
- Actual seat availability checked during booking (strong consistency)
- 60-second staleness doesn't impact user experience

---

### 4. Booking Service ⚠️ CRITICAL
**Availability**: 99.99%
**Consistency**: **STRONG CONSISTENCY** (Most Important!)

**Why Strong**:
- **No double booking allowed** - Business critical
- Revenue loss if same seat sold twice
- Customer trust and legal implications

**Implementation**:

**Phase 1: Seat Locking (Redis Distributed Locks)**
```java
@Service
public class SeatLockService {
    private final RedissonClient redissonClient;
    
    public boolean acquireLock(UUID showId, String seatNumber, UUID userId) {
        String lockKey = "seat:lock:" + showId + ":" + seatNumber;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // Atomic operation - only ONE thread/instance can acquire
            boolean acquired = lock.tryLock(
                3,      // Wait up to 3 seconds
                600,    // Hold lock for 10 minutes (TTL)
                TimeUnit.SECONDS
            );
            
            if (acquired) {
                // Store userId atomically
                redissonClient.getBucket(lockKey + ":user")
                    .set(userId.toString(), 600, TimeUnit.SECONDS);
                return true;
            }
            
            return false; // Lock already held by another user
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
```

**Phase 2: Booking Confirmation (Database Transaction)**
```java
@Transactional(isolation = Isolation.SERIALIZABLE)
public Booking confirmBooking(UUID holdId, UUID paymentId) {
    // 1. Validate hold exists and not expired
    Hold hold = holdRepository.findById(holdId)
        .orElseThrow(() -> new HoldExpiredException());
    
    if (hold.getExpiresAt().isBefore(Instant.now())) {
        throw new HoldExpiredException();
    }
    
    // 2. Verify payment successful
    Payment payment = paymentService.getPayment(paymentId);
    if (payment.getStatus() != PaymentStatus.SUCCESS) {
        throw new PaymentNotSuccessfulException();
    }
    
    // 3. Create booking (atomic)
    Booking booking = new Booking();
    booking.setUserId(hold.getUserId());
    booking.setShowId(hold.getShowId());
    booking.setStatus(BookingStatus.CONFIRMED);
    booking.setSeats(hold.getSeats());
    
    booking = bookingRepository.save(booking);
    
    // 4. Release locks (after transaction commits)
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                seatLockService.releaseLocks(hold.getShowId(), hold.getSeats());
            }
        }
    );
    
    return booking;
}
```

**Consistency Guarantees**:
- ✅ Redis distributed locks prevent concurrent seat booking
- ✅ Database SERIALIZABLE isolation prevents race conditions
- ✅ At most ONE confirmed booking per seat per show
- ✅ Lock TTL (10 minutes) prevents deadlocks

**Trade-offs**:
- ✅ Absolute consistency: No double booking
- ⚠️ Higher latency: < 500ms (vs < 200ms for eventual)
- ⚠️ Lower throughput: ~5,000 bookings/min (still sufficient)

**Why This Is Critical**:
- Double booking = Revenue loss + Customer dissatisfaction
- Legal implications (contract breach)
- Platform reputation damage

---

### 5. Payment Service
**Availability**: 99.95%
**Consistency**: **Eventual Consistency**

**Why Eventual**:
- Payment webhook processing is asynchronous
- Retries on failure (idempotent)
- Booking confirmation happens after payment event

**Implementation**:
```java
// Webhook processing (idempotent)
@Transactional
public void processWebhook(PaymentWebhookEvent event) {
    Payment payment = paymentRepository.findById(event.getPaymentId())
        .orElseThrow(() -> new PaymentNotFoundException());
    
    // Idempotency check - safe to receive duplicate webhooks
    if (payment.getStatus() != PaymentStatus.PENDING) {
        log.info("Payment already processed: {}", event.getPaymentId());
        return;
    }
    
    // Update payment status
    payment.setStatus(event.getStatus());
    payment.setGatewayTransactionId(event.getGatewayTransactionId());
    payment.setUpdatedAt(Instant.now());
    paymentRepository.save(payment);
    
    // Publish event to Kafka (eventual consistency)
    if (event.getStatus() == PaymentStatus.SUCCESS) {
        PaymentSuccessEvent successEvent = new PaymentSuccessEvent(
            payment.getId(),
            payment.getBookingId(),
            payment.getAmount(),
            event.getGatewayTransactionId()
        );
        kafkaTemplate.send("payment.success", successEvent);
    } else {
        PaymentFailedEvent failedEvent = new PaymentFailedEvent(
            payment.getId(),
            payment.getBookingId(),
            event.getReason()
        );
        kafkaTemplate.send("payment.failed", failedEvent);
    }
}
```

**Kafka Event Consumption (Booking Service)**:
```java
@KafkaListener(topics = "payment.success", groupId = "booking-service")
public void handlePaymentSuccess(PaymentSuccessEvent event) {
    try {
        // Eventually consistent - might take 1-5 seconds
        bookingService.confirmBooking(event.getBookingId(), event.getPaymentId());
        log.info("Booking confirmed: {}", event.getBookingId());
    } catch (Exception e) {
        log.error("Failed to confirm booking: {}", event.getBookingId(), e);
        // Kafka will retry based on configuration
        throw e;
    }
}
```

**Trade-offs**:
- ✅ Resilient to payment gateway failures (retries)
- ✅ Idempotent webhook processing (safe for duplicates)
- ⚠️ Booking confirmation delayed by 1-5 seconds
- ✅ Guaranteed delivery via Kafka

**Acceptable Because**:
- 1-5 second delay is imperceptible to users
- Payment gateways can be unreliable (network issues)
- Retries ensure eventual consistency

---

### 6. Notification Service
**Availability**: 99.9%
**Consistency**: **Eventual Consistency (Best Effort)**

**Why Eventual**:
- Notifications are non-critical
- Can be delayed or retried
- Failures don't block booking flow

**Implementation**:
```java
@KafkaListener(topics = "booking.confirmed", groupId = "notification-service")
public void handleBookingConfirmed(BookingConfirmedEvent event) {
    try {
        NotificationMessage message = buildConfirmationMessage(event);
        notificationSender.send(message);
        log.info("Notification sent: {}", event.getBookingId());
    } catch (Exception e) {
        log.error("Notification failed, will retry: {}", event.getBookingId(), e);
        // Kafka will retry based on configuration
        throw e; // Trigger retry
    }
}

private NotificationMessage buildConfirmationMessage(BookingConfirmedEvent event) {
    return NotificationMessage.builder()
        .recipient(event.getUserEmail())
        .subject("Booking Confirmed")
        .content("Your booking " + event.getBookingId() + " is confirmed!")
        .type(NotificationType.EMAIL)
        .build();
}
```

**Trade-offs**:
- ✅ Doesn't block critical operations
- ✅ Automatic retries on failure
- ⚠️ Notification might arrive late (acceptable)
- ⚠️ Possible duplicate notifications (idempotency needed)

**Acceptable Because**:
- Notifications are informational, not transactional
- Users can check booking status in app
- Late notification better than blocking booking

---

## CAP Theorem Analysis

### CP (Consistency + Partition Tolerance)
**Booking Service** - Chose consistency over availability

**Scenario**: Network partition between booking service instances
```
Instance A: User tries to book seat A1
Instance B: User tries to book seat A1

With CP:
- Redis distributed lock ensures only ONE succeeds
- Other user gets "Seat unavailable" error
- Result: Consistent (no double booking)

Without CP (if we chose AP):
- Both instances might succeed
- Result: Double booking (unacceptable)
```

### AP (Availability + Partition Tolerance)
**All Other Services** - Chose availability over consistency

**Scenario**: Network partition between service and database
```
Theatre Service: User searches for shows

With AP:
- Return cached results (might be stale)
- User sees slightly outdated show times
- Result: Available (search works)

Without AP (if we chose CP):
- Return error if can't reach database
- User can't search at all
- Result: Unavailable (bad UX)
```

---

## Consistency Guarantees in Action

### Strong Consistency Example (Booking)
```
Time: 10:00:00.000
User A: Click "Book seat A1"
User B: Click "Book seat A1"

Time: 10:00:00.100
User A: Redis lock acquired for seat A1 ✅
User B: Redis lock attempt fails ❌

Time: 10:00:00.200
User A: Payment initiated
User B: Sees "Seat unavailable" error

Time: 10:00:05.000
User A: Payment successful, booking confirmed
User B: Can try booking a different seat

Result: Only ONE booking for seat A1 (guaranteed)
```

### Eventual Consistency Example (Theatre Search)
```
Time: 10:00:00
Admin: Update show time from 2:00 PM to 3:00 PM
Database: Updated immediately
Cache: Still shows 2:00 PM (60-second TTL)

Time: 10:00:30
User: Searches for shows
Result: Sees 2:00 PM (stale data from cache)

Time: 10:01:00
Cache: Expires
Next User: Searches for shows
Result: Sees 3:00 PM (fresh data from database)

Result: Eventually consistent (60-second delay acceptable)
```

### Eventual Consistency Example (Payment → Booking)
```
Time: 10:00:00.000
Payment Gateway: Webhook sent (payment successful)

Time: 10:00:00.500
Payment Service: Webhook received, payment marked SUCCESS

Time: 10:00:01.000
Payment Service: Kafka event published to "payment.success"

Time: 10:00:02.000
Booking Service: Kafka event consumed

Time: 10:00:03.000
Booking Service: Booking marked CONFIRMED

Time: 10:00:04.000
Notification Service: Confirmation email sent

Result: 3-5 second delay, but guaranteed delivery
```

---

## Summary

### When We Use Strong Consistency
- ✅ Booking Service (seat locking, booking confirmation)
- **Why**: Business critical - no double booking allowed
- **Trade-off**: Higher latency (< 500ms) but absolute correctness

### When We Use Eventual Consistency
- ✅ Auth Service (token cache)
- ✅ Theatre Service (search/browse)
- ✅ Payment Service (webhook processing)
- ✅ Notification Service (best effort)
- **Why**: Better performance and availability
- **Trade-off**: Slightly stale data (acceptable for these use cases)

### Key Insight
We use **strong consistency only where business requires it** (no double booking), 
and **eventual consistency everywhere else** for better performance and availability.

This is the optimal trade-off for a booking platform! 🎯
