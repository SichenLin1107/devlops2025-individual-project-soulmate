<template>
  <div 
    class="workflow-node" 
    :class="{ selected, configured, [nodeType]: true, 'control-node': isControlNode }"
    :style="nodeStyle"
  >
    <!-- 左侧输入端口 -->
    <div class="handles-left" v-if="inputs.length > 0">
      <div 
        v-for="(input, idx) in inputs" 
        :key="input.id"
        class="handle-wrapper input-handle"
        :style="{ top: getPortPosition(idx, inputs.length) }"
      >
        <Handle
          type="target"
          :position="Position.Left"
          :id="input.id"
          class="port input-port"
        />
        <span class="handle-label" v-if="inputs.length > 1 || showHandleLabels">{{ input.name }}</span>
      </div>
    </div>

    <!-- 节点主体 -->
    <div class="node-container">
      <!-- 头部 -->
      <div class="node-header" :style="headerStyle">
        <div class="header-icon" :style="iconStyle">
          <el-icon><component :is="icon" /></el-icon>
        </div>
        <div class="header-content">
          <span class="node-title">{{ label }}</span>
          <span class="node-type">{{ nodeTypeLabel }}</span>
        </div>
        <div v-if="configured" class="config-badge" title="已配置">
          <el-icon><Check /></el-icon>
        </div>
        <div v-else-if="needsConfig" class="config-warning" title="需要配置">
          <el-icon><Warning /></el-icon>
        </div>
      </div>
      
      <!-- 内容区域 - 根据节点类型显示不同内容 -->
      <div class="node-body" v-if="showBody">
        <!-- 节点描述 -->
        <div class="node-description" v-if="description && !hasConfig">
          {{ description }}
        </div>
        
        <!-- 开始节点 -->
        <div v-if="nodeType === 'start'" class="node-specific start-content">
          <div class="content-item">
            <el-icon><User /></el-icon>
            <span>接收用户输入</span>
          </div>
        </div>
        
        <!-- 结束节点 -->
        <div v-else-if="nodeType === 'end'" class="node-specific end-content">
          <div class="content-item">
            <el-icon><Finished /></el-icon>
            <span>输出最终回复</span>
          </div>
        </div>
        
        <!-- 安全检测节点 -->
        <div v-else-if="nodeType === 'safety_check'" class="node-specific safety-content">
          <div class="content-item" v-if="config.checkLevel">
            <span class="config-key">检测级别:</span>
            <span class="config-value">{{ formatCheckLevel(config.checkLevel) }}</span>
          </div>
          <div class="content-item" v-if="config.enableCrisisIntervention !== undefined">
            <el-icon :class="config.enableCrisisIntervention ? 'text-success' : 'text-muted'">
              <CircleCheck v-if="config.enableCrisisIntervention" />
              <CircleClose v-else />
            </el-icon>
            <span>{{ config.enableCrisisIntervention ? '已启用' : '未启用' }}危机干预</span>
          </div>
          <div class="output-labels">
            <span class="output-label safe">✓ 安全通过</span>
            <span class="output-label crisis">⚠ 危机处理</span>
          </div>
        </div>
        
        <!-- LLM处理节点 -->
        <div v-else-if="nodeType === 'llm_process'" class="node-specific llm-content">
          <div class="content-item" v-if="config.modelId">
            <span class="config-key">模型:</span>
            <span class="config-value">{{ getModelDisplayName(config.modelId) }}</span>
          </div>
          <div class="content-item" v-if="config.temperature !== undefined">
            <span class="config-key">温度:</span>
            <span class="config-value">{{ config.temperature }}</span>
          </div>
          <div class="prompt-preview" v-if="config.systemPrompt">
            <span class="prompt-label">系统提示:</span>
            <span class="prompt-text">{{ truncateText(config.systemPrompt, 50) }}</span>
          </div>
        </div>
        
        <!-- RAG检索节点 -->
        <div v-else-if="nodeType === 'rag_retrieval'" class="node-specific rag-content">
          <div class="content-item" v-if="config.knowledgeBaseId">
            <span class="config-key">知识库:</span>
            <span class="config-value">{{ getKbDisplayName(config.knowledgeBaseId) }}</span>
          </div>
          <div class="content-item">
            <span class="config-key">返回数量:</span>
            <span class="config-value">Top {{ config.topK || 5 }}</span>
          </div>
          <div class="content-item" v-if="config.similarityThreshold">
            <span class="config-key">相似度阈值:</span>
            <span class="config-value">{{ (config.similarityThreshold * 100).toFixed(0) }}%</span>
          </div>
        </div>
        
        <!-- 情绪识别节点 -->
        <div v-else-if="nodeType === 'emotion_recognition'" class="node-specific emotion-content">
          <div class="content-item" v-if="config.modelType">
            <span class="config-key">模型:</span>
            <span class="config-value">{{ formatModelType(config.modelType) }}</span>
          </div>
          <div class="emotion-tags" v-if="config.emotionCategories?.length">
            <span 
              v-for="emotion in config.emotionCategories.slice(0, 4)" 
              :key="emotion"
              class="emotion-tag"
            >
              {{ formatEmotion(emotion) }}
            </span>
            <span v-if="config.emotionCategories.length > 4" class="emotion-more">
              +{{ config.emotionCategories.length - 4 }}
            </span>
          </div>
        </div>
        
        <!-- 文本处理节点 -->
        <div v-else-if="nodeType === 'text_process'" class="node-specific text-content">
          <div class="feature-list">
            <span v-if="config.trimSpaces" class="feature-tag">去空格</span>
            <span v-if="config.removeEmoji" class="feature-tag">去表情</span>
            <span v-if="config.normalizeWhitespace" class="feature-tag">规范化</span>
            <span v-if="config.maxLength" class="feature-tag">限{{ config.maxLength }}字</span>
          </div>
        </div>
        
        <!-- 后处理节点 -->
        <div v-else-if="nodeType === 'post_process'" class="node-specific post-content">
          <div class="feature-list">
            <span v-if="config.formatResponse" class="feature-tag">格式化</span>
            <span v-if="config.addDisclaimer" class="feature-tag">免责声明</span>
            <span v-if="config.logResponse" class="feature-tag">记录日志</span>
          </div>
        </div>
        
        <!-- 通用配置预览 -->
        <div class="config-preview" v-else-if="hasConfig">
          <div 
            v-for="(value, key) in configPreview" 
            :key="key"
            class="config-item"
          >
            <span class="config-key">{{ formatKey(key) }}:</span>
            <span class="config-value">{{ value }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 右侧输出端口 -->
    <div class="handles-right" v-if="outputs.length > 0">
      <div 
        v-for="(output, idx) in outputs" 
        :key="output.id"
        class="handle-wrapper output-handle"
        :style="{ top: getPortPosition(idx, outputs.length) }"
      >
        <span class="handle-label" v-if="outputs.length > 1 || showHandleLabels">{{ output.name }}</span>
        <Handle
          type="source"
          :position="Position.Right"
          :id="output.id"
          class="port output-port"
        />
      </div>
    </div>
    
    <!-- 选中光晕 -->
    <div v-if="selected" class="selection-glow" :style="glowStyle"></div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { Handle, Position } from '@vue-flow/core'
import { 
  Check, Warning, Promotion, Finished, Document, 
  Filter, ChatDotRound, Search, EditPen, Setting,
  User, CircleCheck, CircleClose
} from '@element-plus/icons-vue'
import { llmModelApi } from '@/api/llm'
import { knowledgeApi } from '@/api/knowledge'

// VueFlow 传递给自定义节点的 props
const props = defineProps({
  id: String,
  data: { type: Object, default: () => ({}) },
  selected: { type: Boolean, default: false }
})

// 全局缓存（在模块级别，避免重复加载）
const modelCache = ref(new Map())
const kbCache = ref(new Map())
let modelsLoaded = false
let kbsLoaded = false

// 加载模型列表
const loadModels = async () => {
  if (modelsLoaded) return
  try {
    const res = await llmModelApi.getModels({ page: 1, size: 100 })
    const models = Array.isArray(res) ? res : (res.list || res.data || [])
    models.forEach(model => {
      if (model && model.id) {
        modelCache.value.set(model.id, model.displayName || model.display_name || model.name || model.id)
      }
    })
    modelsLoaded = true
  } catch (err) {
    console.warn('加载模型列表失败:', err)
  }
}

// 加载知识库列表
const loadKnowledgeBases = async () => {
  if (kbsLoaded) return
  try {
    const res = await knowledgeApi.getKnowledgeBases({ page: 1, size: 100 })
    const kbs = Array.isArray(res) ? res : (res.list || res.data || [])
    kbs.forEach(kb => {
      if (kb && kb.id) {
        kbCache.value.set(kb.id, kb.name || kb.id)
      }
    })
    kbsLoaded = true
  } catch (err) {
    console.warn('加载知识库列表失败:', err)
  }
}

// 获取模型显示名称
const getModelDisplayName = (modelId) => {
  if (!modelId) return ''
  return modelCache.value.get(modelId) || modelId
}

// 获取知识库显示名称
const getKbDisplayName = (kbId) => {
  if (!kbId) return ''
  return kbCache.value.get(kbId) || kbId
}

// 组件挂载时预加载数据
onMounted(() => {
  loadModels()
  loadKnowledgeBases()
})

// 从 data 中解析节点属性
const label = computed(() => props.data?.label || '节点')
const nodeType = computed(() => props.data?.nodeType || 'default')
const icon = computed(() => props.data?.icon || 'Setting')
const color = computed(() => props.data?.color || '#7EB8C9')
const inputs = computed(() => props.data?.inputs || [])
const outputs = computed(() => props.data?.outputs || [])
const configured = computed(() => props.data?.configured || false)
const description = computed(() => props.data?.description || '')
const config = computed(() => props.data?.config || {})

// 节点类型标签映射
const nodeTypeLabels = {
  start: '开始节点',
  end: '结束节点',
  text_process: '文本处理',
  safety_check: '安全检测',
  sensitive_word_check: '敏感词检测',
  emotion_recognition: '情绪识别',
  crisis_intervention: '危机干预',
  rag_retrieval: '知识检索',
  llm_process: 'LLM处理',
  llm_generation: 'LLM生成',
  post_process: '回复优化'
}

const nodeTypeLabel = computed(() => nodeTypeLabels[nodeType.value] || nodeType.value)

// 是否是控制节点
const isControlNode = computed(() => ['start', 'end'].includes(nodeType.value))

// 是否显示端口标签
const showHandleLabels = computed(() => {
  return outputs.value.length > 1 || inputs.value.length > 1
})

// 是否需要配置
const needsConfig = computed(() => {
  if (isControlNode.value) return false
  return !configured.value && Object.keys(config.value || {}).length === 0
})

// 是否显示内容区域
const showBody = computed(() => {
  return description.value || hasConfig.value || 
         ['start', 'end', 'safety_check', 'llm_process', 'rag_retrieval', 
          'emotion_recognition', 'text_process', 'post_process'].includes(nodeType.value)
})

// 是否有配置
const hasConfig = computed(() => {
  return config.value && Object.keys(config.value).length > 0
})

// 配置预览（只显示前3项）
const configPreview = computed(() => {
  if (!hasConfig.value) return {}
  const entries = Object.entries(config.value).slice(0, 3)
  return Object.fromEntries(
    entries.map(([key, value]) => {
      let displayValue = value
      // 将模型ID和知识库ID转换为显示名称
      if (key === 'modelId' || key === 'model_id') {
        displayValue = getModelDisplayName(value)
      } else if (key === 'knowledgeBaseId' || key === 'kb_id' || key === 'kbId') {
        displayValue = getKbDisplayName(value)
      } else if (typeof value === 'object') {
        displayValue = Array.isArray(value) ? `[${value.length}项]` : '{...}'
      } else if (typeof value === 'string' && value.length > 20) {
        displayValue = value.substring(0, 20) + '...'
      } else if (typeof value === 'boolean') {
        displayValue = value ? '是' : '否'
      }
      return [key, displayValue]
    })
  )
})

// 计算端口位置
const getPortPosition = (index, total) => {
  if (total === 1) return '50%'
  const padding = 25 // 上下边距百分比
  const range = 100 - padding * 2
  const step = range / (total - 1)
  return `${padding + step * index}%`
}

// 节点样式
const nodeStyle = computed(() => ({
  '--node-color': color.value,
  '--node-color-light': color.value + '20',
  '--node-color-medium': color.value + '35',
  '--node-color-border': color.value + '50',
  '--node-color-bg': color.value + '12'
}))

// 头部样式
const headerStyle = computed(() => ({
  background: `linear-gradient(135deg, ${color.value}25 0%, ${color.value}15 100%)`
}))

// 图标样式
const iconStyle = computed(() => ({
  background: color.value + '30',
  color: color.value
}))

// 光晕样式
const glowStyle = computed(() => ({
  boxShadow: `0 0 0 3px ${color.value}30, 0 0 16px ${color.value}20`
}))

// 格式化函数
const formatKey = (key) => {
  const keyMap = {
    checkLevel: '检测级别',
    enableCrisisIntervention: '危机干预',
    modelId: '模型',
    temperature: '温度',
    maxTokens: '最大Token',
    topK: '返回数量',
    knowledgeBaseId: '知识库'
  }
  return keyMap[key] || key.replace(/_/g, ' ').replace(/([A-Z])/g, ' $1')
}

const formatCheckLevel = (level) => {
  const map = { loose: '宽松', standard: '标准', strict: '严格' }
  return map[level] || level
}

const formatModelType = (type) => {
  const map = { bert: 'BERT', lstm: 'LSTM', rules: '规则' }
  return map[type] || type
}

const formatEmotion = (emotion) => {
  const map = {
    happy: '😊开心', sad: '😢悲伤', angry: '😠愤怒',
    anxious: '😰焦虑', neutral: '😐平静', fear: '😨恐惧'
  }
  return map[emotion] || emotion
}

const truncateText = (text, maxLen) => {
  if (!text) return ''
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}
</script>

<style scoped>
.workflow-node {
  position: relative;
  min-width: 220px;
  max-width: 280px;
  font-family: 'Nunito', 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 控制节点样式 */
.workflow-node.control-node {
  min-width: 180px;
  max-width: 200px;
}

.node-container {
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(8px);
  border: 2px solid var(--node-color-border);
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.05), 0 1px 4px rgba(0, 0, 0, 0.03);
  transition: all 0.25s ease;
}

.workflow-node:hover .node-container {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08), 0 3px 8px rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
  border-color: var(--node-color);
}

