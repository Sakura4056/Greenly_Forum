package com.plant.backend.service;

import com.baidu.aip.imageclassify.AipImageClassify;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Baidu AI Service for Image Recognition
 */
@Slf4j
@Service
public class BaiduAiService {

    @Value("${baidu.ai.app-id:}")
    private String appId;

    @Value("${baidu.ai.api-key:}")
    private String apiKey;

    @Value("${baidu.ai.secret-key:}")
    private String secretKey;

    private AipImageClassify client;

    @PostConstruct
    public void init() {
        if (appId != null && !appId.isEmpty() &&
            apiKey != null && !apiKey.isEmpty() &&
            secretKey != null && !secretKey.isEmpty()) {
            client = new AipImageClassify(appId, apiKey, secretKey);
            // 设置连接超时和socket超时
            client.setConnectionTimeoutInMillis(5000);
            client.setSocketTimeoutInMillis(60000);
            log.info("Baidu AI client initialized successfully");
        } else {
            log.warn("Baidu AI credentials not configured. Image recognition will use mock data.");
        }
    }

    /**
     * Identify plant from image using Baidu Plant Recognition API
     * API: https://aip.baidubce.com/rest/2.0/image-classify/v1/plant
     * 
     * @param imageData Image URL or base64 string
     * @return Plant identification result with name, confidence, and suggestions
     */
    public Map<String, Object> identifyPlant(String imageData) {
        Map<String, Object> result = new HashMap<>();

        if (client == null) {
            log.warn("Baidu AI client not initialized, returning mock data");
            return getMockIdentificationResult();
        }

        try {
            JSONObject response;

            // Check if it's a URL or base64
            if (imageData.startsWith("http://") || imageData.startsWith("https://")) {
                // For URL images, use the SDK method directly
                response = client.plantDetect(imageData, new HashMap<>());
            } else {
                // For base64 images, follow official spec:
                // 1. Remove image header (e.g., "data:image/jpeg;base64,")
                // 2. Base64 decode to bytes
                // 3. SDK will handle URL encoding internally
                String base64Data = imageData.contains(",") ? imageData.split(",")[1] : imageData;
                
                // Validate base64 data
                if (base64Data.isEmpty()) {
                    throw new IllegalArgumentException("Empty base64 image data");
                }
                
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                
                // Validate image size (4MB limit after base64 encoding)
                if (imageBytes.length > 4 * 1024 * 1024) {
                    throw new IllegalArgumentException("Image size exceeds 4MB limit");
                }
                
                // Call Baidu Plant Recognition API
                // The SDK handles URL encoding and parameter formatting
                HashMap<String, String> options = new HashMap<>();
                options.put("baike_num", "1"); // Request encyclopedia info
                response = client.plantDetect(imageBytes, options);
            }

            log.info("Baidu Plant API response: {}", response.toString(2));

            // Parse response according to official spec
            if (response.has("result") && response.getJSONArray("result").length() > 0) {
                JSONArray resultArray = response.getJSONArray("result");
                JSONObject firstResult = resultArray.getJSONObject(0);
                
                // Extract plant name and confidence
                String plantName = firstResult.getString("name");
                double confidence = firstResult.getDouble("score");
                
                result.put("plantName", plantName);
                result.put("confidence", confidence);
                
                // Extract encyclopedia info if available
                if (firstResult.has("baike_info") && !firstResult.isNull("baike_info")) {
                    JSONObject baikeInfo = firstResult.getJSONObject("baike_info");
                    String description = baikeInfo.optString("description", "");
                    String baikeUrl = baikeInfo.optString("baike_url", "");
                    
                    // Generate enriched suggestions with encyclopedia info
                    result.put("suggestions", generateEnrichedCareSuggestions(plantName, description, baikeUrl));
                    result.put("baikeInfo", Map.of(
                        "description", description,
                        "url", baikeUrl,
                        "imageUrl", baikeInfo.optString("image_url", "")
                    ));
                } else {
                    result.put("suggestions", generateCareSuggestions(plantName));
                }
                
                log.info("Plant identified: {} (confidence: {})", plantName, confidence);
                
            } else {
                log.warn("No plant recognized in the image");
                result.put("plantName", "未知植物");
                result.put("confidence", 0.0);
                result.put("suggestions", Arrays.asList(
                    "无法识别该植物，请尝试拍摄更清晰的照片",
                    "确保植物在图片中占据主要位置",
                    "建议在光线充足的环境下拍摄"
                ));
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid image data: {}", e.getMessage());
            result.put("plantName", "识别失败");
            result.put("confidence", 0.0);
            result.put("suggestions", Arrays.asList("图片格式错误：" + e.getMessage()));
            
        } catch (Exception e) {
            log.error("Error identifying plant: {}", e.getMessage(), e);
            result.put("plantName", "识别失败");
            result.put("confidence", 0.0);
            result.put("suggestions", Arrays.asList("发生错误：" + e.getMessage()));
        }

        return result;
    }

    /**
     * Diagnose plant disease from image
     * @param imageData Image URL or base64 string
     * @return Disease diagnosis result
     */
    public Map<String, Object> diagnoseDisease(String imageData) {
        Map<String, Object> result = new HashMap<>();

        if (client == null) {
            log.warn("Baidu AI client not initialized, returning mock data");
            return getMockDiseaseResult();
        }

        try {
            // Use general object recognition as fallback since Baidu doesn't have specific plant disease API
            JSONObject response;

            if (imageData.startsWith("http://") || imageData.startsWith("https://")) {
                response = client.advancedGeneral(imageData, new HashMap<>());
            } else {
                String base64Data = imageData.contains(",") ? imageData.split(",")[1] : imageData;
                byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                response = client.advancedGeneral(imageBytes, new HashMap<>());
            }

            log.info("Baidu API disease diagnosis response: {}", response.toString(2));

            if (response.has("result") && response.getJSONArray("result").length() > 0) {
                JSONObject firstResult = response.getJSONArray("result").getJSONObject(0);
                String keyword = firstResult.getString("keyword");
                double score = firstResult.getDouble("score");

                result.put("plantName", "检测到的植物");
                result.put("issue", "检测到可能的病虫害问题：" + keyword);
                result.put("confidence", score);
                result.put("suggestions", generateDiseaseSuggestions(keyword));
            } else {
                result.put("plantName", "未检测到异常");
                result.put("issue", "未发现明显的病虫害症状");
                result.put("confidence", 0.0);
                result.put("suggestions", Arrays.asList("植物看起来健康，继续保持良好的养护习惯"));
            }

        } catch (Exception e) {
            log.error("Error diagnosing disease: {}", e.getMessage(), e);
            result.put("plantName", "诊断失败");
            result.put("issue", "无法完成诊断");
            result.put("confidence", 0.0);
            result.put("suggestions", Arrays.asList("发生错误：" + e.getMessage()));
        }

        return result;
    }

    /**
     * Generate care suggestions based on plant name
     */
    private List<String> generateCareSuggestions(String plantName) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add(plantName + "是一种常见的观赏植物");
        suggestions.add("保持适宜的光照条件，避免强光直射");
        suggestions.add("适量浇水，保持土壤湿润但不过湿");
        suggestions.add("定期施肥，每月1-2次稀释的液肥");
        suggestions.add("注意通风，防止病虫害发生");
        return suggestions;
    }

