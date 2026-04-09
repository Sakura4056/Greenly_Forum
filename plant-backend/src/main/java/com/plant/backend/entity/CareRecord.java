package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("care_record")
public class CareRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long plantId;
    private String plantSource; // OFFICIAL (官方), LOCAL (自定)
    private Long scheduleId; // 可选，可为空
    
    private LocalDateTime recordTime;
    
    // JSON 字符串: {"water": 200, "fertilizer": "NPK", ...} 或操作列表
    // 规范说明: "operations(JSON format)"
    // 我们为了简单起见在数据库中存为字符串，Service层处理转换，或使用专门的 TypeHandler
    // 对于本项目，字符串是最安全且兼容的方式。
    private String operations; 
    
    private String remarks;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
