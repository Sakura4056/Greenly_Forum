<template>
  <main class="app-container responsive-container" role="main">
    <el-card shadow="never" class="identification-card">
      <template #header>
        <PageHeader title="植物图片识别" subtitle="上传植物照片，AI 智能识别植物种类">
          <template #icon>
            <el-icon :size="24" color="var(--color-primary)"><Picture /></el-icon>
          </template>
        </PageHeader>
      </template>

      <div class="identification-content">
        <!-- 图片上传区域 -->
        <section class="upload-section" aria-label="图片上传">
          <el-upload
            class="image-uploader"
            action="#"
            :auto-upload="false"
            :on-change="handleImageChange"
            :show-file-list="false"
            accept="image/*"
          >
            <div v-if="!selectedImage" class="upload-placeholder">
              <el-icon :size="48"><UploadFilled /></el-icon>
              <p>点击上传植物照片</p>
              <p class="upload-tip">支持 JPG、PNG 格式，最大 10MB</p>
            </div>
            <div v-else class="image-preview">
              <el-image :src="selectedImage" fit="contain" class="preview-image" />
              <el-button type="danger" circle @click.stop="removeImage" class="remove-image">
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </el-upload>
        </section>

        <!-- 操作按钮 -->
        <div class="action-section">
          <el-button type="primary" @click="startIdentification" :loading="identifying" :disabled="!selectedImage" size="large" class="identification-btn">
            <el-icon v-if="!identifying" class="mr-2"><Aim /></el-icon>
            {{ identifying ? '识别中...' : '开始识别' }}
          </el-button>
        </div>

        <!-- 识别结果 -->
        <section v-if="identificationResult" class="result-section" aria-live="polite">
          <ResultCard title="识别结果" status-tag="已完成" status-tag-type="success">
            <div class="result-content">
              <div v-if="identificationResult.confidence" class="confidence-bar">
                <div class="confidence-label">
                  <span>识别置信度</span>
                  <span class="confidence-value">{{ (identificationResult.confidence * 100).toFixed(1) }}%</span>
                </div>
                <el-progress :percentage="Math.round(identificationResult.confidence * 100)" :color="getConfidenceColor(identificationResult.confidence)" :stroke-width="8" />
              </div>

              <ResultItem v-if="identificationResult.plantName || identificationResult.name" label="植物名称" variant="highlight">
                <template #icon><Search /></template>
                <div class="item-value plant-name">{{ identificationResult.plantName || identificationResult.name }}</div>
              </ResultItem>

              <ResultItem v-if="identificationResult.baikeUrl" label="百科链接" variant="info">
                <template #icon><Link /></template>
                <a :href="identificationResult.baikeUrl" target="_blank" class="baike-link" rel="noopener noreferrer">
                  查看百科 <el-icon><TopRight /></el-icon>
                </a>
              </ResultItem>

              <ResultItem v-if="identificationResult.classification" label="识别详情">
                <template #icon><Document /></template>
                <div class="classification-content">
                  <div v-if="extractIdentificationResult(identificationResult.classification)" class="classification-section">
                    <h4>识别结果</h4>
                    <p>{{ extractIdentificationResult(identificationResult.classification) }}</p>
                  </div>
                  <div v-if="extractBaikeIntro(identificationResult.classification)" class="classification-section">
                    <h4>百科简介</h4>
                    <p>{{ extractBaikeIntro(identificationResult.classification) }}</p>
                  </div>
                  <div v-if="extractCareSuggestions(identificationResult.classification).length > 0" class="classification-section">
                    <h4>养护建议</h4>
                    <ol class="care-suggestions-list">
                      <li v-for="(item, index) in extractCareSuggestions(identificationResult.classification)" :key="index" class="care-item">
                        <span class="care-number">{{ index + 1 }}</span>
                        <span class="care-text">{{ item }}</span>
                      </li>
                    </ol>
                  </div>
                </div>
              </ResultItem>
            </div>
          </ResultCard>
        </section>

        <!-- 历史记录 -->
        <section class="history-section">
          <div class="history-header">
            <h3><el-icon><Clock /></el-icon> 识别历史</h3>
            <el-button v-if="historyList.length > 0" type="danger" text size="small" @click="handleClearHistory">
              <el-icon><Delete /></el-icon> 清空
            </el-button>
          </div>

          <div v-if="historyLoading" class="history-loading"><el-skeleton :rows="3" animated /></div>

          <div v-else-if="historyList.length === 0" class="history-empty">
            <el-empty description="暂无识别记录" :image-size="60">
              <template #image><el-icon :size="40" color="var(--el-text-color-placeholder)"><Clock /></el-icon></template>
            </el-empty>
          </div>

          <div v-else class="history-list">
            <div v-for="item in historyList" :key="item.id" class="history-item" @click="viewHistoryDetail(item)">
              <div class="history-item-info">
                <div class="history-plant-name">{{ item.plantName || '未知植物' }}</div>
                <div class="history-meta">
                  <el-tag :type="getConfidenceTagType(item.confidence)" size="small">
                    {{ item.confidence ? (item.confidence * 100).toFixed(1) + '%' : '-' }}
                  </el-tag>
                  <span class="history-time">{{ formatTime(item.createTime) }}</span>
                </div>
              </div>
              <el-button type="danger" text circle size="small" @click.stop="handleDeleteHistory(item.id)">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
        </section>

        <el-empty v-if="!selectedImage && !identificationResult" description="请上传植物照片开始识别" class="empty-state">
          <template #image><el-icon :size="80" color="var(--el-text-color-secondary)"><Picture /></el-icon></template>
        </el-empty>
      </div>
    </el-card>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Picture, UploadFilled, Delete, Search, Aim, Document, Link, TopRight, Clock, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { identifyPlant, getIdentifyHistory, deleteIdentifyHistory, clearIdentifyHistory } from '@/api/ai'
