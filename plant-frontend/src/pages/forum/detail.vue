<template>
    <div class="detail-container" v-loading="loading">
        <!-- 帖子详情 -->
        <el-card v-if="post" class="post-detail-card">
            <div class="post-header">
                <div class="author-info">
                    <el-avatar :size="50" :src="post.avatar || defaultAvatar" />
                    <div class="author-meta">
                        <span class="nickname">{{ post.nickname || post.username }}</span>
                        <span class="time">{{ post.createTime }}</span>
                    </div>
                
                    <el-tag v-if="post.categoryName" type="info" style="margin-left: 15px;">{{ post.categoryName }}</el-tag>
                </div>
                <div class="post-actions">
                    <el-tag v-if="post.isTop === 1" type="danger">置顶</el-tag>
                    <el-tag v-if="post.isEssence === 1" type="warning">精华</el-tag>
                    <el-button 
                        v-if="canDelete" 
                        type="danger" 
                        size="small" 
                        @click="handleDeletePost"
                    >
                        删除
                    </el-button>
                    <el-button 
                        v-if="isAdmin" 
                        size="small" 
                        @click="handleToggleTop"
                    >
                        {{ post.isTop === 1 ? '取消置顶' : '置顶' }}
                    </el-button>
                    <el-button 
                        v-if="isAdmin" 
                        size="small" 
                        @click="handleToggleEssence"
                    >
                        {{ post.isEssence === 1 ? '取消加精' : '加精' }}
                    </el-button>
                </div>
            </div>

            <h1 class="post-title">{{ post.title }}</h1>

            <div class="post-content">
                {{ post.content }}
            </div>

            <div class="post-stats">
                <span><el-icon><View /></el-icon> 浏览 {{ post.viewCount }}</span>
                <span><el-icon><ChatDotRound /></el-icon> 回复 {{ post.replyCount }}</span>
                <span>
                    <el-icon><Star /></el-icon> 点赞 {{ post.likeCount }}
                    <el-button 
                        :type="post.isLiked ? 'primary' : 'default'" 
                        size="small" 
                        circle
                        @click="handleLikePost"
                        class="like-btn"
                    >
                        <el-icon><Star /></el-icon>
                    </el-button>
                </span>
            </div>
        </el-card>

        <!-- 评论区 -->
        <el-card class="comments-card" v-if="post">
            <template #header>
                <div class="comments-header">
                    <span>评论 ({{ post.replyCount }})</span>
                </div>
            </template>

            <!-- 发表评论 -->
            <div class="comment-form">
                <el-input
                    v-model="commentForm.content"
                    type="textarea"
                    :rows="3"
                    placeholder="写下你的评论..."
                    maxlength="2000"
                    show-word-limit
                />
                <div class="form-actions">
                    <el-button type="primary" @click="handleSubmitComment" :loading="submitting">
                        发表评论
                    </el-button>
                </div>
            </div>

            <!-- 评论列表 -->
            <div class="comments-list" v-loading="commentsLoading">
                <div v-for="comment in comments" :key="comment.commentId" class="comment-item">
                    <div class="comment-header">
                        <el-avatar :size="36" :src="comment.avatar || defaultAvatar" />
                        <div class="comment-meta">
                            <span class="nickname">{{ comment.nickname || comment.username }}</span>
                            <span class="time">{{ comment.createTime }}</span>
                        </div>
                        <el-button 
                            v-if="canDeleteComment(comment)" 
                            type="danger" 
                            link 
                            size="small"
                            @click="handleDeleteComment(comment.commentId)"
                        >
                            删除
                        </el-button>
                    </div>
                    <div class="comment-content">
                        <span v-if="comment.replyToUsername" class="reply-to">
                            回复 @{{ comment.replyToUsername }}：
                        </span>
                        {{ comment.content }}
                    </div>
                    <div class="comment-footer">
                        <el-button 
                            :type="comment.isLiked ? 'primary' : 'default'" 
                            size="small" 
                            text
                            @click="handleLikeComment(comment)"
                        >
                            <el-icon><Star /></el-icon>
                            {{ comment.likeCount }}
                        </el-button>
                        <el-button size="small" text @click="handleReplyComment(comment)">
                            <el-icon><ChatDotRound /></el-icon>
                            回复
                        </el-button>
                    </div>
                </div>

                <el-empty v-if="!commentsLoading && comments.length === 0" description="暂无评论，快来抢沙发吧！" />
            </div>
        </el-card>
    </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { View, ChatDotRound, Star } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { 
    getPostDetail, 
    getCommentList, 
    createComment, 
    deletePost, 
    deleteComment,
    toggleLike,
    toggleTop,
    toggleEssence
} from '@/api/forum'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const post = ref(null)
const comments = ref([])
const loading = ref(false)
const commentsLoading = ref(false)
const submitting = ref(false)
const commentForm = ref({ content: '' })
const replyingTo = ref(null) // 正在回复的评论
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 计算属性：当前用户是否可以删除帖子
const canDelete = computed(() => {
    if (!userStore.isLoggedIn || !post.value) return false
    return userStore.userId === post.value.userId || userStore.isAdmin
})

