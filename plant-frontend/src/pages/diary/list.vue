<template>
  <div class="app-container">
    <!-- Stats Bar -->
    <div class="stats-bar">
      <div class="stat-item">
        <span class="stat-num">{{ total }}</span>
        <span class="stat-label">篇日记</span>
      </div>
      <div v-for="(item, key) in moodStats" :key="key" class="stat-item stat-mood">
        <span class="stat-emoji">{{ getMoodEmoji(key) }}</span>
        <span class="stat-num">{{ item }}</span>
      </div>
    </div>

    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <h2>植物日记</h2>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>
            写日记
          </el-button>
        </div>
      </template>

      <!-- Filters -->
      <div class="filter-row">
        <el-input
          v-model="keyword"
          placeholder="搜索日记标题或内容..."
          clearable
          prefix-icon="Search"
          style="width: 260px"
          @clear="fetchDiaries"
          @keyup.enter="fetchDiaries"
        />
        <el-select v-model="filterForm.plantId" placeholder="植物" clearable style="width: 150px">
          <el-option
            v-for="plant in plants"
            :key="plant.id"
            :label="plant.nickname"
            :value="plant.id"
          />
        </el-select>
        <el-select v-model="filterForm.mood" placeholder="心情" clearable style="width: 120px">
          <el-option label="😊 开心" value="happy" />
          <el-option label="🤩 兴奋" value="excited" />
          <el-option label="😐 平静" value="neutral" />
          <el-option label="😟 担心" value="worried" />
          <el-option label="😢 难过" value="sad" />
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 260px"
          value-format="YYYY-MM-DD"
          @change="fetchDiaries"
        />
        <el-button @click="resetFilter">重置</el-button>
      </div>

      <!-- Diary Timeline -->
      <div v-loading="loading" class="diary-timeline">
        <el-empty v-if="!loading && diaries.length === 0" description="还没有日记，写一篇吧 ✍️">
          <el-button type="primary" @click="openCreateDialog">写日记</el-button>
        </el-empty>

        <div class="timeline">
          <div v-for="diary in diaries" :key="diary.id" class="timeline-item">
            <div class="timeline-dot" :class="'mood-' + diary.mood" />
            <div class="timeline-card" @click="viewDetail(diary)">
              <div class="card-top">
                <div class="card-date">
                  <span class="date-day">{{ formatDay(diary.diaryDate) }}</span>
                  <span class="date-ym">{{ formatYearMonth(diary.diaryDate) }}</span>
                </div>
                <div class="card-info">
                  <div class="card-title-row">
                    <h3>{{ diary.title || '无标题' }}</h3>
                    <div class="card-tags">
                      <span class="mood-badge" :class="'mood-' + diary.mood">
                        {{ getMoodEmoji(diary.mood) }}
                      </span>
                      <span v-if="diary.weather" class="weather-badge">
                        {{ getWeatherEmoji(diary.weather) }}
                      </span>
                    </div>
                  </div>
                  <div class="card-meta">
                    <span v-if="diary.plantNickname" class="meta-tag">🌱 {{ diary.plantNickname }}</span>
                    <span class="meta-time">{{ relativeTime(diary.createTime) }}</span>
                  </div>
                </div>
              </div>

              <!-- Photos -->
              <div v-if="diary.photoUrls?.length" class="card-photos" @click.stop>
                <el-image
                  v-for="(url, idx) in diary.photoUrls.slice(0, 4)"
                  :key="idx"
                  :src="url"
                  fit="cover"
                  class="card-photo"
                  :preview-src-list="diary.photoUrls"
                  :initial-index="idx"
                />
                <div v-if="diary.photoUrls.length > 4" class="photo-more" @click.stop="viewDetail(diary)">
                  +{{ diary.photoUrls.length - 4 }}
                </div>
              </div>

              <div class="card-content">
                <p>{{ truncateContent(diary.content, 120) }}</p>
              </div>

              <div class="card-actions" @click.stop>
                <el-button type="primary" link size="small" @click="viewDetail(diary)">
                  <el-icon><View /></el-icon> 查看
                </el-button>
                <el-button type="warning" link size="small" @click="editDiary(diary)">
                  <el-icon><Edit /></el-icon> 编辑
                </el-button>
                <el-button type="danger" link size="small" @click="confirmDelete(diary.id)">
                  <el-icon><Delete /></el-icon> 删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="onPageChange"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog
      v-model="showDetailDialog"
      :title="null"
      width="680px"
      class="diary-detail-dialog"
    >
      <template #header>
        <div class="detail-header">
          <h2>{{ detailDiary?.title || '日记详情' }}</h2>
          <div class="detail-header-tags">
            <span class="mood-badge-lg" :class="'mood-' + detailDiary?.mood">
              {{ getMoodEmoji(detailDiary?.mood) }} {{ getMoodText(detailDiary?.mood) }}
            </span>
            <span v-if="detailDiary?.weather" class="weather-badge-lg">
              {{ getWeatherEmoji(detailDiary.weather) }} {{ getWeatherText(detailDiary.weather) }}
            </span>
          </div>
        </div>
      </template>
      <div v-if="detailDiary" class="diary-detail">
        <div class="detail-meta">
          <span v-if="detailDiary.plantNickname" class="meta-item">🌱 {{ detailDiary.plantNickname }}</span>
          <span class="meta-item">📅 {{ detailDiary.diaryDate }}</span>
          <span class="meta-item">🕐 {{ detailDiary.createTime }}</span>
        </div>

        <!-- Photos -->
        <div v-if="detailDiary.photoUrls?.length" class="detail-photos">
          <el-image
            v-for="(url, idx) in detailDiary.photoUrls"
            :key="idx"
            :src="url"
            fit="cover"
            class="detail-photo"
            :preview-src-list="detailDiary.photoUrls"
            :initial-index="idx"
          />
        </div>

        <div class="detail-content">
          <p>{{ detailDiary.content }}</p>
        </div>
      </div>
    </el-dialog>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingDiary ? '编辑日记' : '✍️ 写日记'"
      width="680px"
      class="diary-form-dialog"
      @closed="resetForm"
    >
      <el-form :model="diaryForm" label-width="70px">
        <el-form-item label="标题">
          <el-input v-model="diaryForm.title" placeholder="给日记起个名字（可选）" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="植物">
          <el-select v-model="diaryForm.plantId" placeholder="选择植物（可选）" clearable style="width: 100%">
            <el-option
              v-for="plant in plants"
              :key="plant.id"
              :label="plant.nickname"
              :value="plant.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="diaryForm.content"
            type="textarea"
            :rows="6"
            placeholder="记录今天的植物养护心得..."
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="照片">
          <div class="upload-area">
            <el-upload
              v-model:file-list="diaryForm.fileList"
              :http-request="customUpload"
              list-type="picture-card"
              :on-success="onUploadSuccess"
              :on-remove="onUploadRemove"
              :on-preview="onPreview"
              accept="image/*"
              :limit="9"
              :on-exceed="() => ElMessage.warning('最多上传9张照片')"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
          </div>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="心情">
              <div class="mood-picker">
                <div
                  v-for="m in moodOptions"
                  :key="m.value"
                  class="mood-option"
                  :class="{ active: diaryForm.mood === m.value }"
                  @click="diaryForm.mood = m.value"
                >
                  <span class="mood-icon">{{ m.emoji }}</span>
                  <span class="mood-label">{{ m.label }}</span>
                </div>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="天气">
              <el-select v-model="diaryForm.weather" placeholder="选择天气" clearable style="width: 100%">
                <el-option label="☀️ 晴天" value="sunny" />
                <el-option label="⛅ 多云" value="cloudy" />
                <el-option label="🌧️ 雨天" value="rainy" />
                <el-option label="❄️ 雪天" value="snowy" />
                <el-option label="💨 有风" value="windy" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期">
              <el-date-picker
                v-model="diaryForm.diaryDate"
                type="date"
                placeholder="选择日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitDiary" :loading="submitting">
          {{ editingDiary ? '保存修改' : '发布日记' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Image Preview Dialog -->
    <el-dialog v-model="showPreview" width="800px" :show-close="true">
      <img :src="previewUrl" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, View, Edit, Delete } from '@element-plus/icons-vue'
import { queryDiaries, createDiary, updateDiary, deleteDiary, getMoodStatistics } from '@/api/diary'
import { getMyPlantList } from '@/api/my-plant'
import request from '@/api/request'

const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const showDetailDialog = ref(false)
const showPreview = ref(false)
const previewUrl = ref('')
const editingDiary = ref(null)
const detailDiary = ref(null)
const diaries = ref([])
const plants = ref([])
const total = ref(0)
const keyword = ref('')
const dateRange = ref(null)
const moodStats = ref({})

const pageNum = ref(1)
const pageSize = 10

const moodOptions = [
  { value: 'happy', label: '开心', emoji: '😊' },
  { value: 'excited', label: '兴奋', emoji: '🤩' },
  { value: 'neutral', label: '平静', emoji: '😐' },
  { value: 'worried', label: '担心', emoji: '😟' },
  { value: 'sad', label: '难过', emoji: '😢' }
]

const filterForm = reactive({
  plantId: null,
  mood: null
})

const diaryForm = reactive({
  id: null,
  plantId: null,
  title: '',
  content: '',
  mood: 'neutral',
  weather: '',
  diaryDate: new Date(),
  fileList: [],
  photoIds: []
})

// Upload
const customUpload = async (options) => {
  const { file, onSuccess, onError } = options
  const formData = new FormData()
  formData.append('file', file)
  formData.append('plantSource', 'LOCAL')
  formData.append('isPublic', '0')
  if (diaryForm.plantId) formData.append('plantId', diaryForm.plantId)
  try {
    const res = await request({ url: '/photo/upload', method: 'post', data: formData, headers: { 'Content-Type': 'multipart/form-data' } })
    onSuccess(res)
  } catch (err) { onError(err) }
}

const onUploadSuccess = (response) => {
  const data = response?.data || response
  if (data?.id) diaryForm.photoIds.push(data.id)
}

const onUploadRemove = (file) => {
  const data = file.response?.data || file.response
  if (data?.id) {
    const idx = diaryForm.photoIds.indexOf(data.id)
    if (idx > -1) diaryForm.photoIds.splice(idx, 1)
  }
}

const onPreview = (file) => {
  previewUrl.value = file.url || file.response?.data?.url || file.response?.url
  showPreview.value = true
}

// Data
const fetchPlants = async () => {
  try {
    const res = await getMyPlantList({ pageNum: 1, pageSize: 100 })
    plants.value = res.records || []
  } catch (e) { console.error(e) }
}

const fetchDiaries = async () => {
  loading.value = true
  try {
    const params = {
      plantId: filterForm.plantId,
      mood: filterForm.mood,
      startDate: dateRange.value?.[0] || null,
      endDate: dateRange.value?.[1] || null,
      pageNum: pageNum.value,
      pageSize
    }
    const res = await queryDiaries(params)
    let records = res.records || []

    // Client-side keyword filter
    if (keyword.value?.trim()) {
      const kw = keyword.value.trim().toLowerCase()
      records = records.filter(d =>
        (d.title || '').toLowerCase().includes(kw) ||
        (d.content || '').toLowerCase().includes(kw)
      )
    }

    diaries.value = records
    total.value = res.total || 0
  } catch (e) {
    ElMessage.error('获取日记列表失败')
  } finally {
    loading.value = false
  }
}

const fetchMoodStats = async () => {
  try {
    const res = await getMoodStatistics({})
    moodStats.value = res || {}
  } catch (e) { /* ignore */ }
}

const resetFilter = () => {
  filterForm.plantId = null
  filterForm.mood = null
  keyword.value = ''
  dateRange.value = null
  pageNum.value = 1
  fetchDiaries()
}

const onPageChange = (p) => {
  pageNum.value = p
  fetchDiaries()
}

// Actions
const viewDetail = (diary) => {
  detailDiary.value = diary
  showDetailDialog.value = true
}

const openCreateDialog = () => {
  editingDiary.value = null
  resetForm()
  showCreateDialog.value = true
}

const editDiary = (diary) => {
  editingDiary.value = diary
  Object.assign(diaryForm, {
    id: diary.id,
    plantId: diary.plantId,
    title: diary.title || '',
    content: diary.content,
    mood: diary.mood || 'neutral',
    weather: diary.weather || '',
    diaryDate: new Date(diary.diaryDate),
    photoIds: diary.photoIds ? [...diary.photoIds] : [],
    fileList: (diary.photoUrls || []).map((url, i) => ({ name: `photo_${i}`, url }))
  })
  showCreateDialog.value = true
}

const confirmDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇日记吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    await deleteDiary(id)
    ElMessage.success('删除成功')
    fetchDiaries()
    fetchMoodStats()
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('删除失败')
  }
}