import PageHeader from '@/components/business/PageHeader.vue'
import ResultCard from '@/components/business/ResultCard.vue'
import ResultItem from '@/components/business/ResultItem.vue'

const selectedImage = ref('')
const identifying = ref(false)
const identificationResult = ref(null)
const historyList = ref([])
const historyLoading = ref(false)

onMounted(() => { loadHistory() })

const loadHistory = async () => {
  historyLoading.value = true
  try {
    const data = await getIdentifyHistory(20)
    historyList.value = Array.isArray(data) ? data : []
  } catch (e) { console.error('Failed to load history:', e) }
  finally { historyLoading.value = false }
}

const viewHistoryDetail = (item) => {
  identificationResult.value = {
    plantName: item.plantName, confidence: item.confidence,
    baikeUrl: item.baikeUrl, classification: item.classification,
    name: item.plantName, score: item.confidence
  }
  selectedImage.value = ''
}

const handleDeleteHistory = async (id) => {
  try { await deleteIdentifyHistory(id); historyList.value = historyList.value.filter(h => h.id !== id); ElMessage.success('已删除') }
  catch (e) { ElMessage.error('删除失败') }
}

const handleClearHistory = async () => {
  try {
    await ElMessageBox.confirm('确定清空所有识别历史？', '提示', { type: 'warning' })
    await clearIdentifyHistory(); historyList.value = []; ElMessage.success('已清空')
  } catch (e) {}
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr), now = new Date(), diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return `${d.getMonth() + 1}-${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const extractIdentificationResult = (c) => { if (!c) return ''; const m = c.match(/识别结果[：:]\s*(.+?)(?:\n|$)/); return m ? m[1].trim() : '' }
const extractBaikeIntro = (c) => { if (!c) return ''; const m = c.match(/百科简介[：:]\s*([\s\S]*?)(?=养护建议[：:]|$)/); return m ? m[1].trim().replace(/\n/g, ' ') : '' }
const extractCareSuggestions = (c) => {
  if (!c) return []; const m = c.match(/养护建议[：:]\s*([\s\S]*)/)
  if (m) return m[1].trim().split('\n').map(l => l.trim()).filter(l => l.startsWith('- ') || l.startsWith('•')).map(l => l.replace(/^[-•]\s*/, '').trim())
  return []
}
const getConfidenceTagType = (c) => { if (!c) return 'info'; if (c >= 0.8) return 'success'; if (c >= 0.6) return 'warning'; return 'danger' }
const getConfidenceColor = (c) => { if (c >= 0.8) return '#67c23a'; if (c >= 0.6) return '#e6a23c'; return '#f56c6c' }

const handleImageChange = (file) => {
  if (file.size / 1024 / 1024 >= 10) { ElMessage.error('图片大小不能超过 10MB！'); return }
  const reader = new FileReader()
  reader.onload = (e) => { selectedImage.value = e.target.result; identificationResult.value = null }
  reader.readAsDataURL(file.raw)
}

const removeImage = () => { selectedImage.value = ''; identificationResult.value = null }

const startIdentification = async () => {
  if (!selectedImage.value) { ElMessage.warning('请先上传植物照片'); return }
  identifying.value = true; identificationResult.value = null
  try {
    const response = await identifyPlant({ image: selectedImage.value })
    let resultData = null
    if (response?.results?.length > 0) resultData = response.results[0]
    else if (response?.data) resultData = response.data
    if (resultData) {
      identificationResult.value = { plantName: resultData.plantName || resultData.name, confidence: resultData.confidence || resultData.score, baikeUrl: resultData.baikeUrl, classification: resultData.classification, ...resultData }
      ElMessage.success('识别完成'); loadHistory()
    } else throw new Error('识别结果为空')
  } catch (error) { ElMessage.error(error.response?.data?.msg || error.message || '识别失败') }
  finally { identifying.value = false }
}
</script>

<style scoped lang="scss">
.app-container { min-height: calc(100vh - 120px); padding-bottom: 24px; }

.identification-card {
  border-radius: 16px; box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.5); background-color: rgba(255, 255, 255, 0.6);
}

.identification-content { padding: 24px 0; }

.upload-section { display: flex; justify-content: center; padding: 16px 0; margin-bottom: 24px; }
.image-uploader { width: 100%; max-width: 500px; }

.upload-placeholder {
  border: 2px dashed var(--el-border-color-darker); border-radius: 16px;
  padding: 48px 20px; text-align: center; cursor: pointer; transition: all 0.3s;
  background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.03), rgba(var(--el-color-primary-rgb), 0.08));
  &:hover { border-color: var(--el-color-primary); transform: translateY(-2px); }
  .el-icon { color: var(--el-color-primary-light-3); margin-bottom: 12px; }
  p { margin: 8px 0 4px; font-size: 16px; color: var(--el-text-color-regular); font-weight: 500; }
  .upload-tip { font-size: 13px; color: var(--el-text-color-secondary); }
}

.image-preview {
  position: relative; width: 100%; max-width: 500px; margin: 0 auto;
  border-radius: 16px; overflow: hidden; box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  &:hover .remove-image { opacity: 1; }
}
.preview-image { width: 100%; height: 400px; object-fit: contain; background: #fafafa; }
.remove-image {
  position: absolute; top: 12px; right: 12px; background: rgba(255,255,255,0.95);
  border: none; box-shadow: 0 2px 8px rgba(0,0,0,0.15); opacity: 0; transition: opacity 0.3s;
}

.action-section { display: flex; justify-content: center; margin: 24px 0; }
.identification-btn {
  padding: 14px 48px; font-size: 16px; border-radius: 28px;
  box-shadow: 0 4px 16px rgba(var(--el-color-primary-rgb), 0.3);
  &:hover:not(:disabled) { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(var(--el-color-primary-rgb), 0.4); }
}

.result-section { margin-top: 24px; animation: fadeInUp 0.5s ease; }
@keyframes fadeInUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

.result-content {
  margin-top: 8px;
  .confidence-bar {
    margin-bottom: 24px; padding: 16px; border-radius: 8px;
    background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.03), rgba(var(--el-color-primary-rgb), 0.06));
    .confidence-label { display: flex; justify-content: space-between; margin-bottom: 8px; font-size: 14px; font-weight: 500;
      .confidence-value { color: var(--el-color-primary); font-weight: 600; }
    }
  }
  .item-value { color: var(--el-text-color-regular); line-height: 1.6; font-size: 15px;
    &.plant-name { font-size: 20px; font-weight: 600; color: var(--el-color-success); }
  }
  .care-suggestions-list {
    margin-top: 8px; list-style: none; padding: 0;
    .care-item {
      display: flex; gap: 8px; margin-bottom: 8px; padding: 8px; background: white; border-radius: 6px;
      .care-number { flex-shrink: 0; width: 24px; height: 24px; background: linear-gradient(135deg, var(--el-color-primary), var(--el-color-primary-light-3)); color: white; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 600; }
      .care-text { flex: 1; line-height: 1.6; }
    }
  }
}

.baike-link { display: inline-flex; align-items: center; gap: 4px; color: var(--el-color-primary); text-decoration: none; font-size: 13px;
  &:hover { text-decoration: underline; }
}

.history-section { margin-top: 40px; padding-top: 24px; border-top: 1px solid var(--el-border-color-lighter); }
.history-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  h3 { margin: 0; font-size: 16px; font-weight: 600; display: flex; align-items: center; gap: 8px; }
}
.history-list { display: flex; flex-direction: column; gap: 8px; }
.history-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: 12px 16px; background: #fafafa; border-radius: 10px;
  cursor: pointer; transition: all 0.2s;
  &:hover { background: rgba(var(--el-color-primary-rgb), 0.05); }
}
.history-item-info { flex: 1; min-width: 0; }
.history-plant-name { font-weight: 600; font-size: 15px; margin-bottom: 4px; }
.history-meta { display: flex; align-items: center; gap: 8px; }
.history-time { font-size: 12px; color: var(--el-text-color-secondary); }
.history-empty { padding: 24px 0; }
.history-loading { padding: 16px 0; }
.empty-state { padding: 48px 0; }
.mr-2 { margin-right: 8px; }

@media (max-width: 768px) {
  .app-container { padding-bottom: 16px; }
  .identification-card { border-radius: 12px; }
  .identification-content { padding: 16px 0; }
  .upload-placeholder { padding: 36px 16px; border-radius: 12px; }
  .preview-image { height: 280px; }
  .identification-btn { padding: 12px 32px; font-size: 15px; }
  .result-content .item-value.plant-name { font-size: 18px; }
  .history-item { padding: 10px 12px; }
}

@media (max-width: 480px) {
  .upload-placeholder { padding: 28px 12px;
    p { font-size: 14px; }
    .upload-tip { font-size: 12px; }
  }
  .preview-image { height: 220px; }
  .identification-btn { padding: 10px 24px; width: 100%; }
  .confidence-bar { padding: 12px; }
  .result-section :deep(.el-table) { font-size: 12px; }
}
</style>
