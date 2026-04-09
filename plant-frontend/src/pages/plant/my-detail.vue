<template>
  <div class="my-plant-detail-container" v-loading="loading">
    <!-- 我的植物详情页 - 查看用户已拥有的植物 -->
    <div class="header-banner">
      <div 
        class="banner-bg" 
        :style="{ backgroundImage: `url(${getImageUrl(detail.plant?.coverUrl) || defaultCover})` }"
      ></div>
      <div class="banner-content">
        <el-button class="back-btn" circle @click="goBack">
          <el-icon><Back /></el-icon>
        </el-button>
        <div class="info-block">
          <el-avatar 
            :size="120" 
            :src="getImageUrl(detail.plant?.coverUrl) || defaultCover" 
            class="main-avatar"
          />
          <div class="text-block">
            <div class="title-row">
              <h1 class="plant-title">{{ detail.plant?.nickname }}</h1>
              <span class="status-badge" :class="detail.plant?.status?.toLowerCase()">
                {{ getStatusText(detail.plant?.status) }}
              </span>
            </div>
            <div class="meta-row">
              <span class="meta-item"><el-icon><Location /></el-icon> {{ detail.plant?.location || '未定位置' }}</span>
              <span class="meta-item"><el-icon><CollectionTag /></el-icon> {{ detail.plant?.source || '未知来源' }}</span>
              <span class="meta-item"><el-icon><Calendar /></el-icon> {{ detail.plant?.acquiredDate || '未知' }} 入住</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Content Tabs -->
    <div class="content-wrapper">
      <el-card shadow="never" class="main-card">
        <el-tabs v-model="activeTab" class="detail-tabs">
          <!-- Tab 1: 养护计划 -->
          <el-tab-pane name="schedule" :label="`养护计划 (${detail.totalSchedules || 0})`">
            <div class="pane-content">
              <div class="pane-header">
                <h3>近期计划</h3>
                <el-button link type="primary" @click="goAll('schedule')">查看全部 ></el-button>
              </div>
              <el-empty v-if="!detail.recentSchedules?.length" description="暂无计划" :image-size="100" />
              <el-timeline v-else class="schedule-timeline">
                <el-timeline-item
                  v-for="(item, idx) in detail.recentSchedules"
                  :key="idx"
                  :timestamp="formatDate(item.dueTime)"
                  placement="top"
                  :type="item.status === 0 ? 'primary' : (item.status === 1 ? 'success' : 'danger')"
                >
                  <el-card shadow="hover" class="timeline-card">
                    <h4>{{ item.taskName }}</h4>
                    <p class="timeline-meta" v-if="item.recurrenceType !== 'NONE'">
                      循环：每 {{ item.recurrenceInterval }} {{ getRecurrenceTypeName(item.recurrenceType) }}
                    </p>
                  </el-card>
                </el-timeline-item>
              </el-timeline>
            </div>
          </el-tab-pane>

          <!-- Tab 2: 养护记录 -->
          <el-tab-pane name="record" :label="`养护记录 (${detail.totalRecords || 0})`">
            <div class="pane-content">
              <div class="pane-header">
                <h3>近期记录</h3>
                <el-button link type="primary" @click="goAll('record')">查看全部 ></el-button>
              </div>
              <el-empty v-if="!detail.recentRecords?.length" description="暂无记录" :image-size="100" />
              <div v-else class="record-list">
                <el-card shadow="never" v-for="(item, idx) in detail.recentRecords" :key="idx" class="record-card">
                  <div class="record-header">
                    <span class="record-time">{{ formatDate(item.recordTime) }}</span>
                  </div>
                  <div class="record-body">
                    <div class="ops-list">
                      <el-tag size="small" v-for="(val, op) in parseOps(item.operations)" :key="op" type="success" effect="light">
                        {{ getOpName(op) }}: {{ val }}
                      </el-tag>
                    </div>
                    <div class="record-remark" v-if="item.remarks">{{ item.remarks }}</div>
                  </div>
                </el-card>
              </div>
            </div>
          </el-tab-pane>

          <!-- Tab 3: 成长相册 -->
          <el-tab-pane name="photo" :label="`成长相册 (${detail.totalPhotos || 0})`">
            <div class="pane-content">
              <div class="pane-header">
                <h3>精选照片</h3>
                <el-button link type="primary" @click="goAll('photo')">查看全部 ></el-button>
              </div>
              <el-empty v-if="!detail.recentPhotos?.length" description="还没拍过照哦" :image-size="100" />
              <div v-else class="photo-grid">
                <el-image 
                  v-for="(photo, idx) in detail.recentPhotos" 
                  :key="idx"
                  :src="getImageUrl(photo.url)" 
                  class="grid-img"
                  fit="cover"
                  :preview-src-list="detail.recentPhotos.map(p => getImageUrl(p.url))"
                  :initial-index="idx"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </el-card>
      
      <!-- Statistics Card -->
      <div class="side-panel">
        <el-card shadow="hover" class="note-card">
          <template #header>
            <div class="card-header">
              <span><el-icon><Document /></el-icon> 备注信息</span>
              <el-button link type="primary" @click="editPlant"><el-icon><Edit /></el-icon></el-button>
            </div>
          </template>
          <div class="note-text">
            {{ detail.plant?.notes || '主人什么都没写~' }}
          </div>
        </el-card>
      </div>
    </div>

    <!-- Floating Action Buttons -->
    <div class="fab-container">
      <el-dropdown trigger="click" placement="top" @command="handleFabAction">
        <el-button type="primary" circle class="fab-btn" size="large">
          <el-icon size="24"><Plus /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu class="fab-menu">
            <el-dropdown-item command="schedule"><el-icon><Calendar /></el-icon> 添加计划</el-dropdown-item>
            <el-dropdown-item command="record"><el-icon><DocumentAdd /></el-icon> 记录养护</el-dropdown-item>
            <el-dropdown-item command="photo"><el-icon><Camera /></el-icon> 上传照片</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Back, Location, Calendar, CollectionTag,
  Document, Edit, Plus, Camera, DocumentAdd
} from '@element-plus/icons-vue'
import { getMyPlantDetail } from '@/api/my-plant'

