package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.plant.backend.dto.ForumDTO;
import com.plant.backend.entity.ForumPost;

/**
 * 论坛帖子服务接口
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
public interface ForumPostService extends IService<ForumPost> {

    /**
     * 创建帖子
     *
     * @param userId  用户ID
     * @param request 发帖请求
     * @return 创建的帖子
     */
    ForumPost createPost(Long userId, ForumDTO.CreatePostRequest request);

    /**
     * 分页查询帖子列表
     *
     * @param query         查询条件
     * @param currentUserId 当前用户ID（可为null）
     * @return 帖子分页数据
     */
    Page<ForumDTO.PostResponse> listPosts(ForumDTO.PostQueryRequest query, Long currentUserId);

    /**
     * 获取帖子详情
     *
     * @param postId        帖子ID
     * @param currentUserId 当前用户ID（可为null）
     * @return 帖子详情
     */
    ForumDTO.PostResponse getPostDetail(Long postId, Long currentUserId);

    ForumPost editPost(Long postId, Long userId, ForumDTO.EditPostRequest request);

    /**
     * 删除帖子（软删除）
     *
     * @param postId 帖子ID
     * @param userId 用户ID（用于权限验证）
     */
    void deletePost(Long postId, Long userId);

    /**
     * 增加浏览量
     *
     * @param postId 帖子ID
     */
    void incrementViewCount(Long postId);

    /**
     * 管理员置顶/取消置顶
     *
     * @param postId 帖子ID
     * @param isTop  是否置顶
     */
    void toggleTop(Long postId, boolean isTop);

    /**
     * 管理员加精/取消加精
     *
     * @param postId    帖子ID
     * @param isEssence 是否精华
     */
    void toggleEssence(Long postId, boolean isEssence);

    /**
     * 点赞/取消点赞
     *
     * @param userId     用户ID
     * @param targetType 目标类型（POST/COMMENT）
     * @param targetId   目标ID
     */
    void toggleLike(Long userId, String targetType, Long targetId);
}
