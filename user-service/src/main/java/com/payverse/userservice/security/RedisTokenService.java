package com.payverse.userservice.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.time.Duration;

@Service
public class RedisTokenService {

    private final StringRedisTemplate redisTemplate;

    public RedisTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void saveRefreshToken(
            Long userId,
            String refreshToken) {

        String key = "refresh_token:" + userId;

        redisTemplate.opsForValue()
                .set(
                    key,
                    refreshToken,
                    Duration.ofDays(7)
                );
    }


    public String getRefreshToken(Long userId) {

        String key = "refresh_token:" + userId;

        return redisTemplate.opsForValue()
                .get(key);
    }


    public void deleteRefreshToken(Long userId) {

        String key = "refresh_token:" + userId;

        redisTemplate.delete(key);
    }

 public Long findUserIdByRefreshToken(String refreshToken) {

    Set<String> keys = redisTemplate.keys("refresh_token:*");

    if (keys == null) {
        return null;
    }

    for (String key : keys) {

        String storedToken =
                redisTemplate.opsForValue().get(key);

        if (refreshToken.equals(storedToken)) {

            return Long.parseLong(
                    key.replace("refresh_token:", "")
            );
        }
    }

    return null;
}
}