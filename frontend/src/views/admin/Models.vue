<template>
  <Layout>
    <div class="admin-models-page">
      <!-- 主标题区域 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">
            模型管理
            <el-tag :type="models.length > 0 ? 'success' : 'info'" size="small" class="count-tag">
              {{ sortedModels.length }} 个模型
            </el-tag>
          </h1>
          <p class="page-subtitle">管理和配置AI对话模型与向量模型，为智能体提供强大的AI能力</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="openCreateDialog" size="large" class="add-button">
          <el-icon><Plus /></el-icon>
          新建模型
        </el-button>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <div class="search-filters-container">
        <div class="search-flex-container">
          <div class="search-left">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索模型名称..."
              clearable
              @clear="handleSearchChange"
              @input="handleSearchChange"
              class="search-input"
              :loading="isSearching"
              size="large"
              :prefix-icon="Search"
            />
      </div>

          <div class="filters-right">
            <el-select
              v-model="searchForm.providerId"
              placeholder="提供商"
              clearable
              @change="loadModels"
              class="filter-select"
            >
              <el-option label="全部提供商" value="" />
            <el-option
              v-for="provider in providers"
              :key="provider.id"
              :label="provider.name"
              :value="provider.id"
            />
          </el-select>
            
            <el-select
              v-model="searchForm.modelType"
              placeholder="模型类型"
              clearable
              @change="loadModels"
              class="filter-select model-type-select"
            >
              <el-option label="全部类型" value="" />
              <el-option label="对话模型" value="chat">
                <template #default>
                  <span class="model-type-option">
                <el-icon><ChatLineRound /></el-icon>
                <span>对话模型</span>
              </span>
            </template>
              </el-option>
              <el-option label="向量模型" value="embedding">
                <template #default>
                  <span class="model-type-option">
                <el-icon><Box /></el-icon>
                <span>向量模型</span>
              </span>
            </template>
              </el-option>
            </el-select>
            
            <el-select
              v-model="searchForm.isActive"
              placeholder="状态"
              clearable
              @change="loadModels"
              class="filter-select"
            >
              <el-option label="全部状态" :value="null" />
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 卡片网格 -->
      <div v-loading="loading" class="models-grid">
        <div
          v-for="model in sortedModels"
          :key="model.id"
          class="model-card"
          :class="{ 'model-disabled': model.isActive !== 1 }"
          @click="viewModelDetail(model)"
        >
          <div class="model-card-header">
            <div class="model-card-icon">
              <el-icon :size="32"><Cpu /></el-icon>
            </div>
            <div class="model-card-title-section">
              <h3 class="model-card-title">{{ model.displayName || model.name }}</h3>
              <div class="model-card-tags">
                <el-tag size="small" type="info">{{ getProviderName(model.providerId) }}</el-tag>
                <el-tag 
                  size="small" 
                  :type="model.modelType === 'chat' ? 'primary' : 'warning'"
                  class="model-type-tag"
                >
                  <el-icon v-if="model.modelType === 'chat'"><ChatLineRound /></el-icon>
                  <el-icon v-else><Box /></el-icon>
                  {{ model.modelType === 'chat' ? '对话' : '向量' }}
                </el-tag>
                <el-tag size="small" :type="model.isActive === 1 ? 'success' : 'danger'">
                  {{ model.isActive === 1 ? '启用' : '禁用' }}
                </el-tag>
              </div>
            </div>
            <el-button
              class="model-delete-btn"
              type="danger"
              size="small"
              :icon="Delete"
              circle
              @click.stop="deleteModel(model)"
            />
          </div>
          <div class="model-card-divider"></div>
          <div class="model-card-footer">
            <div class="model-card-actions">
              <el-button
                size="small"
                type="primary"
                @click.stop="viewModelDetail(model)"
              >
                配置
              </el-button>
              <el-button
                size="small"
                :type="model.isActive === 1 ? 'danger' : 'success'"
                @click.stop="toggleModelStatus(model)"
              >
                {{ model.isActive === 1 ? '禁用' : '启用' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="!loading && sortedModels.length === 0" description="暂无模型" />

      <!-- 模型详情/配置对话框 -->
      <el-dialog 
        v-model="showDetailDialog" 
        :title="modelForm.id ? '模型配置' : '新建模型'" 
        width="900px"
        @close="resetForm"
        :close-on-click-modal="false"
        class="model-dialog"
      >
        <div class="model-form-container">
          <el-form :model="modelForm" label-width="120px" :rules="rules" ref="modelFormRef" class="model-form">
            
            <!-- 基础信息区域 -->
            <div class="form-section">
              <h4 class="section-title">
                <el-icon><Cpu /></el-icon>
                基础信息
              </h4>
              
              <div class="form-grid">
                <el-form-item label="显示名称" prop="displayName" required>
                  <el-input v-model="modelForm.displayName" placeholder="请输入显示名称（必填）" />
                </el-form-item>
                
                <el-form-item label="模型名称" prop="name" required>
                  <el-input v-model="modelForm.name" placeholder="如：deepseek-chat（必填）" />
                </el-form-item>
                
                <el-form-item label="提供商" prop="providerId" required>
                  <el-select v-model="modelForm.providerId" placeholder="选择提供商（必填）" style="width: 100%">
                    <el-option
                      v-for="provider in providers"
                      :key="provider.id"
                      :label="provider.name"
                      :value="provider.id"
                    />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="模型类型" prop="modelType" required>
                  <el-select v-model="modelForm.modelType" placeholder="选择模型类型（必填）" style="width: 100%">
                    <el-option label="对话" value="chat" />
                    <el-option label="向量" value="embedding" />
                  </el-select>
                </el-form-item>
              </div>
              
              <el-form-item label="API地址">
                <el-input v-model="modelForm.apiBase" placeholder="可选，如果不填则使用提供商默认地址" />
              </el-form-item>
              
              <el-form-item label="API密钥">
                <el-input 
                  v-model="modelForm.apiKey" 
                  type="password" 
                  show-password
                  placeholder="可选，如果不填则使用提供商默认密钥"
                />
              </el-form-item>
              
              <el-form-item label="状态">
                <el-switch 
                  v-model="modelForm.isActive" 
                  :active-value="1" 
                  :inactive-value="0"
                  active-text="启用"
                  inactive-text="禁用"
                />
              </el-form-item>
            </div>
            
            <!-- 默认配置区域 - 仅对话模型显示 -->
            <div v-if="modelForm.modelType === 'chat'" class="form-section config-section">
              <div class="section-header">
                <h4 class="section-title">
                  <el-icon><Setting /></el-icon>
                  默认配置
                </h4>
                <div class="section-actions">
                  <el-button size="small" @click="resetDefaultConfig" type="primary" plain>
                    <el-icon><Refresh /></el-icon>
                    重置默认值
                  </el-button>
                  <el-button size="small" @click="toggleAdvancedMode" :type="showAdvancedMode ? 'warning' : 'info'">
                    <el-icon v-if="showAdvancedMode"><Edit /></el-icon>
                    <el-icon v-else><DocumentCopy /></el-icon>
                    {{ showAdvancedMode ? '简单模式' : 'JSON模式' }}
                  </el-button>
                </div>
              </div>
              
                <!-- 简单表单模式 -->
                <div v-if="!showAdvancedMode" class="config-form">
                  <!-- 第一行：温度和最大Token数 -->
                  <div class="config-row">
                    <div class="config-item">
                      <div class="param-header">
                        <span class="param-label">温度</span>
                        <span class="param-sub-label">(Temperature)</span>
                      </div>
                      <div class="slider-wrapper">
                        <div class="slider-control">
                          <el-slider
                            v-model="modelForm.temperature"
                            :min="0"
                            :max="2"
                            :step="0.1"
                            :marks="temperatureMarks"
                            class="param-slider"
                          />
                        </div>
                        <div class="slider-input">
                          <el-input-number
                            v-model="modelForm.temperature"
                            :min="0"
                            :max="2"
                            :step="0.1"
                            :precision="1"
                            size="small"
                            controls-position="right"
                          />
                        </div>
                      </div>
                      <div class="param-hint">
                        <el-icon><InfoFilled /></el-icon>
                        控制输出随机性，建议 0.7-0.9
                      </div>
                    </div>
                    
                    <div class="config-item">
                      <div class="param-header">
                        <span class="param-label">最大Token数</span>
                        <span class="param-sub-label">(Max Tokens)</span>
                      </div>
                      <div class="number-input-wrapper-wide">
                        <el-input-number
                          v-model="modelForm.maxTokens"
                          :min="100"
                          :max="8000"
                          :step="100"
                          controls-position="right"
                          class="token-number-input"
                        />
                      </div>
                      <div class="param-hint">
                        <el-icon><InfoFilled /></el-icon>
                        最大生成token数，建议 1000-4000
                      </div>
                    </div>
                  </div>
                  
                  <!-- 第二行：Top-P和存在惩罚 -->
                  <div class="config-row">
                    <div class="config-item">
                      <div class="param-header">
                        <span class="param-label">Top-P</span>
                        <span class="param-sub-label">(核采样)</span>
                      </div>
                      <div class="slider-wrapper">
                        <div class="slider-control">
                          <el-slider
                            v-model="modelForm.topP"
                            :min="0.1"
                            :max="1"
                            :step="0.05"
                            :marks="topPMarks"
                            class="param-slider"
                          />
                        </div>
                        <div class="slider-input">
                          <el-input-number
                            v-model="modelForm.topP"
                            :min="0.1"
                            :max="1"
                            :step="0.05"
                            :precision="2"
                            size="small"
                            controls-position="right"
                          />
                        </div>
                      </div>
                      <div class="param-hint">
                        <el-icon><InfoFilled /></el-icon>
                        核采样参数，建议 0.8-0.95
                      </div>
                    </div>
                    
                    <div class="config-item">
                      <div class="param-header">
                        <span class="param-label">存在惩罚</span>
                        <span class="param-sub-label">(Presence Penalty)</span>
                      </div>
                      <div class="number-input-wrapper">
                        <el-input-number
                          v-model="modelForm.presencePenalty"
                          :min="-2"
                          :max="2"
                          :step="0.1"
                          :precision="1"
                          controls-position="right"
                          class="full-width-number"
                        />
                      </div>
                      <div class="param-hint">
                        <el-icon><InfoFilled /></el-icon>
                        减少重复话题，范围 -2.0~2.0
                      </div>
                    </div>
                  </div>
                  
                  <!-- 第三行：频率惩罚和测试对话 -->
                  <div class="config-row">
                    <div class="config-item">
                      <div class="param-header">
                        <span class="param-label">频率惩罚</span>
                        <span class="param-sub-label">(Frequency Penalty)</span>
                      </div>
                      <div class="number-input-wrapper">
                        <el-input-number
                          v-model="modelForm.frequencyPenalty"
                          :min="-2"
                          :max="2"
                          :step="0.1"
                          :precision="1"
                          controls-position="right"
                          class="full-width-number"
                        />
                      </div>
                      <div class="param-hint">
                        <el-icon><InfoFilled /></el-icon>
                        减少重复词汇，范围 -2.0~2.0
                      </div>
                    </div>
                    
                    <div class="config-item">
                      <!-- 测试对话按钮 - 仅对聊天类型模型显示 -->
                      <div class="test-chat-container" v-if="modelForm.modelType === 'chat'">
                        <div class="param-header">
                          <span class="param-label">测试对话</span>
                          <span class="param-sub-label">(Test Chat)</span>
                        </div>
                        <el-button 
                          type="success" 
                          @click="openTestChat" 
                          :disabled="!canTestChat"
                          class="test-chat-btn"
                          size="large"
                        >
                          <el-icon><ChatLineRound /></el-icon>
                          开始测试
                        </el-button>
                        <div class="param-hint">
                          <el-icon><InfoFilled /></el-icon>
                          快速测试当前配置效果
                        </div>
                      </div>
                      
                      <!-- 占位保持对齐 - 非聊天类型模型 -->
                      <div v-else></div>
                    </div>
                  </div>
                </div>
              
                <!-- JSON 高级模式 -->
                <div v-else class="json-editor">
                  <div class="editor-toolbar">
                    <el-button @click="formatJson" type="info" plain class="json-btn">
                      <el-icon><DocumentCopy /></el-icon>
                      格式化JSON
                    </el-button>
                    <el-button @click="syncFromFormToJson" type="primary" plain class="json-btn">
                      <el-icon><Download /></el-icon>
                      从表单同步
                    </el-button>
                    <el-button 
                      v-if="modelForm.modelType === 'chat'" 
                      @click="openTestChat" 
                      :disabled="!canTestChat" 
                      type="success" 
                      plain 
                      class="json-btn"
                    >
                      <el-icon><ChatLineRound /></el-icon>
                      测试对话
                    </el-button>
                  </div>
                  <el-input
                    v-model="modelForm.defaultConfigJson"
                    type="textarea"
                    :rows="8"
                    placeholder='{"temperature": 0.7, "top_p": 0.9, "max_tokens": 2000}'
                  />
                </div>
              
              <div class="config-hint">
                <el-icon><InfoFilled /></el-icon>
                <span>这些配置将作为模型的默认参数，可在对话时进行覆盖</span>
              </div>
            </div>
            
          </el-form>
        </div>
        
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="showDetailDialog = false">取消</el-button>
            <el-button type="primary" @click="saveModel" :loading="saving">
              {{ modelForm.id ? '保存' : '创建' }}
            </el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 测试对话弹窗 -->
      <el-dialog
        v-model="showTestChat"
        title="测试对话"
        width="750px"
        :close-on-click-modal="false"
        class="test-chat-dialog"
      >
        <div class="test-chat-container-dialog">
          <div class="test-model-info">
            <el-tag type="info">{{ modelForm.displayName || modelForm.name }}</el-tag>
            <el-tag type="primary" v-if="showAdvancedMode">JSON模式</el-tag>
            <el-tag type="success" v-else>简单模式</el-tag>
          </div>
          
          <div class="test-chat-messages" ref="testMessagesRef">
            <div v-if="testChatMessages.length === 0" class="test-empty">
              <el-icon size="48"><ChatLineRound /></el-icon>
              <p>开始测试对话，体验当前配置效果</p>
            </div>
            
            <div 
              v-for="(message, index) in testChatMessages" 
              :key="index"
              class="test-message"
              :class="message.role"
            >
              <div class="message-content">
                <div class="message-text">{{ message.content }}</div>
                <div class="message-time">{{ message.time }}</div>
              </div>
            </div>
            
            <div v-if="testChatLoading" class="test-message assistant">
              <div class="message-content">
                <div class="message-loading">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  AI正在思考中...
                </div>
              </div>
            </div>
          </div>
          
          <div class="test-chat-input">
            <div class="input-with-button">
              <el-input
                v-model="testInput"
                placeholder="输入测试消息..."
                @keyup.enter="sendTestMessage"
                :disabled="testChatLoading"
                class="test-input"
                size="small"
              />
              <el-button 
                @click="sendTestMessage" 
                :disabled="!testInput.trim() || testChatLoading"
                type="primary"
                size="small"
                class="send-btn"
              >
                <el-icon><Promotion /></el-icon>
                发送
              </el-button>
            </div>
          </div>
        </div>
        
        <template #footer>
          <div class="test-dialog-footer">
            <el-button @click="clearTestChat" type="warning" plain>清空对话</el-button>
            <el-button @click="showTestChat = false">关闭</el-button>
          </div>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, ChatLineRound, Box, Cpu, Search, InfoFilled, Setting, Refresh, Edit, DocumentCopy, Download, Close, Promotion, Loading } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { llmModelApi, llmProviderApi } from '@/api/llm'

const loading = ref(false)

// 计算是否可以测试对话
const canTestChat = computed(() => {
  return modelForm.value.name && 
         modelForm.value.providerId && 
         modelForm.value.modelType === 'chat'
})
const saving = ref(false)
const isSearching = ref(false)
const models = ref([])
const providers = ref([])
const showDetailDialog = ref(false)
const modelFormRef = ref(null)
const searchTimer = ref(null)

const searchForm = ref({
  keyword: '',
  providerId: '',
  modelType: 'chat',
  isActive: null
})

const showAdvancedMode = ref(false)
const showTestChat = ref(false)
const testChatMessages = ref([])
const testChatLoading = ref(false)
const testInput = ref('')

const modelForm = ref({
  id: null,
  providerId: '',
  name: '',
  displayName: '',
  modelType: 'chat',
  apiBase: '',
  apiKey: '',
  isActive: 1,
  // 表单模式参数
  temperature: 0.7,
  topP: 0.9,
  maxTokens: 2000,
  presencePenalty: 0.0,
  frequencyPenalty: 0.0,
  // JSON 模式
  defaultConfigJson: ''
})

// 滑块标记
const temperatureMarks = ref({
  0: {
    style: {
      color: '#909399'
    },
    label: '保守'
  },
  0.7: {
    style: {
      color: '#409EFF'
    },
    label: '均衡'
  },
  1.4: {
    style: {
      color: '#E6A23C'
    },
    label: '创意'
  },
  2: {
    style: {
      color: '#F56C6C'
    },
    label: '随机'
  }
})

const topPMarks = ref({
  0.1: {
    style: {
      color: '#909399'
    },
    label: '精准'
  },
  0.5: {
    style: {
      color: '#409EFF'
    },
    label: '均衡'
  },
  0.9: {
    style: {
      color: '#67C23A'
    },
    label: '多样'
  },
  1: {
    style: {
      color: '#E6A23C'
    },
    label: '随机'
  }
})

const rules = {
  name: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  providerId: [{ required: true, message: '请选择提供商', trigger: 'change' }],
  modelType: [{ required: true, message: '请选择模型类型', trigger: 'change' }]
}

// 获取提供商名称
const getProviderName = computed(() => {
  return (providerId) => {
    const provider = providers.value.find(p => p.id === providerId)
    return provider ? provider.name : providerId || '未知'
  }
})

// 过滤和排序模型：禁用模型排在最后，并应用暗色样式
const sortedModels = computed(() => {
  const filtered = models.value.filter(model => {
    // 关键词搜索
    if (searchForm.value.keyword) {
      const keyword = searchForm.value.keyword.toLowerCase()
      const nameMatch = (model.name || '').toLowerCase().includes(keyword)
      const displayNameMatch = (model.displayName || '').toLowerCase().includes(keyword)
      if (!nameMatch && !displayNameMatch) {
        return false
      }
    }
    return true
  })
  
  // 排序：启用的在前，禁用的在后
  return filtered.sort((a, b) => {
    if (a.isActive === 1 && b.isActive !== 1) return -1
    if (a.isActive !== 1 && b.isActive === 1) return 1
    return 0
  })
})

const handleSearchChange = () => {
  // 防抖搜索
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
  isSearching.value = true
  searchTimer.value = setTimeout(() => {
    isSearching.value = false
  }, 150)
}

const loadProviders = async () => {
  try {
    providers.value = await llmProviderApi.getProviders()
  } catch (error) {
    console.error('加载提供商列表失败', error)
    ElMessage.warning('加载提供商列表失败')
  }
}

const loadModels = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.value.providerId) {
      params.providerId = searchForm.value.providerId
    }
    if (searchForm.value.modelType) {
      params.modelType = searchForm.value.modelType
    }
    if (searchForm.value.isActive !== null) {
      params.isActive = searchForm.value.isActive
    }
    models.value = await llmModelApi.getModels(params)
  } catch (error) {
    ElMessage.error('加载模型列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  resetForm()
  showDetailDialog.value = true
}

const viewModelDetail = (model) => {
  // 解析默认配置到表单字段
  const config = model.defaultConfig || {}
  
  modelForm.value = {
    id: model.id,
    providerId: model.providerId,
    name: model.name,
    displayName: model.displayName,
    modelType: model.modelType,
    apiBase: model.apiBase || '',
    apiKey: '', // API密钥不返回，留空
    isActive: model.isActive,
    // 表单模式参数
    temperature: config.temperature ?? 0.7,
    topP: config.top_p ?? 0.9,
    maxTokens: config.max_tokens ?? 2000,
    presencePenalty: config.presence_penalty ?? 0.0,
    frequencyPenalty: config.frequency_penalty ?? 0.0,
    // JSON 模式
    defaultConfigJson: model.defaultConfig 
      ? JSON.stringify(model.defaultConfig, null, 2)
      : ''
  }
  
  showAdvancedMode.value = false
  showDetailDialog.value = true
}

const resetForm = () => {
  modelForm.value = {
    id: null,
    providerId: '',
    name: '',
    displayName: '',
    modelType: 'chat',
    apiBase: '',
    apiKey: '',
    isActive: 1,
    // 表单模式参数
    temperature: 0.7,
    topP: 0.9,
    maxTokens: 2000,
    presencePenalty: 0.0,
    frequencyPenalty: 0.0,
    // JSON 模式
    defaultConfigJson: ''
  }
  showAdvancedMode.value = false
  if (modelFormRef.value) {
    modelFormRef.value.resetFields()
  }
}

const saveModel = async () => {
  if (!modelFormRef.value) return
  
  await modelFormRef.value.validate(async (valid) => {
    if (!valid) return

    let defaultConfig = null
    
    // 只有对话模型才需要处理defaultConfig
    if (modelForm.value.modelType === 'chat') {
      if (showAdvancedMode.value) {
        // JSON 模式：验证JSON格式
        if (modelForm.value.defaultConfigJson && modelForm.value.defaultConfigJson.trim()) {
          try {
            defaultConfig = JSON.parse(modelForm.value.defaultConfigJson)
          } catch (e) {
            ElMessage.error('默认配置JSON格式错误，请检查')
            return
          }
        }
      } else {
        // 表单模式：从表单字段构建配置
        defaultConfig = {
          temperature: modelForm.value.temperature,
          top_p: modelForm.value.topP,
          max_tokens: modelForm.value.maxTokens,
          presence_penalty: modelForm.value.presencePenalty,
          frequency_penalty: modelForm.value.frequencyPenalty
        }
      }
    }

    saving.value = true
    try {
      // 构建请求数据，确保所有必需字段都有值
      const requestData = {
        providerId: modelForm.value.providerId,
        name: modelForm.value.name,
        displayName: modelForm.value.displayName,
        modelType: modelForm.value.modelType,
        isActive: modelForm.value.isActive || 1
      }
      
      // 可选字段：只有有值时才添加
      if (modelForm.value.apiBase && modelForm.value.apiBase.trim()) {
        requestData.apiBase = modelForm.value.apiBase.trim()
      }
      
      if (modelForm.value.apiKey && modelForm.value.apiKey.trim()) {
        requestData.apiKey = modelForm.value.apiKey.trim()
      }
      
      // 只有对话模型才需要defaultConfig
      if (modelForm.value.modelType === 'chat' && defaultConfig) {
        requestData.defaultConfig = defaultConfig
      }

      if (modelForm.value.id) {
        // 更新：必须发送所有必需字段
        await llmModelApi.updateModel(modelForm.value.id, requestData)
        ElMessage.success('更新成功')
      } else {
        // 创建：必须发送所有必需字段
        await llmModelApi.createModel(requestData)
        ElMessage.success('创建成功')
      }
      showDetailDialog.value = false
      resetForm()
      loadModels()
    } catch (error) {
      ElMessage.error(modelForm.value.id ? '更新失败' : '创建失败')
      console.error(error)
    } finally {
      saving.value = false
    }
  })
}

const deleteModel = async (model) => {
  try {
    await ElMessageBox.confirm('确定要删除这个模型吗？删除后无法恢复。', '确认删除', {
      type: 'warning'
    })
    await llmModelApi.deleteModel(model.id)
    ElMessage.success('删除成功')
    loadModels()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

const toggleModelStatus = async (model) => {
  try {
    const newStatus = model.isActive === 1 ? 0 : 1
    
    // 如果要禁用，先检查关联的智能体
    if (newStatus === 0) {
      try {
        const countRes = await llmModelApi.countRelatedAgents(model.id)
        const relatedCount = countRes.count || 0
        
        if (relatedCount > 0) {
          const result = await ElMessageBox.confirm(
            `禁用此模型将影响 ${relatedCount} 个关联的智能体。是否同时禁用这些智能体？`,
            '确认禁用',
            {
              type: 'warning',
              confirmButtonText: '同时禁用智能体',
              cancelButtonText: '仅禁用模型',
              distinguishCancelAndClose: true
            }
          )
          
          // 构建完整的更新数据，包含所有必需字段
          const requestData = {
            providerId: model.providerId,
            name: model.name,
            displayName: model.displayName || model.name,
            modelType: model.modelType,
            isActive: newStatus,
            disableRelatedAgents: true // 用户确认了同时禁用智能体
          }
          
          // 可选字段：如果有值则添加
          if (model.apiBase) {
            requestData.apiBase = model.apiBase
          }
          
          // 如果前端有 defaultConfig，则添加
          if (model.defaultConfig) {
            requestData.defaultConfig = model.defaultConfig
          }
          
          await llmModelApi.updateModel(model.id, requestData)
          ElMessage.success(`已禁用模型，并同时禁用了 ${relatedCount} 个关联的智能体`)
        } else {
          // 没有关联的智能体，直接禁用
          const requestData = {
            providerId: model.providerId,
            name: model.name,
            displayName: model.displayName || model.name,
            modelType: model.modelType,
            isActive: newStatus
          }
          
          if (model.apiBase) {
            requestData.apiBase = model.apiBase
          }
          
          if (model.defaultConfig) {
            requestData.defaultConfig = model.defaultConfig
          }
          
          await llmModelApi.updateModel(model.id, requestData)
          ElMessage.success('已禁用')
        }
      } catch (confirmError) {
        if (confirmError === 'cancel') {
          // 用户选择仅禁用模型，不禁用智能体
          const requestData = {
            providerId: model.providerId,
            name: model.name,
            displayName: model.displayName || model.name,
            modelType: model.modelType,
            isActive: newStatus,
            disableRelatedAgents: false
          }
          
          if (model.apiBase) {
            requestData.apiBase = model.apiBase
          }
          
          if (model.defaultConfig) {
            requestData.defaultConfig = model.defaultConfig
          }
          
          await llmModelApi.updateModel(model.id, requestData)
          ElMessage.success('已禁用模型')
        } else {
          throw confirmError
        }
      }
    } else {
      // 启用模型
      const requestData = {
        providerId: model.providerId,
        name: model.name,
        displayName: model.displayName || model.name,
        modelType: model.modelType,
        isActive: newStatus
      }
      
      if (model.apiBase) {
        requestData.apiBase = model.apiBase
      }
      
      if (model.defaultConfig) {
        requestData.defaultConfig = model.defaultConfig
      }
      
      await llmModelApi.updateModel(model.id, requestData)
      ElMessage.success('已启用')
    }
    
    loadModels()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error(error)
    }
  }
}

const formatJson = () => {
  if (!modelForm.value.defaultConfigJson) {
    ElMessage.warning('请先输入JSON内容')
    return
  }
  try {
    const parsed = JSON.parse(modelForm.value.defaultConfigJson)
    modelForm.value.defaultConfigJson = JSON.stringify(parsed, null, 2)
    ElMessage.success('格式化成功')
  } catch (e) {
    ElMessage.error('JSON格式错误，无法格式化')
  }
}

const resetDefaultConfig = () => {
  // 重置表单字段
  modelForm.value.temperature = 0.7
  modelForm.value.topP = 0.9
  modelForm.value.maxTokens = 2000
  modelForm.value.presencePenalty = 0.0
  modelForm.value.frequencyPenalty = 0.0
  
  // 同时更新JSON
  const defaultConfig = {
    temperature: 0.7,
    top_p: 0.9,
    max_tokens: 2000,
    presence_penalty: 0.0,
    frequency_penalty: 0.0
  }
  modelForm.value.defaultConfigJson = JSON.stringify(defaultConfig, null, 2)
  
  ElMessage.success('已重置为默认配置')
}

const toggleAdvancedMode = () => {
  if (showAdvancedMode.value) {
    // 从 JSON 模式切换到表单模式，尝试解析 JSON
    if (modelForm.value.defaultConfigJson) {
      try {
        const config = JSON.parse(modelForm.value.defaultConfigJson)
        modelForm.value.temperature = config.temperature ?? 0.7
        modelForm.value.topP = config.top_p ?? 0.9
        modelForm.value.maxTokens = config.max_tokens ?? 2000
        modelForm.value.presencePenalty = config.presence_penalty ?? 0.0
        modelForm.value.frequencyPenalty = config.frequency_penalty ?? 0.0
      } catch (e) {
        ElMessage.warning('JSON 格式错误，使用默认值')
      }
    }
  } else {
    // 从表单模式切换到 JSON 模式，同步表单数据到 JSON
    syncFromFormToJson()
  }
  showAdvancedMode.value = !showAdvancedMode.value
}

const syncFromFormToJson = () => {
  const config = {
    temperature: modelForm.value.temperature,
    top_p: modelForm.value.topP,
    max_tokens: modelForm.value.maxTokens,
    presence_penalty: modelForm.value.presencePenalty,
    frequency_penalty: modelForm.value.frequencyPenalty
  }
  modelForm.value.defaultConfigJson = JSON.stringify(config, null, 2)
  ElMessage.success('已同步表单配置到JSON')
}

// 测试对话相关方法
const openTestChat = () => {
  if (!canTestChat.value) {
    ElMessage.warning('请先配置好模型基本信息')
    return
  }
  testChatMessages.value = []
  testInput.value = '你好，你是什么公司的模型，请介绍一下你自己！'
  showTestChat.value = true
}

const sendTestMessage = async () => {
  if (!testInput.value.trim()) {
    return
  }

  const userMessage = testInput.value.trim()
  testInput.value = ''
  
  // 添加用户消息
  testChatMessages.value.push({
    role: 'user',
    content: userMessage,
    time: new Date().toLocaleTimeString()
  })

  testChatLoading.value = true
  
  try {
    // 构建测试配置
    let config = {}
    if (showAdvancedMode.value && modelForm.value.defaultConfigJson) {
      try {
        config = JSON.parse(modelForm.value.defaultConfigJson)
      } catch (e) {
        // JSON格式错误，使用表单配置
        config = {
          temperature: modelForm.value.temperature,
          top_p: modelForm.value.topP,
          max_tokens: modelForm.value.maxTokens,
          presence_penalty: modelForm.value.presencePenalty,
          frequency_penalty: modelForm.value.frequencyPenalty
        }
      }
    } else {
      config = {
        temperature: modelForm.value.temperature,
        top_p: modelForm.value.topP,
        max_tokens: modelForm.value.maxTokens,
        presence_penalty: modelForm.value.presencePenalty,
        frequency_penalty: modelForm.value.frequencyPenalty
      }
    }

    // 构建测试请求 - 符合后端TestChatRequest格式
    const testRequest = {
      providerId: modelForm.value.providerId,
      modelName: modelForm.value.name,
      apiBase: modelForm.value.apiBase,
      apiKey: modelForm.value.apiKey,
      config: config,
      message: userMessage
    }

    // 调用测试API
    const response = await llmModelApi.testChat(testRequest)
    
    // 添加AI回复
    testChatMessages.value.push({
      role: 'assistant',
      content: response.data?.response || response.response || '回复为空',
      time: new Date().toLocaleTimeString()
    })
  } catch (error) {
    console.error('测试对话失败:', error)
    testChatMessages.value.push({
      role: 'assistant',
      content: '❌ 测试失败: ' + (error.message || '网络错误，请检查配置'),
      time: new Date().toLocaleTimeString(),
      isError: true
    })
  } finally {
    testChatLoading.value = false
    // 滚动到底部
    nextTick(() => {
      const messagesEl = document.querySelector('.test-chat-messages')
      if (messagesEl) {
        messagesEl.scrollTop = messagesEl.scrollHeight
      }
    })
  }
}

const clearTestChat = () => {
  testChatMessages.value = []
  ElMessage.success('对话已清空')
}

onMounted(() => {
  loadProviders()
  loadModels()
})
</script>

<style scoped>
.admin-models-page {
  padding: 32px;
  max-width: 1400px;
  margin: 0 auto;
  background: var(--el-bg-color-page);
  min-height: calc(100vh - 60px);
}

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

.add-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
}

/* 搜索筛选区域 */
.search-filters-container {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.search-flex-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.search-left {
  flex: 1;
  max-width: 400px;
}

.filters-right {
  display: flex;
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
}


.filter-select {
  width: 150px;
}

.filter-select :deep(.el-select__wrapper) {
  border-radius: 8px;
}

/* 模型类型选择器样式优化 */
.model-type-select :deep(.el-select__wrapper) {
  border-radius: 8px;
}

.model-type-option {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 模型卡片网格 */
.models-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.model-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s ease;
}

.model-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

/* 禁用模型的暗色样式 */
.model-card.model-disabled {
  opacity: 0.6;
  background: rgba(245, 247, 250, 0.8);
  filter: grayscale(0.3);
}

.model-card.model-disabled:hover {
  opacity: 0.8;
  filter: grayscale(0.2);
}

.model-card-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.model-card-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  border: 2px solid var(--el-color-primary);
  border-radius: 10px;
  color: var(--el-color-primary);
  transition: all 0.3s ease;
}

.model-card:hover .model-card-icon {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
}

.model-card-title-section {
  flex: 1;
  min-width: 0;
}

.model-card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.model-type-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.model-delete-btn {
  flex-shrink: 0;
}

.model-card-divider {
  height: 1px;
  background: var(--el-border-color-lighter);
  margin: 16px 0;
}

.model-card-footer {
  display: flex;
  justify-content: flex-end;
}

.model-card-actions {
  display: flex;
  gap: 8px;
}

/* 对话框样式优化 */
.model-dialog :deep(.el-dialog) {
  background-color: var(--el-bg-color) !important;
  border-radius: 12px !important;
  box-shadow: var(--el-box-shadow) !important;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.model-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background-color: transparent;
  flex-shrink: 0;
}

.model-dialog :deep(.el-dialog__title) {
  color: var(--el-text-color-primary) !important;
  font-weight: 600;
  font-size: 18px;
}

.model-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: var(--el-text-color-regular);
}

