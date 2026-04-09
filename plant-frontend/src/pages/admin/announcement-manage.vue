<template>
  <div class="app-container">
    <!-- 发布公告表单 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发布公告</span>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="公告标题" prop="title">
          <el-input 
            v-model="form.title" 
            placeholder="请输入公告标题" 
            maxlength="200" 
            show-word-limit 
            clearable
          />
        </el-form-item>
        
        <el-form-item label="公告内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="8" 
            placeholder="请输入公告内容"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            发布公告
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 历史公告列表 -->
    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>历史公告</span>
        </div>
      </template>

      <el-table :data="announcementList" border v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="publishTime" label="发布时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchList"
          @size-change="fetchList"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
/**
 * 公告管理页面
 * 管理员可以发布系统公告，查看所有历史公告
 */
import { ref, reactive, onMounted } from 'vue'
import { publishAnnouncement, getAnnouncementList } from '@/api/announcement'
import { ElMessage } from 'element-plus'

const formRef = ref(null)
const loading = ref(false)
const form = reactive({
  title: '',
  content: ''
})

const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' },
    { min: 1, max: 200, message: '标题长度在 1 到 200 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' },
    { min: 1, max: 5000, message: '内容长度在 1 到 5000 个字符', trigger: 'blur' }
  ]
}

const announcementList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

/**
 * 提交发布公告
 */
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    
    await publishAnnouncement({
      title: form.title,
      content: form.content
    })
    
    ElMessage.success('公告发布成功')
    handleReset()
    fetchList()
  } catch (error) {
    console.error('发布公告失败', error)
  } finally {
    loading.value = false
  }
}

/**
 * 重置表单
 */
const handleReset = () => {
  form.title = ''
  form.content = ''
  formRef.value?.clearValidate()
}

/**
 * 获取公告列表
 */
const fetchList = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList(queryParams)
    announcementList.value = res.records
    total.value = res.total
  } catch (error) {
    console.error('获取公告列表失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 16px;
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
