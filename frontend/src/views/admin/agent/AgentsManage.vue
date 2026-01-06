<template>
  <Layout>
    <div class="admin-agents-page">
      <!-- 页面标题区域 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">
            智能体管理
            <el-tag :type="agents.length > 0 ? 'success' : 'info'" size="small" class="count-tag">
              {{ total }} 个智能体
            </el-tag>
          </h1>
          <p class="page-subtitle">管理所有智能体的配置和设置，为用户提供温暖的心灵陪伴</p>
        </div>
        <div class="header-actions">
          <el-button type="primary" @click="goToTestPage" size="large" class="add-button">
            <el-icon><Plus /></el-icon>
            新建智能体
          </el-button>
        </div>
      </div>

      <!-- 搜索和筛选 -->
      <div class="search-filters-container">
        <div class="search-flex-container">
          <div class="search-left">
            <el-input
              v-model="searchQuery"
              placeholder="搜索智能体名称或描述..."
              clearable
              @clear="loadAgents"
              @input="handleSearch"
              @keyup.enter="loadAgents"
              class="search-input"
              :loading="isSearching"
              size="large"
              :prefix-icon="Search"
            />
          </div>
          <div class="filters-right">
            <el-select
              v-model="sortBy"
              placeholder="排序方式"
              clearable
              @change="loadAgents"
              class="filter-select"
            >
              <el-option label="按热度" value="heat" />
              <el-option label="按创建时间" value="createTime" />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 智能体列表 -->
      <div v-loading="loading" class="common-cards-grid">
        <el-empty v-if="agents.length === 0 && !loading" description="暂无智能体">
          <el-button type="primary" @click="goToTestPage">创建第一个智能体</el-button>
        </el-empty>
        
        <div
          v-for="agent in agents"
          :key="agent.id"
          class="agent-card"
          @click="goToEdit(agent.id)"
        >
          <div class="agent-card-header">
            <div class="agent-card-icon">
              <el-avatar :size="48" :src="getAgentAvatar(agent)" class="agent-avatar">
                {{ agent.name?.[0] || 'A' }}
              </el-avatar>
            </div>
            <div class="agent-card-title-section">
              <h3 class="agent-card-title">{{ agent.name }}</h3>
              <div class="agent-card-tags">
                <el-text 
                  class="agent-description-text" 
                  :line-clamp="2" 
                  :size="'small'"
                >
                  {{ agent.description || '暂无描述' }}
                </el-text>
                <el-tag size="small" :type="getStatusTagType(agent.status)">
                  {{ getStatusText(agent.status) }}
                </el-tag>
                <el-tag size="small" :type="getHeatTagType(agent.heatValue || 0)" class="heat-tag">
                  <span class="flame-emoji">🔥</span> {{ formatHeat(agent.heatValue || 0) }}
                </el-tag>
              </div>
            </div>
            <el-button
              class="agent-delete-btn"
              type="danger"
              size="small"
              :icon="Delete"
              circle
              @click.stop="deleteAgent(agent)"
            />
          </div>
          <div class="agent-card-divider"></div>
          <div class="agent-card-footer">
            <div class="agent-card-actions">
              <!-- 启动/禁用按钮 -->
              <el-button
                v-if="agent.status === 'published'"
                size="small"
                type="warning"
                @click.stop="toggleAgentStatus(agent, 'offline')"
                :loading="statusLoading === agent.id"
              >
                <el-icon><SwitchButton /></el-icon>
                禁用
              </el-button>
              <el-button
                v-else-if="agent.status === 'offline'"
                size="small"
                type="success"
                @click.stop="toggleAgentStatus(agent, 'published')"
                :loading="statusLoading === agent.id"
              >
                <el-icon><CircleCheck /></el-icon>
                启动
              </el-button>
              <el-button
                size="small"
                type="primary"
                @click.stop="goToEdit(agent.id)"
              >
                <el-icon><Edit /></el-icon>
                配置
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click.stop="deleteAgent(agent)"
              >
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-if="total > 0"
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 48]"
          layout="prev, pager, next, sizes, jumper"
          @size-change="handlePageSizeChange"
          @current-change="loadAgents"
          class="simplified-pagination"
          background
          :hide-on-single-page="false"
        />
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete, ChatDotRound, Edit, SwitchButton, CircleCheck } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { agentApi } from '@/api/agent'

const router = useRouter()

const agents = ref([])
const loading = ref(false)
const isSearching = ref(false)
const searchQuery = ref('')
const sortBy = ref('heat')
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const statusLoading = ref(null) // 正在更新状态的智能体ID

let searchTimer = null

const handleSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  isSearching.value = true
  searchTimer = setTimeout(() => {
    page.value = 1
    loadAgents().finally(() => {
      isSearching.value = false
    })
  }, 500)
}

const handlePageSizeChange = () => {
  page.value = 1
  loadAgents()
}

const loadAgents = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      sort: sortBy.value
    }
    if (searchQuery.value) {
      params.keyword = searchQuery.value
    }

    const data = await agentApi.getAgents(params)
    agents.value = data.list || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error('加载智能体列表失败')
    console.error('加载智能体列表失败:', error)
  } finally {
    loading.value = false
  }
}

const goToEdit = (id) => {
  router.push(`/admin/agents/detail?id=${id}`)
}

const goToTestPage = () => {
  router.push('/admin/agents/detail')
}

const resetFilters = () => {
  searchQuery.value = ''
  sortBy.value = 'heat'
  page.value = 1
  loadAgents()
}

const deleteAgent = async (agent) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除智能体"${agent.name}"吗？删除后无法恢复。`,
      '确认删除',
      {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      }
    )
    await agentApi.deleteAgent(agent.id)
    ElMessage.success('删除成功')
    loadAgents()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 切换智能体状态（启动/禁用）
const toggleAgentStatus = async (agent, newStatus) => {
  if (statusLoading.value) return // 防止重复点击
  
  const statusText = newStatus === 'published' ? '发布' : '禁用'
  const confirmText = newStatus === 'published' 
    ? `确定要启动智能体"${agent.name}"吗？启动后将在广场展示。`
    : `确定要禁用智能体"${agent.name}"吗？禁用后将不在广场展示。`
  
  try {
    await ElMessageBox.confirm(
      confirmText,
      `确认${statusText}`,
      {
        type: 'warning',
        confirmButtonText: `确定${statusText}`,
        cancelButtonText: '取消'
      }
    )
    
    statusLoading.value = agent.id
    await agentApi.updateStatus(agent.id, newStatus)
    ElMessage.success(`${statusText}成功`)
    // 更新本地状态，避免重新加载整个列表
    agent.status = newStatus
    statusLoading.value = null
  } catch (error) {
    statusLoading.value = null
    if (error !== 'cancel') {
      ElMessage.error(`${statusText}失败`)
      console.error(error)
    }
  }
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'published': '已启用',
    'offline': '已下架'
  }
  return statusMap[status] || '未知'
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const typeMap = {
    'published': 'success',
    'offline': 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取智能体头像
const getAgentAvatar = (agent) => {
  if (agent.avatar) {
    return agent.avatar
  }
  // 使用默认头像生成器，为心灵陪伴场景选择温暖的头像风格
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${agent.name || 'agent'}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`
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

// 获取热度标签类型 (与el-tag的type对应)
const getHeatTagType = (heat) => {
  if (heat >= 1000) {
    return 'danger' // 非常热
  } else if (heat >= 500) {
    return 'warning' // 很热
  } else if (heat >= 100) {
    return '' // 默认类型，中等热度
  } else if (heat >= 10) {
    return 'info' // 较热
  }
  return 'info' // 一般热度
}

onMounted(() => {
  loadAgents()
})
</script>

<style scoped>
.admin-agents-page {
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

/* 智能体卡片网格 */
.common-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.agent-card {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.3s ease;
}

.agent-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.agent-card-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.agent-card-icon {
  flex-shrink: 0;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
  border: 2px solid var(--el-color-primary);
  border-radius: 10px;
  transition: all 0.3s ease;
}

.agent-card:hover .agent-card-icon {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary);
}

.agent-avatar {
  width: 100%;
  height: 100%;
  border-radius: 8px;
}

.agent-card-title-section {
  flex: 1;
  min-width: 0;
}

.agent-card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.agent-card-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  align-items: flex-start;
}

.agent-description-text {
  width: 100%;
  color: var(--el-text-color-regular);
  font-size: 12px;
  line-height: 1.5;
  margin-bottom: 4px;
}

/* 热度徽章 - 现代设计 */
.heat-badge {
  position: relative;
  display: inline-flex;
  align-items: center;
  padding: 6px 12px;
  border-radius: 20px;
  background: linear-gradient(135deg, rgba(255, 184, 77, 0.15) 0%, rgba(255, 152, 0, 0.15) 100%);
  border: 1px solid rgba(255, 184, 77, 0.3);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  overflow: hidden;
}

.heat-badge:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 152, 0, 0.2);
}

.heat-badge-content {
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;
  z-index: 1;
}

.flame-icon {
  font-size: 18px;
  line-height: 1;
  display: inline-block;
  animation: flameFlicker 2s ease-in-out infinite;
  filter: drop-shadow(0 0 3px rgba(255, 69, 0, 0.5));
}