.model-dialog :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: var(--el-color-primary);
}

.model-dialog :deep(.el-dialog__body) {
  padding: 0;
  color: var(--el-text-color-regular) !important;
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.model-dialog :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid var(--el-border-color-lighter);
  flex-shrink: 0;
}

/* 表单容器 */
.model-form-container {
  padding: 24px;
  height: 100%;
  overflow-y: auto;
  max-height: 65vh;
}

.model-form {
  display: flex;
  flex-direction: column;
  gap: 32px;
  max-width: 800px;
  margin: 0 auto;
}

.form-section {
  background: var(--el-fill-color-extra-light);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid var(--el-border-color-lighter);
}

.form-section.config-section {
  background: linear-gradient(135deg, var(--el-color-primary-light-9), var(--el-fill-color-extra-light));
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 20px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--el-color-primary-light-8);
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title .el-icon {
  font-size: 18px;
  color: var(--el-color-primary);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.section-actions .el-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 基础信息网格 */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

/* 配置参数布局 */
.config-form {
  display: flex;
  flex-direction: column;
  gap: 32px;
  width: 100%;
}

.config-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
  width: 100%;
}

.config-item {
  display: flex;
  flex-direction: column;
  min-width: 0;
  width: 100%;
}

.slider-container {
  padding: 8px 0;
}

