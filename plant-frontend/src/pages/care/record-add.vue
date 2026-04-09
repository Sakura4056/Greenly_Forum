<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>添加养护记录</span>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 600px">
        <el-form-item label="任务信息" v-if="form.taskName">
          <el-tag>{{ form.taskName }}</el-tag>
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">(来自养护计划)</span>
        </el-form-item>

        <el-form-item label="选择植物" prop="selectedPlantKey" required>
          <el-select 
            v-model="form.selectedPlantKey" 
            filterable 
            remote
            reserve-keyword 
            placeholder="请输入植物名称搜索"
            :remote-method="handleSearch"
            :loading="searchLoading" 
            style="width: 100%" 
            @change="handlePlantSelect"
          >
            <el-option 
              v-for="item in plantOptions" 
              :key="item.value" 
              :label="item.label"
              :value="item.value" 
            />
          </el-select>
        </el-form-item>

        <el-form-item label="养护操作">
          <el-space direction="vertical" alignment="start">
            <el-checkbox-group v-model="selectedOperations">
              <el-checkbox value="WATERING">浇水</el-checkbox>
              <el-checkbox value="FERTILIZING">施肥</el-checkbox>
              <el-checkbox value="PRUNING">修剪</el-checkbox>
              <el-checkbox value="PEST_CONTROL">除虫</el-checkbox>
            </el-checkbox-group>
          </el-space>
        </el-form-item>

        <!-- Detailed JSON input helper based on selection -->
        <el-form-item label="详细数据">
          <div v-if="selectedOperations.includes('WATERING')">
            <el-input v-model="operationDetails.waterAmount" placeholder="浇水量(ml)"
              style="width: 200px; margin-bottom: 5px;">
              <template #prepend>浇水</template>
              <template #append>ml</template>
            </el-input>
          </div>
          <div v-if="selectedOperations.includes('FERTILIZING')">
            <el-input v-model="operationDetails.fertilizerType" placeholder="肥料类型"
              style="width: 200px; margin-bottom: 5px;">
              <template #prepend>施肥</template>
            </el-input>
          </div>
        </el-form-item>

        <el-form-item label="备注" prop="remarks">
          <el-input v-model="form.remarks" type="textarea" placeholder="例如：生长状况良好" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交记录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from 'vue'
import request from '@/api/request'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const selectedOperations = ref([])
const operationDetails = reactive({
  waterAmount: '',
  fertilizerType: ''
})

const form = reactive({
  userId: userStore.userId,
  plantId: null,  // 将从选择的植物中获取
  plantSource: 'LOCAL', // 默认值
  scheduleId: null,
  operations: '',
  recordTime: '',
  remarks: '',
  selectedPlantKey: ''  // 用于 el-select 绑定
})

const rules = {
  selectedPlantKey: [{ required: true, message: '请选择植物', trigger: 'change' }]
}

// 植物搜索相关
const plantOptions = ref([])
const searchLoading = ref(false)

// 植物搜索处理函数
const handleSearch = async (query) => {
  if (query !== '') {
    searchLoading.value = true
    try {
      const res = await request.get('/my-plant', {
        params: { current: 1, size: 50, keyword: query }
      })

      const options = []

      // 处理本地植物（我的植物）
      if (res && res.records && Array.isArray(res.records)) {
        res.records.forEach(item => {
          options.push({
            value: `LOCAL_${item.id}`,
            label: `${item.nickname || '我的植物'}`,
            plantId: item.id,
            plantSource: 'LOCAL',
            details: item
          })
        })
      }

      plantOptions.value = options
    } catch (e) {
      console.error('搜索植物失败:', e)
      ElMessage.error('搜索植物失败')
    } finally {
      searchLoading.value = false
    }
  } else {
    plantOptions.value = []
  }
}

// 植物选择处理函数
const handlePlantSelect = (val) => {
  const selected = plantOptions.value.find(item => item.value === val)
  if (selected) {
    form.plantId = selected.plantId
    form.plantSource = selected.plantSource
    console.log('✅ 已选择植物:', selected.label, 'ID:', form.plantId, '来源:', form.plantSource)
  }
}

