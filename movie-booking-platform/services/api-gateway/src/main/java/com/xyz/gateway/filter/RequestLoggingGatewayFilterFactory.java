package com.xyz.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class RequestLoggingGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final List<String> SENSITIVE_HEADERS = Arrays.asList(
            "authorization", "password", "token", "secret", "api-key"
    );

    public RequestLoggingGatewayFilterFactory() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            long startTime = System.currentTimeMillis();

            String correlationId = request.getHeaders().getFirst("X-Correlation-ID");
            String method = request.getMethod().name();
            String path = request.getPath().value();
            String userId = request.getHeaders().getFirst("X-User-Id");

            log.info("Incoming request - correlationId: {}, method: {}, path: {}, userId: {}", 
                    correlationId, method, path, userId != null ? userId : "anonymous");

            return chain.filter(exchange).then(
                    org.springframework.web.server.ServerWebExchange.LOG_ID_ATTRIBUTE != null ?
                    reactor.core.publisher.Mono.fromRunnable(() -> {
                        ServerHttpResponse response = exchange.getResponse();
                        long duration = System.currentTimeMillis() - startTime;
                        int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;

                        log.info("Outgoing response - correlationId: {}, method: {}, path: {}, status: {}, duration: {}ms", 
                                correlationId, method, path, statusCode, duration);
                    }) : reactor.core.publisher.Mono.empty()
            );
        };
    }

    @Override
    public String name() {
        return "RequestLogging";
    }

    private boolean isSensitiveHeader(String headerName) {
        return SENSITIVE_HEADERS.stream()
                .anyMatch(sensitive -> headerName.toLowerCase().contains(sensitive));
    }
}