const submitDiary = async () => {
  if (!diaryForm.content?.trim()) {
    ElMessage.warning('请填写日记内容')
    return
  }
  submitting.value = true
  try {
    const data = {
      plantId: diaryForm.plantId || null,
      title: diaryForm.title,
      content: diaryForm.content,
      mood: diaryForm.mood,
      weather: diaryForm.weather || null,
      photoIds: diaryForm.photoIds,
      diaryDate: diaryForm.diaryDate instanceof Date
        ? diaryForm.diaryDate.toISOString().split('T')[0]
        : diaryForm.diaryDate
    }
    if (editingDiary.value) {
      data.id = editingDiary.value.id
      await updateDiary(data)
      ElMessage.success('更新成功')
    } else {
      await createDiary(data)
      ElMessage.success('发布成功 🌿')
    }
    showCreateDialog.value = false
    resetForm()
    fetchDiaries()
    fetchMoodStats()
  } catch (error) {
    ElMessage.error(error.response?.data?.msg || error.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  editingDiary.value = null
  Object.assign(diaryForm, {
    id: null, plantId: null, title: '', content: '',
    mood: 'neutral', weather: '', diaryDate: new Date(),
    fileList: [], photoIds: []
  })
}

// Helpers
const getMoodEmoji = (mood) => ({ happy: '😊', excited: '🤩', neutral: '😐', worried: '😟', sad: '😢' }[mood] || '😐')
const getMoodText = (mood) => ({ happy: '开心', excited: '兴奋', neutral: '平静', worried: '担心', sad: '难过' }[mood] || '平静')
const getWeatherEmoji = (w) => ({ sunny: '☀️', cloudy: '⛅', rainy: '🌧️', snowy: '❄️', windy: '💨' }[w] || '')
const getWeatherText = (w) => ({ sunny: '晴天', cloudy: '多云', rainy: '雨天', snowy: '雪天', windy: '有风' }[w] || '')

const truncateContent = (content, max) => {
  if (!content) return ''
  return content.length > max ? content.slice(0, max) + '...' : content
}

const formatDay = (dateStr) => {
  if (!dateStr) return ''
  return new Date(dateStr).getDate().toString().padStart(2, '0')
}

const formatYearMonth = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}.${(d.getMonth() + 1).toString().padStart(2, '0')}`
}

const relativeTime = (timeStr) => {
  if (!timeStr) return ''
  const now = Date.now()
  const t = new Date(timeStr).getTime()
  const diff = now - t
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return '刚刚'
  if (mins < 60) return `${mins}分钟前`
  const hours = Math.floor(mins / 60)
  if (hours < 24) return `${hours}小时前`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}天前`
  const months = Math.floor(days / 30)
  if (months < 12) return `${months}个月前`
  return `${Math.floor(months / 12)}年前`
}

