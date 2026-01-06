<template>
  <Layout>
    <div class="agents-market-page">
      <!-- 页面头部 -->
      <header class="market-header">
        <div class="header-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
        </div>
        <div class="header-content">
          <div class="header-text">
            <h1 class="market-title">
              <el-icon class="title-icon"><ChatDotRound /></el-icon>
              心灵伙伴广场
            </h1>
            <p class="market-subtitle">
              找到最适合您的AI心理陪伴者，倾听您的心声，温暖您的每一天
            </p>
          </div>
        </div>
      </header>

      <!-- 搜索和筛选区域 -->
      <section class="search-filter-section">
        <!-- 搜索框和排序在同一行 -->
        <div class="search-sort-row">
          <div class="search-wrapper">
            <el-input
              v-model="searchQuery"
              placeholder="搜索心灵伙伴的名字、特长或描述..."
              clearable
              size="large"
              @input="handleSearch"
              @keyup.enter="loadAgents"
              class="warm-search-input"
            >
              <template #prefix>
                <el-icon class="search-icon"><Search /></el-icon>
              </template>
            </el-input>
          </div>
          
          <div class="sort-wrapper">
            <el-select 
              v-model="sortBy" 
              @change="loadAgents" 
              class="warm-sort-select"
              size="large"
            >
              <el-option label="🔥 热度优先" value="heat" />
              <el-option label="✨ 最新发布" value="createTime" />
            </el-select>
          </div>
        </div>

      </section>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-section">
        <div class="skeleton-grid">
          <div v-for="i in 6" :key="i" class="skeleton-card">
            <el-skeleton animated>
              <template #template>
                <div class="skeleton-avatar"></div>
                <el-skeleton-item variant="h3" style="width: 60%; margin: 1rem 0" />
                <el-skeleton-item variant="text" style="width: 100%" />
                <el-skeleton-item variant="text" style="width: 80%" />
              </template>
            </el-skeleton>
          </div>
        </div>
      </div>

      <!-- 智能体卡片展示区 -->
      <div v-else-if="agents.length > 0" class="agents-grid-section">
        <div class="agents-grid">
          <div
            v-for="agent in agents"
            :key="agent.id"
            class="warm-agent-card"
            @click="startChat(agent)"
          >
            <div class="card-content">
              <!-- 热度徽章 -->
              <div v-if="agent.heatValue > 0" class="heat-badge">
                <span class="flame-icon">🔥</span>
                <span>{{ formatHeat(agent.heatValue) }}</span>
              </div>
              
              <!-- 头像区域 -->
              <div class="avatar-wrapper">
                <div class="avatar-ring"></div>
                <el-avatar 
                  :src="getAgentAvatar(agent)" 
                  :size="72" 
                  class="warm-avatar"
                >
                  {{ agent.name?.[0] || '心' }}
                </el-avatar>
              </div>
              
              <!-- 信息区域 -->
              <div class="info-section">
                <h3 class="agent-name">{{ agent.name }}</h3>
                <p class="agent-description">
                  {{ agent.description || '一位温暖的心灵伙伴，随时准备倾听您的心声' }}
                </p>
              </div>

              <!-- 标签区域 -->
              <div v-if="agent.tags && getTagsArray(agent.tags).length > 0" class="tags-section">
                <el-tag
                  v-for="(tag, index) in getTagsArray(agent.tags).slice(0, 3)"
                  :key="tag"
                  size="small"
                  class="warm-skill-tag"
                  effect="plain"
                >
                  {{ tag }}
                </el-tag>
              </div>

              <!-- 行动按钮 -->
              <div class="action-section">
                <el-button 
                  type="primary" 
                  class="chat-button"
                  :loading="creatingSession === agent.id"
                  @click.stop="startChat(agent)"
                >
                  <el-icon><ChatDotRound /></el-icon>
                  <span>开始对话</span>
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else class="empty-state">
        <div class="empty-content">
          <div class="empty-icon-wrapper">
            <el-icon class="empty-icon"><ChatLineRound /></el-icon>
          </div>
          <h3 class="empty-title">暂未找到合适的心灵伙伴</h3>
          <p class="empty-description">
            试试调整搜索关键词，或者浏览其他类型的陪伴者
          </p>
          <el-button 
            type="primary" 
            @click="clearAllFilters" 
            class="reset-filter-btn"
          >
            <el-icon><Refresh /></el-icon>
            重新搜索
          </el-button>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > 0 && !loading" class="pagination-container">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 48]"
          layout="prev, pager, next, sizes, jumper"
          @size-change="handlePageSizeChange"
          @current-change="loadAgents"
          class="simplified-pagination"
          :hide-on-single-page="false"
        />
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  ChatDotRound, 
  Close,
  Promotion,
  ChatLineRound,
  Refresh
} from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { agentApi } from '@/api/agent.js'
import { chatApi } from '@/api/chat.js'

