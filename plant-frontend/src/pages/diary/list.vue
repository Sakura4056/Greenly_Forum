<template>
  <div class="app-container">
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
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="植物">
          <el-select v-model="filterForm.plantId" placeholder="选择植物" clearable style="width: 200px">
            <el-option
              v-for="plant in plants"
              :key="plant.id"
              :label="plant.nickname"
              :value="plant.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="心情">
          <el-select v-model="filterForm.mood" placeholder="选择心情" clearable style="width: 150px">
            <el-option label="开心" value="happy" />
            <el-option label="难过" value="sad" />
            <el-option label="平静" value="neutral" />
            <el-option label="兴奋" value="excited" />
            <el-option label="担心" value="worried" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchDiaries">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- Diary List -->
      <div v-loading="loading" class="diary-list">
        <el-empty v-if="!loading && diaries.length === 0" description="还没有日记，开始记录吧！" />

        <div v-for="diary in diaries" :key="diary.id" class="diary-card">
          <el-card shadow="hover" @click="viewDetail(diary)">
            <div class="diary-header">
              <div class="diary-title">
                <h3>{{ diary.title || '无标题' }}</h3>
                <el-tag :type="getMoodTagType(diary.mood)" size="small">
                  {{ getMoodEmoji(diary.mood) }} {{ getMoodText(diary.mood) }}
                </el-tag>
                <el-tag v-if="diary.weather" type="info" size="small" style="margin-left: 8px">
                  {{ getWeatherEmoji(diary.weather) }} {{ getWeatherText(diary.weather) }}
                </el-tag>
              </div>
              <div class="diary-meta">
                <span class="plant-name">🌱 {{ diary.plantNickname }}</span>
                <span class="date">📅 {{ diary.diaryDate }}</span>
              </div>
            </div>

            <!-- Photo thumbnails -->
            <div v-if="diary.photoUrls && diary.photoUrls.length > 0" class="diary-photos">
              <el-image
                v-for="(url, idx) in diary.photoUrls.slice(0, 3)"
                :key="idx"
                :src="url"
                fit="cover"
                class="diary-thumb"
                :preview-src-list="diary.photoUrls"
                :initial-index="idx"
                @click.stop
              />
              <span v-if="diary.photoUrls.length > 3" class="photo-more">
                +{{ diary.photoUrls.length - 3 }}
              </span>
            </div>

            <div class="diary-content">
              <p>{{ truncateContent(diary.content, 150) }}</p>
            </div>

            <div class="diary-footer" @click.stop>
              <el-button type="primary" link @click="viewDetail(diary)">查看详情</el-button>
              <el-button type="warning" link @click="editDiary(diary)">编辑</el-button>
              <el-button type="danger" link @click="confirmDelete(diary.id)">删除</el-button>
            </div>
          </el-card>
        </div>
      </div>

      <!-- Pagination -->
      <el-pagination
        v-if="total > 0"
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="fetchDiaries"
        style="margin-top: 20px; justify-content: center"
      />
    </el-card>

    <!-- Detail Dialog -->
    <el-dialog
      v-model="showDetailDialog"
      :title="detailDiary?.title || '日记详情'"
      width="700px"
      class="diary-detail-dialog"
    >
      <div v-if="detailDiary" class="diary-detail">
        <div class="detail-meta">
          <span class="meta-item">🌱 {{ detailDiary.plantNickname }}</span>
          <span class="meta-item">📅 {{ detailDiary.diaryDate }}</span>
          <el-tag :type="getMoodTagType(detailDiary.mood)" size="small">
            {{ getMoodEmoji(detailDiary.mood) }} {{ getMoodText(detailDiary.mood) }}
          </el-tag>
          <el-tag v-if="detailDiary.weather" type="info" size="small" style="margin-left: 8px">
            {{ getWeatherEmoji(detailDiary.weather) }} {{ getWeatherText(detailDiary.weather) }}
          </el-tag>
        </div>

        <!-- Photos -->
        <div v-if="detailDiary.photoUrls && detailDiary.photoUrls.length > 0" class="detail-photos">
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

        <div class="detail-time">
          创建于 {{ detailDiary.createTime }}
        </div>
      </div>
    </el-dialog>

    <!-- Create/Edit Dialog -->
    <el-dialog
      v-model="showCreateDialog"
      :title="editingDiary ? '编辑日记' : '写日记'"
      width="700px"
      @closed="resetForm"
    >
      <el-form :model="diaryForm" label-width="80px">
        <el-form-item label="植物" required>
          <el-select v-model="diaryForm.plantId" placeholder="选择植物" style="width: 100%">
            <el-option
              v-for="plant in plants"
              :key="plant.id"
              :label="plant.nickname"
              :value="plant.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="diaryForm.title" placeholder="输入标题（可选）" />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="diaryForm.content"
            type="textarea"
            :rows="6"
            placeholder="记录今天的植物养护心得..."
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
        <el-form-item label="心情">
          <el-radio-group v-model="diaryForm.mood">
            <el-radio value="happy">😊 开心</el-radio>
            <el-radio value="excited">🤩 兴奋</el-radio>
            <el-radio value="neutral">😐 平静</el-radio>
            <el-radio value="worried">😟 担心</el-radio>
            <el-radio value="sad">😢 难过</el-radio>
          </el-radio-group>
        </el-form-item>
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
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitDiary" :loading="submitting">
          保存
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { queryDiaries, createDiary, updateDiary, deleteDiary } from '@/api/diary'
import { getMyPlantList } from '@/api/my-plant'

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

