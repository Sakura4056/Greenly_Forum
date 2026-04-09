package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 论坛点赞实体类
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@Data
@TableName("forum_like")
public class ForumLike {

    @TableId(type = IdType.AUTO)
    private Long likeId;

    private Long userId;

    private String targetType;

    private Long targetId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
