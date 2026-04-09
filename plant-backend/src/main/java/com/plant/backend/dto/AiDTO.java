package com.plant.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * AI Related DTOs
 */
public class AiDTO {

    @Data
    public static class ChatRequest {
        private String message;
        private String sessionId;
        private String provider; // deepseek/openai/gemini
        private String model;
        private String apiKey;
        private String baseUrl;
    }

    @Data
    public static class ChatResponse {
        private String content;
        private String model;
        private Integer tokensUsed;
        private String sessionId;
    }

    @Data
    public static class ImageDiagnosisRequest {
        private String imageUrl;
        private String diagnosisType; // identification/disease/care
        private String provider;
        private String apiKey;
    }

    @Data
    public static class ImageDiagnosisResponse {
        private String plantName;
        private String issue;
        private List<String> suggestions;
        private Double confidence;
    }

    @Data
    public static class ConversationHistoryRequest {
        private String sessionId;
        private Integer limit;
    }

    @Data
    public static class ConversationMessage {
        private Long id;
        private String role;
        private String content;
        private String createTime;
    }

    @Data
    public static class ConversationHistoryResponse {
        private String sessionId;
        private List<ConversationMessage> messages;
        private Integer total;
    }
}
