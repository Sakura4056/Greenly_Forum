package com.plant.backend.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plant.backend.entity.Reminder;

@Mapper
public interface ReminderMapper extends BaseMapper<Reminder> {
}
