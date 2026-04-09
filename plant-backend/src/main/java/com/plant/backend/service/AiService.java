package com.plant.backend.service;

import com.plant.backend.dto.AiDTO;
import java.util.List;

/**
 * AI Service Interface
 */
public interface AiService {

    AiDTO.ChatResponse chat(Long userId, AiDTO.ChatRequest request);

    AiDTO.ImageDiagnosisResponse diagnoseImage(Long userId, AiDTO.ImageDiagnosisRequest request);

    AiDTO.ConversationHistoryResponse getConversationHistory(Long userId, String sessionId, Integer limit);

    void deleteSession(Long userId, String sessionId);

    List<AiDTO.SessionInfo> getSessionList(Long userId, int limit);
}
