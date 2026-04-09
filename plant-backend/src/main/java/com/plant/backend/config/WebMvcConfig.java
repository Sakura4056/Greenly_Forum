package com.plant.backend.config;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.file-upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(@Nonnull ResourceHandlerRegistry registry) {
        // 映射 /uploads/** 到 file:./data/uploads/
        // 确保路径以 / 结尾
        String path = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
    }
}
