package com.plant.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plant.backend.entity.PlantDiary;
import org.apache.ibatis.annotations.Mapper;

/**
 * Plant Diary Mapper
 */
@Mapper
public interface PlantDiaryMapper extends BaseMapper<PlantDiary> {
}
