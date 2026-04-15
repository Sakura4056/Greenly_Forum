<template>
  <main class="app-container responsive-container" role="main">
    <!-- 系统公告横幅 -->
    <section v-if="latestAnnouncement" class="announcement-banner mb-4" :class="{ 'unread': !isAnnouncementRead }" aria-label="系统公告">
      <el-card shadow="hover" class="announcement-card">
        <div class="announcement-content">
          <div class="announcement-icon">
            <el-icon :size="24"><Bell /></el-icon>
          </div>
          <div class="announcement-text">
            <h3 class="announcement-title">{{ latestAnnouncement.title }}</h3>
            <p class="announcement-preview">{{ truncateContent(latestAnnouncement.content, 100) }}</p>
            <time class="announcement-time" :datetime="latestAnnouncement.publishTime">
              发布时间: {{ formatAnnouncementTime(latestAnnouncement.publishTime) }}
            </time>
          </div>
          <el-button type="primary" size="small" @click="showAnnouncementDetail" round>
            查看详情
          </el-button>
          <el-button 
            v-if="!isAnnouncementRead" 
            type="success" 
            size="small" 
            @click="markAnnouncementAsRead" 
            round
          >
            标记已读
          </el-button>
        </div>
      </el-card>
    </section>

    <!-- 天气组件 -->
    <WeatherWidget
      :weather-data="weather"
      :status-text="weatherStatus"
      :amap-key="AMAP_KEY"
      @city-change="handleCityChange"
    />

    <!-- 统计卡片区域 -->
    <section class="stats-section mb-6" aria-label="数据统计">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="8">
          <StatCard
            :value="stats.plantCount || 0"
            label="我的植物"
            variant="success"
            to="/plant/my-list"
          >
            <template #icon><Pear /></template>
          </StatCard>
        </el-col>
        <el-col :xs="24" :sm="8">
          <StatCard
            :value="stats.pendingTaskCount || 0"
            label="待办任务"
            variant="warning"
            to="/care/schedule-list"
          >
            <template #icon><List /></template>
          </StatCard>
        </el-col>
        <el-col :xs="24" :sm="8">
          <StatCard
            :value="stats.todayReminderCount || 0"
            label="今日提醒"
            variant="danger"
            to="/reminder/unread"
          >
            <template #icon><Bell /></template>
          </StatCard>
        </el-col>
      </el-row>
    </section>

    <!-- 内容区域 -->
    <el-row :gutter="24">
      <!-- 快捷操作 -->
      <el-col :xs="24" :lg="16" class="mb-4">
        <el-card shadow="never" class="action-card card-hover">
          <template #header>
            <div class="card-header">
              <h3>快捷操作</h3>
              <el-tag type="info" size="small" effect="plain">常用功能</el-tag>
            </div>
          </template>
          <nav class="quick-actions-grid" aria-label="快捷操作">
            <button
              v-for="action in quickActions"
              :key="action.path"
              class="action-item"
              @click="$router.push(action.path)"
              :aria-label="action.label"
            >
              <div class="action-icon-circle" :class="action.iconClass">
                <el-icon color="#fff"><component :is="action.icon" /></el-icon>
              </div>
              <span>{{ action.label }}</span>
            </button>
          </nav>
        </el-card>
      </el-col>

      <!-- 最近任务 -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="task-list-card card-hover">
          <template #header>
            <div class="card-header">
              <h3>最近未完成</h3>
              <el-button link type="primary" @click="$router.push('/care/schedule-list')">
                全部
              </el-button>
            </div>
          </template>

          <div v-if="recentTasks.length > 0" class="task-list">
            <article
              v-for="task in recentTasks"
              :key="task.id"
              class="task-item"
              :aria-label="`任务: ${task.taskName}`"
            >
              <div class="task-status-line"></div>
              <div class="task-content">
                <div class="task-title">{{ task.taskName }}</div>
                <time class="task-time" :datetime="formatTimeISO(task.dueTime)">
                  {{ formatTime(task.dueTime) }}
                </time>
              </div>
              <el-tag size="small" type="warning" effect="light" role="status">待办</el-tag>
            </article>
          </div>
          <div v-else class="empty-state">
            <span class="empty-text">太棒了，没有未完成任务！</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 公告详情对话框 -->
    <el-dialog
      v-model="announcementDialogVisible"
      :title="latestAnnouncement?.title || '系统公告'"
      width="600px"
      class="announcement-dialog"
    >
      <div class="announcement-detail">
        <div class="detail-meta">
          <time :datetime="latestAnnouncement?.publishTime">
            发布时间: {{ formatAnnouncementTime(latestAnnouncement?.publishTime) }}
          </time>
        </div>
        <div class="detail-content" v-html="latestAnnouncement?.content"></div>
      </div>
      <template #footer>
        <el-button @click="announcementDialogVisible = false">关闭</el-button>
        <el-button 
          v-if="!isAnnouncementRead" 
          type="primary" 
          @click="markAnnouncementAsRead"
        >
          标记为已读
        </el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