/* 表单控件样式 */
.model-form :deep(.el-form-item) {
  margin-bottom: 20px;
  width: 100%;
}

.model-form :deep(.el-form-item__content) {
  width: 100%;
}

.model-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--el-text-color-primary);
  font-size: 14px;
  line-height: 1.5;
}

.model-form :deep(.el-input__wrapper) {
  border-radius: 6px;
  background-color: #FFFFFF !important;
  box-shadow: 0 0 0 1px var(--el-border-color) inset !important;
  transition: all 0.3s;
}

.model-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

.model-form :deep(.el-select .el-input .el-input__wrapper) {
  border-radius: 6px;
  background-color: #FFFFFF !important;
  box-shadow: 0 0 0 1px var(--el-border-color) inset !important;
}

/* 滑块容器样式 */
.slider-wrapper {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 8px 0;
  width: 100%;
  min-width: 0;
}

.slider-control {
  flex: 1;
  min-width: 200px;
  padding: 0 8px;
}

.slider-input {
  width: 90px;
  flex-shrink: 0;
}

/* 滑块样式优化 */
.param-slider {
  width: 100%;
  min-width: 200px;
}

.model-form :deep(.el-slider) {
  margin: 16px 0 40px 0;
  width: 100% !important;
}

.model-form :deep(.el-slider__runway) {
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
  height: 6px;
  width: 100%;
}

