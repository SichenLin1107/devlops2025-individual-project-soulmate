<template>
  <Layout>
    <div class="agent-detail-page" v-loading="loading">
      <!-- 页面头部 -->
      <div class="page-header-container">
        <el-button 
          @click="handleBack" 
          :icon="ArrowLeft" 
          text
          type="primary"
          class="back-button"
        >
          返回
        </el-button>
        <div class="header-content">
          <h1 class="agent-title">{{ agentForm.id ? (agentForm.name || '智能体详情') : '新建智能体' }}</h1>
          <span class="agent-subtitle">智能体配置与管理</span>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="saveAgentConfig" :loading="saving" size="large">
            保存智能体配置
          </el-button>
        </div>
      </div>
      
      <!-- 三栏布局容器 -->
      <div class="three-column-layout">
        <!-- 左侧栏：Prompt和人设配置 -->
        <div class="column left-column">
          <div class="column-content">
            <div class="config-section">
              <h3>智能体名称 <span class="required-mark">*</span></h3>
              <el-input
                v-model="agentForm.name"
                placeholder="请输入智能体名称（必填）"
              />
            </div>
            <div class="config-section">
              <h3>头像</h3>
              <div class="avatar-upload-container">
                <div class="avatar-uploader" @click="handleAvatarClick">
                  <img v-if="agentForm.avatar" :src="agentForm.avatar" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </div>
                <div class="avatar-hint">点击设置头像</div>
              </div>
            </div>
            <div class="config-section">
              <h3>简介</h3>
              <el-input
                v-model="agentForm.description"
                type="textarea"
                :rows="3"
                placeholder="请输入智能体简介"
              />
            </div>
            <div class="config-section">
              <h3>标签</h3>
              <el-select
                v-model="agentForm.tags"
                multiple
                filterable
                allow-create
                default-first-option
                :reserve-keyword="false"
                placeholder="输入标签，按回车添加（多个标签）"
                style="width: 100%"
                collapse-tags
                collapse-tags-tooltip
              >
                <el-option
                  v-for="tag in allTags"
                  :key="tag"
                  :label="tag"
                  :value="tag"
                >
                  <span>{{ tag }}</span>
                </el-option>
              </el-select>
              <div class="config-hint">支持自定义标签，多个标签用于分类和搜索</div>
            </div>
            <div class="config-section">
              <h3>系统提示词 (System Prompt)</h3>
              <el-input
                v-model="agentForm.systemPrompt"
                type="textarea"
                :rows="12"
                placeholder="输入系统提示词，定义智能体的角色、能力和行为准则..."
              />
            </div>
            <div class="config-section">
              <h3>问候语 (Greeting)</h3>
              <el-input
                v-model="agentForm.greeting"
                type="textarea"
                :rows="4"
                placeholder="输入智能体的开场白..."
              />
            </div>
          </div>
        </div>

        <!-- 中间栏：配置选项 -->
        <div class="column middle-column">
          <div class="column-content">
            <div class="config-section">
              <h3>对话模型 <span class="required-mark">*</span></h3>
              <el-select 
                v-model="agentForm.modelId" 
                style="width: 100%" 
                :loading="loadingModels" 
                placeholder="请选择对话模型（必填）"
                @change="handleModelChange"
              >
                <el-option
                  v-for="model in activeModels"
                  :key="model.id"
                  :label="model.name"
                  :value="model.id"
                >
                  <span>{{ model.name }}</span>
                  <span style="color: var(--el-text-color-secondary); font-size: 13px; margin-left: 10px">
                    {{ model.providerName || model.provider }}
                  </span>
                </el-option>
              </el-select>
              <div class="config-hint">选择智能体使用的对话模型（LLM）</div>
            </div>

            <div class="config-section">
              <h3>绑定知识库（可多选）</h3>
              <el-select
                v-model="agentForm.kbIds"
                placeholder="请选择知识库（可选）"
                style="width: 100%"
                :loading="loadingKBs"
                multiple
                clearable
                collapse-tags
                collapse-tags-tooltip
              >
                <el-option
                  v-for="kb in knowledgeBases"
                  :key="kb.id"
                  :label="kb.name"
                  :value="kb.id"
                >
                  <span>{{ kb.name }}</span>
                  <span style="color: var(--el-text-color-secondary); font-size: 13px; margin-left: 10px">
                    {{ kb.description || '无描述' }}
                  </span>
                </el-option>
              </el-select>
              <div class="config-hint">选择的知识库将在保存时绑定到智能体</div>
            </div>

            <div class="config-section">
              <h3>选择工作流</h3>
              <el-select
                v-model="agentForm.workflowId"
                placeholder="请选择工作流（可选）"
                style="width: 100%"
                :loading="loadingWorkflows"
                clearable
              >
                <el-option
                  v-for="workflow in workflows"
                  :key="workflow.id"
                  :label="workflow.name"
                  :value="workflow.id"
                >
                  <span>{{ workflow.name }}</span>
                  <span style="color: var(--el-text-color-secondary); font-size: 13px; margin-left: 10px">
                    {{ workflow.description || '无描述' }}
                  </span>
                </el-option>
              </el-select>
              <div class="config-hint">一个智能体只能关联一个主工作流</div>
            </div>

            <div class="config-section">
              <h3>模型参数配置</h3>
              <div style="margin-bottom: 16px">
                <label style="display: block; margin-bottom: 8px; font-size: 13px">Temperature</label>
                <el-slider
                  v-model="agentForm.modelConfig.temperature"
                  :min="0"
                  :max="2"
                  :step="0.1"
                  show-input
                  :show-input-controls="false"
                />
                <div class="config-hint">控制回复的随机性，值越大越随机 (0.0-2.0)</div>
              </div>
              <div style="margin-bottom: 16px">
                <label style="display: block; margin-bottom: 8px; font-size: 13px">Top-P</label>
                <el-slider
                  v-model="agentForm.modelConfig.top_p"
                  :min="0"
                  :max="1"
                  :step="0.05"
                  show-input
                  :show-input-controls="false"
                />
                <div class="config-hint">核采样参数 (0.0-1.0)</div>
              </div>
              <div style="margin-bottom: 16px">
                <label style="display: block; margin-bottom: 8px; font-size: 13px">Max Tokens</label>
                <el-input-number
                  v-model="agentForm.modelConfig.max_tokens"
                  :min="100"
                  :max="8000"
                  :step="100"
                  style="width: 100%"
                />
                <div class="config-hint">最大生成token数</div>
              </div>
              <div style="margin-bottom: 16px">
                <label style="display: block; margin-bottom: 8px; font-size: 13px">Presence Penalty</label>
                <el-slider
                  v-model="agentForm.modelConfig.presence_penalty"
                  :min="-2"
                  :max="2"
                  :step="0.1"
                  show-input
                  :show-input-controls="false"
                />
                <div class="config-hint">存在惩罚 (-2.0-2.0)</div>
              </div>
              <div>
                <label style="display: block; margin-bottom: 8px; font-size: 13px">Frequency Penalty</label>
                <el-slider
                  v-model="agentForm.modelConfig.frequency_penalty"
                  :min="-2"
                  :max="2"
                  :step="0.1"
                  show-input
                  :show-input-controls="false"
                />
                <div class="config-hint">频率惩罚 (-2.0-2.0)</div>
              </div>
            </div>

            <div class="config-section">
              <h3>状态</h3>
              <el-radio-group v-model="agentForm.status">
                <el-radio label="published">已启用</el-radio>
                <el-radio label="offline">已下架</el-radio>
              </el-radio-group>
              <div class="config-hint">
                <span v-if="agentForm.status === 'published'">已启用：用户可在广场看到并使用</span>
                <span v-else-if="agentForm.status === 'offline'">已下架：不在广场展示，但保留配置</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧栏：聊天页面 -->
        <div class="column right-column">
          <div class="column-content chat-content">
            <div class="chat-header">
              <h3>测试对话</h3>
              <div class="chat-header-actions">
                <el-button
                  :icon="Plus"
                  circle
                  text
                  type="primary"
                  size="small"
                  @click="createTestSession"
                  :loading="creatingSession"
                  :disabled="!agentForm.id"
                  title="新建会话"
                />
                <el-button
                  :icon="Clock"
                  circle
                  text
                  type="primary"
                  size="small"
                  @click="sessionsDrawerVisible = true"
                  :disabled="!agentForm.id"
                  title="历史记录"
                />
                <el-button
                  :icon="Delete"
                  circle
                  text
                  type="danger"
                  size="small"
                  @click="clearMessages"
                  :disabled="messages.length === 0 || !currentSessionId"
                  title="清屏"
                />
              </div>
            </div>
            <div class="chat-container">
              <div class="messages-list" ref="messagesRef">
                <div v-if="messages.length === 0 && !generating && agentForm.greeting" class="empty-chat-greeting">
                  <div class="greeting-text">{{ agentForm.greeting }}</div>
                </div>
                <div
                  v-for="msg in messages"
                  :key="msg.id"
                  class="message-item"
                  :class="msg.role"
                >
                  <div 
                    class="message-content"
                    v-html="msg.role === 'assistant' ? formatMarkdown(msg.content) : msg.content"
                  ></div>
                  <div class="message-time">{{ formatTime(msg.createdAt || msg.createTime) }}</div>
                </div>
                <div v-if="generating" class="message-item assistant">
                  <div class="message-content">正在生成中...<span class="cursor">▋</span></div>
                </div>
              </div>
              <div class="chat-input-area">
                <el-input
                  v-model="inputText"
                  type="textarea"
                  :rows="3"
                  placeholder="输入消息进行测试..."
                  @keyup.ctrl.enter="sendMessage"
                  :disabled="generating"
                />
                <div class="input-actions">
                  <el-button
                    type="primary"
                    @click="sendMessage"
                    :loading="generating"
                    :disabled="!inputText.trim()"
                  >
                    发送 (Ctrl+Enter)
                  </el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 会话列表抽屉 -->
    <el-drawer
      v-model="sessionsDrawerVisible"
      title="测试会话列表"
      :size="400"
      direction="rtl"
    >
      <div class="drawer-content">
        <div class="sessions-list">
          <div
            v-for="session in testSessions"
            :key="session.id"
            class="session-item"
            :class="{ active: currentSessionId === session.id, pinned: session.isPinned }"
            @click="selectTestSession(session.id); sessionsDrawerVisible = false"
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
                    <el-dropdown-item :command="{ action: 'pin', session }">
                      {{ session.isPinned ? '取消置顶' : '置顶' }}
                    </el-dropdown-item>
                    <el-dropdown-item :command="{ action: 'delete', session }" divided>删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
          <div v-if="testSessions.length === 0" class="empty-sessions">
            <p>暂无测试会话</p>
          </div>
        </div>
      </div>
    </el-drawer>
  </Layout>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { marked } from 'marked'
