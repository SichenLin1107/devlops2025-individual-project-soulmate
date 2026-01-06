<template>
  <Layout>
    <div class="admin-kbs-page">
      <!-- 页面标题区域 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">
            知识库管理
            <el-tag :type="knowledgeBases.length > 0 ? 'success' : 'info'" size="small" class="count-tag">
              {{ knowledgeBases.length }} 个知识库
            </el-tag>
          </h1>
          <p class="page-subtitle">管理和配置知识库，为智能体提供丰富的知识支持</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="openCreateDialog" size="large" class="add-button">
            <el-icon><Plus /></el-icon>
            新建知识库
          </el-button>
        </div>
      </div>

      <!-- 搜索筛选 -->
      <div class="search-filters-container">
        <div class="search-flex-container">
          <div class="search-left">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索知识库名称..."
              clearable
              @clear="loadKnowledgeBases"
              @input="handleSearchChange"
              class="search-input"
              :loading="isSearching"
              size="large"
              :prefix-icon="Search"
            />
          </div>
        </div>
      </div>

      <!-- 卡片网格 -->
      <div v-loading="loading" class="kb-grid">
        <el-empty v-if="knowledgeBases.length === 0 && !loading" description="暂无知识库">
          <el-button type="primary" @click="openCreateDialog">创建第一个知识库</el-button>
        </el-empty>
        
        <div
          v-for="kb in filteredKnowledgeBases"
          :key="kb.id"
          class="kb-card"
          @click="viewKnowledgeBase(kb)"
        >
          <div class="kb-card-header">
            <div class="kb-card-icon">
              <el-icon :size="32"><Collection /></el-icon>
            </div>
            <div class="kb-card-title-section">
              <h3 class="kb-card-title">{{ kb.name }}</h3>
              <div class="kb-card-tags">
                <el-text 
                  class="kb-description-text" 
                  :line-clamp="2" 
                  :size="'small'"
                >
                  {{ kb.description || '暂无描述' }}
                </el-text>
                <el-tag size="small" type="primary">
                  <el-icon><Document /></el-icon> 
                  文档: {{ kb.docCount || 0 }}
                </el-tag>
                <el-tag size="small" type="success">
                  <el-icon><Box /></el-icon>
                  切片: {{ kb.segmentCount || 0 }}
                </el-tag>
                <el-tag size="small" :type="kb.isActive === 1 ? 'success' : 'danger'">
                  {{ kb.isActive === 1 ? '启用' : '禁用' }}
                </el-tag>
              </div>
            </div>
            <el-button
              class="kb-delete-btn"
              type="danger"
              size="small"
              :icon="Delete"
              circle
              @click.stop="deleteKnowledgeBase(kb)"
            />
          </div>
          <div class="kb-card-divider"></div>
          <div class="kb-card-footer">
            <div class="kb-card-actions">
              <el-button
                size="small"
                type="primary"
                @click.stop="viewKnowledgeBase(kb)"
              >
                <el-icon><View /></el-icon>
                查看详情
              </el-button>
              <el-button
                size="small"
                type="info"
                @click.stop="editKnowledgeBase(kb)"
              >
                <el-icon><Edit /></el-icon>
                编辑
              </el-button>
              <el-button
                size="small"
                :type="kb.isActive === 1 ? 'warning' : 'success'"
                @click.stop="toggleKnowledgeBaseStatus(kb)"
              >
                {{ kb.isActive === 1 ? '禁用' : '启用' }}
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 创建/编辑知识库对话框 -->
      <el-dialog 
        v-model="showCreateDialog" 
        :title="kbForm.id ? '编辑知识库' : '创建知识库'" 
        width="600px"
        @close="resetForm"
      >
        <el-form :model="kbForm" label-width="100px" :rules="rules" ref="kbFormRef">
          <el-form-item label="名称" prop="name" required>
            <el-input v-model="kbForm.name" placeholder="请输入知识库名称（必填）" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="kbForm.description" type="textarea" :rows="3" placeholder="请输入知识库描述" />
          </el-form-item>
          <el-form-item label="向量模型">
            <el-select 
              v-model="kbForm.embeddingModel" 
              placeholder="选择向量模型（可选）" 
              clearable
              filterable
              style="width: 100%"
            >
              <el-option
                v-for="model in embeddingModels"
                :key="model.id"
                :label="model.displayName || model.name"
                :value="model.id"
              >
                <div style="display: flex; justify-content: space-between; align-items: center;">
                  <span>{{ model.displayName || model.name }}</span>
                  <el-tag v-if="model.isActive !== 1" size="small" type="info" style="margin-left: 8px">
                    已禁用
                  </el-tag>
                </div>
              </el-option>
            </el-select>
            <div class="form-hint">
              <el-icon><InfoFilled /></el-icon>
              留空则使用系统默认向量模型
            </div>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="saveKnowledgeBase" :loading="saving">
            {{ kbForm.id ? '保存' : '创建' }}
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
import { Plus, Delete, Collection, Document, Edit, View, Search, Box, InfoFilled } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { knowledgeApi } from '@/api/knowledge'
import { llmModelApi } from '@/api/llm'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const isSearching = ref(false)
const knowledgeBases = ref([])
const showCreateDialog = ref(false)
const kbFormRef = ref(null)
const searchTimer = ref(null)
const embeddingModels = ref([])

