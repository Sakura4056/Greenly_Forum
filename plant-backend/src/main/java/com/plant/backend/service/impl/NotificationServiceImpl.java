package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plant.backend.entity.CareSchedule;
import com.plant.backend.entity.Reminder;
import com.plant.backend.entity.ReminderConfig;
import com.plant.backend.entity.User;
import com.plant.backend.mapper.ReminderConfigMapper;
import com.plant.backend.mapper.ReminderMapper;
import com.plant.backend.mapper.UserMapper;
import com.plant.backend.service.EmailService;
import com.plant.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final ReminderMapper reminderMapper;
    private final ReminderConfigMapper configMapper;
    private final EmailService emailService;
    private final UserMapper userMapper;

    @Override
    public void sendCareReminderSummary(Long userId, List<CareSchedule> dueSchedules) {
        log.info("========== 开始处理提醒通知 ==========");
        log.info("用户ID: {}", userId);
        log.info("待处理任务数: {}", dueSchedules == null ? 0 : dueSchedules.size());
        
        if (dueSchedules == null || dueSchedules.isEmpty()) {
            log.info("没有待处理的任务，跳过");
            return;
        }

        try {
            // 1. 生成站内信
            log.info("步骤1: 生成站内信提醒...");
            int createdCount = 0;
            for (CareSchedule s : dueSchedules) {
                // 检查是否已存在提醒
                boolean exists = reminderMapper.exists(new LambdaQueryWrapper<Reminder>()
                        .eq(Reminder::getUserId, userId)
                        .eq(Reminder::getScene, "careSchedule")
                        .eq(Reminder::getBusinessId, s.getId()));

                if (!exists) {
                    Reminder r = new Reminder();
                    r.setUserId(userId);
                    r.setScene("careSchedule");
                    r.setBusinessId(s.getId());
                    r.setTitle("养护任务提醒");
                    r.setContent("任务 [" + s.getTaskName() + "] 今天需要完成。");
                    r.setIsRead(0);
                    reminderMapper.insert(r);
                    createdCount++;
                    log.info("  - 创建站内信: {}", s.getTaskName());
                } else {
                    log.info("  - 跳过（已存在）: {}", s.getTaskName());
                }
            }
            log.info("站内信创建完成，新增 {} 条", createdCount);

            // 2. 发送汇总邮件 - 直接从 sys_user 表获取邮箱地址
            log.info("步骤2: 从 sys_user 表获取用户邮箱...");
            User user = userMapper.selectById(userId);
            
            if (user == null) {
                log.error("❌ 用户 {} 不存在，跳过邮件发送", userId);
                return;
            }
            
            String targetEmail = user.getEmail();
            if (targetEmail == null || targetEmail.isEmpty()) {
                log.warn("⚠️ 用户 {} 的邮箱地址为空，跳过邮件发送", userId);
                return;
            }
            
            log.info("✅ 使用 sys_user 表中的邮箱: {}", targetEmail);

            // 检查邮件通知是否被用户禁用
            boolean shouldSendEmail = true;
            ReminderConfig config = configMapper.selectOne(new LambdaQueryWrapper<ReminderConfig>()
                    .eq(ReminderConfig::getUserId, userId));
            
            if (config != null && config.getSceneConfig() != null && !config.getSceneConfig().isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(config.getSceneConfig());
                    if (node.has("emailEnabled") && !node.get("emailEnabled").asBoolean()) {
                        shouldSendEmail = false;
                        log.info("邮件通知被用户禁用 (emailEnabled=false)");
                    } else {
                        log.info("邮件通知已启用");
                    }
                } catch (Exception e) {
                    log.warn("解析 sceneConfig 失败，默认启用邮件", e);
                }
            } else {
                log.info("未找到场景配置，默认启用邮件通知");
            }

            if (shouldSendEmail && targetEmail != null) {
                log.info("步骤3: 准备发送汇总邮件至 {}", targetEmail);
                
                // 构建美观的 HTML 模板
                StringBuilder htmlBuilder = new StringBuilder();
                htmlBuilder.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">")
                        .append("<style>")
                        .append("body { font-family: 'Helvetica Neue', Arial, sans-serif; background-color: #f4f9f4; padding: 20px; color: #333; }")
                        .append(".container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05); }")
                        .append(".header { background-color: #4CAF50; color: white; padding: 30px 20px; text-align: center; }")
                        .append(".header h1 { margin: 0; font-size: 24px; letter-spacing: 1px; }")
                        .append(".content { padding: 30px; }")
                        .append(".greeting { font-size: 16px; margin-bottom: 20px; color: #555; }")
                        .append(".task-list { list-style: none; padding: 0; margin: 0; }")
                        .append(".task-item { background: #f9fbf9; border-left: 4px solid #81C784; margin-bottom: 12px; padding: 15px; border-radius: 4px; display: flex; justify-content: space-between; align-items: center; }")
                        .append(".task-name { font-weight: bold; font-size: 16px; color: #2E7D32; }")
                        .append(".task-date { font-size: 14px; color: #666; }")
                        .append(".footer { background-color: #f1f8f1; padding: 15px; text-align: center; font-size: 12px; color: #888; border-top: 1px solid #e0ebe0; }")
                        .append(".btn { display: inline-block; padding: 10px 20px; margin-top: 20px; background-color: #4CAF50; color: white; text-decoration: none; border-radius: 20px; font-weight: bold; }")
                        .append("</style></head><body>")
                        .append("<div class=\"container\">")
                        .append("<div class=\"header\"><h1>🌿 Greenly 养护日报</h1></div>")
                        .append("<div class=\"content\">")
                        .append("<div class=\"greeting\">亲爱的绿植主理人，您今日有 <strong>").append(dueSchedules.size())
                        .append("</strong> 项养护任务待处理，别让植物们等太久哦：</div>")
                        .append("<ul class=\"task-list\">");

                for (CareSchedule s : dueSchedules) {
                    htmlBuilder.append("<li class=\"task-item\">")
                            .append("<span class=\"task-name\">🌱 ").append(s.getTaskName()).append("</span>")
                            .append("<span class=\"task-date\">").append(s.getDueTime().toLocalDate()).append("</span>")
                            .append("</li>");
                }

                htmlBuilder.append("</ul>")
                        .append("</div>")
                        .append("<div class=\"footer\">感谢您对绿植的贴心呵护，愿您的生活充满生机！<br>© 2026 Greenly</div>")
                        .append("</div></body></html>");

                try {
                    emailService.sendHtmlMail(targetEmail, "【Greenly】今日养护任务提示 🌿", htmlBuilder.toString());
                    log.info("✅ 每日汇总邮件发送成功，用户: {}", userId);
                } catch (Exception e) {
                    log.error("❌ 发送邮件时发生错误，用户: {}, 邮箱: {}", userId, targetEmail, e);
                    // 不抛出异常，避免影响站内信的创建
                }
            } else {
                log.info("跳过邮件发送: shouldSendEmail={}, targetEmail={}", shouldSendEmail, targetEmail);
            }

        } catch (Exception e) {
            log.error("❌ 处理用户 {} 的通知时发生未预期错误", userId, e);
        }
        
        log.info("========== 提醒通知处理完成 ==========");
    }
}
