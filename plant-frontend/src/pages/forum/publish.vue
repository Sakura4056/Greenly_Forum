<template>
    <div class="publish-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span>发布帖子</span>
                </div>
            </template>

            <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
                                <el-form-item label="分类" prop="categoryId">
                    <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
                        <el-option 
                            v-for="cat in categories" 
                            :key="cat.categoryId" 
                            :label="cat.icon + ' ' + cat.name" 
                            :value="cat.categoryId" 
                        />
                    </el-select>
                </el-form-item>
                
                <el-form-item label="标题" prop="title">
                    <el-input 
                        v-model="form.title" 
                        placeholder="请输入帖子标题（最多200字）"
                        maxlength="200"
                        show-word-limit
                        clearable
                    />
                </el-form-item>
                
                <el-form-item label="内容" prop="content">
                    <el-input
                        v-model="form.content"
                        type="textarea"
                        :rows="12"
                        placeholder="分享你的植物养护经验、心得体会或问题..."
                        maxlength="5000"
                        show-word-limit
                    />
                </el-form-item>
                
                <el-form-item>
                    <el-button type="primary" @click="handleSubmit" :loading="submitting" size="large">
                        发布
                    </el-button>
                    <el-button @click="$router.back()" size="large">取消</el-button>
                </el-form-item>
            </el-form>

            <!-- 发帖提示 -->
            <el-alert
                title="发帖须知"
                type="info"
                :closable="false"
                show-icon
                class="tips-alert"
            >
                <template #default>
                    <ul>
                        <li>请遵守社区规范，文明发言</li>
                        <li>禁止发布广告、色情、暴力等不良内容</li>
                        <li>鼓励分享植物养护经验和心得</li>
                        <li>违规内容将被删除，严重者可能被封号</li>
                    </ul>
                </template>
            </el-alert>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { createPost, getCategories } from '@/api/forum'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])

const form = reactive({
    title: '',
    content: '',
    categoryId: null
})

const rules = {
    categoryId: [
        { required: true, message: '请选择分类', trigger: 'change' }
    ],
    title: [
        { required: true, message: '请输入标题', trigger: 'blur' },
        { min: 2, max: 200, message: '标题长度在 2 到 200 个字符', trigger: 'blur' }
    ],
    content: [
        { required: true, message: '请输入内容', trigger: 'blur' },
        { min: 5, max: 5000, message: '内容长度在 5 到 5000 个字符', trigger: 'blur' }
    ]
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
    if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
    }

    await formRef.value.validate((valid) => {
        if (!valid) {
            return false
        }
    })

    submitting.value = true
    try {
        console.log('发布帖子，提交数据:', form)
        console.log('当前用户信息:', userStore.userInfo)
        console.log('Token:', userStore.token ? '存在' : '不存在')
        
        await createPost(form)
        ElMessage.success('发布成功')
        router.push('/forum/list')
    } catch (error) {
        console.error('发布失败详情:', error)
        console.error('错误响应:', error.response?.data)
        ElMessage.error('发布失败：' + (error.message || '未知错误'))
    } finally {
        submitting.value = false
    }
}

onMounted(async () => {
    // 检查登录状态
    if (!userStore.isLoggedIn) {
        ElMessage.warning('请先登录')
        router.push('/login')
        return
    }
    // 获取分类列表
    try {
        const res = await getCategories()
        categories.value = res || []
    } catch (error) {
        console.error('获取分类失败:', error)
    }
})
</script>

<style scoped lang="scss">
.publish-container {
    padding: 20px;
    max-width: 900px;
    margin: 0 auto;

    .card-header {
        font-size: 18px;
        font-weight: bold;
    }

    .tips-alert {
        margin-top: 20px;

        ul {
            margin: 0;
            padding-left: 20px;
            line-height: 1.8;

            li {
                font-size: 14px;
                color: var(--el-text-color-regular);
            }
        }
    }
}
</style>

