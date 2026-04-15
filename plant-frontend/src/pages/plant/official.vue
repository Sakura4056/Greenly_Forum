<template>
  <div class="official-plant-library">
    <!-- 顶部标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">🌿 官方植物库</h2>
        <p class="page-subtitle">植物百科知识库 · 已收录 <strong>{{ total }}</strong> 种植物</p>
      </div>
    </div>

    <!-- 搜索 + 筛选 -->
    <div class="search-bar">
      <el-input
        v-model="queryParams.keyword"
        placeholder="搜索植物名称、科属..."
        clearable
        size="large"
        class="search-input"
        @keyup.enter="handleQuery"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" size="large" @click="handleQuery" :loading="loading">
        搜索
      </el-button>
    </div>

    <!-- 难度筛选标签 -->
    <div class="filter-row">
      <span
        class="filter-tag"
        :class="{ active: !queryParams.difficulty }"
        @click="filterByDifficulty('')"
      >全部</span>
      <span
        v-for="d in difficultyOptions"
        :key="d.value"
        class="filter-tag"
        :class="['diff-' + d.cls, { active: queryParams.difficulty === d.value }]"
        @click="filterByDifficulty(d.value)"
      >
        {{ d.icon }} {{ d.label }}
      </span>
    </div>

    <!-- 植物卡片网格 -->
    <div v-loading="loading" class="plant-grid">
      <div
        v-for="plant in plantList"
        :key="plant.id"
        class="plant-card"
        @click="handleDetail(plant)"
      >
        <!-- 图片区域 -->
        <div class="card-image">
          <el-image
            v-if="plant.imageUrl"
            :src="plant.imageUrl"
            fit="cover"
            class="plant-img"
            lazy
          >
            <template #error>
              <div class="img-placeholder">
                <span class="placeholder-emoji">🌿</span>
              </div>
            </template>
          </el-image>
          <div v-else class="img-placeholder">
            <span class="placeholder-emoji">🌿</span>
          </div>
          <!-- 难度角标 -->
          <div v-if="plant.difficulty" class="difficulty-badge" :class="getDifficultyClass(plant.difficulty)">
            {{ plant.difficulty }}
          </div>
        </div>

        <!-- 信息区域 -->
        <div class="card-body">
          <h3 class="plant-name">{{ plant.name }}</h3>
          <p class="plant-taxonomy">{{ plant.genus }} · {{ plant.species }}</p>

          <!-- 快捷标签 -->
          <div class="tag-row">
            <el-tag v-if="plant.lightReq" size="small" effect="plain" type="warning">
              ☀️ {{ plant.lightReq }}
            </el-tag>
            <el-tag v-if="plant.waterReq" size="small" effect="plain" type="primary">
              💧 {{ plant.waterReq }}
            </el-tag>
            <el-tag v-if="plant.bloomSeason" size="small" effect="plain" type="danger">
              🌸 {{ plant.bloomSeason }}
            </el-tag>
          </div>

          <!-- 底部操作 -->
          <div class="card-footer">
            <el-button text type="primary" size="small" @click.stop="handleDetail(plant)">
              查看详情 →
            </el-button>
            <el-button text type="success" size="small" @click.stop="handleQuickAdd(plant)">
              <el-icon><Plus /></el-icon> 收录
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && plantList.length === 0" description="未找到相关植物" />

    <!-- 分页 -->
    <div class="pagination-container" v-if="total > queryParams.pageSize">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[12, 24, 48]"
        :background="true"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, PictureFilled, Plus } from '@element-plus/icons-vue'
import request from '@/api/request'

const router = useRouter()
const loading = ref(false)
const plantList = ref([])
const total = ref(0)

const queryParams = reactive({
  keyword: '',
  category: '',
  difficulty: '',
  pageNum: 1,
  pageSize: 12
})

const difficultyOptions = [
  { value: '简单', label: '简单', icon: '🟢', cls: 'easy' },
  { value: '中等', label: '中等', icon: '🟡', cls: 'medium' },
  { value: '较难', label: '较难', icon: '🔴', cls: 'hard' },
]

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await request.get('/plant/official/query', { params: queryParams })
    plantList.value = res.records
    total.value = res.total
  } catch (e) {
    console.error('查询失败:', e)
  } finally {
    loading.value = false
  }
}

const filterByDifficulty = (val) => {
  queryParams.difficulty = val
  queryParams.pageNum = 1
  // difficulty 后端没单独字段，前端用 keyword 兼容过滤
  // 如果后端 OfficialQuery 没有 difficulty 字段，通过 keyword 搜索
  handleQuery()
}

const handleDetail = (plant) => {
  router.push(`/plant/official/${plant.id}`)
}

const handleQuickAdd = (plant) => {
  router.push({
    path: '/plant/my-add',
    query: {
      officialPlantId: plant.id,
      name: plant.name,
      genus: plant.genus,
      species: plant.species
    }
  })
}

const getDifficultyClass = (difficulty) => {
  return { '简单': 'easy', '中等': 'medium', '困难': 'hard', '较难': 'hard' }[difficulty] || ''
}

onMounted(() => { handleQuery() })
</script>

<style scoped>
.official-plant-library {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header { margin-bottom: 20px; }
.page-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 4px;
  color: var(--el-text-color-primary);
}
.page-subtitle {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin: 0;
}
.page-subtitle strong { color: var(--el-color-primary); }

/* 搜索栏 */
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.search-input { max-width: 400px; }

/* 筛选标签 */
.filter-row {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.filter-tag {
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid var(--el-border-color-lighter);
  color: var(--el-text-color-regular);
  background: var(--el-bg-color);
  transition: all 0.2s;
  user-select: none;
}

.filter-tag:hover {
  border-color: var(--el-color-primary-light-5);
  color: var(--el-color-primary);
}

.filter-tag.active {
  background: var(--el-color-primary);
  color: #fff;
  border-color: var(--el-color-primary);
}

.filter-tag.diff-easy.active { background: #67c23a; border-color: #67c23a; }
.filter-tag.diff-medium.active { background: #e6a23c; border-color: #e6a23c; }
.filter-tag.diff-hard.active { background: #f56c6c; border-color: #f56c6c; }

/* 卡片网格 */
.plant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 200px;
}

.plant-card {
  background: var(--el-bg-color);
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.plant-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: var(--el-color-primary-light-5);
}

.card-image {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #f0f9f0 0%, #e8f5e9 100%);
}

.plant-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.plant-card:hover .plant-img { transform: scale(1.05); }

.img-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
}

.placeholder-emoji {
  font-size: 56px;
  filter: drop-shadow(0 2px 6px rgba(0,0,0,0.08));
}

.difficulty-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  backdrop-filter: blur(4px);
}
.difficulty-badge.easy { background: rgba(103,194,58,0.85); }
.difficulty-badge.medium { background: rgba(230,162,60,0.85); }
.difficulty-badge.hard { background: rgba(245,108,108,0.85); }

.card-body {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.plant-name {
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 4px;
}

.plant-taxonomy {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin: 0 0 12px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 28px;
  padding-bottom: 20px;
}

@media (max-width: 768px) {
  .official-plant-library { padding: 16px; }
  .plant-grid { grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; }
  .card-image { height: 140px; }
  .card-body { padding: 12px; }
  .plant-name { font-size: 15px; }
  .search-bar { flex-direction: column; }
  .search-input { max-width: 100%; }
  .filter-tag { padding: 4px 12px; font-size: 12px; }
}
</style>
