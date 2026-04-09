package com.plant.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.plant.backend.entity.AiConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI Conversation Mapper
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {
}
