package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统公告实体类
 * 
 * @author Greenly Team
 * @date 2026-04-05
 */
@Data
@TableName("announcement")
public class Announcement {
    
    /**
     * 公告 ID（主键自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    @TableField
    private String title;

    /**
     * 公告内容
     */
    @TableField
    private String content;

    /**
     * 发布人 ID（关联 sys_user.user_id）
     */
    @TableField
    private Long publisherId;

    /**
     * 发布时间
     */
    @TableField
    private LocalDateTime publishTime;

    /**
     * 状态：0-草稿, 1-已发布
     */
    @TableField
    private Integer status;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识：0 未删除 / 1 已删除
     */
    @TableLogic
    private Integer deleted;
}
