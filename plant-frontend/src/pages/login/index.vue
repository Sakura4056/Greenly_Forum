<template>
  <div class="login-container">
    <div class="login-content">
      <el-card class="login-card glass-panel">
        <template #header>
          <div class="login-header">
            <div class="logo-circle">
              <el-icon :size="32" color="white"><opportunity /></el-icon>
            </div>
            <h2 class="app-title text-gradient">Greenly</h2>
            <p class="app-subtitle">植物养护管理系统</p>
          </div>
        </template>
        
        <el-form 
          :model="loginForm" 
          :rules="rules" 
          ref="loginFormRef" 
          label-position="top" 
          size="large"
          @keyup.enter="handleLogin"
        >
          <el-form-item label="账号" prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名" 
              :prefix-icon="User" 
              class="modern-input"
              clearable
            />
          </el-form-item>
          
          <el-form-item label="密码" prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码" 
              :prefix-icon="Lock" 
              show-password 
              class="modern-input"
            />
          </el-form-item>
          
          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            </div>
          </el-form-item>
          
          <el-form-item style="margin-top: 32px">
            <el-button 
              type="primary" 
              :loading="loading" 
              class="login-btn" 
              @click="handleLogin" 
              round
              native-type="submit"
            >
              登 录
            </el-button>
          </el-form-item>
          
          <div class="login-footer">
            <span class="text-secondary">还没有账号？</span>
            <router-link to="/register" class="link-primary">立即注册</router-link>
          </div>
          

        </el-form>
      </el-card>
    </div>

    <!-- 系统公告弹窗 -->
    <el-dialog
      v-model="announcementDialogVisible"
      :title="unreadAnnouncement?.title || '系统公告'"
      width="600px"
      class="announcement-dialog"
      :close-on-click-modal="false"
    >
      <div class="announcement-content">
        <div class="announcement-meta">
          <time :datetime="unreadAnnouncement?.createTime">
            发布时间: {{ formatTime(unreadAnnouncement?.createTime) }}
          </time>
        </div>
        <div class="announcement-text" v-html="unreadAnnouncement?.content"></div>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleAnnouncementConfirm">
          我知道了
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Opportunity } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/user'
import { getUnread, markRead as markReadAPI } from '@/api/reminder'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

// 系统公告相关
const announcementDialogVisible = ref(false)
const unreadAnnouncement = ref(null)

/**
 * 格式化时间
 */
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  if (Array.isArray(timeStr)) {
    return `${timeStr[0]}-${String(timeStr[1]).padStart(2, '0')}-${String(timeStr[2]).padStart(2, '0')} ${String(timeStr[3]).padStart(2, '0')}:${String(timeStr[4]).padStart(2, '0')}`
  }
  return timeStr.replace('T', ' ').substring(0, 16)
}

/**
 * 检查并显示未读公告
 */
const checkAndShowAnnouncements = async () => {
  try {
    const res = await getUnread(userStore.userId)
    if (res && res.details && res.details.announcement && res.details.announcement.length > 0) {
      // 获取最新的未读公告
      const announcements = res.details.announcement.filter(a => a.isRead === 0)
      if (announcements.length > 0) {
        announcements.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
        unreadAnnouncement.value = announcements[0]
        announcementDialogVisible.value = true
      }
    }
  } catch (error) {
    console.error('检查公告失败:', error)
  }
}

/**
 * 处理公告确认
 */
const handleAnnouncementConfirm = async () => {
  if (unreadAnnouncement.value && unreadAnnouncement.value.id) {
    try {
      await markReadAPI(unreadAnnouncement.value.id)
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
  announcementDialogVisible.value = false
}

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2-20 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码长度在 6-16 个字符之间', trigger: 'blur' }
  ]
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  try {
    const valid = await loginFormRef.value.validate()
    if (!valid) {
      console.error('表单验证失败')
      return
    }
    
    loading.value = true
    console.log('开始登录...', loginForm)
    const res = await login(loginForm)
    console.log('登录响应:', res)
    
    // 保存登录信息
    userStore.setLoginInfo(res)
    console.log('登录信息已保存，token:', userStore.token)
    
    // 是否记住我（延长 token 有效期）
    if (rememberMe.value) {
      localStorage.setItem('rememberMe', 'true')
    }
    
    ElMessage.success('登录成功')
    
    // 检查是否有未读公告
    await checkAndShowAnnouncements()
    
    // 跳转到首页或之前访问的页面
    const redirect = route.query.redirect || '/'
    console.log('准备跳转到:', redirect)
    
    try {
      await router.push(redirect)
      console.log('跳转成功')
    } catch (routerError) {
      console.error('路由跳转失败:', routerError)
      ElMessage.error('跳转失败，请手动刷新页面')
    }
  } catch (error) {
    console.error('登录失败:', error)
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

/**
 * 快速登录（测试用）
 */
const quickLogin = (username, password) => {
  loginForm.username = username
  loginForm.password = password
  handleLogin()
}
</script>

<style scoped lang="scss">
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
    animation: rotate 30s linear infinite;
  }
}

@keyframes rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.login-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 20px;
}

.login-card {
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
  border-radius: 20px;
  overflow: hidden;
  
  :deep(.el-card__header) {
    border-bottom: none;
    padding-bottom: 0;
    background: transparent;
  }
  
  :deep(.el-card__body) {
    padding: 30px 40px 40px;
  }
}

.login-header {
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  
  .logo-circle {
    width: 64px;
    height: 64px;
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
    box-shadow: 0 4px 12px rgba(82, 196, 26, 0.4);
  }
  
  .app-title {
    margin: 0;
    font-size: 32px;
    font-weight: 800;
    letter-spacing: 1px;
    background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
  
  .app-subtitle {
    margin: 8px 0 0;
    font-size: 14px;
    color: #666;
    font-weight: 500;
  }
}

.modern-input {
  :deep(.el-input__wrapper) {
    box-shadow: none;
    background-color: #f5f7fa;
    border-radius: 8px;
    transition: all 0.3s;
    padding: 10px 15px;
    
    &.is-focus {
      background-color: #fff;
      box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.2) inset;
    }
    
    &:hover:not(.is-focus) {
      background-color: #eef1f6;
    }
  }
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(82, 196, 26, 0.4);
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(82, 196, 26, 0.6);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
  
  .text-secondary {
    color: #666;
  }
  
  .link-primary {
    color: #52c41a;
    text-decoration: none;
    font-weight: 600;
    margin-left: 5px;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

.el-divider {
  margin: 24px 0 16px;
  font-size: 12px;
  color: #999;
}

.quick-login {
  display: flex;
  gap: 12px;
  justify-content: center;
  
  .el-button {
    flex: 1;
  }
}

/* 公告弹窗样式 */
.announcement-dialog {
  :deep(.el-dialog__body) {
    padding: 24px;
    max-height: 60vh;
    overflow-y: auto;
  }
  
  .announcement-content {
    .announcement-meta {
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid #e4e7ed;
      
      time {
        font-size: 14px;
        color: #909399;
      }
    }
    
    .announcement-text {
      font-size: 15px;
      line-height: 1.8;
      color: #303133;
      white-space: pre-wrap;
      word-wrap: break-word;
    }
  }
}
</style>
