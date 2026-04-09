package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.dto.UserDTO;
import com.plant.backend.entity.User;
import com.plant.backend.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务单元测试
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@DisplayName("用户服务测试")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserDTO.RegisterRequest registerRequest;
    private UserDTO.LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        registerRequest = new UserDTO.RegisterRequest();
        registerRequest.setUsername("testuser_" + System.currentTimeMillis());
        registerRequest.setPassword("test123456");
        registerRequest.setNickname("测试用户");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPhone("13800138000");

        loginRequest = new UserDTO.LoginRequest();
        loginRequest.setUsername(registerRequest.getUsername());
        loginRequest.setPassword(registerRequest.getPassword());
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void testRegister_Success() {
        // When
        UserDTO.LoginResponse response = userService.register(registerRequest);

        // Then
        assertNotNull(response);
        assertNotNull(response.getToken());
        assertNotNull(response.getUserId());
        assertEquals(registerRequest.getUsername(), response.getUsername());
        assertEquals("USER", response.getRole());

        // 验证数据库中是否存在
        User savedUser = userService.getById(response.getUserId());
        assertNotNull(savedUser);
        assertEquals(registerRequest.getUsername(), savedUser.getUsername());
        assertTrue(passwordEncoder.matches(registerRequest.getPassword(), savedUser.getPassword()));
    }

    @Test
    @DisplayName("用户注册 - 用户名重复")
    void testRegister_DuplicateUsername() {
        // Given - 先注册一个用户
        userService.register(registerRequest);

        // When & Then - 再次注册相同用户名应抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(registerRequest);
        });

        assertEquals("用户名已存在", exception.getMessage());
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void testLogin_Success() {
        // Given - 先注册用户
        UserDTO.LoginResponse registerResponse = userService.register(registerRequest);

        // When - 使用正确的密码登录
        UserDTO.LoginResponse loginResponse = userService.login(loginRequest);

        // Then
        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getToken());
        assertEquals(registerResponse.getUserId(), loginResponse.getUserId());
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void testLogin_WrongPassword() {
        // Given - 先注册用户
        userService.register(registerRequest);

        // When - 使用错误的密码登录
        loginRequest.setPassword("wrongpassword");

        // Then - 应抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("用户登录 - 用户不存在")
    void testLogin_UserNotFound() {
        // Given - 设置不存在的用户名
        loginRequest.setUsername("nonexistent_user");

        // When & Then - 应抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.login(loginRequest);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("更新用户信息 - 成功")
    void testUpdate_Success() {
        // Given - 先注册用户
        UserDTO.LoginResponse registerResponse = userService.register(registerRequest);
        Long userId = registerResponse.getUserId();

        UserDTO.UpdateRequest updateRequest = new UserDTO.UpdateRequest();
        updateRequest.setUserId(userId);
        updateRequest.setNickname("新昵称");
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setPhone("13900139000");
        updateRequest.setGender(1);
        updateRequest.setSignature("新的个性签名");

        // When
        User updatedUser = userService.update(updateRequest, userId, "USER");

        // Then
        assertNotNull(updatedUser);
        assertEquals("新昵称", updatedUser.getNickname());
        assertEquals("newemail@example.com", updatedUser.getEmail());
        assertEquals("13900139000", updatedUser.getPhone());
        assertEquals(1, updatedUser.getGender());
        assertEquals("新的个性签名", updatedUser.getSignature());
    }

    @Test
    @DisplayName("更新用户信息 - 无权修改他人信息")
    void testUpdate_Forbidden() {
        // Given - 注册两个用户
        UserDTO.LoginResponse user1 = userService.register(registerRequest);
        
        registerRequest.setUsername("testuser2_" + System.currentTimeMillis());
        registerRequest.setEmail("test2@example.com");
        registerRequest.setPhone("13800138001");
        UserDTO.LoginResponse user2 = userService.register(registerRequest);

        UserDTO.UpdateRequest updateRequest = new UserDTO.UpdateRequest();
        updateRequest.setUserId(user2.getUserId());
        updateRequest.setNickname("非法修改");

        // When & Then - 用户 1 尝试修改用户 2 的信息应被拒绝
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.update(updateRequest, user1.getUserId(), "USER");
        });

        assertEquals("无权修改其他用户信息", exception.getMessage());
    }

    @Test
    @DisplayName("管理员更新任意用户信息 - 成功")
    void testUpdate_ByAdmin() {
        // Given - 先注册用户
        UserDTO.LoginResponse registerResponse = userService.register(registerRequest);
        Long userId = registerResponse.getUserId();

        UserDTO.UpdateRequest updateRequest = new UserDTO.UpdateRequest();
        updateRequest.setUserId(userId);
        updateRequest.setNickname("管理员修改的昵称");
        updateRequest.setRole("ADMIN"); // 管理员可以修改角色

        // When - 以管理员身份更新
        User updatedUser = userService.update(updateRequest, 1L, "ADMIN");

        // Then
        assertEquals("管理员修改的昵称", updatedUser.getNickname());
    }

    @Test
    @DisplayName("修改密码 - 成功")
    void testChangePassword_Success() {
        // Given - 先注册用户
        UserDTO.LoginResponse registerResponse = userService.register(registerRequest);
        Long userId = registerResponse.getUserId();

        String newPassword = "new123456";

        // When
        userService.changePassword(userId, registerRequest.getPassword(), newPassword);

        // Then - 使用新密码登录应该成功
        UserDTO.LoginRequest newLoginRequest = new UserDTO.LoginRequest();
        newLoginRequest.setUsername(registerRequest.getUsername());
        newLoginRequest.setPassword(newPassword);

        UserDTO.LoginResponse loginResponse = userService.login(newLoginRequest);
        assertNotNull(loginResponse);
        assertEquals(userId, loginResponse.getUserId());
    }

    @Test
    @DisplayName("修改密码 - 原密码错误")
    void testChangePassword_WrongOldPassword() {
        // Given - 先注册用户
        UserDTO.LoginResponse registerResponse = userService.register(registerRequest);
        Long userId = registerResponse.getUserId();

        // When & Then - 原密码错误应抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.changePassword(userId, "wrongoldpassword", "new123456");
        });

        assertEquals("原密码错误", exception.getMessage());
    }

    @Test
    @DisplayName("删除用户 - 成功")
    void testDeleteUser_Success() {
        // Given - 先注册用户
        UserDTO.LoginResponse registerResponse = userService.register(registerRequest);
        Long userId = registerResponse.getUserId();

        // When
        userService.deleteUser(userId);

        // Then - 用户应被逻辑删除（查询不到）
        User deletedUser = userService.getById(userId);
        assertNull(deletedUser);
    }

    @Test
    @DisplayName("分页查询用户列表 - 成功")
    void testListUsers_Success() {
        // Given - 创建多个测试用户
        for (int i = 0; i < 5; i++) {
            registerRequest.setUsername("testuser_" + System.currentTimeMillis() + "_" + i);
            registerRequest.setEmail("test" + i + "@example.com");
            registerRequest.setPhone("1380013800" + i);
            userService.register(registerRequest);
        }

        // When
        Page<User> page = new Page<>(1, 10);
        Page<UserDTO.UserListItem> result = userService.listUsers(page, null);

        // Then
        assertNotNull(result);
        assertTrue(result.getTotal() >= 5);
        assertFalse(result.getRecords().isEmpty());
    }

    @Test
    @DisplayName("关键词搜索用户 - 成功")
    void testListUsers_WithKeyword() {
        // Given - 创建特定用户
        registerRequest.setUsername("search_test_user");
        registerRequest.setNickname("搜索测试");
        registerRequest.setEmail("search@test.com");
        userService.register(registerRequest);

        // When - 使用关键词搜索
        Page<User> page = new Page<>(1, 10);
        Page<UserDTO.UserListItem> result = userService.listUsers(page, "搜索");

        // Then
        assertNotNull(result);
        assertTrue(result.getTotal() >= 1);
        boolean found = result.getRecords().stream()
                .anyMatch(u -> u.getNickname() != null && u.getNickname().contains("搜索"));
        assertTrue(found);
    }

    @Test
    @DisplayName("获取用户详情 - 成功")
    void testGetUserInfo_Success() {
        // Given - 先注册用户
        UserDTO.LoginResponse registerResponse = userService.register(registerRequest);
        Long userId = registerResponse.getUserId();

        // When
        UserDTO.Info userInfo = userService.getUserInfo(userId);

        // Then
        assertNotNull(userInfo);
        assertEquals(userId, userInfo.getUserId());
        assertEquals(registerRequest.getUsername(), userInfo.getUsername());
        assertEquals(registerRequest.getEmail(), userInfo.getEmail());
    }
}
