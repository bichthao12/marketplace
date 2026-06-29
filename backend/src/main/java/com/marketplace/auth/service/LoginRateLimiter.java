package com.marketplace.auth.service;

import com.marketplace.common.exception.ApiException;
import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(15);

    private final StringRedisTemplate redisTemplate;

    public LoginRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void checkAllowed(String ip) {
        String key = key(ip);
        String value = redisTemplate.opsForValue().get(key);
        if (value != null && Integer.parseInt(value) >= MAX_ATTEMPTS) {
            throw new ApiException("TOO_MANY_REQUESTS", "Too many login attempts. Try again later.", HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    public void recordFailure(String ip) {
        String key = key(ip);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, WINDOW);
        }
    }

    public void reset(String ip) {
        redisTemplate.delete(key(ip));
    }

    private static String key(String ip) {
        return "auth:login_attempts:" + ip;
    }
}
