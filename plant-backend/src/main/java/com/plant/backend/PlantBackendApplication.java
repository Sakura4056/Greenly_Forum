package com.plant.backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Sun
 */
@EnableScheduling // 开启定时任务
@SpringBootApplication // 启动Spring Boot应用
// 扫描Mapper接口
@MapperScan("com.plant.backend.mapper")
public class PlantBackendApplication {

    static {
        // 在类加载时立即加载 .env 文件，确保环境变量在 Spring 上下文初始化前可用
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory(".")
                    .ignoreIfMissing()
                    .load();
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        } catch (Exception e) {
            // 如果 .env 文件不存在，忽略错误，继续使用系统环境变量
            System.err.println("Warning: .env file not found, using system environment variables");
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(PlantBackendApplication.class, args);
    }

}
