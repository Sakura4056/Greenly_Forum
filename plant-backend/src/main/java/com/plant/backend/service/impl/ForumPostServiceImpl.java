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
import com.plant.backend.mapper.ForumPostMapper;
import com.plant.backend.mapper.ForumCategoryMapper;
import com.plant.backend.entity.ForumCategory;
import com.plant.backend.service.ForumPostService;
import com.plant.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 论坛帖子服务实现类
 *
 * @author Greenly Team
 * @date 2026-04-05
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForumPostServiceImpl extends ServiceImpl<ForumPostMapper, ForumPost> implements ForumPostService {

    private final UserService userService;
    private final ForumCommentMapper forumCommentMapper;
    private final ForumLikeMapper forumLikeMapper;
    private final ForumPostMapper forumPostMapper;
    private final ForumCategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumPost createPost(Long userId, ForumDTO.CreatePostRequest request) {
        log.info("=== 开始发布帖子 ===");
        log.info("userId: {}", userId);
        log.info("request.title: {}", request.getTitle());
        log.info("request.content length: {}", request.getContent() != null ? request.getContent().length() : 0);

        // 1. 校验用户状态
        if (userId == null) {
            log.error("userId 为 null，无法发布帖子");
            throw new BusinessException("未登录或登录已过期");
        }
        
        User user = userService.getById(userId);
        if (user == null) {
            log.error("用户不存在，userId: {}", userId);
            throw new BusinessException("用户不存在");
        }
        if (user.getStatus() == 0) {
            log.error("用户状态异常，userId: {}, status: {}", userId, user.getStatus());
            throw new BusinessException("用户状态异常");
        }

        // 2. 创建帖子
        ForumPost post = new ForumPost();
        post.setUserId(userId);
        post.setCategoryId(request.getCategoryId());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        if (request.getCategoryId() != null) {
            post.setCategoryId(request.getCategoryId());
        }
        post.setViewCount(0);
        post.setReplyCount(0);
        post.setLikeCount(0);
        post.setIsTop(0);
        post.setIsEssence(0);
        post.setStatus(1);

        save(post);
        log.info("帖子发布成功，帖子ID：{}", post.getPostId());

        return post;
    }

    @Override
    public Page<ForumDTO.PostResponse> listPosts(ForumDTO.PostQueryRequest query, Long currentUserId) {
        log.debug("查询帖子列表，页码：{}，每页数量：{}，排序：{}",
                query.getPageNum(), query.getPageSize(), query.getSortBy());

        Page<ForumPost> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumPost::getStatus, 1);
        
        // 分类筛选
        if (query.getCategoryId() != null) {
            wrapper.eq(ForumPost::getCategoryId, query.getCategoryId());
        }

        // 排序逻辑
        if ("hot".equals(query.getSortBy())) {
            wrapper.orderByDesc(ForumPost::getLikeCount, ForumPost::getCreateTime);
        } else if ("top".equals(query.getSortBy())) {
            wrapper.orderByDesc(ForumPost::getIsTop, ForumPost::getCreateTime);
        } else {
            wrapper.orderByDesc(ForumPost::getCreateTime);
        }

        Page<ForumPost> result = page(page, wrapper);

        // 转换为 DTO 并关联用户信息
        List<ForumDTO.PostResponse> responses = convertToPostResponses(result.getRecords(), currentUserId);

        Page<ForumDTO.PostResponse> dtoPage = new Page<>();
        dtoPage.setCurrent(result.getCurrent());
        dtoPage.setSize(result.getSize());
        dtoPage.setTotal(result.getTotal());
        dtoPage.setRecords(responses);

        return dtoPage;
    }

    @Override
    public ForumDTO.PostResponse getPostDetail(Long postId, Long currentUserId) {
        log.debug("获取帖子详情，帖子ID：{}", postId);

        ForumPost post = getById(postId);
        if (post == null || post.getStatus() == 0) {
            throw new BusinessException("帖子不存在或已被隐藏");
        }

        return convertToPostResponse(post, currentUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ForumPost editPost(Long postId, Long userId, ForumDTO.EditPostRequest request) {
        ForumPost post = getById(postId);
        if (post == null || post.getStatus() == 0) throw new BusinessException("帖子不存在或已被隐藏");
        if (!post.getUserId().equals(userId)) throw new BusinessException("无权限编辑此帖子");
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        if (request.getCategoryId() != null) {
            post.setCategoryId(request.getCategoryId());
        }
        updateById(post);
        return post;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId) {
        log.info("删除帖子，帖子ID：{}，操作用户：{}", postId, userId);

        ForumPost post = getById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        // 权限验证：仅作者或管理员可删除
        User currentUser = userService.getById(userId);
        if (!post.getUserId().equals(userId) && !"ADMIN".equals(currentUser.getRole())) {
            throw new BusinessException("无权限删除此帖子");
        }

        // 软删除
        removeById(postId);
        log.info("帖子删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long postId) {
        forumPostMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ForumPost>()
                        .eq(ForumPost::getPostId, postId)
                        .setSql("view_count = view_count + 1"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleTop(Long postId, boolean isTop) {
        log.info("{}帖子，帖子ID：{}", isTop ? "置顶" : "取消置顶", postId);

        ForumPost post = getById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        post.setIsTop(isTop ? 1 : 0);
        updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleEssence(Long postId, boolean isEssence) {
        log.info("{}帖子，帖子ID：{}", isEssence ? "加精" : "取消加精", postId);

        ForumPost post = getById(postId);
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }

        post.setIsEssence(isEssence ? 1 : 0);
        updateById(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleLike(Long userId, String targetType, Long targetId) {
        log.debug("用户 {} 对 {} {} 进行点赞/取消点赞操作", userId, targetType, targetId);

        // 1. 检查是否已点赞
        LambdaQueryWrapper<ForumLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumLike::getUserId, userId)
                .eq(ForumLike::getTargetType, targetType)
                .eq(ForumLike::getTargetId, targetId);

        ForumLike existingLike = forumLikeMapper.selectOne(wrapper);

        if (existingLike != null) {
            // 取消点赞
            forumLikeMapper.deleteById(existingLike.getLikeId());
            decrementLikeCount(targetType, targetId);
            log.debug("取消点赞成功");
        } else {
            // 添加点赞
            ForumLike like = new ForumLike();
            like.setUserId(userId);
            like.setTargetType(targetType);
            like.setTargetId(targetId);
            forumLikeMapper.insert(like);
            incrementLikeCount(targetType, targetId);
            log.debug("点赞成功");
        }
    }

    /**
     * 增加点赞数
     */
    private void incrementLikeCount(String targetType, Long targetId) {
        if ("POST".equals(targetType)) {
            forumPostMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ForumPost>()
                            .eq(ForumPost::getPostId, targetId)
                            .setSql("like_count = like_count + 1"));
        } else if ("COMMENT".equals(targetType)) {
            forumCommentMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ForumComment>()
                            .eq(ForumComment::getCommentId, targetId)
                            .setSql("like_count = like_count + 1"));
        }
    }

    /**
     * 减少点赞数
     */
    private void decrementLikeCount(String targetType, Long targetId) {
        if ("POST".equals(targetType)) {
            forumPostMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ForumPost>()
                            .eq(ForumPost::getPostId, targetId)
                            .setSql("like_count = GREATEST(like_count - 1, 0)"));
        } else if ("COMMENT".equals(targetType)) {
            forumCommentMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<ForumComment>()
                            .eq(ForumComment::getCommentId, targetId)
                            .setSql("like_count = GREATEST(like_count - 1, 0)"));
        }
    }

    /**
     * 转换帖子列表为响应 DTO
     */
    private List<ForumDTO.PostResponse> convertToPostResponses(List<ForumPost> posts, Long currentUserId) {
        if (posts == null || posts.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // 批量查询用户信息
        List<Long> userIds = posts.stream().map(ForumPost::getUserId).distinct().collect(Collectors.toList());
        List<User> users = userService.listByIds(userIds);
        java.util.Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        // 批量查询当前用户的点赞状态
        java.util.Set<Long> likedPostIds = java.util.Collections.emptySet();
        if (currentUserId != null) {
            List<ForumLike> likes = forumLikeMapper.selectList(new LambdaQueryWrapper<ForumLike>()
                    .eq(ForumLike::getUserId, currentUserId)
                    .eq(ForumLike::getTargetType, "POST")
                    .in(ForumLike::getTargetId, posts.stream().map(ForumPost::getPostId).collect(Collectors.toList())));
            likedPostIds = likes.stream().map(ForumLike::getTargetId).collect(Collectors.toSet());
        }

        final java.util.Set<Long> finalLikedPostIds = likedPostIds;
        return posts.stream().map(post -> {
            ForumDTO.PostResponse response = convertToPostResponse(post, currentUserId);
            User user = userMap.get(post.getUserId());
            if (user != null) {
                response.setUsername(user.getUsername());
                response.setNickname(user.getNickname());
                response.setAvatar(user.getAvatar());
            }
            response.setIsLiked(finalLikedPostIds.contains(post.getPostId()));
            
            // 设置分类名称
            if (post.getCategoryId() != null) {
                // 简单查询分类名称（可优化为批量查询）
                try {
                    var category = categoryMapper.selectById(post.getCategoryId());
                    if (category != null) {
                        response.setCategoryName(category.getName());
                    }
                } catch (Exception e) {
                    log.warn("查询分类名称失败: categoryId={}", post.getCategoryId());
                }
            }
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 转换单个帖子为响应 DTO
     */
    private ForumDTO.PostResponse convertToPostResponse(ForumPost post, Long currentUserId) {
        ForumDTO.PostResponse response = new ForumDTO.PostResponse();
        response.setPostId(post.getPostId());
        response.setUserId(post.getUserId());
        response.setCategoryId(post.getCategoryId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setViewCount(post.getViewCount());
        response.setReplyCount(post.getReplyCount());
        response.setLikeCount(post.getLikeCount());
        response.setIsTop(post.getIsTop());
        response.setIsEssence(post.getIsEssence());
        response.setCreateTime(post.getCreateTime());
        
        // 设置分类名称
        if (post.getCategoryId() != null) {
            try {
                var category = categoryMapper.selectById(post.getCategoryId());
                if (category != null) {
                    response.setCategoryName(category.getName());
                }
            } catch (Exception e) {
                log.warn("查询分类名称失败: categoryId={}", post.getCategoryId());
            }
        }

        // 查询用户信息
        User user = userService.getById(post.getUserId());
        if (user != null) {
            response.setUsername(user.getUsername());
            response.setNickname(user.getNickname());
            response.setAvatar(user.getAvatar());
        }

        // 查询当前用户是否已点赞
        if (currentUserId != null) {
            LambdaQueryWrapper<ForumLike> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ForumLike::getUserId, currentUserId)
                    .eq(ForumLike::getTargetType, "POST")
                    .eq(ForumLike::getTargetId, post.getPostId());
            response.setIsLiked(forumLikeMapper.selectCount(wrapper) > 0);
        } else {
            response.setIsLiked(false);
        }

        return response;
    }
}


