<template>
  <div class="app-container">
    <div class="page-header mb-6">
      <div class="header-left">
        <h2 class="page-title">养护计划</h2>
        <p class="page-subtitle">查看和管理您的植物养护任务</p>
      </div>
      <div class="header-right">
        <el-button type="primary" icon="Plus" @click="$router.push('/care/schedule-add')" round>新增计划</el-button>
      </div>
    </div>

    <el-card shadow="never" class="filter-card mb-4 glass-panel">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="状态筛选">
          <el-radio-group v-model="queryParams.status" @change="handleQuery">
            <el-radio-button value="">全部</el-radio-button>
            <el-radio-button :value="0">未完成</el-radio-button>
            <el-radio-button :value="1">已完成</el-radio-button>
            <el-radio-button :value="2">逾期</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" plain icon="Search" @click="handleQuery" :loading="loading">刷新</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <div class="table-container glass-panel">
      <el-table 
        v-loading="loading" 
        :data="scheduleList" 
        style="width: 100%"
        :header-cell-style="{ background: '#f8fafc', color: '#606266', fontWeight: '600' }"
      >
        <el-table-column prop="taskName" label="任务名称" min-width="150">
          <template #default="{ row }">
            <span class="font-medium text-main">{{ row.taskName }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="plantSource" label="植物来源" width="120">
          <template #default="{ row }">
            <el-tag type="info" effect="plain" round size="small">{{ row.plantSource }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="重复规则" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.recurrenceType === 'NONE' || !row.recurrenceType" type="info" effect="light" size="small">单次任务</el-tag>
            <el-tag v-else type="primary" effect="light" size="small">
              每 {{ row.recurrenceInterval }} {{ row.recurrenceType === 'DAY' ? '天' : row.recurrenceType === 'MONTH' ? '月' : '年' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="dueTime" label="截止/完成时间" width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(row.dueTime) }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" effect="light">未完成</el-tag>
            <el-tag v-else-if="row.status === 1" type="success" effect="light">已完成</el-tag>
            <el-tag v-else type="danger" effect="light">逾期</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="success" link icon="Check" @click="completeTask(row)">打卡</el-button>
            <el-button v-if="row.status === 0" type="primary" link icon="Edit" @click="editTask(row)">编辑</el-button>
            <el-button type="danger" link icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
        
        <template #empty>
          <el-empty description="暂无养护计划" />
        </template>
      </el-table>
      
      <div class="pagination-container p-4">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          layout="total, prev, pager, next"
          :total="total"
          @current-change="handleQuery"
          background
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import request from '@/api/request'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Clock, Check, Edit, Delete } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const scheduleList = ref([])
const total = ref(0)

const queryParams = reactive({
  userId: userStore.userId,
  status: '', // Changed to empty string for 'All'
  pageNum: 1,
  pageSize: 10
})

const formatTime = (arr) => {
  if(Array.isArray(arr)) {
    return `${arr[0]}-${String(arr[1]).padStart(2,'0')}-${String(arr[2]).padStart(2,'0')} ${String(arr[3]).padStart(2,'0')}:${String(arr[4]).padStart(2,'0')}`
  }
  return arr
}

const handleQuery = async () => {
  loading.value = true
  try {
    // If status is empty string, send undefined to backend if backend expects null for All, or keep as is if backend handles it.
    // Assuming backend treats null/undefined as All.
    const params = { ...queryParams }
    if (params.status === '') params.status = undefined
    
    const res = await request.get('/care/schedule/query', { params })
    scheduleList.value = res.records
    total.value = res.total
  } catch(e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const completeTask = (row) => {
    router.push({
        path: '/care/record-add',
        query: {
            scheduleId: row.id,
            plantId: row.plantId,
            plantSource: row.plantSource,
            taskName: row.taskName
        }
    })
}

const editTask = (row) => {
    router.push({
        path: '/care/schedule-add',
        query: {
            id: row.id,
            plantId: row.plantId,
            plantSource: row.plantSource,
            taskName: row.taskName,
            dueTime: formatTime(row.dueTime),
            recurrenceType: row.recurrenceType,
            recurrenceInterval: row.recurrenceInterval
        }
    })
}

const handleDelete = (row) => {
    ElMessageBox.confirm(
        '确认删除该养护计划吗？',
        '警告',
        {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning',
        }
    ).then(async () => {
        try {
            await request.delete(`/care/schedule/delete/${row.id}`)
            ElMessage.success('删除成功')
            handleQuery()
        } catch(e) {}
    })
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped lang="scss">
.mb-6 { margin-bottom: 24px; }
.mb-4 { margin-bottom: 16px; }

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  
  .header-left {
    .page-title {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
      color: var(--color-text-main);
    }
    .page-subtitle {
      margin: 4px 0 0;
      color: var(--color-text-secondary);
      font-size: 14px;
    }
  }
}

.filter-card {
  border: none;
  background-color: var(--color-surface);
  
  :deep(.el-card__body) {
    padding: 16px 24px;
  }
}

.filter-form {
  margin-bottom: 0;
  
  :deep(.el-form-item) {
    margin-bottom: 0;
    margin-right: 24px;
  }
}

.table-container {
  background-color: var(--color-surface);
  border-radius: var(--border-radius-base);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  padding: 0;
}

.font-medium {
  font-weight: 500;
}

.text-main {
  color: var(--color-text-main);
}

.flex {
  display: flex;
}

.items-center {
  align-items: center;
}

.gap-2 {
  gap: 8px;
}

.p-4 {
  padding: 16px;
}
</style>
