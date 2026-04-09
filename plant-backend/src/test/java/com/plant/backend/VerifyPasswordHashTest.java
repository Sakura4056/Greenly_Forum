package com.plant.backend;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 验证特定 BCrypt 哈希是否有效
 */
public class VerifyPasswordHashTest {
    
    @Test
    public void verifyAdminPassword() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        String rawPassword = "admin123";
        String storedHash = "$2a$10$ywpUoXpf2gHfZea39308fOHgVjBOrwIOrOXLeOsOspKI1z0dP8tae";
        
        System.out.println("原始密码：" + rawPassword);
        System.out.println("存储的哈希：" + storedHash);
        System.out.println("验证结果：" + passwordEncoder.matches(rawPassword, storedHash));
        
        // 应该输出 true
        assert passwordEncoder.matches(rawPassword, storedHash) : "Admin password verification failed!";
    }
    
    @Test
    public void verifyUserPassword() {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        String rawPassword = "user123";
        String storedHash = "$2a$10$7RWPknPrDSbeOOA3w4l1gu8D.GfgkOv/RpAQZeL6gHx28lWqxnM62";
        
        System.out.println("原始密码：" + rawPassword);
        System.out.println("存储的哈希：" + storedHash);
        System.out.println("验证结果：" + passwordEncoder.matches(rawPassword, storedHash));
        
        // 应该输出 true
        assert passwordEncoder.matches(rawPassword, storedHash) : "User password verification failed!";
    }
}