.model-form :deep(.el-slider__bar) {
  background-color: var(--el-color-primary);
  border-radius: 4px;
  height: 6px;
}

.model-form :deep(.el-slider__button) {
  border: 3px solid var(--el-color-primary);
  background-color: #fff;
  width: 20px;
  height: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
}

.model-form :deep(.el-slider__button:hover) {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.model-form :deep(.el-slider__button-wrapper) {
  z-index: 1001;
}

.model-form :deep(.el-slider__marks) {
  margin-top: 15px;
  width: 100%;
}

.model-form :deep(.el-slider__marks-text) {
  font-size: 11px;
  font-weight: 500;
  white-space: nowrap;
  transform: translateX(-50%);
  color: var(--el-text-color-secondary);
}

/* 输入框样式 */
.slider-input :deep(.el-input-number) {
  width: 100%;
}

.slider-input :deep(.el-input__wrapper) {
  border-radius: 6px;
}

.number-input-wrapper {
  margin: 8px 0;
}

.full-width-number {
  width: 100% !important;
}

.model-form :deep(.el-input-number) {
  width: 100%;
}

.model-form :deep(.el-input-number .el-input__wrapper) {
  border-radius: 6px;
  background-color: #FFFFFF !important;
  box-shadow: 0 0 0 1px var(--el-border-color) inset !important;
  transition: all 0.3s;
}

.model-form :deep(.el-input-number .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

.model-form :deep(.el-input-number .el-input-number__decrease),
.model-form :deep(.el-input-number .el-input-number__increase) {
  background-color: var(--el-fill-color-light);
  border-left: 1px solid var(--el-border-color);
}

.model-form :deep(.el-input-number .el-input-number__decrease:hover),
.model-form :deep(.el-input-number .el-input-number__increase:hover) {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

/* 数字输入框样式 */
.model-form :deep(.el-input-number) {
  width: 100%;
}

.model-form :deep(.el-input-number .el-input__wrapper) {
  padding-right: 40px;
}

/* 开关样式 */
.model-form :deep(.el-switch) {
  height: 24px;
}

.model-form :deep(.el-switch__label) {
  font-size: 14px;
  color: var(--el-text-color-regular);
}

/* 参数标题样式 */
.param-header {
  margin-bottom: 8px;
  display: flex;
  align-items: baseline;
  gap: 6px;
  flex-wrap: wrap;
}

.param-label {
  font-weight: 600;
  color: var(--el-text-color-primary);
  font-size: 15px;
  white-space: nowrap;
}

.param-sub-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: normal;
  white-space: nowrap;
}

/* 参数提示 */
.param-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 8px;
  line-height: 1.4;
  display: flex;
  align-items: center;
  gap: 4px;
  background: var(--el-fill-color-lighter);
  padding: 6px 10px;
  border-radius: 6px;
  border-left: 3px solid var(--el-color-primary-light-5);
}

.param-hint .el-icon {
  font-size: 14px;
  color: var(--el-color-primary);
  flex-shrink: 0;
}

/* Token输入框样式 */
.number-input-wrapper-wide {
  margin: 8px 0;
}

.token-number-input {
  width: 100% !important;
  min-width: 200px; /* 增加最小宽度让输入框更长 */
}

.token-number-input :deep(.el-input__wrapper) {
  padding-right: 50px;
  min-width: 200px; /* 确保足够宽度显示数字 */
}

/* 测试对话按钮容器 */
.test-chat-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding-top: 8px;
}

