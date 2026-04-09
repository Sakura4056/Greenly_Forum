<template>
  <div class="app-container">
    <div class="ai-layout">
      <!-- 左侧历史会话列表 -->
      <aside class="session-sidebar" :class="{ collapsed: sidebarCollapsed }">
        <div class="sidebar-header">
          <h3 v-show="!sidebarCollapsed">对话历史</h3>
          <el-button text circle size="small" @click="sidebarCollapsed = !sidebarCollapsed">
            <el-icon><DArrowLeft v-if="!sidebarCollapsed" /><DArrowRight v-else /></el-icon>
          </el-button>
        </div>
        <div v-show="!sidebarCollapsed" class="session-list">
          <div
            v-for="session in sessionList"
            :key="session.sessionId"
            class="session-item"
            :class="{ active: session.sessionId === currentSessionId }"
            @click="loadSession(session.sessionId)"
          >
            <div class="session-info">
              <div class="session-preview">{{ session.lastMessage || '新对话' }}</div>
              <div class="session-meta">
                <span class="msg-count">{{ session.messageCount }}条对话</span>
                <span class="session-time">{{ formatTime(session.lastMessageTime) }}</span>
              </div>
            </div>
            <el-button type="danger" text circle size="small" @click.stop="handleDeleteSession(session.sessionId)">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
          <el-empty v-if="sessionList.length === 0 && !sessionsLoading" description="暂无对话记录" :image-size="50">
            <template #image><el-icon :size="36" color="var(--el-text-color-placeholder)"><ChatDotRound /></el-icon></template>
          </el-empty>
        </div>
        <div v-show="!sidebarCollapsed" class="sidebar-footer">
          <el-button type="primary" plain size="small" @click="newChat" style="width:100%">
            <el-icon><Plus /></el-icon> 新对话
          </el-button>
        </div>
      </aside>

      <!-- 右侧聊天主区域 -->
      <el-card shadow="never" class="ai-card">
        <template #header>
          <div class="ai-header">
            <div class="header-content">
              <el-icon :size="24" color="var(--color-primary)"><ChatDotRound /></el-icon>
              <div class="title-area">
                <h2>AI 植物顾问</h2>
                <p>您的私人植物专家，随时解答养护难题</p>
              </div>
            </div>
          </div>
        </template>

        <div class="chat-messages" ref="messagesContainer">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-wrapper"
            :class="{ 'user-message': msg.role === 'user', 'ai-message': msg.role === 'ai' }"
          >
            <div class="avatar">
              <el-avatar v-if="msg.role === 'ai'" :icon="Service" class="ai-avatar" />
              <el-avatar v-else :icon="UserFilled" class="user-avatar" />
            </div>
            <div class="message-content glass-panel">
              <p v-html="formatMessage(msg.content)"></p>
              <span class="time">{{ msg.time }}</span>
            </div>
          </div>

          <div v-if="loading" class="message-wrapper ai-message">
            <el-avatar :icon="Service" class="ai-avatar" />
            <div class="message-content glass-panel typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>

        <div class="chat-input-area">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入您的问题，例如：发财树叶子变黄了怎么办？"
            @keydown.enter.ctrl="sendMessage"
            resize="none"
            class="custom-textarea"
          />
          <div class="input-actions">
            <span class="tip">按 Ctrl + Enter 发送</span>
            <el-button size="small" @click="showApiKeyDialog = true" style="margin-right: 10px;">
              <el-icon><Key /></el-icon>
              {{ aiConfig.apiKey ? 'API Key 已配置' : '配置 API Key' }}
            </el-button>
            <el-button type="primary" @click="sendMessage" :loading="loading" round>
              发送 <el-icon class="el-icon--right"><Position /></el-icon>
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- API Key 配置对话框 -->
    <el-dialog v-model="showApiKeyDialog" title="DeepSeek API Key 配置" width="500px">
      <el-form label-width="100px">
        <el-form-item label="API Key">
          <el-input v-model="aiConfig.apiKey" placeholder="请输入您的 DeepSeek API Key" type="password" show-password />
        </el-form-item>
        <el-form-item label="接口地址">
          <el-input v-model="aiConfig.baseUrl" placeholder="https://api.deepseek.com" />
          <div style="font-size: 12px; color: #909399; margin-top: 5px;">默认使用官方地址，如有代理可修改</div>
        </el-form-item>
        <el-alert title="如何获取 API Key？" type="info" description="请访问 DeepSeek 开放平台 (https://platform.deepseek.com) 注册账号并创建 API Key" :closable="false" style="margin-top: 10px;" />
      </el-form>
      <template #footer>
        <el-button @click="showApiKeyDialog = false">取消</el-button>
        <el-button type="primary" @click="saveApiKey">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, reactive } from 'vue'
