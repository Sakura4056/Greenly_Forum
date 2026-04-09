<template>
  <main class="app-container responsive-container" role="main">
    <el-card shadow="never" class="identification-card">
      <!-- 页面头部 -->
      <template #header>
        <PageHeader title="植物图片识别" subtitle="上传植物照片，AI 智能识别植物种类、诊断病虫害并提供养护建议">
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
            role="button"
            aria-label="上传植物照片"
            tabindex="0"
            @keydown.enter="triggerUpload"
            @keydown.space.prevent="triggerUpload"
          >
            <div v-if="!selectedImage" class="upload-placeholder">
              <el-icon :size="48"><UploadFilled /></el-icon>
              <p>点击或拖拽上传植物照片</p>
              <p class="upload-tip">支持 JPG、PNG 格式，最大 10MB</p>
            </div>
            <div v-else class="image-preview">
              <el-image :src="selectedImage" fit="contain" class="preview-image" />
              <el-button
                type="danger"
                circle
                @click.stop="removeImage"
                class="remove-image"
                aria-label="移除图片"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
          </el-upload>
        </section>

        <!-- 识别类型选择 -->
        <section class="diagnosis-options" aria-label="识别选项">
          <el-form label-width="100px">
            <el-form-item label="识别类型">
              <el-select v-model="diagnosisType" disabled class="w-full" aria-label="识别类型选择">
                <el-option label="植物种类识别" value="identification">
                  <template #default>
                    <div class="option-content">
                      <el-icon><Search /></el-icon>
                      <span>植物种类识别 - 识别植物的名称和基本信息</span>
                    </div>
                  </template>
                </el-option>
              </el-select>
              <div class="form-tip">当前仅支持植物种类识别功能</div>
            </el-form-item>
          </el-form>
        </section>

        <!-- 操作按钮 -->
        <div class="action-section">
          <el-button
            type="primary"
            @click="startIdentification"
            :loading="identifying"
            :disabled="!selectedImage"
            size="large"
            class="identification-btn"
            aria-label="开始识别"
          >
            <el-icon v-if="!identifying" class="mr-2"><Aim /></el-icon>
            {{ identifying ? '识别中...' : '开始识别' }}
          </el-button>
        </div>

        <!-- 识别结果展示 -->
        <section v-if="identificationResult" class="result-section" aria-label="识别结果" aria-live="polite">
          <ResultCard title="识别结果" status-tag="已完成" status-tag-type="success">
            <div class="result-content">
              <!-- 置信度 -->
              <div v-if="identificationResult.confidence" class="confidence-bar">
                <div class="confidence-label">
                  <span>识别置信度</span>
                  <span class="confidence-value">{{ (identificationResult.confidence * 100).toFixed(1) }}%</span>
                </div>
                <el-progress
                  :percentage="Math.round(identificationResult.confidence * 100)"
                  :color="getConfidenceColor(identificationResult.confidence)"
                  :stroke-width="8"
                  role="progressbar"
                  :aria-valuenow="Math.round(identificationResult.confidence * 100)"
                  aria-valuemin="0"
                  aria-valuemax="100"
                />
              </div>

              <!-- 植物名称 -->
              <ResultItem
                v-if="identificationResult.plantName || identificationResult.name"
                label="植物名称"
                variant="highlight"
              >
                <template #icon><Search /></template>
                <div class="item-value plant-name">{{ identificationResult.plantName || identificationResult.name }}</div>
              </ResultItem>

              <!-- 百度百科链接 -->
              <ResultItem
                v-if="identificationResult.baikeUrl"
                label="百科链接"
                variant="info"
              >
                <template #icon><Link /></template>
                <a :href="identificationResult.baikeUrl" target="_blank" class="baike-link" rel="noopener noreferrer">
                  {{ identificationResult.baikeUrl }}
                  <el-icon><TopRight /></el-icon>
                </a>
              </ResultItem>

              <!-- 分类信息 -->
              <ResultItem
                v-if="identificationResult.classification"
                label="识别详情"
              >
                <template #icon><Document /></template>
                <div class="classification-content">
                  <!-- 提取识别结果 -->
                  <div v-if="extractIdentificationResult(identificationResult.classification)" class="classification-section">
                    <h4>识别结果</h4>
                    <p>{{ extractIdentificationResult(identificationResult.classification) }}</p>
                  </div>

                  <!-- 提取百科简介 -->
                  <div v-if="extractBaikeIntro(identificationResult.classification)" class="classification-section">
                    <h4>百科简介</h4>
                    <p>{{ extractBaikeIntro(identificationResult.classification) }}</p>
                  </div>

                  <!-- 提取养护建议 -->
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

              <!-- 问题描述 -->
              <ResultItem
                v-if="identificationResult.issue"
                label="问题描述"
                variant="warning"
              >
                <template #icon><WarningFilled /></template>
                <div class="item-value">{{ identificationResult.issue }}</div>
              </ResultItem>

              <!-- 建议方案 -->
              <ResultItem
                v-if="identificationResult.suggestions && identificationResult.suggestions.length > 0"
                label="建议方案"
              >
                <template #icon><Document /></template>
                <ol class="suggestions-list">
                  <li v-for="(suggestion, index) in identificationResult.suggestions" :key="index" class="suggestion-item">
                    <span class="suggestion-number">{{ index + 1 }}</span>
                    <span class="suggestion-text">{{ suggestion }}</span>
                  </li>
                </ol>
              </ResultItem>

              <!-- 详细信息表格 -->
              <ResultItem label="详细信息">
                <template #icon><InfoFilled /></template>
                <el-table :data="[identificationResult]" border stripe class="detail-table">
                  <el-table-column prop="name" label="名称" min-width="120" />
                  <el-table-column prop="plantName" label="植物名" min-width="120" />
                  <el-table-column label="置信度" width="120">
                    <template #default="{ row }">
                      <el-tag :type="getConfidenceTagType(row.confidence)">
                        {{ row.confidence ? (row.confidence * 100).toFixed(1) + '%' : '-' }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="score" label="得分" width="100" />
                  <el-table-column label="百科链接" min-width="200">
                    <template #default="{ row }">
                      <a v-if="row.baikeUrl" :href="row.baikeUrl" target="_blank" class="table-link" rel="noopener noreferrer">
                        查看百科 <el-icon><TopRight /></el-icon>
                      </a>
                      <span v-else>-</span>
                    </template>
                  </el-table-column>
                </el-table>
              </ResultItem>
            </div>
          </ResultCard>
        </section>

        <!-- 空状态提示 -->
        <el-empty
          v-if="!selectedImage && !identificationResult"
          description="请上传植物照片开始识别"
          class="empty-state"
        >
          <template #image>
            <el-icon :size="80" color="var(--el-text-color-secondary)"><Picture /></el-icon>
          </template>
        </el-empty>
      </div>
    </el-card>
  </main>
</template>

<script setup>
/**
 * 植物图片识别页面
 * 提供图片上传、AI识别和结果展示功能
 */
import { ref } from 'vue'
import {
  Picture, UploadFilled, Delete, Search, WarningFilled,
  Aim, CircleCheckFilled, Document, InfoFilled, Link, TopRight
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { identifyPlant } from '@/api/ai'
import PageHeader from '@/components/business/PageHeader.vue'
import ResultCard from '@/components/business/ResultCard.vue'
import ResultItem from '@/components/business/ResultItem.vue'

// 响应式状态
const selectedImage = ref('')
const diagnosisType = ref('identification')
const identifying = ref(false)
const identificationResult = ref(null)

/**
 * 从 classification 字段中提取识别结果
 * @param {string} classification - 分类信息文本
 * @returns {string} 识别结果
 */
const extractIdentificationResult = (classification) => {
  if (!classification) return ''
  const match = classification.match(/识别结果[：:]\s*(.+?)(?:\n|$)/)
  return match ? match[1].trim() : ''
}

/**
 * 从 classification 字段中提取百科简介
 * @param {string} classification - 分类信息文本
 * @returns {string} 百科简介
 */
const extractBaikeIntro = (classification) => {
  if (!classification) return ''
  const match = classification.match(/百科简介[：:]\s*([\s\S]*?)(?=养护建议[：:]|$)/)
  if (match) {
    return match[1].trim().replace(/\n/g, ' ')
  }
  return ''
}

/**
 * 从 classification 字段中提取养护建议
 * @param {string} classification - 分类信息文本
 * @returns {Array<string>} 养护建议列表
 */
const extractCareSuggestions = (classification) => {
  if (!classification) return []
  const match = classification.match(/养护建议[：:]\s*([\s\S]*)/)
  if (match) {
    const suggestionsText = match[1].trim()
    return suggestionsText
      .split('\n')
      .map((line) => line.trim())
      .filter((line) => line.startsWith('- ') || line.startsWith('•'))
      .map((line) => line.replace(/^[-•]\s*/, '').trim())
  }
  return []
}

/**
 * 根据置信度获取标签类型
 * @param {number} confidence - 置信度值
 * @returns {string} 标签类型
 */
const getConfidenceTagType = (confidence) => {
  if (!confidence) return 'info'
  if (confidence >= 0.8) return 'success'
  if (confidence >= 0.6) return 'warning'
  return 'danger'
}

/**
 * 触发文件上传
 */
const triggerUpload = () => {
  const uploadEl = document.querySelector('.image-uploader input[type="file"]')
  if (uploadEl) {
    uploadEl.click()
  }
}

/**
 * 处理图片上传
 * @param {Object} file - 上传的文件对象
 */
const handleImageChange = (file) => {
  // 验证文件大小
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB！')
    return
  }

  const reader = new FileReader()
  reader.onload = (e) => {
    selectedImage.value = e.target.result
    // 清除之前的识别结果
    identificationResult.value = null
  }
  reader.onerror = () => {
    ElMessage.error('图片读取失败，请重试')
  }
  reader.readAsDataURL(file.raw)
}

/**
 * 移除图片
 */
const removeImage = () => {
  selectedImage.value = ''
  identificationResult.value = null
}

/**
 * 开始识别
 */
const startIdentification = async () => {
  if (!selectedImage.value) {
    ElMessage.warning('请先上传植物照片')
    return
  }

  identifying.value = true
  identificationResult.value = null

  try {
    // 检查图片大小
    const imageSizeMB = (selectedImage.value.length / 1024 / 1024) * 0.75
    if (imageSizeMB > 5) {
      ElMessage.warning(`图片过大 (${imageSizeMB.toFixed(1)}MB)，建议使用小于 5MB 的图片`)
    }

    // 调用后端API进行图像识别
    const response = await identifyPlant({
      image: selectedImage.value
    })

    // 处理后端返回的识别结果
    let resultData = null

    if (response && response.results && Array.isArray(response.results) && response.results.length > 0) {
      resultData = response.results[0]
    } else if (response && response.data) {
      resultData = response.data
    }

    if (resultData) {
      identificationResult.value = {
        plantName: resultData.plantName || resultData.name,
        issue: resultData.issue || resultData.problem,
        suggestions: resultData.suggestions || resultData.advice,
        confidence: resultData.confidence || resultData.score,
        ...resultData
      }

      ElMessage.success('识别完成')
    } else {
      throw new Error('识别结果为空')
    }
  } catch (error) {
    let errorMsg = '识别失败，请重试'
    if (error.response?.status === 413) {
      errorMsg = '图片过大，请使用小于 5MB 的图片'
    } else if (error.response?.data?.msg) {
      errorMsg = error.response.data.msg
    } else if (error.message) {
      errorMsg = error.message
    }

    ElMessage.error(errorMsg)
  } finally {
    identifying.value = false
  }
}

/**
 * 根据置信度获取颜色
 * @param {number} confidence - 置信度值
 * @returns {string} 颜色值
 */
const getConfidenceColor = (confidence) => {
  if (confidence >= 0.8) return '#67c23a'
  if (confidence >= 0.6) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped lang="scss">
@import '@/styles/responsive.scss';

.app-container {
  min-height: calc(100vh - 120px);
  padding-bottom: var(--spacing-xl);
}

.identification-card {
  border-radius: var(--border-radius-large);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.5);
  background-color: rgba(255, 255, 255, 0.6);
}

.identification-content {
  padding: var(--spacing-xl) 0;
}

.upload-section {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: var(--spacing-lg) 0;
  margin-bottom: var(--spacing-xl);
}

.image-uploader {
  width: 100%;
  max-width: 500px;
}

.upload-placeholder {
  border: 2px dashed var(--el-border-color-darker);
  border-radius: var(--border-radius-large);
  padding: 60px 20px;
  text-align: center;
  background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.03) 0%, rgba(var(--el-color-primary-rgb), 0.08) 100%);
  cursor: pointer;
  transition: all var(--transition-normal);

  &:hover {
    border-color: var(--el-color-primary);
    background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.08) 0%, rgba(var(--el-color-primary-rgb), 0.12) 100%);
    transform: translateY(-2px);
  }

  .el-icon {
    color: var(--el-color-primary-light-3);
    margin-bottom: var(--spacing-md);
  }

  p {
    margin: var(--spacing-sm) 0 4px;
    font-size: 16px;
    color: var(--el-text-color-regular);
    font-weight: 500;
  }

  .upload-tip {
    font-size: 13px;
    color: var(--el-text-color-secondary);
    margin-top: var(--spacing-sm);
  }
}

