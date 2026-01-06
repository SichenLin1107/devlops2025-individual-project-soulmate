<template>
  <div class="execution-panel">
    <!-- 输入区域 -->
    <div class="input-section">
      <div class="section-header">
        <el-icon><EditPen /></el-icon>
        <span>测试输入</span>
      </div>
      
      <div class="input-form">
        <div class="form-field">
          <label class="field-label">用户消息</label>
          <textarea
            v-model="inputData.userInput"
            class="message-input"
            rows="4"
            placeholder="输入测试消息，模拟用户发送的内容..."
          ></textarea>
        </div>
        
        <div class="form-field">
          <label class="field-label">会话 ID <span class="optional">(可选)</span></label>
          <input
            v-model="inputData.sessionId"
            type="text"
            class="field-input"
            placeholder="留空将自动生成"
          />
        </div>
        
        <button 
          class="run-btn" 
          :class="{ loading: running }"
          :disabled="!canRun || running"
          @click="runTest"
        >
          <el-icon v-if="!running"><VideoPlay /></el-icon>
          <el-icon v-else class="is-loading"><Loading /></el-icon>
          <span>{{ running ? '运行中...' : '开始运行' }}</span>
        </button>
        
        <p v-if="!workflowId" class="warning-hint">
          <el-icon><Warning /></el-icon>
          请先保存工作流后再运行测试
        </p>
      </div>
    </div>

    <!-- 结果区域 -->
    <div class="result-section" v-if="result">
      <div class="section-header">
        <el-icon><DataAnalysis /></el-icon>
        <span>运行结果</span>
        <span class="execution-time" v-if="result.duration">
          {{ result.duration }}ms
        </span>
      </div>
      
      <!-- 状态摘要 -->
      <div class="result-summary">
        <div class="summary-card" :class="result.isCrisis ? 'danger' : 'success'">
          <div class="card-icon">
            <el-icon><Warning v-if="result.isCrisis" /><CircleCheck v-else /></el-icon>
          </div>
          <div class="card-content">
            <span class="card-label">安全状态</span>
            <span class="card-value">{{ result.isCrisis ? '发现风险' : '安全通过' }}</span>
          </div>
        </div>
        
        <div class="summary-card info" v-if="result.emotion">
          <div class="card-icon">
            <el-icon><ChatDotRound /></el-icon>
          </div>
          <div class="card-content">
            <span class="card-label">情绪识别</span>
            <span class="card-value">{{ emotionLabel }}</span>
          </div>
        </div>
      </div>

      <!-- 回复内容 -->
      <div class="response-box">
        <div class="box-header">
          <el-icon><ChatLineSquare /></el-icon>
          <span>AI 回复</span>
        </div>
        <div class="response-content">
          {{ result.response || '(无回复内容)' }}
        </div>
      </div>

      <!-- 执行路径 -->
      <div class="execution-path" v-if="result.nodesExecuted?.length">
        <div class="box-header">
          <el-icon><Connection /></el-icon>
          <span>执行路径</span>
        </div>
        <div class="path-timeline">
          <div 
            v-for="(nodeId, idx) in result.nodesExecuted" 
            :key="idx"
            class="path-node"
            :class="{ first: idx === 0, last: idx === result.nodesExecuted.length - 1 }"
          >
            <div class="node-dot"></div>
            <div class="node-line" v-if="idx < result.nodesExecuted.length - 1"></div>
            <div class="node-info">
              <span class="node-name">{{ getNodeName(nodeId) }}</span>
              <span class="node-id">#{{ nodeId }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 详细数据 -->
      <div class="raw-data">
        <button class="toggle-btn" @click="showRawData = !showRawData">
          <el-icon :class="{ rotated: showRawData }"><ArrowDown /></el-icon>
          <span>{{ showRawData ? '收起详细数据' : '查看详细数据' }}</span>
        </button>
        
        <transition name="expand">
          <div v-if="showRawData" class="data-content">
            <pre>{{ JSON.stringify(result, null, 2) }}</pre>
          </div>
        </transition>
      </div>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-else>
      <el-icon :size="48"><Monitor /></el-icon>
      <p>运行工作流后在此查看结果</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { workflowApi } from '@/api/workflow'
import { ElMessage } from 'element-plus'
import {
  EditPen, VideoPlay, Loading, Warning, DataAnalysis,
  CircleCheck, ChatDotRound, ChatLineSquare, Connection,
  ArrowDown, Monitor
} from '@element-plus/icons-vue'