.test-chat-btn {
  padding: 14px 24px;
  border-radius: 8px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 140px;
  justify-content: center;
}

/* JSON 编辑器 */
.json-editor {
  width: 100%;
}

  .editor-toolbar {
    display: flex;
    gap: 12px;
    margin-bottom: 12px;
    flex-wrap: wrap;
  }
  
  .json-btn {
    padding: 12px 20px; /* 增加按钮内边距 */
    border-radius: 6px;
    font-weight: 500;
    display: flex;
    align-items: center;
    min-width: 140px; /* 增加最小宽度确保文字显示完整 */
    justify-content: center;
    gap: 6px;
    font-size: 14px; /* 确保字体大小适合 */
  }

/* 配置提示 */
.config-hint {
  margin-top: 16px;
  padding: 12px;
  background: var(--el-color-info-light-9);
  border-left: 3px solid var(--el-color-info);
  border-radius: 0 6px 6px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--el-text-color-regular);
}

.config-hint .el-icon {
  color: var(--el-color-info);
  font-size: 16px;
  flex-shrink: 0;
}

/* 对话框底部 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.dialog-footer .el-button {
  border-radius: 6px;
  padding: 8px 20px;
  font-weight: 500;
}

/* 测试对话弹窗样式 */
.test-chat-dialog :deep(.el-dialog) {
  border-radius: 12px;
}