const searchForm = ref({
  keyword: ''
})

const kbForm = ref({
  id: null,
  name: '',
  description: '',
  embeddingModel: ''
})

const rules = {
  name: [{ required: true, message: '请输入知识库名称', trigger: 'blur' }]
}

// 过滤知识库
const filteredKnowledgeBases = computed(() => {
  if (!searchForm.value.keyword) {
    return knowledgeBases.value
  }
  const keyword = searchForm.value.keyword.toLowerCase()
  return knowledgeBases.value.filter(kb => 
    (kb.name || '').toLowerCase().includes(keyword) ||
    (kb.description || '').toLowerCase().includes(keyword)
  )
})

const handleSearchChange = () => {
  // 防抖搜索
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
  isSearching.value = true
  searchTimer.value = setTimeout(() => {
    isSearching.value = false
    // 执行搜索
    loadKnowledgeBases()
  }, 500)  // 500ms防抖
}

const loadKnowledgeBases = async () => {
  loading.value = true
  try {
    const params = {
      page: 1,
      size: 1000  // 获取所有知识库
    }
    
    // 如果有搜索关键词，添加到参数中
    if (searchForm.value.keyword) {
      params.keyword = searchForm.value.keyword
    }
    
    const response = await knowledgeApi.getKnowledgeBases(params)
    
    // 处理分页响应（后端返回PageResult格式：{list, total, page, size, pages}）
    if (response.list) {
      knowledgeBases.value = response.list
    } else if (response.items) {
      knowledgeBases.value = response.items
    } else if (Array.isArray(response)) {
      knowledgeBases.value = response
    } else {
      knowledgeBases.value = []
    }
  } catch (error) {
    ElMessage.error('加载知识库列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  resetForm()
  showCreateDialog.value = true
}

const loadEmbeddingModels = async () => {
  try {
    const models = await llmModelApi.getModels({ modelType: 'embedding' })
    embeddingModels.value = models || []
  } catch (error) {
    console.error('加载向量模型列表失败', error)
    // 不显示错误提示，避免干扰用户
  }
}

const editKnowledgeBase = async (kb) => {
  try {
    // 获取知识库详情
    const detail = await knowledgeApi.getKnowledgeBase(kb.id)
    kbForm.value = {
      id: detail.id,
      name: detail.name,
      description: detail.description || '',
      embeddingModel: detail.embeddingModel || ''
    }
    showCreateDialog.value = true
  } catch (error) {
    ElMessage.error('获取知识库详情失败')
    console.error(error)
  }
}

const viewKnowledgeBase = (kb) => {
  router.push(`/admin/knowledge-bases/${kb.id}`)
}

const resetForm = () => {
  kbForm.value = {
    id: null,
    name: '',
    description: '',
    embeddingModel: ''
  }
  if (kbFormRef.value) {
    kbFormRef.value.resetFields()
  }
}

const saveKnowledgeBase = async () => {
  if (!kbFormRef.value) return
  
  await kbFormRef.value.validate(async (valid) => {
    if (!valid) return

    saving.value = true
    try {
      const data = {
        name: kbForm.value.name,
        description: kbForm.value.description || ''
      }
      if (kbForm.value.embeddingModel) {
        data.embeddingModel = kbForm.value.embeddingModel
      }

      if (kbForm.value.id) {
        await knowledgeApi.updateKnowledgeBase(kbForm.value.id, data)
        ElMessage.success('更新成功')
      } else {
        await knowledgeApi.createKnowledgeBase(data)
        ElMessage.success('创建成功')
      }
      showCreateDialog.value = false
      resetForm()
      await loadKnowledgeBases()
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.message || (kbForm.value.id ? '更新失败' : '创建失败')
      ElMessage.error(errorMsg)
      console.error(error)
    } finally {
      saving.value = false
    }
  })
}

const deleteKnowledgeBase = async (kb) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除知识库"${kb.name}"吗？删除后无法恢复，且会同步删除所有文档和向量数据。`, 
      '确认删除', 
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        dangerouslyUseHTMLString: false
      }
    )
    await knowledgeApi.deleteKnowledgeBase(kb.id)
    ElMessage.success('删除成功')
    await loadKnowledgeBases()
  } catch (error) {
    if (error !== 'cancel') {
      const errorMsg = error.response?.data?.message || error.message || '删除失败'
      ElMessage.error(errorMsg)
      console.error(error)
    }
  }
}

const toggleKnowledgeBaseStatus = async (kb) => {
  try {
    const newStatus = kb.isActive === 1 ? 0 : 1
    
    // 如果要禁用，先检查关联的智能体
    if (newStatus === 0) {
      try {
        const countRes = await knowledgeApi.countRelatedAgents(kb.id)
        const relatedCount = countRes.count || 0
        
        if (relatedCount > 0) {
          const result = await ElMessageBox.confirm(
            `禁用此知识库将影响 ${relatedCount} 个关联的智能体。是否同时禁用这些智能体？`,
            '确认禁用',
            {
              type: 'warning',
              confirmButtonText: '同时禁用智能体',
              cancelButtonText: '仅禁用知识库',
              distinguishCancelAndClose: true
            }
          )
          
          await knowledgeApi.updateStatus(kb.id, newStatus, true)
          ElMessage.success(`已禁用知识库，并同时禁用了 ${relatedCount} 个关联的智能体`)
        } else {
          await knowledgeApi.updateStatus(kb.id, newStatus, false)
          ElMessage.success('已禁用')
        }
      } catch (confirmError) {
        if (confirmError === 'cancel') {
          await knowledgeApi.updateStatus(kb.id, newStatus, false)
          ElMessage.success('已禁用知识库')
        } else {
          throw confirmError
        }
      }
    } else {
      await knowledgeApi.updateStatus(kb.id, newStatus, false)
      ElMessage.success('已启用')
    }
    
    await loadKnowledgeBases()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error(error)
    }
  }
}

onMounted(() => {
  loadKnowledgeBases()
  loadEmbeddingModels()
})
</script>

<style scoped>
.admin-kbs-page {
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


/* 知识库卡片网格 */
.kb-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.kb-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s ease;
}

.kb-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.kb-card-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.kb-card-icon {
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

.kb-card:hover .kb-card-icon {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
}

.kb-card-title-section {
  flex: 1;
  min-width: 0;
}

.kb-card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.kb-card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.kb-description-text {
  width: 100%;
  color: var(--el-text-color-regular);
  font-size: 12px;
  line-height: 1.5;
  margin-bottom: 4px;
}

.kb-delete-btn {
  flex-shrink: 0;
}

.kb-card-divider {
  height: 1px;
  background: var(--el-border-color-lighter);
  margin: 16px 0;
}

.kb-card-footer {
  display: flex;
  justify-content: flex-end;
}

.kb-card-actions {
  display: flex;
  gap: 8px;
}

.form-hint {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .admin-kbs-page {
    padding: 24px;
  }
  
  .kb-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .admin-kbs-page {
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
  }
  
  .header-actions .el-button {
    width: 100%;
  }
  
  .kb-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>
