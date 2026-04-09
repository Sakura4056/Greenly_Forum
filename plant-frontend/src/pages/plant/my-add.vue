<template>
  <div class="my-plant-add-container">
    <el-card class="form-card" shadow="hover">
      <template #header>
        <div class="header-container">
          <el-button link @click="goBack" class="back-btn">
            <el-icon><Back /></el-icon> 返回
          </el-button>
          <span class="title">{{ isEdit ? '编辑我的植物' : '添加我的植物' }}</span>
        </div>
      </template>

      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        class="plant-form"
        @submit.prevent
      >
        <el-row :gutter="40">
          <el-col :span="16">
            <el-form-item label="植物昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="给植物起个名字吧，比如：胖橘多肉" />
            </el-form-item>

            <el-form-item label="关联官方图鉴" prop="officialId">
              <el-select 
                v-model="form.officialId" 
                filterable 
                remote 
                reserve-keyword 
                placeholder="搜索官方植物库 (可选)" 
                :remote-method="searchOfficialPlants" 
                :loading="searchLoading"
                clearable
              >
                <el-option 
                  v-for="item in officialPlants" 
                  :key="item.id" 
                  :label="item.name" 
                  :value="item.id" 
                />
              </el-select>
            </el-form-item>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="摆放位置" prop="location">
                  <el-select v-model="form.location" placeholder="选择位置" allow-create filterable class="full-width">
                    <el-option label="主阳台" value="主阳台" />
                    <el-option label="次卧" value="次卧" />
                    <el-option label="客厅" value="客厅" />
                    <el-option label="办公桌" value="办公桌" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="植物来源" prop="source">
                  <el-select v-model="form.source" placeholder="选择来源" allow-create filterable class="full-width">
                    <el-option label="线上购买" value="购买" />
                    <el-option label="花市" value="花市" />
                    <el-option label="赠送" value="赠送" />
                    <el-option label="扦插繁殖" value="扦插" />
                    <el-option label="野外带回" value="野外" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="入住日期" prop="acquiredDate">
                  <el-date-picker 
                    v-model="form.acquiredDate" 
                    type="date" 
                    placeholder="选个好日子" 
                    value-format="YYYY-MM-DD" 
                    class="full-width"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="当前状态" prop="status">
                  <el-select v-model="form.status" placeholder="状态" class="full-width">
                    <el-option label="健康" value="HEALTHY" />
                    <el-option label="生病" value="SICK" />
                    <el-option label="阵亡" value="DEAD" />
                    <el-option label="送人" value="GIFTED" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="备注说明" prop="notes">
              <el-input 
                v-model="form.notes" 
                type="textarea" 
                :rows="4" 
                placeholder="记录它的特殊喜好、养护经验等..." 
              />
            </el-form-item>
          </el-col>

          <el-col :span="8">
            <div class="cover-uploader-wrapper">
              <div class="uploader-label">植物封面图</div>
              <el-upload
                class="cover-uploader"
                action="/api/photo/upload"
                :show-file-list="false"
                :headers="headers"
                :data="uploadData"
                :on-success="handleUploadSuccess"
                :before-upload="beforeUpload"
              >
                <img 
                  v-if="form.coverUrlDisplay || form.coverUrl" 
                  :src="form.coverUrlDisplay || form.coverUrl" 
                  class="cover-preview"
                  @error="handleImageError"
                />
                <!-- 已有图片时，悬停显示更换提示 -->
                <div v-if="form.coverUrlDisplay || form.coverUrl" class="upload-overlay">
                  <el-icon class="upload-overlay-icon"><Camera /></el-icon>
                  <span class="upload-overlay-text">点击更换封面</span>
                </div>
                <!-- 无图片时，显示上传提示 -->
                <div v-else class="upload-trigger">
                  <el-icon class="upload-icon"><Plus /></el-icon>
                  <span class="upload-text">点击上传封面</span>
                </div>
              </el-upload>
              <div class="cover-tip">建议尺寸 800x800，支持 JPG/PNG/WEBP 格式</div>
            </div>
          </el-col>
        </el-row>

        <div class="form-actions">
          <el-button @click="goBack" class="cancel-btn">取消</el-button>
          <el-button type="primary" :loading="submitLoading" class="submit-btn" @click="submitForm">
            <el-icon><Check /></el-icon> <span>保存信息</span>
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Back, Plus, Check, Camera } from '@element-plus/icons-vue'
import { getMyPlantById, addMyPlant, updateMyPlant } from '@/api/my-plant'
import { getOfficialPlantList } from '@/api/plant'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()  // ✅ 初始化 store 实例

const formRef = ref(null)
const submitLoading = ref(false)
const isEdit = ref(false)

