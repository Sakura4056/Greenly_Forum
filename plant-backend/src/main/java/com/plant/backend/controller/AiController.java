package com.plant.backend.controller;

import com.plant.backend.base.BaseController;
import com.plant.backend.dto.AiDTO;
import com.plant.backend.dto.PlantDetectDTO;
import com.plant.backend.service.AiService;
import com.plant.backend.service.BaiduAiService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController extends BaseController {

    private final AiService aiService;
    private final BaiduAiService baiduAiService;
    private final JwtUtil jwtUtil;

    /**
     * Chat with AI
     */
    @PostMapping("/chat")
    public Result<AiDTO.ChatResponse> chat(@RequestBody AiDTO.ChatRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(aiService.chat(userId, request));
    }

    /**
     * Diagnose plant from image
     */
    @PostMapping("/diagnose-image")
    public Result<AiDTO.ImageDiagnosisResponse> diagnoseImage(@RequestBody AiDTO.ImageDiagnosisRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(aiService.diagnoseImage(userId, request));
    }

    /**
     * Identify plant from image (Baidu AI)
     */
    @PostMapping("/identify-plant")
    public Result<PlantDetectDTO.DetectResponse> identifyPlant(
            @RequestBody PlantDetectDTO.DetectRequest request, 
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        
        try {
            // Determine if using URL or base64
            String imageData = request.getImageUrl() != null ? request.getImageUrl() : request.getImage();
            
            if (imageData == null || imageData.isEmpty()) {
                PlantDetectDTO.DetectResponse errorResponse = new PlantDetectDTO.DetectResponse();
                errorResponse.setSuccess(false);
                errorResponse.setErrorMessage("请提供图片URL或Base64编码");
                return Result.success(errorResponse);
            }
            
            // Call Baidu AI service
            Map<String, Object> result = baiduAiService.identifyPlant(imageData);
            
            // Convert to response DTO
            PlantDetectDTO.DetectResponse response = new PlantDetectDTO.DetectResponse();
            response.setSuccess(true);
            
            PlantDetectDTO.DetectResponse.PlantResult plantResult = new PlantDetectDTO.DetectResponse.PlantResult();
            plantResult.setName((String) result.getOrDefault("plantName", "未知植物"));
            plantResult.setScore((Double) result.getOrDefault("confidence", 0.0));
            
            // Extract suggestions as classification info
            @SuppressWarnings("unchecked")
            java.util.List<String> suggestions = (java.util.List<String>) result.get("suggestions");
            if (suggestions != null && !suggestions.isEmpty()) {
                plantResult.setClassification(String.join("\n", suggestions));
            }
            
            // Extract baike info if available
            @SuppressWarnings("unchecked")
            Map<String, String> baikeInfo = (Map<String, String>) result.get("baikeInfo");
            if (baikeInfo != null) {
                plantResult.setBaikeUrl(baikeInfo.get("url"));
            }
            
            response.setResults(java.util.Collections.singletonList(plantResult));
            
            log.info("User {} identified plant: {}", userId, plantResult.getName());
            return Result.success(response);
            
        } catch (Exception e) {
            log.error("Error identifying plant for user {}", userId, e);
            PlantDetectDTO.DetectResponse errorResponse = new PlantDetectDTO.DetectResponse();
            errorResponse.setSuccess(false);
            errorResponse.setErrorMessage("识别失败：" + e.getMessage());
            return Result.success(errorResponse);
        }
    }

    /**
     * Get conversation history
     */
    @GetMapping("/conversation-history")
    public Result<AiDTO.ConversationHistoryResponse> getConversationHistory(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false, defaultValue = "50") Integer limit,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(aiService.getConversationHistory(userId, sessionId, limit));
    }

    /**
     * Delete conversation session
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        aiService.deleteSession(userId, sessionId);
        return Result.success(null);
    }

    /**
     * Extract user ID from JWT token
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            throw new RuntimeException("未授权访问");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            throw new RuntimeException("认证失败");
        }
        return userId;
    }
}
