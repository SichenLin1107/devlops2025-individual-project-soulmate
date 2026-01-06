<template>
  <Layout>
    <div class="admin-user-management-page">
      <!-- 主标题区域 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">
            用户管理
            <el-tag :type="users.length > 0 ? 'success' : 'info'" size="small" class="count-tag">
              {{ pagination.total }} 个用户
            </el-tag>
          </h1>
        <p class="page-subtitle">管理系统用户信息和权限，为用户提供优质的服务体验</p>
      </div>
    </div>

    <!-- 搜索和筛选 -->
      <div class="search-filters-container">
        <div class="search-flex-container">
          <div class="search-left">
          <el-input 
              v-model="searchForm.keyword"
              placeholder="搜索用户名或昵称..."
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
              v-model="searchForm.role"
              placeholder="角色"
              clearable
              @change="loadUsers"
              class="filter-select"
            >
              <el-option label="全部角色" value="" />
            <el-option label="超级管理员" value="superadmin" />
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
          </el-select>
            
            <el-select
              v-model="searchForm.status"
              placeholder="状态"
              clearable
              @change="loadUsers"
              class="filter-select"
            >
              <el-option label="全部状态" :value="null" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          </div>
        </div>
    </div>

    <!-- 用户列表 -->
      <div class="table-container">
        <el-table 
          v-loading="loading" 
          :data="users" 
          class="users-table"
          row-key="id"
        >
          <el-table-column label="序号" width="70" align="center" fixed="left">
          <template #default="{ $index }">
              <span class="row-number">{{ (pagination.page - 1) * pagination.size + $index + 1 }}</span>
          </template>
        </el-table-column>
          
          <el-table-column label="头像" width="70" align="center">
          <template #default="{ row }">
            <el-avatar :src="row.avatar" :size="40">
              {{ (row.nickname || row.username || 'U')[0].toUpperCase() }}
            </el-avatar>
          </template>
        </el-table-column>
          
          <el-table-column prop="username" label="用户名" width="120">
            <template #default="{ row }">
              <el-tooltip 
                :content="row.username" 
                placement="top"
                :disabled="!row.username || row.username.length <= 10"
                :popper-options="{ 
                  strategy: 'fixed',
                  modifiers: [
                    { name: 'offset', options: { offset: [0, -8] } },
                    { name: 'flip', enabled: true },
                    { name: 'preventOverflow', enabled: true, options: { padding: 8 } }
                  ]
                }"
              >
                <div class="cell-content">{{ row.username }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          
          <el-table-column prop="nickname" label="昵称" width="90">
            <template #default="{ row }">
              <el-tooltip 
                :content="row.nickname || '--'" 
                placement="top"
                :disabled="!row.nickname || row.nickname.length <= 8"
                :popper-options="{ 
                  strategy: 'fixed',
                  modifiers: [
                    { name: 'offset', options: { offset: [0, -8] } },
                    { name: 'flip', enabled: true },
                    { name: 'preventOverflow', enabled: true, options: { padding: 8 } }
                  ]
                }"
              >
                <div class="cell-content">{{ row.nickname || '--' }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          
          <el-table-column prop="role" label="角色" width="120" align="center">
            <template #default="{ row }">
              <!-- 超级管理员只显示标签，不显示下拉框 -->
              <el-tag v-if="row.role === 'superadmin'" type="danger" size="small">
                超级管理员
              </el-tag>
              <!-- 其他角色显示下拉框 -->
              <el-select
                v-else
                v-model="row.role"
                @change="handleRoleChange(row)"
                :loading="row._roleLoading"
                size="small"
                style="width: 100px"
              >
                <el-option label="普通用户" value="user" />
                <el-option label="管理员" value="admin" />
              </el-select>
            </template>
          </el-table-column>
          
          <el-table-column prop="bio" label="个人简介" width="200">
            <template #default="{ row }">
              <el-tooltip 
                :content="row.bio || '--'" 
                placement="top"
                :disabled="!row.bio || row.bio.length <= 20"
                :popper-options="{ 
                  strategy: 'fixed',
                  modifiers: [
                    { name: 'offset', options: { offset: [0, -8] } },
                    { name: 'flip', enabled: true },
                    { name: 'preventOverflow', enabled: true, options: { padding: 8 } }
                  ]
                }"
              >
                <div class="cell-content bio-text">{{ row.bio || '--' }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          
          <el-table-column prop="createdAt" label="注册时间" width="130" align="center">
            <template #default="{ row }">
              <el-tooltip 
                :content="formatDateTime(row.createdAt)" 
                placement="top"
                :popper-options="{ 
                  strategy: 'fixed',
                  modifiers: [
                    { name: 'offset', options: { offset: [0, -8] } },
                    { name: 'flip', enabled: true },
                    { name: 'preventOverflow', enabled: true, options: { padding: 8 } }
                  ]
                }"
              >
                <div class="cell-content time-text">{{ formatDateTime(row.createdAt) }}</div>
              </el-tooltip>
            </template>
          </el-table-column>
          
          <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch 
              v-model="row.status" 
              :active-value="1"
              :inactive-value="0"
              @change="handleToggleStatus(row)"
                :loading="row._statusLoading"
                class="status-switch"
            />
          </template>
        </el-table-column>
          
          <el-table-column label="操作" width="90" fixed="right" align="center">
          <template #default="{ row }">
              <div class="action-buttons">
            <el-button 
              size="small" 
              type="danger" 
                  link
              @click="handleDeleteUser(row)"
                  :icon="Delete"
            >
              删除
            </el-button>
              </div>
          </template>
        </el-table-column>
      </el-table>
      </div>

      <!-- 分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 30]"
          layout="prev, pager, next, sizes, jumper"
          @size-change="handlePageSizeChange"
          @current-change="loadUsers"
          class="simplified-pagination"
          background
          :hide-on-single-page="false"
        />
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { adminUserApi } from '@/api/user'

