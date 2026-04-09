package com.plant.backend.service;

import com.plant.backend.dto.CareRecordDTO;

/**
 * 养护记录统计服务接口
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
public interface CareRecordStatisticService {
    
    /**
     * 获取养护统计数据
     * 
     * @param userId 用户 ID
     * @param plantId 植物 ID（可选）
     * @param range 时间范围（如：7d, 30d, 90d）
     * @return 统计数据
     */
    CareRecordDTO.StatsResponse getStats(Long userId, Long plantId, String range);
    
    /**
     * 获取统计图表数据
     * 
     * @param query 查询条件
     * @param currentUserId 当前用户 ID
     * @return 图表数据
     */
    java.util.Map<String, Object> getChartData(CareRecordDTO.StatQuery query, Long currentUserId);
}
