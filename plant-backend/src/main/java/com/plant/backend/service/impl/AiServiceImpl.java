package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.plant.backend.dto.AiDTO;
import com.plant.backend.entity.AiConversation;
import com.plant.backend.mapper.AiConversationMapper;
import com.plant.backend.service.AiService;
import com.plant.backend.service.BaiduAiService;
import com.plant.backend.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiConversationMapper aiConversationMapper;
    private final BaiduAiService baiduAiService;
    private final CacheService cacheService;
    private final RestTemplate restTemplate;

    private static final String CACHE_KEY_PREFIX = "ai:diagnosis:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    @Override
    public AiDTO.ChatResponse chat(Long userId, AiDTO.ChatRequest request) {
        log.info("AI Chat request from user {}: message={}, provider={}", userId, request.getMessage(), request.getProvider());

        saveMessage(userId, request.getSessionId(), "user", request.getMessage(), request.getModel());

        String aiResponse = callAiApi(request);

        saveMessage(userId, request.getSessionId(), "assistant", aiResponse, request.getModel());

        AiDTO.ChatResponse response = new AiDTO.ChatResponse();
        response.setContent(aiResponse);
        response.setModel(request.getModel());
        response.setSessionId(request.getSessionId());
        response.setTokensUsed(estimateTokens(request.getMessage() + aiResponse));

        return response;
    }

    @Override
    public AiDTO.ImageDiagnosisResponse diagnoseImage(Long userId, AiDTO.ImageDiagnosisRequest request) {
        log.info("AI Image diagnosis request from user {}: type={}, provider={}",
                userId, request.getDiagnosisType(), request.getProvider());

        String cacheKey = generateCacheKey(request.getImageUrl(), request.getDiagnosisType());

        AiDTO.ImageDiagnosisResponse cachedResponse = cacheService.get(cacheKey, AiDTO.ImageDiagnosisResponse.class);
        if (cachedResponse != null) {
            log.info("Cache hit for image diagnosis: key={}", cacheKey);
            return cachedResponse;
        }

        AiDTO.ImageDiagnosisResponse response = new AiDTO.ImageDiagnosisResponse();

        try {
            if (request.getProvider() == null || "baidu".equals(request.getProvider())) {
                Map<String, Object> baiduResult;

                if ("identification".equals(request.getDiagnosisType())) {
                    baiduResult = baiduAiService.identifyPlant(request.getImageUrl());
                    response.setPlantName((String) baiduResult.get("plantName"));
                    response.setConfidence((Double) baiduResult.get("confidence"));
                    @SuppressWarnings("unchecked")
                    List<String> suggestions = (List<String>) baiduResult.get("suggestions");
                    response.setSuggestions(suggestions);
                } else if ("disease".equals(request.getDiagnosisType())) {
                    baiduResult = baiduAiService.diagnoseDisease(request.getImageUrl());
                    response.setPlantName((String) baiduResult.get("plantName"));
                    response.setIssue((String) baiduResult.get("issue"));
                    response.setConfidence((Double) baiduResult.get("confidence"));
                    @SuppressWarnings("unchecked")
                    List<String> suggestions = (List<String>) baiduResult.get("suggestions");
                    response.setSuggestions(suggestions);
                } else {
                    baiduResult = baiduAiService.identifyPlant(request.getImageUrl());
                    response.setPlantName((String) baiduResult.get("plantName"));
                    response.setConfidence((Double) baiduResult.get("confidence"));
                    @SuppressWarnings("unchecked")
                    List<String> suggestions = (List<String>) baiduResult.get("suggestions");
                    response.setSuggestions(suggestions);
                }

                log.info("Baidu AI diagnosis completed: plant={}, confidence={}",
                        response.getPlantName(), response.getConfidence());

            } else {
                response = generateMockDiagnosis(request);
            }

            cacheService.set(cacheKey, response, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("Cached diagnosis result: key={}", cacheKey);

        } catch (Exception e) {
            log.error("Error during image diagnosis: {}", e.getMessage(), e);
            response.setPlantName("诊断失败");
            response.setConfidence(0.0);
            response.setSuggestions(Arrays.asList("诊断过程中发生错误：" + e.getMessage()));
        }

        return response;
    }

    private AiDTO.ImageDiagnosisResponse generateMockDiagnosis(AiDTO.ImageDiagnosisRequest request) {
        AiDTO.ImageDiagnosisResponse response = new AiDTO.ImageDiagnosisResponse();

        if ("identification".equals(request.getDiagnosisType())) {
            response.setPlantName("绿萝 (Pothos)");
            response.setConfidence(0.95);
            response.setSuggestions(Arrays.asList(
                "绿萝是一种常见的室内观叶植物",
                "适合放置在半阴处，避免阳光直射",
                "浇水要适量，保持土壤湿润但不过湿"
            ));
        } else if ("disease".equals(request.getDiagnosisType())) {
            response.setPlantName("绿萝");
            response.setIssue("叶片发黄，可能是由于浇水过多或光照不足");
            response.setConfidence(0.88);
            response.setSuggestions(Arrays.asList(
                "减少浇水频率，让土壤适当干燥",
                "将植物移至光线更充足的位置",
                "可以适当施肥补充养分"
            ));
        } else {
            response.setPlantName("绿萝");
            response.setSuggestions(Arrays.asList(
                "浇水：每周1-2次，保持土壤湿润",
                "光照：适合明亮的散射光",
                "温度：适宜温度18-30℃",
                "施肥：每月1次稀释的液肥"
            ));
        }

        return response;
    }

    @Override
    public AiDTO.ConversationHistoryResponse getConversationHistory(Long userId, String sessionId, Integer limit) {
        log.info("Get conversation history for user {}, session: {}", userId, sessionId);

        if (limit == null || limit <= 0) {
            limit = 50;
        }

        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getUserId, userId)
               .eq(AiConversation::getSessionId, sessionId)
               .ne(AiConversation::getRole, "system")
               .orderByAsc(AiConversation::getCreateTime)
               .last("LIMIT " + limit);

        List<AiConversation> conversations = aiConversationMapper.selectList(wrapper);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<AiDTO.ConversationMessage> messages = conversations.stream().map(conv -> {
            AiDTO.ConversationMessage msg = new AiDTO.ConversationMessage();
            msg.setId(conv.getId());
            msg.setRole(conv.getRole());
            msg.setContent(conv.getContent());
            msg.setCreateTime(conv.getCreateTime().format(formatter));
            return msg;
        }).collect(Collectors.toList());

        AiDTO.ConversationHistoryResponse response = new AiDTO.ConversationHistoryResponse();
        response.setSessionId(sessionId);
        response.setMessages(messages);
        response.setTotal(messages.size());

        return response;
    }

    @Override
    public void deleteSession(Long userId, String sessionId) {
        log.info("Delete conversation session for user {}, session: {}", userId, sessionId);

        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getUserId, userId)
               .eq(AiConversation::getSessionId, sessionId);

        aiConversationMapper.delete(wrapper);
    }

    @Override
    public List<AiDTO.SessionInfo> getSessionList(Long userId, int limit) {
        if (limit <= 0) limit = 20;
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getUserId, userId)
               .orderByDesc(AiConversation::getCreateTime)
               .last("LIMIT 500");
        List<AiConversation> allConvs = aiConversationMapper.selectList(wrapper);
        Map<String, AiConversation> sessionMap = new LinkedHashMap<>();
        Map<String, Integer> countMap = new HashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (AiConversation conv : allConvs) {
            String sid = conv.getSessionId();
            countMap.merge(sid, 1, Integer::sum);
            if (!sessionMap.containsKey(sid)) { sessionMap.put(sid, conv); }
        }
        List<AiDTO.SessionInfo> result = new ArrayList<>();
        int cnt = 0;
        for (Map.Entry<String, AiConversation> entry : sessionMap.entrySet()) {
            if (cnt >= limit) break;
            AiDTO.SessionInfo info = new AiDTO.SessionInfo();
            info.setSessionId(entry.getKey());
            AiConversation last = entry.getValue();
            String content = last.getContent();
            info.setLastMessage(content != null && content.length() > 100 ? content.substring(0, 100) + "..." : content);
            info.setLastMessageTime(last.getCreateTime().format(formatter));
            info.setMessageCount(countMap.get(entry.getKey()));
            result.add(info);
            cnt++;
        }
        return result;
    }

    private String callAiApi(AiDTO.ChatRequest request) {
        try {
            if ("gemini".equals(request.getProvider())) {
                return callGeminiApi(request);
            } else {
                return callOpenAICompatibleApi(request);
            }
        } catch (Exception e) {
            log.error("Error calling AI API: {}", e.getMessage(), e);
            return "抱歉，AI服务暂时不可用，请稍后重试。错误信息：" + e.getMessage();
        }
    }

    private String callOpenAICompatibleApi(AiDTO.ChatRequest request) {
        String url = request.getBaseUrl() + "/chat/completions";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", request.getModel() != null ? request.getModel() : "gpt-3.5-turbo");
        requestBody.put("stream", false);

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "你是一个叫 Greenly AI 的植物养护专家。请用专业、友好、简洁的中文回答关于植物养护、病虫害防治、植物识别等问题。如果用户问了与植物无关的问题，请委婉地将其引导回植物话题。");
        messages.add(systemMsg);

        List<AiConversation> history = getRecentHistory(request.getSessionId(), 10);
        for (AiConversation conv : history) {
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user".equals(conv.getRole()) ? "user" : "assistant");
            msg.put("content", conv.getContent());
            messages.add(msg);
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", request.getMessage());
        messages.add(userMsg);

        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(request.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }

        throw new RuntimeException("Invalid response from AI API");
    }

    private String callGeminiApi(AiDTO.ChatRequest request) {
        String model = request.getModel() != null ? request.getModel() : "gemini-1.5-flash";
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + request.getApiKey();

        Map<String, Object> requestBody = new HashMap<>();
        List<Map<String, Object>> contents = new ArrayList<>();

        List<AiConversation> history = getRecentHistory(request.getSessionId(), 10);
        for (AiConversation conv : history) {
            if ("system".equals(conv.getRole())) continue;
            Map<String, Object> content = new HashMap<>();
            content.put("role", "user".equals(conv.getRole()) ? "user" : "model");
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", conv.getContent());
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
        }

        Map<String, Object> currentContent = new HashMap<>();
        currentContent.put("role", "user");
        List<Map<String, String>> parts = new ArrayList<>();
        Map<String, String> part = new HashMap<>();
        part.put("text", request.getMessage());
        parts.add(part);
        currentContent.put("parts", parts);
        contents.add(currentContent);

        requestBody.put("contents", contents);

        Map<String, Object> systemInstruction = new HashMap<>();
        List<Map<String, String>> systemParts = new ArrayList<>();
        Map<String, String> systemPart = new HashMap<>();
        systemPart.put("text", "你是一个叫 Greenly AI 的植物养护专家。请用专业、友好、简洁的中文回答关于植物养护、病虫害防治、植物识别等问题。");
        systemParts.add(systemPart);
        systemInstruction.put("parts", systemParts);
        requestBody.put("systemInstruction", systemInstruction);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST, entity, (Class<Map<String, Object>>) (Class<?>) Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> contentParts = (List<Map<String, Object>>) content.get("parts");
                if (contentParts != null && !contentParts.isEmpty()) {
                    return (String) contentParts.get(0).get("text");
                }
            }
        }

        throw new RuntimeException("Invalid response from Gemini API");
    }

    private List<AiConversation> getRecentHistory(String sessionId, int limit) {
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getSessionId, sessionId)
               .ne(AiConversation::getRole, "system")
               .orderByDesc(AiConversation::getCreateTime)
               .last("LIMIT " + limit);

        List<AiConversation> history = aiConversationMapper.selectList(wrapper);
        Collections.reverse(history);
        return history;
    }

    private void saveMessage(Long userId, String sessionId, String role, String content, String model) {
        AiConversation conversation = new AiConversation();
        conversation.setUserId(userId);
        conversation.setSessionId(sessionId);
        conversation.setRole(role);
        conversation.setContent(content);
        conversation.setModel(model);
        conversation.setCreateTime(LocalDateTime.now());

        aiConversationMapper.insert(conversation);
    }

    private Integer estimateTokens(String text) {
        return text.length() / 2;
    }

    private String generateCacheKey(String imageUrl, String diagnosisType) {
        try {
            String rawKey = imageUrl + ":" + diagnosisType;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return CACHE_KEY_PREFIX + hexString.toString();
        } catch (Exception e) {
            log.warn("Failed to generate cache key using SHA-256, fallback to simple key", e);
            return CACHE_KEY_PREFIX + imageUrl.hashCode() + ":" + diagnosisType;
        }
    }
}
