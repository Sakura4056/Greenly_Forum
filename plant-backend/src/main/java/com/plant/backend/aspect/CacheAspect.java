package com.plant.backend.aspect;

import com.plant.backend.annotation.Cacheable;
import com.plant.backend.service.CacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 缓存切面
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CacheAspect {

    private final CacheService cacheService;
    private final ExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.plant.backend.annotation.Cacheable)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 获取注解信息
        Cacheable cacheable = method.getAnnotation(Cacheable.class);
        String prefix = cacheable.prefix();
        String keyExpression = cacheable.key();
        long expire = cacheable.expire();
        TimeUnit timeUnit = cacheable.timeUnit();
        boolean ignoreNull = cacheable.ignoreNull();
        
        // 生成缓存键
        String cacheKey = generateCacheKey(prefix, keyExpression, joinPoint);
        
        // 尝试从缓存获取
        Object cachedValue = cacheService.get(cacheKey);
        if (cachedValue != null) {
            log.debug("命中缓存：key={}", cacheKey);
            return cachedValue;
        }
        
        // 执行方法
        Object result = joinPoint.proceed();
        
        // 缓存结果（如果忽略空值且结果为 null，则不缓存）
        if (!(ignoreNull && result == null)) {
            cacheService.set(cacheKey, result, expire, timeUnit);
            log.debug("设置缓存：key={}, expire={} {}", cacheKey, expire, timeUnit);
        }
        
        return result;
    }
    
    /**
     * 生成缓存键
     */
    private String generateCacheKey(String prefix, String keyExpression, ProceedingJoinPoint joinPoint) {
        StringBuilder cacheKey = new StringBuilder();
        
        // 添加前缀
        if (StringUtils.hasText(prefix)) {
            cacheKey.append(prefix).append(":");
        }
        
        // 解析 SpEL 表达式
        if (StringUtils.hasText(keyExpression)) {
            try {
                MethodSignature signature = (MethodSignature) joinPoint.getSignature();
                String[] paramNames = signature.getParameterNames();
                Object[] args = joinPoint.getArgs();
                
                StandardEvaluationContext context = new StandardEvaluationContext();
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
                
                Expression expression = parser.parseExpression(keyExpression);
                Object keyValue = expression.getValue(context);
                cacheKey.append(keyValue);
            } catch (Exception e) {
                log.warn("解析 SpEL 表达式失败：{}, 使用原始值", keyExpression);
                cacheKey.append(keyExpression);
            }
        }
        
        return cacheKey.toString();
    }
}
