<template>
  <Layout>
    <div class="chat-container">
      <!-- 智能体信息头部 -->
      <div class="chat-header">
        <div class="agent-info">
          <el-avatar :src="agent?.avatar" :size="56" class="agent-avatar">
            {{ agent?.name?.[0] || 'A' }}
          </el-avatar>
          <div class="agent-details">
            <h3>{{ agent?.name || '智能体' }}</h3>
            <p v-if="!agent" class="agent-loading">正在加载...</p>
            <p v-else class="agent-description">{{ agent.description || '暂无描述' }}</p>
          </div>
        </div>
        <div class="header-actions">
          <el-button 
            :icon="Plus" 
            @click="createSession"
            class="action-btn primary-btn"
            :loading="creatingSession"
          >
            新建会话
          </el-button>
          <el-button 
            :icon="Clock" 
            @click="sessionsDrawerVisible = true"
            class="action-btn secondary-btn"
          >
            历史记录
          </el-button>
          <el-button 
            :icon="Delete" 
            @click="clearCurrentSession"
            :disabled="!currentSessionId || messages.length === 0"
            class="action-btn secondary-btn"
          >
            清空对话
          </el-button>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="messages-container" ref="messagesContainer">
        <div v-if="loadingMessages" class="loading-messages">
          <el-skeleton :rows="3" animated />
        </div>
        
        <div v-else-if="messages.length === 0 && !loadingMessages && !currentSessionId" class="empty-chat">
          <el-icon class="empty-icon"><ChatDotSquare /></el-icon>
          <p>开始与智能体对话吧！</p>
          <div class="quick-questions" v-if="agent?.greeting">
            <div class="greeting-text">{{ agent.greeting }}</div>
          </div>
        </div>
        
        <div v-else-if="messages.length === 0 && !loadingMessages && currentSessionId" class="empty-chat">
          <el-icon class="empty-icon"><ChatDotSquare /></el-icon>
          <p>会话已创建，开始对话吧！</p>
        </div>

        <div
          v-for="(message, index) in messages"
          :key="message.id || index"
          :class="['message-item', message.role]"
        >
          <div class="message-avatar">
            <el-avatar 
              v-if="message.role === 'user'"
              :size="36"
              :src="userAvatar"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
            <el-avatar 
              v-else
              :size="36"
              :src="agent?.avatar"
            >
              {{ agent?.name?.[0] || 'A' }}
            </el-avatar>
          </div>
          <div class="message-content-wrapper">
            <div 
              class="message-content" 
              :class="{ 'risk-alert': message.msgType === 'risk_alert' }"
              v-html="message.role === 'assistant' ? formatMarkdown(message.content) : escapeHtml(message.content)"
            ></div>
            <!-- 知识库引用提示（仅助手消息且有关联知识库时显示） -->
            <div 
              v-if="message.role === 'assistant' && messageKbReferences.get(message.id)" 
              class="kb-reference-hint"
            >
              <el-icon><Document /></el-icon>
              <span>参考了知识库：{{ messageKbReferences.get(message.id).join('、') }}</span>
            </div>
            <div class="message-time">{{ formatTime(message.createdAt || message.createTime) }}</div>
          </div>
        </div>

        <div v-if="generating" class="message-item assistant thinking">
          <div class="message-avatar">
            <el-avatar :size="36" :src="agent?.avatar">
              {{ agent?.name?.[0] || 'A' }}
            </el-avatar>
          </div>
          <div class="message-content-wrapper">
            <div class="message-content">
              <div class="thinking-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 输入区域 -->
      <div class="input-container">
        <div class="input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            :placeholder="currentSessionId ? '输入消息... (Ctrl+Enter 发送)' : '请先创建会话'"
            @keydown.ctrl.enter="handleSend"
            :disabled="generating || !currentSessionId"
            class="message-input"
            resize="none"
          />
          <div class="input-actions">
            <el-button
              type="primary"
              @click="handleSend"
              :loading="generating"
              :disabled="!inputMessage.trim() || !currentSessionId"
              class="send-button"
            >
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 会话列表抽屉 -->
    <el-drawer
      v-model="sessionsDrawerVisible"
      title="会话列表"
      :size="400"
      direction="rtl"
    >
      <div class="drawer-content">
        <div class="sessions-list">
          <div
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ active: currentSessionId === session.id, pinned: session.isPinned }"
            @click="selectSession(session.id); sessionsDrawerVisible = false"
          >
            <div class="session-content">
              <div class="session-title-wrapper">
                <el-icon v-if="session.isPinned" class="pin-icon"><StarFilled /></el-icon>
                <div class="session-title" :title="session.title">{{ session.title }}</div>
              </div>
              <div class="session-time">{{ formatTime(session.updatedAt || session.updateTime) }}</div>
            </div>
            <div class="session-actions" @click.stop>
              <el-dropdown trigger="click" @command="handleSessionAction">
                <el-button text size="small" class="action-btn">
                  <el-icon><MoreFilled /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :command="{ action: 'rename', session }">重命名</el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'pin', session }">
                      {{ session.isPinned ? '取消置顶' : '置顶' }}
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'delete', session }" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </Layout>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ChatDotSquare,
  User,
  Promotion,
  Delete,
  Clock,
  Plus,
  StarFilled,
  MoreFilled,
  Document
} from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { agentApi } from '@/api/agent.js'
import { chatApi } from '@/api/chat.js'
import { useUserStore } from '@/stores/user'
import { marked } from 'marked'

