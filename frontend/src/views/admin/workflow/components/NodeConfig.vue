<template>
  <div class="node-config">
    <!-- 节点类型标识 -->
    <div class="node-type-badge" :style="{ '--badge-color': node?.data?.color || '#4A90A4' }">
      <el-icon><component :is="node?.data?.icon || 'Setting'" /></el-icon>
      <span>{{ nodeTypeLabel }}</span>
    </div>

    <!-- 基本信息 -->
    <div class="config-section">
      <label class="section-label">基本信息</label>
      
      <div class="form-field">
        <label class="field-label">节点名称</label>
        <input
          v-if="!isControlNode"
          v-model="formData.name"
          type="text"
          class="field-input"
          placeholder="请输入节点名称"
          @input="markNameChanged"
        />
        <div v-else class="field-value-readonly">{{ formData.name }}</div>
      </div>
      
      <div class="form-field" v-if="node?.data?.description">
        <label class="field-label">功能说明</label>
        <p class="field-description">{{ node.data.description }}</p>
      </div>

      <!-- 端口信息 -->
      <div class="ports-info" v-if="hasPortInfo">
        <div class="ports-row" v-if="node?.data?.inputs?.length">
          <span class="ports-label">输入:</span>
          <span class="port-tag" v-for="input in node.data.inputs" :key="input.id">
            {{ input.name }}
          </span>
        </div>
        <div class="ports-row" v-if="node?.data?.outputs?.length">
          <span class="ports-label">输出:</span>
          <span class="port-tag" v-for="output in node.data.outputs" :key="output.id">
            {{ output.name }}
          </span>
        </div>
      </div>
    </div>

    <!-- 开始/结束节点的特殊提示 -->
    <div class="config-section" v-if="isControlNode">
      <div class="control-node-hint">
        <el-icon :size="40" class="hint-icon">
          <component :is="node?.data?.nodeType === 'start' ? 'Promotion' : 'Finished'" />
        </el-icon>
        <h4>{{ node?.data?.nodeType === 'start' ? '开始节点' : '结束节点' }}</h4>
        <p>{{ node?.data?.nodeType === 'start' ? '工作流的起点，接收用户输入的消息' : '工作流的终点，输出最终回复给用户' }}</p>
        <p class="hint-note">此节点无需配置</p>
      </div>
    </div>

    <!-- 配置参数 -->
    <div class="config-section" v-else-if="hasConfigSchema">
      <label class="section-label">
        配置参数
        <span class="required-hint" v-if="hasRequiredFields">* 标记为必填项</span>
      </label>
      
      <div 
        v-for="(field, key) in definition?.configSchema" 
        :key="key" 
        class="form-field"
      >
        <label class="field-label">
          {{ field.label || formatKey(key) }}
          <span v-if="field.required" class="required-mark">*</span>
        </label>
        
        <!-- 知识库 / 模型 下拉选择（显示名称，保存ID） -->
        <template v-if="shouldShowAsSelect(key, field)">
          <div class="select-wrapper">
            <select 
              v-model="formData.config[key]" 
              class="field-select"
              @focus="handleSelectFocus(key)"
            >
              <option value="" disabled>
                {{ isLoadingOptions(key) ? '加载中...' : '请选择...' }}
              </option>
              <option 
                v-for="opt in getFieldOptions(key)" 
                :key="opt.value" 
                :value="opt.value"
              >
                {{ opt.label }}
              </option>
            </select>
            <el-icon class="select-arrow"><ArrowDown /></el-icon>
          </div>
        </template>
        
        <!-- 字符串输入 -->
        <template v-else-if="field.type === 'string'">
          <textarea
            v-if="field.format === 'textarea'"
            v-model="formData.config[key]"
            class="field-textarea"
            :rows="field.rows || 4"
            :placeholder="field.description || field.placeholder"
          />
          <input
            v-else
            v-model="formData.config[key]"
            type="text"
            class="field-input"
            :placeholder="field.description || field.placeholder"
          />
        </template>
        
        <!-- 数字输入 -->
        <template v-else-if="field.type === 'number' || field.type === 'integer'">
          <div class="number-input-wrapper">
            <button 
              class="number-btn" 
              @click="decrementNumber(key, field)"
              :disabled="formData.config[key] <= (field.min ?? -Infinity)"
            >
              <el-icon><Minus /></el-icon>
            </button>
            <input
              v-model.number="formData.config[key]"
              type="number"
              class="field-input number-input"
              :min="field.min"
              :max="field.max"
              :step="field.step || 1"
            />
            <button 
              class="number-btn" 
              @click="incrementNumber(key, field)"
              :disabled="formData.config[key] >= (field.max ?? Infinity)"
            >
              <el-icon><Plus /></el-icon>
            </button>
          </div>
        </template>
        
        <!-- 布尔开关 -->
        <template v-else-if="field.type === 'boolean'">
          <div class="switch-wrapper">
            <button 
              class="toggle-switch" 
              :class="{ active: formData.config[key] }"
              @click="formData.config[key] = !formData.config[key]"
            >
              <span class="toggle-thumb"></span>
            </button>
            <span class="switch-label">{{ formData.config[key] ? '启用' : '禁用' }}</span>
          </div>
        </template>
        
        <!-- 下拉选择 -->
        <template v-else-if="field.options">
          <div class="select-wrapper">
            <select v-model="formData.config[key]" class="field-select">
              <option value="" disabled>请选择...</option>
              <option 
                v-for="opt in field.options" 
                :key="opt.value" 
                :value="opt.value"
              >
                {{ opt.label }}
              </option>
            </select>
            <el-icon class="select-arrow"><ArrowDown /></el-icon>
          </div>
        </template>
        
        <!-- 数组类型 - 带 options 时显示为多选复选框 -->
        <template v-else-if="field.type === 'array' && field.options">
          <div class="checkbox-group">
            <label 
              v-for="opt in field.options" 
              :key="opt.value"
              class="checkbox-item"
              :class="{ checked: (formData.config[key] || []).includes(opt.value) }"
            >
              <input 
                type="checkbox"
                :checked="(formData.config[key] || []).includes(opt.value)"
                @change="toggleArrayOption(key, opt.value)"
              />
              <span class="checkbox-mark"></span>
              <span class="checkbox-label">{{ opt.label }}</span>
            </label>
          </div>
        </template>
        
        <!-- 数组类型 - 无 options 时显示为标签输入 -->
        <template v-else-if="field.type === 'array'">
          <div class="tags-input">
            <div class="tags-list">
              <span 
                v-for="(item, idx) in (formData.config[key] || [])" 
                :key="idx"
                class="tag-item"
              >
                {{ item }}
                <button class="tag-remove" @click="removeArrayItem(key, idx)">
                  <el-icon><Close /></el-icon>
                </button>
              </span>
            </div>
            <input
              v-model="tempArrayInput[key]"
              type="text"
              class="tag-input"
              :placeholder="field.placeholder || '输入后按回车添加'"
              @keydown.enter.prevent="addArrayItem(key)"
            />
          </div>
        </template>
        
        <!-- JSON/对象输入 -->
        <template v-else-if="field.type === 'object' || field.type === 'map'">
          <div class="json-editor">
            <textarea
              v-model="jsonStrings[key]"
              class="field-textarea json-input"
              :rows="6"
              placeholder="请输入 JSON 格式数据"
              @blur="updateJsonField(key)"
            />
            <div v-if="jsonErrors[key]" class="json-error">
              <el-icon><Warning /></el-icon>
              {{ jsonErrors[key] }}
            </div>
          </div>
        </template>
        
        <!-- 默认文本输入 -->
        <template v-else>
          <input
            v-model="formData.config[key]"
            type="text"
            class="field-input"
            :placeholder="field.description"
          />
        </template>
        
        <!-- 字段描述 -->
        <p v-if="field.description" class="field-hint">{{ field.description }}</p>
      </div>
    </div>

    <!-- 无配置提示（非控制节点但无配置schema时显示） -->
    <div v-else-if="!isControlNode" class="no-config-hint">
      <el-icon :size="36"><Setting /></el-icon>
      <p>该节点无需额外配置</p>
    </div>

    <!-- 操作按钮 -->
    <div class="config-actions" v-if="!isControlNode">
      <button class="action-btn primary" @click="handleSave">
        <el-icon><Check /></el-icon>
        应用配置
      </button>
      <button 
        v-if="canDelete" 
        class="action-btn danger" 
        @click="handleDelete"
      >
        <el-icon><Delete /></el-icon>
        删除节点
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  Plus, Minus, ArrowDown, Close, Warning, 
  Setting, Check, Delete, Promotion, Finished,
  Document, Filter, ChatDotRound, Search, EditPen
} from '@element-plus/icons-vue'
import { knowledgeApi } from '@/api/knowledge'
import { llmModelApi } from '@/api/llm'