const route = useRoute()
const router = useRouter()
const activeTab = ref('schedule')
const loading = ref(false)
const detail = ref({
  plant: null,
  recentSchedules: [],
  recentRecords: [],
  recentPhotos: [],
  totalSchedules: 0,
  totalRecords: 0,
  totalPhotos: 0
})
const defaultCover = 'https://images.unsplash.com/photo-1463320726281-696a485928c7?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'

// 处理图片 URL：将 /uploads/xxx.jpg 转换为完整的后端 URL
const getImageUrl = (url) => {
  if (!url) return ''
  // 如果已经是完整 URL，直接返回
  if (url.startsWith('http')) return url
  // 如果已经是正确的 API 路径，转换为完整 URL
  if (url.startsWith('/api/photo/view/')) {
    return url
  }
  if (url.startsWith('/uploads/')) {
    const filename = url.replace('/uploads/', '')
    return `/api/photo/view/${filename}`
  }
  return url
}

onMounted(() => {
  const id = route.params.id
  if (id) {
    loadDetail(id)
  }
})

const loadDetail = async (id) => {
  loading.value = true
  try {
    const res = await getMyPlantDetail(id)
    console.log('API 响应:', res)
    
    // 拦截器已经返回 res.data，所以直接访问 res
    if (res) {
      detail.value = res
      console.log('加载植物详情成功:', detail.value)
      console.log('养护计划数量:', detail.value.totalSchedules)
      console.log('近期计划:', detail.value.recentSchedules)
      console.log('养护记录数量:', detail.value.totalRecords)
      console.log('近期记录:', detail.value.recentRecords)
    } else {
      console.error('响应数据为空')
    }
  } catch (e) {
    console.error('加载详情失败:', e)
  } finally {
    loading.value = false
  }
}

const getStatusText = (status) => {
  const map = {
    HEALTHY: '健康',
    SICK: '生病',
    DEAD: '阵亡',
    GIFTED: '送人'
  }
  return map[status] || status
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  return dateString.substring(0, 16).replace('T', ' ')
}

const getRecurrenceTypeName = (type) => {
  const map = { DAY: '天', MONTH: '月', YEAR: '年' }
  return map[type] || ''
}

const parseOps = (jsonStr) => {
  if (!jsonStr) return {}
  try {
    return JSON.parse(jsonStr)
  } catch (e) {
    return {}
  }
}

const getOpName = (key) => {
  const map = { water: '浇水', fertilizer: '施肥', pruning: '修剪', pestControl: '防虫' }
  return map[key] || key
}

const handleFabAction = (command) => {
  const plantId = detail.value.plant?.id
  const plantName = detail.value.plant?.nickname
  if (command === 'schedule') {
    router.push({ path: '/care/schedule-add', query: { addPlantId: plantId, source: 'LOCAL', name: plantName } })
  } else if (command === 'record') {
    router.push({ path: '/care/record-add', query: { addPlantId: plantId, source: 'LOCAL', name: plantName } })
  } else if (command === 'photo') {
    router.push({ path: '/photo', query: { addPlantId: plantId, source: 'LOCAL' } })
  }
}

