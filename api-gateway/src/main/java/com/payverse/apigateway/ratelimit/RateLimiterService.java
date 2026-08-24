package com.payverse.apigateway.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RateLimiterService {

    private final StringRedisTemplate redisTemplate;

    private final DefaultRedisScript<Long> rateLimitScript;

    public RateLimiterService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;

        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setScriptText("""
                local current = redis.call('INCR', KEYS[1])

                if current == 1 then
                    redis.call('EXPIRE', KEYS[1], ARGV[1])
                end

                return current
                """);

        this.rateLimitScript.setResultType(Long.class);
    }

    public boolean isAllowed(String userId, int limit, int windowSeconds) {

        String key = "rate_limit:user:" + userId;

        Long currentCount = redisTemplate.execute(
                rateLimitScript,
                List.of(key),
                String.valueOf(windowSeconds)
        );

        return currentCount != null && currentCount <= limit;
    }
}