package com.plant.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 客户端配置类
 * <p>
 * 配置 RestTemplate 和 ObjectMapper 单例 Bean，提供连接池和超时控制
 * </p>
 *
 * @author Greenly Team
 * @date 2026-04-05
 * @version 1.0
 */
@Configuration
public class HttpClientConfig {

    /**
     * 配置 RestTemplate Bean
     * <p>
     * 设置连接超时和读取超时，避免请求无限等待
     * </p>
     *
     * @return 配置好的 RestTemplate 实例
     */
    @Bean
    public RestTemplate restTemplate() {
        ClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 连接超时时间：5秒
        ((SimpleClientHttpRequestFactory) factory).setConnectTimeout(5000);
        // 读取超时时间：30秒（AI API 响应可能较慢）
        ((SimpleClientHttpRequestFactory) factory).setReadTimeout(30000);

        return new RestTemplate(factory);
    }

    /**
     * 配置 ObjectMapper Bean
     * <p>
     * 统一 JSON 序列化配置，避免重复创建对象
     * </p>
     *
     * @return 配置好的 ObjectMapper 实例
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // 注册 Java 8 日期时间模块（支持 LocalDateTime, LocalDate 等）
        mapper.registerModule(new JavaTimeModule());
        
        // 禁用将日期写为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
}
