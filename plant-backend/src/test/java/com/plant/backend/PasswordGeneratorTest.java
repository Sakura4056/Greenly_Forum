package com.plant.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码生成测试类
 */
public class PasswordGeneratorTest {
    
    @Test
    public void generatePasswords() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        System.out.println("=== BCrypt 密码生成器 ===");
        System.out.println("注意：BCrypt 每次都会生成不同的哈希值\n");
        
        // 为 admin123 生成哈希
        String adminPassword = "admin123";
        String adminHash = passwordEncoder.encode(adminPassword);
        System.out.println("密码：" + adminPassword);
        System.out.println("BCrypt 哈希：" + adminHash);
        System.out.println("验证：" + passwordEncoder.matches(adminPassword, adminHash));
        System.out.println();
        
        // 为 user123 生成哈希
        String userPassword = "user123";
        String userHash = passwordEncoder.encode(userPassword);
        System.out.println("密码：" + userPassword);
        System.out.println("BCrypt 哈希：" + userHash);
        System.out.println("验证：" + passwordEncoder.matches(userPassword, userHash));
        System.out.println();
        
        // 多生成几个供选择
        System.out.println("=== 更多 admin123 的哈希选项 ===");
        for (int i = 0; i < 3; i++) {
            String hash = passwordEncoder.encode("admin123");
            System.out.println((i + 1) + ". " + hash);
        }
    }
}
