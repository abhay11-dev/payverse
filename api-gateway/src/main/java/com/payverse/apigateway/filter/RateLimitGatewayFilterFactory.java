package com.payverse.apigateway.filter;

import com.payverse.apigateway.ratelimit.RateLimiterService;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class RateLimitGatewayFilterFactory
        extends AbstractGatewayFilterFactory<RateLimitGatewayFilterFactory.Config> {

    private final RateLimiterService rateLimiterService;

    public RateLimitGatewayFilterFactory(
            RateLimiterService rateLimiterService) {

        super(Config.class);
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            String userId = exchange.getRequest()
                    .getHeaders()
                    .getFirst("X-User-Id");

            /*
             * Public endpoints don't have X-User-Id.
             * JWT filter handles authentication separately.
             */
            if (userId == null || userId.isBlank()) {
                return chain.filter(exchange);
            }

            boolean allowed = rateLimiterService.isAllowed(
                    userId,
                    config.getLimit(),
                    config.getWindowSeconds()
            );

            if (!allowed) {

                exchange.getResponse()
                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS);

                return exchange.getResponse().setComplete();
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {

        private int limit = 10;

        private int windowSeconds = 60;

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(int windowSeconds) {
            this.windowSeconds = windowSeconds;
        }
    }
}