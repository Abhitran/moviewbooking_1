package com.xyz.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterGatewayFilterFactoryTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private GatewayFilterChain chain;

    private RateLimiterGatewayFilterFactory filterFactory;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        filterFactory = new RateLimiterGatewayFilterFactory(redisTemplate, 100, "rate-limit");
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowRequestWhenUnderLimit() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", "user123")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        var filter = filterFactory.apply(new Object());
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(any());
        verify(redisTemplate.opsForValue()).increment(anyString());
    }

    @Test
    void shouldBlockRequestWhenOverLimit() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", "user123")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(valueOperations.increment(anyString())).thenReturn(101L);

        // When
        var filter = filterFactory.apply(new Object());
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
        verify(chain, never()).filter(any());
    }

    @Test
    void shouldUseIpAddressWhenNoUserId() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .remoteAddress(new java.net.InetSocketAddress("192.168.1.1", 8080))
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        var filter = filterFactory.apply(new Object());
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(valueOperations).increment(contains("192.168.1.1"));
    }

    @Test
    void shouldSetExpiryOnFirstRequest() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-User-Id", "user123")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.expire(anyString(), any(Duration.class))).thenReturn(true);

        // When
        var filter = filterFactory.apply(new Object());
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        verify(redisTemplate).expire(anyString(), eq(Duration.ofMinutes(1)));
    }
}
