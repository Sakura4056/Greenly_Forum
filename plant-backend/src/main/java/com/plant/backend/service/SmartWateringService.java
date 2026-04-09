package com.plant.backend.service;

import java.util.List;
import java.util.Map;

/**
 * 智能浇水提醒服务接口
 * <p>
 * 根据植物种类、养护记录、天气情况计算最佳浇水时间
 * </p>
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
public interface SmartWateringService {

    /**
     * 计算植物的最佳浇水时间
     *
     * @param myPlantId 我的植物ID
     * @return 浇水建议（包含下次浇水时间、原因等）
     */
    Map<String, Object> calculateWateringSchedule(Long myPlantId);

    /**
     * 获取用户所有植物的浇水提醒列表
     *
     * @param userId 用户ID
     * @return 浇水提醒列表
     */
    List<Map<String, Object>> getWateringReminders(Long userId);

    /**
     * 发送智能浇水提醒
     * <p>
     * 检查所有需要浇水的植物，发送通知给用户
     * </p>
     *
     * @param userId 用户ID
     */
    void sendSmartWateringReminders(Long userId);
}