// 获取 API 基础 URL
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090'

const form = reactive({
  id: '',
  nickname: '',
  officialId: null,
  location: '',
  source: '',
  acquiredDate: '',
  status: 'HEALTHY',
  coverUrl: '',
  coverUrlDisplay: '', // 用于显示的 URL（/api/photo/view/xxx.jpg）
  notes: ''
})

const rules = {
  nickname: [
    { required: true, message: '请输入植物昵称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ]
}

const headers = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

// 上传附加数据 - 使用 computed 确保响应式更新
const uploadData = computed(() => {
  const data = {
    userId: userStore.userId,
    plantSource: 'LOCAL',
    isPublic: 0,
    fileName: ''
  }
  
  // 只有在编辑模式或有植物ID时才传递 plantId
  if (form.id) {
    data.plantId = Number(form.id)
  }
  // 否则不传 plantId，让后端使用 null
  
  return data
})

const searchLoading = ref(false)
const officialPlants = ref([])

onMounted(() => {
  const id = route.query.id || route.params.id
  if (id) {
    isEdit.value = true
    form.id = id
    loadPlantData(id)
  }
})

const loadPlantData = async (id) => {
  try {
    const res = await getMyPlantById(id)
    console.log('加载植物详情响应:', res)
    
    // 拦截器已经返回 res.data，所以直接访问 res
    if (res) {
      // 逐个字段赋值，保持响应式
      form.id = res.id || ''
      form.nickname = res.nickname || ''
      form.officialId = res.officialId || null
      form.location = res.location || ''
      form.source = res.source || ''
      form.acquiredDate = res.acquiredDate || ''
      form.status = res.status || 'HEALTHY'
      
      // 处理封面图片 URL：分离存储路径和显示路径
      let coverUrlStorage = res.coverUrl || '' // 存储路径：/uploads/xxx.jpg
      let coverUrlDisplay = '' // 显示路径：完整 URL
      
      if (coverUrlStorage) {
        // 使用统一的图片 URL 处理函数
        const { getPhotoViewUrl } = await import('@/api/photo')
        if (coverUrlStorage.startsWith('/uploads/')) {
          // 后端返回的是存储路径，生成显示路径
          const filename = coverUrlStorage.replace('/uploads/', '')
          coverUrlDisplay = getPhotoViewUrl(filename)
        } else if (!coverUrlStorage.startsWith('http')) {
          // 其他相对路径，直接使用 getPhotoViewUrl
          coverUrlDisplay = getPhotoViewUrl(coverUrlStorage)
        } else {
          // 绝对路径
          coverUrlDisplay = coverUrlStorage
        }
      }
      
      form.coverUrl = coverUrlStorage // 保存存储路径用于提交
      form.coverUrlDisplay = coverUrlDisplay // 保存显示路径用于展示
      
      form.notes = res.notes || ''
      
      console.log('表单数据已加载:', form)
      console.log('封面URL:', form.coverUrl)
      
      if (form.officialId) {
        // Just for display, mock a simple option if not loaded
        officialPlants.value = [{ id: form.officialId, name: '已关联官方植物' }]
      }
    } else {
      console.error('响应数据为空')
      ElMessage.error('加载植物数据失败')
    }
  } catch (err) {
    console.error('加载植物数据失败:', err)
    ElMessage.error('加载植物数据失败: ' + (err.message || '未知错误'))
  }
}

const searchOfficialPlants = async (query) => {
  if (query) {
    searchLoading.value = true
    try {
      const res = await getOfficialPlantList({ keyword: query, size: 20 })
      // 拦截器已经返回 res.data，所以直接访问 res.records
      officialPlants.value = res.records || []
      console.log('搜索官方植物结果:', officialPlants.value)
    } catch (e) {
      console.error('搜索官方植物失败:', e)
    } finally {
      searchLoading.value = false
    }
  } else {
    officialPlants.value = []
  }
}

const handleUploadSuccess = async (res) => {
  console.log('=== 上传响应 ===')
  console.log('完整响应:', res)
  
  // 拦截器已经返回 res.data，所以直接访问 res
  // PlantPhoto 对象包含 url 字段（相对路径 /uploads/xxx.jpg）
  let imageUrl = ''
  
  if (res && res.url) {
    imageUrl = res.url
  } else if (res.code === 200 && res.data && res.data.url) {
    // 兼容未通过拦截器的情况
    imageUrl = res.data.url
  }
  
  if (imageUrl) {
    // 分离存储路径和显示路径
    let coverUrlStorage = ''
    let coverUrlDisplay = ''
    
    const { getPhotoViewUrl } = await import('@/api/photo')
    if (imageUrl.startsWith('/uploads/')) {
      // 后端返回的是存储路径，生成显示路径
      coverUrlStorage = imageUrl
      const filename = imageUrl.replace('/uploads/', '')
      coverUrlDisplay = getPhotoViewUrl(filename)
    } else if (!imageUrl.startsWith('http')) {
      // 其他相对路径
      coverUrlStorage = imageUrl
      coverUrlDisplay = getPhotoViewUrl(imageUrl)
    } else {
      // 绝对路径
      coverUrlStorage = imageUrl
      coverUrlDisplay = imageUrl
    }
    
    form.coverUrl = coverUrlStorage // 保存存储路径用于提交
    form.coverUrlDisplay = coverUrlDisplay // 保存显示路径用于展示
    
    console.log('封面存储路径:', form.coverUrl)
    console.log('封面显示路径:', form.coverUrlDisplay)
    ElMessage.success('封面上传成功')
  } else {
    console.error('上传响应格式错误:', res)
    ElMessage.error(res.message || '上传失败')
  }
}

const beforeUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/webp'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) ElMessage.error('上传图片只能是 JPG/PNG/WEBP 格式!')
  if (!isLt5M) ElMessage.error('上传图片大小不能超过 5MB!')
  
  // 文件名会在上传时自动从 file 对象获取，无需手动设置
  
  return isImage && isLt5M
}

// 处理图片加载错误
const handleImageError = (e) => {
  console.warn('图片加载失败，使用默认占位图', e.target.src)
  // 设置为默认占位图
  e.target.src = 'https://images.unsplash.com/photo-1463320726281-696a485928c7?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 构造提交数据，只包含后端需要的字段
        const submitData = {
          nickname: form.nickname,
          officialId: form.officialId,
          location: form.location,
          source: form.source,
          acquiredDate: form.acquiredDate,
          status: form.status,
          coverUrl: form.coverUrl, // 使用存储路径（/uploads/xxx.jpg）
          notes: form.notes
        }
        
        console.log('提交数据:', submitData)
        
        if (isEdit.value) {
          await updateMyPlant(form.id, submitData)
          ElMessage.success('更新成功')
        } else {
          await addMyPlant(submitData)
          ElMessage.success('添加成功')
        }
        router.back()
      } catch (err) {
        console.error('保存失败:', err)
        ElMessage.error(err.message || '保存失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.my-plant-add-container {
  padding: 32px;
  max-width: 1000px;
  margin: 0 auto;
}

.form-card {
  border-radius: 16px;
  border: none;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  box-shadow: 0 12px 36px rgba(110, 136, 110, 0.1);
  overflow: hidden;
}

.header-container {
  display: flex;
  align-items: center;
  gap: 16px;
}

.back-btn {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.back-btn:hover {
  color: var(--el-color-primary);
}

.title {
  font-size: 20px;
  font-weight: bold;
  color: var(--el-text-color-primary);
}

.plant-form {
  padding: 24px 0;
}

.full-width {
  width: 100%;
}

.cover-uploader-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px;
}

.uploader-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-regular);
  margin-bottom: 16px;
  width: 100%;
}

