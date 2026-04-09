<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>上传成长照片</span>
        </div>
      </template>

      <el-form :model="form" ref="formRef" label-width="100px" style="max-width: 600px">
        <el-form-item label="选择植物" required>
            <el-select 
                v-model="form.plantId" 
                placeholder="请选择植物" 
                style="width: 100%"
                :loading="loadingPlants"
                @change="handlePlantChange"
            >
                <el-option
                    v-for="item in plantOptions"
                    :key="item.id"
                    :label="item.nickname || item.name"
                    :value="item.id"
                />
            </el-select>
        </el-form-item>
        
        <el-form-item label="照片备注">
            <el-input v-model="form.remarks" type="textarea" />
        </el-form-item>
        
         <el-form-item label="图片文件" required>
            <el-upload
                class="upload-demo"
                drag
                :action="uploadUrl"
                :headers="headers"
                :data="formData"
                :before-upload="beforeUpload"
                :on-success="handleSuccess"
                :on-error="handleError"
            >
                <el-icon class="el-icon--upload"><upload-filled /></el-icon>
                <div class="el-upload__text">
                    拖拽文件到此处 或 <em>点击上传</em>
                </div>
                <template #tip>
                    <div class="el-upload__tip">
                        只能上传 jpg/png 文件，且不超过 5MB
                    </div>
                </template>
            </el-upload>
        </el-form-item>
        
        <el-form-item>
            <el-button @click="$router.back()">返回列表</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getMyPlantList } from '@/api/my-plant'

const router = useRouter()
const userStore = useUserStore()
const uploadUrl = '/api/photo/upload'

const form = reactive({
    plantId: '',
    plantSource: 'LOCAL', // LOCAL 表示我的植物
    plantName: '',
    remarks: ''
})

const loadingPlants = ref(false)
const plantOptions = ref([])

const headers = computed(() => ({
    'Authorization': `Bearer ${userStore.token}`
}))

// Extra data to send with the file
const formData = reactive({
    userId: userStore.userId,
    plantId: form.plantId,
    plantSource: form.plantSource,
    remarks: form.remarks,
    isPublic: 0, // Default private
    fileName: '' // Will be set in beforeUpload
})

// Watch for form changes to update formData
watch(() => form.plantId, (val) => {
    formData.plantId = val
}, { immediate: true })

watch(() => form.plantSource, (val) => {
    formData.plantSource = val
}, { immediate: true })

watch(() => form.remarks, (val) => {
    formData.remarks = val
}, { immediate: true })

/**
 * 加载我的植物列表
 */
const loadPlants = async () => {
    loadingPlants.value = true
    try {
        const res = await getMyPlantList({ pageNum: 1, pageSize: 1000 })
        plantOptions.value = res.records || []
        console.log('我的植物列表:', plantOptions.value)
    } catch (e) {
        console.error('加载植物列表失败:', e)
        ElMessage.error('无法加载植物列表')
    } finally {
        loadingPlants.value = false
    }
}

/**
 * 处理植物选择变化
 */
const handlePlantChange = (val) => {
    const selected = plantOptions.value.find(p => p.id === val)
    if (selected) {
        // 使用昵称作为显示名称
        form.plantName = selected.nickname || selected.name
        // 确保 plantSource 为 LOCAL
        form.plantSource = 'LOCAL'
    }
}

const beforeUpload = (rawFile) => {
    if (!form.plantId) {
        ElMessage.warning('请先选择植物')
        return false
    }
    if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
        ElMessage.error('只能上传 JPG/PNG 格式的图片!')
        return false
    }
    if (rawFile.size / 1024 / 1024 > 5) {
        ElMessage.error('图片大小不能超过 5MB!')
        return false
    }
    // 保存文件名到 extraData 中
    formData.fileName = rawFile.name
    return true
}

const handleSuccess = (res) => {
    if (res.code === 200) {
        ElMessage.success('上传成功')
        router.push('/photo/list')
    } else {
        ElMessage.error(res.message || '上传失败')
    }
}

const handleError = () => {
    ElMessage.error('上传出错')
}

onMounted(() => {
    loadPlants()
})
</script>
