package com.plant.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

public class ForumDTO {

    @Data
    @Schema(description = "发帖请求")
    public static class CreatePostRequest {

        @NotNull(message = "分类ID不能为空")
        @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
        private Long categoryId;

        @NotBlank(message = "标题不能为空")
        @Size(max = 200, message = "标题不能超过200字符")
        @Schema(description = "帖子标题", requiredMode = Schema.RequiredMode.REQUIRED)
        private String title;

        @NotBlank(message = "内容不能为空")
        @Size(max = 5000, message = "内容不能超过5000字符")
        @Schema(description = "帖子内容", requiredMode = Schema.RequiredMode.REQUIRED)
        private String content;
    }

    @Data
    @Schema(description = "编辑帖子请求")
    public static class EditPostRequest {
        @Schema(description = "分类ID")
        private Long categoryId;

        @NotBlank(message = "标题不能为空")
        @Size(max = 200, message = "标题不能超过200字符")
        @Schema(description = "帖子标题", requiredMode = Schema.RequiredMode.REQUIRED)
        private String title;

        @NotBlank(message = "内容不能为空")
        @Size(max = 5000, message = "内容不能超过5000字符")
        @Schema(description = "帖子内容", requiredMode = Schema.RequiredMode.REQUIRED)
        private String content;
    }

    @Data
    @Schema(description = "帖子响应")
    public static class PostResponse {
        private Long postId;
        private Long userId;
        private String username;
        private String nickname;
        private String avatar;
        private Long categoryId;
        private String categoryName;
        private String title;
        private String content;
        private Integer viewCount;
        private Integer replyCount;
        private Integer likeCount;
        private Boolean isLiked;
        private Integer isTop;
        private Integer isEssence;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    @Data
    @Schema(description = "发表评论请求")
    public static class CreateCommentRequest {
        @NotNull(message = "帖子ID不能为空")
        private Long postId;
        private Long parentId;
        private Long replyToUserId;
        @NotBlank(message = "评论内容不能为空")
        @Size(max = 2000)
        private String content;
    }

    @Data
    @Schema(description = "编辑评论请求")
    public static class EditCommentRequest {
        @NotBlank(message = "评论内容不能为空")
        @Size(max = 2000)
        private String content;
    }

    @Data
    @Schema(description = "评论响应")
    public static class CommentResponse {
        private Long commentId;
        private Long postId;
        private Long userId;
        private String username;
        private String nickname;
        private String avatar;
        private Long parentId;
        private Long replyToUserId;
        private String replyToUsername;
        private String content;
        private Integer likeCount;
        private Boolean isLiked;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    @Data
    @Schema(description = "帖子列表查询请求")
    public static class PostQueryRequest {
        private Integer pageNum = 1;
        private Integer pageSize = 10;
        private String sortBy = "latest";
        private Long categoryId;
    }

    @Data
    @Schema(description = "点赞请求")
    public static class LikeRequest {
        @NotBlank(message = "目标类型不能为空")
        private String targetType;
        @NotNull(message = "目标ID不能为空")
        private Long targetId;
    }
}

