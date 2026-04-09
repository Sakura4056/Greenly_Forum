package com.plant.backend.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
@Data
@TableName("sys_user")
public class User {
    
    /**
     * 用户 ID（主键自增）
     */
    @TableId(type = IdType.AUTO)
    private Long userId;

    /**
     * 用户名（唯一，不能为空）
     */
    @TableField
    private String username;

    /**
     * 密码（BCrypt 加密存储）
     */
    @TableField
    @JsonIgnore
    private String password;

    /**
     * 昵称（显示名称）
     */
    @TableField
    private String nickname;

    /**
     * 邮箱地址
     */
    @TableField
    private String email;

    /**
     * 手机号码
     */
    @TableField
    private String phone;

    /**
     * 头像 URL 地址
     */
    @TableField
    private String avatar;

    /**
     * 角色：USER（普通用户）/ ADMIN（管理员）
     */
    @TableField
    private String role;

    /**
     * 状态：1 正常 / 0 禁用
     */
    @TableField
    private Integer status;

    /**
     * 性别：0 未知 / 1 男 / 2 女
     */
    @TableField
    private Integer gender;

    /**
     * 出生日期
     */
    @TableField
    private LocalDate birthday;

    /**
     * 个性签名
     */
    @TableField
    private String signature;

    /**
     * 最后登录时间
     */
    @TableField
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录 IP 地址
     */
    @TableField
    private String lastLoginIp;

    /**
     * 创建时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间（自动填充）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标识：0 未删除 / 1 已删除
     */
    @TableLogic
    @JsonIgnore
    private Integer deleted;
}
