package com.plant.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class PhotoDTO {

    @Data
    public static class UploadRequest {
        @NotNull
        private Long userId;
        
        // plantId 可以为 null（新增植物时先上传封面）
        private Long plantId;
        
        @NotNull
        private String plantSource;

        private Integer isPublic; // 0/1, 默认为 0
        private String remarks;

        // 分片信息
        private Integer chunk; // 当前索引 (基于0)
        private Integer chunks; // 总分片数
        private String fileName; // 原始文件名

        // 文件部分 (通常由 Controller @RequestParam 处理, 或 model attribute)
        // DTO 字段主要用于校验逻辑
    }

    @Data
    public static class UpdateRequest {
        @NotNull
        private Long id;
        private Integer isPublic;
        private String remarks;
    }

    @Data
    public static class Query {
        private String keyword;
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
        private java.time.LocalDate startDate;
        @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
        private java.time.LocalDate endDate;

        private Long userId;
        private Long plantId;
        private String plantSource;
        private Integer isPublic;
        private Integer pageNum = 1;
        private Integer pageSize = 10;
    }
}
