package com.xyz.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Component
public class CorrelationIdGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    public CorrelationIdGatewayFilterFactory() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);

            if (!StringUtils.hasText(correlationId)) {
                correlationId = UUID.randomUUID().toString();
                log.debug("Generated new correlation ID: {}", correlationId);
            }

            final String finalCorrelationId = correlationId;

            return chain.filter(
                    exchange.mutate()
                            .request(r -> r.headers(headers -> 
                                    headers.add(CORRELATION_ID_HEADER, finalCorrelationId)))
                            .response(r -> r.headers(headers -> 
                                    headers.add(CORRELATION_ID_HEADER, finalCorrelationId)))
                            .build()
            );
        };
    }

    @Override
    public String name() {
        return "CorrelationId";
    }
}
