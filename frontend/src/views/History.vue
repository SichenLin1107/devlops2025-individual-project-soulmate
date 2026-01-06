<template>
  <Layout>
    <div class="history-page">
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">
            历史记录
            <el-tag :type="sessions.length > 0 ? 'success' : 'info'" size="small" class="count-tag">
              {{ total }} 条记录
            </el-tag>
          </h1>
          <p class="page-subtitle">管理您的所有聊天会话记录</p>
        </div>
        <div class="header-actions">
          <!-- 可以在这里添加操作按钮，如导出等 -->
        </div>
      </div>

      <!-- 搜索和筛选栏 -->
      <div class="filter-bar">
        <div class="filter-left">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索会话标题或智能体名称..."
            clearable
            style="width: 300px"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select
            v-model="selectedAgentId"
            placeholder="筛选智能体"
            clearable
            style="width: 200px"
            @change="handleFilter"
          >
            <el-option
              v-for="agent in agents"
              :key="agent.id"
              :label="agent.name"
              :value="agent.id"
            />
          </el-select>
        </div>
        <div class="filter-right">
          <el-button @click="handleRefresh">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
          <el-button
            type="danger"
            :disabled="selectedSessions.length === 0"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>
            批量删除 ({{ selectedSessions.length }})
          </el-button>
        </div>
      </div>

      <!-- 会话列表 -->
      <div v-loading="loading" class="sessions-container">
        <div v-if="sessions.length === 0 && !loading" class="empty-state">
          <el-icon class="empty-icon"><ChatDotRound /></el-icon>
          <p class="empty-text">还没有聊天记录</p>
          <el-button type="primary" @click="$router.push('/agents')">
            去智能体广场看看
          </el-button>
        </div>

        <div v-else class="sessions-grid">
          <div
            v-for="session in sessions"
            :key="session.id"
            :class="['session-card', { 'is-selected': selectedSessions.includes(session.id) }]"
            @click="toggleSelect(session.id)"
          >
            <div class="session-checkbox">
              <el-checkbox
                :model-value="selectedSessions.includes(session.id)"
                @click.stop
                @change="toggleSelect(session.id)"
              />
            </div>
            <div class="session-content" @click.stop="viewSession(session)">
              <div class="session-header">
                <el-avatar :src="session.agentAvatar" :size="40" class="agent-avatar">
                  <el-icon><ChatDotRound /></el-icon>
                </el-avatar>
                <div class="session-info">
                  <div class="session-title">
                    {{ session.title || '新对话' }}
                    <el-tag v-if="session.isPinned" type="warning" size="small" class="pin-tag">
                      <el-icon><Star /></el-icon>
                      置顶
                    </el-tag>
                  </div>
                  <div class="session-meta">
                    <span class="agent-name">{{ session.agentName || '未知智能体' }}</span>
                    <span class="separator">·</span>
                    <span class="message-count">{{ session.messageCount || 0 }} 条消息</span>
                  </div>
                </div>
              </div>
              <div class="session-footer">
                <span class="update-time">{{ formatTime(session.updatedAt) }}</span>
                <div class="session-actions" @click.stop>
                  <el-button
                    text
                    type="primary"
                    size="small"
                    @click="viewSession(session)"
                  >
                    <el-icon><View /></el-icon>
                    查看
                  </el-button>
                  <el-button
                    text
                    type="danger"
                    size="small"
                    @click="deleteSession(session.id)"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="loadSessions"
        @size-change="handleSizeChange"
        class="pagination"
      />

      <!-- 查看会话对话框 -->
      <el-dialog
        v-model="viewSessionVisible"
        width="800px"
        :title="currentSession?.title || '聊天记录'"
        top="10vh"
        :close-on-click-modal="false"
        class="session-dialog"
      >
        <div v-loading="messagesLoading" class="messages-container">
          <div v-if="messages.length === 0 && !messagesLoading" class="empty-messages">
            暂无消息
          </div>
          <div
            v-for="msg in messages"
            :key="msg.id"
            :class="['message-item', msg.role === 'user' ? 'user-message' : 'assistant-message']"
          >
            <el-avatar
              :src="msg.role === 'user' ? (userInfo?.avatar || defaultAvatar) : currentSession?.agentAvatar"
              :size="32"
            >
              <el-icon v-if="msg.role === 'user'"><User /></el-icon>
              <el-icon v-else><ChatDotRound /></el-icon>
            </el-avatar>
              <div class="message-content">
              <div class="message-text">{{ msg.content }}</div>
              <div class="message-time">{{ formatTime(msg.createdAt || msg.createTime) }}</div>
            </div>
          </div>
        </div>
        <template #footer>
          <el-button @click="viewSessionVisible = false">关闭</el-button>
          <el-button
            type="primary"
            @click="goToAgentDetail"
            v-if="currentSession?.agentId"
          >
            查看智能体详情
          </el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Clock,
  Search,
  Refresh,
  Delete,
  ChatDotRound,
  View,
  Star,
  User
} from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { useUserStore } from '@/stores/user'
import { chatApi } from '@/api/chat'
import { http } from '@/utils/http'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const loading = ref(false)
const sessions = ref([])
const searchKeyword = ref('')
const selectedAgentId = ref('')
const agents = ref([])
const selectedSessions = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 查看会话
const viewSessionVisible = ref(false)
const currentSession = ref(null)
const messages = ref([])
const messagesLoading = ref(false)

