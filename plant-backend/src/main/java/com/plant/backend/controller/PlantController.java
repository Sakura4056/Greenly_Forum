package com.plant.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.dto.PlantDTO;
import com.plant.backend.entity.OfficialPlant;
import com.plant.backend.service.PlantService;
import com.plant.backend.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 植物管理控制器
 * <p>
 * 提供官方植物库查询、植物详情等接口
 * </p>
 *
 * @author Greenly Team
 * @date 2026-04-05
 * @version 1.0
 */
@Tag(name = "植物管理", description = "官方植物库查询、植物详情接口")
@RestController
@RequestMapping("/api/plant")
public class PlantController {

    private final PlantService plantService;

    public PlantController(PlantService plantService) {
        this.plantService = plantService;
    }

    /**
     * 查询官方植物库
     * <p>
     * 支持分页、关键词搜索、分类筛选
     * </p>
     *
     * @param query 查询参数（页码、每页数量、关键词、分类）
     * @return 官方植物列表分页数据
     */
    @Operation(summary = "查询官方植物库", description = "分页查询官方植物库，支持关键词搜索和分类筛选")
    @GetMapping("/official/query")
    public Result<Page<OfficialPlant>> queryOfficial(PlantDTO.OfficialQuery query) {
        return Result.success(plantService.queryOfficial(query));
    }
    
    /**
     * 获取官方植物详情
     *
     * @param id 植物ID
     * @return 植物详细信息
     */
    @Operation(summary = "获取植物详情", description = "根据ID获取官方植物的详细信息")
    @GetMapping("/official/{id}")
    public Result<OfficialPlant> getOfficialDetail(
            @Parameter(description = "植物ID", example = "1")
            @PathVariable Long id) {
        return Result.success(plantService.getOfficialDetail(id));
    }
}
