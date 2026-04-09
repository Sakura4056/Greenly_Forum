package com.plant.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密工具类
 * 用于生成和验证 BCrypt 密码哈希
 */
public class PasswordUtils {
    
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return BCrypt 哈希值
     */
    public static String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
    
    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 主函数 - 用于生成密码哈希
     * 使用方法：修改 main 函数中的密码，运行后复制输出的哈希值到数据库
     */
    public static void main(String[] args) {
        // 要加密的密码列表
        String[] passwords = {"admin123", "user123"};
        
        System.out.println("=== BCrypt 密码生成器 ===");
        System.out.println("警告：每次运行都会生成不同的哈希值（这是 BCrypt 的特性）\n");
        
        for (String password : passwords) {
            String encoded = encode(password);
            System.out.println("原始密码：" + password);
            System.out.println("BCrypt 哈希：" + encoded);
            System.out.println("验证结果：" + matches(password, encoded));
            System.out.println("---");
        }
        
        // 生成固定次数的哈希，可以选择一个使用
        System.out.println("\n=== 为 admin123 生成多个哈希供选择 ===");
        for (int i = 0; i < 5; i++) {
            String hash = encode("admin123");
            System.out.println((i + 1) + ". " + hash);
        }
    }
}
