package com.plant.backend.controller;

import com.plant.backend.base.BaseController;
import com.plant.backend.dto.AiDTO;
import com.plant.backend.dto.PlantDetectDTO;
import com.plant.backend.entity.IdentifyHistory;
import com.plant.backend.service.AiService;
import com.plant.backend.service.BaiduAiService;
import com.plant.backend.service.IdentifyHistoryService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController extends BaseController {

    private final AiService aiService;
    private final BaiduAiService baiduAiService;
    private final IdentifyHistoryService identifyHistoryService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @PostMapping("/chat")
    public Result<AiDTO.ChatResponse> chat(@RequestBody AiDTO.ChatRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(aiService.chat(userId, request));
    }

    @PostMapping("/diagnose-image")
    public Result<AiDTO.ImageDiagnosisResponse> diagnoseImage(@RequestBody AiDTO.ImageDiagnosisRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(aiService.diagnoseImage(userId, request));
    }

    @PostMapping("/identify-plant")
    public Result<PlantDetectDTO.DetectResponse> identifyPlant(
            @RequestBody PlantDetectDTO.DetectRequest request, 
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        
        try {
            String imageData = request.getImageUrl() != null ? request.getImageUrl() : request.getImage();
            
            if (imageData == null || imageData.isEmpty()) {
                PlantDetectDTO.DetectResponse errorResponse = new PlantDetectDTO.DetectResponse();
                errorResponse.setSuccess(false);
                errorResponse.setErrorMessage("请提供图片URL或Base64编码");
                return Result.success(errorResponse);
            }
            
            Map<String, Object> result = baiduAiService.identifyPlant(imageData);
            
            PlantDetectDTO.DetectResponse response = new PlantDetectDTO.DetectResponse();
            response.setSuccess(true);
            
            PlantDetectDTO.DetectResponse.PlantResult plantResult = new PlantDetectDTO.DetectResponse.PlantResult();
            plantResult.setName((String) result.getOrDefault("plantName", "未知植物"));
            plantResult.setScore((Double) result.getOrDefault("confidence", 0.0));
            
            @SuppressWarnings("unchecked")
            List<String> suggestions = (List<String>) result.get("suggestions");
            if (suggestions != null && !suggestions.isEmpty()) {
                plantResult.setClassification(String.join("\n", suggestions));
            }
            
            @SuppressWarnings("unchecked")
            Map<String, String> baikeInfo = (Map<String, String>) result.get("baikeInfo");
            String baikeUrl = null;
            if (baikeInfo != null) {
                baikeUrl = baikeInfo.get("url");
                plantResult.setBaikeUrl(baikeUrl);
            }
            
            response.setResults(java.util.Collections.singletonList(plantResult));
            
            // Save identify history
            try {
                String rawJson = objectMapper.writeValueAsString(result);
                String imageUrl = request.getImageUrl() != null ? request.getImageUrl() : "[base64]";
                identifyHistoryService.saveHistory(userId, imageUrl, plantResult.getName(),
                        plantResult.getScore(), baikeUrl, plantResult.getClassification(), rawJson);
            } catch (Exception e) {
                log.warn("Failed to save identify history: {}", e.getMessage());
            }
            
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

    @GetMapping("/conversation-history")
    public Result<AiDTO.ConversationHistoryResponse> getConversationHistory(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false, defaultValue = "50") Integer limit,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(aiService.getConversationHistory(userId, sessionId, limit));
    }

    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(@PathVariable String sessionId, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        aiService.deleteSession(userId, sessionId);
        return Result.success(null);
    }

    // ========== Identify History Endpoints ==========

    @GetMapping("/identify-history")
    public Result<List<IdentifyHistory>> getIdentifyHistory(
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(identifyHistoryService.getUserHistory(userId, limit));
    }

    @DeleteMapping("/identify-history/{id}")
    public Result<Void> deleteIdentifyHistory(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        identifyHistoryService.deleteHistory(userId, id);
        return Result.success(null);
    }

    @DeleteMapping("/identify-history")
    public Result<Void> clearIdentifyHistory(HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        identifyHistoryService.clearHistory(userId);
        return Result.success(null);
    }

    // ========== Conversation Session List ==========

    @GetMapping("/sessions")
    public Result<List<AiDTO.SessionInfo>> getSessionList(
            @RequestParam(required = false, defaultValue = "20") Integer limit,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(aiService.getSessionList(userId, limit));
    }

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