import { chatApi } from '@/api/chat.js'

// Configure marked options
marked.setOptions({
  breaks: true, // Enable line breaks
  gfm: true // Enable GitHub Flavored Markdown
})
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowLeft, Collection, Delete, Clock, StarFilled, MoreFilled } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { agentApi } from '@/api/agent'
import { llmModelApi } from '@/api/llm'
import { knowledgeApi } from '@/api/knowledge'
import { workflowApi } from '@/api/workflow'
import { http } from '@/utils/http'

const route = useRoute()
const router = useRouter()

// 配置表单
const agentForm = ref({
  id: null,
  name: '',
  avatar: '',
  description: '',
  tags: [], // 改为数组
  systemPrompt: '', // 改为systemPrompt
  greeting: '',
  modelId: null,
  modelConfig: {
    modelId: null,
    temperature: 0.7,
    top_p: 0.9,
    max_tokens: 2000,
    presence_penalty: 0.0,
    frequency_penalty: 0.0
  },
  kbIds: [],
  workflowId: null,
  status: 'offline',
  // 详情字段
  heatValue: 0,
  createdAt: null,
  updatedAt: null,
  publishedAt: null,
  createdBy: null
})

// 已绑定的知识库列表
const boundKnowledgeBases = ref([])

// 常用标签
const commonTags = ref(['情感', '职场', '压力', '焦虑', '抑郁', '睡眠', '人际关系', '成长', '心理', '陪伴'])