const filterForm = reactive({
  plantId: null,
  mood: null
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10
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

import request from '@/api/request'

// Custom upload handler - includes plantSource param
const customUpload = async (options) => {
  const { file, onSuccess, onError } = options
  const formData = new FormData()
  formData.append('file', file)
  formData.append('plantSource', 'LOCAL')
  formData.append('isPublic', '0')
  if (diaryForm.plantId) {
    formData.append('plantId', diaryForm.plantId)
  }
  try {
    const res = await request({
      url: '/photo/upload',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    onSuccess(res)
  } catch (err) {
    onError(err)
  }
}

// Upload handlers
const onUploadSuccess = (response) => {
  const data = response?.data || response
  if (data && data.id) {
    diaryForm.photoIds.push(data.id)
  }
}

const onUploadRemove = (file) => {
  const data = file.response?.data || file.response
  if (data && data.id) {
    const idx = diaryForm.photoIds.indexOf(data.id)
    if (idx > -1) diaryForm.photoIds.splice(idx, 1)
  }
}

const onPreview = (file) => {
  previewUrl.value = file.url || file.response?.data?.url || file.response?.url
  showPreview.value = true
}

// Fetch plants
const fetchPlants = async () => {
  try {
    const res = await getMyPlantList({ pageNum: 1, pageSize: 100 })
    plants.value = res.records || []
  } catch (error) {
    console.error('Failed to fetch plants:', error)
  }
}

// Fetch diaries
const fetchDiaries = async () => {
  loading.value = true
  try {
    const params = {
      ...filterForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await queryDiaries(params)
    diaries.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('获取日记列表失败')
  } finally {
    loading.value = false
  }
}

// Reset filter
const resetFilter = () => {
  filterForm.plantId = null
  filterForm.mood = null
  pagination.pageNum = 1
  fetchDiaries()
}

// View detail
const viewDetail = (diary) => {
  detailDiary.value = diary
  showDetailDialog.value = true
}

// Open create dialog
const openCreateDialog = () => {
  editingDiary.value = null
  resetForm()
  showCreateDialog.value = true
}

// Edit diary
const editDiary = (diary) => {
  editingDiary.value = diary
  // Populate form
  diaryForm.id = diary.id
  diaryForm.plantId = diary.plantId
  diaryForm.title = diary.title || ''
  diaryForm.content = diary.content
  diaryForm.mood = diary.mood || 'neutral'
  diaryForm.weather = diary.weather || ''
  diaryForm.diaryDate = new Date(diary.diaryDate)
  diaryForm.photoIds = diary.photoIds ? [...diary.photoIds] : []
  // Populate fileList from existing photos
  diaryForm.fileList = (diary.photoUrls || []).map((url, idx) => ({
    name: `photo_${idx}`,
    url: url
  }))
  showCreateDialog.value = true
}

// Confirm delete
const confirmDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇日记吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteDiary(id)
    ElMessage.success('删除成功')
    fetchDiaries()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// Submit diary
const submitDiary = async () => {
  if (!diaryForm.plantId || !diaryForm.content) {
    ElMessage.warning('请选择植物并填写内容')
    return
  }

  submitting.value = true
  try {
    const data = {
      plantId: diaryForm.plantId,
      title: diaryForm.title,
      content: diaryForm.content,
      mood: diaryForm.mood,
      weather: diaryForm.weather,
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
      ElMessage.success('创建成功')
    }

    showCreateDialog.value = false
    resetForm()
    fetchDiaries()
  } catch (error) {
    const errorMsg = error.response?.data?.msg || error.message || '操作失败'
    ElMessage.error(errorMsg)
  } finally {
    submitting.value = false
  }
}

// Reset form
const resetForm = () => {
  editingDiary.value = null
  Object.assign(diaryForm, {
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
}

// Helper functions
const getMoodTagType = (mood) => {
  const types = { happy: 'success', excited: 'success', neutral: 'info', worried: 'warning', sad: 'danger' }
  return types[mood] || 'info'
}

const getMoodEmoji = (mood) => {
  const emojis = { happy: '😊', excited: '🤩', neutral: '😐', worried: '😟', sad: '😢' }
  return emojis[mood] || '😐'
}

const getMoodText = (mood) => {
  const texts = { happy: '开心', excited: '兴奋', neutral: '平静', worried: '担心', sad: '难过' }
  return texts[mood] || '平静'
}

const getWeatherEmoji = (weather) => {
  const emojis = { sunny: '☀️', cloudy: '⛅', rainy: '🌧️', snowy: '❄️', windy: '💨' }
  return emojis[weather] || ''
}

const getWeatherText = (weather) => {
  const texts = { sunny: '晴天', cloudy: '多云', rainy: '雨天', snowy: '雪天', windy: '有风' }
  return texts[weather] || ''
}

const truncateContent = (content, maxLength) => {
  if (!content) return ''
  return content.length > maxLength ? content.substring(0, maxLength) + '...' : content
}

onMounted(() => {
  fetchPlants()
  fetchDiaries()
})
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  h2 { margin: 0; font-size: 20px; }
}

.filter-form { margin-bottom: 20px; }

.diary-list { min-height: 400px; }

.diary-card {
  margin-bottom: 16px;
  cursor: pointer;
  &:last-child { margin-bottom: 0; }
  :deep(.el-card__body) { transition: background-color 0.2s; }
}

.diary-header { margin-bottom: 12px; }

.diary-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
  h3 { margin: 0; font-size: 16px; flex: 1; }
}

.diary-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.diary-photos {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
  align-items: center;
  flex-wrap: wrap;
}

.diary-thumb {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  cursor: pointer;
}

.photo-more {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  padding: 0 8px;
}

.diary-content {
  margin: 12px 0;
  p { margin: 0; line-height: 1.6; color: var(--el-text-color-regular); }
}

.diary-footer {
  display: flex;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}

/* Detail Dialog */
.diary-detail {
  padding: 8px 0;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
  .meta-item { font-size: 14px; color: var(--el-text-color-secondary); }
}

.detail-photos {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.detail-photo {
  width: 150px;
  height: 150px;
  border-radius: 8px;
  cursor: pointer;
}

.detail-content {
  margin-bottom: 16px;
  p {
    margin: 0;
    line-height: 1.8;
    font-size: 15px;
    color: var(--el-text-color-regular);
    white-space: pre-wrap;
  }
}

.detail-time {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  text-align: right;
}

/* Upload Area */
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
</style>