onMounted(() => {
  fetchPlants()
  fetchDiaries()
  fetchMoodStats()
})
</script>

<style scoped lang="scss">
/* Stats Bar */
.stats-bar {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.stat-item {
  background: var(--el-fill-color-lighter);
  border-radius: 12px;
  padding: 12px 20px;
  display: flex;
  align-items: baseline;
  gap: 6px;

  .stat-num {
    font-size: 22px;
    font-weight: 700;
    color: var(--el-color-primary);
  }
  .stat-label {
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
}

.stat-mood {
  padding: 12px 14px;
  .stat-emoji { font-size: 18px; }
  .stat-num { font-size: 16px; font-weight: 600; }
}

/* Header */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  h2 { margin: 0; font-size: 20px; }
}

/* Filters */
.filter-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

/* Timeline */
.diary-timeline { min-height: 300px; }

.timeline { position: relative; }

.timeline-item {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  position: relative;

  &:not(:last-child)::before {
    content: '';
    position: absolute;
    left: 11px;
    top: 28px;
    bottom: -20px;
    width: 2px;
    background: var(--el-border-color-lighter);
  }
}

.timeline-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 4px;
  background: var(--el-color-primary-light-5);
  border: 3px solid var(--el-color-primary-light-8);
  transition: all 0.2s;

  &.mood-happy { background: #67c23a; border-color: #e1f3d8; }
  &.mood-excited { background: #e6a23c; border-color: #faecd8; }
  &.mood-worried { background: #f56c6c; border-color: #fde2e2; }
  &.mood-sad { background: #909399; border-color: #e9e9eb; }
  &.mood-neutral { background: #409eff; border-color: #d9ecff; }
}

.timeline-card {
  flex: 1;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  padding: 16px 20px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    border-color: var(--el-color-primary-light-5);
    box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  }
}

.card-top {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
}

.card-date {
  text-align: center;
  min-width: 48px;
  flex-shrink: 0;

  .date-day {
    display: block;
    font-size: 26px;
    font-weight: 700;
    color: var(--el-color-primary);
    line-height: 1;
  }
  .date-ym {
    display: block;
    font-size: 11px;
    color: var(--el-text-color-placeholder);
    margin-top: 2px;
  }
}

.card-info { flex: 1; min-width: 0; }

.card-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;

  h3 {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.card-tags { display: flex; gap: 4px; }

.mood-badge, .mood-badge-lg {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 13px;
  background: var(--el-color-primary-light-9);

  &.mood-happy { background: #f0f9eb; }
  &.mood-excited { background: #fdf6ec; }
  &.mood-worried { background: #fef0f0; }
  &.mood-sad { background: #f4f4f5; }
  &.mood-neutral { background: #ecf5ff; }
}

.mood-badge-lg { font-size: 14px; padding: 4px 12px; }
.weather-badge, .weather-badge-lg {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 13px;
  background: var(--el-fill-color-lighter);
}
.weather-badge-lg { font-size: 14px; padding: 4px 12px; }

.card-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.meta-tag {
  background: var(--el-fill-color-lighter);
  padding: 1px 8px;
  border-radius: 10px;
}

.card-photos {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  align-items: center;
}

.card-photo {
  width: 72px;
  height: 72px;
  border-radius: 8px;
}

.photo-more {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  background: var(--el-fill-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: var(--el-text-color-secondary);
  cursor: pointer;
}

.card-content {
  margin-bottom: 10px;
  p {
    margin: 0;
    line-height: 1.6;
    font-size: 14px;
    color: var(--el-text-color-regular);
  }
}

.card-actions {
  display: flex;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid var(--el-border-color-lighter);
}

/* Detail Dialog */
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  h2 { margin: 0; font-size: 18px; }
}

.detail-header-tags { display: flex; gap: 8px; }

.diary-detail { padding: 8px 0; }

.detail-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  .meta-item { font-size: 13px; color: var(--el-text-color-secondary); }
}

.detail-photos {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.detail-photo {
  width: 140px;
  height: 140px;
  border-radius: 8px;
}

.detail-content {
  p {
    margin: 0;
    line-height: 1.8;
    font-size: 15px;
    white-space: pre-wrap;
  }
}

/* Mood Picker */
.mood-picker {
  display: flex;
  gap: 6px;
}

.mood-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  padding: 6px 10px;
  border-radius: 10px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.15s;

  &:hover { background: var(--el-fill-color-lighter); }
  &.active {
    border-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }

  .mood-icon { font-size: 22px; }
  .mood-label { font-size: 11px; color: var(--el-text-color-secondary); }
}

/* Upload */
.upload-area {
  :deep(.el-upload--picture-card) {
    width: 100px;
    height: 100px;
    line-height: 108px;
  }
  :deep(.el-upload-list--picture-card .el-upload-list__item) {
    width: 100px;
    height: 100px;
  }
}

/* Responsive */
@media (max-width: 640px) {
  .filter-row { flex-direction: column; }
  .filter-row .el-input,
  .filter-row .el-select,
  .filter-row .el-date-picker { width: 100% !important; }
  .stats-bar { gap: 8px; }
  .stat-item { padding: 8px 12px; }
  .timeline-item { gap: 10px; }
  .card-top { flex-direction: column; gap: 8px; }
  .card-date {
    display: flex;
    gap: 6px;
    align-items: baseline;
    text-align: left;
    .date-day { font-size: 18px; }
  }
  .card-photos .card-photo { width: 60px; height: 60px; }
  .mood-picker { flex-wrap: wrap; }
}
</style>
