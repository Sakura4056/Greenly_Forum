package com.plant.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 百度AI配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "baidu.ai")
public class BaiduAiConfig {

    /**
     * 百度AI App ID
     */
    private String appId;

    /**
     * 百度AI API Key
     */
    private String apiKey;

    /**
     * 百度AI Secret Key
     */
    private String secretKey;

    /**
     * 是否启用植物识别功能
     */
    private Boolean plantDetectEnabled = true;
}
