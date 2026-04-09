import request from '@/api/request'

/**
 * 获取帖子列表
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.sortBy - 排序方式：latest/hot/top
 */
export function getPostList(params) {
    return request({
        url: '/forum/posts',
        method: 'get',
        params
    })
}

/**
 * 获取帖子详情
 * @param {number} postId - 帖子ID
 */
export function getPostDetail(postId) {
    return request({
        url: `/forum/posts/${postId}`,
        method: 'get'
    })
}

/**
 * 发布帖子
 * @param {Object} data - 帖子数据
 * @param {string} data.title - 标题
 * @param {string} data.content - 内容
 */
export function createPost(data) {
    return request({
        url: '/forum/posts',
        method: 'post',
        data
    })
}

/**
 * 删除帖子
 * @param {number} postId - 帖子ID
 */
export function deletePost(postId) {
    return request({
        url: `/forum/posts/${postId}`,
        method: 'delete'
    })
}

/**
 * 获取评论列表
 * @param {Object} params - 查询参数
 * @param {number} params.postId - 帖子ID
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 */
export function getCommentList(params) {
    return request({
        url: '/forum/comments',
        method: 'get',
        params
    })
}

/**
 * 发表评论
 * @param {Object} data - 评论数据
 * @param {number} data.postId - 帖子ID
 * @param {string} data.content - 评论内容
 * @param {number} data.parentId - 父评论ID（可选）
 * @param {number} data.replyToUserId - 回复的用户ID（可选）
 */
export function createComment(data) {
    return request({
        url: '/forum/comments',
        method: 'post',
        data
    })
}

/**
 * 删除评论
 * @param {number} commentId - 评论ID
 */
export function deleteComment(commentId) {
    return request({
        url: `/forum/comments/${commentId}`,
        method: 'delete'
    })
}

/**
 * 点赞/取消点赞
 * @param {Object} data - 点赞数据
 * @param {string} data.targetType - 目标类型：POST/COMMENT
 * @param {number} data.targetId - 目标ID
 */
export function toggleLike(data) {
    return request({
        url: '/forum/likes',
        method: 'post',
        data
    })
}

/**
 * 置顶/取消置顶帖子（管理员）
 * @param {number} postId - 帖子ID
 * @param {boolean} isTop - 是否置顶
 */
export function toggleTop(postId, isTop) {
    return request({
        url: `/forum/posts/${postId}/top`,
        method: 'put',
        params: { isTop }
    })
}

/**
 * 加精/取消加精帖子（管理员）
 * @param {number} postId - 帖子ID
 * @param {boolean} isEssence - 是否精华
 */
export function toggleEssence(postId, isEssence) {
    return request({
        url: `/forum/posts/${postId}/essence`,
        method: 'put',
        params: { isEssence }
    })
}

/**
 * 获取论坛分类列表
 */
export function getCategories() {
    return request({
        url: '/forum/categories',
        method: 'get'
    })
}

