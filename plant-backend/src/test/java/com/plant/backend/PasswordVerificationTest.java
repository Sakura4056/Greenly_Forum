package com.plant.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 验证更新后的 BCrypt 哈希是否正确
 */
public class PasswordVerificationTest {
    
    @Test
    public void verifyUser123Password() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        String rawPassword = "user123";
        String storedHash = "$2a$10$D0WC2cOO9qc3.aQOW4ek5OusEkpvTGCHQTBDE1.ghcje5NzhJEXlO";
        
        System.out.println("=== 验证 user123 密码 ===");
        System.out.println("原始密码：" + rawPassword);
        System.out.println("存储的哈希：" + storedHash);
        
        boolean matches = passwordEncoder.matches(rawPassword, storedHash);
        System.out.println("验证结果：" + matches);
        
        if (matches) {
            System.out.println("✅ 密码验证成功！");
        } else {
            System.out.println("❌ 密码验证失败！");
        }
        
        // 应该输出 true
        assert matches : "user123 password verification failed!";
    }
}
