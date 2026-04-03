package com.xyz.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for managing cache operations and invalidation strategies.
 * Implements cache-aside pattern with TTL-based expiration.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;

    /**
     * Invalidate show availability cache on booking confirmation.
     * This ensures customers see updated seat availability immediately.
     *
     * @param showId The show ID whose availability cache should be invalidated
     */
    public void invalidateShowAvailability(UUID showId) {
        Cache cache = cacheManager.getCache("showAvailability");
        if (cache != null) {
            cache.evict(showId);
            log.info("Invalidated show availability cache for showId: {}", showId);
        }
    }

    /**
     * Invalidate booking hold cache when hold expires or is confirmed.
     *
     * @param holdId The hold ID to invalidate
     */
    public void invalidateBookingHold(UUID holdId) {
        Cache cache = cacheManager.getCache("bookingHolds");
        if (cache != null) {
            cache.evict(holdId);
            log.info("Invalidated booking hold cache for holdId: {}", holdId);
        }
    }

    /**
     * Clear all caches (use with caution, typically for admin operations).
     */
    public void clearAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("Cleared cache: {}", cacheName);
            }
        });
    }
}