const loading = ref(false)
const isSearching = ref(false)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const searchForm = reactive({
  keyword: '',
  role: '',
  status: null
})

const users = ref([])

// 防抖搜索
let searchTimer = null
const handleSearchChange = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  isSearching.value = true
  searchTimer = setTimeout(() => {
    pagination.page = 1
    loadUsers()
    isSearching.value = false
  }, 500)
}

// 加载用户列表
const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }
    
    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }
    
    if (searchForm.role) {
      params.role = searchForm.role
    }
    
    if (searchForm.status !== null) {
      params.status = searchForm.status
    }
    
    const result = await adminUserApi.getUsers(params)
    
    if (result && result.list) {
      // 对用户列表进行排序，确保superadmin始终在第一位
      const sortedList = [...result.list].sort((a, b) => {
        if (a.role === 'superadmin' && b.role !== 'superadmin') return -1
        if (a.role !== 'superadmin' && b.role === 'superadmin') return 1
        if (a.role === 'admin' && b.role === 'user') return -1
        if (a.role === 'user' && b.role === 'admin') return 1
        return 0
      })
      
      users.value = sortedList.map(user => ({
        ...user,
        _statusLoading: false,
        _roleLoading: false,
        _originalRole: user.role // 保存原始角色，用于失败时恢复
      }))
      pagination.total = result.total || 0
    } else if (Array.isArray(result)) {
      // 对用户列表进行排序，确保superadmin始终在第一位
      const sortedList = [...result].sort((a, b) => {
        if (a.role === 'superadmin' && b.role !== 'superadmin') return -1
        if (a.role !== 'superadmin' && b.role === 'superadmin') return 1
        if (a.role === 'admin' && b.role === 'user') return -1
        if (a.role === 'user' && b.role === 'admin') return 1
        return 0
      })
      
      users.value = sortedList.map(user => ({
        ...user,
        _statusLoading: false,
        _roleLoading: false,
        _originalRole: user.role // 保存原始角色，用于失败时恢复
      }))
      pagination.total = result.length
    } else {
      users.value = []
      pagination.total = 0
    }
  } catch (error) {
    ElMessage.error(error?.message || '获取用户列表失败')
    users.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

const getRoleLabel = (role) => {
  const labels = {
    admin: '管理员',
    user: '普通用户'
  }
  return labels[role] || role
}

const getRoleTagType = (role) => {
  const types = {
    admin: 'danger',
    user: 'primary'
  }
  return types[role] || 'default'
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleToggleStatus = async (user) => {
  const newStatus = user.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  user._statusLoading = true
  try {
    await adminUserApi.updateUserStatus(user.id, newStatus)
    ElMessage.success(`用户已${action}`)
  } catch (error) {
    // 如果失败，恢复原状态
    user.status = user.status === 1 ? 0 : 1
    ElMessage.error(error?.message || `${action}失败`)
  } finally {
    user._statusLoading = false
  }
}

const handleRoleChange = async (user) => {
  const oldRole = user._originalRole // 获取原始角色
  const newRole = user.role // 获取新角色
  
  // 如果角色没有变化，直接返回
  if (oldRole === newRole) {
    return
  }
  
  user._roleLoading = true
  try {
    await adminUserApi.updateUser(user.id, { role: newRole })
    // 更新成功后，更新原始角色
    user._originalRole = newRole
    ElMessage.success(`用户角色已更新为${getRoleLabel(newRole)}`)
  } catch (error) {
    // 如果失败，恢复原角色
    user.role = oldRole
    ElMessage.error(error?.message || '更新角色失败')
  } finally {
    user._roleLoading = false
  }
}

const handleDeleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${user.username}"吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await adminUserApi.deleteUser(user.id)
    ElMessage.success('用户删除成功')
    loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error?.message || '删除用户失败')
    }
  }
}

