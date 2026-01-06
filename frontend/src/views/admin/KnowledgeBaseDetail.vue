<template>
  <Layout>
    <div class="kb-detail-page">
      <!-- 页面头部 -->
      <div class="page-header-container">
        <el-button 
          @click="goBack" 
          :icon="ArrowLeft" 
          text
          type="primary"
          class="back-button"
        >
          返回
        </el-button>
        <div class="header-content">
          <h1 class="kb-title">{{ knowledgeBase.name || '知识库详情' }}</h1>
          <span class="kb-subtitle">知识库管理与文档处理</span>
        </div>
      </div>

      <el-row :gutter="20" class="detail-row">
        <!-- 左侧：知识库信息 -->
        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8" class="left-col">
          <!-- 知识库信息 -->
          <el-card shadow="hover" class="info-card">
            <template #header>
              <div class="card-header-title">
                <el-icon class="header-icon"><Document /></el-icon>
                <span class="header-text">知识库信息</span>
              </div>
            </template>

            <el-descriptions :column="1" border size="default" class="kb-descriptions" v-loading="loading">
              <el-descriptions-item label="名称">
                <span class="kb-name-value">{{ knowledgeBase.name || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="描述">
                <span class="text-regular">{{ knowledgeBase.description || '暂无描述' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="文档数">
                <el-tag type="primary" size="default">{{ knowledgeBase.docCount || 0 }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="切片数">
                <el-tag type="success" size="default">{{ knowledgeBase.segmentCount || 0 }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="向量模型">
                <span class="text-regular">{{ getEmbeddingModelName(knowledgeBase.embeddingModel) || '默认模型' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="创建者">
                <span class="text-regular">{{ getCreatorName(knowledgeBase.createdBy) || '-' }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                <span class="text-regular">{{ formatTime(knowledgeBase.createdAt) }}</span>
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <!-- 右侧：文档列表和检索测试 -->
        <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16" class="right-col">
          <div class="right-cards-container">
            <!-- 文档列表 -->
            <el-card class="documents-card">
              <template #header>
                <div class="card-header">
                  <div class="card-header-title">
                    <el-icon class="header-icon"><Document /></el-icon>
                    <span class="header-text">文档列表</span>
                  </div>
                  <div class="card-header-actions">
                    <el-select 
                      v-model="statusFilter" 
                      placeholder="筛选状态" 
                      clearable 
                      style="width: 150px; margin-right: 10px;"
                      @change="loadDocuments"
                    >
                      <el-option label="全部" value="" />
                      <el-option label="待处理" value="pending" />
                      <el-option label="处理中" value="processing" />
                      <el-option label="已完成" value="completed" />
                      <el-option label="失败" value="failed" />
                    </el-select>
                    <el-button type="primary" @click="showUploadDialog = true" class="upload-button">
                      <el-icon><Upload /></el-icon>
                      上传文档
                    </el-button>
                  </div>
                </div>
              </template>

              <!-- 文档列表 -->
              <div class="table-container">
                <el-table 
                  v-loading="documentsLoading" 
                  :data="documents" 
                  stripe 
                  empty-text="暂无文档"
                  class="documents-table"
                >
                <el-table-column prop="fileName" label="文件名" min-width="200" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span>{{ row.fileName || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="fileType" label="类型" width="80">
                  <template #default="{ row }">
                    <el-tag size="small">{{ (row.fileType || '').toUpperCase() || '-' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="fileSize" label="大小" width="100">
                  <template #default="{ row }">
                    {{ formatSize(row.fileSize) }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="处理状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="getStatusType(row.status)" size="small">
                      {{ getStatusLabel(row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="segmentCount" label="切片数" width="80">
                  <template #default="{ row }">
                    <span>{{ row.segmentCount || 0 }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="上传时间" width="180">
                  <template #default="{ row }">
                    {{ formatTime(row.createdAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80">
                  <template #default="{ row }">
                    <div class="action-buttons">
                      <el-button 
                        v-if="row.status === 'failed'"
                        size="small" 
                        type="success" 
                        link 
                        @click="handleRetryDocument(row)"
                        class="action-btn"
                      >
                        重试
                      </el-button>
                      <el-button size="small" type="danger" link @click="confirmDeleteDoc(row)" class="action-btn">
                        删除
                      </el-button>
                    </div>
                  </template>
                </el-table-column>
                </el-table>
              </div>

              <!-- 分页 -->
              <div class="pagination-container">
                <el-pagination
                  v-model:current-page="pagination.page"
                  v-model:page-size="pagination.pageSize"
                  :total="pagination.total"
                  :page-sizes="[10, 20, 50]"
                  layout="total, sizes, prev, pager, next"
                  @size-change="loadDocuments"
                  @current-change="loadDocuments"
                />
              </div>
            </el-card>

            <!-- 向量检索测试 -->
            <el-card shadow="hover" class="search-card">
              <template #header>
                <div class="search-card-header">
                  <div class="card-header-title">
                    <el-icon class="header-icon-small"><Search /></el-icon>
                    <span class="header-text">向量检索测试</span>
                  </div>
                  <div class="card-header-actions">
                    <el-select 
                      v-model="searchTopK" 
                      size="default" 
                      style="width: 150px; margin-right: 10px;"
                    >
                      <el-option label="返回 3 条" :value="3" />
                      <el-option label="返回 5 条" :value="5" />
                      <el-option label="返回 10 条" :value="10" />
                      <el-option label="返回 20 条" :value="20" />
                    </el-select>
                    <el-button 
                      type="primary" 
                      :loading="searching" 
                      @click="handleSearch"
                      :disabled="!searchQuery.trim()"
                      class="search-button"
                    >
                      <el-icon v-if="!searching" class="button-icon"><Search /></el-icon>
                      {{ searching ? '检索中...' : '开始检索' }}
                    </el-button>
                  </div>
                </div>
              </template>

              <div class="search-content">
                <div class="search-input-wrapper">
                  <el-input
                    v-model="searchQuery"
                    type="textarea"
                    :rows="3"
                    placeholder="输入查询内容，测试向量检索效果..."
                    :disabled="searching"
                    class="search-textarea"
                  />
                </div>
                
                <div class="search-controls">
                  <!-- 相似度阈值设置 -->
                  <div class="similarity-threshold-box">
                    <div class="threshold-header">
                      <span class="threshold-label">相似度阈值:</span>
                      <el-slider
                        v-model="searchScoreThreshold"
                        :min="0.3"
                        :max="1.0"
                        :step="0.05"
                        :format-tooltip="(val) => `${(val * 100).toFixed(0)}%`"
                        class="threshold-slider"
                      />
                      <span class="threshold-value">
                        {{ (searchScoreThreshold * 100).toFixed(0) }}%
                      </span>
                      <el-popover placement="top" width="250" trigger="hover">
                        <template #reference>
                          <el-icon class="help-icon">
                            <QuestionFilled />
                          </el-icon>
                        </template>
                        <div class="threshold-help">
                          <p class="help-title">相似度阈值说明：</p>
                          <p>• 80-100%：极高相关（严格）</p>
                          <p>• 70-80%：高相关（推荐）</p>
                          <p>• 60-70%：中等相关</p>
                          <p>• 50-60%：低相关（宽松）</p>
                          <p>• &lt;50%：不推荐</p>
                        </div>
                      </el-popover>
                    </div>
                  </div>
                </div>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>

      <!-- 上传文档对话框 -->
      <el-dialog v-model="showUploadDialog" title="上传文档" width="600px">
        <el-form :model="uploadForm" label-width="120px">
          <el-form-item label="选择文件" required>
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :limit="1"
              :accept="'.txt,.md'"
              :on-change="handleFileChange"
            >
              <el-button type="primary">选择文件</el-button>
              <template #tip>
                <div class="el-upload__tip">
                  只支持 TXT 和 Markdown 格式，文件大小不超过 10MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="showUploadDialog = false">取消</el-button>
          <el-button type="primary" :loading="uploading" @click="confirmUpload">
            <el-icon><Upload /></el-icon>
            上传
          </el-button>
        </template>
      </el-dialog>

      <!-- 检索结果对话框 -->
      <el-dialog 
        v-model="showSearchResultsDialog" 
        title="向量检索结果" 
        width="800px"
        :close-on-click-modal="false"
      >
        <div class="search-results-dialog-content">
          <div class="results-summary">
            <el-tag type="success" size="large">
              找到 {{ searchResults.length }} 个相关结果
            </el-tag>
          </div>
          
          <div class="results-list">
            <div 
              v-for="(result, index) in searchResults" 
              :key="index"
              class="result-item-dialog"
            >
              <el-card shadow="hover" class="result-card-dialog">
                <div class="result-header-inline">
                  <div class="result-number">
                    <el-tag size="small" type="primary">
                      #{{ index + 1 }}
                    </el-tag>
                  </div>
                  <div class="result-similarity">
                    <el-tag 
                      :type="result.score > 0.8 ? 'success' : result.score > 0.6 ? 'warning' : 'info'"
                      size="default"
                    >
                      相似度: {{ (result.score * 100).toFixed(1) }}%
                    </el-tag>
                  </div>
                </div>
                
                <div class="result-content-inline">
                  <div class="result-text-inline">
                    {{ result.text || '-' }}
                  </div>
                  
                  <div class="result-meta-inline" v-if="result.metadata && Object.keys(result.metadata).length > 0">
                    <el-divider />
                    <div class="meta-items">
                      <span v-for="(value, key) in result.metadata" :key="key" class="meta-item-inline">
                        <el-icon><Document /></el-icon>
                        <strong>{{ key }}:</strong> {{ value }}
                      </span>
                    </div>
                  </div>
                </div>
              </el-card>
            </div>
          </div>
        </div>

        <template #footer>
          <el-button @click="showSearchResultsDialog = false">关闭</el-button>
        </template>
      </el-dialog>

      <!-- 文档状态对话框 -->
      <el-dialog 
        v-model="showStatusDialog" 
        :title="`文档状态 - ${currentDocument?.fileName || ''}`" 
        width="600px"
      >
        <div v-if="documentStatus">
          <el-descriptions :column="2" border>
            <el-descriptions-item label="文件名">
              {{ currentDocument?.fileName }}
            </el-descriptions-item>
            <el-descriptions-item label="文件类型">
              <el-tag size="small">{{ (currentDocument?.fileType || '').toUpperCase() }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="文件大小">
              {{ formatSize(currentDocument?.fileSize) }}
            </el-descriptions-item>
            <el-descriptions-item label="处理状态">
              <el-tag :type="getStatusType(documentStatus.status)" size="small">
                {{ getStatusLabel(documentStatus.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="切片数">
              {{ documentStatus.segmentCount || documentStatus.segment_count || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="重试次数">
              {{ documentStatus.retryCount || documentStatus.retry_count || 0 }}
            </el-descriptions-item>
            <el-descriptions-item label="错误信息" v-if="documentStatus.errorMessage || documentStatus.error_message" :span="2">
              <el-alert type="error" :closable="false">
                {{ documentStatus.errorMessage || documentStatus.error_message }}
              </el-alert>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间" :span="2">
              {{ formatTime(documentStatus.createdAt || documentStatus.created_at) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间" :span="2">
              {{ formatTime(documentStatus.updatedAt || documentStatus.updated_at) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <template #footer>
          <el-button @click="showStatusDialog = false">关闭</el-button>
          <el-button 
            v-if="documentStatus?.status === 'failed'"
            type="primary" 
            @click="handleRetryDocument(currentDocument)"
          >
            重试处理
          </el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Upload, 
  Search, 
  Document, 
  QuestionFilled,
  ArrowLeft
} from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { knowledgeApi } from '@/api/knowledge'
import { llmModelApi } from '@/api/llm'
import { adminUserApi } from '@/api/user'
import { getErrorInfo, isApiKeyError, getErrorSuggestion } from '@/utils/errorCodes'

const route = useRoute()
const router = useRouter()

// 数据
const loading = ref(false)
const documentsLoading = ref(false)
const uploading = ref(false)
const showUploadDialog = ref(false)
const showStatusDialog = ref(false)
const knowledgeBase = ref({})
const documents = ref([])
const selectedFile = ref(null)
const currentDocument = ref(null)
const documentStatus = ref(null)
const statusFilter = ref('')
const embeddingModels = ref([])
const users = ref([]) // 用户信息缓存

// 轮询相关
const pollingInterval = ref(null)
const processingDocIds = ref(new Set()) // 正在处理的文档ID集合
const POLLING_INTERVAL = 10000 // 10秒轮询一次
const MAX_POLLING_TIME = 600000 // 最多轮询10分钟

// 向量检索相关
const searchQuery = ref('')
const searchTopK = ref(5)
const searchScoreThreshold = ref(0.7)
const searchResults = ref([])
const searching = ref(false)
const showSearchResultsDialog = ref(false)

const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

const uploadForm = reactive({
  file: null
})

// 方法
const loadEmbeddingModels = async () => {
  try {
    const models = await llmModelApi.getModels({ modelType: 'embedding' })
    embeddingModels.value = models || []
  } catch (error) {
    console.error('加载向量模型列表失败', error)
  }
}

const loadUsers = async () => {
  try {
    // 加载所有用户（用于显示创建者名称）
    const response = await adminUserApi.getUsers({ page: 1, size: 1000 })
    if (response.list) {
      users.value = response.list
    } else if (Array.isArray(response)) {
      users.value = response
    }
  } catch (error) {
    console.error('加载用户列表失败', error)
  }
}

const getEmbeddingModelName = (modelId) => {
  if (!modelId) return null
  const model = embeddingModels.value.find(m => m.id === modelId)
  return model ? (model.displayName || model.name) : null
}

const getCreatorName = (userId) => {
  if (!userId) return null
  const user = users.value.find(u => u.id === userId)
  // 优先显示nickname，如果没有则显示username
  return user ? (user.nickname || user.username) : null
}

const loadKnowledgeBase = async () => {
  loading.value = true
  try {
    const kbId = route.params.id
    const data = await knowledgeApi.getKnowledgeBase(kbId)
    knowledgeBase.value = data
  } catch (error) {
    ElMessage.error('加载知识库信息失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const loadDocuments = async () => {
  documentsLoading.value = true
  try {
    const kbId = route.params.id
    const params = {
      page: pagination.page,
      size: pagination.pageSize
    }
    
    // 如果有状态筛选，添加到参数中
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    
    const response = await knowledgeApi.getDocuments(kbId, params)
    
    // 处理分页响应（后端返回PageResult格式：{list, total, page, size, pages}）
    if (response.list) {
      documents.value = response.list
      pagination.total = response.total || 0
      pagination.page = response.page || pagination.page
      pagination.pageSize = response.size || pagination.pageSize
    } else if (response.items) {
      documents.value = response.items
      pagination.total = response.total || 0
    } else if (Array.isArray(response)) {
      documents.value = response
      pagination.total = response.length
    } else {
      documents.value = []
      pagination.total = 0
    }
  } catch (error) {
    ElMessage.error('加载文档列表失败')
    console.error(error)
  } finally {
    documentsLoading.value = false
  }
}

const getStatusType = (status) => {
  const typeMap = {
    pending: 'info',
    processing: 'warning',
    completed: 'success',
    failed: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusLabel = (status) => {
  const labelMap = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成',
    failed: '失败'
  }
  return labelMap[status] || status
}

const formatSize = (bytes) => {
  if (!bytes) return '0B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + sizes[i]
}

const formatTime = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

const handleFileChange = (file) => {
  // 检查文件大小（10MB = 10485760 bytes）
  const MAX_SIZE = 10 * 1024 * 1024
  
  if (file.size > MAX_SIZE) {
    ElMessage.error(`文件大小超过限制，最大支持 10MB（当前文件：${(file.size / 1024 / 1024).toFixed(2)}MB）`)
    return false
  }
  
  selectedFile.value = file.raw
}

const confirmUpload = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择文件')
    return
  }

  uploading.value = true
  try {
    const kbId = route.params.id
    const response = await knowledgeApi.uploadDocument(kbId, selectedFile.value)
    // 后端返回格式：{data: {docId: 123}}
    const docId = response?.data?.docId || response?.docId

    ElMessage.success('上传成功，文档处理任务已提交，系统将自动刷新状态')
    showUploadDialog.value = false
    selectedFile.value = null
    
    // 立即刷新数据
    await loadDocuments()
    await loadKnowledgeBase()
    
    // 如果文档ID存在，添加到轮询列表
    if (docId) {
      processingDocIds.value.add(docId)
      startPolling()
    }
  } catch (error) {
    const errorCode = error.code || error.response?.data?.code
    const errorInfo = getErrorInfo(errorCode, error.message || error.response?.data?.message || '上传失败')
    
    // 显示带标题的错误提示
    ElMessage({
      type: errorInfo.type || 'error',
      message: `${errorInfo.title}: ${errorInfo.message}`,
      duration: 5000
    })
    
    // 如果是API Key错误，给出额外提示
    if (isApiKeyError(errorCode)) {
      setTimeout(() => {
        ElMessage.warning(getErrorSuggestion(errorCode))
      }, 500)
    }
    
    console.error(error)
  } finally {
    uploading.value = false
  }
}

const viewDocumentStatus = async (doc) => {
  currentDocument.value = doc
  showStatusDialog.value = true
  
  try {
    const kbId = route.params.id
    const status = await knowledgeApi.getDocumentStatus(kbId, doc.id)
    documentStatus.value = status
    
    // 如果状态对话框已打开，更新当前文档信息
    if (currentDocument.value && currentDocument.value.id === doc.id) {
      // 合并状态信息到当前文档
      Object.assign(currentDocument.value, {
        status: status.status,
        segmentCount: status.segmentCount || status.segment_count || 0,
        errorMessage: status.errorMessage || status.error_message || null,
        retryCount: status.retryCount || status.retry_count || 0
      })
    }
  } catch (error) {
    ElMessage.error('获取文档状态失败')
    console.error(error)
    documentStatus.value = null
  }
}

const handleRetryDocument = async (doc) => {
  try {
    await ElMessageBox.confirm(
      `确定要重试处理文档"${doc.fileName}"吗？最多可重试3次。`, 
      '确认重试', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    const kbId = route.params.id
    await knowledgeApi.retryDocument(kbId, doc.id)
    ElMessage.success('重试任务已提交，系统将自动刷新状态')
    
    // 立即刷新状态对话框（如果打开）
    if (showStatusDialog.value && currentDocument.value?.id === doc.id) {
      await viewDocumentStatus(doc)
    }
    
    // 添加到轮询列表
    processingDocIds.value.add(doc.id)
    startPolling()
    
    // 立即刷新列表和知识库
    await loadDocuments()
    await loadKnowledgeBase()
  } catch (error) {
    if (error !== 'cancel') {
      const errorCode = error.code || error.response?.data?.code
      const errorInfo = getErrorInfo(errorCode, error.message || error.response?.data?.message || '提交重试任务失败')
      
      ElMessage({
        type: errorInfo.type || 'error',
        message: `${errorInfo.title}: ${errorInfo.message}`,
        duration: 5000
      })
      
      if (isApiKeyError(errorCode)) {
        setTimeout(() => {
          ElMessage.warning(getErrorSuggestion(errorCode))
        }, 500)
      }
      
      console.error(error)
    }
  }
}

const confirmDeleteDoc = (doc) => {
  ElMessageBox.confirm(`确定要删除文档"${doc.fileName}"吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const kbId = route.params.id
      await knowledgeApi.deleteDocument(kbId, doc.id)
      ElMessage.success('删除成功')
      
      // 从轮询列表中移除（如果存在）
      processingDocIds.value.delete(doc.id)
      
      // 刷新数据
      await loadDocuments()
      await loadKnowledgeBase()
      
      // 如果删除的是当前查看的文档，关闭状态对话框
      if (showStatusDialog.value && currentDocument.value?.id === doc.id) {
        showStatusDialog.value = false
      }
    } catch (error) {
      ElMessage.error('删除失败：' + (error.response?.data?.message || error.message))
      console.error(error)
    }
  }).catch(() => {
    // 用户取消
  })
}

// 向量检索
const handleSearch = async () => {
  if (!searchQuery.value.trim()) {
    ElMessage.warning('请输入查询内容')
    return
  }

  if (knowledgeBase.value.segmentCount === 0) {
    ElMessage.warning('知识库中暂无已向量化的内容，请先上传并处理文档')
    return
  }

  searching.value = true
  searchResults.value = []

  try {
    const kbId = route.params.id
    const results = await knowledgeApi.testRetrieval(kbId, {
      query: searchQuery.value,
      topK: searchTopK.value,
      scoreThreshold: searchScoreThreshold.value
    })

    // 后端返回的是RetrievalResult数组
    if (results && Array.isArray(results) && results.length > 0) {
      searchResults.value = results
      showSearchResultsDialog.value = true
    } else {
      searchResults.value = []
      ElMessage.info('未找到相关内容，请尝试调整关键词或相似度阈值')
    }
  } catch (error) {
    console.error('检索失败:', error)
    const errorCode = error.code || error.response?.data?.code
    const errorInfo = getErrorInfo(errorCode, error.message || error.response?.data?.message || '检索失败')
    
    // 显示带标题的错误提示
    ElMessage({
      type: errorInfo.type || 'error',
      message: `${errorInfo.title}: ${errorInfo.message}`,
      duration: 5000
    })
    
    // 如果是API Key错误，给出额外提示
    if (isApiKeyError(errorCode)) {
      setTimeout(() => {
        ElMessage.warning(getErrorSuggestion(errorCode))
      }, 500)
    }
    
    searchResults.value = []
  } finally {
    searching.value = false
  }
}

// 轮询机制：检查正在处理的文档状态
const checkProcessingDocuments = async () => {
  if (processingDocIds.value.size === 0) {
    stopPolling()
    return
  }

  try {
    const kbId = route.params.id
    let hasProcessingDocs = false
    let hasStatusChanged = false

    // 检查每个正在处理的文档
    const docIdsToCheck = Array.from(processingDocIds.value)
    for (const docId of docIdsToCheck) {
      try {
        const status = await knowledgeApi.getDocumentStatus(kbId, docId)
        const currentStatus = status.status
        
        // 如果文档已完成或失败，从轮询列表中移除
        if (currentStatus === 'completed' || currentStatus === 'failed') {
          processingDocIds.value.delete(docId)
          hasStatusChanged = true
          
          // 更新文档列表中的对应文档
          const docIndex = documents.value.findIndex(d => d.id === docId)
          if (docIndex !== -1) {
            documents.value[docIndex].status = currentStatus
            documents.value[docIndex].segmentCount = status.segmentCount || status.segment_count || 0
            if (currentStatus === 'failed') {
              documents.value[docIndex].errorMessage = status.errorMessage || status.error_message
            }
          }
          
          // 如果正在查看该文档的状态对话框，更新状态
          if (showStatusDialog.value && currentDocument.value?.id === docId) {
            documentStatus.value = status
          }
        } else {
          hasProcessingDocs = true
        }
      } catch (error) {
        console.error(`检查文档状态失败: docId=${docId}`, error)
        // 如果文档不存在或其他错误，从轮询列表中移除
        processingDocIds.value.delete(docId)
      }
    }

    // 如果有状态变化，刷新列表和知识库统计
    if (hasStatusChanged) {
      await loadDocuments()
      await loadKnowledgeBase()
      
      // 如果所有文档都处理完成，显示提示
      if (processingDocIds.value.size === 0) {
        ElMessage.success('所有文档处理完成')
      }
    }

    // 如果没有正在处理的文档，停止轮询
    if (!hasProcessingDocs) {
      stopPolling()
    }
  } catch (error) {
    console.error('轮询检查失败:', error)
  }
}

// 启动轮询
const startPolling = () => {
  // 如果已经有轮询在运行，不重复启动
  if (pollingInterval.value) {
    return
  }

  // 检查是否有正在处理的文档
  const hasProcessing = documents.value.some(doc => 
    doc.status === 'pending' || doc.status === 'processing'
  )

  if (hasProcessing || processingDocIds.value.size > 0) {
    // 初始化轮询：将当前所有处理中的文档添加到轮询列表
    documents.value.forEach(doc => {
      if (doc.status === 'pending' || doc.status === 'processing') {
        processingDocIds.value.add(doc.id)
      }
    })

    pollingInterval.value = setInterval(() => {
      checkProcessingDocuments()
    }, POLLING_INTERVAL)
  }
}

// 停止轮询
const stopPolling = () => {
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value)
    pollingInterval.value = null
  }
}

// 监听文档列表变化，自动启动轮询
watch(() => documents.value, (newDocs) => {
  const hasProcessing = newDocs.some(doc => 
    doc.status === 'pending' || doc.status === 'processing'
  )
  
  if (hasProcessing && !pollingInterval.value) {
    startPolling()
  } else if (!hasProcessing && pollingInterval.value && processingDocIds.value.size === 0) {
    stopPolling()
  }
}, { deep: true })

const goBack = () => {
  stopPolling()
  router.push('/admin/knowledge-bases')
}

onMounted(() => {
  loadEmbeddingModels()
  loadUsers()
  loadKnowledgeBase()
  loadDocuments().then(() => {
    // 加载完文档后，检查是否有正在处理的文档，如果有则启动轮询
    startPolling()
  })
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.kb-detail-page {
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
  flex-shrink: 0;
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
}

.kb-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 1.4;
}

.kb-subtitle {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

/* 行布局 */
.detail-row {
  display: flex;
  align-items: stretch;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.left-col,
.right-col {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

/* 卡片样式 */
.info-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.info-card :deep(.el-card__body) {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
}

.right-cards-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 16px;
  overflow: hidden;
}

.documents-card {
  flex: 0 0 50%;
  display: flex;
  flex-direction: column;
  margin-bottom: 0;
  min-height: 0;
}

.documents-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.table-container {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  position: relative;
}

/* 确保表格fixed列完全覆盖 */
.documents-table :deep(.el-table__fixed-right) {
  background-color: var(--el-bg-color) !important;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.15);
  z-index: 100 !important;
}

.documents-table :deep(.el-table__fixed-right-patch) {
  background-color: var(--el-bg-color) !important;
  z-index: 100 !important;
}

/* 确保操作列所有单元格完全不透明 */
.documents-table :deep(.el-table__fixed-right .el-table__cell) {
  background-color: var(--el-bg-color) !important;
  opacity: 1 !important;
  z-index: 100 !important;
}

/* 确保表体单元格完全不透明 */
.documents-table :deep(.el-table__fixed-right .el-table__body-wrapper .el-table__row .el-table__cell) {
  background-color: var(--el-bg-color) !important;
  opacity: 1 !important;
  z-index: 100 !important;
}

/* 确保表头也完全不透明 */
.documents-table :deep(.el-table__fixed-right .el-table__header-wrapper .el-table__header .el-table__cell) {
  background-color: var(--el-bg-color) !important;
  opacity: 1 !important;
  z-index: 100 !important;
}

/* 确保操作列内容区域完全不透明 */
.documents-table :deep(.el-table__fixed-right .action-buttons) {
  background-color: transparent !important;
  opacity: 1 !important;
}

.documents-table :deep(.el-table__fixed-right .action-btn) {
  opacity: 1 !important;
  background-color: transparent !important;
}


.search-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-top: 0;
  margin-bottom: 0;
  min-height: 0;
}

.search-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 0;
}

.search-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.search-input-wrapper {
  position: relative;
  margin-top: 8px;
  flex-shrink: 0;
}

.search-textarea {
  width: 100%;
}

.search-controls {
  flex-shrink: 0;
  margin-top: 16px;
}

.search-results-list {
  flex: 1;
  overflow-y: auto;
  margin-top: 16px;
  min-height: 0;
  max-height: 100%;
}

/* 卡片标题 */
.card-header-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 18px;
  color: var(--el-color-primary);
}

.header-icon-small {
  font-size: 14px;
  color: var(--el-color-primary);
  margin-right: 2px;
}

.header-text {
  font-weight: 600;
  font-size: 15px;
  color: var(--el-text-color-primary);
}

.search-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0;
}

.search-card .card-header-title {
  margin-bottom: 0;
  gap: 6px;
}

.test-tag {
  margin-left: auto;
}

/* 知识库信息描述 */
.kb-descriptions {
  margin-top: 4px;
}

.kb-name-value {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.text-regular {
  color: var(--el-text-color-regular);
}

/* 检索控制区域 */

.similarity-threshold-box {
  margin-bottom: 15px;
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter);
}

.threshold-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.threshold-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
  font-weight: 500;
  min-width: 80px;
}

.threshold-slider {
  flex: 1;
  margin: 0 10px;
}

.threshold-value {
  font-size: 14px;
  color: var(--el-color-primary);
  font-weight: 600;
  min-width: 45px;
  text-align: right;
}

.help-icon {
  cursor: help;
  color: var(--el-text-color-secondary);
  font-size: 16px;
  transition: color 0.3s;
}

.help-icon:hover {
  color: var(--el-color-primary);
}

.threshold-help {
  font-size: 12px;
  line-height: 1.8;
}

.help-title {
  margin: 0 0 8px 0;
  font-weight: 600;
}

.search-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.search-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

.button-icon {
  margin-right: 4px;
}

/* 检索结果 */
.search-results {
  margin-top: 20px;
}

.results-tag {
  font-size: 13px;
}

.result-item {
  margin-bottom: 15px;
}

.result-card {
  transition: transform 0.2s, box-shadow 0.2s;
}

.result-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.result-index {
  flex-shrink: 0;
}

.result-doc-title {
  font-size: 14px;
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.similarity-tag {
  flex-shrink: 0;
}

.result-content {
  padding: 0;
}

.result-text {
  font-size: 13px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
}

.result-meta {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 空状态 */
.empty-state {
  margin-top: 20px;
}

/* 检索结果对话框样式 */
.search-results-dialog-content {
  max-height: 70vh;
  overflow-y: auto;
}

.results-summary {
  margin-bottom: 20px;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-item-dialog {
  width: 100%;
}

.result-card-dialog {
  width: 100%;
}

.result-card-dialog :deep(.el-card__body) {
  padding: 16px;
}

.result-header-inline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.result-number {
  flex-shrink: 0;
}

.result-similarity {
  flex-shrink: 0;
}

.result-content-inline {
  padding: 0;
}

.result-text-inline {
  font-size: 14px;
  color: var(--el-text-color-primary);
  line-height: 1.8;
  white-space: pre-wrap;
  word-wrap: break-word;
  margin-bottom: 12px;
}

.result-meta-inline {
  margin-top: 12px;
  padding-top: 12px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.result-meta-inline :deep(.el-divider) {
  margin: 12px 0;
}

.meta-items {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.meta-item-inline {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

/* 文档列表 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.upload-button {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 6px;
}

.action-btn {
  padding: 0 4px;
  margin: 0;
  white-space: nowrap;
  font-size: 13px;
  opacity: 1 !important;
}

.action-btn:hover {
  opacity: 1 !important;
}

.status-btn {
  min-width: 70px;
  text-align: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .kb-detail-page {
    padding: 15px;
  }

  .info-card,
  .search-card {
    margin-bottom: 15px;
  }

  .search-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .test-tag {
    margin-left: 0;
  }

  .threshold-header {
    flex-wrap: wrap;
  }

  .threshold-slider {
    width: 100%;
    margin: 8px 0;
  }

  .search-actions {
    flex-direction: column;
  }

  .topk-select,
  .search-button {
    width: 100%;
  }

  .result-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .upload-button {
    width: 100%;
  }
}
</style>

