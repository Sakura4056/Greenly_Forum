package com.plant.backend.service;

import com.plant.backend.dto.AiDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI 服务单元测试
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@DisplayName("AI 服务测试")
class AiServiceTest {

    @Autowired
    private AiService aiService;

    @Test
    @DisplayName("测试 AI 聊天 - 正常流程")
    void testChat_Success() {
        // Given
        Long userId = 1L;
        String sessionId = "test-session-" + UUID.randomUUID();
        AiDTO.ChatRequest request = new AiDTO.ChatRequest();
        request.setMessage("如何养护绿萝？");
        request.setSessionId(sessionId);
        request.setProvider("mock");
        request.setModel("test-model");

        // When
        AiDTO.ChatResponse response = aiService.chat(userId, request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getContent());
        assertEquals(sessionId, response.getSessionId());
    }

    @Test
    @DisplayName("测试获取对话历史 - 正常流程")
    void testGetConversationHistory_Success() {
        // Given
        Long userId = 1L;
        String sessionId = "test-history-" + UUID.randomUUID();

        // 先发送一条消息创建历史记录
        AiDTO.ChatRequest chatRequest = new AiDTO.ChatRequest();
        chatRequest.setMessage("测试消息");
        chatRequest.setSessionId(sessionId);
        chatRequest.setProvider("mock");
        aiService.chat(userId, chatRequest);

        // When
        AiDTO.ConversationHistoryResponse history = aiService.getConversationHistory(userId, sessionId, 10);

        // Then
        assertNotNull(history);
        assertEquals(sessionId, history.getSessionId());
        assertTrue(history.getTotal() >= 2);
    }

    @Test
    @DisplayName("测试删除会话 - 正常流程")
    void testDeleteSession_Success() {
        // Given
        Long userId = 1L;
        String sessionId = "test-delete-" + UUID.randomUUID();

        AiDTO.ChatRequest chatRequest = new AiDTO.ChatRequest();
        chatRequest.setMessage("测试消息");
        chatRequest.setSessionId(sessionId);
        chatRequest.setProvider("mock");
        aiService.chat(userId, chatRequest);

        // When
        aiService.deleteSession(userId, sessionId);

        // Then
        AiDTO.ConversationHistoryResponse history = aiService.getConversationHistory(userId, sessionId, 10);
        assertEquals(0, history.getTotal());
    }

    @Test
    @DisplayName("测试图片诊断 - 植物识别")
    void testDiagnoseImage_Identification() {
        // Given
        Long userId = 1L;
        AiDTO.ImageDiagnosisRequest request = new AiDTO.ImageDiagnosisRequest();
        request.setImageUrl("https://example.com/test.jpg");
        request.setDiagnosisType("identification");
        request.setProvider("mock");

        // When
        AiDTO.ImageDiagnosisResponse response = aiService.diagnoseImage(userId, request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getPlantName());
    }

    @Test
    @DisplayName("测试图片诊断 - 病害诊断")
    void testDiagnoseImage_Disease() {
        // Given
        Long userId = 1L;
        AiDTO.ImageDiagnosisRequest request = new AiDTO.ImageDiagnosisRequest();
        request.setImageUrl("https://example.com/disease.jpg");
        request.setDiagnosisType("disease");
        request.setProvider("mock");

        // When
        AiDTO.ImageDiagnosisResponse response = aiService.diagnoseImage(userId, request);

        // Then
        assertNotNull(response);
    }

    @Test
    @DisplayName("测试图片诊断 - 养护建议")
    void testDiagnoseImage_CareAdvice() {
        // Given
        Long userId = 1L;
        AiDTO.ImageDiagnosisRequest request = new AiDTO.ImageDiagnosisRequest();
        request.setImageUrl("https://example.com/care.jpg");
        request.setDiagnosisType("care");
        request.setProvider("mock");

        // When
        AiDTO.ImageDiagnosisResponse response = aiService.diagnoseImage(userId, request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getSuggestions());
    }

    @Test
    @DisplayName("测试缓存机制 - 相同请求返回缓存")
    void testDiagnoseImage_CacheHit() {
        // Given
        Long userId = 1L;
        String imageUrl = "https://example.com/cache-test.jpg";

        AiDTO.ImageDiagnosisRequest request1 = new AiDTO.ImageDiagnosisRequest();
        request1.setImageUrl(imageUrl);
        request1.setDiagnosisType("identification");
        request1.setProvider("mock");

        // When - 第一次调用
        AiDTO.ImageDiagnosisResponse response1 = aiService.diagnoseImage(userId, request1);

        // When - 第二次调用（应该命中缓存）
        AiDTO.ImageDiagnosisResponse response2 = aiService.diagnoseImage(userId, request1);

        // Then
        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals(response1.getPlantName(), response2.getPlantName());
    }
}
