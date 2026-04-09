package com.plant.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.base.BaseController;
import com.plant.backend.dto.PlantDiaryDTO;
import com.plant.backend.service.PlantDiaryService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Plant Diary Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/diary")
@RequiredArgsConstructor
public class PlantDiaryController extends BaseController {

    private final PlantDiaryService plantDiaryService;
    private final JwtUtil jwtUtil;

    /**
     * Create diary entry
     */
    @PostMapping
    public Result<Long> createDiary(@RequestBody PlantDiaryDTO.CreateRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        Long diaryId = plantDiaryService.createDiary(userId, request);
        return Result.success(diaryId);
    }

    /**
     * Update diary entry
     */
    @PutMapping
    public Result<Void> updateDiary(@RequestBody PlantDiaryDTO.UpdateRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        plantDiaryService.updateDiary(userId, request);
        return Result.success(null);
    }

    /**
     * Delete diary entry
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDiary(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        plantDiaryService.deleteDiary(userId, id);
        return Result.success(null);
    }

    /**
     * Get diary by ID
     */
    @GetMapping("/{id}")
    public Result<PlantDiaryDTO.DiaryResponse> getDiaryById(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(plantDiaryService.getDiaryById(userId, id));
    }

    /**
     * Query diaries with pagination
     */
    @GetMapping("/query")
    public Result<Page<PlantDiaryDTO.DiaryResponse>> queryDiaries(PlantDiaryDTO.QueryRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(plantDiaryService.queryDiaries(userId, request));
    }

    /**
     * Get diaries by plant ID
     */
    @GetMapping("/plant/{plantId}")
    public Result<List<PlantDiaryDTO.DiaryResponse>> getDiariesByPlant(@PathVariable Long plantId, HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(plantDiaryService.getDiariesByPlant(userId, plantId));
    }

    /**
     * Get mood statistics
     */
    @GetMapping("/mood-stats")
    public Result<Map<String, Integer>> getMoodStatistics(
            @RequestParam(required = false) Long plantId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletRequest httpRequest) {
        Long userId = getUserIdFromRequest(httpRequest);
        return Result.success(plantDiaryService.getMoodStatistics(userId, plantId, startDate, endDate));
    }

    /**
     * Extract user ID from JWT token
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            throw new RuntimeException("未授权访问");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
            throw new RuntimeException("认证失败");
        }
        return userId;
    }
}
