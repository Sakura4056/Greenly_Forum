package com.plant.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.plant.backend.base.BaseController;
import com.plant.backend.dto.ForumDTO;
import com.plant.backend.entity.ForumComment;
import com.plant.backend.entity.ForumPost;
import com.plant.backend.entity.ForumCategory;
import com.plant.backend.mapper.ForumCategoryMapper;
import com.plant.backend.service.ForumCommentService;
import com.plant.backend.service.ForumPostService;
import com.plant.backend.util.JwtUtil;
import com.plant.backend.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 论坛控制器
 * 处理社区论坛帖子、评论、点赞等请求
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@Slf4j
@Tag(name = "社区论坛", description = "论坛帖子和评论管理接口")
@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController extends BaseController {

    private final ForumPostService forumPostService;
    private final ForumCategoryMapper categoryMapper;
    private final ForumCommentService forumCommentService;
    private final JwtUtil jwtUtil;

    /**
     * 创建帖子
     */
    @Operation(summary = "发布帖子", description = "登录用户发布新帖子")
    @PostMapping("/posts")
    public Result<ForumPost> createPost(@RequestBody @Valid ForumDTO.CreatePostRequest request,
                                        HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        log.info("用户 {} 发布帖子：{}", userId, request.getTitle());

        ForumPost post = forumPostService.createPost(userId, request);
        return success(post);
    }

    /**
     * 分页查询帖子列表
     */
    @Operation(summary = "帖子列表", description = "分页查询帖子列表，支持排序")
    @GetMapping("/posts")
    public Result<Page<ForumDTO.PostResponse>> listPosts(ForumDTO.PostQueryRequest query,
                                                         HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserIdOrNull(httpRequest);
        return success(forumPostService.listPosts(query, currentUserId));
    }

    /**
     * 获取帖子详情
     */
    @Operation(summary = "帖子详情", description = "获取帖子详细信息，自动增加浏览量")
    @GetMapping("/posts/{postId}")
    public Result<ForumDTO.PostResponse> getPostDetail(@PathVariable Long postId,
                                                       HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserIdOrNull(httpRequest);
        forumPostService.incrementViewCount(postId);
        return success(forumPostService.getPostDetail(postId, currentUserId));
    }

    @Operation(summary = "编辑帖子", description = "仅作者可编辑自己的帖子")
    @PutMapping("/posts/{postId}")
    public Result<ForumPost> editPost(@PathVariable Long postId, @RequestBody @Valid ForumDTO.EditPostRequest request, HttpServletRequest httpRequest) {
        return success(forumPostService.editPost(postId, getUserId(httpRequest), request));
    }

    /**
     * 删除帖子
     */
    @Operation(summary = "删除帖子", description = "仅作者或管理员可删除")
    @DeleteMapping("/posts/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId,
                                   HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        forumPostService.deletePost(postId, userId);
        return success();
    }

    /**
     * 发表评论
     */
    @Operation(summary = "发表评论", description = "对帖子发表评论或回复评论")
    @PostMapping("/comments")
    public Result<ForumComment> createComment(@RequestBody @Valid ForumDTO.CreateCommentRequest request,
                                              HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        ForumComment comment = forumCommentService.createComment(userId, request);
        return success(comment);
    }

    /**
     * 查询帖子的评论列表
     */
    @Operation(summary = "评论列表", description = "分页查询指定帖子的评论")
    @GetMapping("/comments")
    public Result<Page<ForumDTO.CommentResponse>> listComments(
            @Parameter(description = "帖子ID", required = true)
            @RequestParam Long postId,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "20") Integer pageSize,
            HttpServletRequest httpRequest) {
        Long currentUserId = getCurrentUserIdOrNull(httpRequest);
        return success(forumCommentService.listCommentsByPost(postId, pageNum, pageSize, currentUserId));
    }

    @Operation(summary = "编辑评论", description = "仅作者可编辑自己的评论")
    @PutMapping("/comments/{commentId}")
    public Result<ForumComment> editComment(@PathVariable Long commentId, @RequestBody @Valid ForumDTO.EditCommentRequest request, HttpServletRequest httpRequest) {
        return success(forumCommentService.editComment(commentId, getUserId(httpRequest), request));
    }

    /**
     * 删除评论
     */
    @Operation(summary = "删除评论", description = "仅作者或管理员可删除")
    @DeleteMapping("/comments/{commentId}")
    public Result<Void> deleteComment(@PathVariable Long commentId,
                                      HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        forumCommentService.deleteComment(commentId, userId);
        return success();
    }

    /**
     * 点赞/取消点赞
     */
    @Operation(summary = "点赞", description = "对帖子或评论点赞/取消点赞")
    @PostMapping("/likes")
    public Result<Void> toggleLike(@RequestBody @Valid ForumDTO.LikeRequest request,
                                   HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        forumPostService.toggleLike(userId, request.getTargetType(), request.getTargetId());
        return success();
    }

    /**
     * 管理员置顶
     */
    @Operation(summary = "置顶帖子", description = "管理员操作")
    @PutMapping("/posts/{postId}/top")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> toggleTop(@PathVariable Long postId,
                                  @RequestParam boolean isTop) {
        forumPostService.toggleTop(postId, isTop);
        return success();
    }

    /**
     * 管理员加精
     */
    @Operation(summary = "加精帖子", description = "管理员操作")
    @PutMapping("/posts/{postId}/essence")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> toggleEssence(@PathVariable Long postId,
                                      @RequestParam boolean isEssence) {
        forumPostService.toggleEssence(postId, isEssence);
        return success();
    }

    /**
     * 从请求中获取当前用户ID
     */
    private Long getUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            throw new com.plant.backend.exception.BusinessException(
                    com.plant.backend.util.ResultCode.UNAUTHORIZED.getCode(), "未认证");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new com.plant.backend.exception.BusinessException(
                    com.plant.backend.util.ResultCode.UNAUTHORIZED.getCode(), "无效的Token");
        }
        return userId;
    }

    /**
     * 从请求中获取当前用户ID（允许未登录）
     */
    private Long getCurrentUserIdOrNull(HttpServletRequest request) {
        try {
            return getUserId(request);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取论坛分类列表
     */
    @Operation(summary = "分类列表", description = "获取所有可用的论坛分类")
    @GetMapping("/categories")
    public Result<List<ForumCategory>> listCategories() {
        List<ForumCategory> categories = categoryMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ForumCategory>()
                .eq(ForumCategory::getStatus, 1)
                .orderByAsc(ForumCategory::getSortOrder)
        );
        return success(categories);
    }
}