// 所有标签（常用标签 + 用户自定义标签）
const allTags = computed(() => {
  const selectedTags = agentForm.value.tags || []
  const customTags = selectedTags.filter(tag => !commonTags.value.includes(tag))
  return [...commonTags.value, ...customTags]
})

// 下拉选项数据
const knowledgeBases = ref([])
const workflows = ref([])
const models = ref([])
const loadingKBs = ref(false)
const loadingWorkflows = ref(false)
const loadingModels = ref(false)
const loading = ref(false)
const saving = ref(false)

// 原始表单数据，用于检测是否有修改
const originalForm = ref(null)

// 过滤出已启用的对话模型（排除向量模型）
const activeModels = computed(() => {
  return models.value.filter(m => m.isActive && m.modelType !== 'embedding')
})

// 检查是否有未保存的修改
const hasUnsavedChanges = computed(() => {
  if (!originalForm.value) return false
  
  const current = JSON.stringify({
    name: agentForm.value.name,
    avatar: agentForm.value.avatar,
    description: agentForm.value.description,
    tags: [...(agentForm.value.tags || [])].sort(),
    systemPrompt: agentForm.value.systemPrompt,
    greeting: agentForm.value.greeting,
    modelId: agentForm.value.modelId,
    modelConfig: agentForm.value.modelConfig,
    kbIds: [...agentForm.value.kbIds].sort(),
    workflowId: agentForm.value.workflowId,
    status: agentForm.value.status
  })
  
  const original = JSON.stringify({
    name: originalForm.value.name,
    avatar: originalForm.value.avatar,
    description: originalForm.value.description,
    tags: [...(originalForm.value.tags || [])].sort(),
    systemPrompt: originalForm.value.systemPrompt,
    greeting: originalForm.value.greeting,
    modelId: originalForm.value.modelId,
    modelConfig: originalForm.value.modelConfig,
    kbIds: [...(originalForm.value.kbIds || [])].sort(),
    workflowId: originalForm.value.workflowId,
    status: originalForm.value.status
  })
  
  return current !== original
})

// 聊天相关
const messages = ref([])
const inputText = ref('')
const generating = ref(false)
const messagesRef = ref(null)
const currentSessionId = ref(null)
const testSessions = ref([])
const sessionsDrawerVisible = ref(false)
const creatingSession = ref(false)

// 错误码映射表（基础错误）
const ERROR_MESSAGES = {
    40001: '用户不存在',
    40002: '用户已存在',
    40003: '用户已被禁用',
    40004: '用户已被锁定',
    40005: '用户名或密码错误',
    40006: '密码无效',
    40007: '缺少必需参数',
    40008: '参数错误',
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
    40301: '权限不足',
    40302: '权限不足',
    40303: '无权访问此会话',
    40304: '无权访问此消息',
    40400: '资源不存在',
    40401: '会话不存在',
    40402: '消息不存在',
    40403: '智能体不存在',
    40404: '模型不存在',
    50000: '服务器内部错误',
    50001: '数据库错误',
    50002: '网络错误',
    50003: 'AI服务调用失败，请稍后重试',
    50004: '知识库检索服务失败',
    50005: '流式响应处理失败',
    50006: '模型配置错误',
    // RAG相关错误 (5xxx)
    5007: 'RAG服务调用失败',
    5008: 'RAG服务不可用',
    5009: 'Embedding API认证失败，请检查智谱AI的API Key配置',
    5010: 'Embedding API额度不足，请充值后重试',
    5011: 'Embedding API请求频率超限，请稍后重试',
    5012: 'Embedding API调用超时，请检查网络',
    5013: '文本向量化失败',
    // LLM相关错误 (8xxx)
    8001: 'LLM提供商不存在',
    8002: 'LLM模型不存在',
    8003: 'LLM调用失败',
    8004: 'LLM API认证失败，请检查对应提供商的API Key配置',
    8005: 'LLM API额度不足，请充值后重试',
    8006: 'LLM API请求频率超限，请稍后重试',
    8007: 'LLM API调用超时，请检查网络',
    8008: 'LLM模型不可用或配置错误'
}