import {
  ChatDotRound, Position, Service, UserFilled, Key, Plus, Close,
  DArrowLeft, DArrowRight
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { generateAIResponse } from '@/utils/ai-service'
import { getSessionList, getConversationHistory, deleteSession } from '@/api/ai'
import request from '@/api/request'

const messagesContainer = ref(null)
const inputMessage = ref('')
const loading = ref(false)
const showApiKeyDialog = ref(false)
const sidebarCollapsed = ref(false)
const sessionsLoading = ref(false)

const currentSessionId = ref('')
const sessionList = ref([])

const aiConfig = reactive({
  provider: 'deepseek',
  model: 'deepseek-chat',
  apiKey: '',
  baseUrl: 'https://api.deepseek.com'
})

const messages = ref([
  {
    role: 'ai',
    content: '你好！我是 Greenly 的 AI 植物顾问。我可以帮你识别植物问题、提供养护建议或解答关于植物的任何疑问。请问有什么可以帮你的吗？',
    time: getCurrentTime()
  }
])

function getCurrentTime() {
  const now = new Date()
  return `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`
}

function generateSessionId() {
  return 'sess_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
}

const loadSessions = async () => {
  sessionsLoading.value = true
  try {
    const data = await getSessionList(20)
    sessionList.value = Array.isArray(data) ? data : []
  } catch (e) {
    console.error('Failed to load sessions:', e)
  } finally {
    sessionsLoading.value = false
  }
}

const loadSession = async (sessionId) => {
  if (sessionId === currentSessionId.value) return
  currentSessionId.value = sessionId
  try {
    const data = await getConversationHistory(sessionId, 100)
    if (data?.messages?.length > 0) {
      messages.value = data.messages.map(m => ({
        role: m.role === 'assistant' ? 'ai' : m.role,
        content: m.content,
        time: formatChatTime(m.createTime)
      }))
    } else {
      messages.value = [{
        role: 'ai',
        content: '你好！我是 Greenly 的 AI 植物顾问。请问有什么可以帮你的吗？',
        time: getCurrentTime()
      }]
    }
    scrollToBottom()
  } catch (e) {
    ElMessage.error('加载对话失败')
  }
}

const newChat = () => {
  currentSessionId.value = ''
  messages.value = [{
    role: 'ai',
    content: '你好！我是 Greenly 的 AI 植物顾问。请问有什么可以帮你的吗？',
    time: getCurrentTime()
  }]
}

const handleDeleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm('确定删除该对话？', '提示', { type: 'warning' })
    await deleteSession(sessionId)
    sessionList.value = sessionList.value.filter(s => s.sessionId !== sessionId)
    if (currentSessionId.value === sessionId) newChat()
    ElMessage.success('已删除')
  } catch (e) { /* cancelled */ }
}

