package com.plant.backend.service;

/**
 * 邮箱验证码服务
 * 
 * @author Greenly Team
 * @date 2026-04-12
 */
public interface EmailVerificationService {

    /**
     * 发送注册验证码
     * 
     * @param email 收件邮箱
     */
    void sendRegisterCode(String email);

    /**
     * 发送重置密码验证码
     * 
     * @param email 收件邮箱
     */
    void sendResetPasswordCode(String email);

    /**
     * 发送邮箱绑定验证码
     * 
     * @param email 收件邮箱
     */
    void sendBindEmailCode(String email);

    /**
     * 验证验证码
     * 
     * @param email 邮箱
     * @param code 验证码
     * @param type 类型: register / reset / bind
     * @return 是否验证通过
     */
    boolean verifyCode(String email, String code, String type);

    /**
     * 消费验证码（验证通过后删除，防止重复使用）
     * 
     * @param email 邮箱
     * @param code 验证码
     * @param type 类型
     * @return 是否消费成功
     */
    boolean consumeCode(String email, String code, String type);
}