// API Key相关错误码
const API_KEY_ERROR_CODES = [5009, 8004]
// 额度相关错误码
const QUOTA_ERROR_CODES = [5010, 8005]
// 频率限制错误码
const RATE_LIMIT_ERROR_CODES = [5011, 8006]

// 根据错误码获取友好的错误信息
const getErrorMessage = (error) => {
  return ERROR_MESSAGES[error.code] || error.message || '操作失败'
}

// 获取错误建议
const getErrorSuggestion = (code) => {
  if (API_KEY_ERROR_CODES.includes(code)) {
    return '请联系管理员检查或更新API Key配置'
  }
  if (QUOTA_ERROR_CODES.includes(code)) {
    return '请联系管理员充值API额度'
  }
  if (RATE_LIMIT_ERROR_CODES.includes(code)) {
    return '请等待几秒后重试'
  }
  return null
}

// 统一错误处理
const handleError = (error, showMessage = true) => {
  const message = getErrorMessage(error)
  if (showMessage) {
    ElMessage.error(message)
    
    // 对于API Key或额度相关错误，给出额外提示
    const suggestion = getErrorSuggestion(error.code)
    if (suggestion) {
      setTimeout(() => {
        ElMessage.warning(suggestion)
      }, 500)
    }
  }
  return message
}

// 格式化 Markdown
const formatMarkdown = (content) => {
  if (!content) return ''
  try {
    return marked.parse(content)
  } catch (e) {
    console.error('Markdown parsing error:', e)
    return content
  }
}

// 加载消息
const loadMessages = async () => {
  if (!currentSessionId.value) {
    messages.value = []
    // 如果没有会话且有问候语，显示问候语（仅显示，不作为消息）
    return
  }
  try {
    const data = await chatApi.getMessages(currentSessionId.value, {
      page: 1,
      size: 50
    })
    const serverMessages = data.list || []
    
    // 智能去重：将临时消息替换为服务器消息，避免重复显示
    const serverMessageIds = new Set(serverMessages.map(m => m.id))
    
    // 构建服务器消息的匹配键（用于识别重复消息）
    // 使用 role + content 作为匹配键，因为时间戳可能略有差异
    const serverMessageKeys = new Set(
      serverMessages.map(m => `${m.role}:${m.content}`)
    )
    
    // 过滤临时消息：如果临时消息的内容已存在于服务器消息中，则过滤掉
    const tempMessages = messages.value.filter(m => {
      // 处理当前会话的临时消息（ID以temp_开头）
      if (!m.id) return false
      const messageId = m.id.toString()
      if (!messageId.startsWith('temp_')) return false
      if (m.sessionId !== currentSessionId.value) return false
      
      // 如果临时消息的内容已存在于服务器消息中，过滤掉（避免重复）
      const messageKey = `${m.role}:${m.content}`
      if (serverMessageKeys.has(messageKey)) return false
      
      // 保留其他临时消息（例如正在生成中的消息）
      return true
    })
    
    // 合并服务器消息和剩余的临时消息
    let allMessages = [...serverMessages, ...tempMessages].sort((a, b) => {
      return new Date(a.createdAt || a.createTime || 0).getTime() - new Date(b.createdAt || b.createTime || 0).getTime()
    })
    
    messages.value = allMessages
    scrollToBottom()
  } catch (error) {
    // 会话不存在或无权访问时静默处理
    const silentErrorCodes = [40401, 40303]
    if (!silentErrorCodes.includes(error.code)) {
      ElMessage.warning(handleError(error, false))
    }
    messages.value = []
  }
}

// 加载知识库列表
const loadKnowledgeBases = async () => {
  loadingKBs.value = true
  try {
    const response = await knowledgeApi.getKnowledgeBases({
      page: 1,
      size: 1000  // 获取所有知识库
    })
    // 如果返回的是分页数据，取list字段；否则直接使用数组
    knowledgeBases.value = response.list || response || []
  } catch (error) {
    console.error('加载知识库列表失败', error)
    ElMessage.error('加载知识库列表失败')
  } finally {
    loadingKBs.value = false
  }
}

// 加载已绑定的知识库
const loadBoundKnowledgeBases = async (agentId) => {
  try {
    const kbs = await agentApi.getAgentKnowledgeBases(agentId)
    boundKnowledgeBases.value = kbs || []
    // 同步到表单的kbIds
    agentForm.value.kbIds = kbs.map(kb => kb.id)
  } catch (error) {
    console.error('加载已绑定知识库失败', error)
    // 不显示错误，避免干扰
  }
}

// 解绑知识库
const handleUnbindKB = async (kbId) => {
  if (!agentForm.value.id) return
  
  try {
    await ElMessageBox.confirm(
      '确定要解绑该知识库吗？',
      '确认解绑',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    await agentApi.unbindKnowledgeBase(agentForm.value.id, kbId)
    ElMessage.success('解绑成功')
    // 重新加载已绑定的知识库
    await loadBoundKnowledgeBases(agentForm.value.id)
    // 更新原始数据
    originalForm.value = JSON.parse(JSON.stringify(agentForm.value))
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('解绑失败')
      console.error(error)
    }
  }
}

// 加载工作流列表
const loadWorkflows = async () => {
  loadingWorkflows.value = true
  try {
    const response = await workflowApi.getWorkflows({ isActive: 1 })
    workflows.value = response.list || response || []
  } catch (error) {
    console.error('加载工作流列表失败', error)
    ElMessage.error('加载工作流列表失败')
  } finally {
    loadingWorkflows.value = false
  }
}

