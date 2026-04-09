<template>
  <div class="register-container">
    <div class="register-left">
      <el-card class="register-card glass-panel" shadow="never">
        <div class="register-header">
          <div class="logo-circle">
            <el-icon :size="32" color="white"><Opportunity /></el-icon>
          </div>
          <h2 class="app-title">创建新账号</h2>
          <p class="app-subtitle">加入 Greenly，开始您的植物养护之旅</p>
        </div>

        <el-form
          :model="registerForm"
          :rules="rules"
          ref="registerFormRef"
          label-position="top"
          size="large"
          class="register-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="2-20 位字符，支持中文、字母和数字"
              :prefix-icon="User"
              clearable
              maxlength="20"
              show-word-limit
              class="modern-input"
            />
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="6-16 位，包含字母和数字"
              :prefix-icon="Lock"
              show-password
              maxlength="16"
              class="modern-input"
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              :prefix-icon="Lock"
              show-password
              class="modern-input"
            />
          </el-form-item>

          <el-form-item label="昵称" prop="nickname">
            <el-input
              v-model="registerForm.nickname"
              placeholder="给自己起个好听的名字（可选）"
              :prefix-icon="Avatar"
              clearable
              class="modern-input"
            />
          </el-form-item>

          <el-form-item label="邮箱" prop="email">
            <el-input
              v-model="registerForm.email"
              placeholder="example@email.com"
              :prefix-icon="Message"
              clearable
              class="modern-input"
            />
          </el-form-item>

          <el-form-item label="手机号" prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="11 位手机号码"
              :prefix-icon="Iphone"
              clearable
              maxlength="11"
              class="modern-input"
            />
          </el-form-item>

          <el-form-item style="margin-top: 24px">
            <el-button
              type="primary"
              :loading="loading"
              class="register-btn"
              @click="handleRegister"
              round
              native-type="submit"
            >
              立即注册
            </el-button>
          </el-form-item>

          <div class="register-footer">
            <span class="text-secondary">已有账号？</span>
            <router-link to="/login" class="link-primary">去登录</router-link>
          </div>
        </el-form>
      </el-card>
    </div>

    <div class="register-right">
      <div class="right-overlay">
        <div class="right-text">
          <h2>🌿 Greenly</h2>
          <p>智能植物养护管理系统</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, Iphone, Avatar, Opportunity } from '@element-plus/icons-vue'
import registerBg from '@/assets/images/register-bg.jpg'
import { register } from '@/api/user'

const bgImage = registerBg
const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePasswordStrength = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 16) {
    callback(new Error('密码长度需在 6-16 位之间'))
  } else if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d!@#$%^&*()_+\-=]+$/.test(value)) {
    callback(new Error('密码需包含字母和数字'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在 2-20 个字符之间', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9\u4e00-\u9fa5]{2,20}$/,
      message: '用户名只能包含中文、字母和数字',
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, validator: validatePasswordStrength, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await register({
          username: registerForm.username,
          password: registerForm.password,
          nickname: registerForm.nickname || registerForm.username,
          email: registerForm.email,
          phone: registerForm.phone
        })
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        console.error('注册失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.register-container {
  display: flex;
  min-height: 100vh;
  background: #f0f5f0;
}

.register-left {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  overflow-y: auto;
}

.register-card {
  width: 100%;
  max-width: 440px;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);

  :deep(.el-card__body) {
    padding: 40px;
  }
}

.register-header {
  text-align: center;
  margin-bottom: 28px;

  .logo-circle {
    width: 60px;
    height: 60px;
    background: linear-gradient(135deg, #52c41a, #389e0d);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    box-shadow: 0 4px 12px rgba(82, 196, 26, 0.4);
  }

  .app-title {
    margin: 0;
    font-size: 28px;
    font-weight: 800;
    background: linear-gradient(135deg, #52c41a, #389e0d);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .app-subtitle {
    margin: 8px 0 0;
    font-size: 14px;
    color: #999;
  }
}

.register-form {
  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #333;
  }
}

.modern-input {
  :deep(.el-input__wrapper) {
    box-shadow: none !important;
    background-color: #f5f7fa;
    border-radius: 8px;
    padding: 8px 15px;
    transition: all 0.3s;

    &.is-focus {
      background-color: #fff;
      box-shadow: 0 0 0 2px rgba(82, 196, 26, 0.2) inset !important;
    }

    &:hover:not(.is-focus) {
      background-color: #eef1f6;
    }
  }
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #52c41a, #389e0d);
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

.register-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;

  .text-secondary {
    color: #999;
  }

  .link-primary {
    color: #52c41a;
    text-decoration: none;
    font-weight: 600;
    margin-left: 4px;

    &:hover {
      text-decoration: underline;
    }
  }
}

.register-right {
  width: 45%;
  position: relative;
  background: v-bind('bgImage') center/cover no-repeat;

  .right-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(46, 125, 50, 0.7), rgba(27, 94, 32, 0.85));
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .right-text {
    text-align: center;
    color: #fff;

    h2 {
      font-size: 36px;
      font-weight: 800;
      margin: 0 0 12px;
      text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
    }

    p {
      font-size: 18px;
      opacity: 0.9;
      margin: 0;
    }
  }
}

@media (max-width: 768px) {
  .register-right {
    display: none;
  }

  .register-left {
    padding: 20px;
  }

  .register-card {
    :deep(.el-card__body) {
      padding: 24px;
    }
  }
}
</style>
