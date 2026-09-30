package com.school.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 缓存服务：优先 Redis，连接失败自动回退内存缓存（保证无 Redis 环境也能演示）
 */
@Slf4j
@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final Map<String, Object> memory = new ConcurrentHashMap<>();
    private final Map<String, Long> memoryExpire = new ConcurrentHashMap<>();

    public void set(String key, Object value, long seconds) {
        try {
            redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
            return;
        } catch (Exception e) {
            // fall through to memory
        }
        memory.put(key, value);
        memoryExpire.put(key, System.currentTimeMillis() + seconds * 1000);
    }

    public Object get(String key) {
        try {
            Object v = redisTemplate.opsForValue().get(key);
            if (v != null) {
                return v;
            }
        } catch (Exception e) {
            // fall through
        }
        Long exp = memoryExpire.get(key);
        if (exp != null && exp < System.currentTimeMillis()) {
            memory.remove(key);
            memoryExpire.remove(key);
            return null;
        }
        return memory.get(key);
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            // ignore
        }
        memory.remove(key);
        memoryExpire.remove(key);
    }
}
