package com.xyz.gateway.filter;

import com.xyz.gateway.dto.ValidateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final WebClient webClient;
    private final List<String> publicPaths;

    public JwtAuthenticationGatewayFilterFactory(
            @Value("${auth.service.url}") String authServiceUrl,
            @Value("${auth.public-paths}") List<String> publicPaths) {
        super(Object.class);
        this.webClient = WebClient.builder()
                .baseUrl(authServiceUrl)
                .build();
        this.publicPaths = publicPaths;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().value();

            // Skip authentication for public paths
            if (isPublicPath(path)) {
                log.debug("Public path accessed: {}", path);
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Validate token with auth service
            return validateToken(authHeader)
                    .flatMap(validateResponse -> {
                        if (!validateResponse.isValid()) {
                            log.warn("Invalid token for path: {}", path);
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }

                        // Add user context to request headers
                        ServerWebExchange modifiedExchange = exchange.mutate()
                                .request(r -> r.headers(headers -> {
                                    headers.add("X-User-Id", validateResponse.getUserId());
                                    headers.add("X-User-Role", validateResponse.getRole());
                                }))
                                .build();

                        log.debug("Token validated for user: {}, role: {}", 
                                validateResponse.getUserId(), validateResponse.getRole());

                        return chain.filter(modifiedExchange);
                    })
                    .onErrorResume(e -> {
                        log.error("Error validating token: {}", e.getMessage());
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        };
    }

    private Mono<ValidateResponse> validateToken(String authHeader) {
        return webClient.get()
                .uri("/api/auth/validate")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(ApiResponseWrapper.class)
                .map(wrapper -> wrapper.getData());
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }

    @Override
    public String name() {
        return "JwtAuthentication";
    }

    // Wrapper class to match auth service response structure
    private static class ApiResponseWrapper {
        private ValidateResponse data;

        public ValidateResponse getData() {
            return data;
        }

        public void setData(ValidateResponse data) {
            this.data = data;
        }
    }
}
