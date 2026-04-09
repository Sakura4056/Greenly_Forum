package com.plant.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plant.backend.entity.MyPlant;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MyPlantMapper extends BaseMapper<MyPlant> {
}
