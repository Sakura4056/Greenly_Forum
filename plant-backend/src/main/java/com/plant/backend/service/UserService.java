package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.plant.backend.dto.UserDTO;
import com.plant.backend.entity.User;

/**
 * 用户服务接口
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户注册
     * 
     * @param request 注册请求 DTO
     * @return 登录响应（包含 Token）
     */
    UserDTO.LoginResponse register(UserDTO.RegisterRequest request);

    /**
     * 用户登录
     * 
     * @param request 登录请求 DTO
     * @return 登录响应（包含 Token）
     */
    UserDTO.LoginResponse login(UserDTO.LoginRequest request);

    /**
     * 更新用户信息
     * 
     * @param request 更新请求 DTO
     * @param currentUserId 当前登录用户 ID
     * @param currentRole 当前登录用户角色
     * @return 更新后的用户信息
     */
    User update(UserDTO.UpdateRequest request, Long currentUserId, String currentRole);

    /**
     * 修改密码
     * 
     * @param userId 用户 ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 删除用户（逻辑删除）
     * 
     * @param userId 用户 ID
     */
    void deleteUser(Long userId);

    /**
     * 获取用户列表（分页）
     * 
     * @param page 分页参数
     * @param keyword 搜索关键词（用户名/邮箱/手机）
     * @return 用户列表分页数据
     */
    Page<UserDTO.UserListItem> listUsers(Page<User> page, String keyword);

    /**
     * 根据 ID 获取用户详情
     * 
     * @param userId 用户 ID
     * @return 用户信息 DTO
     */
    UserDTO.Info getUserInfo(Long userId);

    /**
     * 获取当前登录用户信息
     * 
     * @param userId 用户 ID
     * @return 用户信息 DTO
     */
    UserDTO.Info getCurrentUserInfo(Long userId);

    /**
     * 更新最后登录信息
     * 
     * @param userId 用户 ID
     * @param ip 登录 IP
     */
    void updateLastLoginInfo(Long userId, String ip);

    /**
     * 检查邮箱是否已注册
     */
    boolean isEmailRegistered(String email);

    /**
     * 绑定邮箱
     */
    void bindEmail(Long userId, UserDTO.BindEmailRequest request);

}