const defaultAvatar = computed(() => {
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${userInfo.value?.username || 'user'}`
})

// 加载智能体列表
const loadAgents = async () => {
  try {
    const result = await http.get('/v1/agents', { params: { page: 1, size: 100 } })
    agents.value = result.list || []
  } catch (e) {
    console.error('加载智能体列表失败:', e)
  }
}

// 加载会话列表
const loadSessions = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    }
    if (selectedAgentId.value) {
      params.agentId = selectedAgentId.value
    }
    const result = await chatApi.getSessions(params)
    sessions.value = result.list || []
    total.value = result.total || 0
  } catch (e) {
    ElMessage.error(e?.message || '加载历史记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    sessions.value = sessions.value.filter(s => 
      s.title?.toLowerCase().includes(keyword) || 
      s.agentName?.toLowerCase().includes(keyword)
    )
  } else {
    loadSessions()
  }
}

// 筛选
const handleFilter = () => {
  currentPage.value = 1
  loadSessions()
}

// 刷新
const handleRefresh = () => {
  searchKeyword.value = ''
  selectedAgentId.value = ''
  selectedSessions.value = []
  currentPage.value = 1
  loadSessions()
}

const toggleSelect = (sessionId) => {
  const index = selectedSessions.value.indexOf(sessionId)
  index > -1 
    ? selectedSessions.value.splice(index, 1)
    : selectedSessions.value.push(sessionId)
}

// 批量删除
const handleBatchDelete = async () => {
  if (selectedSessions.value.length === 0) return
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedSessions.value.length} 条记录吗？删除后无法恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    const deletePromises = selectedSessions.value.map(id => chatApi.deleteSession(id))
    await Promise.all(deletePromises)
    ElMessage.success('删除成功')
    selectedSessions.value = []
    await loadSessions()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message || '删除失败')
    }
  }
}

// 查看会话
const viewSession = async (session) => {
  viewSessionVisible.value = true
  currentSession.value = session
  messagesLoading.value = true
  try {
    const result = await chatApi.getMessages(session.id, { page: 1, size: 100 })
    messages.value = result.list || []
  } catch (e) {
    ElMessage.error(e?.message || '加载消息失败')
  } finally {
    messagesLoading.value = false
  }
}

const deleteSession = async (sessionId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条聊天记录吗？删除后无法恢复。', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await chatApi.deleteSession(sessionId)
    ElMessage.success('删除成功')
    const index = selectedSessions.value.indexOf(sessionId)
    if (index > -1) selectedSessions.value.splice(index, 1)
    await loadSessions()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.message || '删除失败')
  }
}

const goToAgentDetail = () => {
  if (currentSession.value?.agentId) {
    router.push(`/agents/${currentSession.value.agentId}`)
    viewSessionVisible.value = false
  }
}

const handleSizeChange = () => {
  currentPage.value = 1
  loadSessions()
}
const formatTime = (date) => {
  if (!date) return '-'
  const d = new Date(date)
  const now = new Date()
  const diff = now - d
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(() => {
  loadAgents()
  loadSessions()
})
</script>

<style scoped>
.history-page {
  padding: 32px;
  max-width: 1400px;
  margin: 0 auto;
  background: var(--el-bg-color-page);
  min-height: calc(100vh - 60px);
}

/* 页面头部 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0;
}

.header-left {
  flex: 1;
}

.page-title {
  font-size: 26px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.count-tag {
  font-weight: 500;
}

.page-subtitle {
  color: var(--el-text-color-regular);
  font-size: 14px;
  margin: 0;
  line-height: 1.6;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 筛选栏 */
.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: var(--app-card-bg);
  border-radius: 12px;
  box-shadow: var(--el-box-shadow-light);
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-right {
  display: flex;
  gap: 12px;
}

/* 会话容器 */
.sessions-container {
  min-height: 400px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: var(--app-card-bg);
  border-radius: 12px;
  box-shadow: var(--el-box-shadow-light);
}

.empty-icon {
  font-size: 64px;
  color: var(--el-text-color-placeholder);
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  color: var(--el-text-color-secondary);
  margin: 0 0 24px 0;
}

/* 会话网格 */
.sessions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

@media (min-width: 1200px) {
  .sessions-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (min-width: 1600px) {
  .sessions-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}

.session-card {
  position: relative;
  background: var(--app-card-bg);
  border-radius: 12px;
  padding: 12px;
  border: 2px solid var(--el-border-color-lighter);
  transition: all 0.3s ease;
  cursor: pointer;
}

.session-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: var(--el-box-shadow);
  transform: translateY(-2px);
}

.session-card.is-selected {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.session-checkbox {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
}

.session-content {
  padding-right: 32px;
}

.session-header {
  display: flex;
  gap: 10px;
  margin-bottom: 10px;
}

.agent-avatar {
  flex-shrink: 0;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pin-tag {
  flex-shrink: 0;
}

.session-meta {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.separator {
  color: var(--el-text-color-placeholder);
}

.session-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid var(--el-border-color-lighter);
  margin-top: 8px;
}

.update-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.session-actions {
  display: flex;
  gap: 8px;
}

/* 消息容器 */
.messages-container {
  max-height: calc(80vh - 200px);
  min-height: 300px;
  overflow-y: auto;
  padding: 16px;
}

/* 会话对话框样式 */
:deep(.session-dialog) {
  .el-dialog {
    max-height: 85vh;
    display: flex;
    flex-direction: column;
  }
  
  .el-dialog__body {
    flex: 1;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    padding: 0;
  }
  
  .el-dialog__footer {
    flex-shrink: 0;
    padding: 16px 20px;
    border-top: 1px solid var(--el-border-color-lighter);
  }
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.user-message {
  flex-direction: row-reverse;
}

.message-content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-message .message-content {
  align-items: flex-end;
}

.message-text {
  background: var(--el-fill-color-light);
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-wrap: break-word;
}

.user-message .message-text {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.message-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
  padding: 2px 0;
  opacity: 0.8;
}

.empty-messages {
  text-align: center;
  padding: 40px;
  color: var(--el-text-color-secondary);
}

/* 分页 */
.pagination {
  margin-top: 24px;
  justify-content: center;
}

/* 响应式 */
@media (max-width: 768px) {
  .sessions-grid {
    grid-template-columns: 1fr;
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-left,
  .filter-right {
    width: 100%;
  }

  .filter-left {
    flex-direction: column;
  }
  
  /* 移动端弹窗适配 */
  :deep(.session-dialog) {
    .el-dialog {
      width: 95% !important;
      max-height: 90vh;
      margin: 5vh auto;
    }
    
    .messages-container {
      max-height: calc(90vh - 180px);
      min-height: 200px;
    }
  }
}
</style>