// 加载模型列表
const loadModels = async () => {
  loadingModels.value = true
  try {
    models.value = await llmModelApi.getModels()
    // 如果当前没有选择模型，自动选择第一个启用的模型作为默认值
    if (!agentForm.value.modelId && activeModels.value.length > 0) {
      const firstActiveModel = activeModels.value[0]
      agentForm.value.modelId = firstActiveModel.id
      handleModelChange(firstActiveModel.id)
    }
  } catch (error) {
    console.error('加载模型列表失败', error)
    ElMessage.error('加载模型列表失败')
  } finally {
    loadingModels.value = false
  }
}

// 当选择模型时,更新modelConfig中的modelId
const handleModelChange = (modelId) => {
  if (modelId) {
    agentForm.value.modelConfig.modelId = modelId
    // 如果模型有默认配置,可以在这里加载并合并
    const selectedModel = models.value.find(m => m.id === modelId)
    if (selectedModel && selectedModel.defaultConfig) {
      // 合并默认配置,但保留用户已设置的配置
      const defaultConfig = typeof selectedModel.defaultConfig === 'string' 
        ? JSON.parse(selectedModel.defaultConfig) 
        : selectedModel.defaultConfig
      agentForm.value.modelConfig = {
        ...defaultConfig,
        ...agentForm.value.modelConfig,
        modelId: modelId
      }
    }
  }
}

// 加载智能体详情
const loadAgentDetail = async (id) => {
  loading.value = true
  try {
    const agent = await agentApi.getAgent(id)
    
    // 解析modelConfig
    let modelConfig = {
      modelId: null,
      temperature: 0.7,
      top_p: 0.9,
      max_tokens: 2000,
      presence_penalty: 0.0,
      frequency_penalty: 0.0
    }
    if (agent.modelConfig) {
      try {
        const parsed = typeof agent.modelConfig === 'string' 
          ? JSON.parse(agent.modelConfig) 
          : agent.modelConfig
        modelConfig = { ...modelConfig, ...parsed }
      } catch (e) {
        console.error('解析modelConfig失败', e)
      }
    }
    
    // 检查绑定的模型是否已被禁用（只有在模型列表已加载时才检查）
    const selectedModelId = modelConfig.modelId || null
    if (selectedModelId && models.value.length > 0) {
      const selectedModel = models.value.find(m => m.id === selectedModelId)
      if (selectedModel && !selectedModel.isActive) {
        ElMessage.warning(`智能体绑定的模型"${selectedModel.name}"已被禁用，已自动切换到默认模型`)
        // 如果模型已禁用，尝试选择第一个启用的模型
        if (activeModels.value.length > 0) {
          const firstActiveModel = activeModels.value[0]
          modelConfig.modelId = firstActiveModel.id
          modelConfig = {
            ...modelConfig,
            modelId: firstActiveModel.id
          }
        } else {
          modelConfig.modelId = null
        }
      }
    }
    
    // 处理tags：后端返回的是数组
    const tags = Array.isArray(agent.tags) ? agent.tags : (agent.tags ? agent.tags.split(',').map(t => t.trim()).filter(t => t) : [])
    
    // modelId应该从agent.modelId获取，而不是从modelConfig
    const agentModelId = agent.modelId || modelConfig.modelId || null
    
    agentForm.value = {
      id: agent.id,
      name: agent.name || '',
      avatar: agent.avatar || '',
      description: agent.description || '',
      tags: tags,
      systemPrompt: agent.systemPrompt || agent.prompt || '', // 兼容旧字段名
      greeting: agent.greeting || '',
      modelId: agentModelId,
      modelConfig: modelConfig,
      kbIds: [], // 初始化为空，稍后加载绑定的知识库
      workflowId: agent.workflowId || null,
      status: agent.status || 'offline',
      // 详情字段
      heatValue: agent.heatValue || 0,
      createdAt: agent.createdAt,
      updatedAt: agent.updatedAt,
      publishedAt: agent.publishedAt,
      createdBy: agent.createdBy
    }
    
    // 加载已绑定的知识库
    await loadBoundKnowledgeBases(agent.id)
    
    // 保存原始数据用于比较
    originalForm.value = JSON.parse(JSON.stringify(agentForm.value))
    
    // 加载测试会话列表
    await loadTestSessions()
  } catch (error) {
    console.error('加载智能体详情失败', error)
    ElMessage.error('加载智能体详情失败')
  } finally {
    loading.value = false
  }
}

// 头像点击处理
const handleAvatarClick = () => {
  ElMessage.info('头像上传功能正在开发中，敬请期待')
}

// 上传相关（已禁用）
const uploadAction = computed(() => {
  return `${import.meta.env.VITE_API_BASE_URL || ''}/api/v1/upload/avatar`
})

const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})

// 以下代码已禁用，保留用于未来恢复
/*
const handleAvatarSuccess = (response) => {
  if (response && response.data && response.data.url) {
    agentForm.value.avatar = response.data.url
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error('头像上传失败')
  }
}

  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}
*/

