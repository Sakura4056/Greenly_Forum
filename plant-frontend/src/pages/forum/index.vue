<template>
    <div class="forum-container">
        <!-- 顶部操作栏 -->
        <el-card class="header-card">
            <div class="header-actions">
                <el-radio-group v-model="sortBy" @change="handleSortChange">
                    <el-radio-button value="latest">最新</el-radio-button>
                    <el-radio-button value="hot">热门</el-radio-button>
                    <el-radio-button value="top">置顶</el-radio-button>
                </el-radio-group>
                
                <el-select v-model="selectedCategory" placeholder="全部分类" clearable @change="handleCategoryChange" style="width: 150px; margin-left: 15px;">
                    <el-option label="全部分类" :value="null" />
                    <el-option v-for="cat in categories" :key="cat.categoryId" :label="cat.icon + ' ' + cat.name" :value="cat.categoryId" />
                </el-select>
                <el-button type="primary" @click="$router.push('/forum/publish')">
                    <el-icon><Plus /></el-icon>
                    发布帖子
                </el-button>
            </div>
        </el-card>

        <!-- 帖子列表 -->
        <div v-loading="loading">
            <el-card v-for="post in postList" :key="post.postId" class="post-card" shadow="hover">
                <div class="post-item" @click="goToDetail(post.postId)">
                    <div class="post-header">
                        <el-avatar :size="40" :src="post.avatar || defaultAvatar" />
                        <div class="post-meta">
                            <span class="nickname">{{ post.nickname || post.username }}</span>
                            <span class="time">{{ post.createTime }}</span>
                        </div>
                        <el-tag v-if="post.isTop === 1" type="danger" size="small">置顶</el-tag>
                        <el-tag v-if="post.isEssence === 1" type="warning" size="small">精华</el-tag>
                        <el-tag v-if="post.categoryName" type="info" size="small">{{ post.categoryName }}</el-tag>
                    </div>
                    <h3 class="post-title">{{ post.title }}</h3>
                    <p class="post-preview">{{ truncateContent(post.content, 150) }}</p>
                    <div class="post-footer">
                        <span><el-icon><View /></el-icon> {{ post.viewCount }}</span>
                        <span><el-icon><ChatDotRound /></el-icon> {{ post.replyCount }}</span>
                        <span><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
                    </div>
                </div>
            </el-card>

            <!-- 空状态 -->
            <el-empty v-if="!loading && postList.length === 0" description="暂无帖子，快来发布第一个帖子吧！" />
        </div>

        <!-- 分页 -->
        <el-pagination
            v-if="total > 0"
            v-model:current-page="pageNum"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="fetchPostList"
            @size-change="handleSizeChange"
            class="pagination"
        />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus, View, ChatDotRound, Star } from '@element-plus/icons-vue'
import { getPostList, getCategories } from '@/api/forum'

const router = useRouter()
const postList = ref([])
const sortBy = ref('latest')
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const categories = ref([])
const selectedCategory = ref(null)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'


/**
 * 获取分类列表
 */
const fetchCategories = async () => {
    try {
        const res = await getCategories()
        categories.value = res || []
    } catch (error) {
        console.error('获取分类失败:', error)
    }
}
/**
 * 获取帖子列表
 */
const fetchPostList = async () => {
    loading.value = true
    try {
        const res = await getPostList({
            pageNum: pageNum.value,
            pageSize: pageSize.value,
            sortBy: sortBy.value,
            categoryId: selectedCategory.value
        })
        // 请求拦截器已经返回 res.data，所以直接访问 res.records
        postList.value = res.records || []
        total.value = res.total || 0
    } catch (error) {
        ElMessage.error('获取帖子列表失败')
        console.error(error)
    } finally {
        loading.value = false
    }
}


/**
 * 分类筛选改变
 */
const handleCategoryChange = (categoryId) => {
    selectedCategory.value = categoryId
    pageNum.value = 1
    fetchPostList()
}
/**
 * 排序方式改变
 */
const handleSortChange = () => {
    pageNum.value = 1
    fetchPostList()
}

/**
 * 每页数量改变
 */
const handleSizeChange = () => {
    pageNum.value = 1
    fetchPostList()
}

/**
 * 跳转到帖子详情
 */
const goToDetail = (postId) => {
    router.push(`/forum/detail/${postId}`)
}

/**
 * 截断内容
 */
const truncateContent = (content, maxLength) => {
    if (!content) return ''
    if (content.length <= maxLength) return content
    return content.substring(0, maxLength) + '...'
}

onMounted(() => {
    fetchCategories()
    fetchPostList()
})
</script>

<style scoped lang="scss">
.forum-container {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;

    .header-card {
        margin-bottom: 20px;

        .header-actions {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
    }

    .post-card {
        margin-bottom: 15px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
            box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
            transform: translateY(-2px);
        }

        .post-item {
            .post-header {
                display: flex;
                align-items: center;
                gap: 10px;
                margin-bottom: 10px;

                .post-meta {
                    flex: 1;
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

            .post-title {
                font-size: 18px;
                margin: 10px 0;
                color: var(--el-color-primary);
                font-weight: 600;
            }

            .post-preview {
                color: var(--el-text-color-regular);
                line-height: 1.6;
                margin: 10px 0;
            }

            .post-footer {
                display: flex;
                gap: 20px;
                margin-top: 10px;
                color: var(--el-text-color-secondary);
                font-size: 14px;

                span {
                    display: flex;
                    align-items: center;
                    gap: 4px;
                }
            }
        }
    }

    .pagination {
        margin-top: 20px;
        display: flex;
        justify-content: center;
    }
}
</style>

