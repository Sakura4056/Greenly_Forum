package com.plant.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.plant.backend.dto.ForumDTO;
import com.plant.backend.entity.ForumComment;
import com.plant.backend.entity.ForumLike;
import com.plant.backend.entity.ForumPost;
import com.plant.backend.entity.User;
import com.plant.backend.exception.BusinessException;
import com.plant.backend.mapper.ForumCommentMapper;
import com.plant.backend.mapper.ForumLikeMapper;
import com.plant.backend.service.ForumCommentService;
import com.plant.backend.service.ForumPostService;
import com.plant.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 论坛评论服务实现类
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForumCommentServiceImpl extends ServiceImpl<ForumCommentMapper, ForumComment> implements ForumCommentService {

    private final UserService userService;
    @Lazy
    private final ForumPostService forumPostService;
    private final ForumLikeMapper forumLikeMapper;
    private final ForumCommentMapper forumCommentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumComment createComment(Long userId, ForumDTO.CreateCommentRequest request) {
        log.info("用户 {} 对帖子 {} 发表评论", userId, request.getPostId());

        // 1. 校验帖子是否存在
        ForumPost post = forumPostService.getById(request.getPostId());
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException("帖子不存在或已被隐藏");
        }

        // 2. 校验用户状态
        User user = userService.getById(userId);
        if (user == null || user.getStatus() == 0) {
            throw new BusinessException("用户状态异常");
        }

        // 3. 如果是回复评论，校验父评论是否存在
        if (request.getParentId() != null) {
            ForumComment parentComment = getById(request.getParentId());
            if (parentComment == null) {
                throw new BusinessException("父评论不存在");
            }
        }

        // 4. 创建评论
        ForumComment comment = new ForumComment();
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setParentId(request.getParentId());
        comment.setReplyToUserId(request.getReplyToUserId());
        comment.setContent(request.getContent());
        comment.setLikeCount(0);
        comment.setStatus(1);

        save(comment);

        // 5. 更新帖子的回复数
        forumPostService.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ForumPost>()
                        .eq(ForumPost::getPostId, request.getPostId())
                        .setSql("reply_count = reply_count + 1"));

        log.info("评论发布成功，评论ID：{}", comment.getCommentId());

        return comment;
    }

    @Override
    public Page<ForumDTO.CommentResponse> listCommentsByPost(Long postId, Integer pageNum, Integer pageSize, Long currentUserId) {
        log.debug("查询帖子 {} 的评论列表，页码：{}，每页数量：{}", postId, pageNum, pageSize);

        Page<ForumComment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ForumComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumComment::getPostId, postId)
                .eq(ForumComment::getStatus, 1)
                .orderByAsc(ForumComment::getCreateTime);

        Page<ForumComment> result = page(page, wrapper);

        // 转换为 DTO 并关联用户信息
        List<ForumDTO.CommentResponse> responses = convertToCommentResponses(result.getRecords(), currentUserId);

        Page<ForumDTO.CommentResponse> dtoPage = new Page<>();
        dtoPage.setCurrent(result.getCurrent());
        dtoPage.setSize(result.getSize());
        dtoPage.setTotal(result.getTotal());
        dtoPage.setRecords(responses);

        return dtoPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumComment editComment(Long commentId, Long userId, ForumDTO.EditCommentRequest request) {
        ForumComment comment = getById(commentId);
        if (comment == null || comment.getStatus() == 0) throw new BusinessException("评论不存在或已被删除");
        if (!comment.getUserId().equals(userId)) throw new BusinessException("无权限编辑此评论");
        comment.setContent(request.getContent());
        updateById(comment);
        return comment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long userId) {
        log.info("删除评论，评论ID：{}，操作用户：{}", commentId, userId);

        ForumComment comment = getById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 权限验证：仅作者或管理员可删除
        User currentUser = userService.getById(userId);
        if (!comment.getUserId().equals(userId) && !"ADMIN".equals(currentUser.getRole())) {
            throw new BusinessException("无权限删除此评论");
        }

        // 软删除
        removeById(commentId);

        // 更新帖子的回复数
        forumPostService.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ForumPost>()
                        .eq(ForumPost::getPostId, comment.getPostId())
                        .setSql("reply_count = GREATEST(reply_count - 1, 0)"));

        log.info("评论删除成功");
    }

    /**
     * 转换评论列表为响应 DTO
     */
    private List<ForumDTO.CommentResponse> convertToCommentResponses(List<ForumComment> comments, Long currentUserId) {
        if (comments == null || comments.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 批量查询用户信息
        List<Long> userIds = comments.stream().map(ForumComment::getUserId).distinct().collect(Collectors.toList());
        // 添加回复目标用户ID
        comments.stream().map(ForumComment::getReplyToUserId)
                .filter(id -> id != null)
                .forEach(userIds::add);
        
        userIds = userIds.stream().distinct().collect(Collectors.toList());
        List<User> users = userService.listByIds(userIds);
        java.util.Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        // 批量查询当前用户的点赞状态
        java.util.Set<Long> likedCommentIds = java.util.Collections.emptySet();
        if (currentUserId != null) {
            List<ForumLike> likes = forumLikeMapper.selectList(new LambdaQueryWrapper<ForumLike>()
                    .eq(ForumLike::getUserId, currentUserId)
                    .eq(ForumLike::getTargetType, "COMMENT")
                    .in(ForumLike::getTargetId, comments.stream().map(ForumComment::getCommentId).collect(Collectors.toList())));
            likedCommentIds = likes.stream().map(ForumLike::getTargetId).collect(Collectors.toSet());
        }

        final java.util.Set<Long> finalLikedCommentIds = likedCommentIds;
        return comments.stream().map(comment -> {
            ForumDTO.CommentResponse response = convertToCommentResponse(comment, currentUserId);
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                response.setUsername(user.getUsername());
                response.setNickname(user.getNickname());
                response.setAvatar(user.getAvatar());
            }

            // 设置回复目标用户名
            if (comment.getReplyToUserId() != null) {
                User replyToUser = userMap.get(comment.getReplyToUserId());
                if (replyToUser != null) {
                    response.setReplyToUsername(replyToUser.getUsername());
                }
            }

            response.setIsLiked(finalLikedCommentIds.contains(comment.getCommentId()));
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 转换单个评论为响应 DTO
     */
    private ForumDTO.CommentResponse convertToCommentResponse(ForumComment comment, Long currentUserId) {
        ForumDTO.CommentResponse response = new ForumDTO.CommentResponse();
        response.setCommentId(comment.getCommentId());
        response.setPostId(comment.getPostId());
        response.setUserId(comment.getUserId());
        response.setParentId(comment.getParentId());
        response.setReplyToUserId(comment.getReplyToUserId());
        response.setContent(comment.getContent());
        response.setLikeCount(comment.getLikeCount());
        response.setCreateTime(comment.getCreateTime());

        // 查询用户信息
        User user = userService.getById(comment.getUserId());
        if (user != null) {
            response.setUsername(user.getUsername());
            response.setNickname(user.getNickname());
            response.setAvatar(user.getAvatar());
        }

        // 查询回复目标用户名
        if (comment.getReplyToUserId() != null) {
            User replyToUser = userService.getById(comment.getReplyToUserId());
            if (replyToUser != null) {
                response.setReplyToUsername(replyToUser.getUsername());
            }
        }

        // 查询当前用户是否已点赞
        if (currentUserId != null) {
            LambdaQueryWrapper<ForumLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ForumLike::getUserId, currentUserId)
                    .eq(ForumLike::getTargetType, "COMMENT")
                    .eq(ForumLike::getTargetId, comment.getCommentId());
            response.setIsLiked(forumLikeMapper.selectCount(wrapper) > 0);
        } else {
            response.setIsLiked(false);
        }

        return response;
    }
}
