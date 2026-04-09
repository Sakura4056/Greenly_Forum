package com.plant.backend.controller;

import com.plant.backend.base.BaseController;
import com.plant.backend.dto.ReminderDTO;
import com.plant.backend.entity.ReminderConfig;
import com.plant.backend.service.ReminderService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author Sun
 */
@Slf4j
@RestController
@RequestMapping("/api/reminder")
@RequiredArgsConstructor
public class ReminderController extends BaseController {

    private final ReminderService reminderService;
    private final JwtUtil jwtUtil;
    private final com.plant.backend.task.CareTask careTask;

    @PutMapping("/config/update")
    public Result<ReminderConfig> updateConfig(@RequestBody ReminderDTO.ConfigUpdateRequest request,
            HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }

        return success(reminderService.updateConfig(request, userId));
    }

    @GetMapping("/config")
    public Result<ReminderConfig> getConfig(HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }
        return success(reminderService.getConfig(userId));
    }

    @GetMapping("/unread/{userId}")
    public Result<ReminderDTO.UnreadResponse> getUnread(@PathVariable("userId") Long userId,
            HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long tokenUserId = jwtUtil.getUserIdFromToken(token);
        if (tokenUserId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }

        // 验证: 用户只能查看自己的提醒
        if (!userId.equals(tokenUserId)) {
            // 检查是否管理员? 或严格模式?
            // "普通用户仅能查自身"
            // 让我们严格检查，或允许管理员。
            // 目前最简单的是检查匹配。
            String role = jwtUtil.getRoleFromToken(token);
            if (!"ADMIN".equals(role)) {
                // 返回 Forbidden 还是只使用 Token ID?
                // 需求: "Path parameter userId". 安全的方法是验证。
                // 如果不匹配且不是管理员，我们将返回错误。
                // 目前，为了安全起见，如果通过了 ID 无关紧要，但规范说路径参数。
                return Result.error(com.plant.backend.util.ResultCode.FORBIDDEN);
            }
        }

        return success(reminderService.getUnread(userId));
    }

    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable("id") Long id, HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }

        reminderService.markAsRead(id, userId);
        return success();
    }

    @PostMapping("/test-email")
    public Result<Void> triggerTestEmail() {
        // 异步执行，立即返回 200，不阻塞前端
        new Thread(() -> careTask.scanDueSchedules(true)).start();
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }
        return success(reminderService.getUnreadCount(userId));
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }
        reminderService.markAllAsRead(userId);
        return success();
    }
}