// Configure marked options
marked.setOptions({
  breaks: true,
  gfm: true
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const agentId = ref(route.params.id)
const agent = ref(null)
const loading = ref(false)
const loadingMessages = ref(false)
const sessions = ref([])
const currentSessionId = ref(null)
const messages = ref([])
const inputMessage = ref('')
const generating = ref(false)
const creatingSession = ref(false)
const messagesContainer = ref(null)
const sessionsDrawerVisible = ref(false)
// 存储消息ID和知识库引用信息的映射（用于显示知识库引用提示）
const messageKbReferences = ref(new Map())

// 用户头像
const userAvatar = computed(() => {
  const userInfo = userStore.userInfo
  if (userInfo?.avatar) {
    return userInfo.avatar
  }
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${userStore.username || 'user'}`
})

// 错误码映射表
const ERROR_MESSAGES = {
  40009: '会话标题无效，长度必须在1-100个字符之间',
  40010: '智能体ID不能为空',
  40011: '会话ID不能为空',
  40012: '消息内容不能为空',
  40013: '会话标题不能为空',
  40014: '会话类型无效，只能是 normal 或 test',
  40015: '消息内容过长',
  40016: '分页参数无效',
  40017: '置顶状态参数无效，必须是0或1',
  40101: '未登录或登录已过期',
  40102: '登录令牌已过期',
  40103: '登录令牌无效',
  40303: '无权访问此会话',
  40401: '会话不存在',
  40403: '智能体不存在',
  50003: 'AI服务调用失败，请稍后重试',
  50004: '知识库检索服务失败',
  50005: '流式响应处理失败'
}

// 根据错误码获取友好的错误信息
const getErrorMessage = (error) => {
  return ERROR_MESSAGES[error.code] || error.message || '操作失败'
}

// 统一错误处理
const handleError = (error, showMessage = true) => {
  const message = getErrorMessage(error)
  if (showMessage) {
    ElMessage.error(message)
  }
  return message
}

// 加载智能体详情
const loadAgent = async () => {
  loading.value = true
  try {
    const data = await agentApi.getAgent(agentId.value)
    agent.value = data
    await loadSessions()
  } catch (error) {
    handleError(error)
    if (error.code === 40403) {
      router.push('/agents')
    }
  } finally {
    loading.value = false
  }
}

// 加载会话列表（按智能体过滤）
const loadSessions = async () => {
  try {
    const data = await chatApi.getSessions({
      agentId: agentId.value,
      page: 1,
      size: 50
    })
    sessions.value = data.list || []
    
    // 如果有会话，自动选择第一个
    if (sessions.value.length > 0 && !currentSessionId.value) {
      selectSession(sessions.value[0].id)
    }
    // 如果没有会话，不自动创建，只显示问候语页面
  } catch (error) {
    const authErrorCodes = [40101, 40102, 40103]
    if (!authErrorCodes.includes(error.code)) {
      handleError(error, false)
    }
  }
}

// 创建新会话
const createSession = async () => {
  if (creatingSession.value) return
  
  creatingSession.value = true
  try {
    const data = await chatApi.createSession({
      agentId: agentId.value,
      title: '新对话',
      sessionType: 'normal'
    })
    currentSessionId.value = data.id
    messages.value = []
    await loadSessions()
    // 加载新会话的消息（包括开场白）
    await loadMessages()
    ElMessage.success('会话创建成功')
  } catch (error) {
    handleError(error)
  } finally {
    creatingSession.value = false
  }
}

// 选择会话
const selectSession = async (sessionId) => {
  if (currentSessionId.value === sessionId) return
  currentSessionId.value = sessionId
  messages.value = []
  await loadMessages()
}

// 加载消息
const loadMessages = async () => {
  if (!currentSessionId.value) {
    messages.value = []
    return
  }
  
  loadingMessages.value = true
  try {
    const data = await chatApi.getMessages(currentSessionId.value, {
      page: 1,
      size: 100
    })
    messages.value = data.list || []
    scrollToBottom()
  } catch (error) {
    const silentErrorCodes = [40401, 40303]
    if (!silentErrorCodes.includes(error.code)) {
      ElMessage.warning(handleError(error, false))
    }
    messages.value = []
  } finally {
    loadingMessages.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || generating.value || !currentSessionId.value) return

  const content = inputMessage.value.trim()
  inputMessage.value = ''
  generating.value = true

  // 立即添加用户消息到界面（临时显示）
  const tempUserMessage = {
    id: `temp_user_${Date.now()}`,
    sessionId: currentSessionId.value,
    role: 'user',
    content,
    createdAt: new Date().toISOString()
  }
  messages.value.push(tempUserMessage)
  scrollToBottom()

  try {
    const response = await chatApi.sendMessage(currentSessionId.value, {
      content
    })
    
    // 请求成功，后端已保存消息
    generating.value = false
    
    // 如果有知识库引用信息，先保存到映射中（使用响应中的assistantMessage.id）
    // 注意：http拦截器已经返回 payload.data，所以直接访问 response.referencedKnowledgeBases
    if (response?.referencedKnowledgeBases && response.referencedKnowledgeBases.length > 0) {
      const assistantMsgId = response.assistantMessage?.id
      if (assistantMsgId) {
        console.log('[调试] 设置知识库引用:', {
          messageId: assistantMsgId,
          knowledgeBases: response.referencedKnowledgeBases
        })
        messageKbReferences.value.set(assistantMsgId, response.referencedKnowledgeBases)
      } else {
        console.warn('[调试] 响应中没有assistantMessage.id')
      }
    } else {
      console.log('[调试] 响应中没有知识库引用信息，完整响应:', response)
    }
    
    // 重新加载消息列表（确保显示最新消息）
    await loadMessages()
    
    await loadSessions()
    scrollToBottom()
  } catch (error) {
    handleError(error)
    generating.value = false
    // 错误时移除临时消息，然后重新加载
    messages.value = messages.value.filter(m => m.id !== tempUserMessage.id)
    await loadMessages()
  }
}

// 处理发送
const handleSend = () => {
  sendMessage()
}

// 清空当前会话
const clearCurrentSession = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要清空当前对话记录吗？',
      '确认清空',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    messages.value = []
    ElMessage.success('对话记录已清空')
  } catch {
    // 用户取消
  }
}

// 删除会话
const deleteSession = async (sessionId) => {
  try {
    await chatApi.deleteSession(sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      messages.value = []
      // 如果有其他会话，选择第一个
      if (sessions.value.length > 1) {
        const remainingSessions = sessions.value.filter(s => s.id !== sessionId)
        if (remainingSessions.length > 0) {
          selectSession(remainingSessions[0].id)
        }
      }
      // 如果没有其他会话，不自动创建，只显示问候语页面
    }
    await loadSessions()
    ElMessage.success('会话已删除')
  } catch (error) {
    handleError(error)
  }
}

// 处理会话操作（重命名、置顶、删除）
const handleSessionAction = async (command) => {
  const { action, session } = command
  
  if (action === 'rename') {
    const { value: newTitle } = await ElMessageBox.prompt('请输入新标题', '重命名会话', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: session.title,
      inputValidator: (value) => {
        if (!value?.trim()) return '标题不能为空'
        if (value.length > 100) return '标题长度不能超过100个字符'
        return true
      }
    }).catch(() => {})
    
    if (newTitle) {
      try {
        await chatApi.updateSessionTitle(session.id, newTitle)
        await loadSessions()
        ElMessage.success('重命名成功')
      } catch (error) {
        handleError(error)
      }
    }
  } else if (action === 'pin') {
    try {
      await chatApi.pinSession(session.id, session.isPinned ? 0 : 1)
      await loadSessions()
      ElMessage.success(session.isPinned ? '已取消置顶' : '已置顶')
    } catch (error) {
      handleError(error)
    }
  } else if (action === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除此会话吗？删除后无法恢复。', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await deleteSession(session.id)
    } catch {
      // 用户取消删除
    }
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

// 格式化 Markdown
const formatMarkdown = (content) => {
  if (!content) return ''
  try {
    return marked.parse(content)
  } catch (e) {
    console.error('Markdown parsing error:', e)
    return escapeHtml(content)
  }
}

// HTML 转义
const escapeHtml = (text) => {
  if (!text) return ''
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  if (isNaN(date.getTime())) return ''
  
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

onMounted(() => {
  loadAgent()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 64px);
  background: var(--el-bg-color-page);
  padding-top: 16px;
}

/* 头部样式 */
.chat-header {
  background: white;
  padding: 24px 24px;
  min-height: 120px;
  border-bottom: 1px solid var(--el-border-color-light);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.agent-info {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
  height: 100%;
}

.agent-avatar {
  flex-shrink: 0;
  border: 3px solid var(--el-color-primary-light-7);
  box-shadow: 0 2px 8px rgba(74, 144, 164, 0.15);
}

.agent-details {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.agent-details h3 {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.agent-details p {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.agent-loading {
  color: var(--el-text-color-placeholder);
  font-style: italic;
}

.agent-description {
  max-width: 600px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-shrink: 0;
  margin-left: 16px;
  height: 100%;
}

/* 按钮样式 */
.action-btn {
  height: 36px;
  padding: 0 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.action-btn :deep(.el-icon) {
  font-size: 16px;
}

.primary-btn {
  background: var(--app-primary-gradient);
  border: none;
  color: white;
  box-shadow: 0 2px 8px rgba(74, 144, 164, 0.25);
}

.primary-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(74, 144, 164, 0.35);
}

.primary-btn:active:not(:disabled) {
  transform: translateY(0);
}

.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.secondary-btn {
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color);
  color: var(--el-text-color-primary);
}

.secondary-btn:hover:not(:disabled) {
  background: var(--el-fill-color);
  border-color: var(--el-color-primary-light-7);
  color: var(--el-color-primary);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.secondary-btn:active:not(:disabled) {
  transform: translateY(0);
}

.secondary-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: var(--el-fill-color-extra-light);
}

/* 消息容器 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: var(--el-bg-color-page);
}

.loading-messages {
  padding: 20px;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--el-text-color-secondary);
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-chat p {
  font-size: 16px;
  margin-bottom: 24px;
}

.quick-questions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  max-width: 500px;
}

.quick-title {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin-bottom: 8px;
}

.greeting-text {
  padding: 16px 20px;
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-7);
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--el-text-color-primary);
  text-align: center;
}

/* 消息项 */
.message-item {
  display: flex;
  gap: 12px;
  animation: messageSlideIn 0.3s ease-out;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content-wrapper {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.message-item.user .message-content-wrapper {
  align-items: flex-end;
}

.message-item.assistant .message-content-wrapper {
  align-items: flex-start;
}

.message-content {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.message-item.user .message-content {
  background: var(--app-primary-gradient);
  color: #FFFFFF;
  font-weight: 500;
}

.message-item.assistant .message-content {
  background: white;
  color: var(--el-text-color-primary);
  border: 1px solid var(--el-border-color-light);
}

.message-content.risk-alert {
  background: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
  border: 1px solid var(--el-color-danger-light-5);
  font-weight: 500;
}

/* Markdown 样式 */
.message-content :deep(p) {
  margin: 0 0 8px 0;
}

.message-content :deep(p:last-child) {
  margin-bottom: 0;
}

.message-content :deep(code) {
  background: rgba(0, 0, 0, 0.05);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', monospace;
}

.message-item.user .message-content :deep(code) {
  background: rgba(255, 255, 255, 0.2);
}

.message-content :deep(pre) {
  background: var(--el-fill-color-light);
  padding: 12px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 8px 0;
}

.message-content :deep(pre code) {
  background: none;
  padding: 0;
}

.message-content :deep(ul),
.message-content :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}

.message-content :deep(li) {
  margin: 4px 0;
  line-height: 1.8;
}

.message-content :deep(blockquote) {
  border-left: 4px solid var(--el-color-primary);
  padding: 8px 16px;
  margin: 12px 0;
  background: var(--el-fill-color-extra-light);
  color: var(--el-text-color-secondary);
  border-radius: 0 4px 4px 0;
}

.message-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  padding: 4px 8px;
  margin-top: 4px;
  opacity: 0.8;
}

/* 知识库引用提示样式 */
.kb-reference-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
  padding: 4px 8px;
  opacity: 0.7;
}

.kb-reference-hint .el-icon {
  font-size: 12px;
}

.message-item.assistant .kb-reference-hint {
  color: var(--el-text-color-secondary);
}

/* 思考中动画 */
.thinking {
  opacity: 0.8;
}

.thinking-dots {
  display: flex;
  gap: 4px;
  padding: 8px 0;
}

.thinking-dots span {
  width: 8px;
  height: 8px;
  background: var(--el-text-color-placeholder);
  border-radius: 50%;
  animation: thinking 1.4s infinite;
}

.thinking-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.thinking-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes thinking {
  0%, 80%, 100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1.2);
    opacity: 1;
  }
}

/* 输入区域 */
.input-container {
  background: white;
  padding: 16px 24px;
  border-top: 1px solid var(--el-border-color-light);
  flex-shrink: 0;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.04);
}

.input-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
}

.message-input {
  flex: 1;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.send-button {
  box-shadow: 0 2px 8px rgba(74, 144, 164, 0.3);
  transition: all 0.3s ease;
}

.send-button:hover:not(:disabled) {
  box-shadow: 0 4px 12px rgba(74, 144, 164, 0.4);
  transform: translateY(-1px);
}

.send-button:active:not(:disabled) {
  transform: translateY(0);
}

/* 会话列表抽屉 */
.drawer-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--el-bg-color-page);
}

.sessions-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
}

.session-item {
  padding: 12px;
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
}

.session-item:hover {
  background: var(--el-color-primary-light-9);
}

.session-item.active {
  background: var(--el-color-primary-light-8);
  border-color: var(--el-color-primary-light-5);
}

.session-content {
  flex: 1;
  min-width: 0;
}

.session-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.pin-icon {
  color: var(--el-color-warning);
  font-size: 14px;
}

.session-title {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--el-text-color-primary);
}

.session-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.session-actions {
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .chat-header {
    padding: 16px;
    min-height: auto;
    flex-wrap: wrap;
    gap: 12px;
  }

  .agent-info {
    gap: 12px;
  }

  .agent-avatar {
    width: 48px !important;
    height: 48px !important;
  }

  .agent-details h3 {
    font-size: 18px;
  }

  .agent-description {
    max-width: 100%;
    -webkit-line-clamp: 1;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
    margin-left: 0;
    margin-top: 8px;
  }

  .action-btn {
    flex: 1;
    justify-content: center;
    min-width: 0;
  }

  .messages-container {
    padding: 16px;
  }

  .message-content-wrapper {
    max-width: 85%;
  }

  .input-container {
    padding: 12px 16px;
  }
}
</style>