// 计算属性：当前用户是否是管理员
const isAdmin = computed(() => {
    return userStore.isLoggedIn && userStore.isAdmin
})

/**
 * 获取帖子详情
 */
const fetchPostDetail = async () => {
    loading.value = true
    try {
        const res = await getPostDetail(route.params.id)
        post.value = res  // 拦截器已解包
    } catch (error) {
        ElMessage.error('获取帖子详情失败')
        console.error(error)
    } finally {
        loading.value = false
    }
}

/**
 * 获取评论列表
 */
const fetchComments = async () => {
    commentsLoading.value = true
    try {
        const res = await getCommentList({
            postId: route.params.id,
            pageNum: 1,
            pageSize: 50
        })
        // 请求拦截器已经返回 res.data，所以直接访问 res.records
        comments.value = res.records || []
    } catch (error) {
        ElMessage.error('获取评论列表失败')
        console.error(error)
    } finally {
        commentsLoading.value = false
    }
}

/**
 * 提交评论
 */
const handleSubmitComment = async () => {
    if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
    }

    if (!commentForm.value.content.trim()) {
        ElMessage.warning('评论内容不能为空')
        return
    }

    submitting.value = true
    try {
        await createComment({
            postId: route.params.id,
            content: commentForm.value.content,
            parentId: replyingTo.value?.commentId || null,
            replyToUserId: replyingTo.value?.userId || null
        })

        ElMessage.success('评论成功')
        commentForm.value.content = ''
        replyingTo.value = null
        await fetchComments()
        await fetchPostDetail() // 刷新回复数
    } catch (error) {
        ElMessage.error('评论失败')
        console.error(error)
    } finally {
        submitting.value = false
    }
}

/**
 * 点赞帖子
 */
const handleLikePost = async () => {
    if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
    }

    try {
        await toggleLike({
            targetType: 'POST',
            targetId: post.value.postId
        })
        await fetchPostDetail() // 刷新点赞数
    } catch (error) {
        ElMessage.error('操作失败')
        console.error(error)
    }
}

/**
 * 点赞评论
 */
const handleLikeComment = async (comment) => {
    if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
    }

    try {
        await toggleLike({
            targetType: 'COMMENT',
            targetId: comment.commentId
        })
        await fetchComments() // 刷新评论列表
    } catch (error) {
        ElMessage.error('操作失败')
        console.error(error)
    }
}

/**
 * 回复评论
 */
const handleReplyComment = (comment) => {
    if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
    }

    replyingTo.value = comment
    commentForm.value.content = `回复 @${comment.nickname || comment.username}：`
    // 聚焦到输入框（可以通过ref实现）
}

/**
 * 删除帖子
 */
const handleDeletePost = async () => {
    try {
        await ElMessageBox.confirm('确定要删除这个帖子吗？', '提示', {
            type: 'warning'
        })

        await deletePost(post.value.postId)
        ElMessage.success('删除成功')
        router.back()
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('删除失败')
            console.error(error)
        }
    }
}

