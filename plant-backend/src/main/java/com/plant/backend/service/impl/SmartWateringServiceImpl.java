package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plant.backend.entity.CareRecord;
import com.plant.backend.entity.MyPlant;
import com.plant.backend.mapper.CareRecordMapper;
import com.plant.backend.mapper.MyPlantMapper;
import com.plant.backend.service.SmartWateringService;
import com.plant.backend.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 智能浇水提醒服务实现类
 * <p>
 * 基于植物种类、最近浇水记录、天气预报计算最佳浇水时间
 * </p>
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartWateringServiceImpl implements SmartWateringService {

    private final MyPlantMapper myPlantMapper;
    private final CareRecordMapper careRecordMapper;
    private final WeatherService weatherService;

    // 不同植物类型的默认浇水间隔（天）
    private static final Map<String, Integer> WATERING_INTERVALS = new HashMap<>();
    static {
        WATERING_INTERVALS.put("多肉植物", 7);
        WATERING_INTERVALS.put("仙人掌", 10);
        WATERING_INTERVALS.put("观叶植物", 3);
        WATERING_INTERVALS.put("开花植物", 2);
        WATERING_INTERVALS.put("草本植物", 2);
        WATERING_INTERVALS.put("default", 3);
    }

    @Override
    public Map<String, Object> calculateWateringSchedule(Long myPlantId) {
        log.info("计算植物浇水计划: myPlantId={}", myPlantId);

        try {
            MyPlant myPlant = myPlantMapper.selectById(myPlantId);
            if (myPlant == null) {
                log.warn("植物不存在: myPlantId={}", myPlantId);
                return Collections.emptyMap();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("myPlantId", myPlantId);
            result.put("plantName", myPlant.getNickname() != null ? myPlant.getNickname() : "我的植物");

            // 1. 获取植物类型和默认浇水间隔
            String plantType = myPlant.getNickname() != null ? myPlant.getNickname() : "default"; // 简化：使用昵称作为类型
            int defaultInterval = WATERING_INTERVALS.getOrDefault(plantType, WATERING_INTERVALS.get("default"));

            // 2. 查询最近一次浇水记录
            LambdaQueryWrapper<CareRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CareRecord::getPlantId, myPlantId)
                    .like(CareRecord::getOperations, "\"water\"")  // 查找包含 "water" 键的记录
                    .orderByDesc(CareRecord::getRecordTime)
                    .last("LIMIT 1");
            CareRecord lastWatering = careRecordMapper.selectOne(wrapper);

            LocalDate lastWaterDate = lastWatering != null ? lastWatering.getRecordTime().toLocalDate() : null;
            LocalDate today = LocalDate.now();

            // 3. 计算下次浇水日期
            LocalDate nextWaterDate;
            if (lastWaterDate != null) {
                nextWaterDate = lastWaterDate.plusDays(defaultInterval);
            } else {
                // 没有浇水记录，建议今天浇水
                nextWaterDate = today;
            }

            long daysUntilNextWater = ChronoUnit.DAYS.between(today, nextWaterDate);

            // 4. 结合天气调整建议
            try {
                Map<String, Object> weatherAdvice = weatherService.shouldWaterToday("北京"); // 简化：固定城市
                boolean shouldWaterToday = (boolean) weatherAdvice.getOrDefault("shouldWater", true);

                // 如果天气不适合浇水，推迟1-2天
                if (!shouldWaterToday && daysUntilNextWater <= 0) {
                    nextWaterDate = nextWaterDate.plusDays(2);
                    daysUntilNextWater = ChronoUnit.DAYS.between(today, nextWaterDate);
                }

                result.put("weatherAdvice", weatherAdvice);
            } catch (Exception e) {
                log.warn("获取天气建议失败，使用默认值", e);
                result.put("weatherAdvice", Map.of("shouldWater", true, "reasons", Collections.emptyList()));
            }

            result.put("lastWaterDate", lastWaterDate);
            result.put("nextWaterDate", nextWaterDate);
            result.put("daysUntilNextWater", daysUntilNextWater);
            result.put("defaultInterval", defaultInterval);
            result.put("urgency", calculateUrgency(daysUntilNextWater));

            return result;
        } catch (Exception e) {
            log.error("计算浇水计划失败: myPlantId={}", myPlantId, e);
            throw new RuntimeException("计算浇水计划失败", e);
        }
    }

    @Override
    public List<Map<String, Object>> getWateringReminders(Long userId) {
        log.info("获取用户浇水提醒: userId={}", userId);

        try {
            // 查询用户的所有植物
            LambdaQueryWrapper<MyPlant> plantWrapper = new LambdaQueryWrapper<>();
            plantWrapper.eq(MyPlant::getUserId, userId);
            List<MyPlant> plants = myPlantMapper.selectList(plantWrapper);

            if (plants == null || plants.isEmpty()) {
                log.info("用户 {} 没有植物", userId);
                return Collections.emptyList();
            }

            List<Map<String, Object>> reminders = new ArrayList<>();

            for (MyPlant plant : plants) {
                try {
                    Map<String, Object> schedule = calculateWateringSchedule(plant.getId());
                    long daysUntil = (long) schedule.get("daysUntilNextWater");

                    // 只返回需要关注的植物（3天内需要浇水或已逾期）
                    if (daysUntil <= 3) {
                        reminders.add(schedule);
                    }
                } catch (Exception e) {
                    log.error("计算植物 {} 的浇水计划失败", plant.getId(), e);
                }
            }

            // 按紧急程度排序
            reminders.sort(Comparator.comparingLong(r -> (long) r.get("daysUntilNextWater")));

            return reminders;
        } catch (Exception e) {
            log.error("获取浇水提醒失败: userId={}", userId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public void sendSmartWateringReminders(Long userId) {
        log.info("发送智能浇水提醒: userId={}", userId);

        List<Map<String, Object>> reminders = getWateringReminders(userId);

        if (reminders.isEmpty()) {
            log.info("没有需要浇水的植物");
            return;
        }

        // 构建提醒消息
        StringBuilder message = new StringBuilder();
        message.append("您有 ").append(reminders.size()).append(" 株植物需要浇水：\n\n");

        for (Map<String, Object> reminder : reminders) {
            String plantName = (String) reminder.get("plantName");
            long daysUntil = (long) reminder.get("daysUntilNextWater");
            String urgency = (String) reminder.get("urgency");

            if (daysUntil < 0) {
                message.append("⚠️ ").append(plantName).append(" - 已逾期 ")
                        .append(Math.abs(daysUntil)).append(" 天（").append(urgency).append("）\n");
            } else if (daysUntil == 0) {
                message.append("💧 ").append(plantName).append(" - 今天需要浇水（").append(urgency).append("）\n");
            } else {
                message.append("🌱 ").append(plantName).append(" - ").append(daysUntil)
                        .append(" 天后需要浇水\n");
            }
        }

        // 发送通知（站内信）
        // 注意：这里简化处理，实际应调用 NotificationService 创建提醒
        log.info("浇水提醒消息:\n{}", message.toString());
    }

    /**
     * 计算紧急程度
     */
    private String calculateUrgency(long daysUntil) {
        if (daysUntil < 0) {
            return "urgent"; // 已逾期
        } else if (daysUntil == 0) {
            return "today"; // 今天
        } else if (daysUntil <= 1) {
            return "soon"; // 即将
        } else {
            return "normal"; // 正常
        }
    }
}
