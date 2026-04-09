package com.plant.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.base.BaseController;
import com.plant.backend.dto.AnnouncementDTO;
import com.plant.backend.entity.Announcement;
import com.plant.backend.service.AnnouncementService;
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
 * 公告控制器
 * 处理系统公告发布、查询等请求
 * 
 * @author Greenly Team
 * @date 2026-04-05
 */
@Slf4j
@Tag(name = "公告管理", description = "管理员发布公告接口")
@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AnnouncementController extends BaseController {

    private final AnnouncementService announcementService;
    private final JwtUtil jwtUtil;

    /**
     * 发布公告
     * 管理员发布系统公告，自动为所有用户创建提醒
     */
    @Operation(summary = "发布公告", description = "管理员发布系统公告，自动为所有用户创建提醒")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Announcement> publish(@RequestBody @Valid AnnouncementDTO.PublishRequest request,
                                        HttpServletRequest httpRequest) {
        String token = extractToken(httpRequest);
        Long publisherId = jwtUtil.getUserIdFromToken(token);
        
        log.info("接收到发布公告请求，标题：{}，发布人 ID：{}", request.getTitle(), publisherId);
        
        Announcement announcement = announcementService.publishAnnouncement(
            publisherId, request.getTitle(), request.getContent()
        );
        
        logOperation("ANNOUNCEMENT_PUBLISH", "发布公告: " + request.getTitle());
        return success(announcement);
    }

    /**
     * 查询公告列表（公开接口）
     * 分页查询已发布的公告列表，无需认证
     */
    @Operation(summary = "公告列表（公开）", description = "分页查询已发布的公告列表，无需认证")
    @GetMapping("/public/list")
    public Result<Page<AnnouncementDTO.AnnouncementResponse>> publicList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        
        log.debug("查询公开公告列表，页码：{}，每页数量：{}", pageNum, pageSize);
        
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        return success(announcementService.listAnnouncements(page));
    }

    /**
     * 查询公告列表
     * 分页查询公告列表
     */
    @Operation(summary = "公告列表", description = "分页查询公告列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<AnnouncementDTO.AnnouncementResponse>> list(
            @Parameter(description = "页码", example = "1")
            @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        
        log.debug("查询公告列表，页码：{}，每页数量：{}", pageNum, pageSize);
        
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        return success(announcementService.listAnnouncements(page));
    }
}
