package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛帖子实体类
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@Data
@TableName("forum_post")
public class ForumPost {

    @TableId(type = IdType.AUTO)
    private Long postId;

    private Long userId;
    
    private Long categoryId;


    private String title;

    private String content;

    private Integer viewCount;

    private Integer replyCount;

    private Integer likeCount;

    private Integer isTop;

    private Integer isEssence;

    private Integer status;

    @TableLogic
    @JsonIgnore
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