const router = useRouter()

const agents = ref([])
const loading = ref(false)
const searchQuery = ref('')
const selectedTag = ref('')
const sortBy = ref('heat')
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)
const availableTags = ref([])
const searchTimer = ref(null)
const creatingSession = ref(null) // 正在创建会话的智能体ID

const SEARCH_DEBOUNCE_DELAY = 300

// 搜索防抖
const handleSearch = () => {
  if (searchTimer.value) clearTimeout(searchTimer.value)
  searchTimer.value = setTimeout(() => {
    page.value = 1
    loadAgents()
  }, SEARCH_DEBOUNCE_DELAY)
}

// 切换标签筛选
const toggleTag = (tag) => {
  if (selectedTag.value === tag) {
    selectedTag.value = ''
  } else {
    selectedTag.value = tag
  }
  page.value = 1
  loadAgents()
}

// 清除标签筛选
const clearTag = () => {
  selectedTag.value = ''
  page.value = 1
  loadAgents()
}

// 清除所有筛选
const clearAllFilters = () => {
  searchQuery.value = ''
  selectedTag.value = ''
  sortBy.value = 'heat'
  page.value = 1
  loadAgents()
}

// 处理分页大小变化
const handlePageSizeChange = () => {
  page.value = 1
  loadAgents()
}

// 获取标签数组
const getTagsArray = (tags) => {
  if (!tags) return []
  if (Array.isArray(tags)) return tags
  return tags.split(',').map(t => t.trim()).filter(t => t)
}

// 格式化热度值
const formatHeat = (heat) => {
  if (heat >= 10000) return `${(heat / 10000).toFixed(1)}w`
  if (heat >= 1000) return `${(heat / 1000).toFixed(1)}k`
  return heat.toString()
}

// 获取智能体头像
const getAgentAvatar = (agent) => {
  if (agent.avatar) return agent.avatar
  return null // 使用默认头像（显示首字符）
}

