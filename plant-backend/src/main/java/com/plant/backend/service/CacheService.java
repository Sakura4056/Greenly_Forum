package com.plant.backend.service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务接口
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
public interface CacheService {
    
    /**
     * 设置缓存
     * 
     * @param key 键
     * @param value 值
     */
    void set(String key, Object value);
    
    /**
     * 设置缓存（带过期时间）
     * 
     * @param key 键
     * @param value 值
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    void set(String key, Object value, long timeout, TimeUnit unit);
    
    /**
     * 获取缓存
     * 
     * @param key 键
     * @return 值
     */
    Object get(String key);
    
    /**
     * 获取缓存并转换为指定类型
     * 
     * @param key 键
     * @param clazz 目标类型
     * @return 值
     */
    <T> T get(String key, Class<T> clazz);
    
    /**
     * 删除缓存
     * 
     * @param key 键
     */
    void delete(String key);
    
    /**
     * 判断键是否存在
     * 
     * @param key 键
     * @return true-存在 false-不存在
     */
    boolean hasKey(String key);
    
    /**
     * 设置过期时间
     * 
     * @param key 键
     * @param timeout 超时时间
     * @param unit 时间单位
     */
    void expire(String key, long timeout, TimeUnit unit);
    
    /**
     * 获取剩余过期时间
     * 
     * @param key 键
     * @param unit 时间单位
     * @return 剩余时间
     */
    long getExpire(String key, TimeUnit unit);
    
    /**
     * 自增（用于计数器）
     * 
     * @param key 键
     * @return 自增后的值
     */
    long increment(String key);
    
    /**
     * 自减
     * 
     * @param key 键
     * @return 自减后的值
     */
    long decrement(String key);
}
