package com.xyz.theatre.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for managing cache operations with dual cache strategy:
 * - Redis (distributed): 60s TTL for show availability
 * - Caffeine (local): 10s TTL for theatre metadata
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    @Qualifier("redisCacheManager")
    private final CacheManager redisCacheManager;

    @Qualifier("localCacheManager")
    private final CacheManager localCacheManager;

    /**
     * Invalidate show availability cache in Redis.
     * Cache-aside pattern with 60s TTL.
     *
     * @param showId The show ID whose availability cache should be invalidated
     */
    public void invalidateShowAvailability(UUID showId) {
        Cache cache = redisCacheManager.getCache("showAvailability");
        if (cache != null) {
            cache.evict(showId);
            log.info("Invalidated show availability cache for showId: {}", showId);
        }
    }

    /**
     * Invalidate theatre search results cache in Redis.
     *
     * @param cacheKey The search cache key (e.g., "city:movie:date")
     */
    public void invalidateTheatreSearch(String cacheKey) {
        Cache cache = redisCacheManager.getCache("theatreSearch");
        if (cache != null) {
            cache.evict(cacheKey);
            log.info("Invalidated theatre search cache for key: {}", cacheKey);
        }
    }

    /**
     * Invalidate theatre metadata in local Caffeine cache.
     * Local cache with 10s TTL for frequently accessed metadata.
     *
     * @param theatreId The theatre ID whose metadata cache should be invalidated
     */
    public void invalidateTheatreMetadata(UUID theatreId) {
        Cache cache = localCacheManager.getCache("theatreMetadata");
        if (cache != null) {
            cache.evict(theatreId);
            log.info("Invalidated theatre metadata cache for theatreId: {}", theatreId);
        }
    }

    /**
     * Clear all Redis caches.
     */
    public void clearRedisCaches() {
        redisCacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = redisCacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("Cleared Redis cache: {}", cacheName);
            }
        });
    }

    /**
     * Clear all local Caffeine caches.
     */
    public void clearLocalCaches() {
        localCacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = localCacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                log.info("Cleared local cache: {}", cacheName);
            }
        });
    }
}
