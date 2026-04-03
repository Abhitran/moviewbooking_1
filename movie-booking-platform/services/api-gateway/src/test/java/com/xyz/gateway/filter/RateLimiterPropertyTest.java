package com.xyz.gateway.filter;

import net.jqwik.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for RateLimiterGatewayFilterFactory
 * 
 * **Validates: Requirements NFR-SEC-06**
 */
class RateLimiterPropertyTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private GatewayFilterChain chain;

    @Property
    @Label("Rate limiter should allow requests under the limit")
    void shouldAllowRequestsUnderLimit(@ForAll @IntRange(min = 1, max = 100) int requestCount) {
        // Given
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn((long) requestCount);
        when(chain.filter(any())).thenReturn(Mono.empty());

        RateLimiterGatewayFilterFactory filterFactory = 
                new RateLimiterGatewayFilterFactory(redisTemplate, 100, "rate-limit");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", "user123")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        var filter = filterFactory.apply(new Object());
        filter.filter(exchange, chain).block();

        // Then
        assertNotEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode(),
                "Request count " + requestCount + " should be allowed (limit: 100)");
    }

    @Property
    @Label("Rate limiter should block requests over the limit")
    void shouldBlockRequestsOverLimit(@ForAll @IntRange(min = 101, max = 1000) int requestCount) {
        // Given
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn((long) requestCount);
        when(chain.filter(any())).thenReturn(Mono.empty());

        RateLimiterGatewayFilterFactory filterFactory = 
                new RateLimiterGatewayFilterFactory(redisTemplate, 100, "rate-limit");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", "user123")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        var filter = filterFactory.apply(new Object());
        filter.filter(exchange, chain).block();

        // Then
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode(),
                "Request count " + requestCount + " should be blocked (limit: 100)");
    }

    @Property
    @Label("Rate limiter should handle different user IDs independently")
    void shouldHandleDifferentUsersIndependently(
            @ForAll @AlphaChars @StringLength(min = 5, max = 20) String userId1,
            @ForAll @AlphaChars @StringLength(min = 5, max = 20) String userId2,
            @ForAll @IntRange(min = 1, max = 100) int count1,
            @ForAll @IntRange(min = 1, max = 100) int count2) {
        
        Assume.that(!userId1.equals(userId2));

        // Given
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(chain.filter(any())).thenReturn(Mono.empty());

        RateLimiterGatewayFilterFactory filterFactory = 
                new RateLimiterGatewayFilterFactory(redisTemplate, 100, "rate-limit");

        // User 1 request
        when(valueOperations.increment(anyString())).thenReturn((long) count1);
        MockServerHttpRequest request1 = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", userId1)
                .build();
        ServerWebExchange exchange1 = MockServerWebExchange.from(request1);

        // User 2 request
        when(valueOperations.increment(anyString())).thenReturn((long) count2);
        MockServerHttpRequest request2 = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", userId2)
                .build();
        ServerWebExchange exchange2 = MockServerWebExchange.from(request2);

        // When
        var filter = filterFactory.apply(new Object());
        filter.filter(exchange1, chain).block();
        filter.filter(exchange2, chain).block();

        // Then - both should be allowed since they're under limit
        assertNotEquals(HttpStatus.TOO_MANY_REQUESTS, exchange1.getResponse().getStatusCode(),
                "User " + userId1 + " with count " + count1 + " should be allowed");
        assertNotEquals(HttpStatus.TOO_MANY_REQUESTS, exchange2.getResponse().getStatusCode(),
                "User " + userId2 + " with count " + count2 + " should be allowed");
    }

    @Property
    @Label("Rate limiter should set correct rate limit headers")
    void shouldSetCorrectRateLimitHeaders(@ForAll @IntRange(min = 1, max = 100) int requestCount) {
        // Given
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn((long) requestCount);
        when(chain.filter(any())).thenReturn(Mono.empty());

        int limit = 100;
        RateLimiterGatewayFilterFactory filterFactory = 
                new RateLimiterGatewayFilterFactory(redisTemplate, limit, "rate-limit");

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", "user123")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        var filter = filterFactory.apply(new Object());
        filter.filter(exchange, chain).block();

        // Then
        String limitHeader = exchange.getResponse().getHeaders().getFirst("X-RateLimit-Limit");
        String remainingHeader = exchange.getResponse().getHeaders().getFirst("X-RateLimit-Remaining");

        assertNotNull(limitHeader, "X-RateLimit-Limit header should be present");
        assertEquals(String.valueOf(limit), limitHeader, "Limit header should match configured limit");
        
        if (requestCount <= limit) {
            assertNotNull(remainingHeader, "X-RateLimit-Remaining header should be present");
            int remaining = Integer.parseInt(remainingHeader);
            assertTrue(remaining >= 0, "Remaining count should be non-negative");
            assertEquals(limit - requestCount, remaining, 
                    "Remaining should be limit minus current count");
        }
    }
}