const props = defineProps({
  node: Object,
  definition: Object
})

const emit = defineEmits(['update', 'delete'])

// 下拉选项数据
const knowledgeBases = ref([])
const llmModels = ref([])
const loadingKnowledgeBases = ref(false)
const loadingModels = ref(false)

// 加载知识库列表
const loadKnowledgeBases = async () => {
  if (knowledgeBases.value.length > 0) return // 已加载过则不重复加载
  loadingKnowledgeBases.value = true
  try {
    const res = await knowledgeApi.getKnowledgeBases({ page: 1, size: 100 })
    // http 拦截器已经提取了 data 字段，但分页接口可能返回 PageResult，需要检查结构
    const kbs = Array.isArray(res) ? res : (res.list || res.data || [])
    knowledgeBases.value = kbs
      .filter(kb => kb && kb.name) // 只包含有名称的知识库
      .map(kb => ({
        value: kb.id,
        label: kb.name // 只使用名称，不使用ID
      }))
    console.log('加载知识库列表成功:', knowledgeBases.value.length, '个知识库')
  } catch (err) {
    console.error('加载知识库列表失败:', err)
    ElMessage.error('加载知识库列表失败: ' + (err.message || '未知错误'))
    knowledgeBases.value = []
  } finally {
    loadingKnowledgeBases.value = false
  }
}