/** Save a message to backend for history */
const saveToBackend = async (role, content) => {
  if (!aiConfig.apiKey) return
  try {
    await request({
      url: '/ai/chat',
      method: 'post',
      data: {
        message: content,
        sessionId: currentSessionId.value,
        provider: aiConfig.provider,
        model: aiConfig.model,
        apiKey: aiConfig.apiKey,
        baseUrl: aiConfig.baseUrl
      }
    })
  } catch (e) {
    // Silent fail for history saving
    console.warn('Failed to save chat to backend:', e)
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim()) return

  const userMsg = inputMessage.value.trim()

  // Generate session ID if needed
  if (!currentSessionId.value) {
    currentSessionId.value = generateSessionId()
  }

  messages.value.push({
    role: 'user',
    content: userMsg,
    time: getCurrentTime()
  })

  inputMessage.value = ''
  scrollToBottom()

  if (!aiConfig.apiKey) {
    ElMessage.warning('请先配置 DeepSeek API Key')
    return
  }

  loading.value = true

  try {
    const aiMessageIndex = messages.value.length
    messages.value.push({
      role: 'ai',
      content: '',
      time: getCurrentTime()
    })

    await generateAIResponse(
      aiConfig.provider,
      aiConfig.baseUrl,
      aiConfig.apiKey,
      aiConfig.model,
      messages.value.slice(0, aiMessageIndex),
      (chunkText) => {
        messages.value[aiMessageIndex].content = chunkText
        scrollToBottom()
      }
    )

    // Save user + AI messages to backend for history
    const aiContent = messages.value[aiMessageIndex].content
    if (aiContent) {
      saveToBackend('user', userMsg)
      saveToBackend('assistant', aiContent)
      // Refresh session list after a short delay
      setTimeout(loadSessions, 1000)
    }
  } catch (error) {
    ElMessage.error(error.message)
    if (messages.value[messages.value.length - 1].content === '') {
      messages.value.pop()
    }
  } finally {
    loading.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const formatMessage = (text) => {
  if (!text) return ''
  let formatted = text.replace(/\n/g, '<br>')
  formatted = formatted.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  return formatted
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const d = new Date(timeStr)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return `${d.getMonth() + 1}-${d.getDate()}`
}

const formatChatTime = (timeStr) => {
  if (!timeStr) return getCurrentTime()
  const d = new Date(timeStr)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const saveApiKey = () => {
  if (!aiConfig.apiKey) { ElMessage.warning('请输入 API Key'); return }
  localStorage.setItem('greenly_ai_config', JSON.stringify(aiConfig))
  ElMessage.success('API Key 已保存')
  showApiKeyDialog.value = false
}

onMounted(() => {
  const savedConfig = localStorage.getItem('greenly_ai_config')
  if (savedConfig) Object.assign(aiConfig, JSON.parse(savedConfig))
  scrollToBottom()
  loadSessions()
})
</script>

<style scoped lang="scss">
.app-container {
  min-height: calc(100vh - 120px);
  display: flex;
}

.ai-layout {
  display: flex;
  gap: 16px;
  width: 100%;
  min-height: calc(100vh - 160px);
}

/* Sidebar */
.session-sidebar {
  width: 280px;
  flex-shrink: 0;
  background: white;
  border-radius: 16px;
  border: 1px solid rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;

  &.collapsed {
    width: 48px;
    .sidebar-header h3 { display: none; }
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);

  h3 {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background-color: rgba(0,0,0,0.1); border-radius: 2px; }
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 4px;

  &:hover { background-color: #f5f7fa; }
  &.active { background-color: rgba(var(--el-color-primary-rgb), 0.08); border: 1px solid rgba(var(--el-color-primary-rgb), 0.2); }
}

.session-info { flex: 1; min-width: 0; }
.session-preview {
  font-size: 13px;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
  font-weight: 500;
}
.session-meta {
  display: flex;
  gap: 8px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}

/* Main chat card */
.ai-card {
  flex: 1;
  border-radius: 16px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.05);
  border: 1px solid rgba(255,255,255,0.5);
  background-color: rgba(255, 255, 255, 0.6);
  display: flex;
  flex-direction: column;
}

.ai-header {
  display: flex;
  align-items: center;
  padding: 16px 0;
  .header-content {
    display: flex; align-items: center; gap: 16px;
    .title-area {
      h2 { margin: 0; font-size: 18px; color: var(--color-text-main); font-weight: 600; }
      p { margin: 4px 0 0; font-size: 13px; color: var(--color-text-secondary); }
    }
  }
}

.chat-messages {
  flex: 1;
  padding: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background-color: rgba(255,255,255,0.3);
  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb { background-color: rgba(0,0,0,0.1); border-radius: 3px; }
}

.message-wrapper {
  display: flex; gap: 12px; max-width: 80%;
  &.user-message {
    align-self: flex-end; flex-direction: row-reverse;
    .message-content {
      background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-dark) 100%);
      color: white; border-bottom-right-radius: 4px;
      .time { color: rgba(255,255,255,0.8); text-align: right; }
    }
  }
  &.ai-message {
    align-self: flex-start;
    .message-content {
      background: white; color: var(--color-text-main); border-bottom-left-radius: 4px;
      .time { text-align: left; }
    }
  }
  .avatar { margin-top: 4px; flex-shrink: 0; }
  .message-content {
    padding: 12px 16px; border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.05);
    min-width: 60px; word-break: break-word;
    p { margin: 0; line-height: 1.6; font-size: 15px; }
    .time { font-size: 11px; color: var(--color-text-secondary); margin-top: 4px; display: block; }
  }
}

.chat-input-area {
  margin-top: 24px; padding: 16px 24px;
  background: white; border-radius: 12px; border: 1px solid rgba(0,0,0,0.05);
  flex-shrink: 0;
  .custom-textarea {
    :deep(.el-textarea__inner) {
      border: none; background-color: #f5f7fa; border-radius: 12px;
      padding: 12px; font-family: inherit; resize: none; box-shadow: none;
      &:focus { background-color: white; box-shadow: 0 0 0 1px var(--color-primary) inset; }
    }
  }
  .input-actions {
    display: flex; justify-content: flex-end; align-items: center;
    margin-top: 12px; gap: 16px;
    .tip { font-size: 12px; color: var(--color-text-secondary); }
  }
}

.ai-avatar { background-color: var(--color-primary-light-9); color: var(--color-primary); }
.user-avatar { background-color: #f0f2f5; color: #909399; }

.typing-indicator {
  display: flex !important; gap: 4px; padding: 16px !important; align-items: center; min-height: 20px;
  span { width: 6px; height: 6px; background-color: var(--color-text-secondary); border-radius: 50%; animation: bounce 1.4s infinite ease-in-out both; display: inline-block;
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
  }
}

@keyframes bounce { 0%, 80%, 100% { transform: scale(0); } 40% { transform: scale(1); } }

@media (max-width: 768px) {
  .ai-layout { flex-direction: column; }
  .session-sidebar { width: 100%; max-height: 200px; }
}
</style>