const handlePageSizeChange = () => {
  pagination.page = 1
  loadUsers()
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.admin-user-management-page {
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

/* 表格容器 */
.table-container {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
  width: 100%;
  box-sizing: border-box;
  overflow: visible;
  position: relative;
}

.users-table {
  border-radius: 8px;
  width: 100%;
}

.users-table :deep(.el-table) {
  width: 100%;
}

.users-table :deep(.el-table__inner-wrapper) {
  overflow-x: auto;
  overflow-y: visible;
}

.users-table :deep(.el-table__body-wrapper) {
  overflow-x: auto;
  overflow-y: visible;
}

.users-table :deep(.el-table__header-wrapper) {
  overflow-x: auto;
  overflow-y: visible;
}

.users-table :deep(.el-table__body) {
  width: 100% !important;
}

.users-table :deep(.el-table__header) {
  width: 100% !important;
}

.users-table :deep(.el-table__header) {
  background: rgba(255, 255, 255, 0.95);
}

.users-table :deep(.el-table__header th) {
  background: rgba(255, 255, 255, 0.95) !important;
  color: var(--el-text-color-primary);
  font-weight: 600;
  border-bottom: 2px solid var(--el-border-color-lighter);
  text-align: center;
}

/* 固定列的背景色 */
.users-table :deep(.el-table__fixed),
.users-table :deep(.el-table__fixed-right) {
  background-color: #ffffff !important;
  z-index: 100 !important;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.08);
}

/* 确保右侧固定列（操作列）始终在最上层 */
.users-table :deep(.el-table__fixed-right) {
  z-index: 101 !important;
}

.users-table :deep(.el-table__fixed th),
.users-table :deep(.el-table__fixed-right th),
.users-table :deep(.el-table__fixed td),
.users-table :deep(.el-table__fixed-right td) {
  background-color: #ffffff !important;
  position: relative;
  z-index: 1;
}

.users-table :deep(.el-table__fixed-right-patch) {
  background-color: #ffffff !important;
  z-index: 101 !important;
}

.users-table :deep(.el-table__fixed-left) {
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  z-index: 100 !important;
}

.users-table :deep(.el-table__row:hover .el-table__fixed td),
.users-table :deep(.el-table__row:hover .el-table__fixed-right td) {
  background-color: #ffffff !important;
}

/* 确保操作列单元格和按钮始终可见 */
.users-table :deep(.el-table__fixed-right .el-table__cell) {
  z-index: 102 !important;
  position: relative;
}

.users-table :deep(.el-table__fixed-right .action-buttons) {
  position: relative;
  z-index: 103 !important;
}

.users-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.users-table :deep(.el-table__row:hover) {
  background: #f8fafc;
}

.row-number {
  font-weight: 600;
  color: var(--el-text-color-secondary);
}

.role-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 4px 8px;
}

