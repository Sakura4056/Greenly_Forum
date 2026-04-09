package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("my_plant")
public class MyPlant {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long officialId; // 关联官方植物ID（可为空）
    private String nickname;
    private String location; // 位置：阳台/客厅/卧室/办公桌
    private String source;   // 来源：购买/赠送/扦插/野外
    private LocalDate acquiredDate;
    private String status;   // HEALTHY/SICK/DEAD/GIFTED
    private String coverUrl;
    private String notes;

    @TableLogic
    private Integer deleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
