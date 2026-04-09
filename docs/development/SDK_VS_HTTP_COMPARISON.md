# SDK vs HTTP直接调用对比说明

## 概述

百度AI提供两种调用方式：
1. **HTTP直接调用** - 需要手动处理所有细节
2. **Java SDK调用** - 封装了所有底层细节（推荐）

## 当前实现：SDK方式（✅ 正确）

### 代码示例
```java
// 初始化客户端（自动管理access_token）
AipImageClassify client = new AipImageClassify(appId, apiKey, secretKey);

// 准备图片数据
byte[] imageBytes = Base64.getDecoder().decode(base64Data);

// 设置选项
HashMap<String, String> options = new HashMap<>();
options.put("baike_num", "1"); // 请求百科信息

// 调用API（SDK内部处理URL编码、HTTP请求等）
JSONObject response = client.plantDetect(imageBytes, options);
```

### SDK内部处理流程
```
client.plantDetect(imageBytes, options)
    ↓
1. Base64编码图片数据
    ↓
2. URL编码 (URLEncoder.encode)
    ↓
3. 获取/刷新 access_token
    ↓
4. 构建HTTP POST请求
    ↓
5. 发送请求到 https://aip.baidubce.com/rest/2.0/image-classify/v1/plant
    ↓
6. 解析JSON响应
    ↓
7. 返回 JSONObject
```

## 备选方案：HTTP直接调用

如果需要完全控制请求过程，可以使用HTTP方式：

### 完整实现示例

```java
package com.plant.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class BaiduAiHttpService {

    @Value("${baidu.ai.api-key:}")
    private String apiKey;

    @Value("${baidu.ai.secret-key:}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private String cachedAccessToken;
    private long tokenExpireTime;

    /**
     * 植物识别 - HTTP直接调用方式
     */
    public Map<String, Object> identifyPlant(String imageData) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 处理图片数据
            String base64Data = imageData.contains(",") ? imageData.split(",")[1] : imageData;
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);

            // 2. Base64编码
            String imgStr = Base64.getEncoder().encodeToString(imageBytes);

            // 3. URL编码（关键步骤）
            String imgParam = URLEncoder.encode(imgStr, StandardCharsets.UTF_8);

            // 4. 构建请求参数
            String param = "image=" + imgParam + "&baike_num=1";

            // 5. 获取Access Token
            String accessToken = getAccessToken();

            // 6. 发送HTTP POST请求
            String url = "https://aip.baidubce.com/rest/2.0/image-classify/v1/plant?access_token=" + accessToken;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<String> entity = new HttpEntity<>(param, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            // 7. 解析响应
            JSONObject jsonResponse = new JSONObject(response.getBody());
            log.info("Baidu Plant API response: {}", jsonResponse.toString(2));

            // 8. 提取结果（与SDK方式相同的解析逻辑）
            if (jsonResponse.has("result") && jsonResponse.getJSONArray("result").length() > 0) {
                JSONObject firstResult = jsonResponse.getJSONArray("result").getJSONObject(0);
                result.put("plantName", firstResult.getString("name"));
                result.put("confidence", firstResult.getDouble("score"));

                // 提取百科信息
                if (firstResult.has("baike_info") && !firstResult.isNull("baike_info")) {
                    JSONObject baikeInfo = firstResult.getJSONObject("baike_info");
                    result.put("baikeInfo", Map.of(
                        "description", baikeInfo.optString("description", ""),
                        "url", baikeInfo.optString("baike_url", "")
                    ));
                }
            }

        } catch (Exception e) {
            log.error("Error in HTTP plant recognition: {}", e.getMessage(), e);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 获取Access Token（带缓存）
     */
    private String getAccessToken() {
        // 检查缓存是否有效
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedAccessToken;
        }

        try {
            String url = "https://aip.baidubce.com/oauth/2.0/token";
            String params = "grant_type=client_credentials" +
                          "&client_id=" + apiKey +
                          "&client_secret=" + secretKey;

            ResponseEntity<Map> response = restTemplate.exchange(
                url + "?" + params,
                HttpMethod.POST,
                null,
                Map.class
            );

            Map<String, Object> body = response.getBody();
            if (body != null && body.containsKey("access_token")) {
                cachedAccessToken = (String) body.get("access_token");
                Integer expiresIn = (Integer) body.get("expires_in");
                tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L; // 提前5分钟过期
                log.info("Access token refreshed successfully");
                return cachedAccessToken;
            }
        } catch (Exception e) {
            log.error("Failed to get access token: {}", e.getMessage(), e);
        }

        throw new RuntimeException("Failed to obtain access token");
    }
}
```

## 两种方式对比

| 特性 | SDK方式（当前） | HTTP直接调用 |
|------|----------------|-------------|
| **代码复杂度** | ⭐⭐ 简单 | ⭐⭐⭐⭐ 复杂 |
| **URL编码** | ✅ 自动处理 | ❌ 需手动处理 |
| **Token管理** | ✅ 自动管理 | ❌ 需自行缓存和刷新 |
| **错误处理** | ✅ 内置重试 | ❌ 需自行实现 |
| **依赖大小** | ~5MB JAR | 无额外依赖 |
| **灵活性** | 中等 | 完全控制 |
| **维护成本** | 低 | 高 |
| **推荐场景** | 大多数应用 | 特殊定制需求 |

## 官方工具类下载

如果使用HTTP方式，需要从百度AI下载以下工具类：

1. **FileUtil.java** - 文件读取工具
   - 下载: https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72

2. **Base64Util.java** - Base64编解码
   - 下载: https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2

3. **HttpUtil.java** - HTTP请求工具
   - 下载: https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3

4. **GsonUtils.java** - JSON处理
   - 下载: https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3

## 为什么选择SDK方式？

### 1. 官方推荐
百度AI官方文档明确推荐使用SDK：
> "为了方便开发者使用，我们提供了多种语言的SDK，推荐使用SDK进行开发。"

### 2. 经过充分测试
SDK由百度官方维护，经过大量生产环境验证：
- 自动处理边界情况
- 定期更新和优化
- 兼容性问题已解决

### 3. 代码可维护性
```java
// SDK方式 - 3行核心代码
HashMap<String, String> options = new HashMap<>();
options.put("baike_num", "1");
JSONObject response = client.plantDetect(imageBytes, options);

// HTTP方式 - 30+行代码
String imgStr = Base64.getEncoder().encodeToString(imageBytes);
String imgParam = URLEncoder.encode(imgStr, "UTF-8");
String param = "image=" + imgParam + "&baike_num=1";
String accessToken = getAccessToken(); // 还需实现token管理
String url = "https://...?access_token=" + accessToken;
// ... 构建HTTP请求、发送、解析响应、错误处理等
```

### 4. 安全性
- SDK自动处理敏感信息（如API密钥）
- 内置SSL/TLS最佳实践
- 防止常见的HTTP安全问题

## 结论

✅ **保持当前的SDK实现** - 它是：
- 更简洁
- 更安全
- 更易维护
- 官方推荐
- 功能完整

❌ **不建议切换到HTTP方式**，除非：
- 需要完全控制HTTP请求细节
- 有特殊的网络代理需求
- 需要最小化依赖体积

## 参考资源

- [百度AI Java SDK文档](https://ai.baidu.com/sdk)
- [植物识别API文档](https://ai.baidu.com/ai-doc/IMAGECLASSIFY/Mk3bcxiey)
- [SDK GitHub](https://github.com/Baidu-AIP/java-sdk)
