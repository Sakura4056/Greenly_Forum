package com.plant.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.base.BaseController;
import com.plant.backend.dto.MyPlantDTO;
import com.plant.backend.entity.MyPlant;
import com.plant.backend.service.MyPlantService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/my-plant")
@RequiredArgsConstructor
public class MyPlantController extends BaseController {

    private final MyPlantService myPlantService;
    private final JwtUtil jwtUtil;

    private Long getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            log.warn("未提供有效的认证令牌");
            return null;
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无效的认证令牌");
        }
        return userId;
    }

    @PostMapping
    public Result<Long> addMyPlant(@Valid @RequestBody MyPlantDTO.AddRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        return Result.success(myPlantService.addMyPlant(userId, request));
    }

    @PutMapping("/{id}")
    public Result<Void> updateMyPlant(@PathVariable Long id, @Valid @RequestBody MyPlantDTO.UpdateRequest request, HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        myPlantService.updateMyPlant(userId, id, request);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMyPlant(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        myPlantService.deleteMyPlant(userId, id);
        return Result.success(null);
    }

    @GetMapping
    public Result<Page<MyPlant>> getMyPlants(MyPlantDTO.QueryRequest query, HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        return Result.success(myPlantService.getMyPlants(userId, query));
    }

    @GetMapping("/{id}")
    public Result<MyPlant> getMyPlant(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        return Result.success(myPlantService.getMyPlantById(userId, id));
    }

    @GetMapping("/{id}/detail")
    public Result<MyPlantDTO.DetailResponse> getMyPlantDetail(@PathVariable Long id, HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        return Result.success(myPlantService.getMyPlantDetail(userId, id));
    }
}