const props = defineProps({
  workflowId: String,
  nodesConfig: Object
})

// 输入数据
const inputData = ref({
  userInput: '你好，我今天心情不太好。',
  sessionId: '',
  agentId: 'debug-agent'
})

// 运行状态
const running = ref(false)
const result = ref(null)
const showRawData = ref(false)

// 是否可以运行
const canRun = computed(() => !!props.workflowId)

// 情绪标签映射
const emotionLabels = {
  happy: '😊 开心',
  sad: '😢 悲伤',
  angry: '😠 愤怒',
  anxious: '😰 焦虑',
  neutral: '😐 平静',
  fear: '😨 恐惧',
  surprise: '😲 惊讶',
  disgust: '😤 厌恶'
}

const emotionLabel = computed(() => {
  const emotion = result.value?.emotion
  return emotionLabels[emotion] || emotion || '未识别'
})

// 获取节点名称
const getNodeName = (nodeId) => {
  if (!props.nodesConfig?.nodes) return nodeId
  const node = props.nodesConfig.nodes.find(n => n.id === nodeId)
  return node?.data?.label || nodeId
}

// 运行测试
const runTest = async () => {
  if (!props.workflowId) {
    ElMessage.warning('请先保存工作流后再运行测试')
    return
  }
  
  running.value = true
  result.value = null
  showRawData.value = false
  
  try {
    const startTime = Date.now()
    const res = await workflowApi.executeWorkflow(props.workflowId, {
      ...inputData.value,
      sessionId: inputData.value.sessionId || `debug-${Date.now()}`
    })
    
    result.value = {
      ...res,
      duration: Date.now() - startTime
    }
    
    ElMessage.success('运行完成')
  } catch (err) {
    console.error('运行失败:', err)
    ElMessage.error('运行失败: ' + (err.message || '系统错误'))
    
    // 模拟结果用于展示
    result.value = {
      error: err.message || '执行失败',
      response: null,
      emotion: null,
      isCrisis: false,
      nodesExecuted: [],
      duration: 0
    }
  } finally {
    running.value = false
  }
}
</script>