.test-chat-container-dialog {
  display: flex;
  flex-direction: column;
  height: 280px;
}

.test-model-info {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.test-chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: var(--el-fill-color-extra-light);
  border-radius: 8px;
  margin-bottom: 16px;
  border: 1px solid var(--el-border-color-lighter);
}

.test-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--el-text-color-secondary);
  gap: 12px;
}

.test-empty .el-icon {
  color: var(--el-color-primary-light-5);
}

.test-message {
  margin-bottom: 16px;
  display: flex;
}

.test-message.user {
  justify-content: flex-end;
}

.test-message.assistant {
  justify-content: flex-start;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-light);
}

.test-message.user .message-content {
  background: var(--el-color-primary);
  color: white;
  border-color: var(--el-color-primary);
}

.test-message.assistant .message-content {
  background: white;
  border-color: var(--el-border-color);
}

.message-text {
  line-height: 1.6;
  word-wrap: break-word;
}

.message-time {
  font-size: 11px;
  opacity: 0.7;
  margin-top: 4px;
  text-align: right;
}

.message-loading {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
}

.test-chat-input {
  flex-shrink: 0;
  padding: 8px 0;
}

.input-with-button {
  display: flex;
  gap: 8px;
  align-items: center;
}

.test-input {
  flex: 1;
  border-radius: 6px;
}

