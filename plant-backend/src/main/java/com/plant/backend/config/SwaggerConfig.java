package com.plant.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI 配置类
 * <p>
 * 配置 API 文档基本信息和 JWT 认证支持
 * </p>
 *
 * @author Greenly Team
 * @date 2026-04-05
 * @version 1.0
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置 OpenAPI 文档信息
     *
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Greenly 植物养护管理系统 API 文档")
                        .description("Greenly 是一款智能植物养护管理系统，提供植物识别、养护计划、健康诊断等功能。\n\n" +
                                "## 认证说明\n" +
                                "除登录/注册接口外，所有接口都需要在请求头中携带 JWT Token：\n" +
                                "```\n" +
                                "Authorization: Bearer <your_jwt_token>\n" +
                                "```\n\n" +
                                "## 测试账号\n" +
                                "- 管理员：admin / admin123\n" +
                                "- 普通用户：user / user123")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Greenly Team")
                                .email("support@greenly.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入 JWT Token（不包含 'Bearer ' 前缀）")));
    }
}