// 加载LLM模型列表
const loadLlmModels = async () => {
  if (llmModels.value.length > 0) return // 已加载过则不重复加载
  loadingModels.value = true
  try {
    const res = await llmModelApi.getModels({ page: 1, size: 100 })
    // http 拦截器已经提取了 data 字段，所以 res 直接是数组
    const models = Array.isArray(res) ? res : (res.list || res.data || [])
    llmModels.value = models
      .filter(model => model && (model.displayName || model.display_name || model.name || model.id)) // 只包含有效的模型
      .map(model => ({
        value: model.id,
        label: model.displayName || model.display_name || model.name || model.id // 优先使用显示名称，否则使用ID
      }))
    console.log('加载模型列表成功:', llmModels.value.length, '个模型')
  } catch (err) {
    console.error('加载模型列表失败:', err)
    ElMessage.error('加载模型列表失败: ' + (err.message || '未知错误'))
    llmModels.value = []
  } finally {
    loadingModels.value = false
  }
}

// 判断是否是需要加载下拉数据的字段（同时支持驼峰和下划线命名）
const isKnowledgeBaseField = (key) => ['knowledgeBaseId', 'kb_id', 'kbId'].includes(key)
const isModelField = (key) => ['modelId', 'model_id'].includes(key)

// 获取字段的下拉选项
const getFieldOptions = (key) => {
  if (isKnowledgeBaseField(key)) return knowledgeBases.value
  if (isModelField(key)) return llmModels.value
  return []
}

// 是否正在加载下拉选项
const isLoadingOptions = (key) => {
  if (isKnowledgeBaseField(key)) return loadingKnowledgeBases.value
  if (isModelField(key)) return loadingModels.value
  return false
}

