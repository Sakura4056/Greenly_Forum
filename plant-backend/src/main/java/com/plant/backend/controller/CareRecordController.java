package com.plant.backend.controller;

import com.plant.backend.base.BaseController;
import com.plant.backend.dto.CareRecordDTO;
import com.plant.backend.entity.CareRecord;
import com.plant.backend.service.CareRecordService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/care/record")
@RequiredArgsConstructor
public class CareRecordController extends BaseController {

    private final CareRecordService careRecordService;
    private final JwtUtil jwtUtil;

    @PostMapping("/add")
    public Result<CareRecord> add(@RequestBody @Valid CareRecordDTO.AddRequest request, HttpServletRequest httpRequest) {
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
        
        return success(careRecordService.add(request));
    }

    @GetMapping("/statistic")
    public Result<Map<String, Object>> statistic(CareRecordDTO.StatQuery query, HttpServletRequest httpRequest) {
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
        
        if (query.getUserId() == null) {
            query.setUserId(userId);
        }
        
        return success(careRecordService.statistic(query, userId));
    }

    @GetMapping
    public Result<com.baomidou.mybatisplus.extension.plugins.pagination.Page<CareRecord>> query(CareRecordDTO.Query query, HttpServletRequest httpRequest) {
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
        
        return success(careRecordService.query(query, userId, "USER"));
    }

    /**
     * 养护统计详情（功能 3 新增）
     */
    @GetMapping("/stats")
    public Result<CareRecordDTO.StatsResponse> getStats(
            @RequestParam(required = false) Long plantId,
            @RequestParam(defaultValue = "30d") String range,
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
        
        return success(careRecordService.getStats(userId, plantId, range));
    }
}