    /**
     * Generate enriched care suggestions with encyclopedia information
     */
    private List<String> generateEnrichedCareSuggestions(String plantName, String description, String baikeUrl) {
        List<String> suggestions = new ArrayList<>();
        
        // Add plant identification info
        suggestions.add("识别结果：" + plantName);
        
        // Add encyclopedia description if available (truncated to reasonable length)
        if (description != null && !description.isEmpty()) {
            String truncatedDesc = description.length() > 100 ? description.substring(0, 100) + "..." : description;
            suggestions.add("百科简介：" + truncatedDesc);
        }
        
        // Add basic care tips
        suggestions.add("养护建议：");
        suggestions.add("- 光照：保持适宜的光照条件，避免强光直射");
        suggestions.add("- 浇水：适量浇水，保持土壤湿润但不过湿");
        suggestions.add("- 施肥：定期施肥，每月1-2次稀释的液肥");
        suggestions.add("- 通风：注意通风，防止病虫害发生");
        
        // Add encyclopedia link if available
        if (baikeUrl != null && !baikeUrl.isEmpty()) {
            suggestions.add("更多信息可查看百度百科：" + baikeUrl);
        }
        
        return suggestions;
    }

    /**
     * Generate disease treatment suggestions
     */
    private List<String> generateDiseaseSuggestions(String issue) {
        List<String> suggestions = new ArrayList<>();
        suggestions.add("观察到问题：" + issue);
        suggestions.add("建议隔离患病植物，防止扩散");
        suggestions.add("根据具体症状选择合适的农药或治疗方法");
        suggestions.add("改善养护环境，增强植物抵抗力");
        suggestions.add("如情况严重，建议咨询专业园艺师");
        return suggestions;
    }

    /**
     * Mock identification result (fallback when API is not configured)
     */
    private Map<String, Object> getMockIdentificationResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("plantName", "绿萝 (Pothos)");
        result.put("confidence", 0.95);
        result.put("suggestions", Arrays.asList(
            "绿萝是一种常见的室内观叶植物",
            "适合放置在半阴处，避免阳光直射",
            "浇水要适量，保持土壤湿润但不过湿",
            "可以定期擦拭叶片以保持光泽",
            "适宜温度18-30℃"
        ));
        return result;
    }

    /**
     * Mock disease result (fallback when API is not configured)
     */
    private Map<String, Object> getMockDiseaseResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("plantName", "绿萝");
        result.put("issue", "叶片发黄，可能是由于浇水过多或光照不足");
        result.put("confidence", 0.88);
        result.put("suggestions", Arrays.asList(
            "减少浇水频率，让土壤适当干燥",
            "将植物移至光线更充足的位置",
            "可以适当施肥补充养分",
            "检查根系是否有腐烂现象",
            "修剪发黄的叶片，促进新叶生长"
        ));
        return result;
    }
}
