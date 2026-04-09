package com.plant.backend.util;

/**
 * 缓存键常量类
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
public final class CacheKeys {
    
    /**
     * 用户相关缓存键
     */
    public static final String USER_INFO = "user:info:";              // user:info:{userId}
    public static final String USER_TOKEN = "user:token:";            // user:token:{userId}
    public static final String USER_REFRESH_TOKEN = "user:refresh:";  // user:refresh:{userId}
    
    /**
     * 植物相关缓存键
     */
    public static final String MY_PLANT_LIST = "plant:my:list:";      // plant:my:list:{userId}
    public static final String MY_PLANT_DETAIL = "plant:my:detail:";  // plant:my:detail:{plantId}
    public static final String OFFICIAL_PLANT_LIST = "plant:official:list:";  // plant:official:list:{categoryId}
    
    /**
     * 养护记录相关缓存键
     */
    public static final String CARE_RECORD_STATS = "care:stats:";     // care:stats:{userId}:{range}
    public static final String CARE_RECORD_LIST = "care:record:list:"; // care:record:list:{userId}:{plantId}
    
    /**
     * 养护计划相关缓存键
     */
    public static final String CARE_SCHEDULE_TODAY = "care:schedule:today:";  // care:schedule:today:{userId}
    public static final String CARE_SCHEDULE_WEEK = "care:schedule:week:";    // care:schedule:week:{userId}
    
    /**
     * 统计相关缓存键
     */
    public static final String USER_STATS = "stats:user:";            // stats:user:{userId}
    public static final String GLOBAL_STATS = "stats:global";         // 全局统计
    
    /**
     * 系统配置相关缓存键
     */
    public static final String SYSTEM_CONFIG = "system:config:";      // system:config:{key}
    public static final String SYSTEM_DICT = "system:dict:";          // system:dict:{type}
    
    /**
     * 验证码相关缓存键
     */
    public static final String SMS_CODE = "sms:code:";                // sms:code:{phone}
    public static final String EMAIL_CODE = "email:code:";            // email:code:{email}
    public static final String IMAGE_CODE = "image:code:";            // image:code:{uuid}
    
    /**
     * 限流相关缓存键
     */
    public static final String RATE_LIMIT = "rate:limit:";            // rate:limit:{api}:{userId}
    
    /**
     * 分布式锁相关缓存键
     */
    public static final String LOCK = "lock:";                        // lock:{businessKey}
    
    private CacheKeys() {
        // 防止实例化
    }
    
    /**
     * 生成带前缀的键
     * 
     * @param prefix 前缀
     * @param key 键
     * @return 完整的键
     */
    public static String buildKey(String prefix, Object key) {
        return prefix + key.toString();
    }
    
    /**
     * 生成多个参数的键
     * 
     * @param prefix 前缀
     * @param keys 多个键
     * @return 完整的键（用冒号连接）
     */
    public static String buildKey(String prefix, Object... keys) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object key : keys) {
            sb.append(":").append(key);
        }
        return sb.toString();
    }
}
