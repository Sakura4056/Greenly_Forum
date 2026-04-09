package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reminder_config")
public class ReminderConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    // 注意：邮箱地址统一从 sys_user 表获取，此处不重复存储
    private String phone;  // 用于未来短信通知扩展
    
    // 开关: 0/1
    private Integer popupEnabled;
    private Integer bellEnabled;
    
    // 场景详情配置 (如果需要 JSON)
    private String sceneConfig;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
