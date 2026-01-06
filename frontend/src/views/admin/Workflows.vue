<template>
  <Layout>
    <div class="admin-workflows-page">
      <!-- 页面标题区域 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">
            工作流管理
            <el-tag :type="workflows.length > 0 ? 'success' : 'info'" size="small" class="count-tag">
              {{ workflows.length }} 个工作流
            </el-tag>
          </h1>
          <p class="page-subtitle">设计和管理智能体的工作流程，提升用户体验和服务质量</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="openCreateDialog" size="large" class="add-button">
            <el-icon><Plus /></el-icon>
            新建工作流
          </el-button>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <div class="search-filters-container">
        <div class="search-flex-container">
          <div class="search-left">
            <el-input
              v-model="searchQuery"
              placeholder="搜索工作流名称或描述..."
              clearable
              @clear="loadWorkflows"
              @input="handleSearch"
              class="search-input"
              :loading="isSearching"
              size="large"
              :prefix-icon="Search"
            />
          </div>
        </div>
      </div>

      <!-- 卡片网格 -->
      <div v-loading="loading" class="workflows-grid">
        <el-empty v-if="workflows.length === 0 && !loading" description="暂无工作流">
          <el-button type="primary" @click="openCreateDialog">创建第一个工作流</el-button>
        </el-empty>
        
        <div
          v-for="workflow in filteredWorkflows"
          :key="workflow.id"
          class="workflow-card"
          @click="editWorkflow(workflow)"
        >
          <div class="workflow-card-header">
            <div class="workflow-card-icon">
              <el-icon :size="32"><Operation /></el-icon>
            </div>
            <div class="workflow-card-title-section">
              <h3 class="workflow-card-title">{{ workflow.name }}</h3>
              <div class="workflow-card-tags">
                <el-tag size="small" type="primary">
                  <el-icon><Operation /></el-icon>
                  工作流
                </el-tag>
                <el-tag size="small" :type="workflow.isActive === 1 ? 'success' : 'danger'">
                  {{ workflow.isActive === 1 ? '启用' : '禁用' }}
                </el-tag>
              </div>
            </div>
            <el-button
              class="workflow-delete-btn"
              type="danger"
              size="small"
              :icon="Delete"
              circle
              @click.stop="deleteWorkflow(workflow)"
            />
          </div>
          <div class="workflow-card-divider"></div>
          <div class="workflow-card-body">
            <p class="workflow-description" :title="workflow.description || '暂无描述'">
              {{ workflow.description || '暂无描述' }}
            </p>
          </div>
          <div class="workflow-card-footer">
            <div class="workflow-card-actions">
              <el-button
                size="small"
                type="primary"
                @click.stop="editWorkflow(workflow)"
              >
                <el-icon><Edit /></el-icon>
                配置
              </el-button>
              <el-button
                size="small"
                :type="workflow.isActive === 1 ? 'warning' : 'success'"
                @click.stop="toggleWorkflowStatus(workflow)"
              >
                {{ workflow.isActive === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click.stop="deleteWorkflow(workflow)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete, Operation, Edit, Search } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { workflowApi } from '@/api/workflow'

const router = useRouter()
const loading = ref(false)
const isSearching = ref(false)
const workflows = ref([])
const searchQuery = ref('')

let searchTimer = null

const handleSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  isSearching.value = true
  searchTimer = setTimeout(() => {
    isSearching.value = false
  }, 500)
}

const filteredWorkflows = computed(() => {
  if (!searchQuery.value) {
    return workflows.value
  }
  const keyword = searchQuery.value.toLowerCase()
  return workflows.value.filter(workflow => 
    (workflow.name || '').toLowerCase().includes(keyword) ||
    (workflow.description || '').toLowerCase().includes(keyword)
  )
})

const loadWorkflows = async () => {
  loading.value = true
  try {
    const res = await workflowApi.getWorkflows()
    // 后端返回的是 PageResult，包含 list 字段
    workflows.value = res.list || res
  } catch (error) {
    ElMessage.error('加载工作流列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const editWorkflow = (workflow) => {
  router.push(`/admin/workflows/editor/${workflow.id}`)
}

const openCreateDialog = () => {
  router.push({
    name: 'admin-workflow-editor-new'
  })
}

const deleteWorkflow = async (workflow) => {
  try {
    await ElMessageBox.confirm(`确定要删除工作流「${workflow.name}」吗？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    await workflowApi.deleteWorkflow(workflow.id)
    ElMessage.success('删除成功')
    loadWorkflows()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

const toggleWorkflowStatus = async (workflow) => {
  try {
    const newStatus = workflow.isActive === 1 ? 0 : 1
    
    // 如果要禁用，先检查关联的智能体
    if (newStatus === 0) {
      try {
        const countRes = await workflowApi.countRelatedAgents(workflow.id)
        const relatedCount = countRes.count || 0
        
        if (relatedCount > 0) {
          const result = await ElMessageBox.confirm(
            `禁用此工作流将影响 ${relatedCount} 个关联的智能体。是否同时禁用这些智能体？`,
            '确认禁用',
            {
              type: 'warning',
              confirmButtonText: '同时禁用智能体',
              cancelButtonText: '仅禁用工作流',
              distinguishCancelAndClose: true
            }
          )
          
          await workflowApi.updateStatus(workflow.id, newStatus, true)
          ElMessage.success(`已禁用工作流，并同时禁用了 ${relatedCount} 个关联的智能体`)
        } else {
          await workflowApi.updateStatus(workflow.id, newStatus, false)
          ElMessage.success('已禁用')
        }
      } catch (confirmError) {
        if (confirmError === 'cancel') {
          await workflowApi.updateStatus(workflow.id, newStatus, false)
          ElMessage.success('已禁用工作流')
        } else {
          throw confirmError
        }
      }
    } else {
      await workflowApi.updateStatus(workflow.id, newStatus, false)
      ElMessage.success('已启用')
    }
    
    loadWorkflows()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
      console.error(error)
    }
  }
}


onMounted(() => {
  loadWorkflows()
})
</script>

<style scoped>
.admin-workflows-page {
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


/* 工作流卡片网格 */
.workflows-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.workflow-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s ease;
}

.workflow-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.workflow-card-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.workflow-card-icon {
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

.workflow-card:hover .workflow-card-icon {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
}

.workflow-card-title-section {
  flex: 1;
  min-width: 0;
}

.workflow-card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.workflow-card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.workflow-card-tags :deep(.el-tag--primary) {
  background-color: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-5);
  color: var(--el-color-primary);
}

.workflow-delete-btn {
  flex-shrink: 0;
}

.workflow-card-divider {
  height: 1px;
  background: var(--el-border-color-lighter);
  margin: 16px 0;
}

.workflow-card-body {
  flex: 1;
  margin-bottom: 12px;
}

.workflow-description {
  margin: 0;
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.workflow-card-footer {
  display: flex;
  justify-content: flex-end;
}

.workflow-card-actions {
  display: flex;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .admin-workflows-page {
    padding: 24px;
  }
  
  .workflows-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .admin-workflows-page {
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
  
  .workflows-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>

