package com.plant.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plant.backend.entity.ForumComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 论坛评论 Mapper 接口
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@Mapper
public interface ForumCommentMapper extends BaseMapper<ForumComment> {
}