// 保存智能体配置
const saveAgentConfig = async () => {
  if (!agentForm.value.name) {
    ElMessage.warning('请先输入智能体名称')
    return
  }
  
  if (!agentForm.value.modelId) {
    ElMessage.warning('请先选择模型')
    return
  }

  saving.value = true
  try {
    // 构建modelConfig,确保包含modelId
    const modelConfig = {
      ...agentForm.value.modelConfig,
      modelId: agentForm.value.modelId
    }
    
    // 处理modelConfig，移除modelId（modelId作为独立字段传递）
    const { modelId: _, ...configWithoutModelId } = modelConfig
    
    const requestData = {
      name: agentForm.value.name,
      avatar: agentForm.value.avatar,
      description: agentForm.value.description,
      tags: Array.isArray(agentForm.value.tags) ? agentForm.value.tags.join(',') : agentForm.value.tags, // 后端需要逗号分隔的字符串
      systemPrompt: agentForm.value.systemPrompt,
      greeting: agentForm.value.greeting,
      modelId: agentForm.value.modelId, // modelId单独传递
      modelConfig: configWithoutModelId, // modelConfig不包含modelId
      kbIds: agentForm.value.kbIds,
      workflowId: agentForm.value.workflowId,
      status: agentForm.value.status
    }
    
    if (agentForm.value.id) {
      // 更新现有智能体
      await agentApi.updateAgent(agentForm.value.id, requestData)
      ElMessage.success('更新成功')
      // 重新加载智能体详情以获取最新数据（包括更新时间等）
      await loadAgentDetail(agentForm.value.id)
    } else {
      // 创建新智能体
      const result = await agentApi.createAgent(requestData)
      agentForm.value.id = result.id
      ElMessage.success('创建成功')
      // 更新URL，添加id参数
      router.replace({ query: { id: result.id } })
      // 重新加载智能体详情以获取完整数据
      await loadAgentDetail(result.id)
    }
    // 重置会话，使用新的智能体配置
    currentSessionId.value = null
    messages.value = []
    generating.value = false
    // 重新加载会话列表
    if (agentForm.value.id) {
      await loadTestSessions()
    }
  } catch (error) {
    ElMessage.error('保存失败')
    console.error(error)
  } finally {
    saving.value = false
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputText.value.trim() || generating.value) {
    return
  }

  // 检查是否已保存智能体配置
  if (!agentForm.value.id) {
    ElMessage.warning('请先保存智能体配置')
    return
  }

  const content = inputText.value.trim()
  inputText.value = ''
  generating.value = true

  // 创建会话（如果需要）- 调试模式使用 test 类型会话
  if (!currentSessionId.value) {
    try {
      const sessionData = await chatApi.createSession({
        agentId: agentForm.value.id,
        title: '测试对话',
        sessionType: 'test'  // 调试模式使用 test 类型会话，不计入热度统计
      })
      currentSessionId.value = sessionData.id
      // 创建会话后加载消息
      await loadMessages()
      // 重新加载会话列表
      await loadTestSessions()
    } catch (error) {
      handleError(error)
      generating.value = false
      return
    }
  }

  // 立即添加用户消息到界面（临时显示，提升用户体验）
  const tempUserMessageId = `temp_user_${Date.now()}`
  const tempUserMessage = {
    id: tempUserMessageId,
    sessionId: currentSessionId.value,
    role: 'user',
    content: content,
    createdAt: new Date().toISOString()
  }
  messages.value.push(tempUserMessage)
  scrollToBottom()

  // 发送HTTP请求
  try {
    await chatApi.sendMessage(currentSessionId.value, {
      content: content
    })
    
    // 请求成功，后端已保存消息，重新加载消息列表
    generating.value = false
    
    // 重新加载消息列表，确保显示最新的消息（包括刚发送的用户消息和AI回复）
    await loadMessages()
    scrollToBottom()
  } catch (error) {
    handleError(error)
    generating.value = false
    // 错误时移除临时消息，然后重新加载
    messages.value = messages.value.filter(m => m.id !== tempUserMessageId)
    await loadMessages()
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 清屏功能
const clearMessages = async () => {
  if (messages.value.length === 0) {
    return
  }
  
  try {
    await ElMessageBox.confirm(
      '确定要清空所有对话消息吗？',
      '确认清屏',
      {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      }
    )
    
    // 清空消息列表
    messages.value = []
    // 重置会话ID，下次发送消息时会创建新会话
    currentSessionId.value = null
    // 停止生成状态
    generating.value = false
    
    ElMessage.success('已清空对话')
  } catch (error) {
    // 用户取消操作，不做任何处理
    if (error !== 'cancel') {
      console.error('清屏操作失败', error)
    }
  }
}

// 加载测试会话列表
const loadTestSessions = async () => {
  if (!agentForm.value.id) {
    testSessions.value = []
    return
  }
  try {
    const data = await chatApi.getSessions({
      agentId: agentForm.value.id,
      sessionType: 'test',  // 只加载测试类型的会话
      page: 1,
      size: 50
    })
    testSessions.value = data.list || []
  } catch (error) {
    console.error('加载测试会话列表失败', error)
    // 静默处理错误，避免干扰用户
  }
}

// 创建新的测试会话
const createTestSession = async () => {
  if (creatingSession.value || !agentForm.value.id) return
  
  creatingSession.value = true
  try {
    const sessionData = await chatApi.createSession({
      agentId: agentForm.value.id,
      title: '测试对话',
      sessionType: 'test'  // 测试类型会话，不计入热度统计
    })
    currentSessionId.value = sessionData.id
    messages.value = []
    await loadTestSessions()
    // 加载新会话的消息（包括问候语）
    await loadMessages()
    ElMessage.success('测试会话创建成功')
  } catch (error) {
    handleError(error)
  } finally {
    creatingSession.value = false
  }
}

// 选择测试会话
const selectTestSession = async (sessionId) => {
  if (currentSessionId.value === sessionId) return
  currentSessionId.value = sessionId
  messages.value = []
  await loadMessages()
}

// 删除会话
const deleteTestSession = async (sessionId) => {
  try {
    await chatApi.deleteSession(sessionId)
    if (currentSessionId.value === sessionId) {
      currentSessionId.value = null
      messages.value = []
      // 如果有其他会话，选择第一个
      const remainingSessions = testSessions.value.filter(s => s.id !== sessionId)
      if (remainingSessions.length > 0) {
        await selectTestSession(remainingSessions[0].id)
      }
    }
    await loadTestSessions()
    ElMessage.success('会话已删除')
  } catch (error) {
    handleError(error)
  }
}

// 处理会话操作（置顶、删除，不支持重命名）
const handleSessionAction = async (command) => {
  const { action, session } = command
  
  if (action === 'pin') {
    try {
      await chatApi.pinSession(session.id, session.isPinned ? 0 : 1)
      await loadTestSessions()
      ElMessage.success(session.isPinned ? '已取消置顶' : '已置顶')
    } catch (error) {
      handleError(error)
    }
  } else if (action === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除此测试会话吗？删除后无法恢复。', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await deleteTestSession(session.id)
    } catch (error) {
      // 用户取消删除，不做处理
      if (error !== 'cancel') {
        console.error('删除会话失败', error)
      }
    }
  }
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
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

// 格式化日期时间
const formatDateTime = (timeStr) => {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 格式化热度值
const formatHeat = (heat) => {
  if (heat >= 10000) {
    return (heat / 10000).toFixed(1) + 'w'
  } else if (heat >= 1000) {
    return (heat / 1000).toFixed(1) + 'k'
  }
  return heat.toString()
}

// 获取热度标签类型
const getHeatTagType = (heat) => {
  if (heat >= 1000) {
    return 'danger'
  } else if (heat >= 500) {
    return 'warning'
  } else if (heat >= 100) {
    return ''
  }
  return 'info'
}

// 处理返回 - 直接跳转，由路由守卫统一处理提示
const handleBack = () => {
  router.push('/admin/agents')
}

// 路由守卫：离开前检查未保存的修改
onBeforeRouteLeave(async (to, from, next) => {
  if (hasUnsavedChanges.value) {
    try {
      await ElMessageBox.confirm(
        '您有未保存的修改，是否保存后再离开？',
        '提示',
        {
          confirmButtonText: '保存并离开',
          cancelButtonText: '不保存离开',
          distinguishCancelAndClose: true,
          type: 'warning'
        }
      )
      // 用户选择保存
      await saveAgentConfig()
      next()
    } catch (error) {
      if (error === 'cancel') {
        // 用户选择不保存，允许离开
        next()
      } else {
        // 用户关闭对话框，阻止离开
        next(false)
      }
    }
  } else {
    // 没有未保存的修改，允许离开
    next()
  }
})

onMounted(async () => {
  // 先加载模型列表（需要先加载以便设置默认模型）
  await loadModels()
  
  // 加载其他下拉选项
  await Promise.all([
    loadKnowledgeBases(),
    loadWorkflows()
  ])
  
  // 如果URL中有id参数，加载智能体详情
  const agentId = route.query.id
  if (agentId) {
    await loadAgentDetail(agentId)
  } else {
    // 新建模式，初始化原始数据
    originalForm.value = JSON.parse(JSON.stringify(agentForm.value))
  }
})

onUnmounted(() => {
  // 清理工作（如果需要）
})
</script>

<style scoped>
.agent-detail-page {
  padding: 20px;
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color-page);
  overflow: hidden;
}

/* 页面头部容器 */
.page-header-container {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.back-button {
  flex-shrink: 0;
  padding: 8px 12px;
  font-size: 14px;
}

.header-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-width: calc(100% - 300px);
}

.header-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: auto;
  margin-right: 40px;
}

.agent-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
}

