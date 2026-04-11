package com.alethia.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.alethia.gateway.config.JwtValidator;

import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

/**
 * Gateway filter that validates JWT tokens and forwards user identity
 * as X-Internal-User-Id header to downstream services.
 *
 * Applied to protected routes via route config (Open/Closed principle — 
 * new routes just add the filter name in YAML without modifying this class).
 */
@Component
public class JwtAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private final JwtValidator jwtValidator;

    public JwtAuthenticationGatewayFilterFactory(JwtValidator jwtValidator) {
        super(Config.class);
        this.jwtValidator = jwtValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange);
            }

            String token = authHeader.substring(7);

            if (!jwtValidator.isValid(token)) {
                return unauthorized(exchange);
            }

            Claims claims = jwtValidator.validateAndExtract(token);
            String username = claims.getSubject();
            String userId = claims.get("userId", String.class);

            // Forward identity to downstream services
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(r -> r
                            .header("X-Internal-User-Id", userId != null ? userId : username)
                            .header("X-Internal-Roles", claims.get("roles", String.class)))
                    .build();

            return chain.filter(mutatedExchange);
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Placeholder for future config (e.g., required roles)
    }
}
