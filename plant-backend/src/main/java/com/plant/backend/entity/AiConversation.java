package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI Conversation History Entity
 */
@Data
@TableName("ai_conversation")
public class AiConversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String sessionId;
    private String role; // user/assistant/system
    private String content;
    private String model;
    private Integer tokensUsed;
    private String metadata; // JSON string
    private LocalDateTime createTime;
}
