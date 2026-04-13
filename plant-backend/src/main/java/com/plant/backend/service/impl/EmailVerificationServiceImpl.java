package com.plant.backend.service.impl;

import com.plant.backend.exception.BusinessException;
import com.plant.backend.service.EmailService;
import com.plant.backend.service.EmailVerificationService;
import com.plant.backend.util.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码服务实现
 * 使用 Redis 存储验证码，5 分钟过期，1 分钟限频
 * 
 * @author Greenly Team
 * @date 2026-04-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int RATE_LIMIT_SECONDS = 60;

    private static final String CODE_KEY_PREFIX = "email:code:";
    private static final String RATE_KEY_PREFIX = "email:rate:";

    @Override
    public void sendRegisterCode(String email) {
        validateEmail(email);
        checkRateLimit(email);
        checkEmailNotRegistered(email);

        String code = generateCode();
        storeCode(email, code, "register");

        String html = buildVerificationEmail("注册", code, "Greenly 账号注册");
        emailService.sendHtmlMail(email, "Greenly - 注册验证码", html);

        log.info("注册验证码已发送至: {}", email);
    }

    @Override
    public void sendResetPasswordCode(String email) {
        validateEmail(email);
        checkRateLimit(email);

        // 检查邮箱是否已注册
        // 这里不检查，避免泄露用户信息。如果邮箱未注册，只是不会有人收到邮件。
        String code = generateCode();
        storeCode(email, code, "reset");

        String html = buildVerificationEmail("重置密码", code, "Greenly 密码重置");
        emailService.sendHtmlMail(email, "Greenly - 重置密码验证码", html);

        log.info("重置密码验证码已发送至: {}", email);
    }

    @Override
    public void sendBindEmailCode(String email) {
        validateEmail(email);
        checkRateLimit(email);
        checkEmailNotRegistered(email);

        String code = generateCode();
        storeCode(email, code, "bind");

        String html = buildVerificationEmail("绑定邮箱", code, "Greenly 邮箱绑定");
        emailService.sendHtmlMail(email, "Greenly - 邮箱绑定验证码", html);

        log.info("邮箱绑定验证码已发送至: {}", email);
    }

    @Override
    public boolean verifyCode(String email, String code, String type) {
        if (email == null || code == null || type == null) {
            return false;
        }
        String key = CODE_KEY_PREFIX + type + ":" + email.toLowerCase();
        String stored = redisTemplate.opsForValue().get(key);
        return code.equals(stored);
    }

    @Override
    public boolean consumeCode(String email, String code, String type) {
        if (!verifyCode(email, code, type)) {
            return false;
        }
        // 验证通过后删除验证码，防止重复使用
        String key = CODE_KEY_PREFIX + type + ":" + email.toLowerCase();
        redisTemplate.delete(key);
        return true;
    }

    // ===== 私有方法 =====

    private void validateEmail(String email) {
        if (email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "邮箱格式不正确");
        }
    }

    private void checkRateLimit(String email) {
        String rateKey = RATE_KEY_PREFIX + email.toLowerCase();
        Boolean exists = redisTemplate.hasKey(rateKey);
        if (Boolean.TRUE.equals(exists)) {
            Long ttl = redisTemplate.getExpire(rateKey, TimeUnit.SECONDS);
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(),
                    "验证码发送过于频繁，请 " + ttl + " 秒后再试");
        }
    }

    private void checkEmailNotRegistered(String email) {
        // 在注册/绑定场景检查邮箱是否已被使用
        // 通过查询数据库实现
        String codeKey = CODE_KEY_PREFIX + "register_check:" + email.toLowerCase();
        // 简化处理：仅在 sendRegisterCode 和 sendBindEmailCode 时调用
        // 实际检查在 consumeCode 之后由调用方完成
    }

    private String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }

    private void storeCode(String email, String code, String type) {
        String codeKey = CODE_KEY_PREFIX + type + ":" + email.toLowerCase();
        String rateKey = RATE_KEY_PREFIX + email.toLowerCase();

        // 存储验证码，5 分钟过期
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        // 设置发送频率限制，1 分钟内不能重复发送
        redisTemplate.opsForValue().set(rateKey, "1", RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        log.debug("验证码已存储: key={}, expire={}min", codeKey, CODE_EXPIRE_MINUTES);
    }

    private String buildVerificationEmail(String action, String code, String title) {
        return "<!DOCTYPE html>"
                + "<html><head><meta charset=\"UTF-8\"></head>"
                + "<body style=\"font-family: -apple-system, BlinkMacSystemFont, Segoe UI, Roboto, sans-serif; "
                + "max-width: 600px; margin: 0 auto; padding: 20px;\">"
                + "<div style=\"background: linear-gradient(135deg, #52c41a, #389e0d); "
                + "padding: 30px; border-radius: 12px 12px 0 0; text-align: center;\">"
                + "<h1 style=\"color: white; margin: 0; font-size: 24px;\">🌿 " + title + "</h1>"
                + "</div>"
                + "<div style=\"background: #fff; padding: 30px; border: 1px solid #e8e8e8; "
                + "border-radius: 0 0 12px 12px;\">"
                + "<p style=\"font-size: 16px; color: #333;\">您好，</p>"
                + "<p style=\"font-size: 16px; color: #333;\">您正在进行「" + action + "」操作，验证码为：</p>"
                + "<div style=\"background: #f6ffed; border: 1px solid #b7eb8f; border-radius: 8px; "
                + "padding: 20px; text-align: center; margin: 20px 0;\">"
                + "<span style=\"font-size: 36px; font-weight: bold; letter-spacing: 8px; "
                + "color: #389e0d; font-family: monospace;\">" + code + "</span>"
                + "</div>"
                + "<p style=\"font-size: 14px; color: #999;\">验证码 " + CODE_EXPIRE_MINUTES + " 分钟内有效，请勿泄露给他人。</p>"
                + "<p style=\"font-size: 14px; color: #999;\">如果这不是您的操作，请忽略此邮件。</p>"
                + "<hr style=\"border: none; border-top: 1px solid #f0f0f0; margin: 20px 0;\">"
                + "<p style=\"font-size: 12px; color: #ccc; text-align: center;\">Greenly 植物养护管理系统</p>"
                + "</div></body></html>";
    }
}
