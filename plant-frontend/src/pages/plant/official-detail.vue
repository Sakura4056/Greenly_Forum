<template>
  <div class="official-plant-detail-container">
    <!-- 官方植物库详情页 - 查看植物百科信息，可添加到我的植物 -->
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <el-page-header @back="$router.back()">
            <template #content>
              <span class="page-title">{{ plant.name || '植物详情' }}</span>
            </template>
          </el-page-header>
        </div>
      </template>

      <div v-if="plant" class="detail-container">
        <!-- 顶部大图 -->
        <div class="plant-image">
          <el-image
            v-if="plant.imageUrl"
            :src="plant.imageUrl"
            :preview-src-list="[plant.imageUrl]"
            fit="cover"
            class="plant-image-main"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
                <span>暂无图片</span>
              </div>
            </template>
          </el-image>
          <div v-else class="plant-image-placeholder">
            <el-icon size="60"><PictureFilled /></el-icon>
            <span>暂无图片</span>
          </div>
        </div>

        <!-- 基本信息卡片 -->
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-title">
              <el-icon><Collection /></el-icon>
              <span>基本信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="植物名称">{{ plant.name }}</el-descriptions-item>
            <el-descriptions-item label="科属">
              {{ plant.genus }} {{ plant.species }}
            </el-descriptions-item>
            <el-descriptions-item label="养护难度">
              <el-tag :type="getDifficultyType(plant.difficulty)">
                {{ plant.difficulty || '未知' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="花期">
              {{ plant.bloomSeason || '未知' }}
            </el-descriptions-item>
            <el-descriptions-item label="光照需求">
              <el-tag effect="plain">{{ plant.lightReq || '未知' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="浇水频率">
              <el-tag effect="plain">{{ plant.waterReq || '未知' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="适宜温度">
              {{ plant.tempRange || '未知' }}
            </el-descriptions-item>
            <el-descriptions-item label="土壤要求">
              {{ plant.soilReq || '未知' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 描述 -->
        <el-card v-if="plant.description" class="info-card" shadow="hover">
          <template #header>
            <div class="card-title">
              <el-icon><Document /></el-icon>
              <span>植物描述</span>
            </div>
          </template>
          <p class="description-text">{{ plant.description }}</p>
        </el-card>

        <!-- 养护指南折叠面板 -->
        <el-card class="info-card" shadow="hover">
          <template #header>
            <div class="card-title">
              <el-icon><Guide /></el-icon>
              <span>养护指南</span>
            </div>
          </template>
          <el-collapse accordion>
            <el-collapse-item title="💡 光照需求" name="light">
              <div class="collapse-content">
                <el-icon><Sunny /></el-icon>
                <span>{{ plant.lightReq || '暂无信息' }}</span>
              </div>
            </el-collapse-item>
            <el-collapse-item title="💧 浇水频率" name="water">
              <div class="collapse-content">
                <el-icon><Umbrella /></el-icon>
                <span>{{ plant.waterReq || '暂无信息' }}</span>
              </div>
            </el-collapse-item>
            <el-collapse-item title="🌡️ 适宜温度" name="temp">
              <div class="collapse-content">
                <el-icon><Odometer /></el-icon>
                <span>{{ plant.tempRange || '暂无信息' }}</span>
              </div>
            </el-collapse-item>
            <el-collapse-item title="🌱 土壤要求" name="soil">
              <div class="collapse-content">
                <el-icon><Grid /></el-icon>
                <span>{{ plant.soilReq || '暂无信息' }}</span>
              </div>
            </el-collapse-item>
            <el-collapse-item title="🐛 常见病虫害" name="diseases">
              <div class="collapse-content">
                <el-icon><Warning /></el-icon>
                <span>{{ plant.commonDiseases || '暂无信息' }}</span>
              </div>
            </el-collapse-item>
            <el-collapse-item title="📝 养护小贴士" name="tips">
              <div class="collapse-content tips-content">
                {{ plant.careTips || '暂无信息' }}
              </div>
            </el-collapse-item>
          </el-collapse>
        </el-card>

        <!-- 底部操作栏 -->
        <div class="bottom-actions">
          <el-button type="primary" size="large" @click="handleAddToMyPlant">
            <el-icon><Plus /></el-icon>
            添加到我的植物
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Picture,
  PictureFilled,
  Collection,
  Document,
  Guide,
  Sunny,
  Umbrella,  // 使用雨伞图标代表浇水
  Odometer,
  Grid,
  Warning,
  Plus
} from '@element-plus/icons-vue'
import request from '@/api/request'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const plant = ref({})

const getDifficultyType = (difficulty) => {
  if (!difficulty) return 'info'
  const map = {
    '简单': 'success',
    '中等': 'warning',
    '困难': 'danger'
  }
  return map[difficulty] || 'info'
}

const fetchPlantDetail = async () => {
  loading.value = true
  try {
    console.log('请求 URL:', `/api/plant/official/${route.params.id}`)
    const res = await request.get(`/plant/official/${route.params.id}`)
    console.log('植物详情响应:', res)
    plant.value = res.data || res
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

onMounted(() => {
  fetchPlantDetail()
})
</script>

<style scoped>
.official-plant-detail-container {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
}

.detail-container {
  max-width: 900px;
  margin: 0 auto;
}

.plant-image {
  margin-bottom: 20px;
  text-align: center;
}

.plant-image-main {
  width: 100%;
  max-height: 400px;
  border-radius: 8px;
}

.plant-image-placeholder {
  width: 100%;
  height: 300px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
}

.image-error {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #909399;
}

.info-card {
  margin-bottom: 20px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: bold;
}

.description-text {
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.collapse-content {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
}

.tips-content {
  display: block;
  line-height: 1.8;
  color: #606266;
}

.bottom-actions {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style>