// 加载智能体列表
const loadAgents = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      status: 'published', // 只显示已发布的智能体
      sort: sortBy.value
    }
    // 搜索关键词：支持搜索name、description、tags
    if (searchQuery.value && searchQuery.value.trim()) {
      params.keyword = searchQuery.value.trim()
    }
    // 标签筛选
    if (selectedTag.value && selectedTag.value.trim()) {
      params.tag = selectedTag.value.trim()
    }

    const data = await agentApi.getAgents(params)
    agents.value = data.list || []
    total.value = data.total || 0
    
    // 提取所有标签
    const tagSet = new Set()
    agents.value.forEach(agent => {
      const tags = getTagsArray(agent.tags)
      tags.forEach(tag => {
        if (tag) tagSet.add(tag)
      })
    })
    availableTags.value = Array.from(tagSet).sort()
  } catch (error) {
    ElMessage.error('加载智能体列表失败')
    console.error('加载智能体列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 开始对话：创建会话并跳转到对话页
const startChat = async (agent) => {
  if (creatingSession.value) return // 防止重复点击
  
  creatingSession.value = agent.id
  try {
    // 创建会话
    await chatApi.createSession({
      agentId: agent.id,
      title: '新对话',
      sessionType: 'normal'
    })
    
    // 跳转到对话页面
    router.push(`/agents/${agent.id}`)
  } catch (error) {
    ElMessage.error(error.message || '创建会话失败，请稍后重试')
    console.error('创建会话失败:', error)
  } finally {
    creatingSession.value = null
  }
}

onMounted(() => {
  loadAgents()
})
</script>

<style scoped>
/* ===== 整体页面布局 ===== */
.agents-market-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem 1.5rem;
  min-height: calc(100vh - 120px);
  background: linear-gradient(to bottom, #fff8f5 0%, #fef9f6 50%, #ffffff 100%);
  position: relative;
  overflow: hidden;
}

.agents-market-page::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 400px;
  background: radial-gradient(ellipse at top, rgba(74, 144, 164, 0.12) 0%, rgba(102, 126, 234, 0.08) 50%, transparent 70%);
  pointer-events: none;
  z-index: 0;
}

.agents-market-page > * {
  position: relative;
  z-index: 1;
}

/* ===== 页面头部 ===== */
.market-header {
  margin-bottom: 2rem;
  padding: 2rem 0;
  position: relative;
}

.header-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.6;
  filter: blur(40px);
}

.circle-1 {
  width: 200px;
  height: 200px;
  background: linear-gradient(135deg, rgba(74, 144, 164, 0.4) 0%, rgba(102, 126, 234, 0.3) 100%);
  top: -50px;
  right: 10%;
  animation: float 6s ease-in-out infinite;
}

.circle-2 {
  width: 150px;
  height: 150px;
  background: linear-gradient(135deg, rgba(168, 184, 255, 0.4) 0%, rgba(116, 235, 213, 0.3) 100%);
  top: 50%;
  left: 5%;
  animation: float 8s ease-in-out infinite reverse;
}

.circle-3 {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, rgba(139, 184, 204, 0.4) 0%, rgba(174, 220, 224, 0.3) 100%);
  bottom: -30px;
  right: 20%;
  animation: float 7s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) translateX(0px);
  }
  33% {
    transform: translateY(-20px) translateX(10px);
  }
  66% {
    transform: translateY(10px) translateX(-10px);
  }
}

.header-content {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  position: relative;
  z-index: 1;
  align-items: center;
}

.header-text {
  flex: 1;
  text-align: center;
}

.market-title {
  font-size: 2.25rem;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  line-height: 1.4;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  background: linear-gradient(135deg, #4A90A4 0%, #8CB8CC 50%, #fecfef 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  background-size: 200% auto;
  animation: gradient-shift 4s ease infinite;
}

@keyframes gradient-shift {
  0%, 100% {
    background-position: 0% center;
  }
  50% {
    background-position: 100% center;
  }
}

.title-icon {
  font-size: 1.5rem;
  color: var(--el-color-primary);
  animation: float-icon 3s ease-in-out infinite;
  flex-shrink: 0;
}

@keyframes float-icon {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  25% {
    transform: translateY(-6px) rotate(2deg);
  }
  50% {
    transform: translateY(-10px) rotate(0deg);
  }
  75% {
    transform: translateY(-6px) rotate(-2deg);
  }
}

.market-subtitle {
  font-size: 0.95rem;
  color: var(--el-text-color-secondary);
  margin: 0;
  line-height: 1.6;
  text-align: center;
}

/* ===== 搜索和筛选区域 ===== */
.search-filter-section {
  margin-bottom: 2rem;
  padding: 1rem 1.5rem;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(74, 144, 164, 0.15);
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06), 0 0 0 1px rgba(74, 144, 164, 0.05);
  transition: all 0.3s ease;
}