.heat-number {
  font-size: 14px;
  font-weight: 700;
  color: #FF6B35;
  letter-spacing: 0.3px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.heat-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  height: 100%;
  background: radial-gradient(circle, rgba(255, 107, 53, 0.2) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}

.heat-badge:hover .heat-glow {
  opacity: 1;
}

/* 不同热度级别的样式 */
.heat-badge.heat-very-hot {
  background: linear-gradient(135deg, rgba(255, 69, 0, 0.2) 0%, rgba(255, 45, 0, 0.2) 100%);
  border-color: rgba(255, 69, 0, 0.4);
  animation: heatPulse 2s ease-in-out infinite;
}

.heat-badge.heat-very-hot .heat-number {
  color: #FF4500;
}

.heat-badge.heat-very-hot .flame-icon {
  filter: drop-shadow(0 0 5px rgba(255, 69, 0, 0.8));
}

.heat-badge.heat-hot {
  background: linear-gradient(135deg, rgba(255, 107, 53, 0.18) 0%, rgba(255, 69, 0, 0.18) 100%);
  border-color: rgba(255, 107, 53, 0.35);
}

.heat-badge.heat-hot .heat-number {
  color: #FF6B35;
}

.heat-badge.heat-warm {
  background: linear-gradient(135deg, rgba(255, 140, 66, 0.16) 0%, rgba(255, 107, 53, 0.16) 100%);
  border-color: rgba(255, 140, 66, 0.3);
}

.heat-badge.heat-warm .heat-number {
  color: #FF8C42;
}

.heat-badge.heat-mild {
  background: linear-gradient(135deg, rgba(255, 165, 0, 0.14) 0%, rgba(255, 140, 66, 0.14) 100%);
  border-color: rgba(255, 165, 0, 0.28);
}

.heat-badge.heat-mild .heat-number {
  color: #FFA500;
}

.heat-badge.heat-cool {
  background: linear-gradient(135deg, rgba(255, 184, 77, 0.12) 0%, rgba(255, 165, 0, 0.12) 100%);
  border-color: rgba(255, 184, 77, 0.25);
}

.heat-badge.heat-cool .heat-number {
  color: #FFB84D;
}

/* 动画效果 */
@keyframes flameFlicker {
  0%, 100% {
    transform: scale(1) rotate(0deg);
    opacity: 1;
  }
  25% {
    transform: scale(1.1) rotate(-2deg);
    opacity: 0.95;
  }
  50% {
    transform: scale(1.05) rotate(2deg);
    opacity: 0.9;
  }
  75% {
    transform: scale(1.08) rotate(-1deg);
    opacity: 0.95;
  }
}

@keyframes heatPulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(255, 69, 0, 0.4);
  }
  50% {
    box-shadow: 0 0 0 6px rgba(255, 69, 0, 0);
  }
}

.agent-card-tags :deep(.el-tag--success) {
  background-color: var(--el-color-success-light-9);
  border-color: var(--el-color-success-light-5);
  color: var(--el-color-success);
}

.agent-card-tags :deep(.el-tag--info) {
  background-color: var(--el-fill-color-lighter);
  border-color: var(--el-border-color-lighter);
  color: var(--el-text-color-secondary);
}

.agent-card-tags :deep(.el-tag--warning) {
  background-color: var(--el-color-warning-light-9);
  border-color: var(--el-color-warning-light-5);
  color: var(--el-color-warning);
}

.agent-card-tags :deep(.el-tag--warning) {
  background-color: var(--el-color-warning-light-9);
  border-color: var(--el-color-warning-light-5);
  color: var(--el-color-warning);
}

.agent-delete-btn {
  flex-shrink: 0;
}

.agent-card-divider {
  height: 1px;
  background: var(--el-border-color-lighter);
  margin: 16px 0;
}

.agent-card-footer {
  display: flex;
  justify-content: flex-end;
}

.agent-card-actions {
  display: flex;
  gap: 8px;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 0;
}

.simplified-pagination {
  width: 100%;
  display: flex;
  justify-content: center;
}

.simplified-pagination :deep(.el-pager li) {
  border-radius: 6px;
  margin: 0 2px;
}

.simplified-pagination :deep(.el-pagination__sizes .el-select .el-input) {
  border-radius: 6px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .admin-agents-page {
    padding: 24px;
  }
  
  .common-cards-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .admin-agents-page {
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
  
  .filters-right {
    width: 100%;
    flex-direction: column;
    gap: 12px;
  }
  
  .filter-select {
    width: 100%;
  }
  
  .common-cards-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>
