package com.plant.backend.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * 环境变量配置类（已废弃）
 * <p>
 * 注意：.env 文件加载已移至 PlantBackendApplication 的静态块中执行，
 * 确保在 Spring 上下文初始化之前加载环境变量。
 * 此类保留仅用于向后兼容，实际不再执行任何操作。
 * </p>
 *
 * @author Greenly Team
 * @date 2026-04-05
 * @version 1.1
 */
@Slf4j
@Configuration
public class DotenvConfig {
    // 此类已废弃，.env 加载逻辑已移至 PlantBackendApplication.static{}
}
