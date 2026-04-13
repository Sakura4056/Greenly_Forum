package com.plant.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.base.BaseController;
import com.plant.backend.dto.CareScheduleDTO;
import com.plant.backend.entity.CareSchedule;
import com.plant.backend.service.CareScheduleService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/care/schedule")
@RequiredArgsConstructor
public class CareScheduleController extends BaseController {

    private final CareScheduleService careScheduleService;
    private final JwtUtil jwtUtil;

    @PostMapping("/add")
    public Result<CareSchedule> add(@RequestBody @Valid CareScheduleDTO.AddRequest request,
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
        request.setUserId(userId);

        return success(careScheduleService.add(request));
    }

    @PutMapping("/update/{scheduleId}")
    public Result<CareSchedule> update(@PathVariable("scheduleId") Long scheduleId,
            @RequestBody @Valid CareScheduleDTO.UpdateRequest request, HttpServletRequest httpRequest) {
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

        request.setId(scheduleId);

        return success(careScheduleService.updateSchedule(request, userId));
    }

    @DeleteMapping("/delete/{scheduleId}")
    public Result<Void> delete(@PathVariable("scheduleId") Long scheduleId, HttpServletRequest httpRequest) {
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
        careScheduleService.deleteSchedule(scheduleId, userId);
        return success();
    }

    @GetMapping("/query")
    public Result<Page<CareSchedule>> query(CareScheduleDTO.Query query, HttpServletRequest httpRequest) {
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
        String role = jwtUtil.getRoleFromToken(token);

        return success(careScheduleService.query(query, userId, role));
    }

    @GetMapping("/calendar")
    public Result<Map<String, List<CareSchedule>>> getCalendar(@RequestParam Integer year, @RequestParam Integer month, HttpServletRequest httpRequest) {
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

        Map<String, List<CareSchedule>> calendar = careScheduleService.getCalendar(year, month, userId);
        return success(calendar);
    }
}
