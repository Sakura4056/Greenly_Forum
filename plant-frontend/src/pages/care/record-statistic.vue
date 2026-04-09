<template>
  <div class="app-container">
    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="filterForm">
        <el-form-item label="时间范围">
          <el-radio-group v-model="filterForm.range" @change="fetchData">
            <el-radio-button value="7d">7 天</el-radio-button>
            <el-radio-button value="30d">30 天</el-radio-button>
            <el-radio-button value="90d">90 天</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="植物筛选">
          <el-select v-model="filterForm.plantId" placeholder="全部植物" clearable @change="fetchData">
            <el-option
              v-for="plant in myPlants"
              :key="plant.id"
              :label="plant.name"
              :value="plant.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #409EFF">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalRecords || 0 }}</div>
              <div class="stat-label">总记录数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #F56C6C">
              <el-icon><Trophy /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.streakDays || 0 }}</div>
              <div class="stat-label">连续天数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #67C23A">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ Object.keys(stats.byType || {}).length }}</div>
              <div class="stat-label">养护类型</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <!-- 饼图：养护类型分布 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>养护类型分布</span>
          </template>
          <div ref="pieChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>

      <!-- 柱状图：按植物分布 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>植物养护次数 TOP</span>
          </template>
          <div ref="barChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 每日趋势折线图 -->
    <el-card class="trend-card">
      <template #header>
        <span>每日养护趋势</span>
      </template>
      <div ref="lineChartRef" style="width: 100%; height: 300px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { Document, TrendCharts, DataLine, Trophy } from '@element-plus/icons-vue'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const filterForm = reactive({
  range: '30d',
  plantId: null
})

const stats = ref({
  totalRecords: 0,
  streakDays: 0,
  byType: {},
  byPlant: [],
  dailyTrend: []
})

const myPlants = ref([])
const pieChartRef = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let barChart = null
let lineChart = null

// 获取我的植物列表（用于筛选）
const fetchMyPlants = async () => {
  try {
    const res = await request.get('/my-plant', {
      params: { current: 1, size: 100 }
    })
    myPlants.value = res.records || []
  } catch (error) {
    console.error('获取植物列表失败:', error)
  }
}

// 获取统计数据
const fetchData = async () => {
  try {
    const res = await request.get('/care/record/stats', {
      params: {
        plantId: filterForm.plantId,
        range: filterForm.range
      }
    })
    stats.value = res
    
    // 更新图表
    updatePieChart()
    updateBarChart()
    updateLineChart()
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 初始化图表
const initCharts = () => {
  if (pieChartRef.value) {
    pieChart = echarts.init(pieChartRef.value)
  }
  if (barChartRef.value) {
    barChart = echarts.init(barChartRef.value)
  }
  if (lineChartRef.value) {
    lineChart = echarts.init(lineChartRef.value)
  }
  
  window.addEventListener('resize', () => {
    pieChart?.resize()
    barChart?.resize()
    lineChart?.resize()
  })
}

// 更新饼图
const updatePieChart = () => {
  if (!pieChart) return
  
  const data = Object.entries(stats.value.byType || {}).map(([name, value]) => ({
    name,
    value
  }))
  
  const option = {
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', left: 'left' },
    series: [
      {
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  pieChart.setOption(option)
}

// 更新柱状图
const updateBarChart = () => {
  if (!barChart) return
  
  const data = (stats.value.byPlant || []).slice(0, 10) // TOP 10
  
  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      data: data.map(item => item.plantName),
      axisLabel: { interval: 0, rotate: 30 }
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '养护次数',
        type: 'bar',
        data: data.map(item => item.count),
        itemStyle: { color: '#409EFF' }
      }
    ]
  }
  
  barChart.setOption(option)
}

// 更新折线图
const updateLineChart = () => {
  if (!lineChart) return
  
  const option = {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: (stats.value.dailyTrend || []).map(item => item.date)
    },
    yAxis: { type: 'value' },
    series: [
      {
        name: '养护次数',
        type: 'line',
        smooth: true,
        data: (stats.value.dailyTrend || []).map(item => item.count),
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        itemStyle: { color: '#409EFF' }
      }
    ]
  }
  
  lineChart.setOption(option)
}

onMounted(() => {
  initCharts()
  fetchMyPlants()
  fetchData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.trend-card {
  margin-top: 20px;
}
</style>
