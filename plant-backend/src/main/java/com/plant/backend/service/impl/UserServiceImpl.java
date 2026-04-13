package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plant.backend.dto.UserDTO;
import com.plant.backend.entity.User;
import com.plant.backend.event.UserDeletedEvent;
import com.plant.backend.exception.BusinessException;
import com.plant.backend.mapper.UserMapper;
import com.plant.backend.service.EmailVerificationService;
import com.plant.backend.service.UserService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final JwtUtil jwtUtil;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDTO.LoginResponse register(UserDTO.RegisterRequest request) {
        log.info("用户注册，用户名：{}", request.getUsername());
        
        // 检查用户名是否存在
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名已存在");
        }

        // 创建用户实体
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole("USER"); // 默认角色为普通用户
        user.setStatus(1); // 默认状态为正常
        user.setGender(0); // 默认性别为未知

        save(user);
        log.info("用户注册成功，userId: {}", user.getUserId());

        // 注册成功后自动登录
        return generateLoginResponse(user);
    }

    @Override
    public UserDTO.LoginResponse login(UserDTO.LoginRequest request) {
        log.info("用户登录，用户名：{}", request.getUsername());
        
        // 查询用户
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));

        // 验证用户是否存在
        if (user == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名或密码错误");
        }

        // 验证用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账号已被禁用，请联系管理员");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名或密码错误");
        }

        // 更新最后登录信息
        updateLastLoginInfo(user.getUserId(), null);

        log.info("用户登录成功，userId: {}, role: {}", user.getUserId(), user.getRole());
        return generateLoginResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User update(UserDTO.UpdateRequest request, Long currentUserId, String currentRole) {
        log.info("更新用户信息，userId: {}, operatorId: {}, operatorRole: {}", 
                request.getUserId(), currentUserId, currentRole);
        
        // 权限检查：管理员可以更新任何人，用户只能更新自己
        if (!"ADMIN".equals(currentRole) && !currentUserId.equals(request.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权修改其他用户信息");
        }

        // 获取用户
        User user = getById(request.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }

        // 更新允许修改的字段
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            // 允许设置为空字符串
            user.setEmail(request.getEmail());
            log.info("更新邮箱: {} -> {}", user.getEmail(), request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
        if (StringUtils.hasText(request.getSignature())) {
            user.setSignature(request.getSignature());
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(request.getAvatar());
        }

        // 只有管理员可以修改角色和状态
        if ("ADMIN".equals(currentRole)) {
            if (StringUtils.hasText(request.getRole())) {
                user.setRole(request.getRole());
            }
            if (request.getStatus() != null) {
                user.setStatus(request.getStatus());
            }
        }

        updateById(user);
        log.info("用户信息更新成功，userId: {}", user.getUserId());

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改密码，userId: {}", userId);
        
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
        
        log.info("密码修改成功，userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        log.info("删除用户，userId: {}", userId);
        
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }

        // 执行逻辑删除（MyBatis Plus @TableLogic 自动处理）
        removeById(userId);

        // 发布删除事件，解耦下游清理逻辑（如清理关联数据）
        eventPublisher.publishEvent(new UserDeletedEvent(this, userId));
        
        log.info("用户删除成功，userId: {}", userId);
    }

    @Override
    public Page<UserDTO.UserListItem> listUsers(Page<User> page, String keyword) {
        log.debug("查询用户列表，keyword: {}", keyword);
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getEmail, keyword)
                    .or()
                    .like(User::getPhone, keyword)
                    .or()
                    .like(User::getNickname, keyword));
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = page(page, wrapper);

        // 转换为 DTO
        Page<UserDTO.UserListItem> dtoPage = new Page<>();
        dtoPage.setCurrent(result.getCurrent());
        dtoPage.setSize(result.getSize());
        dtoPage.setTotal(result.getTotal());

        List<UserDTO.UserListItem> dtos = result.getRecords().stream()
                .map(this::convertToUserListItem)
                .collect(Collectors.toList());

        dtoPage.setRecords(dtos);
        return dtoPage;
    }

    @Override
    public UserDTO.Info getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        return convertToUserInfo(user);
    }

    @Override
    public UserDTO.Info getCurrentUserInfo(Long userId) {
        return getUserInfo(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastLoginInfo(Long userId, String ip) {
        User user = new User();
        user.setUserId(userId);
        user.setLastLoginTime(LocalDateTime.now());
        if (StringUtils.hasText(ip)) {
            user.setLastLoginIp(ip);
        }
        updateById(user);
    }

    /**
     * 生成登录响应
     */
    private UserDTO.LoginResponse generateLoginResponse(User user) {
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        UserDTO.LoginResponse response = new UserDTO.LoginResponse();
        response.setToken(token);
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        // Token 过期时间（24 小时后）
        response.setExpireTime(System.currentTimeMillis() + 86400000L);

        return response;
    }

    /**
     * 转换为 UserListItem DTO
     */
    private UserDTO.UserListItem convertToUserListItem(User user) {
        UserDTO.UserListItem item = new UserDTO.UserListItem();
        item.setUserId(user.getUserId());
        item.setUsername(user.getUsername());
        item.setNickname(user.getNickname());
        item.setEmail(user.getEmail());
        item.setPhone(user.getPhone());
        item.setRole(user.getRole());
        item.setStatus(user.getStatus());
        item.setLastLoginTime(user.getLastLoginTime());
        item.setCreateTime(user.getCreateTime());
        return item;
    }

    /**
     * 转换为 UserInfo DTO
     */
    private UserDTO.Info convertToUserInfo(User user) {
        UserDTO.Info info = new UserDTO.Info();
        info.setUserId(user.getUserId());
        info.setUsername(user.getUsername());
        info.setNickname(user.getNickname());
        info.setEmail(user.getEmail());
        info.setPhone(user.getPhone());
        info.setAvatar(user.getAvatar());
        info.setRole(user.getRole());
        info.setStatus(user.getStatus());
        info.setGender(user.getGender());
        info.setBirthday(user.getBirthday());
        info.setSignature(user.getSignature());
        info.setLastLoginTime(user.getLastLoginTime());
        info.setCreateTime(user.getCreateTime());
        return info;
    }

    @Override
    public boolean isEmailRegistered(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email.trim().toLowerCase());
        return baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    @Transactional
    public void bindEmail(Long userId, UserDTO.BindEmailRequest request) {
        if (!emailVerificationService.consumeCode(request.getEmail(), request.getCode(), "bind")) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "验证码错误或已过期");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail().trim().toLowerCase())
               .ne(User::getUserId, userId);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "该邮箱已被其他用户绑定");
        }

        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在");
        }
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(user);

        log.info("用户 {} 绑定邮箱: {}", userId, request.getEmail());
    }

}
