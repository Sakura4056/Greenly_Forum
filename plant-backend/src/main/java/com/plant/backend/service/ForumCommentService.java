package com.plant.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.plant.backend.dto.ForumDTO;
import com.plant.backend.entity.ForumComment;

/**
 * 论坛评论服务接口
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
public interface ForumCommentService extends IService<ForumComment> {

    /**
     * 发表评论
     *
     * @param userId  用户ID
     * @param request 评论请求
     * @return 创建的评论
     */
    ForumComment createComment(Long userId, ForumDTO.CreateCommentRequest request);

    /**
     * 分页查询帖子的评论列表
     *
     * @param postId        帖子ID
     * @param pageNum       页码
     * @param pageSize      每页数量
     * @param currentUserId 当前用户ID（可为null）
     * @return 评论分页数据
     */
    Page<ForumDTO.CommentResponse> listCommentsByPost(Long postId, Integer pageNum, Integer pageSize, Long currentUserId);

    ForumComment editComment(Long commentId, Long userId, ForumDTO.EditCommentRequest request);

    /**
     * 删除评论（软删除）
     *
     * @param commentId 评论ID
     * @param userId    用户ID（用于权限验证）
     */
    void deleteComment(Long commentId, Long userId);
}