.test-input :deep(.el-input__wrapper) {
  border-radius: 6px;
  transition: all 0.2s ease;
}

.test-input :deep(.el-input__wrapper):focus-within {
  box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
}

.send-btn {
  border-radius: 6px;
  padding: 0 12px;
  min-width: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-weight: 500;
}

.test-dialog-footer {
  display: flex;
  justify-content: space-between;
}

.test-dialog-footer .el-button {
  border-radius: 6px;
  padding: 8px 16px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .admin-models-page {
    padding: 24px;
  }
  
  .page-header {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .header-actions {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .search-flex-container {
    flex-wrap: wrap;
  }
  
  .filters-right {
    flex-wrap: wrap;
    width: 100%;
  }
  
  .filter-select {
    flex: 1;
    min-width: 140px;
  }
  
  .models-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
  }
  
  /* 对话框响应式 */
  .model-dialog :deep(.el-dialog) {
    width: 95% !important;
    max-width: 800px;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .config-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .admin-models-page {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .header-left {
    width: 100%;
  }
  
  .page-title {
    font-size: 22px;
    flex-wrap: wrap;
  }
  
  .header-actions {
    width: 100%;
    flex-direction: column;
    gap: 12px;
  }
  
  .header-actions .el-button {
    width: 100%;
  }
  
  .search-filters-container {
    padding: 16px;
  }
  
  .search-flex-container {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  
  .search-left {
    max-width: none;
    width: 100%;
  }
  
  .filters-right {
    width: 100%;
    flex-direction: column;
    gap: 12px;
  }
  
  .filter-select {
    width: 100%;
    min-width: auto;
  }
  
  .models-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  /* 移动端对话框 */
  .model-dialog :deep(.el-dialog) {
    width: 95% !important;
    margin: 0 auto;
  }
  
  .model-form-container {
    padding: 16px;
    max-height: 70vh;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .config-row {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .slider-control {
    min-width: 150px;
  }
  
  .section-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .section-actions {
    width: 100%;
    justify-content: flex-start;
  }
  
  .form-section {
    padding: 16px;
  }
}

@media (max-width: 480px) {
  .admin-models-page {
    padding: 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .page-subtitle {
    font-size: 13px;
  }
  
  .search-filters-container {
    padding: 12px;
  }
}
</style>