// 需要显示为下拉选择的字段
const shouldShowAsSelect = (key, field) => {
  return isKnowledgeBaseField(key) || isModelField(key)
}

// 表单数据
const formData = ref({
  name: '',
  config: {}
})

// 临时数组输入
const tempArrayInput = ref({})

// JSON 字符串缓存
const jsonStrings = ref({})
const jsonErrors = ref({})

// 节点类型标签
const nodeTypeLabels = {
  start: '开始节点',
  end: '结束节点',
  text_process: '文本处理',
  safety_check: '安全检测',
  emotion_recognition: '情绪识别',
  rag_retrieval: '知识检索',
  llm_process: 'LLM处理',
  post_process: '回复优化'
}

const nodeTypeLabel = computed(() => {
  const type = props.node?.data?.nodeType
  return nodeTypeLabels[type] || type || '节点配置'
})

// 是否有端口信息
const hasPortInfo = computed(() => {
  return (props.node?.data?.inputs?.length > 1) || (props.node?.data?.outputs?.length > 1)
})

// 是否有配置 schema
const hasConfigSchema = computed(() => {
  return props.definition?.configSchema && 
    Object.keys(props.definition.configSchema).length > 0
})

// 是否是开始/结束节点（控制节点不可编辑）
const isControlNode = computed(() => {
  const nodeType = props.node?.data?.nodeType
  return nodeType === 'start' || nodeType === 'end'
})

// 是否有必填字段
const hasRequiredFields = computed(() => {
  if (!props.definition?.configSchema) return false
  return Object.values(props.definition.configSchema).some(field => field.required)
})

// 是否可删除（开始和结束节点不可删除）
const canDelete = computed(() => {
  return props.node?.id !== 'start' && props.node?.id !== 'end'
})

// 监听节点变化，初始化表单
watch(() => props.node, async (newNode) => {
  if (newNode) {
    formData.value = {
      name: newNode.data?.label || '',
      config: JSON.parse(JSON.stringify(newNode.data?.config || {}))
    }
    
    // 初始化 JSON 字符串和预加载下拉数据
    if (props.definition?.configSchema) {
      let needLoadModels = false
      let needLoadKnowledgeBases = false
      
      Object.entries(props.definition.configSchema).forEach(([key, field]) => {
        if (field.type === 'object' || field.type === 'map') {
          const value = formData.value.config[key]
          jsonStrings.value[key] = value ? JSON.stringify(value, null, 2) : ''
        }
        
        // 检查是否需要加载下拉数据（无论是否有值都预加载，以便下拉框能正常显示）
        if (isModelField(key)) {
          needLoadModels = true
        } else if (isKnowledgeBaseField(key)) {
          needLoadKnowledgeBases = true
        }
      })
      
      // 预加载下拉数据
      if (needLoadModels) {
        loadLlmModels()
      }
      if (needLoadKnowledgeBases) {
        loadKnowledgeBases()
      }
    }
    
    // 清除错误
    jsonErrors.value = {}
  }
}, { immediate: true, deep: true })

// 格式化键名
const formatKey = (key) => {
  return key
    .replace(/_/g, ' ')
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, str => str.toUpperCase())
}

// 下拉选择框获得焦点时加载数据
const handleSelectFocus = (key) => {
  if (isKnowledgeBaseField(key)) {
    loadKnowledgeBases()
  } else if (isModelField(key)) {
    loadLlmModels()
  }
}

// 数字增减
const incrementNumber = (key, field) => {
  const step = field.step || 1
  const max = field.max ?? Infinity
  const current = formData.value.config[key] || 0
  formData.value.config[key] = Math.min(current + step, max)
}

const decrementNumber = (key, field) => {
  const step = field.step || 1
  const min = field.min ?? -Infinity
  const current = formData.value.config[key] || 0
  formData.value.config[key] = Math.max(current - step, min)
}