/**
 * 仪表板页面
 * 展示用户概览数据、天气信息和快捷操作
 */
import { ref, onMounted, reactive, nextTick, watch, computed, onUnmounted } from 'vue'
import { getMyPlantList } from '@/api/my-plant'
import { getScheduleList } from '@/api/care'
import { getUnread, markRead as markReadAPI } from '@/api/reminder'
import {
  Pear, List, Bell, Plus, Upload, Search, DataLine, Location,
  Sunny, Cloudy, Pouring, PartlyCloudy, ChatDotRound
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import WeatherWidget from '@/components/business/WeatherWidget.vue'
import StatCard from '@/components/business/StatCard.vue'

const userStore = useUserStore()

// 系统公告相关
const latestAnnouncement = ref(null)
const isAnnouncementRead = ref(true)
const announcementDialogVisible = ref(false)

/**
 * 截断公告内容
 */
const truncateContent = (content, maxLength) => {
  if (!content) return ''
  return content.length > maxLength ? content.substring(0, maxLength) + '...' : content
}

/**
 * 格式化公告时间
 */
const formatAnnouncementTime = (timeStr) => {
  if (!timeStr) return ''
  // 如果时间是数组格式 [year, month, day, hour, minute]
  if (Array.isArray(timeStr)) {
    return `${timeStr[0]}-${String(timeStr[1]).padStart(2, '0')}-${String(timeStr[2]).padStart(2, '0')} ${String(timeStr[3]).padStart(2, '0')}:${String(timeStr[4]).padStart(2, '0')}`
  }
  // 字符串格式直接返回或格式化
  return timeStr.replace('T', ' ').substring(0, 16)
}

/**
 * 显示公告详情
 */
const showAnnouncementDetail = () => {
  announcementDialogVisible.value = true
}

/**
 * 标记公告为已读
 */
const markAnnouncementAsRead = async () => {
  if (!latestAnnouncement.value || !latestAnnouncement.value.id) return
  
  try {
    await markReadAPI(latestAnnouncement.value.id)
    isAnnouncementRead.value = true
    ElMessage.success('已标记为已读')
    announcementDialogVisible.value = false
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

/**
 * 加载最新公告
 */
const loadLatestAnnouncement = async () => {
  try {
    const res = await getUnread(userStore.userId)
    if (res && res.details && res.details.announcement && res.details.announcement.length > 0) {
      // 获取最新的公告（按 createTime 排序）
      const announcements = res.details.announcement
      announcements.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
      latestAnnouncement.value = announcements[0]
      isAnnouncementRead.value = announcements[0].isRead === 1
    }
  } catch (error) {
    console.error('加载公告失败:', error)
  }
}

// 统计数据
const stats = reactive({
  plantCount: 0,
  pendingTaskCount: 0,
  todayReminderCount: 0
})
const recentTasks = ref([])
const isLoading = ref(false)
const lastFetchTime = ref(0)
const CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存

// 天气数据
const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || ''
const weather = reactive({
  city: '',
  adcode: '',
  weather: '',
  temperature: '',
  humidity: '',
  reportTime: ''
})
const weatherStatus = ref('正在获取位置...')
const isEditingCity = ref(false)
const inputCity = ref('')
const cityInputRef = ref(null)
const weatherCache = ref(null)
const weatherCacheTime = ref(0)
const WEATHER_CACHE_DURATION = 15 * 60 * 1000 // 15分钟缓存

// 快捷操作配置
const quickActions = [
  {
    path: '/care/schedule-add',
    label: '新建计划',
    icon: Plus,
    iconClass: 'bg-primary-gradient'
  },
  {
    path: '/photo/upload',
    label: '上传照片',
    icon: Upload,
    iconClass: 'bg-success-gradient'
  },
  {
    path: '/plant/official',
    label: '查询百科',
    icon: Search,
    iconClass: 'bg-info-gradient'
  },
  {
    path: '/care/statistic',
    label: '查看统计',
    icon: DataLine,
    iconClass: 'bg-warning-gradient'
  },
  {
    path: '/diary/list',
    label: '写日记',
    icon: ChatDotRound,
    iconClass: 'bg-danger-gradient'
  },
  {
    path: '/plant/my-list',
    label: '我的植物',
    icon: Pear,
    iconClass: 'bg-teal-gradient'
  }
]

/**
 * 格式化时间显示
 * @param {Array} arr - 时间数组 [year, month, day, hour, minute]
 * @returns {string} 格式化后的时间字符串
 */
const formatTime = (arr) => {
  if (Array.isArray(arr)) {
    return `${arr[1]}-${arr[2]} ${arr[3]}:${String(arr[4]).padStart(2, '0')}`
  }
  return arr
}

/**
 * 格式化时间为 ISO 格式（用于 datetime 属性）
 * @param {Array} arr - 时间数组
 * @returns {string} ISO 格式时间
 */
const formatTimeISO = (arr) => {
  if (Array.isArray(arr)) {
    return `${arr[0]}-${String(arr[1]).padStart(2, '0')}-${String(arr[2]).padStart(2, '0')}T${String(arr[3]).padStart(2, '0')}:${String(arr[4]).padStart(2, '0')}`
  }
  return arr
}

/**
 * 简化时间格式
 * @param {string} timeStr - 时间字符串
 * @returns {string} 简化后的时间
 */
const formatTimeSimple = (timeStr) => {
  if (!timeStr) return ''
  return timeStr.split(' ')[1] || timeStr
}

/**
 * 获取仪表板数据
 */
const fetchDashboardData = async () => {
  const now = Date.now()
  if (now - lastFetchTime.value < CACHE_DURATION) {
    return
  }

  isLoading.value = true
  try {
    const [plantRes, scheduleRes, reminderRes] = await Promise.all([
      getMyPlantList({ pageNum: 1, pageSize: 1000 }),
      getScheduleList({ userId: userStore.userId, status: 0, pageSize: 4 }),
      getUnread(userStore.userId)
    ])

    if (plantRes && plantRes.records) {
      stats.plantCount = plantRes.records.length
    }

    if (scheduleRes && scheduleRes.records) {
      stats.pendingTaskCount = scheduleRes.total
      recentTasks.value = scheduleRes.records
    }

    if (reminderRes) {
      stats.todayReminderCount = reminderRes.totalUnread
    }

    lastFetchTime.value = now
  } catch (e) {
    ElMessage.error('获取数据失败，请稍后重试')
  } finally {
    isLoading.value = false
  }
}

/**
 * 通过浏览器地理定位获取城市代码
 * @returns {Promise<string|null>} 城市代码或 null
 */
const getLocationByBrowser = () => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('浏览器不支持地理定位'))
      return
    }

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        try {
          const { latitude, longitude } = position.coords
          // 使用高德地图逆地理编码 API 获取城市信息
          const res = await fetch(
            `https://restapi.amap.com/v3/geocode/regeo?location=${longitude},${latitude}&key=${AMAP_KEY}&extensions=all`
          )
          const data = await res.json()
          if (data.status === '1' && data.regeocode && data.regeocode.addressComponent) {
            const adcode = data.regeocode.addressComponent.adcode
            resolve(adcode)
          } else {
            reject(new Error('逆地理编码失败'))
          }
        } catch (error) {
          reject(error)
        }
      },
      (error) => {
        let message = '定位失败'
        switch (error.code) {
          case error.PERMISSION_DENIED:
            message = '用户拒绝定位'
            break
          case error.POSITION_UNAVAILABLE:
            message = '位置信息不可用'
            break
          case error.TIMEOUT:
            message = '定位超时'
            break
        }
        reject(new Error(message))
      },
      {
        enableHighAccuracy: false,
        timeout: 8000,
        maximumAge: 300000 // 5分钟缓存
      }
    )
  })
}

