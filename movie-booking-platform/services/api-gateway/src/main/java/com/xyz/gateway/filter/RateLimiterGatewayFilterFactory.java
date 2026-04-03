package com.xyz.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RateLimiterGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final StringRedisTemplate redisTemplate;
    private final int requestsPerMinute;
    private final String redisKeyPrefix;

    public RateLimiterGatewayFilterFactory(
            StringRedisTemplate redisTemplate,
            @Value("${rate-limiter.requests-per-minute:100}") int requestsPerMinute,
            @Value("${rate-limiter.redis-key-prefix:rate-limit}") String redisKeyPrefix) {
        super(Object.class);
        this.redisTemplate = redisTemplate;
        this.requestsPerMinute = requestsPerMinute;
        this.redisKeyPrefix = redisKeyPrefix;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            
            // If no user ID (public routes), use IP address
            if (!StringUtils.hasText(userId)) {
                userId = exchange.getRequest().getRemoteAddress() != null 
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "anonymous";
            }

            String rateLimitKey = redisKeyPrefix + ":" + userId;

            try {
                // Token bucket algorithm using Redis
                Long currentCount = redisTemplate.opsForValue().increment(rateLimitKey);
                
                if (currentCount == null) {
                    currentCount = 0L;
                }

                if (currentCount == 1) {
                    // First request in this window, set expiry
                    redisTemplate.expire(rateLimitKey, Duration.ofMinutes(1));
                }

                if (currentCount > requestsPerMinute) {
                    log.warn("Rate limit exceeded for user: {}, count: {}", userId, currentCount);
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
                    exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "0");
                    return exchange.getResponse().setComplete();
                }

                // Add rate limit headers
                exchange.getResponse().getHeaders().add("X-RateLimit-Limit", String.valueOf(requestsPerMinute));
                exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", 
                        String.valueOf(requestsPerMinute - currentCount));

                log.debug("Rate limit check passed for user: {}, count: {}/{}", 
                        userId, currentCount, requestsPerMinute);

                return chain.filter(exchange);

            } catch (Exception e) {
                log.error("Error checking rate limit for user: {}", userId, e);
                // On error, allow the request to proceed (fail open)
                return chain.filter(exchange);
            }
        };
    }

    @Override
    public String name() {
        return "RateLimiter";
    }
}
