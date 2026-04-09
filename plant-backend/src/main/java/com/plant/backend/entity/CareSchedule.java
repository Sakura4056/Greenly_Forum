package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("care_schedule")
public class CareSchedule {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long plantId;
    private String plantSource; // OFFICIAL (官方), LOCAL (自定)

    private String taskName;
    private LocalDateTime dueTime;

    // 0: 未完成, 1: 已完成, 2: 逾期
    private Integer status;

    private String recurrenceType; // NONE, DAY, MONTH, YEAR
    private Integer recurrenceInterval;

    private String reminderConfig; // JSON 或简单字符串

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
