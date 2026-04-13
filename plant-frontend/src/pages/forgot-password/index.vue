<template>
  <div class="forgot-container">
    <el-card class="forgot-card glass-panel" shadow="never">
      <div class="forgot-header">
        <div class="logo-circle"><el-icon :size="32" color="white"><Key /></el-icon></div>
        <h2 class="app-title">找回密码</h2>
        <p class="app-subtitle">通过邮箱验证码重置您的密码</p>
      </div>
      <el-steps :active="currentStep" finish-status="success" class="steps" align-center>
        <el-step title="验证邮箱" />
        <el-step title="重置密码" />
        <el-step title="完成" />
      </el-steps>

      <el-form v-if="currentStep === 0" :model="step1Form" :rules="step1Rules" ref="step1FormRef" label-position="top" size="large" class="forgot-form">
        <el-form-item label="注册邮箱" prop="email">
          <el-input v-model="step1Form.email" placeholder="请输入注册时使用的邮箱" :prefix-icon="Message" clearable class="modern-input" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input-group">
            <el-input v-model="step1Form.code" placeholder="6 位验证码" maxlength="6" class="modern-input code-input" />
            <el-button type="primary" :disabled="codeCountdown > 0 || codeSending" :loading="codeSending" class="code-btn" @click="handleSendResetCode">
              {{ codeCountdown > 0 ? codeCountdown + 's' : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
        <el-form-item style="margin-top: 24px">
          <el-button type="primary" class="submit-btn" @click="handleVerifyCode" round>下一步</el-button>
        </el-form-item>
      </el-form>

      <el-form v-if="currentStep === 1" :model="step2Form" :rules="step2Rules" ref="step2FormRef" label-position="top" size="large" class="forgot-form">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="step2Form.newPassword" type="password" placeholder="6-16 位，包含字母和数字" :prefix-icon="Lock" show-password maxlength="16" class="modern-input" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="step2Form.confirmPassword" type="password" placeholder="请再次输入新密码" :prefix-icon="Lock" show-password class="modern-input" />
        </el-form-item>
        <el-form-item style="margin-top: 24px">
          <el-button type="primary" :loading="loading" class="submit-btn" @click="handleResetPassword" round>重置密码</el-button>
        </el-form-item>
      </el-form>

      <div v-if="currentStep === 2" class="success-step">
        <el-result icon="success" title="密码重置成功" sub-title="您现在可以使用新密码登录">
          <template #extra><el-button type="primary" @click="$router.push('/login')" round size="large">去登录</el-button></template>
        </el-result>
      </div>

      <div class="forgot-footer">
        <router-link to="/login" class="link-secondary"><el-icon><ArrowLeft /></el-icon> 返回登录</router-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Message, Lock, Key, ArrowLeft } from '@element-plus/icons-vue'
import { sendResetCode, resetPassword } from '@/api/user'

const router = useRouter()
const currentStep = ref(0)
const loading = ref(false)
const codeSending = ref(false)
const codeCountdown = ref(0)
let countdownTimer = null
const step1FormRef = ref(null)
const step2FormRef = ref(null)

const step1Form = reactive({ email: '', code: '' })
const step2Form = reactive({ newPassword: '', confirmPassword: '' })

const validatePasswordStrength = (rule, value, callback) => {
  if (!value) return callback(new Error('请输入密码'))
  if (value.length < 6 || value.length > 16) return callback(new Error('密码长度需在 6-16 位之间'))
  if (!/^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d!@#$%^&*()_+\-=]+$/.test(value)) return callback(new Error('密码需包含字母和数字'))
  callback()
}

const step1Rules = {
  email: [{ required: true, message: '请输入邮箱地址', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱地址', trigger: ['blur', 'change'] }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { pattern: /^\d{6}$/, message: '验证码为 6 位数字', trigger: 'blur' }]
}
const step2Rules = {
  newPassword: [{ required: true, validator: validatePasswordStrength, trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请再次输入密码', trigger: 'blur' }, { validator: (r, v, cb) => v !== step2Form.newPassword ? cb(new Error('两次密码不一致')) : cb(), trigger: 'blur' }]
}

const handleSendResetCode = async () => {
  if (!step1Form.email) return ElMessage.warning('请先输入邮箱地址')
  codeSending.value = true
  try {
    await sendResetCode({ email: step1Form.email })
    ElMessage.success('验证码已发送，请查收邮件')
    codeCountdown.value = 60
    countdownTimer = setInterval(() => { codeCountdown.value--; if (codeCountdown.value <= 0) { clearInterval(countdownTimer); countdownTimer = null } }, 1000)
  } catch (e) { console.error(e) } finally { codeSending.value = false }
}

const handleVerifyCode = async () => {
  if (!step1FormRef.value) return
  await step1FormRef.value.validate(valid => { if (valid) currentStep.value = 1 })
}

const handleResetPassword = async () => {
  if (!step2FormRef.value) return
  await step2FormRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      await resetPassword({ email: step1Form.email, code: step1Form.code, newPassword: step2Form.newPassword })
      currentStep.value = 2
    } catch (e) { console.error(e) } finally { loading.value = false }
  })
}
</script>

<style scoped lang="scss">
.forgot-container { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%); padding: 20px; }
.forgot-card { width: 100%; max-width: 480px; border: none; background: rgba(255,255,255,0.95); backdrop-filter: blur(20px); border-radius: 20px; box-shadow: 0 8px 32px rgba(0,0,0,0.08); :deep(.el-card__body) { padding: 40px; } }
.forgot-header { text-align: center; margin-bottom: 24px;
  .logo-circle { width: 60px; height: 60px; background: linear-gradient(135deg, #52c41a, #389e0d); border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; box-shadow: 0 4px 12px rgba(82,196,26,0.4); }
  .app-title { margin: 0; font-size: 26px; font-weight: 800; background: linear-gradient(135deg, #52c41a, #389e0d); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; }
  .app-subtitle { margin: 8px 0 0; font-size: 14px; color: #999; } }
.steps { margin-bottom: 28px; :deep(.el-step__title.is-success) { color: #52c41a; } :deep(.el-step__icon.is-success) { background: #52c41a; border-color: #52c41a; } }
.forgot-form { :deep(.el-form-item__label) { font-weight: 500; color: #333; } }
.modern-input { :deep(.el-input__wrapper) { box-shadow: none !important; background-color: #f5f7fa; border-radius: 8px; padding: 8px 15px; transition: all 0.3s;
  &.is-focus { background-color: #fff; box-shadow: 0 0 0 2px rgba(82,196,26,0.2) inset !important; }
  &:hover:not(.is-focus) { background-color: #eef1f6; } } }
.code-input-group { display: flex; gap: 12px; width: 100%; .code-input { flex: 1; } .code-btn { width: 130px; flex-shrink: 0; background: linear-gradient(135deg, #52c41a, #389e0d); border: none; } }
.submit-btn { width: 100%; height: 44px; font-size: 16px; font-weight: 600; letter-spacing: 2px; background: linear-gradient(135deg, #52c41a, #389e0d); border: none; box-shadow: 0 4px 14px rgba(82,196,26,0.4); transition: all 0.3s; &:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(82,196,26,0.6); } }
.success-step { padding: 20px 0; }
.forgot-footer { text-align: center; margin-top: 20px; .link-secondary { color: #999; text-decoration: none; font-size: 14px; display: inline-flex; align-items: center; gap: 4px; &:hover { color: #52c41a; } } }
</style>