/**
 * 加载天气数据
 * @param {string} cityCode - 城市代码
 */
const loadWeather = async (cityCode = null) => {
  if (!AMAP_KEY) {
    weatherStatus.value = '请配置 Key'
    ElMessage.warning('请在 .env 文件中配置高德地图 Key')
    return
  }

  const now = Date.now()
  if (!cityCode && weatherCache.value && now - weatherCacheTime.value < WEATHER_CACHE_DURATION) {
    Object.assign(weather, weatherCache.value)
    weatherStatus.value = ''
    return
  }

  // 优先使用浏览器地理定位
  if (!cityCode) {
    try {
      weatherStatus.value = '正在获取位置...'
      cityCode = await getLocationByBrowser()
    } catch (e) {
      console.warn('浏览器定位失败:', e.message)
      // 降级为 IP 定位
      try {
        const ipRes = await fetch(`https://restapi.amap.com/v3/ip?key=${AMAP_KEY}`)
        const ipData = await ipRes.json()
        if (ipData.status === '1' && ipData.adcode && ipData.adcode.length > 0) {
          cityCode = ipData.adcode
          weather.city = ipData.city || '未知城市'
          weatherStatus.value = 'IP 定位（可能不精确）'
        } else {
          // IP定位返回空数据，降级为默认城市
          cityCode = '310100'
          weather.city = '上海'
          weatherStatus.value = '自动定位失败，已使用默认城市'
        }
      } catch (ipError) {
        console.error('IP 定位也失败:', ipError)
        // 降级为默认城市（上海）
        cityCode = '310100'
        weather.city = '上海'
        weatherStatus.value = '自动定位失败，已使用默认城市'
      }
    }
  }

  // 获取天气
  if (cityCode) {
    try {
      const wRes = await fetch(`https://restapi.amap.com/v3/weather/weatherInfo?key=${AMAP_KEY}&city=${cityCode}`)
      const wData = await wRes.json()
      if (wData.status === '1' && wData.lives && wData.lives.length > 0) {
        const live = wData.lives[0]
        const weatherData = {
          city: live.city,
          adcode: cityCode,
          weather: live.weather,
          temperature: live.temperature,
          humidity: live.humidity,
          reportTime: live.reporttime
        }
        Object.assign(weather, weatherData)
        weatherCache.value = weatherData
        weatherCacheTime.value = now
        weatherStatus.value = ''
      } else {
        weatherStatus.value = '无天气数据'
      }
    } catch (e) {
      console.error('获取天气失败:', e)
      weatherStatus.value = '获取天气失败'
      ElMessage.error('天气数据获取失败，请稍后重试')
    }
  }
}

