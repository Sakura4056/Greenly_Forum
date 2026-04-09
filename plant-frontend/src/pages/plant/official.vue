<template>
  <div class="official-plant-library">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-content">
            <span class="page-title">🌿 官方植物库</span>
            <span class="page-subtitle">植物百科知识库 - 可添加到你的植物库</span>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="关键词">
          <el-input v-model="queryParams.keyword" placeholder="植物名称/科属" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="plantList" border style="width: 100%">
        <el-table-column prop="name" label="名称" width="180" />
        <el-table-column prop="genus" label="属" width="120" />
        <el-table-column prop="species" label="种" width="120" />
        <el-table-column label="操作" width="100">
             <template #default="scope">
                 <el-button link type="primary" @click="handleDetail(scope.row)">详情</el-button>
             </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'

const router = useRouter()

const loading = ref(false)
const plantList = ref([])
const total = ref(0)

const handleDetail = (row) => {
    router.push(`/plant/official/${row.id}`)
}

const queryParams = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 10
})

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await request.get('/plant/official/query', { params: queryParams })
    plantList.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.pageNum = 1
  handleQuery()
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.official-plant-library {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}

.page-subtitle {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
</style>
