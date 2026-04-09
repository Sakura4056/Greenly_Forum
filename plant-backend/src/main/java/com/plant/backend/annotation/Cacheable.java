package com.plant.backend.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 缓存注解
 * 用于标记需要缓存的方法
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cacheable {
    
    /**
     * 缓存键前缀
     */
    String prefix() default "";
    
    /**
     * 缓存键表达式（支持 SpEL）
     * 例如：#userId, #plantId
     */
    String key();
    
    /**
     * 过期时间（秒）
     * 默认 30 分钟
     */
    long expire() default 1800;
    
    /**
     * 时间单位
     * 默认秒
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
    
    /**
     * 是否忽略空值
     * 如果为 true，返回值为 null 时不缓存
     */
    boolean ignoreNull() default false;
}