/**
 * 处理城市切换
 * @param {string} adcode - 城市代码
 */
const handleCityChange = async (adcode) => {
  await loadWeather(adcode)
}

/**
 * 处理城市提交
 */
const handleCitySubmit = async () => {
  if (!inputCity.value) {
    isEditingCity.value = false
    return
  }

  try {
    const geoRes = await fetch(`https://restapi.amap.com/v3/geocode/geo?address=${inputCity.value}&key=${AMAP_KEY}`)
    const geoData = await geoRes.json()
    if (geoData.status === '1' && geoData.geocodes && geoData.geocodes.length > 0) {
      const adcode = geoData.geocodes[0].adcode
      await loadWeather(adcode)
      isEditingCity.value = false
    } else {
      ElMessage.warning('找不到该城市')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  }
}

// 自动刷新
let refreshInterval = null

const startAutoRefresh = () => {
  refreshInterval = setInterval(() => {
    fetchDashboardData()
    loadWeather()
  }, 5 * 60 * 1000)
}

const stopAutoRefresh = () => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
    refreshInterval = null
  }
}

watch(isEditingCity, (val) => {
  if (val) {
    inputCity.value = weather.city
    nextTick(() => {
      cityInputRef.value?.focus()
    })
  }
})

onMounted(() => {
  fetchDashboardData()
  loadWeather()
  loadLatestAnnouncement() // 加载最新公告
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped lang="scss">
@import '@/styles/responsive.scss';

.mb-6 { margin-bottom: var(--spacing-xl); }
.mb-4 { margin-bottom: var(--spacing-md); }

/* 系统公告横幅 */
.announcement-banner {
  .announcement-card {
    border-left: 4px solid var(--color-primary);
    transition: all var(--transition-normal);
    
    &.unread {
      border-left-color: var(--color-danger);
      animation: pulse 2s ease-in-out infinite;
    }
  }
  
  .announcement-content {
    display: flex;
    align-items: center;
    gap: var(--spacing-lg);
    
    .announcement-icon {
      flex-shrink: 0;
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-success) 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      
      .unread & {
        background: linear-gradient(135deg, var(--color-danger) 0%, #e74c3c 100%);
      }
    }
    
    .announcement-text {
      flex: 1;
      min-width: 0;
      
      .announcement-title {
        margin: 0 0 4px 0;
        font-size: 16px;
        font-weight: 600;
        color: var(--color-text-main);
      }
      
      .announcement-preview {
        margin: 0 0 4px 0;
        font-size: 14px;
        color: var(--color-text-secondary);
        line-height: 1.5;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .announcement-time {
        font-size: 12px;
        color: var(--color-text-tertiary);
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(231, 76, 60, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(231, 76, 60, 0);
  }
}

/* 公告详情对话框 */
.announcement-dialog {
  :deep(.el-dialog__body) {
    padding: 20px;
  }
  
  .announcement-detail {
    .detail-meta {
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid var(--color-border);
      
      time {
        font-size: 14px;
        color: var(--color-text-secondary);
      }
    }
    
    .detail-content {
      font-size: 15px;
      line-height: 1.8;
      color: var(--color-text-main);
      white-space: pre-wrap;
      word-wrap: break-word;
    }
  }
}

/* 统计卡片区域 */
.stats-section {
  .stat-card {
    background: var(--color-surface);
    border-radius: 16px;
    padding: var(--spacing-xl);
    display: flex;
    align-items: center;
    gap: var(--spacing-lg);
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
    transition: all 0.3s ease;
    cursor: default;
    border: 1px solid rgba(0, 0, 0, 0.04);
    position: relative;
    overflow: hidden;

    &::after {
      content: '';
      position: absolute;
      top: 0;
      right: 0;
      width: 80px;
      height: 80px;
      border-radius: 0 16px 0 80px;
      opacity: 0.06;
      transition: opacity 0.3s;
    }

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
      &::after { opacity: 0.1; }
    }

    &:nth-child(1)::after { background: #52c41a; }
    &:nth-child(2)::after { background: #faad14; }
    &:nth-child(3)::after { background: #ff4d4f; }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      flex-shrink: 0;

      &.bg-success-light { background: linear-gradient(135deg, rgba(82, 196, 26, 0.12), rgba(82, 196, 26, 0.04)); color: #52c41a; }
      &.bg-warning-light { background: linear-gradient(135deg, rgba(250, 173, 20, 0.12), rgba(250, 173, 20, 0.04)); color: #faad14; }
      &.bg-danger-light { background: linear-gradient(135deg, rgba(255, 77, 79, 0.12), rgba(255, 77, 79, 0.04)); color: #ff4d4f; }
    }

    .stat-info {
      display: flex;
      flex-direction: column;

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: var(--color-text-main);
        line-height: 1.2;
        font-variant-numeric: tabular-nums;
      }

      .stat-label {
        font-size: 13px;
        color: var(--color-text-secondary);
        margin-top: 4px;
      }
    }
  }
}

/* 快捷操作 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: var(--color-text-main);
  }
}

.quick-actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: var(--spacing-lg);
  padding: var(--spacing-sm) 0;

  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: var(--spacing-sm);
    cursor: pointer;
    transition: all var(--transition-normal);
    background: none;
    border: none;
    padding: 0;
    font: inherit;
    color: inherit;
    width: 100%;

    &:hover {
      transform: translateY(-2px);

      .action-icon-circle {
        box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
        transform: scale(1.05);
      }

      span {
        color: var(--color-primary);
      }
    }

    &:focus-visible {
      outline: 2px solid var(--color-primary);
      outline-offset: 2px;
      border-radius: var(--border-radius-small);
    }

    .action-icon-circle {
      width: 56px;
      height: 56px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      transition: all var(--transition-normal);

      &.bg-primary-gradient { background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%); }
      &.bg-success-gradient { background: linear-gradient(135deg, #3498db 0%, #2980b9 100%); }
      &.bg-info-gradient { background: linear-gradient(135deg, #9b59b6 0%, #8e44ad 100%); }
      &.bg-warning-gradient { background: linear-gradient(135deg, #f1c40f 0%, #f39c12 100%); }
      &.bg-danger-gradient { background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%); }
      &.bg-teal-gradient { background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%); }
    }

    span {
      font-size: 14px;
      color: var(--color-text-main);
      font-weight: 500;
      transition: color var(--transition-normal);
    }
  }
}

/* 任务列表 */
.task-list {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.task-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-sm);
  background-color: var(--color-background);
  border-radius: var(--border-radius-small);
  position: relative;
  overflow: hidden;
  transition: all var(--transition-normal);

  &:hover {
    background-color: #ebf5f0;
  }

  .task-status-line {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background-color: var(--color-warning);
  }

  .task-content {
    margin-left: 10px;

    .task-title {
      font-size: 14px;
      font-weight: 500;
      color: var(--color-text-main);
      margin-bottom: 4px;
    }

    .task-time {
      font-size: 12px;
      color: var(--color-text-secondary);
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--spacing-xxl) 0;

  .empty-text {
    color: var(--color-text-secondary);
    font-size: 14px;
  }
}

@include respond-to(mobile) {
  .quick-actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
