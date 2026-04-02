package com.xyz.theatre.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Reads user identity from headers injected by the API Gateway after JWT validation.
 * Headers: X-User-Id, X-User-Role
 */
@Slf4j
@Component
public class GatewayAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userId = request.getHeader("X-User-Id");
        String userRole = request.getHeader("X-User-Role");

        if (StringUtils.hasText(userId) && StringUtils.hasText(userRole)) {
            try {
                UUID.fromString(userId); // validate UUID format
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + userRole));
                var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                log.debug("Authenticated user {} with role {}", userId, userRole);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid X-User-Id header: {}", userId);
            }
        }

        filterChain.doFilter(request, response);
    }
}