.cover-uploader {
  width: 100%;
  aspect-ratio: 1;
  border: 2px dashed var(--el-border-color);
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
  background-color: var(--el-fill-color-lighter);
}

.cover-uploader:hover {
  border-color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.1);
}

.cover-preview {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  transition: filter 0.3s ease;
}

/* 当有封面图时，悬停显示遮罩层 */
.cover-uploader:has(.cover-preview):hover .cover-preview {
  filter: brightness(0.7);
}

.upload-trigger {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: var(--el-text-color-secondary);
  transition: all 0.3s ease;
}

.upload-icon {
  font-size: 40px;
  margin-bottom: 12px;
  color: var(--el-color-primary-light-3);
  transition: all 0.3s ease;
}

.cover-uploader:hover .upload-icon {
  transform: scale(1.15);
  color: var(--el-color-primary);
}

.upload-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-regular);
  transition: color 0.3s ease;
}

.cover-uploader:hover .upload-text {
  color: var(--el-color-primary);
}

/* 上传遮罩层 - 显示在已有图片上 */
.upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.cover-uploader:hover .upload-overlay {
  opacity: 1;
}

.upload-overlay-icon {
  font-size: 32px;
  color: white;
  margin-bottom: 8px;
}

.upload-overlay-text {
  font-size: 14px;
  color: white;
  font-weight: 500;
}

.cover-tip {
  margin-top: 12px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  text-align: center;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.submit-btn {
  padding: 10px 32px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(var(--el-color-primary-rgb), 0.3);
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(var(--el-color-primary-rgb), 0.4);
}

.cancel-btn {
  padding: 10px 24px;
  border-radius: 8px;
}
</style>
