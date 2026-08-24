package com.payverse.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final SecretKey secretKey;

    public JwtAuthenticationFilter(
            @Value("${jwt.secret}") String jwtSecret) {

        this.secretKey = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            GatewayFilterChain chain) {

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        /*
         * Authentication endpoints must be reachable
         * without an access token.
         */
        if (isPublicEndpoint(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {

            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Object userIdClaim = claims.get("userId");

            if (userIdClaim == null) {
                return unauthorized(exchange);
            }

            String userId = String.valueOf(userIdClaim);

            ServerWebExchange mutatedExchange =
                    exchange.mutate()
                            .request(
                                    exchange.getRequest()
                                            .mutate()
                                            .header("X-User-Id", userId)
                                            .build()
                            )
                            .build();

            return chain.filter(mutatedExchange);

        } catch (JwtException | IllegalArgumentException ex) {

            return unauthorized(exchange);
        }
    }
private boolean isPublicEndpoint(String path) {

    return path.equals("/api/v1/auth/login")
            || path.equals("/api/v1/auth/register")
            || path.equals("/api/v1/auth/refresh-token");
}

    private Mono<Void> unauthorized(
            ServerWebExchange exchange) {

        exchange.getResponse()
                .setStatusCode(HttpStatus.UNAUTHORIZED);

        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -100;
    }
}