// 数组操作
const addArrayItem = (key) => {
  const value = tempArrayInput.value[key]?.trim()
  if (!value) return
  
  if (!formData.value.config[key]) {
    formData.value.config[key] = []
  }
  formData.value.config[key].push(value)
  tempArrayInput.value[key] = ''
}

const removeArrayItem = (key, idx) => {
  formData.value.config[key].splice(idx, 1)
}

// 多选数组操作
const toggleArrayOption = (key, value) => {
  if (!formData.value.config[key]) {
    formData.value.config[key] = []
  }
  const idx = formData.value.config[key].indexOf(value)
  if (idx >= 0) {
    formData.value.config[key].splice(idx, 1)
  } else {
    formData.value.config[key].push(value)
  }
}

// JSON 更新
const updateJsonField = (key) => {
  try {
    const value = jsonStrings.value[key]?.trim()
    if (value) {
      formData.value.config[key] = JSON.parse(value)
      jsonErrors.value[key] = null
    } else {
      formData.value.config[key] = null
    }
  } catch (e) {
    jsonErrors.value[key] = 'JSON 格式错误'
  }
}

// 标记名称已变更
const markNameChanged = () => {
  // 名称变更会在保存时一起提交，这里可以添加验证逻辑
  if (!formData.value.name || formData.value.name.trim() === '') {
    // 可以添加提示，但不阻止编辑
  }
}

// 保存配置
const handleSave = () => {
  // 检查 JSON 错误
  if (Object.values(jsonErrors.value).some(e => e)) {
    return
  }
  
  // 验证节点名称
  if (!formData.value.name || formData.value.name.trim() === '') {
    ElMessage.warning('请输入节点名称')
    return
  }
  
  emit('update', formData.value)
}

