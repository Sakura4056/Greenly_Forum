package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("identify_history")
public class IdentifyHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String imageUrl;
    private String plantName;
    private Double confidence;
    private String baikeUrl;
    private String classification;
    private String rawResult;
    private LocalDateTime createTime;
}
