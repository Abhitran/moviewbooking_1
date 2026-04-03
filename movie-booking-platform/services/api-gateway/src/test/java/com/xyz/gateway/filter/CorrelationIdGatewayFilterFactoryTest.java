package com.xyz.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorrelationIdGatewayFilterFactoryTest {

    @Mock
    private GatewayFilterChain chain;

    private CorrelationIdGatewayFilterFactory filterFactory;

    @BeforeEach
    void setUp() {
        filterFactory = new CorrelationIdGatewayFilterFactory();
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    @Test
    void shouldGenerateCorrelationIdWhenNotPresent() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        var filter = filterFactory.apply(new Object());
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        String correlationId = exchange.getResponse().getHeaders().getFirst("X-Correlation-ID");
        assertNotNull(correlationId);
        assertFalse(correlationId.isEmpty());
    }

    @Test
    void shouldPreserveExistingCorrelationId() {
        // Given
        String existingId = "existing-correlation-id";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test")
                .header("X-Correlation-ID", existingId)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        var filter = filterFactory.apply(new Object());
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        String correlationId = exchange.getResponse().getHeaders().getFirst("X-Correlation-ID");
        assertEquals(existingId, correlationId);
    }

    @Test
    void shouldAddCorrelationIdToResponse() {
        // Given
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/test").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // When
        var filter = filterFactory.apply(new Object());
        Mono<Void> result = filter.filter(exchange, chain);

        // Then
        StepVerifier.create(result)
                .verifyComplete();

        assertNotNull(exchange.getResponse().getHeaders().getFirst("X-Correlation-ID"));
    }
}
