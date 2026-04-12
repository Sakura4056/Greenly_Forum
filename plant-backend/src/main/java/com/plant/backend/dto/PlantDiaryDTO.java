package com.plant.backend.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.List;

/**
 * Plant Diary DTOs
 */
public class PlantDiaryDTO {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreateRequest {
        private Long plantId;
        private String title;
        private String content;
        private String mood;
        private String weather;
        private List<Long> photoIds;
        private LocalDate diaryDate;
    }

    @Data
    public static class UpdateRequest {
        private Long id;
        private String title;
        private String content;
        private String mood;
        private String weather;
        private List<Long> photoIds;
    }

    @Data
    public static class DiaryResponse {
        private Long id;
        private Long userId;
        private Long plantId;
        private String plantNickname;
        private String title;
        private String content;
        private String mood;
        private String weather;
        private List<Long> photoIds;
        private List<String> photoUrls;
        private String diaryDate;
        private String createTime;
    }

    @Data
    public static class QueryRequest {
        private Long plantId;
        private String startDate;
        private String endDate;
        private String mood;
        private Integer pageNum = 1;
        private Integer pageSize = 10;
    }
}
