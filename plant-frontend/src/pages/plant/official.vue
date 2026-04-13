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

      <div v-loading="loading" class="plant-grid">
        <el-card 
          v-for="plant in plantList" 
          :key="plant.id" 
          class="plant-card" 
          shadow="hover"
          @click="handleDetail(plant)"
        >
          <div class="plant-card-content">
            <div class="plant-image-wrapper">
              <el-image
                v-if="plant.imageUrl"
                :src="plant.imageUrl"
                fit="cover"
                class="plant-thumb"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon size="32"><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="image-placeholder">
                <el-icon size="32"><Picture /></el-icon>
              </div>
            </div>
            <div class="plant-info">
              <h3 class="plant-name">{{ plant.name }}</h3>
              <p class="plant-taxonomy">{{ plant.genus }} · {{ plant.species }}</p>
              <el-tag 
                v-if="plant.difficulty" 
                :type="getDifficultyType(plant.difficulty)" 
                size="small"
                effect="plain"
              >
                {{ plant.difficulty }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </div>

      <div v-if="!loading && plantList.length === 0" class="empty-state">
        <el-empty description="暂无植物数据" />
      </div>

      <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[12, 24, 48]"
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
import { Picture } from '@element-plus/icons-vue'
import request from '@/api/request'

const router = useRouter()

const loading = ref(false)
const plantList = ref([])
const total = ref(0)

const getDifficultyType = (difficulty) => {
  const map = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return map[difficulty] || 'info'
}

const handleDetail = (row) => {
  router.push('/plant/official/' + row.id)
}

const queryParams = reactive({
  keyword: '',
  pageNum: 1,
  pageSize: 12
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

.plant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  min-height: 200px;
}

.plant-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.plant-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.plant-card :deep(.el-card__body) {
  padding: 12px;
}

.plant-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.plant-image-wrapper {
  width: 100%;
  height: 160px;
  border-radius: 8px;
  overflow: hidden;
}

.plant-thumb {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f0f5f0 0%, #e8f0e8 100%);
  color: #a0c4a0;
}

.plant-info {
  text-align: center;
  width: 100%;
}

.plant-name {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.plant-taxonomy {
  margin: 0 0 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.empty-state {
  padding: 40px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
