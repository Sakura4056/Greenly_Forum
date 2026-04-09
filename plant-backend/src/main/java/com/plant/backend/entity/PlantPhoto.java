package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("plant_photo")
public class PlantPhoto {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long plantId;
    private String plantSource;
    private Integer isPublic; // 0: 私有, 1: 公开
    private String remarks;

    private String url; // 访问 URL
    private String filePath; // 物理路径 (可选，可能不返回)

    private LocalDateTime captureTime; // 从 EXIF 提取

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String plantName;
}
