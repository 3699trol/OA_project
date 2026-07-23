package com.recruitment.common.redis.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final ObjectProvider<RedisTemplate<String, Object>> redisTemplateProvider;

    public void set(String key, Object value) {
        RedisTemplate<String, Object> redisTemplate = redisTemplate();
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        RedisTemplate<String, Object> redisTemplate = redisTemplate();
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        }
    }

    public Object get(String key) {
        RedisTemplate<String, Object> redisTemplate = redisTemplate();
        return redisTemplate == null ? null : redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        RedisTemplate<String, Object> redisTemplate = redisTemplate();
        return redisTemplate != null && Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public Long deleteByPattern(String pattern) {
        RedisTemplate<String, Object> redisTemplate = redisTemplate();
        if (redisTemplate == null) {
            return 0L;
        }
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return redisTemplate.delete(keys);
    }

    public Boolean hasKey(String key) {
        RedisTemplate<String, Object> redisTemplate = redisTemplate();
        return redisTemplate != null && Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public Boolean expire(String key, long timeout, TimeUnit unit) {
        RedisTemplate<String, Object> redisTemplate = redisTemplate();
        return redisTemplate != null && Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    public Long increment(String key) {
        RedisTemplate<String, Object> redisTemplate = redisTemplate();
        return redisTemplate == null ? null : redisTemplate.opsForValue().increment(key);
    }

    private RedisTemplate<String, Object> redisTemplate() {
        return redisTemplateProvider.getIfAvailable();
    }
}
