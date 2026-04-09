package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛评论实体类
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@Data
@TableName("forum_comment")
public class ForumComment {

    @TableId(type = IdType.AUTO)
    private Long commentId;

    private Long postId;

    private Long userId;

    private Long parentId;

    private Long replyToUserId;

    private String content;

    private Integer likeCount;

    private Integer status;

    @TableLogic
    @JsonIgnore
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
