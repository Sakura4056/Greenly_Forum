<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>提醒推送配置</span>
        </div>
      </template>

      <el-form :model="form" label-width="120px" style="max-width: 500px">
        <el-form-item label="接收邮箱">
          <el-input 
            v-model="form.email" 
            disabled 
            placeholder="接收通知的邮箱"
          >
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
          <div style="margin-top: 8px; font-size: 12px; color: #909399; line-height: 1.5;">
            <el-icon style="vertical-align: middle;"><InfoFilled /></el-icon>
            邮箱地址来自个人资料，如需修改请前往
            <router-link to="/user/update" style="color: #409eff; text-decoration: none;">个人中心</router-link>
          </div>
        </el-form-item>
        <el-form-item label="接收手机">
          <el-input v-model="form.phone" placeholder="接收通知的手机号" />
        </el-form-item>

        <el-form-item label="推送渠道">
          <el-checkbox v-model="form.channels.popup">网页弹窗</el-checkbox>
          <el-checkbox v-model="form.channels.email">邮件通知</el-checkbox>
        </el-form-item>

        <el-form-item label="打扰模式">
          <el-switch v-model="form.doNotDisturb" active-text="免打扰 (22:00-08:00)" />
        </el-form-item>

        <el-form-item label="每日汇总时间">
          <el-time-select
            v-model="form.summaryTime"
            start="00:00"
            step="01:00"
            end="23:00"
            placeholder="选择发送时间"
          />
          <span style="margin-left: 10px; color: #888; font-size: 12px;">每日将在此时发送邮件汇总提醒</span>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="loading">保存配置</el-button>
          <el-button type="success" plain @click="handleTestEmail" :loading="testLoading" style="margin-left: 10px;">
            测试发送日报邮件
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { updateConfig, getConfig } from '@/api/reminder' // Use API module
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Message, InfoFilled } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false)
const testLoading = ref(false)

const form = reactive({
  email: '',
  phone: '', // Keep phone in UI but maybe disable/hide if SMS is gone? User authorized removing SMS function.
  // Actually backend still has `phone` field in ReminderConfig entity, just SMS service removed.
  // But for UI clarity, I should probably hide "SMS Channel" checkbox.
  channels: {
    popup: true,
    email: false
    // sms removed
  },
  doNotDisturb: false,
  summaryTime: '09:00'
})

const handleSave = async () => {
  loading.value = true
  try {
    const payload = {
      // 邮箱地址从 sys_user 表读取，无需提交
      phone: form.phone,
      popupEnabled: form.channels.popup ? 1 : 0,
      bellEnabled: 1,
      sceneConfig: JSON.stringify({
        doNotDisturb: form.doNotDisturb,
        emailEnabled: form.channels.email,
        summaryTime: form.summaryTime
      })
    }

    console.log('保存提醒配置:', payload)
    await updateConfig(payload)
    ElMessage.success('配置已更新')
  } catch (e) {
    console.error('保存配置失败:', e)
    ElMessage.error('保存失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleTestEmail = async () => {
  testLoading.value = true
  try {
    await request({
      url: '/reminder/test-email',
      method: 'post'
    })
    ElMessage.success('测试邮件触发指令已发送！(需后台存在今天到期的养护计划才会发送邮件)')
  } catch (e) {
    console.error(e)
    ElMessage.error('触发失败，可能是后台未重启或未配置。')
  } finally {
    testLoading.value = false
  }
}

onMounted(async () => {
  console.log('📧 [提醒配置] 页面加载，开始初始化...')
  
  // 确保用户信息已加载
  if (!userStore.isLoggedIn) {
    console.warn('[提醒配置] 用户未登录')
    ElMessage.warning('请先登录')
    return
  }
  
  // 始终刷新用户信息，确保获取最新的邮箱地址
  console.log('[提醒配置] 刷新用户信息以获取最新邮箱...')
  try {
    await userStore.fetchUserInfo()
    console.log('[提醒配置] 用户信息刷新成功:', userStore.userInfo)
  } catch (error) {
    console.error('[提醒配置] 获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败，请刷新页面重试')
    return
  }
  
  // 从用户 Store 获取邮箱（唯一数据源）
  if (userStore.userInfo && userStore.userInfo.email) {
    form.email = userStore.userInfo.email
    console.log('✅ [提醒配置] 邮箱地址已设置:', form.email)
  } else {
    console.warn('⚠️ [提醒配置] 用户邮箱未设置，请前往个人资料页面设置')
    ElMessage.warning('您尚未设置邮箱地址，请前往个人中心完善信息')
  }
  
  // 加载提醒配置
  try {
    const res = await getConfig()
    // request.js interceptor directly returns data of the Result object
    // so `res` IS the ReminderConfig object, not `{ code, data }`
    if (res && res.id !== undefined) {
      console.log('[提醒配置] 配置加载成功:', res)
      // 邮箱不从配置中读取，始终使用用户个人信息中的邮箱
      // form.email 已在上面从 userStore 设置
      form.phone = res.phone || ''
      form.channels.popup = res.popupEnabled === 1
      
      if (res.sceneConfig && res.sceneConfig !== '{}') {
        try {
          const sc = JSON.parse(res.sceneConfig)
          form.doNotDisturb = !!sc.doNotDisturb
          form.channels.email = !!sc.emailEnabled
          if (sc.summaryTime) {
            form.summaryTime = sc.summaryTime
          }
        } catch (e) {
          console.error('[提醒配置] 解析场景配置失败:', e)
        }
      }
    } else {
      console.log('[提醒配置] 暂无配置，使用默认值')
    }
  } catch (e) {
    console.error('[提醒配置] 获取配置失败:', e)
    ElMessage.error('获取配置失败，请刷新页面重试')
  }
})
</script>
