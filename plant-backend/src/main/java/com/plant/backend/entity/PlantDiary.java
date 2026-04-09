package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Plant Diary Entity
 */
@Data
@TableName("plant_diary")
public class PlantDiary {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long plantId;
    private String title;
    private String content;
    private String mood; // happy/sad/neutral/excited/worried
    private String weather; // sunny/cloudy/rainy/snowy/windy
    private String photos; // JSON array of photo IDs
    private LocalDate diaryDate;
    
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
