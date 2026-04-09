package com.plant.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告 DTO 定义
 * 
 * @author Greenly Team
 * @date 2026-04-05
 */
public class AnnouncementDTO {

    /**
     * 发布公告请求 DTO
     */
    @Data
    @Schema(description = "发布公告请求")
    public static class PublishRequest {
        
        @Schema(description = "公告标题", example = "系统维护通知", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "公告标题不能为空")
        private String title;

        @Schema(description = "公告内容", example = "系统将于今晚进行维护...", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "公告内容不能为空")
        private String content;
    }

    /**
     * 公告响应 DTO
     */
    @Data
    @Schema(description = "公告响应")
    public static class AnnouncementResponse {
        
        @Schema(description = "公告 ID")
        private Long id;

        @Schema(description = "公告标题")
        private String title;

        @Schema(description = "公告内容")
        private String content;

        @Schema(description = "发布人 ID")
        private Long publisherId;

        @Schema(description = "发布时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime publishTime;

        @Schema(description = "状态：0-草稿, 1-已发布")
        private Integer status;
    }
}