.workflow-node.selected .node-container {
  border-color: var(--node-color);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

/* 头部 */
.node-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-bottom: 1px solid var(--node-color-medium);
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  flex-shrink: 0;
}

.header-icon .el-icon {
  font-size: 18px;
}

.header-content {
  flex: 1;
  min-width: 0;
}

.node-title {
  display: block;
  font-size: 14px;
  font-weight: 700;
  color: #3A3A3A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.node-type {
  display: block;
  font-size: 11px;
  color: #7A7A7A;
  margin-top: 2px;
  font-weight: 500;
}

.config-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  background: rgba(123, 160, 91, 0.2);
  color: #7BA05B;
  border-radius: 50%;
  flex-shrink: 0;
}

.config-warning {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  background: rgba(232, 196, 154, 0.3);
  color: #D4A574;
  border-radius: 50%;
  flex-shrink: 0;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

.config-badge .el-icon,
.config-warning .el-icon {
  font-size: 12px;
}

/* 内容区域 */
.node-body {
  padding: 12px 14px;
  background: var(--node-color-bg);
}

.node-description {
  font-size: 12px;
  color: #5A5A5A;
  line-height: 1.5;
}

/* 节点特定内容 */
.node-specific {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.content-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: #5A5A5A;
}

.content-item .el-icon {
  font-size: 13px;
  color: var(--node-color);
}

.content-item .config-key {
  color: #7A7A7A;
  font-weight: 500;
}

.content-item .config-value {
  color: #3A3A3A;
  font-weight: 600;
}

/* 安全检测输出标签 */
.output-labels {
  display: flex;
  gap: 8px;
  margin-top: 6px;
}

.output-label {
  font-size: 10px;
  padding: 3px 8px;
  border-radius: 4px;
  font-weight: 600;
}

.output-label.safe {
  background: rgba(123, 160, 91, 0.15);
  color: #7BA05B;
}

.output-label.crisis {
  background: rgba(212, 165, 116, 0.15);
  color: #D4A574;
}

/* LLM提示预览 */
.prompt-preview {
  margin-top: 4px;
  padding: 6px 8px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 6px;
  font-size: 10px;
}

.prompt-label {
  display: block;
  color: #7A7A7A;
  margin-bottom: 2px;
  font-weight: 500;
}

.prompt-text {
  color: #5A5A5A;
  line-height: 1.4;
}

/* 情绪标签 */
.emotion-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.emotion-tag {
  font-size: 10px;
  padding: 2px 6px;
  background: rgba(126, 184, 201, 0.15);
  border-radius: 4px;
  color: #5A7A8A;
}

.emotion-more {
  font-size: 10px;
  color: #7A7A7A;
}

/* 功能标签列表 */
.feature-list {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.feature-tag {
  font-size: 10px;
  padding: 3px 7px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--node-color-medium);
  border-radius: 4px;
  color: #5A5A5A;
  font-weight: 500;
}

/* 配置预览 */
.config-preview {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.config-item {
  display: flex;
  gap: 6px;
  font-size: 11px;
  padding: 4px 8px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 6px;
}

.config-item .config-key {
  color: #7A7A7A;
  font-weight: 500;
}

.config-item .config-value {
  color: #3A3A3A;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 端口容器 */
.handles-left,
.handles-right {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 0;
  z-index: 10;
}

.handles-left {
  left: 0;
}

.handles-right {
  right: 0;
}

.handle-wrapper {
  position: absolute;
  display: flex;
  align-items: center;
  transform: translateY(-50%);
}

.input-handle {
  left: -10px;
  flex-direction: row;
}

.output-handle {
  right: -10px;
  flex-direction: row-reverse;
}

.handle-label {
  font-size: 9px;
  color: #7A7A7A;
  background: rgba(255, 255, 255, 0.9);
  padding: 2px 5px;
  border-radius: 3px;
  white-space: nowrap;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.input-handle .handle-label {
  margin-left: 6px;
}

.output-handle .handle-label {
  margin-right: 6px;
}

/* 端口样式 */
.port {
  position: relative !important;
  width: 14px !important;
  height: 14px !important;
  border-radius: 50% !important;
  background: #ffffff !important;
  border: 3px solid var(--node-color) !important;
  transition: all 0.2s ease !important;
  cursor: crosshair !important;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1) !important;
}

.port:hover {
  transform: scale(1.25) !important;
  box-shadow: 0 0 0 4px var(--node-color-medium), 0 2px 8px rgba(0, 0, 0, 0.12) !important;
}

/* 选中光晕 */
.selection-glow {
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border-radius: 18px;
  pointer-events: none;
  animation: glow-pulse 2s ease-in-out infinite;
}

@keyframes glow-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

/* 辅助样式 */
.text-success {
  color: #7BA05B !important;
}

.text-muted {
  color: #B5B5B5 !important;
}
</style>