.agent-subtitle {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

/* 三栏布局容器 */
.three-column-layout {
  display: flex;
  gap: 16px;
  padding: 0;
  height: calc(100vh - 180px);
  min-height: 0;
  align-items: stretch;
  overflow: hidden;
}

/* 通用列样式 */
.column {
  flex: 1;
  background: var(--el-bg-color);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 0;
  max-height: 100%;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.03), 0 1px 6px -1px rgba(0, 0, 0, 0.02), 0 2px 4px 0 rgba(0, 0, 0, 0.02);
}


/* 头像上传样式 */
.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.avatar-uploader {
  border: 1px dashed var(--el-border-color);
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--el-bg-color-page);
}

.avatar-uploader:hover {
  border-color: var(--el-color-primary);
  background: var(--el-fill-color-light);
}

.avatar {
  width: 80px;
  height: 80px;
  object-fit: cover;
  display: block;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: var(--el-text-color-secondary);
}

.avatar-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.column-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  background: var(--el-bg-color);
}

/* 左侧和中间列内容区域可滚动 */
.left-column .column-content,
.middle-column .column-content {
  overflow-y: auto;
  overflow-x: hidden;
  flex: 1;
  min-height: 0;
}

/* 滚动条样式优化 */
.left-column .column-content::-webkit-scrollbar,
.middle-column .column-content::-webkit-scrollbar {
  width: 6px;
}

