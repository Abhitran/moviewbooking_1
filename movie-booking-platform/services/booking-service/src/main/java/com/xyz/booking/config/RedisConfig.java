package com.xyz.booking.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setConnectionPoolSize(64)
                .setConnectionMinimumIdleSize(10)
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        // Set lock watchdog timeout to 30 seconds
        config.setLockWatchdogTimeout(30000L);

        return Redisson.create(config);
    }

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, org.redisson.spring.cache.CacheConfig> config = new HashMap<>();
        
        // Cache for show availability with 60s TTL
        config.put("showAvailability", new org.redisson.spring.cache.CacheConfig(
                60000L,  // TTL: 60 seconds
                30000L   // Max idle time: 30 seconds
        ));
        
        // Cache for booking holds with 600s TTL (10 minutes)
        config.put("bookingHolds", new org.redisson.spring.cache.CacheConfig(
                600000L,  // TTL: 600 seconds (10 minutes)
                300000L   // Max idle time: 5 minutes
        ));
        
        return new RedissonSpringCacheManager(redissonClient, config);
    }
}
