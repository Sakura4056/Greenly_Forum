package com.plant.backend.controller;

import com.plant.backend.base.BaseController;
import com.plant.backend.entity.MyPlant;
import com.plant.backend.mapper.MyPlantMapper;
import com.plant.backend.service.SmartWateringService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 智能浇水控制器
 * <p>
 * 提供智能浇水提醒、浇水计划计算等接口
 * </p>
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@Slf4j
@Tag(name = "智能浇水", description = "智能浇水提醒、浇水计划计算接口")
@RestController
@RequestMapping("/api/watering")
@RequiredArgsConstructor
public class SmartWateringController extends BaseController {

    private final SmartWateringService smartWateringService;
    private final JwtUtil jwtUtil;
    private final MyPlantMapper myPlantMapper;

    /**
     * 计算植物浇水计划
     */
    @Operation(summary = "计算浇水计划", description = "根据植物种类、养护记录、天气情况计算最佳浇水时间")
    @GetMapping("/schedule/{myPlantId}")
    public Result<Map<String, Object>> calculateWateringSchedule(
            @Parameter(description = "我的植物ID", required = true, example = "1")
            @PathVariable Long myPlantId,
            HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }
        
        // 验证植物是否属于当前用户
        MyPlant myPlant = myPlantMapper.selectById(myPlantId);
        if (myPlant == null || !myPlant.getUserId().equals(userId)) {
            log.warn("植物不存在或无权访问: myPlantId={}, userId={}", myPlantId, userId);
            return error(403, "无权访问该植物");
        }
        
        return success(smartWateringService.calculateWateringSchedule(myPlantId));
    }

    /**
     * 获取用户所有植物的浇水提醒
     */
    @Operation(summary = "获取浇水提醒列表", description = "获取用户所有需要浇水的植物列表，按紧急程度排序")
    @GetMapping("/reminders")
    public Result<List<Map<String, Object>>> getWateringReminders(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }
        return success(smartWateringService.getWateringReminders(userId));
    }

    /**
     * 手动触发浇水提醒检查
     */
    @Operation(summary = "触发浇水提醒检查", description = "手动检查并发送浇水提醒通知")
    @PostMapping("/check")
    public Result<Void> checkWateringReminders(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return error(401, "未登录或登录已过期");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            return error(401, "认证失败");
        }
        smartWateringService.sendSmartWateringReminders(userId);
        return success();
    }
}
