package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reminder")
public class Reminder {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    
    private String scene; // careSchedule (养护计划), plantAudit (植物审核), announcement (公告)
    private Long businessId; // 关联业务 ID
    
    private String title;
    private String content;
    
    private Integer isRead; // 0: 未读, 1: 已读

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
