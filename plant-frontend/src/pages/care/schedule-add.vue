<template>
    <div class="app-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span>{{ isEditMode ? '编辑养护计划' : '新建养护计划' }}</span>
                </div>
            </template>

            <el-form :model="form" :rules="rules" ref="formRef" label-width="120px" style="max-width: 600px">
                <el-form-item label="添加模式">
                    <el-radio-group v-model="form.isNewPlant" :disabled="isEditMode">
                        <el-radio :value="false">选择现有</el-radio>
                        <el-radio :value="true">创建新植物</el-radio>
                    </el-radio-group>
                </el-form-item>

                <template v-if="!form.isNewPlant">
                    <el-form-item label="搜索植物" prop="selectedPlantKey" required>
                        <el-select v-model="form.selectedPlantKey" :disabled="isEditMode" filterable remote
                            reserve-keyword placeholder="请输入植物名称搜索 (官方/自定义)" :remote-method="handleSearch"
                            :loading="searchLoading" style="width: 100%" @change="handlePlantSelect">
                            <!-- We bind visible value to a temp var or just handle change manually, 
                         but element-plus select v-model binds to value. 
                         Our value is distinct string 'LOCAL_1'. 
                         Wait, form.plantId expects Long. 
                         The select value must be unique string. 
                         Let's bind select to a separate model 'selectedPlantKey' and update form on change. -->
                            <el-option v-for="item in plantOptions" :key="item.value" :label="item.label"
                                :value="item.value" />
                        </el-select>
                        <!-- Hidden inputs for validation binding if needed, or we adjust rules to check form.plantId -->
                    </el-form-item>
                </template>

                <template v-else>
                    <el-form-item label="植物名称" prop="plantName">
                        <el-input v-model="form.plantName" placeholder="例如：我的发财树" />
                    </el-form-item>
                    <el-form-item label="科/属" style="margin-bottom: 0">
                        <el-col :span="11">
                            <el-form-item prop="genus">
                                <el-input v-model="form.genus" placeholder="科" />
                            </el-form-item>
                        </el-col>
                        <el-col :span="2" class="text-center">-</el-col>
                        <el-col :span="11">
                            <el-form-item prop="species">
                                <el-input v-model="form.species" placeholder="属" />
                            </el-form-item>
                        </el-col>
                    </el-form-item>
                    <el-form-item label="描述" prop="description" style="margin-top: 18px">
                        <el-input v-model="form.description" type="textarea" placeholder="备注信息" />
                    </el-form-item>
                </template>

                <el-form-item label="任务类型">
                    <el-radio-group v-model="form.taskType">
                        <el-radio-button v-for="type in taskTypeOptions" :key="type" :value="type">{{ type
                        }}</el-radio-button>
                    </el-radio-group>
                </el-form-item>

                <el-form-item label="任务名称" prop="taskName">
                    <el-input v-model="form.taskName" placeholder="例如：浇水、施肥（可自动生成）" />
                </el-form-item>

                <el-form-item label="计划时间" prop="dueTime">
                    <el-date-picker v-model="form.dueTime" type="datetime" placeholder="选择日期时间"
                        value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
                </el-form-item>

                <el-form-item label="重复设置">
                    <el-select v-model="form.recurrenceType" placeholder="选择重复规则" style="width: 120px; margin-right: 10px;">
                        <el-option label="不重复" value="NONE" />
                        <el-option label="按天重复" value="DAY" />
                        <el-option label="按月重复" value="MONTH" />
                        <el-option label="按年重复" value="YEAR" />
                    </el-select>
                    <template v-if="form.recurrenceType !== 'NONE'">
                        <span> 每 </span>
                        <el-input-number v-model="form.recurrenceInterval" :min="1" :max="365" :step="1" style="width: 100px; margin: 0 10px;" />
                        <span> {{ form.recurrenceType === 'DAY' ? '天' : form.recurrenceType === 'MONTH' ? '月' : '年' }}执行一次</span>
                    </template>
                </el-form-item>

                <!-- Simplified Reminder config -->

                <el-form-item>
                    <el-button type="primary" @click="handleSubmit" :loading="loading">{{ isEditMode ? '更新计划' : '创建计划'
                    }}</el-button>
                    <el-button @click="$router.back()">取消</el-button>
                </el-form-item>
            </el-form>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import request from '@/api/request'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { updateSchedule } from '@/api/care'
import { addMyPlant } from '@/api/my-plant'  // ✅ 导入创建植物接口

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const isEditMode = ref(false)
const editId = ref(null)

const taskTypeOptions = ['浇水', '施肥', '修剪', '换盆', '除虫', '其他']

const form = reactive({
    userId: userStore.userId,
    plantId: '',
    plantSource: 'OFFICIAL',
    taskType: '', // Added taskType
    taskName: '',
    dueTime: '',
    recurrenceType: 'NONE',
    recurrenceInterval: 0,
    reminderConfig: '{}',
    // Quick Add flags
    isNewPlant: false,
    plantName: '',
    genus: '',
    species: '',
    description: '',
    selectedPlantKey: ''
})