.image-preview {
  position: relative;
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
  border-radius: var(--border-radius-large);
  overflow: hidden;
  box-shadow: var(--shadow-lg);
  background-color: #f5f7fa;

  &:hover {
    .remove-image {
      opacity: 1;
    }
  }
}

.preview-image {
  width: 100%;
  height: 400px;
  object-fit: contain;
  background-color: #fafafa;
}

.remove-image {
  position: absolute;
  top: var(--spacing-md);
  right: var(--spacing-md);
  background-color: rgba(255, 255, 255, 0.95);
  border: none;
  box-shadow: var(--shadow-md);
  opacity: 0;
  transition: opacity var(--transition-normal);

  &:hover {
    background-color: white;
    transform: scale(1.1);
  }
}

.diagnosis-options {
  background-color: white;
  padding: var(--spacing-xl);
  border-radius: var(--border-radius-base);
  border: 1px solid rgba(0, 0, 0, 0.05);
  margin-bottom: var(--spacing-xl);

  .option-content {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);

    .el-icon {
      color: var(--el-color-primary);
    }
  }

  .form-tip {
    margin-top: var(--spacing-sm);
    font-size: 12px;
    color: var(--el-text-color-secondary);
    padding-left: 100px;
  }
}

.action-section {
  display: flex;
  justify-content: center;
  margin: var(--spacing-xl) 0;
}

