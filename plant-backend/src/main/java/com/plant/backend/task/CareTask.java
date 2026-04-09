package com.plant.backend.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.plant.backend.entity.CareSchedule;

import com.plant.backend.entity.ReminderConfig;
import com.plant.backend.entity.User;
import com.plant.backend.mapper.CareScheduleMapper;
import com.plant.backend.mapper.ReminderConfigMapper;
import com.plant.backend.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.plant.backend.service.NotificationService;
import com.plant.backend.service.SmartWateringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 养护任务定时调度器
 * <p>
 * 负责扫描逾期的养护计划、发送智能浇水提醒等定时任务
 * </p>
 *
 * @author Greenly Team
 * @date 2026-04-05
 * @version 3.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CareTask {

    private final CareScheduleMapper careScheduleMapper;
    private final NotificationService notificationService;
    private final ReminderConfigMapper reminderConfigMapper;
    private final SmartWateringService smartWateringService;
    private final UserMapper userMapper;

    /**
     * 每小时执行一次：扫描到期任务并发送提醒
     * Cron 表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void scanDueSchedules() {
        scanDueSchedules(false);
    }

    /**
     * 每天凌晨 1 点执行：标记逾期任务
     * 将状态为待完成（0）且到期时间已过期的任务标记为逾期（2）
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void markOverdueSchedules() {
        log.info("========== 开始执行逾期标记任务 ==========");

        LocalDateTime now = LocalDateTime.now();

        // 更新所有已过期的待完成任务
        LambdaUpdateWrapper<CareSchedule> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CareSchedule::getStatus, 0)  // 待完成
                .lt(CareSchedule::getDueTime, now)  // 到期时间 < 当前时间
                .set(CareSchedule::getStatus, 2);  // 标记为逾期

        int updatedCount = careScheduleMapper.update(null, wrapper);

        log.info("逾期标记任务完成，共标记 {} 个任务为逾期状态", updatedCount);
        log.info("========== 逾期标记任务结束 ==========");
    }

    /**
     * 扫描到期任务并发送提醒（支持手动触发测试）
     *
     * @param isManualTest 是否为手动测试触发
     */
    public void scanDueSchedules(boolean isManualTest) {
        log.info("正在扫描到期的养护计划（每日汇总）... 手动测试: {}", isManualTest);

        // 查找截止时间 <= 今天结束 (23:59:59) 且未完成的任务
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        LambdaQueryWrapper<CareSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareSchedule::getStatus, 0)
                .le(CareSchedule::getDueTime, todayEnd);

        List<CareSchedule> dueList = careScheduleMapper.selectList(wrapper);

        if (dueList.isEmpty()) {
            log.info("没有待处理的到期任务");
            return;
        }

        log.info("找到 {} 个待处理的到期任务", dueList.size());

        // 按用户分组
        java.util.Map<Long, List<CareSchedule>> userMap = dueList.stream()
                .collect(java.util.stream.Collectors.groupingBy(CareSchedule::getUserId));

        int currentHour = LocalDateTime.now().getHour();
        ObjectMapper mapper = new ObjectMapper();

        // 遍历每个用户发送汇总
        userMap.forEach((userId, schedules) -> {
            // 检查用户的配置
            LambdaQueryWrapper<ReminderConfig> configWrapper = new LambdaQueryWrapper<>();
            configWrapper.eq(ReminderConfig::getUserId, userId);
            ReminderConfig config = reminderConfigMapper.selectOne(configWrapper);

            int targetHour = 9; // 默认早上9点

            if (config != null && config.getSceneConfig() != null) {
                try {
                    JsonNode node = mapper.readTree(config.getSceneConfig());
                    if (node.has("summaryTime")) {
                        String timeStr = node.get("summaryTime").asText(); // e.g. "09:00"
                        if (timeStr != null && timeStr.length() >= 2) {
                            targetHour = Integer.parseInt(timeStr.substring(0, 2));
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse summaryTime for user {}", userId, e);
                }
            }

            // 如果当前小时等于目标小时，或是手动触发的测试，则发送
            if (currentHour == targetHour || isManualTest) {
                notificationService.sendCareReminderSummary(userId, schedules);
            }
        });
    }

    /**
     * 每天早上 8 点执行：发送智能浇水提醒
     * 根据植物种类、养护记录、天气预报计算最佳浇水时间
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendSmartWateringReminders() {
        log.info("========== 开始执行智能浇水提醒任务 ==========");

        // 获取所有用户
        List<User> users = userMapper.selectList(null);

        for (User user : users) {
            try {
                smartWateringService.sendSmartWateringReminders(user.getUserId());
            } catch (Exception e) {
                log.error("发送用户 {} 的浇水提醒失败", user.getUserId(), e);
            }
        }

        log.info("========== 智能浇水提醒任务结束 ==========");
    }
}