.search-filter-section:hover {
  box-shadow: 0 6px 30px rgba(74, 144, 164, 0.12), 0 0 0 1px rgba(74, 144, 164, 0.15);
  border-color: rgba(74, 144, 164, 0.25);
}

.search-sort-row {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.search-wrapper {
  flex: 1;
  min-width: 0;
}

.warm-search-input {
  width: 100%;
}

.search-icon {
  color: #94a3b8;
  font-size: 1.2rem;
}

.sort-wrapper {
  flex-shrink: 0;
  min-width: 150px;
}

.filter-tags-wrapper {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.filter-label {
  font-size: 0.9rem;
  color: #64748b;
  font-weight: 500;
  white-space: nowrap;
}

.warm-filter-tag {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 20px;
  font-size: 0.875rem;
  padding: 0.5rem 1rem;
  border: 2px solid transparent;
}

.warm-filter-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.clear-tag-btn {
  font-size: 0.875rem;
  color: #94a3b8;
}

.sort-wrapper {
  min-width: 150px;
}

.warm-sort-select {
  width: 100%;
}

.warm-sort-select :deep(.el-input__wrapper) {
  border-radius: 12px;
}

/* ===== 加载状态 ===== */
.loading-section {
  margin: 2rem 0;
}

.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 2rem;
}

.skeleton-card {
  background: white;
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.skeleton-avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  margin: 0 auto 1.5rem;
}

/* ===== 智能体卡片网格 ===== */
.agents-grid-section {
  margin: 2rem 0;
}

.agents-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 2rem;
}

/* ===== 温暖的智能体卡片设计 ===== */
.warm-agent-card {
  background: white;
  border-radius: 24px;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  position: relative;
  border: 2px solid transparent;
}

.warm-agent-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 16px 40px rgba(102, 126, 234, 0.2);
  border-color: #667eea;
}

.warm-agent-card:active {
  transform: translateY(-4px) scale(1.01);
}

.card-content {
  padding: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  position: relative;
}

/* 热度徽章 */
.heat-badge {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  color: white;
  border-radius: 16px;
  padding: 0.4rem 0.8rem;
  font-size: 0.8rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  box-shadow: 0 4px 12px rgba(255, 154, 158, 0.4);
  z-index: 2;
  animation: pulse-glow 2s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 0 4px 12px rgba(255, 154, 158, 0.4);
  }
  50% {
    box-shadow: 0 4px 16px rgba(255, 154, 158, 0.6);
  }
}

.heat-badge .flame-icon {
  font-size: 1rem;
  display: inline-block;
  animation: flame-flicker 1.5s ease-in-out infinite;
}

@keyframes flame-flicker {
  0%, 100% {
    transform: scale(1) rotate(0deg);
    opacity: 1;
  }
  25% {
    transform: scale(1.1) rotate(-2deg);
    opacity: 0.9;
  }
  50% {
    transform: scale(0.95) rotate(2deg);
    opacity: 0.95;
  }
  75% {
    transform: scale(1.05) rotate(-1deg);
    opacity: 0.9;
  }
}

/* 头像区域 */
.avatar-wrapper {
  position: relative;
  margin-bottom: 1.5rem;
}

