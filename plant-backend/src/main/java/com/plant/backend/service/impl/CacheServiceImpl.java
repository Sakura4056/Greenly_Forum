package com.plant.backend.service.impl;

import com.plant.backend.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务实现类
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        log.debug("设置缓存：key={}", key);
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
        log.debug("设置缓存：key={}, timeout={} {}", key, timeout, unit);
    }

    @Override
    public Object get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        log.debug("获取缓存：key={}, found={}", key, value != null);
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            log.error("缓存类型转换失败：key={}, expected={}", key, clazz.getName());
            return null;
        }
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("删除缓存：key={}", key);
    }

    @Override
    public boolean hasKey(String key) {
        Boolean hasKey = redisTemplate.hasKey(key);
        return hasKey != null && hasKey;
    }

    @Override
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
        log.debug("设置过期时间：key={}, timeout={} {}", key, timeout, unit);
    }

    @Override
    public long getExpire(String key, TimeUnit unit) {
        Long expire = redisTemplate.getExpire(key, unit);
        return expire != null ? expire : -1;
    }

    @Override
    public long increment(String key) {
        Long value = redisTemplate.opsForValue().increment(key);
        log.debug("自增：key={}, new value={}", key, value);
        return value != null ? value : 0;
    }

    @Override
    public long decrement(String key) {
        Long value = redisTemplate.opsForValue().decrement(key);
        log.debug("自减：key={}, new value={}", key, value);
        return value != null ? value : 0;
    }
}