.cell-content {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
  cursor: default;
}

.bio-text {
  color: var(--el-text-color-regular);
  font-style: italic;
}

.time-text {
  color: var(--el-text-color-regular);
}

.status-switch {
  transform: scale(1.1);
}

.action-buttons {
  display: flex;
  gap: 4px;
  justify-content: center;
  flex-wrap: nowrap;
}

.action-buttons .el-button {
  padding: 4px 8px;
  font-weight: 500;
  white-space: nowrap;
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
  .admin-user-management-page {
    padding: 24px;
  }
  
  .page-header {
    flex-wrap: wrap;
    gap: 16px;
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
}

@media (max-width: 768px) {
  .admin-user-management-page {
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
  
  .page-subtitle {
    font-size: 13px;
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
  
  .table-container {
    padding: 12px;
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .users-table {
    min-width: 800px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 4px;
  }
  
  .action-buttons .el-button {
    width: 100%;
    justify-content: center;
  }
  
  .pagination-container {
    padding: 12px 0;
    overflow-x: auto;
  }
  
  .simplified-pagination {
    min-width: 100%;
  }
  
  .simplified-pagination :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (max-width: 480px) {
  .admin-user-management-page {
    padding: 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .page-subtitle {
    font-size: 12px;
  }
  
  .search-filters-container {
    padding: 12px;
  }
  
  .table-container {
    padding: 8px;
  }
}

/* 动画效果 */
.users-table :deep(.el-table__row) {
  transition: background-color 0.3s ease;
}

.role-tag {
  transition: all 0.3s ease;
}

.role-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 加载状态 */
.users-table :deep(.el-loading-mask) {
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
}

/* 表格行悬停效果增强 */
.users-table :deep(.el-table__row:hover > td) {
  background-color: #f8fafc !important;
  transition: background-color 0.3s ease;
}

/* 修复 tooltip 显示位置问题 */
.users-table :deep(.el-tooltip__popper) {
  z-index: 9999 !important;
  max-width: 300px !important;
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal !important;
  line-height: 1.5;
  pointer-events: none;
}

/* 确保 tooltip 能够正确显示在表格上方 */
.table-container :deep(.el-table__body-wrapper) {
  overflow-x: auto;
  overflow-y: auto;
}

.table-container :deep(.el-table__header-wrapper) {
  overflow-x: auto;
  overflow-y: visible;
}

/* 修复固定列中的 tooltip */
.users-table :deep(.el-table__fixed .el-tooltip__popper),
.users-table :deep(.el-table__fixed-right .el-tooltip__popper) {
  z-index: 10000 !important;
}

/* 确保 tooltip 内容区域可以完整显示 */
.users-table :deep(.el-tooltip__popper .el-tooltip__content) {
  max-width: 300px;
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
}

/* 优化 tooltip 显示，使其更接近鼠标位置 */
.users-table :deep(.el-tooltip__popper.is-dark) {
  background-color: rgba(0, 0, 0, 0.85);
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
}
</style>
