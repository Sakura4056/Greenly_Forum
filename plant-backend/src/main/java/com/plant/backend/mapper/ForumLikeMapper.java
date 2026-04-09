package com.plant.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plant.backend.entity.ForumLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛点赞 Mapper 接口
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@Mapper
public interface ForumLikeMapper extends BaseMapper<ForumLike> {
}
