package com.plant.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.plant.backend.entity.MyPlant;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

public class MyPlantDTO {

    @Data
    public static class AddRequest {
        private Long officialId;
        
        @NotBlank(message = "昵称不能为空")
        private String nickname;
        
        private String location;
        private String source;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate acquiredDate;
        
        private String status;
        private String coverUrl;
        private String notes;
    }

    @Data
    public static class UpdateRequest {
        private Long officialId;
        
        @NotBlank(message = "昵称不能为空")
        private String nickname;
        
        private String location;
        private String source;
        
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate acquiredDate;
        
        private String status;
        private String coverUrl;
        private String notes;
    }

    @Data
    public static class QueryRequest {
        private String keyword;
        private String status;
        
        // For standard pagination
        private Integer current = 1;
        private Integer size = 10;
    }

    // Detail Response matching module 2 requirements
    @Data
    public static class DetailResponse {
        private MyPlant plant;
        
        // Use Object for now as we haven't mapped CareSchedule/CareRecord/PlantPhoto perfectly yet
        private List<?> recentSchedules;  
        private List<?> recentRecords;
        private List<?> recentPhotos;
        
        private Long totalSchedules;
        private Long totalRecords;
        private Long totalPhotos;
    }
}
