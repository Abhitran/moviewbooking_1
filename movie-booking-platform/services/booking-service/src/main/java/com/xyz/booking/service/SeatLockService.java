package com.xyz.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Manages distributed seat locks using Redisson.
 * Key pattern: seat:lock:{showId}:{seatNumber} = {userId}
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockService {

    private final RedissonClient redissonClient;

    @Value("${booking.seat-hold.ttl-seconds:600}")
    private long ttlSeconds;

    private String lockKey(UUID showId, String seatNumber) {
        return "seat:lock:" + showId + ":" + seatNumber;
    }

    /**
     * Attempts to acquire a lock for a single seat.
     * Returns true if lock was acquired (seat was free), false if already held.
     */
    public boolean tryAcquireLock(UUID showId, String seatNumber, UUID userId) {
        String key = lockKey(showId, seatNumber);
        RBucket<String> bucket = redissonClient.getBucket(key);
        boolean set = bucket.setIfAbsent(userId.toString(), Duration.ofSeconds(ttlSeconds));
        if (set) {
            log.debug("Lock acquired: {} by user {}", key, userId);
        }
        return set;
    }

    /**
     * Acquires locks for all seats atomically. Rolls back on any failure.
     */
    public boolean tryAcquireAll(UUID showId, List<String> seatNumbers, UUID userId) {
        List<String> acquired = new java.util.ArrayList<>();
        for (String seat : seatNumbers) {
            if (tryAcquireLock(showId, seat, userId)) {
                acquired.add(seat);
            } else {
                // Rollback already-acquired locks
                acquired.forEach(s -> releaseLock(showId, s, userId));
                log.debug("Failed to acquire lock for seat {}, rolled back {} locks", seat, acquired.size());
                return false;
            }
        }
        return true;
    }

    public void releaseLock(UUID showId, String seatNumber, UUID userId) {
        String key = lockKey(showId, seatNumber);
        RBucket<String> bucket = redissonClient.getBucket(key);
        String holder = bucket.get();
        if (userId.toString().equals(holder)) {
            bucket.delete();
            log.debug("Lock released: {}", key);
        }
    }

    public void releaseAll(UUID showId, List<String> seatNumbers, UUID userId) {
        seatNumbers.forEach(s -> releaseLock(showId, s, userId));
    }

    public boolean isLocked(UUID showId, String seatNumber) {
        return redissonClient.getBucket(lockKey(showId, seatNumber)).isExists();
    }

    public List<String> getHeldSeats(UUID showId, List<String> seatNumbers) {
        return seatNumbers.stream()
            .filter(s -> isLocked(showId, s))
            .toList();
    }
}
