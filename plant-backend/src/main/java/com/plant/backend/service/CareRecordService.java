package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.plant.backend.dto.CareRecordDTO;
import com.plant.backend.entity.CareRecord;

import java.util.Map;

public interface CareRecordService extends IService<CareRecord> {
    CareRecord add(CareRecordDTO.AddRequest request);
    Map<String, Object> statistic(CareRecordDTO.StatQuery query, Long currentUserId);
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<CareRecord> query(CareRecordDTO.Query query, Long currentUserId, String currentRole);
    
    /**
     * 获取养护统计详情（功能 3 新增）
     */
    CareRecordDTO.StatsResponse getStats(Long userId, Long plantId, String range);
}