/**
 * 删除评论
 */
const handleDeleteComment = async (commentId) => {
    try {
        await ElMessageBox.confirm('确定要删除这条评论吗？', '提示', {
            type: 'warning'
        })

        await deleteComment(commentId)
        ElMessage.success('删除成功')
        await fetchComments()
        await fetchPostDetail() // 刷新回复数
    } catch (error) {
        if (error !== 'cancel') {
            ElMessage.error('删除失败')
            console.error(error)
        }
    }
}

/**
 * 判断是否可以删除评论
 */
const canDeleteComment = (comment) => {
    if (!userStore.isLoggedIn) return false
    return userStore.userId === comment.userId || userStore.isAdmin
}

/**
 * 置顶/取消置顶
 */
const handleToggleTop = async () => {
    try {
        await toggleTop(post.value.postId, post.value.isTop === 0)
        ElMessage.success('操作成功')
        await fetchPostDetail()
    } catch (error) {
        ElMessage.error('操作失败')
        console.error(error)
    }
}

/**
 * 加精/取消加精
 */
const handleToggleEssence = async () => {
    try {
        await toggleEssence(post.value.postId, post.value.isEssence === 0)
        ElMessage.success('操作成功')
        await fetchPostDetail()
    } catch (error) {
        ElMessage.error('操作失败')
        console.error(error)
    }
}

onMounted(() => {
    fetchPostDetail()
    fetchComments()
})
</script>

<style scoped lang="scss">
.detail-container {
    padding: 20px;
    max-width: 1000px;
    margin: 0 auto;

    .post-detail-card {
        margin-bottom: 20px;

        .post-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;

            .author-info {
                display: flex;
                align-items: center;
                gap: 12px;

                .author-meta {
                    display: flex;
                    flex-direction: column;

                    .nickname {
                        font-weight: bold;
                        color: var(--el-text-color-primary);
                    }

                    .time {
                        font-size: 12px;
                        color: var(--el-text-color-secondary);
                    }
                }
            }

            .post-actions {
                display: flex;
                gap: 8px;
                align-items: center;
            }
        }

        .post-title {
            font-size: 24px;
            margin: 20px 0;
            color: var(--el-text-color-primary);
        }

        .post-content {
            font-size: 16px;
            line-height: 1.8;
            color: var(--el-text-color-regular);
            white-space: pre-wrap;
            margin: 20px 0;
        }

        .post-stats {
            display: flex;
            gap: 20px;
            padding-top: 15px;
            border-top: 1px solid var(--el-border-color-lighter);
            color: var(--el-text-color-secondary);
            font-size: 14px;

            span {
                display: flex;
                align-items: center;
                gap: 4px;

                .like-btn {
                    margin-left: 8px;
                }
            }
        }
    }

    .comments-card {
        .comments-header {
            font-weight: bold;
            font-size: 16px;
        }

        .comment-form {
            margin-bottom: 20px;

            .form-actions {
                margin-top: 10px;
                text-align: right;
            }
        }

        .comments-list {
            .comment-item {
                padding: 15px 0;
                border-bottom: 1px solid var(--el-border-color-lighter);

                &:last-child {
                    border-bottom: none;
                }

                .comment-header {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    margin-bottom: 10px;

                    .comment-meta {
                        flex: 1;
                        display: flex;
                        flex-direction: column;

                        .nickname {
                            font-weight: bold;
                            font-size: 14px;
                        }

                        .time {
                            font-size: 12px;
                            color: var(--el-text-color-secondary);
                        }
                    }
                }

                .comment-content {
                    font-size: 14px;
                    line-height: 1.6;
                    color: var(--el-text-color-regular);
                    margin-bottom: 10px;

                    .reply-to {
                        color: var(--el-color-primary);
                    }
                }

                .comment-footer {
                    display: flex;
                    gap: 15px;
                }
            }
        }
    }
}
</style>

