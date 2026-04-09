<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!-- 左侧：用户基本信息卡片 -->
      <el-col :xs="24" :sm="8">
        <el-card class="user-profile-card">
          <div class="profile-header">
            <div class="avatar-upload">
              <el-avatar :size="100" :src="getImageUrl(form.avatar)">
                <el-icon :size="50"><User /></el-icon>
              </el-avatar>
              <div class="avatar-overlay" @click="handleEditAvatar">
                <el-icon><Camera /></el-icon>
                <span>更换头像</span>
              </div>
            </div>
            <h3 class="nickname">{{ form.nickname || form.username }}</h3>
            <p class="username">@{{ form.username }}</p>
            <el-tag v-if="form.role === 'ADMIN'" type="danger" size="small">管理员</el-tag>
            <el-tag v-else type="success" size="small">普通用户</el-tag>
          </div>
          
          <el-divider />
          
          <div class="profile-stats">
            <div class="stat-item">
              <div class="stat-value">0</div>
              <div class="stat-label">植物</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">0</div>
              <div class="stat-label">养护记录</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">0</div>
              <div class="stat-label">天数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <!-- 右侧：编辑表单 -->
      <el-col :xs="24" :sm="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>个人信息设置</span>
              <el-button @click="handleReset" size="small">重置</el-button>
            </div>
          </template>

          <el-form 
            :model="form" 
            :rules="rules" 
            ref="formRef" 
            label-width="100px" 
            label-position="left"
          >
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled placeholder="用户名不可修改" />
              <div class="form-tip">用户名注册后不可修改</div>
            </el-form-item>
            
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="请输入昵称" clearable />
            </el-form-item>
            
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio :value="0">未知</el-radio>
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            
            <el-form-item label="生日" prop="birthday">
              <el-date-picker
                v-model="form.birthday"
                type="date"
                placeholder="选择生日"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" clearable />
            </el-form-item>
            
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" clearable maxlength="11" />
            </el-form-item>
            
            <el-form-item label="个性签名" prop="signature">
              <el-input
                v-model="form.signature"
                type="textarea"
                :rows="3"
                placeholder="介绍一下自己吧..."
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="loading" size="default">
                保存修改
              </el-button>
              <el-button @click="handleChangePassword" size="default">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form 
        :model="passwordForm" 
        :rules="passwordRules" 
        ref="passwordFormRef"
        label-width="80px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            placeholder="请输入原密码" 
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="6-16 位，包含字母和数字" 
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码" 
            show-password
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmChangePassword" :loading="passwordLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Camera, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { updateUserInfo, changePassword, getCurrentUserInfo } from '@/api/user'

const userStore = useUserStore()
const formRef = ref(null)
const passwordFormRef = ref(null)
const loading = ref(false)
const passwordLoading = ref(false)
const passwordDialogVisible = ref(false)

// 处理图片 URL：将 /uploads/xxx.jpg 转换为完整的后端 URL
const getImageUrl = (url) => {
  if (!url) return ''
  // 如果已经是完整 URL，直接返回
  if (url.startsWith('http')) return url
  // 如果已经是正确的 API 路径，转换为完整 URL
  if (url.startsWith('/api/photo/view/')) {
    return url
  }
  if (url.startsWith('/uploads/')) {
    const filename = url.replace('/uploads/', '')
    return `/api/photo/view/${filename}`
  }
  return baseUrl + url
}

const form = reactive({
  userId: userStore.userId,
  username: userStore.username,
  nickname: '',
  gender: 0,
  birthday: '',
  email: '',
  phone: '',
  signature: '',
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  nickname: [
    { max: 50, message: '昵称长度不能超过 50 个字符', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  signature: [
    { max: 200, message: '个性签名不能超过 200 个字符', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码长度在 6-16 位之间', trigger: 'blur' },
    { 
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d!@#$%^&*()_+\-=]+$/, 
      message: '密码需包含字母和数字', 
      trigger: 'blur' 
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

/**
 * 获取用户信息
 */
const fetchUserInfo = async () => {
  try {
    const res = await getCurrentUserInfo()
    console.log('获取用户信息响应:', res)
    
    // 响应拦截器已经返回了 res.data，所以直接使用 res
    const user = res.data || res
    
    if (!user) {
      throw new Error('用户信息为空')
    }
    
    form.userId = user.userId
    form.username = user.username
    form.nickname = user.nickname || ''
    form.gender = user.gender || 0
    form.birthday = user.birthday || ''
    form.email = user.email || ''
    form.phone = user.phone || ''
    form.signature = user.signature || ''
    form.avatar = user.avatar || ''
    
    console.log('用户信息加载成功:', form)
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  }
}

/**
 * 提交修改
 */
const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    loading.value = true
    
    await updateUserInfo({
      userId: form.userId,
      nickname: form.nickname,
      gender: form.gender,
      birthday: form.birthday,
      email: form.email,
      phone: form.phone,
      signature: form.signature,
      avatar: form.avatar
    })
    
    // 更新本地存储
    userStore.updateUserInfo({
      nickname: form.nickname,
      avatar: form.avatar
    })
    
    ElMessage.success('修改成功')
  } catch (error) {
    console.error('修改失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 重置表单
 */
const handleReset = () => {
  fetchUserInfo()
  ElMessage.info('已重置为原始信息')
}

/**
 * 修改密码
 */
const handleChangePassword = () => {
  passwordDialogVisible.value = true
  // 重置密码表单
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordFormRef.value?.clearValidate()
}

/**
 * 确认修改密码
 */
const handleConfirmChangePassword = async () => {
  try {
    const valid = await passwordFormRef.value.validate()
    if (!valid) return
    
    passwordLoading.value = true
    
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    ElMessage.success('密码修改成功，请重新登录')
    passwordDialogVisible.value = false
    
    // 退出登录，跳转到登录页
    setTimeout(() => {
      userStore.logout()
      window.location.href = '/login'
    }, 1000)
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

/**
 * 编辑头像（预留功能）
 */
const handleEditAvatar = () => {
  ElMessage.info('头像上传功能开发中...')
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.user-profile-card {
  text-align: center;
  
  .profile-header {
    padding: 20px 0;
    
    .avatar-upload {
      position: relative;
      display: inline-block;
      margin-bottom: 16px;
      
      &:hover {
        .avatar-overlay {
          opacity: 1;
        }
      }
      
      .avatar-overlay {
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        background: rgba(0, 0, 0, 0.5);
        border-radius: 50%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: white;
        cursor: pointer;
        opacity: 0;
        transition: opacity 0.3s;
        
        .el-icon {
          font-size: 20px;
          margin-bottom: 4px;
        }
        
        span {
          font-size: 12px;
        }
      }
    }
    
    .nickname {
      margin: 8px 0;
      font-size: 20px;
      font-weight: 600;
      color: #333;
    }
    
    .username {
      margin: 0 0 12px;
      font-size: 14px;
      color: #999;
    }
  }
  
  .profile-stats {
    display: flex;
    justify-content: space-around;
    padding: 16px 0;
    
    .stat-item {
      text-align: center;
      
      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: #52c41a;
      }
      
      .stat-label {
        margin-top: 4px;
        font-size: 12px;
        color: #999;
      }
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
