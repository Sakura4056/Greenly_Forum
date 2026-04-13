<template>
  <div class="calendar-container">
    <div class="page-header">
      <h2 class="page-title">养护日历</h2>
      <div class="date-selector">
        <el-select v-model="selectedYear" @change="loadCalendar" class="year-select">
          <el-option v-for="y in years" :key="y" :label="y + '年'" :value="y" />
        </el-select>
        <el-select v-model="selectedMonth" @change="loadCalendar" class="month-select">
          <el-option v-for="m in 12" :key="m" :label="m + '月'" :value="m" />
        </el-select>
      </div>
    </div>

    <el-card shadow="never" class="calendar-card">
      <el-calendar v-model="currentDate">
        <template #dateCell="{ date, data }">
          <div class="calendar-date-cell" @click="showDateTasks(date)">
            <span class="date-text">{{ data.day }}</span>
            <span v-if="getTaskCount(date) > 0" class="task-badge">{{ getTaskCount(date) }}</span>
          </div>
        </template>
      </el-calendar>
    </el-card>

    <!-- 任务详情弹窗 -->
    <el-dialog
      v-model="showTaskDialog"
      :title="`${selectedDateStr} 的养护计划`"
      width="400px"
    >
      <div v-if="selectedDateTasks.length === 0" class="empty-tasks">
        <el-empty description="当日没有养护计划" :image-size="80" />
      </div>
      <div v-else class="task-list">
        <el-card
          v-for="(task, index) in selectedDateTasks"
          :key="index"
          shadow="hover"
          class="task-item"
        >
          <div class="task-header">
            <h4 class="task-name">{{ task.taskName }}</h4>
            <el-tag
              :type="task.status === 0 ? 'primary' : (task.status === 1 ? 'success' : 'danger')"
              size="small"
            >
              {{ getStatusText(task.status) }}
            </el-tag>
          </div>
          <div class="task-meta">
            <span class="due-time">{{ formatTime(task.dueTime) }}</span>
            <span v-if="task.recurrenceType !== 'NONE'" class="recurrence">
              循环：每 {{ task.recurrenceInterval }} {{ getRecurrenceTypeName(task.recurrenceType) }}
            </span>
          </div>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getCalendar } from '@/api/care'

const currentDate = ref(new Date())
const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth() + 1)
const calendarData = ref({})
const showTaskDialog = ref(false)
const selectedDateStr = ref('')
const selectedDateTasks = ref([])

const years = computed(() => {
  const currentYear = new Date().getFullYear()
  return Array.from({ length: 5 }, (_, i) => currentYear - 2 + i)
})

onMounted(() => {
  loadCalendar()
})

const loadCalendar = async () => {
  try {
    const res = await getCalendar(selectedYear.value, selectedMonth.value)
    if (res.data) {
      calendarData.value = res.data
    }
  } catch (err) {
    console.error('加载日历数据失败', err)
  }
}

const getTaskCount = (date) => {
  const dateStr = formatDate(date)
  return calendarData.value[dateStr]?.length || 0
}

const showDateTasks = (date) => {
  const dateStr = formatDate(date)
  selectedDateStr.value = dateStr
  selectedDateTasks.value = calendarData.value[dateStr] || []
  showTaskDialog.value = true
}

const formatDate = (date) => {
  return date.toISOString().split('T')[0]
}

const formatTime = (dateString) => {
  if (!dateString) return ''
  return dateString.substring(11, 16)
}

const getStatusText = (status) => {
  const map = { 0: '待完成', 1: '已完成', 2: '已逾期' }
  return map[status] || status
}

const getRecurrenceTypeName = (type) => {
  const map = { DAY: '天', MONTH: '月', YEAR: '年' }
  return map[type] || ''
}
</script>

<style scoped>
.calendar-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--el-text-color-primary);
  margin: 0;
  letter-spacing: -0.5px;
}

.date-selector {
  display: flex;
  gap: 12px;
}

.year-select, .month-select {
  width: 100px;
}

.calendar-card {
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.06);
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
}

.calendar-date-cell {
  position: relative;
  width: 100%;
  height: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.calendar-date-cell:hover {
  background-color: var(--el-fill-color-light);
}

.date-text {
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.task-badge {
  position: absolute;
  top: 4px;
  right: 4px;
  background-color: var(--el-color-primary);
  color: white;
  font-size: 12px;
  font-weight: bold;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
}

.task-item {
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.task-name {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.task-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.empty-tasks {
  padding: 40px 0;
  text-align: center;
}
</style>