const rules = computed(() => {
    const baseRules = {
        taskName: [{ required: true, message: '请输入或生成任务名称', trigger: 'blur' }],
        dueTime: [{ required: true, message: '请选择时间', trigger: 'change' }]
    }
    if (form.isNewPlant) {
        return {
            ...baseRules,
            plantName: [{ required: true, message: '请输入植物名称', trigger: 'blur' }]
        }
    } else {
        return {
            ...baseRules,
            selectedPlantKey: [{ required: true, message: '请选择植物', trigger: 'change' }]
        }
    }
})

// Search Logic
const plantOptions = ref([])
const searchLoading = ref(false)

// Watchers for Auto-Name Generation
// We want to generate "TaskType - PlantName"
// 1. Get current Plant Name
const currentPlantName = computed(() => {
    if (form.isNewPlant) {
        return form.plantName
    } else {
        const selected = plantOptions.value.find(item => item.value === form.selectedPlantKey)
        // If we are in edit mode and didn't load options, we might miss the name,
        // but for now let's rely on what we have.
        // If selected is found, use label (stripped of suffix) or just the name from details
        if (selected && selected.details) return selected.details.name
        return ''
    }
})

// 2. Watch dependencies
watch([() => form.taskType, currentPlantName], ([newType, newPlantName]) => {
    if (newType && newPlantName) {
        // Only auto-fill if taskName is empty OR it matches a pattern we previously generated?
        // Simple approach: Auto-fill if user hasn't manually radically changed it, 
        // or just overwrite if it looks like a generated name.
        // Let's just Overwrite for convenience, or check if empty.
        // Better: Overwrite. The user can edit AFTER selecting type.
        form.taskName = `${newType} - ${newPlantName}`
    } else if (newType) {
        form.taskName = `${newType}`
    }
})


const handleSearch = async (query) => {
    if (query !== '') {
        searchLoading.value = true
        try {
            const [officialRes, localRes] = await Promise.all([
                request.get(`/plant/official/query`, {
                    params: { keyword: query, pageSize: 20 }
                }),
                request.get('/my-plant', {
                    params: { current: 1, size: 100, keyword: query }
                })
            ])

            const options = []

            // 处理本地植物（我的植物）
            if (localRes && localRes.records && Array.isArray(localRes.records)) {
                localRes.records.forEach(item => {
                    options.push({
                        value: `LOCAL_${item.id}`,
                        label: `${item.nickname || '我的植物'} (自定义)`,
                        plantId: item.id,
                        plantSource: 'LOCAL',
                        details: item
                    })
                })
            }

            // 处理官方植物
            if (officialRes && officialRes.records) {
                officialRes.records.forEach(item => {
                    options.push({
                        value: `OFFICIAL_${item.id}`,
                        label: `${item.name} (官方)`,
                        plantId: item.id,
                        plantSource: 'OFFICIAL',
                        details: item
                    })
                })
            }

            plantOptions.value = options
        } catch (e) {
            console.error('搜索植物失败:', e)
        } finally {
            searchLoading.value = false
        }
    } else {
        plantOptions.value = []
    }
}

const handlePlantSelect = (val) => {
    const selected = plantOptions.value.find(item => item.value === val)
    if (selected) {
        form.plantId = selected.plantId
        form.plantSource = selected.plantSource
    }
}

