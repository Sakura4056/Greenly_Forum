package com.plant.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class CareRecordDTO {

    @Data
    public static class AddRequest {
        // userId 由后端从 Token 中提取，前端无需传递
        private Long userId;
        
        @NotNull(message = "植物ID不能为空")
        private Long plantId;
        
        @NotNull(message = "植物来源不能为空")
        private String plantSource;
        
        private Long scheduleId;
        
        @NotNull(message = "养护时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime recordTime;
        
        @NotNull(message = "养护操作不能为空")
        private String operations; // 来自前端的 JSON 字符串
        
        private String remarks;
    }

    @Data
    public static class StatQuery {
        private Long userId;
        private Long plantId;
        private String plantSource;
        private Integer days; // 30 or 90
    }

    @Data
    public static class Query {
        private String keyword;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private java.time.LocalDate startDate;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private java.time.LocalDate endDate;

        private Long userId;
        private Long plantId;
        private String plantSource;
        private Integer pageNum = 1;
        private Integer pageSize = 10;
    }
    
    @Data
    public static class StatResponse {
        // 基于 "趋势图数据" 定义结构
        // 例如 dates: [], water: [], fertilizer: []
        // 或结构化数据
        private Object chartData; 
    }
    
    /**
     * 养护统计响应（功能 3 新增）
     */
    @Data
    public static class StatsResponse {
        private Integer totalRecords;
        private Map<String, Integer> byType;
        private List<PlantStat> byPlant;
        private List<DailyTrend> dailyTrend;
        private Integer streakDays;
        
        @Data
        public static class PlantStat {
            private String plantName;
            private Integer count;
        }
        
        @Data
        public static class DailyTrend {
            private String date;
            private Integer count;
        }
    }
}
