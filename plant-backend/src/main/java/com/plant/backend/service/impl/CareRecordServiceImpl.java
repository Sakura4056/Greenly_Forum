package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plant.backend.dto.CareRecordDTO;
import com.plant.backend.dto.CareRecordDTO.StatQuery;
import com.plant.backend.entity.CareRecord;
import com.plant.backend.entity.CareSchedule;
import com.plant.backend.exception.BusinessException;
import com.plant.backend.mapper.CareRecordMapper;
import com.plant.backend.mapper.CareScheduleMapper;
import com.plant.backend.service.CareRecordService;
import com.plant.backend.service.CareRecordStatisticService;
import com.plant.backend.util.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 养护记录服务实现类
 * 负责养护记录的核心业务逻辑（添加、查询）
 * 统计功能已委托给 CareRecordStatisticService
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CareRecordServiceImpl extends ServiceImpl<CareRecordMapper, CareRecord> implements CareRecordService {

    private final CareScheduleMapper careScheduleMapper;
    private final CareRecordStatisticService statisticService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CareRecord add(CareRecordDTO.AddRequest request) {
        // 1. 验证 plantSource
        String plantSource = request.getPlantSource();
        if (plantSource == null || (!"LOCAL".equals(plantSource) && !"OFFICIAL".equals(plantSource))) {
            log.error("无效的植物来源: {}", plantSource);
            throw new BusinessException(400, "植物来源只能是 LOCAL 或 OFFICIAL");
        }
        
        // 2. 验证 operations JSON 格式
        try {
            String operations = request.getOperations();
            if (operations != null && !operations.isEmpty()) {
                // 尝试解析 JSON 验证格式
                new com.fasterxml.jackson.databind.ObjectMapper()
                    .readTree(operations);
            }
        } catch (Exception e) {
            log.error("operations JSON 格式错误: {}", request.getOperations(), e);
            throw new BusinessException(400, "养护操作数据格式错误");
        }
        
        CareRecord record = new CareRecord();
        record.setUserId(request.getUserId());
        record.setPlantId(request.getPlantId());
        record.setPlantSource(plantSource);
        record.setScheduleId(request.getScheduleId());
        record.setRecordTime(request.getRecordTime());
        record.setOperations(request.getOperations());
        record.setRemarks(request.getRemarks());

        save(record);

        // 如果关联了计划，标记为已完成并生成下次计划
        if (request.getScheduleId() != null) {
            try {
                handleScheduleCompletion(request);
            } catch (Exception e) {
                log.error("处理养护计划完成逻辑失败，scheduleId={}", request.getScheduleId(), e);
                // 不抛出异常，避免影响主流程
            }
        }

        log.info("用户{}添加养护记录成功，recordId={}, plantId={}, scheduleId={}", 
                request.getUserId(), record.getId(), request.getPlantId(), request.getScheduleId());
        return record;
    }

    @Override
    public Map<String, Object> statistic(StatQuery query, Long currentUserId) {
        if (query.getUserId() != null && !query.getUserId().equals(currentUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return statisticService.getChartData(query, currentUserId);
    }

    @Override
    public com.baomidou.mybatisplus.extension.plugins.pagination.Page<CareRecord> query(
            CareRecordDTO.Query query, Long currentUserId, String currentRole) {
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<CareRecord> page = 
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<CareRecord> wrapper = buildQueryWrapper(query, currentUserId, currentRole);
        return page(page, wrapper);
    }

    @Override
    public CareRecordDTO.StatsResponse getStats(Long userId, Long plantId, String range) {
        return statisticService.getStats(userId, plantId, range);
    }

    /**
     * 处理养护计划完成逻辑
     */
    private void handleScheduleCompletion(CareRecordDTO.AddRequest request) {
        CareSchedule schedule = careScheduleMapper.selectById(request.getScheduleId());
        if (schedule == null || !schedule.getUserId().equals(request.getUserId())) {
            return;
        }

        // 标记计划为已完成
        schedule.setStatus(1);
        careScheduleMapper.updateById(schedule);

        // 如果是循环计划，自动生成下次计划
        if (isRecurringSchedule(schedule)) {
            generateNextSchedule(schedule);
        }
    }

    /**
     * 判断是否为循环计划
     */
    private boolean isRecurringSchedule(CareSchedule schedule) {
        return schedule.getRecurrenceType() != null && 
               !"NONE".equalsIgnoreCase(schedule.getRecurrenceType()) &&
               schedule.getRecurrenceInterval() != null && 
               schedule.getRecurrenceInterval() > 0;
    }

    /**
     * 生成下次循环计划
     */
    private void generateNextSchedule(CareSchedule schedule) {
        CareSchedule nextSchedule = new CareSchedule();
        nextSchedule.setUserId(schedule.getUserId());
        nextSchedule.setPlantId(schedule.getPlantId());
        nextSchedule.setPlantSource(schedule.getPlantSource());
        nextSchedule.setTaskName(schedule.getTaskName());
        nextSchedule.setStatus(0); // 未完成
        nextSchedule.setRecurrenceType(schedule.getRecurrenceType());
        nextSchedule.setRecurrenceInterval(schedule.getRecurrenceInterval());
        nextSchedule.setReminderConfig(schedule.getReminderConfig());

        // 计算下次执行时间
        LocalDateTime nextDueTime = calculateNextDueTime(
            schedule.getDueTime(), 
            schedule.getRecurrenceType(), 
            schedule.getRecurrenceInterval()
        );
        nextSchedule.setDueTime(nextDueTime);
        
        careScheduleMapper.insert(nextSchedule);
        log.info("自动生成循环计划，原计划 ID: {}, 新计划 ID: {}", schedule.getId(), nextSchedule.getId());
    }

    /**
     * 计算下次执行时间
     */
    private LocalDateTime calculateNextDueTime(LocalDateTime currentDueTime, 
                                               String recurrenceType, 
                                               Integer interval) {
        switch (recurrenceType.toUpperCase()) {
            case "DAY":
                return currentDueTime.plusDays(interval);
            case "WEEK":
                return currentDueTime.plusWeeks(interval);
            case "MONTH":
                return currentDueTime.plusMonths(interval);
            case "YEAR":
                return currentDueTime.plusYears(interval);
            default:
                return currentDueTime;
        }
    }

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<CareRecord> buildQueryWrapper(
            CareRecordDTO.Query query, Long currentUserId, String currentRole) {
        
        LambdaQueryWrapper<CareRecord> wrapper = new LambdaQueryWrapper<>();

        // 权限控制
        if (!"ADMIN".equals(currentRole)) {
            if (query.getUserId() != null && !query.getUserId().equals(currentUserId)) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            if (query.getUserId() == null) {
                query.setUserId(currentUserId);
            }
        }

        // 构建查询条件
        if (query.getUserId() != null) {
            wrapper.eq(CareRecord::getUserId, query.getUserId());
        }
        if (query.getPlantId() != null) {
            wrapper.eq(CareRecord::getPlantId, query.getPlantId());
        }
        if (query.getPlantSource() != null) {
            wrapper.eq(CareRecord::getPlantSource, query.getPlantSource());
        }
        if (org.apache.commons.lang3.StringUtils.isNotBlank(query.getKeyword())) {
            wrapper.like(CareRecord::getRemarks, query.getKeyword());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(CareRecord::getRecordTime, query.getStartDate().atStartOfDay());
        }
        if (query.getEndDate() != null) {
            wrapper.le(CareRecord::getRecordTime, query.getEndDate().atTime(23, 59, 59));
        }

        // 按时间倒序排序
        wrapper.orderByDesc(CareRecord::getRecordTime);
        return wrapper;
    }
}