const goAll = (type) => {
  const plantId = detail.value.plant?.id
  const keyword = detail.value.plant?.nickname
  if (type === 'schedule') {
    router.push({ path: '/care/schedule-list', query: { plantId, keyword } })
  } else if (type === 'record') {
    router.push({ path: '/care/record-statistic', query: { plantId, keyword } })
  } else if (type === 'photo') {
    router.push({ path: '/photo', query: { plantId, keyword } })
  }
}

const goBack = () => {
  router.back()
}

const editPlant = () => {
  if (!detail.value.plant || !detail.value.plant.id) {
    console.error('Plant data is not loaded yet')
    return
  }
  router.push(`/plant/my-add?id=${detail.value.plant.id}`)
}
</script>

<style scoped>
.my-plant-detail-container {
  min-height: calc(100vh - 60px);
  background-color: var(--el-bg-color-page);
  position: relative;
}

.header-banner {
  position: relative;
  height: 320px;
  overflow: hidden;
  display: flex;
  align-items: flex-end;
}

.banner-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-size: cover;
  background-position: center;
  filter: blur(20px) brightness(0.7);
  transform: scale(1.1);
  z-index: 1;
}

.banner-content {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 40px 40px;
}

.back-btn {
  position: absolute;
  top: 30px;
  left: 40px;
  background: rgba(255,255,255,0.2) !important;
  border: 1px solid rgba(255,255,255,0.4) !important;
  color: white !important;
  backdrop-filter: blur(10px);
}
.back-btn:hover {
  background: rgba(255,255,255,0.4) !important;
}

.info-block {
  display: flex;
  align-items: flex-end;
  gap: 30px;
  margin-top: 40px;
}

.main-avatar {
  border: 4px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 8px 32px rgba(0,0,0,0.2);
  background-color: white;
}

.text-block {
  padding-bottom: 10px;
  color: white;
  text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.title-row {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.plant-title {
  margin: 0;
  font-size: 36px;
  font-weight: 800;
  letter-spacing: 1px;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.status-badge.healthy { background: rgba(103, 194, 58, 0.85); }
.status-badge.sick { background: rgba(230, 162, 60, 0.85); }
.status-badge.dead { background: rgba(245, 108, 108, 0.85); }
.status-badge.gifted { background: rgba(144, 147, 153, 0.85); }

.meta-row {
  display: flex;
  gap: 24px;
  font-size: 15px;
  opacity: 0.9;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Content Area */
.content-wrapper {
  max-width: 1200px;
  margin: -40px auto 40px;
  padding: 0 40px;
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
  position: relative;
  z-index: 10;
}

.main-card {
  border-radius: 16px;
  box-shadow: 0 12px 36px rgba(0,0,0,0.06);
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
}

.detail-tabs :deep(.el-tabs__header) {
  margin: 0 24px 24px;
}

.detail-tabs :deep(.el-tabs__item) {
  font-size: 16px;
  padding: 0 24px;
  height: 50px;
  line-height: 50px;
}

.pane-content {
  padding: 0 24px 24px;
}

.pane-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.pane-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

/* Timeline */
.timeline-card {
  border-radius: 12px;
  margin-bottom: 8px;
  border: 1px solid var(--el-border-color-lighter);
}
.timeline-card h4 {
  margin: 0 0 8px;
  font-size: 16px;
  color: var(--el-text-color-primary);
}
.timeline-meta {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* Records */
.record-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.record-card {
  border-radius: 12px;
  background-color: var(--el-fill-color-light);
}
.record-header {
  margin-bottom: 12px;
  font-weight: 500;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}
.ops-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.record-remark {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

/* Photos */
.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 16px;
}
.grid-img {
  width: 100%;
  aspect-ratio: 1;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  transition: transform 0.3s ease;
  cursor: pointer;
}
.grid-img:hover {
  transform: scale(1.05);
}

/* Sidebar Notes */
.note-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 8px 24px rgba(0,0,0,0.05);
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: bold;
}
.note-text {
  font-size: 15px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  min-height: 100px;
}

/* FAB button */
.fab-container {
  position: fixed;
  right: 50px;
  bottom: 50px;
  z-index: 100;
}
.fab-btn {
  width: 64px;
  height: 64px;
  box-shadow: 0 6px 20px rgba(var(--el-color-primary-rgb), 0.5);
  transition: transform 0.3s, box-shadow 0.3s;
}
.fab-btn:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 10px 24px rgba(var(--el-color-primary-rgb), 0.6);
}
.fab-menu :deep(.el-dropdown-menu__item) {
  padding: 10px 20px;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