.avatar-ring {
  position: absolute;
  top: -4px;
  left: -4px;
  width: calc(100% + 8px);
  height: calc(100% + 8px);
  border-radius: 50%;
  background: linear-gradient(135deg, var(--el-color-primary) 0%, var(--el-color-primary-light-3) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.warm-agent-card:hover .avatar-ring {
  opacity: 0.25;
}

.warm-avatar {
  border: 4px solid #f1f5f9;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transition: all 0.3s ease;
  background: linear-gradient(135deg, var(--el-color-primary) 0%, var(--el-color-primary-light-3) 100%);
  position: relative;
  z-index: 1;
}

.warm-agent-card:hover .warm-avatar {
  transform: scale(1.1);
  border-color: var(--el-color-primary);
  box-shadow: 0 12px 32px rgba(74, 144, 164, 0.35);
}

/* 信息区域 */
.info-section {
  width: 100%;
  margin-bottom: 1.5rem;
}

.agent-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 0.75rem 0;
  line-height: 1.3;
}

.agent-description {
  font-size: 0.875rem;
  color: #94a3b8;
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 4.5rem;
}

/* 标签区域 */
.tags-section {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
  margin-bottom: 1.5rem;
  min-height: 2rem;
}

.warm-skill-tag {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  color: #92400e;
  border: 1px solid #fbbf24;
  border-radius: 12px;
  font-size: 0.8rem;
  padding: 0.3rem 0.8rem;
  font-weight: 500;
}

/* 行动按钮 */
.action-section {
  width: 100%;
  margin-top: auto;
  padding-top: 1rem;
}

.chat-button {
  width: 100%;
  height: 44px;
  border-radius: 16px;
  font-size: 1rem;
  font-weight: 600;
  background: linear-gradient(135deg, var(--el-color-primary) 0%, var(--el-color-primary-light-3) 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(74, 144, 164, 0.3);
  transition: all 0.3s ease;
}

.chat-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(74, 144, 164, 0.4);
  background: linear-gradient(135deg, var(--el-color-primary-light-3) 0%, var(--el-color-primary) 100%);
}

.chat-button:active {
  transform: translateY(0);
}

.chat-button .el-icon {
  margin-right: 0.5rem;
  font-size: 1.1rem;
}

/* ===== 空状态 ===== */
.empty-state {
  margin: 4rem 0;
}

.empty-content {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.empty-icon-wrapper {
  width: 120px;
  height: 120px;
  margin: 0 auto 2rem;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-icon {
  font-size: 4rem;
  color: #f59e0b;
}

.empty-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 1rem 0;
}

.empty-description {
  font-size: 1rem;
  color: #64748b;
  margin: 0 0 2rem 0;
  line-height: 1.6;
}

.reset-filter-btn {
  padding: 0.75rem 2rem;
  font-weight: 600;
  border-radius: 16px;
}

/* ===== 分页 ===== */
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

/* ===== 响应式设计 ===== */
@media (max-width: 1024px) {
  .agents-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 1.5rem;
  }
  
  .search-sort-row {
    flex-direction: column;
  }
  
  .search-wrapper,
  .sort-wrapper {
    width: 100%;
  }
  
  .filter-tags-wrapper {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
}

@media (max-width: 768px) {
  .agents-market-page {
    padding: 1rem;
  }
  
  .market-header {
    margin-bottom: 1.5rem;
  }
  
  .market-title {
    font-size: 1.5rem;
  }
  
  .market-subtitle {
    font-size: 0.9rem;
  }
  
  .search-filter-section {
    padding: 1rem;
  }
  
  .agents-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .card-content {
    padding: 1.5rem;
  }
  
  .warm-avatar {
    width: 60px !important;
    height: 60px !important;
  }
  
  .agent-name {
    font-size: 1.3rem;
  }
  
  .agent-description {
    font-size: 0.9rem;
    min-height: auto;
  }
  
  .empty-content {
    padding: 3rem 1.5rem;
  }
  
  .empty-icon-wrapper {
    width: 100px;
    height: 100px;
  }
  
  .empty-icon {
    font-size: 3rem;
  }
}

@media (max-width: 480px) {
  .market-title {
    font-size: 1.3rem;
  }
  
  .filter-label {
    display: block;
    margin-bottom: 0.5rem;
  }
  
  .tags-container {
    gap: 0.5rem;
  }
  
  .warm-filter-tag {
    font-size: 0.8rem;
    padding: 0.4rem 0.8rem;
  }
  
  .heat-badge {
    top: 1rem;
    right: 1rem;
    padding: 0.3rem 0.6rem;
    font-size: 0.75rem;
  }
}
</style>
