package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("official_plant")
public class OfficialPlant {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String genus; // 属
    private String species; // 种
    private String description;
    
    // 养护相关字段
    private String imageUrl;        // 植物图片
    private String lightReq;        // 光照需求：喜阳/半阴/喜阴
    private String waterReq;        // 浇水频率：耐旱/适中/喜湿
    private String tempRange;       // 适宜温度范围："15-28°C"
    private String soilReq;         // 土壤要求
    private String difficulty;      // 养护难度：简单/中等/困难
    private String bloomSeason;     // 花期
    private String commonDiseases;  // 常见病虫害
    private String careTips;        // 养护小贴士（长文本）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