.identification-btn {
  padding: 14px 48px;
  font-size: 16px;
  border-radius: 28px;
  box-shadow: 0 4px 16px rgba(var(--el-color-primary-rgb), 0.3);
  transition: all var(--transition-normal);

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(var(--el-color-primary-rgb), 0.4);
  }

  &:disabled {
    opacity: 0.6;
  }
}

.result-section {
  margin-top: var(--spacing-xl);
  animation: fadeInUp 0.5s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-content {
  margin-top: var(--spacing-sm);

  .confidence-bar {
    margin-bottom: var(--spacing-xl);
    padding: var(--spacing-md);
    background: linear-gradient(135deg, rgba(var(--el-color-primary-rgb), 0.03) 0%, rgba(var(--el-color-primary-rgb), 0.06) 100%);
    border-radius: var(--border-radius-base);

    .confidence-label {
      display: flex;
      justify-content: space-between;
      margin-bottom: var(--spacing-sm);
      font-size: 14px;
      font-weight: 500;

      .confidence-value {
        color: var(--el-color-primary);
        font-weight: 600;
      }
    }
  }

  .item-value {
    color: var(--el-text-color-regular);
    line-height: 1.6;
    font-size: 15px;

    &.plant-name {
      font-size: 20px;
      font-weight: 600;
      color: var(--el-color-success);
    }
  }

  .suggestions-list,
  .care-suggestions-list {
    margin-top: var(--spacing-sm);
    list-style: none;
    padding: 0;

    .suggestion-item,
    .care-item {
      display: flex;
      gap: var(--spacing-sm);
      margin-bottom: var(--spacing-sm);
      padding: var(--spacing-sm);
      background-color: white;
      border-radius: var(--border-radius-small);
      transition: all 0.2s ease;

      &:hover {
        background-color: rgba(var(--el-color-primary-rgb), 0.03);
        transform: translateX(4px);
      }

      .suggestion-number,
      .care-number {
        flex-shrink: 0;
        width: 24px;
        height: 24px;
        background: linear-gradient(135deg, var(--el-color-primary) 0%, var(--el-color-primary-light-3) 100%);
        color: white;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: 600;
      }

      .suggestion-text,
      .care-text {
        flex: 1;
        line-height: 1.6;
        color: var(--el-text-color-regular);
      }
    }
  }

  .classification {
    .classification-content {
      margin-top: var(--spacing-sm);

      .classification-section {
        margin-bottom: var(--spacing-md);

        &:last-child {
          margin-bottom: 0;
        }

        h4 {
          margin: 0 0 var(--spacing-sm) 0;
          font-size: 14px;
          font-weight: 600;
          color: var(--el-text-color-primary);
        }

        p {
          margin: 0;
          font-size: 14px;
          line-height: 1.8;
          color: var(--el-text-color-regular);
          text-align: justify;
        }
      }
    }
  }

  .info-table {
    .detail-table {
      margin-top: var(--spacing-sm);

      :deep(.el-table__header) {
        th {
          background-color: #f5f7fa;
          color: var(--el-text-color-primary);
          font-weight: 600;
        }
      }

      :deep(.el-table__row) {
        &:hover {
          background-color: rgba(var(--el-color-primary-rgb), 0.02);
        }
      }

      .table-link {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        color: var(--el-color-primary);
        text-decoration: none;

        &:hover {
          text-decoration: underline;
        }

        .el-icon {
          font-size: 12px;
        }
      }
    }
  }
}

.empty-state {
  padding: var(--spacing-xxl) 0;
}

.mr-2 {
  margin-right: var(--spacing-sm);
}

.w-full {
  width: 100%;
}

// 百科链接样式
.baike-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--el-color-primary);
  text-decoration: none;
  font-size: 13px;
  word-break: break-all;

  &:hover {
    text-decoration: underline;
  }

  .el-icon {
    font-size: 12px;
  }
}

@include respond-to(mobile) {
  .preview-image {
    height: 300px;
  }

  .diagnosis-options {
    .form-tip {
      padding-left: 0;
    }
  }
}
</style>
