package com.plant.backend.service;

import com.plant.backend.dto.AiDTO;

/**
 * AI Service Interface
 */
public interface AiService {

    /**
     * Send chat message to AI and get streaming response
     */
    AiDTO.ChatResponse chat(Long userId, AiDTO.ChatRequest request);

    /**
     * Diagnose plant from image
     */
    AiDTO.ImageDiagnosisResponse diagnoseImage(Long userId, AiDTO.ImageDiagnosisRequest request);

    /**
     * Get conversation history
     */
    AiDTO.ConversationHistoryResponse getConversationHistory(Long userId, String sessionId, Integer limit);

    /**
     * Delete conversation session
     */
    void deleteSession(Long userId, String sessionId);
}