<style scoped>
.execution-panel {
  display: flex;
  flex-direction: column;
  gap: 28px;
  padding: 22px;
  height: 100%;
  overflow-y: auto;
  font-family: 'Nunito', 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 区块头部 */
.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 700;
  color: var(--el-text-color-primary, #3A3A3A);
  margin-bottom: 18px;
}

.section-header .el-icon {
  color: var(--el-color-primary, #4A90A4);
}

.execution-time {
  margin-left: auto;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-color-success, #7BA05B);
  background: rgba(123, 160, 91, 0.12);
  padding: 4px 10px;
  border-radius: 6px;
}

/* 输入区域 */
.input-section {
  background: var(--warm-beige, #F5F1EB);
  border-radius: 14px;
  padding: 22px;
}

.input-form {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #7A7A7A);
}

.optional {
  font-weight: 400;
  color: var(--el-text-color-placeholder, #B5B5B5);
}

.message-input,
.field-input {
  width: 100%;
  padding: 14px;
  border: 1px solid var(--el-border-color, #E8E8E8);
  border-radius: 12px;
  font-size: 14px;
  color: var(--el-text-color-primary, #3A3A3A);
  background: #ffffff;
  font-family: inherit;
  transition: all 0.2s;
}

.message-input:focus,
.field-input:focus {
  outline: none;
  border-color: var(--el-color-primary, #4A90A4);
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.12);
}

.message-input {
  resize: vertical;
  min-height: 110px;
}

/* 运行按钮 */
.run-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 24px;
  background: linear-gradient(135deg, var(--el-color-success, #7BA05B) 0%, #6B8F4B 100%);
  color: #ffffff;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.25s;
  font-family: inherit;
}

.run-btn:hover:not(:disabled) {
  box-shadow: 0 6px 20px rgba(123, 160, 91, 0.35);
  transform: translateY(-2px);
}

.run-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.run-btn.loading {
  background: var(--warm-gray, #A8998A);
}

.is-loading {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.warning-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--el-color-warning, #D4A574);
  margin: 0;
}

/* 结果摘要 */
.result-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
  margin-bottom: 22px;
}

.summary-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: var(--warm-beige, #F5F1EB);
  border-radius: 12px;
  border: 1px solid var(--gentle-sand, #E8E2D5);
}

.summary-card.success {
  background: rgba(123, 160, 91, 0.1);
  border-color: rgba(123, 160, 91, 0.25);
}

.summary-card.success .card-icon {
  background: var(--el-color-success, #7BA05B);
}

.summary-card.danger {
  background: rgba(192, 123, 123, 0.1);
  border-color: rgba(192, 123, 123, 0.25);
}

.summary-card.danger .card-icon {
  background: var(--el-color-danger, #C07B7B);
}

.summary-card.info {
  background: rgba(74, 144, 164, 0.08);
  border-color: rgba(74, 144, 164, 0.2);
}

.summary-card.info .card-icon {
  background: var(--el-color-primary, #4A90A4);
}

.card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--warm-gray, #A8998A);
  border-radius: 10px;
  color: #ffffff;
  flex-shrink: 0;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.card-label {
  font-size: 11px;
  color: var(--el-text-color-secondary, #7A7A7A);
  font-weight: 500;
}

.card-value {
  font-size: 14px;
  font-weight: 700;
  color: var(--el-text-color-primary, #3A3A3A);
}

/* 回复框 */
.response-box {
  margin-bottom: 22px;
}

.box-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 700;
  color: var(--el-text-color-secondary, #7A7A7A);
  margin-bottom: 12px;
}

.response-content {
  padding: 18px;
  background: var(--warm-beige, #F5F1EB);
  border-radius: 14px;
  border: 1px solid var(--gentle-sand, #E8E2D5);
  font-size: 14px;
  line-height: 1.75;
  color: var(--el-text-color-primary, #3A3A3A);
  white-space: pre-wrap;
}

/* 执行路径 */
.execution-path {
  margin-bottom: 22px;
}

.path-timeline {
  display: flex;
  flex-direction: column;
}

.path-node {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  position: relative;
  padding-left: 22px;
}

.node-dot {
  position: absolute;
  left: 0;
  top: 5px;
  width: 14px;
  height: 14px;
  background: var(--el-color-primary, #4A90A4);
  border: 3px solid #ffffff;
  border-radius: 50%;
  box-shadow: 0 0 0 2px var(--el-color-primary, #4A90A4);
  z-index: 2;
}

.path-node.first .node-dot {
  background: var(--el-color-success, #7BA05B);
  box-shadow: 0 0 0 2px var(--el-color-success, #7BA05B);
}

.path-node.last .node-dot {
  background: var(--el-color-danger, #C07B7B);
  box-shadow: 0 0 0 2px var(--el-color-danger, #C07B7B);
}

.node-line {
  position: absolute;
  left: 6px;
  top: 19px;
  width: 2px;
  height: calc(100% + 10px);
  background: var(--gentle-sand, #E8E2D5);
  z-index: 1;
}

.node-info {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 10px 0;
}

.node-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary, #3A3A3A);
}

.node-id {
  font-size: 11px;
  color: var(--el-text-color-placeholder, #B5B5B5);
  font-family: 'JetBrains Mono', 'Consolas', monospace;
}

/* 详细数据 */
.raw-data {
  border-top: 1px solid var(--el-border-color-lighter, #F8F8F8);
  padding-top: 18px;
}

.toggle-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  background: transparent;
  border: none;
  font-size: 13px;
  color: var(--el-text-color-secondary, #7A7A7A);
  cursor: pointer;
  padding: 10px 14px;
  border-radius: 8px;
  transition: all 0.2s;
  font-family: inherit;
}

.toggle-btn:hover {
  background: var(--warm-beige, #F5F1EB);
  color: var(--el-text-color-primary, #3A3A3A);
}

.toggle-btn .el-icon {
  transition: transform 0.25s;
}

.toggle-btn .el-icon.rotated {
  transform: rotate(180deg);
}

.data-content {
  margin-top: 14px;
}

.data-content pre {
  padding: 18px;
  background: #2E3440;
  border-radius: 12px;
  font-size: 11px;
  color: #ECEFF4;
  overflow-x: auto;
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  line-height: 1.7;
}

/* 展开动画 */
.expand-enter-active,
.expand-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}

.expand-enter-from,
.expand-leave-to {
  opacity: 0;
  max-height: 0;
}

.expand-enter-to,
.expand-leave-from {
  opacity: 1;
  max-height: 500px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 70px 24px;
  color: var(--el-text-color-placeholder, #B5B5B5);
  text-align: center;
}

.empty-state .el-icon {
  margin-bottom: 18px;
  color: var(--cozy-taupe, #D4C4B0);
}

.empty-state p {
  margin: 0;
  font-size: 15px;
}
</style>
