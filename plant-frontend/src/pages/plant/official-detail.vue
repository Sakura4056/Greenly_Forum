<template>
  <div class="detail-page">
    <!-- 返回 + 标题 -->
    <div class="page-top">
      <el-page-header @back="$router.back()">
        <template #content>
          <span class="page-title">{{ plant.name || '植物详情' }}</span>
        </template>
      </el-page-header>
    </div>

    <div v-if="plant && plant.name" class="detail-body" v-loading="loading">
      <!-- Hero 区域 -->
      <div class="hero-section">
        <el-image
          v-if="plant.imageUrl"
          :src="plant.imageUrl"
          :preview-src-list="[plant.imageUrl]"
          fit="cover"
          class="hero-img"
        >
          <template #error>
            <div class="hero-placeholder">
              <span class="hero-emoji">🌱</span>
              <span class="hero-name">{{ plant.name }}</span>
            </div>
          </template>
        </el-image>
        <div v-else class="hero-placeholder">
          <span class="hero-emoji">🌱</span>
          <span class="hero-name">{{ plant.name }}</span>
        </div>
        <!-- 渐变遮罩 -->
        <div class="hero-overlay">
          <h1 class="hero-title">{{ plant.name }}</h1>
          <p class="hero-subtitle">{{ plant.genus }} · {{ plant.species }}</p>
        </div>
        <!-- 难度徽章 -->
        <div v-if="plant.difficulty" class="hero-badge" :class="getDifficultyClass(plant.difficulty)">
          {{ plant.difficulty }}
        </div>
      </div>

      <!-- 养护参数卡片组 -->
      <div class="stat-cards">
        <div class="stat-card" v-for="item in careStats" :key="item.label">
          <div class="stat-icon">{{ item.icon }}</div>
          <div class="stat-info">
            <div class="stat-label">{{ item.label }}</div>
            <div class="stat-value">{{ item.value || '未知' }}</div>
          </div>
        </div>
      </div>

      <!-- 植物描述 -->
      <div class="section" v-if="plant.description">
        <h3 class="section-title">📖 植物简介</h3>
        <p class="section-text">{{ plant.description }}</p>
      </div>

      <!-- 土壤要求 -->
      <div class="section" v-if="plant.soilReq">
        <h3 class="section-title">🌱 土壤要求</h3>
        <p class="section-text">{{ plant.soilReq }}</p>
      </div>

      <!-- 养护小贴士 -->
      <div class="section tips-section" v-if="plant.careTips">
        <h3 class="section-title">💡 养护小贴士</h3>
        <div class="tips-box">
          <p class="section-text">{{ plant.careTips }}</p>
        </div>
      </div>

      <!-- 常见病虫害 -->
      <div class="section" v-if="plant.commonDiseases">
        <h3 class="section-title">🐛 常见病虫害</h3>
        <p class="section-text">{{ plant.commonDiseases }}</p>
      </div>

      <!-- 底部操作 -->
      <div class="bottom-bar">
        <el-button type="primary" size="large" round @click="handleAddToMyPlant">
          <el-icon><Plus /></el-icon>
          添加到我的植物
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '@/api/request'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const plant = ref({})

const careStats = computed(() => [
  { icon: '☀️', label: '光照需求', value: plant.value.lightReq },
  { icon: '💧', label: '浇水频率', value: plant.value.waterReq },
  { icon: '🌡️', label: '适宜温度', value: plant.value.tempRange },
  { icon: '🌸', label: '花期', value: plant.value.bloomSeason },
])

const getDifficultyClass = (d) => {
  return { '简单': 'easy', '中等': 'medium', '困难': 'hard', '较难': 'hard' }[d] || ''
}

const fetchPlantDetail = async () => {
  loading.value = true
  try {
    const res = await request.get(`/plant/official/${route.params.id}`)
    plant.value = res
  } catch (error) {
    console.error('获取植物详情失败:', error)
    ElMessage.error('获取植物详情失败')
  } finally {
    loading.value = false
  }
}

const handleAddToMyPlant = () => {
  router.push({
    path: '/plant/my-add',
    query: {
      officialPlantId: plant.value.id,
      name: plant.value.name,
      genus: plant.value.genus,
      species: plant.value.species
    }
  })
}

onMounted(() => { fetchPlantDetail() })
</script>

<style scoped>
.detail-page {
  padding: 20px;
  max-width: 860px;
  margin: 0 auto;
}

.page-top {
  margin-bottom: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
}

/* Hero */
.hero-section {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  height: 360px;
  margin-bottom: 24px;
}

.hero-img {
  width: 100%;
  height: 100%;
}

.hero-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 50%, #a5d6a7 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 12px;
}

.hero-emoji {
  font-size: 80px;
  filter: drop-shadow(0 4px 12px rgba(0,0,0,0.1));
}

.hero-name {
  font-size: 28px;
  font-weight: 700;
  color: #2e7d32;
}

.hero-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 40px 28px 24px;
  background: linear-gradient(transparent, rgba(0,0,0,0.65));
  color: #fff;
}

.hero-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 4px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.3);
}

.hero-subtitle {
  font-size: 15px;
  margin: 0;
  opacity: 0.9;
}

.hero-badge {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  backdrop-filter: blur(6px);
}

.hero-badge.easy { background: rgba(103,194,58,0.9); }
.hero-badge.medium { background: rgba(230,162,60,0.9); }
.hero-badge.hard { background: rgba(245,108,108,0.9); }

/* 养护参数卡片 */
.stat-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 28px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: var(--el-bg-color, #fff);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  border-radius: 12px;
  transition: all 0.2s;
}

.stat-card:hover {
  border-color: var(--el-color-primary-light-5, #a0cfff);
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}

.stat-icon {
  font-size: 28px;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 10px;
  flex-shrink: 0;
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399);
  margin-bottom: 2px;
}

.stat-value {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary, #303133);
}

/* 内容区块 */
.section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 12px;
  color: var(--el-text-color-primary, #303133);
}

.section-text {
  font-size: 14px;
  line-height: 1.9;
  color: var(--el-text-color-regular, #606266);
  margin: 0;
}

.tips-section .tips-box {
  background: linear-gradient(135deg, #fff8e1, #fff3e0);
  border-left: 4px solid #ff9800;
  padding: 16px 20px;
  border-radius: 0 12px 12px 0;
}

/* 底部操作 */
.bottom-bar {
  text-align: center;
  padding: 28px 0 12px;
  border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
}

/* 移动端 */
@media (max-width: 768px) {
  .detail-page { padding: 12px; }
  .hero-section { height: 260px; }
  .hero-title { font-size: 22px; }
  .hero-emoji { font-size: 56px; }
  .stat-cards { grid-template-columns: repeat(2, 1fr); gap: 8px; }
  .stat-card { padding: 12px; gap: 10px; }
  .stat-icon { font-size: 22px; width: 36px; height: 36px; }
}
</style>
