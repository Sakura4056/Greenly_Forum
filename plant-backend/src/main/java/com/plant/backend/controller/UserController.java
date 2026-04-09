package com.plant.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.base.BaseController;
import com.plant.backend.dto.UserDTO;
import com.plant.backend.entity.User;
import com.plant.backend.service.UserService;
import com.plant.backend.util.IpUtils;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 处理用户注册、登录、信息管理等请求
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
@Slf4j
@Tag(name = "用户管理", description = "用户注册、登录、信息管理接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     * 注册新用户，注册成功后自动登录
     */
    @Operation(summary = "用户注册", description = "注册新用户，注册成功后自动登录")
    @PostMapping("/register")
    public Result<UserDTO.LoginResponse> register(@RequestBody @Valid UserDTO.RegisterRequest request) {
        log.info("接收到注册请求，用户名：{}", request.getUsername());
        return success(userService.register(request));
    }

    /**
     * 用户登录
     * 用户名密码登录，返回 JWT Token
     */
    @Operation(summary = "用户登录", description = "用户名密码登录，返回 JWT Token")
    @PostMapping("/login")
    public Result<UserDTO.LoginResponse> login(@RequestBody @Valid UserDTO.LoginRequest request,
                                               HttpServletRequest httpRequest) {
        log.info("接收到登录请求，用户名：{}", request.getUsername());
        
        UserDTO.LoginResponse response = userService.login(request);
        
        // 更新最后登录 IP
        String ip = IpUtils.getClientIp(httpRequest);
        userService.updateLastLoginInfo(response.getUserId(), ip);
        
        logOperation("USER_LOGIN", "用户登录成功，username=" + request.getUsername() + ", ip=" + ip);
        return success(response);
    }

    /**
     * 获取当前登录用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/info")
    public Result<UserDTO.Info> getCurrentUserInfo(HttpServletRequest request) {
        String token = extractToken(request);
        Long userId = jwtUtil.getUserIdFromToken(token);
        return success(userService.getCurrentUserInfo(userId));
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息", description = "修改个人信息（昵称、邮箱、手机等）")
    @PutMapping("/update")
    public Result<User> update(@RequestBody @Valid UserDTO.UpdateRequest request, 
                               HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        Long currentUserId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        log.info("收到更新用户信息请求: userId={}, nickname={}, email={}, phone={}", 
                currentUserId, request.getNickname(), request.getEmail(), request.getPhone());

        // 安全处理：普通用户只能修改自己的信息
        if (!"ADMIN".equals(role)) {
            request.setUserId(currentUserId);
        }

        logOperation("USER_UPDATE", "更新用户信息，userId=" + currentUserId);
        return success(userService.update(request, currentUserId, role));
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @PutMapping("/change-password")
    public Result<Void> changePassword(@RequestBody @Valid UserDTO.ChangePasswordRequest request,
                                       HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        logOperation("PASSWORD_CHANGE", "用户修改密码，userId=" + userId);
        return success();
    }

    /**
     * 删除用户（管理员专用）
     */
    @Operation(summary = "删除用户", description = "逻辑删除用户（仅管理员可用）")
    @DeleteMapping("/delete/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable("userId") Long userId) {
        log.info("管理员删除用户，userId: {}", userId);
        userService.deleteUser(userId);
        logOperation("USER_DELETE", "管理员删除用户，userId=" + userId);
        return success();
    }

    /**
     * 用户列表（管理员专用）
     */
    @Operation(summary = "用户列表", description = "分页查询用户列表（仅管理员可用）")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<UserDTO.UserListItem>> list(
            @Parameter(description = "页码", example = "1")
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词")
            @RequestParam(name = "keyword", required = false) String keyword) {
        
        Page<User> page = new Page<>(pageNum, pageSize);
        return success(userService.listUsers(page, keyword));
    }

    /**
     * 根据 ID 获取用户信息（管理员专用）
     */
    @Operation(summary = "获取用户详情", description = "根据用户 ID 获取详细信息（仅管理员可用）")
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserDTO.Info> getUserDetail(@PathVariable("userId") Long userId) {
        return success(userService.getUserInfo(userId));
    }
}
