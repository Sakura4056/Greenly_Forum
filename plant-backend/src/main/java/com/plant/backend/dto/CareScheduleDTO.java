package com.plant.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class CareScheduleDTO {

    @Data
    public static class AddRequest {
        @NotNull
        private Long userId;
        // Optional if using Quick Add
        private Long plantId;
        private String plantSource; // OFFICIAL (官方), LOCAL (自定)
        @NotBlank
        private String taskName;

        // --- Quick Add Fields (Optional, used if plantId is null) ---
        private String plantName;
        private String genus;
        private String species;
        private String description;
        private Long officialPlantId;
        // -----------------------------------------------------------

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dueTime;

        private String recurrenceType = "NONE";
        private Integer recurrenceInterval = 0;

        private String reminderConfig;
    }

    @Data
    public static class UpdateRequest {
        @NotNull
        private Long id;

        private String taskName;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime dueTime;

        private String recurrenceType;
        private Integer recurrenceInterval;

        private String reminderConfig;

        // 状态更新 (标记为完成) 通常是分离或隐式的
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
        private Integer status;
        private Integer pageNum = 1;
        private Integer pageSize = 10;
    }
}
