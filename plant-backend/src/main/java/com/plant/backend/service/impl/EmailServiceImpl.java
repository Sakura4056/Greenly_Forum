package com.plant.backend.service.impl;

import com.plant.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    /**
     * SMTP 认证邮箱（必须与 MAIL_USERNAME 一致）
     */
    @Value("${spring.mail.username}")
    private String smtpUsername;

    /**
     * 发件人显示名称，默认为 "Greenly Support"
     */
    @Value("${app.mail.from-display-name:Greenly Support}")
    private String fromDisplayName;

    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // 使用 "显示名称 <SMTP邮箱>" 格式，确保邮件客户端显示专业名称
            message.setFrom(String.format("%s <%s>", fromDisplayName, smtpUsername));
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            javaMailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    @Override
    public void sendHtmlMail(String to, String subject, String htmlContent) {
        if (smtpUsername == null || smtpUsername.isEmpty()) {
            log.error("发件人邮箱未配置，请检查 spring.mail.username 配置");
            throw new IllegalStateException("发件人邮箱未配置");
        }

        log.info("准备发送邮件:");
        log.info("  - 发件人: {} <{}>", fromDisplayName, smtpUsername);
        log.info("  - 收件人: {}", to);
        log.info("  - 主题: {}", subject);

        try {
            jakarta.mail.internet.MimeMessage message = javaMailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                    message, true, "UTF-8");
            // 关键修复：使用 setFrom(email, displayName) 设置专业显示名称
            // 而不是直接使用 SMTP 账号的默认昵称（如 "我"）
            helper.setFrom(smtpUsername, fromDisplayName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            log.info("正在调用 JavaMailSender.send()...");
            javaMailSender.send(message);
            log.info("✅ HTML 邮件发送成功至: {}", to);
        } catch (Exception e) {
            log.error("❌ 发送 HTML 邮件失败，收件人: {}", to, e);
            log.error("错误类型: {}", e.getClass().getName());
            log.error("错误信息: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("根本原因: {}", e.getCause().getMessage());
            }
            // 重新抛出异常，让调用方知道发送失败
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }
}