// 删除节点
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除节点「${formData.value.name}」吗？`,
      '删除确认',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger'
      }
    )
    emit('delete')
  } catch {
    // 取消删除
  }
}
</script>

<style scoped>
.node-config {
  display: flex;
  flex-direction: column;
  gap: 26px;
  font-family: 'Nunito', 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 节点类型标识 */
.node-type-badge {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: linear-gradient(135deg, var(--badge-color, #4A90A4) 0%, color-mix(in srgb, var(--badge-color, #4A90A4) 80%, white) 100%);
  border-radius: 12px;
  color: white;
  margin: -22px -22px 0 -22px;
  margin-bottom: 10px;
}

.node-type-badge .el-icon {
  font-size: 20px;
}

.node-type-badge span {
  font-size: 15px;
  font-weight: 700;
}

/* 端口信息 */
.ports-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: var(--warm-beige, #F5F1EB);
  border-radius: 10px;
  margin-top: 8px;
}

.ports-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.ports-label {
  font-size: 11px;
  color: #7A7A7A;
  font-weight: 600;
  min-width: 35px;
}

.port-tag {
  font-size: 10px;
  padding: 3px 8px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid var(--el-border-color, #E8E8E8);
  border-radius: 5px;
  color: #5A5A5A;
}

/* 只读字段值 */
.field-value-readonly {
  padding: 12px 14px;
  background: var(--warm-beige, #F5F1EB);
  border: 1px solid var(--el-border-color, #E8E8E8);
  border-radius: 10px;
  font-size: 13px;
  color: var(--el-text-color-primary, #3A3A3A);
  font-weight: 500;
}

/* 控制节点提示 */
.control-node-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
  background: linear-gradient(135deg, rgba(74, 144, 164, 0.05) 0%, rgba(74, 144, 164, 0.02) 100%);
  border-radius: 12px;
  border: 1px dashed var(--el-border-color, #E8E8E8);
}

.control-node-hint .hint-icon {
  color: var(--el-color-primary, #4A90A4);
  margin-bottom: 16px;
}

.control-node-hint h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary, #3A3A3A);
}

.control-node-hint p {
  margin: 0;
  font-size: 13px;
  color: var(--el-text-color-secondary, #7A7A7A);
  line-height: 1.6;
}

.control-node-hint .hint-note {
  margin-top: 16px;
  font-size: 12px;
  color: var(--el-text-color-placeholder, #B5B5B5);
  font-style: italic;
}

/* 配置区块 */
.config-section {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.section-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--el-text-color-secondary, #7A7A7A);
  text-transform: uppercase;
  letter-spacing: 0.6px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--el-border-color-lighter, #F8F8F8);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.required-hint {
  font-size: 10px;
  font-weight: 500;
  color: var(--el-color-danger, #C07B7B);
  text-transform: none;
  letter-spacing: 0;
}

/* 表单字段 */
.form-field {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.field-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary, #3A3A3A);
  display: flex;
  align-items: center;
  gap: 4px;
}

.required-mark {
  color: var(--el-color-danger, #C07B7B);
}

.field-description {
  font-size: 12px;
  color: var(--el-text-color-secondary, #7A7A7A);
  line-height: 1.6;
  margin: 0;
  padding: 10px 14px;
  background: var(--warm-beige, #F5F1EB);
  border-radius: 10px;
}

.field-hint {
  font-size: 11px;
  color: var(--el-text-color-placeholder, #B5B5B5);
  margin: 4px 0 0 0;
}

/* 输入框 - 使用项目主题 */
.field-input {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--el-border-color, #E8E8E8);
  border-radius: 10px;
  font-size: 13px;
  color: var(--el-text-color-primary, #3A3A3A);
  background: #ffffff;
  font-family: inherit;
  transition: all 0.2s;
}

.field-input:focus {
  outline: none;
  border-color: var(--el-color-primary, #4A90A4);
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.12);
}

.field-input::placeholder {
  color: var(--el-text-color-placeholder, #B5B5B5);
}

/* 文本域 */
.field-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--el-border-color, #E8E8E8);
  border-radius: 10px;
  font-size: 13px;
  color: var(--el-text-color-primary, #3A3A3A);
  background: #ffffff;
  resize: vertical;
  font-family: inherit;
  transition: all 0.2s;
}

.field-textarea:focus {
  outline: none;
  border-color: var(--el-color-primary, #4A90A4);
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.12);
}

/* 数字输入 */
.number-input-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.number-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid var(--el-border-color, #E8E8E8);
  background: var(--warm-beige, #F5F1EB);
  border-radius: 8px;
  color: var(--el-text-color-secondary, #7A7A7A);
  cursor: pointer;
  transition: all 0.2s;
}

.number-btn:hover:not(:disabled) {
  background: var(--gentle-sand, #E8E2D5);
  border-color: var(--cozy-taupe, #D4C4B0);
  color: var(--el-text-color-primary, #3A3A3A);
}

.number-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.number-input {
  text-align: center;
  flex: 1;
}

.number-input::-webkit-inner-spin-button,
.number-input::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* 开关 */
.switch-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toggle-switch {
  position: relative;
  width: 48px;
  height: 26px;
  background: var(--gentle-sand, #E8E2D5);
  border: none;
  border-radius: 13px;
  cursor: pointer;
  transition: all 0.25s;
}

.toggle-switch.active {
  background: var(--el-color-primary, #4A90A4);
}

.toggle-thumb {
  position: absolute;
  top: 3px;
  left: 3px;
  width: 20px;
  height: 20px;
  background: #ffffff;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
  transition: transform 0.25s;
}

.toggle-switch.active .toggle-thumb {
  transform: translateX(22px);
}

.switch-label {
  font-size: 13px;
  color: var(--el-text-color-secondary, #7A7A7A);
}

/* 下拉选择 */
.select-wrapper {
  position: relative;
}

.field-select {
  width: 100%;
  padding: 12px 40px 12px 14px;
  border: 1px solid var(--el-border-color, #E8E8E8);
  border-radius: 10px;
  font-size: 13px;
  color: var(--el-text-color-primary, #3A3A3A);
  background: #ffffff;
  appearance: none;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.2s;
}

.field-select:focus {
  outline: none;
  border-color: var(--el-color-primary, #4A90A4);
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.12);
}

.select-arrow {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: var(--el-text-color-placeholder, #B5B5B5);
  pointer-events: none;
}

/* 多选复选框组 */
.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.checkbox-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  background: var(--warm-beige, #F5F1EB);
  border: 1px solid var(--el-border-color, #E8E8E8);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 12px;
}

.checkbox-item:hover {
  border-color: var(--el-color-primary-light-5, #8CB8CC);
  background: var(--el-color-primary-light-9, #EFF4F8);
}

.checkbox-item.checked {
  background: var(--el-color-primary-light-9, #EFF4F8);
  border-color: var(--el-color-primary, #4A90A4);
}

.checkbox-item input[type="checkbox"] {
  display: none;
}

.checkbox-mark {
  width: 16px;
  height: 16px;
  border: 2px solid var(--el-border-color, #DCDFE6);
  border-radius: 4px;
  background: #fff;
  position: relative;
  transition: all 0.2s;
}

.checkbox-item.checked .checkbox-mark {
  background: var(--el-color-primary, #4A90A4);
  border-color: var(--el-color-primary, #4A90A4);
}

.checkbox-item.checked .checkbox-mark::after {
  content: '';
  position: absolute;
  left: 4px;
  top: 1px;
  width: 4px;
  height: 8px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
}

.checkbox-label {
  color: var(--el-text-color-primary, #3A3A3A);
  font-weight: 500;
}

/* 标签输入 */
.tags-input {
  border: 1px solid var(--el-border-color, #E8E8E8);
  border-radius: 10px;
  padding: 10px;
  background: #ffffff;
  transition: all 0.2s;
}

.tags-input:focus-within {
  border-color: var(--el-color-primary, #4A90A4);
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.12);
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  background: var(--el-color-primary-light-9, #EFF4F8);
  border: 1px solid var(--el-color-primary-light-7, #ADCCE0);
  border-radius: 6px;
  font-size: 12px;
  color: var(--el-color-primary, #4A90A4);
  font-weight: 500;
}

.tag-remove {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  background: transparent;
  border: none;
  color: var(--el-color-primary-light-5, #8CB8CC);
  cursor: pointer;
  border-radius: 3px;
  transition: all 0.2s;
}

.tag-remove:hover {
  background: var(--el-color-primary-light-8, #CEE0F4);
  color: var(--el-color-primary, #4A90A4);
}

.tag-input {
  width: 100%;
  border: none;
  background: transparent;
  font-size: 13px;
  color: var(--el-text-color-primary, #3A3A3A);
  outline: none;
  font-family: inherit;
}

.tag-input::placeholder {
  color: var(--el-text-color-placeholder, #B5B5B5);
}

/* JSON 编辑器 */
.json-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.json-input {
  font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
  font-size: 12px;
}

.json-error {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--el-color-danger, #C07B7B);
}

/* 无配置提示 */
.no-config-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 20px;
  color: var(--el-text-color-placeholder, #B5B5B5);
  text-align: center;
}

.no-config-hint .el-icon {
  margin-bottom: 14px;
  color: var(--cozy-taupe, #D4C4B0);
}

.no-config-hint p {
  margin: 0;
  font-size: 14px;
}

/* 操作按钮 */
.config-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 18px;
  border-top: 1px solid var(--el-border-color-lighter, #F8F8F8);
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 18px;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}

.action-btn.primary {
  background: var(--app-primary-gradient, linear-gradient(135deg, #4A90A4 0%, #6BA4B8 100%));
  color: #ffffff;
}

.action-btn.primary:hover {
  box-shadow: 0 4px 16px rgba(74, 144, 164, 0.35);
  transform: translateY(-1px);
}

.action-btn.danger {
  background: rgba(192, 123, 123, 0.1);
  color: var(--el-color-danger, #C07B7B);
  border: 1px solid rgba(192, 123, 123, 0.3);
}

.action-btn.danger:hover {
  background: rgba(192, 123, 123, 0.18);
  border-color: rgba(192, 123, 123, 0.5);
}
</style>
