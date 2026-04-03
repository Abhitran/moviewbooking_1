package com.xyz.gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class RoleBasedAuthorizationGatewayFilterFactory 
        extends AbstractGatewayFilterFactory<RoleBasedAuthorizationGatewayFilterFactory.Config> {

    public RoleBasedAuthorizationGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String userRole = exchange.getRequest().getHeaders().getFirst("X-User-Role");

            if (!StringUtils.hasText(userRole)) {
                log.warn("No user role found in request headers");
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if (!config.getAllowedRoles().contains(userRole)) {
                log.warn("User role {} not authorized for this route. Required: {}", 
                        userRole, config.getAllowedRoles());
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            log.debug("User role {} authorized for route", userRole);
            return chain.filter(exchange);
        };
    }

    @Override
    public String name() {
        return "RoleBasedAuthorization";
    }

    @Data
    public static class Config {
        private String roles;

        public List<String> getAllowedRoles() {
            return Arrays.asList(roles.split(","));
        }
    }
}