onMounted(() => {
  // Fill from query if available
  if (route.query.plantId) {
    form.plantId = Number(route.query.plantId)
    form.plantSource = route.query.plantSource || 'LOCAL'
    form.scheduleId = route.query.scheduleId ? Number(route.query.scheduleId) : null
    form.taskName = route.query.taskName
    
    // 如果有 plantId，设置 selectedPlantKey 以便显示
    if (form.plantId) {
      form.selectedPlantKey = `LOCAL_${form.plantId}`
      // 尝试加载植物信息以显示名称
      loadPlantInfo(form.plantId)
    }
  }
})

// 加载植物信息（用于回显）
const loadPlantInfo = async (plantId) => {
  try {
    const res = await request.get(`/my-plant/${plantId}`)
    if (res) {
      plantOptions.value = [{
        value: `LOCAL_${res.id}`,
        label: res.nickname || '我的植物',
        plantId: res.id,
        plantSource: 'LOCAL',
        details: res
      }]
    }
  } catch (error) {
    console.error('加载植物信息失败:', error)
  }
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      if (selectedOperations.value.length === 0) {
        ElMessage.warning('请至少选择一项养护操作')
        return
      }

      loading.value = true

      // 验证是否选择了植物
      if (!form.plantId) {
        ElMessage.warning('请先选择植物')
        loading.value = false
        return
      }

      // Construct JSON for details
      const opsObj = {}
      if (selectedOperations.value.includes('WATERING')) {
        opsObj.water = operationDetails.waterAmount ? Number(operationDetails.waterAmount) : 1
      }
      if (selectedOperations.value.includes('FERTILIZING')) {
        opsObj.fertilizer = 1 // Basic count or volume based on operationDetails.fertilizerType if we parse it
      }
      if (selectedOperations.value.includes('PRUNING')) {
        opsObj.pruning = 1
      }
      if (selectedOperations.value.includes('PEST_CONTROL')) {
        opsObj.pestControl = 1
      }

      form.operations = JSON.stringify(opsObj)

      // Setup current time for recordTime "yyyy-MM-dd HH:mm:ss"
      const now = new Date()
      const year = now.getFullYear()
      const month = String(now.getMonth() + 1).padStart(2, '0')
      const day = String(now.getDate()).padStart(2, '0')
      const hours = String(now.getHours()).padStart(2, '0')
      const minutes = String(now.getMinutes()).padStart(2, '0')
      const seconds = String(now.getSeconds()).padStart(2, '0')
      
      // 后端期望 yyyy-MM-dd HH:mm:ss 格式
      form.recordTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`

      try {
        // 确保 plantId 和 scheduleId 是数字类型
        const submitData = {
          plantId: Number(form.plantId),
          plantSource: form.plantSource || 'LOCAL',  // 默认值
          scheduleId: form.scheduleId ? Number(form.scheduleId) : null,
          operations: form.operations,
          recordTime: form.recordTime,
          remarks: form.remarks || ''  // 确保不为 undefined
        }
        
        console.log('提交养护记录:', submitData)
        const res = await request.post('/care/record/add', submitData)
        console.log('服务器响应:', res)
        
        ElMessage.success('记录添加成功')
        router.push('/care/schedule-list')
      } catch (error) {
        console.error('添加养护记录失败:', error)
        console.error('错误详情:', error)
        console.error('错误响应:', error.response?.data)
        
        // 显示更详细的错误信息
        let errorMsg = '未知错误'
        
        // 尝试从不同位置获取错误消息
        if (error.response?.data?.msg) {
          errorMsg = error.response.data.msg
        } else if (error.response?.data?.message) {
          errorMsg = error.response.data.message
        } else if (error.message) {
          errorMsg = error.message
        }
        
        ElMessage.error('添加养护记录失败：' + errorMsg)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>
