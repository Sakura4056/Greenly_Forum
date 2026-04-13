package com.plant.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户数据传输对象
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
public class UserDTO {

    /**
     * 用户注册请求 DTO
     */
    @Data
    @Schema(description = "用户注册请求")
    public static class RegisterRequest {
        
        @Schema(description = "用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "用户名不能为空")
        @Pattern(regexp = "^[a-zA-Z0-9]{2,20}$", message = "用户名长度需在 2-20 位之间，仅支持字母和数字")
        private String username;

        @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "密码不能为空")
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+-=]{6,16}$", message = "密码需 6-16 位，支持字母、数字和特殊字符")
        private String password;

        @Schema(description = "昵称", example = "张三")
        private String nickname;

        @Schema(description = "邮箱", example = "zhangsan@example.com")
        @Email(message = "邮箱格式不正确")
        private String email;

        @Schema(description = "手机号", example = "13800138000")
        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;
    }

    /**
     * 用户登录请求 DTO
     */
    @Data
    @Schema(description = "用户登录请求")
    public static class LoginRequest {
        
        @Schema(description = "用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "用户名不能为空")
        private String username;

        @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "密码不能为空")
        private String password;
    }

    /**
     * 登录响应 DTO
     */
    @Data
    @Schema(description = "登录响应")
    public static class LoginResponse {
        
        @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        private String token;
        
        @Schema(description = "用户 ID", example = "1")
        private Long userId;
        
        @Schema(description = "用户名", example = "zhangsan")
        private String username;
        
        @Schema(description = "昵称", example = "张三")
        private String nickname;
        
        @Schema(description = "角色", example = "USER")
        private String role;
        
        @Schema(description = "Token 过期时间（毫秒时间戳）", example = "1712131200000")
        private Long expireTime;
    }

    /**
     * 更新用户信息请求 DTO
     */
    @Data
    @Schema(description = "更新用户信息请求")
    public static class UpdateRequest {
        
        @Schema(description = "用户 ID", hidden = true)
        private Long userId;

        @Schema(description = "昵称", example = "张三")
        private String nickname;

        @Schema(description = "邮箱", example = "zhangsan@example.com")
        @Email(message = "邮箱格式不正确")
        private String email;

        @Schema(description = "手机号", example = "13800138000")
        @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        @Schema(description = "性别", example = "1", allowableValues = {"0", "1", "2"})
        private Integer gender;

        @Schema(description = "生日", example = "1990-01-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthday;

        @Schema(description = "个性签名", example = "热爱生活，热爱植物")
        private String signature;

        @Schema(description = "头像 URL", example = "https://example.com/avatar.jpg")
        private String avatar;

        // 只有管理员可以修改角色和状态
        @Schema(description = "角色", example = "USER", allowableValues = {"USER", "ADMIN"})
        private String role;

        @Schema(description = "状态", example = "1", allowableValues = {"0", "1"})
        private Integer status;
    }

    /**
     * 修改密码请求 DTO
     */
    @Data
    @Schema(description = "修改密码请求")
    public static class ChangePasswordRequest {
        
        @Schema(description = "原密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "原密码不能为空")
        private String oldPassword;

        @Schema(description = "新密码", example = "new123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "新密码不能为空")
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+-=]{6,16}$", message = "密码需 6-16 位")
        private String newPassword;
    }

    /**
     * 用户信息响应 DTO
     */
    @Data
    @Schema(description = "用户信息响应")
    public static class Info {
        
        @Schema(description = "用户 ID", example = "1")
        private Long userId;
        
        @Schema(description = "用户名", example = "zhangsan")
        private String username;
        
        @Schema(description = "昵称", example = "张三")
        private String nickname;
        
        @Schema(description = "邮箱", example = "zhangsan@example.com")
        private String email;
        
        @Schema(description = "手机号", example = "13800138000")
        private String phone;
        
        @Schema(description = "头像 URL", example = "https://example.com/avatar.jpg")
        private String avatar;
        
        @Schema(description = "角色", example = "USER")
        private String role;
        
        @Schema(description = "状态", example = "1")
        private Integer status;
        
        @Schema(description = "性别", example = "1")
        private Integer gender;
        
        @Schema(description = "生日", example = "1990-01-01")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate birthday;
        
        @Schema(description = "个性签名", example = "热爱生活，热爱植物")
        private String signature;
        
        @Schema(description = "最后登录时间", example = "2026-04-03 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginTime;
        
        @Schema(description = "创建时间", example = "2026-01-01 00:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    /**
     * 管理员查看的用户列表项 DTO
     */
    @Data
    @Schema(description = "用户列表项")
    public static class UserListItem {
        
        @Schema(description = "用户 ID", example = "1")
        private Long userId;
        
        @Schema(description = "用户名", example = "zhangsan")
        private String username;
        
        @Schema(description = "昵称", example = "张三")
        private String nickname;
        
        @Schema(description = "邮箱", example = "zhangsan@example.com")
        private String email;
        
        @Schema(description = "手机号", example = "13800138000")
        private String phone;
        
        @Schema(description = "角色", example = "USER")
        private String role;
        
        @Schema(description = "状态", example = "1")
        private Integer status;
        
        @Schema(description = "最后登录时间", example = "2026-04-03 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginTime;
        
        @Schema(description = "创建时间", example = "2026-01-01 00:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
    }

    /**
     * 发送验证码请求 DTO
     */
    @Data
    @Schema(description = "发送验证码请求")
    public static class SendCodeRequest {
        
        @Schema(description = "邮箱地址", example = "zhangsan@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
    }

    /**
     * 绑定邮箱请求 DTO
     */
    @Data
    @Schema(description = "绑定邮箱请求")
    public static class BindEmailRequest {
        
        @Schema(description = "邮箱地址", example = "zhangsan@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @Schema(description = "验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^\\d{6}$", message = "验证码为 6 位数字")
        private String code;
    }

    /**
     * 发送重置密码验证码请求 DTO
     */
    @Data
    @Schema(description = "发送重置密码验证码请求")
    public static class SendResetCodeRequest {
        
        @Schema(description = "邮箱地址", example = "zhangsan@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;
    }

    /**
     * 重置密码请求 DTO
     */
    @Data
    @Schema(description = "重置密码请求")
    public static class ResetPasswordRequest {
        
        @Schema(description = "邮箱地址", example = "zhangsan@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @Schema(description = "验证码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "验证码不能为空")
        @Pattern(regexp = "^\\d{6}$", message = "验证码为 6 位数字")
        private String code;

        @Schema(description = "新密码", example = "new123456", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "新密码不能为空")
        @Pattern(regexp = "^[a-zA-Z0-9!@#$%^&*()_+-=]{6,16}$", message = "密码需 6-16 位")
        private String newPassword;
    }
}