.left-column .column-content::-webkit-scrollbar-track,
.middle-column .column-content::-webkit-scrollbar-track {
  background: var(--el-bg-color-page);
  border-radius: 3px;
}

.left-column .column-content::-webkit-scrollbar-thumb,
.middle-column .column-content::-webkit-scrollbar-thumb {
  background: var(--el-border-color);
  border-radius: 3px;
}

.left-column .column-content::-webkit-scrollbar-thumb:hover,
.middle-column .column-content::-webkit-scrollbar-thumb:hover {
  background: var(--el-border-color-dark);
}

/* 右侧列（聊天区域）保持原有样式 */
.right-column .column-content {
  padding: 0;
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 配置区域样式 */
.config-section {
  margin-bottom: 24px;
}

.config-section:last-child {
  margin-bottom: 0;
}

.config-section h3 {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 12px 0;
}

.config-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 8px;
}

/* 必填标记样式 */
.required-mark {
  color: var(--el-color-danger);
  margin-left: 2px;
  font-weight: normal;
}

/* 聊天区域样式 */
.chat-content {
  padding: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0;
  background: var(--el-bg-color);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.chat-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.chat-header-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 20px;
}

.messages-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 16px;
  padding: 12px;
  background: var(--el-bg-color-page);
  border-radius: 8px;
  min-height: 200px;
  max-height: 400px;
  display: flex;
  flex-direction: column;
}

.empty-chat-greeting {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
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
  max-width: 80%;
}

.message-item {
  margin-bottom: 16px;
}

.message-item.user {
  text-align: right;
}

.message-item.assistant {
  text-align: left;
}

.message-content {
  display: inline-block;
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;
}

/* Markdown Styles */
:deep(.message-content p) {
  margin: 0.5em 0;
}

:deep(.message-content p:first-child) {
  margin-top: 0;
}

:deep(.message-content p:last-child) {
  margin-bottom: 0;
}

:deep(.message-content pre) {
  background-color: var(--el-fill-color-light);
  border-radius: 6px;
  padding: 16px;
  overflow: auto;
  margin: 0.5em 0;
}

:deep(.message-content code) {
  background-color: rgba(175, 184, 193, 0.2);
  border-radius: 4px;
  padding: 0.2em 0.4em;
  font-family: ui-monospace, SFMono-Regular, SF Mono, Menlo, Consolas, Liberation Mono, monospace;
  font-size: 85%;
}

:deep(.message-content pre code) {
  background-color: transparent;
  padding: 0;
  font-size: 100%;
}

:deep(.message-content ul), :deep(.message-content ol) {
  padding-left: 2em;
  margin: 0.5em 0;
}

:deep(.message-content strong) {
  font-weight: 600;
}

:deep(.message-content blockquote) {
  margin: 0.5em 0;
  padding-left: 1em;
  border-left: 4px solid var(--el-border-color);
  color: var(--el-text-color-regular);
}

:deep(.message-content a) {
  color: var(--el-color-primary);
  text-decoration: none;
}

:deep(.message-content a:hover) {
  text-decoration: underline;
}

/* User Message specific overrides if needed */
.message-item.user .message-content :deep(code) {
  background-color: rgba(255, 255, 255, 0.2);
}

.message-item.user .message-content :deep(pre) {
  background-color: rgba(0, 0, 0, 0.1);
}

.message-item.user .message-content :deep(blockquote) {
  border-left-color: rgba(255, 255, 255, 0.4);
  color: rgba(255, 255, 255, 0.8);
}

.message-item.user .message-content {
  background: var(--app-primary-gradient);
  color: #fff;
}

.message-item.assistant .message-content {
  background: var(--el-bg-color);
  color: var(--el-text-color-primary);
  border: 1px solid var(--el-border-color-light);
}

.message-time {
  font-size: 11px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
  padding: 0 4px;
}

.cursor {
  display: inline-block;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}

.chat-input-area {
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 16px;
  flex-shrink: 0;
  background: var(--el-bg-color);
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

/* 信息列表样式 */
.info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.info-label {
  color: var(--el-text-color-regular);
  font-weight: 500;
  min-width: 80px;
}

.info-value {
  color: var(--el-text-color-primary);
}

/* 知识库列表样式 */
.kb-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.kb-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: var(--el-bg-color-page);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
  transition: all 0.3s;
}

.kb-item:hover {
  background: var(--el-fill-color-light);
  border-color: var(--el-color-primary-light-5);
}

.kb-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.kb-info .el-icon {
  color: var(--el-color-primary);
  font-size: 18px;
}

.kb-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.flame-emoji {
  font-size: 14px;
  margin-right: 4px;
}

/* 抽屉样式 */
.drawer-content {
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sessions-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.empty-sessions {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: var(--el-text-color-secondary);
}

.empty-sessions p {
  margin: 0;
  font-size: 14px;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  margin-bottom: 8px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.session-item:hover {
  background: var(--el-fill-color-light);
  border-color: var(--el-color-primary-light-7);
}

.session-item.active {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
}

.session-item.pinned {
  border-left: 3px solid var(--el-color-warning);
}

.session-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.session-title-wrapper {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pin-icon {
  color: var(--el-color-warning);
  font-size: 14px;
}

.session-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.session-actions {
  flex-shrink: 0;
  margin-left: 8px;
}

.session-actions .action-btn {
  padding: 4px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .three-column-layout {
    flex-direction: column;
    gap: 8px;
  }
  
  .column {
    height: auto;
  }
}
</style>

