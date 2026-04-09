package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.plant.backend.dto.CareRecordDTO;
import com.plant.backend.entity.CareRecord;
import com.plant.backend.entity.MyPlant;
import com.plant.backend.mapper.CareRecordMapper;
import com.plant.backend.mapper.MyPlantMapper;
import com.plant.backend.service.CareRecordStatisticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 养护记录统计服务实现类
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CareRecordStatisticServiceImpl implements CareRecordStatisticService {

    private final CareRecordMapper careRecordMapper;
    private final MyPlantMapper myPlantMapper;
    private final ObjectMapper objectMapper;

    @Override
    public CareRecordDTO.StatsResponse getStats(Long userId, Long plantId, String range) {
        // 解析时间范围：7d, 30d, 90d 或自定义
        int days = parseDaysFromRange(range);
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        LocalDateTime endTime = LocalDateTime.now();

        // 基础查询条件
        LambdaQueryWrapper<CareRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareRecord::getUserId, userId)
                .ge(CareRecord::getRecordTime, startTime)
                .le(CareRecord::getRecordTime, endTime);

        if (plantId != null) {
            wrapper.eq(CareRecord::getPlantId, plantId);
        }

        List<CareRecord> records = careRecordMapper.selectList(wrapper);

        CareRecordDTO.StatsResponse response = new CareRecordDTO.StatsResponse();
        
        // 1. 总记录数
        response.setTotalRecords(records.size());

        // 2. 按养护类型统计
        response.setByType(statisticsByType(records));

        // 3. 按植物统计
        response.setByPlant(statisticsByPlant(records));

        // 4. 每日趋势
        response.setDailyTrend(calculateDailyTrend(records));

        // 5. 连续天数
        response.setStreakDays(calculateStreakDays(userId));

        return response;
    }

    @Override
    public Map<String, Object> getChartData(CareRecordDTO.StatQuery query, Long currentUserId) {
        int days = (query.getDays() != null && query.getDays() == 90) ? 90 : 30;
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<CareRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CareRecord::getUserId, currentUserId)
                .ge(CareRecord::getRecordTime, startTime);

        if (query.getPlantId() != null) {
            wrapper.eq(CareRecord::getPlantId, query.getPlantId());
            if (query.getPlantSource() != null) {
                wrapper.eq(CareRecord::getPlantSource, query.getPlantSource());
            }
        }

        List<CareRecord> records = careRecordMapper.selectList(wrapper);
        return aggregateChartData(records);
    }

    /**
     * 解析时间范围字符串
     */
    private int parseDaysFromRange(String range) {
        if (range == null || range.isEmpty()) {
            return 30;
        }
        if (range.endsWith("d")) {
            try {
                return Integer.parseInt(range.substring(0, range.length() - 1));
            } catch (NumberFormatException e) {
                log.warn("解析时间范围失败：{}", range);
            }
        }
        return 30;
    }

    /**
     * 按养护类型统计
     */
    private Map<String, Integer> statisticsByType(List<CareRecord> records) {
        Map<String, Integer> byType = new HashMap<>();
        
        for (CareRecord record : records) {
            try {
                JsonNode root = objectMapper.readTree(record.getOperations());
                if (root.has("water") && root.get("water").asDouble() > 0) {
                    byType.merge("浇水", 1, (a, b) -> a + b);
                }
                if (root.has("fertilizer") && root.get("fertilizer").asDouble() > 0) {
                    byType.merge("施肥", 1, (a, b) -> a + b);
                }
                if (root.has("pruning") && root.get("pruning").asDouble() > 0) {
                    byType.merge("修剪", 1, (a, b) -> a + b);
                }
                if (root.has("pestControl") && root.get("pestControl").asDouble() > 0) {
                    byType.merge("除虫", 1, (a, b) -> a + b);
                }
                if (root.has("repot") && root.get("repot").asBoolean()) {
                    byType.merge("换盆", 1, (a, b) -> a + b);
                }
                if (root.has("other") && !root.get("other").asText().isEmpty()) {
                    byType.merge("其他", 1, (a, b) -> a + b);
                }
            } catch (JsonProcessingException e) {
                log.warn("解析记录{}的操作 JSON 失败", record.getId());
            }
        }
        
        return byType;
    }

    /**
     * 按植物统计
     */
    private List<CareRecordDTO.StatsResponse.PlantStat> statisticsByPlant(List<CareRecord> records) {
        Map<Long, Integer> plantCountMap = new HashMap<>();
        for (CareRecord record : records) {
            plantCountMap.merge(record.getPlantId(), 1, (a, b) -> a + b);
        }
        
        List<CareRecordDTO.StatsResponse.PlantStat> byPlant = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : plantCountMap.entrySet()) {
            MyPlant plant = myPlantMapper.selectById(entry.getKey());
            if (plant != null) {
                CareRecordDTO.StatsResponse.PlantStat stat = new CareRecordDTO.StatsResponse.PlantStat();
                stat.setPlantName(plant.getNickname());
                stat.setCount(entry.getValue());
                byPlant.add(stat);
            }
        }
        
        // 按次数降序排序
        byPlant.sort((a, b) -> b.getCount() - a.getCount());
        return byPlant;
    }

    /**
     * 计算每日趋势
     */
    private List<CareRecordDTO.StatsResponse.DailyTrend> calculateDailyTrend(List<CareRecord> records) {
        Map<String, Integer> dailyMap = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (CareRecord record : records) {
            String dateKey = record.getRecordTime().format(formatter);
            dailyMap.merge(dateKey, 1, (a, b) -> a + b);
        }
        
        List<CareRecordDTO.StatsResponse.DailyTrend> dailyTrend = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dailyMap.entrySet()) {
            CareRecordDTO.StatsResponse.DailyTrend trend = new CareRecordDTO.StatsResponse.DailyTrend();
            trend.setDate(entry.getKey());
            trend.setCount(entry.getValue());
            dailyTrend.add(trend);
        }
        
        return dailyTrend;
    }

    /**
     * 聚合图表数据
     */
    private Map<String, Object> aggregateChartData(List<CareRecord> records) {
        Map<String, Map<String, Double>> aggMap = new TreeMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (CareRecord r : records) {
            String dateStr = r.getRecordTime().format(formatter);
            aggMap.putIfAbsent(dateStr, new HashMap<>());
            Map<String, Double> dayStats = aggMap.get(dateStr);

            dayStats.merge("count", 1.0, (a, b) -> a + b);

            // 解析 JSON 操作
            try {
                JsonNode root = objectMapper.readTree(r.getOperations());
                if (root.has("water")) {
                    dayStats.merge("water", root.get("water").asDouble(), (a, b) -> a + b);
                }
                if (root.has("fertilizer")) {
                    dayStats.merge("fertilizer", root.get("fertilizer").asDouble(), (a, b) -> a + b);
                }
                if (root.has("pruning")) {
                    dayStats.merge("pruning", root.get("pruning").asDouble(), (a, b) -> a + b);
                }
                if (root.has("pestControl")) {
                    dayStats.merge("pestControl", root.get("pestControl").asDouble(), (a, b) -> a + b);
                }
            } catch (JsonProcessingException e) {
                log.warn("解析记录{}的操作 JSON 失败", r.getId());
            }
        }

        // 转换为数组用于图表
        List<String> dates = new ArrayList<>(aggMap.keySet());
        List<Double> waterData = new ArrayList<>();
        List<Double> fertilizerData = new ArrayList<>();
        List<Double> pruningData = new ArrayList<>();
        List<Double> pestControlData = new ArrayList<>();
        List<Double> countData = new ArrayList<>();

        for (String date : dates) {
            Map<String, Double> day = aggMap.get(date);
            waterData.add(day.getOrDefault("water", 0.0));
            fertilizerData.add(day.getOrDefault("fertilizer", 0.0));
            pruningData.add(day.getOrDefault("pruning", 0.0));
            pestControlData.add(day.getOrDefault("pestControl", 0.0));
            countData.add(day.getOrDefault("count", 0.0));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("water", waterData);
        result.put("fertilizer", fertilizerData);
        result.put("pruning", pruningData);
        result.put("pestControl", pestControlData);
        result.put("totalRecords", countData);

        return result;
    }

    /**
     * 计算连续打卡天数
     */
    private int calculateStreakDays(Long userId) {
        LocalDate today = LocalDate.now();
        int streak = 0;
        
        // 从今天开始往前检查
        for (int i = 0; i < 365; i++) {
            LocalDate checkDate = today.minusDays(i);
            
            LambdaQueryWrapper<CareRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CareRecord::getUserId, userId)
                    .ge(CareRecord::getRecordTime, checkDate.atStartOfDay())
                    .lt(CareRecord::getRecordTime, checkDate.plusDays(1).atStartOfDay());
            
            long count = careRecordMapper.selectCount(wrapper);
            
            if (count > 0) {
                streak++;
            } else if (i > 0) {
                // 不是今天，且没有记录，中断
                break;
            }
        }
        
        return streak;
    }
}
