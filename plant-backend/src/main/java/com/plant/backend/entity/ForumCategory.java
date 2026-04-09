package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_category")
public class ForumCategory {

    @TableId(type = IdType.AUTO)
    private Long categoryId;

    private String name;

    private String description;

    private String icon;

    private Integer sortOrder;

    private Integer postCount;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

