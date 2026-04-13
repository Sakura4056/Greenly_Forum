package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.dto.CareScheduleDTO;
import com.plant.backend.entity.CareSchedule;
import com.plant.backend.exception.BusinessException;
import com.plant.backend.mapper.CareScheduleMapper;
import com.plant.backend.service.CareScheduleService;
import com.plant.backend.util.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

@Service
@RequiredArgsConstructor
public class CareScheduleServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<CareScheduleMapper, CareSchedule> implements CareScheduleService {

    private final CareScheduleMapper careScheduleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CareSchedule add(CareScheduleDTO.AddRequest request) {
        CareSchedule schedule = new CareSchedule();
        schedule.setUserId(request.getUserId());
        schedule.setPlantId(request.getPlantId());
        schedule.setPlantSource(request.getPlantSource());

        String plantName = request.getPlantName();
        String taskName = request.getTaskName();
        if (plantName != null && !taskName.startsWith(plantName)) {
            schedule.setTaskName(plantName + " " + taskName);
        } else {
            schedule.setTaskName(taskName);
        }
        schedule.setDueTime(request.getDueTime());
        schedule.setRecurrenceType(request.getRecurrenceType());
        schedule.setRecurrenceInterval(request.getRecurrenceInterval());
        schedule.setReminderConfig(request.getReminderConfig());
        schedule.setStatus(0);
        schedule.setCreateTime(LocalDateTime.now());

        careScheduleMapper.insert(schedule);
        return schedule;
    }

    @Override
    public CareSchedule updateSchedule(CareScheduleDTO.UpdateRequest request, Long userId) {
        CareSchedule schedule = careScheduleMapper.selectById(request.getId());
        if (schedule == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "计划不存在");
        }
        if (!schedule.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 状态更新：允许将待完成(0)或已逾期(2)的计划标记为已完成(1)
        if (request.getStatus() != null) {
            if (request.getStatus() == 1 && (schedule.getStatus() == 0 || schedule.getStatus() == 2)) {
                schedule.setStatus(1);
            } else if (request.getStatus() != schedule.getStatus()) {
                throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "不支持的状态变更");
            }
        }

        // 其他字段仅在待完成状态下可修改
        if (schedule.getStatus() != 0 && request.getStatus() == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "已完成或逾期的计划不可修改");
        }

        if (request.getTaskName() != null)
            schedule.setTaskName(request.getTaskName());
        if (request.getDueTime() != null)
            schedule.setDueTime(request.getDueTime());
        if (request.getRecurrenceType() != null)
            schedule.setRecurrenceType(request.getRecurrenceType());
        if (request.getRecurrenceInterval() != null)
            schedule.setRecurrenceInterval(request.getRecurrenceInterval());
        if (request.getReminderConfig() != null)
            schedule.setReminderConfig(request.getReminderConfig());

        careScheduleMapper.updateById(schedule);
        return schedule;
    }

    @Override
    public void deleteSchedule(Long scheduleId, Long userId) {
        CareSchedule schedule = careScheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "计划不存在");
        }
        if (!schedule.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        careScheduleMapper.deleteById(scheduleId);
    }

    @Override
    public Page<CareSchedule> query(CareScheduleDTO.Query query, Long currentUserId, String currentRole) {
        if (!"ADMIN".equals(currentRole)) {
            if (query.getUserId() != null && !query.getUserId().equals(currentUserId)) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            query.setUserId(currentUserId);
        }

        // 注意：逾期标记已移至定时任务（每天凌晨1点执行），不再在查询时实时更新
        // 这样可以避免每次查询都执行 UPDATE 操作，提升查询性能

        Page<CareSchedule> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<CareSchedule> wrapper = new LambdaQueryWrapper<>();
        if (query.getUserId() != null)
            wrapper.eq(CareSchedule::getUserId, query.getUserId());
        if (query.getPlantId() != null)
            wrapper.eq(CareSchedule::getPlantId, query.getPlantId());
        if (query.getStatus() != null)
            wrapper.eq(CareSchedule::getStatus, query.getStatus());
            
        if (StringUtils.isNotBlank(query.getKeyword()))
            wrapper.like(CareSchedule::getTaskName, query.getKeyword());
        if (query.getStartDate() != null)
            wrapper.ge(CareSchedule::getDueTime, query.getStartDate().atStartOfDay());
        if (query.getEndDate() != null)
            wrapper.le(CareSchedule::getDueTime, query.getEndDate().atTime(23, 59, 59));

        return careScheduleMapper.selectPage(page, wrapper);
    }

    @Override
    public Map<String, List<CareSchedule>> getCalendar(Integer year, Integer month, Long userId) {
        // 注意：逾期标记已移至定时任务，此处直接查询
        
        // 计算当月的开始和结束时间
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        
        // 查询当月的所有计划
        LambdaQueryWrapper<CareSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareSchedule::getUserId, userId)
                .between(CareSchedule::getDueTime, startOfMonth, endOfMonth);
        
        List<CareSchedule> schedules = careScheduleMapper.selectList(wrapper);
        
        // 按日期分组
        Map<String, List<CareSchedule>> calendarMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (CareSchedule schedule : schedules) {
            String dateStr = schedule.getDueTime().format(formatter);
            calendarMap.computeIfAbsent(dateStr, k -> new ArrayList<>()).add(schedule);
        }
        
        return calendarMap;
    }
}