const handleSubmit = () => {
    formRef.value.validate(async (valid) => {
        if (valid) {
            loading.value = true
            try {
                if (isEditMode.value) {
                    // === 编辑模式 ===
                    const updateParams = {
                        id: editId.value,
                        taskName: form.taskName,
                        dueTime: form.dueTime,
                        recurrenceType: form.recurrenceType,
                        recurrenceInterval: form.recurrenceInterval,
                        reminderConfig: form.reminderConfig
                    }
                    await updateSchedule(editId.value, updateParams)
                    ElMessage.success('更新成功')
                } else {
                    // === 新增模式 ===
                    let finalPlantId = form.plantId
                    let finalPlantSource = form.plantSource
                    
                    if (form.isNewPlant) {
                        // ✅ 新建植物模式：先创建植物记录
                        console.log('🌱 开始创建新植物...')
                        
                        const plantData = {
                            nickname: form.plantName,
                            location: '其他',  // 位置：阳台/客厅/卧室/办公桌
                            source: '购买',    // 来源：购买/赠送/扦插/野外
                            acquiredDate: new Date().toISOString().split('T')[0],  // 今天
                            status: 'HEALTHY',  // 健康状态：HEALTHY/SICK/DEAD/GIFTED
                            notes: form.description || ''  // 备注信息
                        }
                        
                        console.log('创建植物参数:', plantData)
                        const plantRes = await addMyPlant(plantData)
                        
                        console.log('🔍 调试信息:')
                        console.log('  - plantRes:', plantRes)
                        console.log('  - typeof plantRes:', typeof plantRes)
                        console.log('  - plantRes === undefined:', plantRes === undefined)
                        console.log('  - plantRes === null:', plantRes === null)
                        
                        // ✅ 获取新建植物的 ID（拦截器已返回 res.data，即直接是 ID 数字）
                        if (plantRes === undefined || plantRes === null) {
                            throw new Error('创建植物失败：未返回植物 ID')
                        }
                        
                        finalPlantId = plantRes  // plantRes 就是数字 ID，如 8
                        finalPlantSource = 'LOCAL'
                        
                        console.log('✅ 植物创建成功，ID:', finalPlantId, '类型:', typeof finalPlantId)
                        ElMessage.success(`植物「${form.plantName}」创建成功`)
                    } else {
                        // ✅ 选择现有植物模式：解析 selectedPlantKey
                        if (!form.selectedPlantKey) {
                            throw new Error('请选择植物')
                        }
                        
                        // 解析 selectedPlantKey (格式："SOURCE_ID")
                        const [source, id] = form.selectedPlantKey.split('_')
                        finalPlantSource = source
                        finalPlantId = Number(id)
                        
                        console.log('✅ 使用现有植物，ID:', finalPlantId, '来源:', finalPlantSource)
                    }
                    
                    // === 创建养护计划 ===
                    console.log('📅 开始创建养护计划...')
                    console.log('finalPlantId:', finalPlantId, '类型:', typeof finalPlantId)
                    console.log('finalPlantSource:', finalPlantSource)
                    
                    // 获取植物名称用于后端拼接任务名称
                    let plantNameForSchedule = ''
                    if (form.isNewPlant) {
                        plantNameForSchedule = form.plantName
                    } else {
                        const selected = plantOptions.value.find(item => item.value === form.selectedPlantKey)
                        if (selected && selected.details) {
                            plantNameForSchedule = selected.details.name || selected.details.nickname || ''
                        }
                    }
                    
                    // ✅ 格式化日期时间：确保是 "yyyy-MM-dd HH:mm:ss" 格式
                    let formattedDueTime = form.dueTime
                    if (formattedDueTime && typeof formattedDueTime === 'string') {
                        // 如果是 ISO 格式（包含 T），转换为空格分隔格式
                        if (formattedDueTime.includes('T')) {
                            formattedDueTime = formattedDueTime.replace('T', ' ')
                        }
                    } else if (formattedDueTime instanceof Date) {
                        // 如果是 Date 对象，转换为字符串
                        const year = formattedDueTime.getFullYear()
                        const month = String(formattedDueTime.getMonth() + 1).padStart(2, '0')
                        const day = String(formattedDueTime.getDate()).padStart(2, '0')
                        const hours = String(formattedDueTime.getHours()).padStart(2, '0')
                        const minutes = String(formattedDueTime.getMinutes()).padStart(2, '0')
                        const seconds = String(formattedDueTime.getSeconds()).padStart(2, '0')
                        formattedDueTime = `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
                    }
                    
                    const scheduleParams = {
                        userId: userStore.userId,
                        plantId: finalPlantId,
                        plantSource: finalPlantSource,
                        plantName: plantNameForSchedule,  // 添加plantName供后端拼接任务名称
                        taskName: form.taskName,
                        dueTime: formattedDueTime,  // 使用格式化后的日期
                        recurrenceType: form.recurrenceType,
                        recurrenceInterval: form.recurrenceInterval,
                        reminderConfig: form.reminderConfig
                    }
                    
                    console.log('养护计划参数:', scheduleParams)
                    
                    // 验证必填字段
                    if (!scheduleParams.plantId || scheduleParams.plantId === 0) {
                        throw new Error('植物 ID 无效，请重试')
                    }
                    if (!scheduleParams.plantSource) {
                        throw new Error('植物来源不能为空')
                    }
                    
                    await request.post('/care/schedule/add', scheduleParams)
                    
                    ElMessage.success('养护计划创建成功')
                }
                
                // 跳转到列表页
                router.push('/care/schedule-list')
            } catch (error) {
                console.error('❌ 操作失败:', error)
                ElMessage.error(error.message || '操作失败，请重试')
            } finally {
                loading.value = false
            }
        }
    })
}

onMounted(() => {
    if (route.query.id) {
        isEditMode.value = true
        editId.value = route.query.id
        form.taskName = route.query.taskName
        
        // ✅ 格式化日期：确保是 "yyyy-MM-dd HH:mm:ss" 格式
        let dueTimeValue = route.query.dueTime
        if (dueTimeValue && typeof dueTimeValue === 'string' && dueTimeValue.includes('T')) {
            // ISO 格式转换为空格分隔格式
            dueTimeValue = dueTimeValue.replace('T', ' ')
        }
        form.dueTime = dueTimeValue
        
        if (route.query.recurrenceType) {
            form.recurrenceType = route.query.recurrenceType
        }
        if (route.query.recurrenceInterval) {
            form.recurrenceInterval = Number(route.query.recurrenceInterval) || 1
        }

        // In edit mode for now, we don't try to reverse-engineer the Task Type
        form.selectedPlantKey = 'CURRENT_PLANT'
    }
})
</script>
