<template>
  <Layout>
    <div class="admin-sensitive-words-page">
      <!-- 主标题区域 -->
      <div class="page-header">
        <div class="header-left">
          <h1 class="page-title">
            敏感词管理
            <el-tag :type="sensitiveWords.length > 0 ? 'success' : 'info'" size="small" class="count-tag">
              {{ pagination.total }} 个敏感词
            </el-tag>
          </h1>
          <p class="page-subtitle">管理心理健康领域的敏感词库，拦截不当内容，保护用户心理健康</p>
        </div>
        <div class="header-actions">
          <el-button @click="exportData" :loading="exporting" :icon="Download" size="large" class="action-button">
            导出数据
          </el-button>
          <el-button type="primary" @click="handleCreate" size="large" class="add-button">
            <el-icon><Plus /></el-icon>
            添加敏感词
          </el-button>
        </div>
      </div>


      <!-- 搜索和筛选 -->
      <div class="search-filters-container">
        <div class="search-flex-container">
          <div class="search-left">
            <el-input
              v-model="searchForm.keyword"
              placeholder="搜索敏感词内容..."
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
              v-model="searchForm.category"
              placeholder="分类"
              clearable
              @change="loadWords"
              class="filter-select"
            >
              <el-option label="全部分类" value="" />
              <el-option label="一般" value="general" />
              <el-option label="危机" value="crisis" />
              <el-option label="禁止" value="prohibited" />
            </el-select>
            
            <el-select
              v-model="searchForm.action"
              placeholder="方式"
              clearable
              @change="loadWords"
              class="filter-select"
            >
              <el-option label="全部方式" value="" />
              <el-option label="拦截" value="block" />
              <el-option label="警告" value="warn" />
              <el-option label="替换" value="replace" />
              <el-option label="危机干预" value="intervention" />
            </el-select>
            
            <el-select
              v-model="searchForm.isActive"
              placeholder="状态"
              clearable
              @change="loadWords"
              class="filter-select"
            >
              <el-option label="全部状态" :value="null" />
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>

          </div>
        </div>
      </div>

      <!-- 简洁现代的批量操作栏 -->
      <div class="batch-action-bar" v-if="allSelectedIds.length > 0">
        <div class="batch-info">
          <el-icon class="batch-icon"><Select /></el-icon>
          <span class="batch-text">
            已选择 <strong>{{ allSelectedIds.length }}</strong> 个敏感词
          </span>
        </div>
        <div class="batch-operations">
          <el-button 
            @click="batchUpdateStatus(1)" 
            :loading="batchLoading" 
            type="success" 
            size="default"
            :icon="Check"
          >
            启用
          </el-button>
          <el-button 
            @click="batchUpdateStatus(0)" 
            :loading="batchLoading" 
            type="warning" 
            size="default"
            :icon="Close"
          >
            禁用
          </el-button>
          <el-button 
            @click="batchDeleteWords" 
            :loading="batchLoading" 
            type="danger" 
            size="default"
            :icon="Delete"
          >
            删除
          </el-button>
          <el-button 
            @click="clearSelection" 
            size="default" 
            text
            class="clear-btn"
          >
            取消选择
          </el-button>
        </div>
      </div>

      <!-- 敏感词列表 -->
      <div class="table-container">
        <el-table 
          v-loading="loading" 
          :data="sensitiveWords" 
          class="sensitive-words-table"
          @selection-change="handleSelectionChange"
          @select-all="handleSelectAll"
          @row-dblclick="handleEdit"
          ref="tableRef"
          row-key="id"
        >
          <el-table-column 
            type="selection" 
            width="55" 
            align="center"
            fixed="left"
            :selectable="() => true"
          />
          
          <el-table-column label="序号" width="70" align="center" fixed="left">
            <template #default="{ $index }">
              <span class="row-number">{{ (pagination.page - 1) * pagination.size + $index + 1 }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="word" label="敏感词" width="120" show-overflow-tooltip>
            <template #default="{ row }">
              <div class="word-cell">
                <span class="word-text">{{ row.word }}</span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column prop="category" label="分类" width="90" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getCategoryTagType(row.category)"
                class="category-tag"
                size="small"
              >
                {{ getCategoryLabel(row.category) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="action" label="处理方式" width="120" align="center">
            <template #default="{ row }">
              <el-tag 
                :type="getActionTagType(row.action)"
                class="action-tag"
                size="small"
              >
                {{ getActionLabel(row.action) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column prop="replacement" label="替换词" width="100" show-overflow-tooltip>
            <template #default="{ row }">
              <span class="replacement-text">
                {{ row.replacement || '--' }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column prop="isActive" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.isActive"
                :active-value="1"
                :inactive-value="0"
                @change="toggleWordStatus(row)"
                :loading="row._statusLoading"
                class="status-switch"
              />
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <div class="action-buttons">
                <el-button 
                  size="small" 
                  type="primary" 
                  link
                  @click="handleEdit(row)"
                  :icon="Edit"
                >
                  编辑
                </el-button>
                <el-button 
                  size="small" 
                  type="primary" 
                  link
                  @click="duplicateWord(row)"
                  :icon="CopyDocument"
                >
                  复制
                </el-button>
                <el-button 
                  size="small" 
                  type="danger" 
                  link
                  @click="deleteWord(row)"
                  :icon="Delete"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 简化后的分页组件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 30]"
          layout="prev, pager, next, sizes, jumper"
          @size-change="handlePageSizeChange"
          @current-change="loadWords"
          class="simplified-pagination"
          background
          :hide-on-single-page="false"
        />
      </div>

      <!-- 创建/编辑对话框 -->
      <el-dialog
        v-model="showCreateDialog"
        :title="editingWord ? '编辑敏感词' : '添加敏感词'"
        width="500px"
        @close="resetForm"
      >
        <el-form :model="wordForm" label-width="100px" :rules="formRules" ref="formRef">
          <el-form-item label="敏感词" prop="word" required>
            <el-input v-model="wordForm.word" placeholder="请输入敏感词" />
          </el-form-item>
          <el-form-item label="分类" prop="category" required>
            <el-select v-model="wordForm.category" style="width: 100%">
              <el-option label="一般" value="general" />
              <el-option label="危机" value="crisis" />
              <el-option label="禁止" value="prohibited" />
            </el-select>
          </el-form-item>
          <el-form-item label="处理方式" prop="action" required>
            <el-select v-model="wordForm.action" style="width: 100%" @change="handleActionChange">
              <el-option label="拦截" value="block" />
              <el-option label="警告" value="warn" />
              <el-option label="替换" value="replace" />
              <el-option label="危机干预" value="intervention" />
            </el-select>
          </el-form-item>
          <el-form-item 
            v-if="wordForm.action === 'replace'" 
            label="替换词" 
            prop="replacement"
            required
          >
            <el-input v-model="wordForm.replacement" placeholder="请输入替换词" />
          </el-form-item>
          <el-form-item label="状态" prop="isActive">
            <el-select v-model="wordForm.isActive" style="width: 100%">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button type="primary" @click="saveWord" :loading="saving">保存</el-button>
        </template>
      </el-dialog>

    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Edit, Delete, CopyDocument, Plus, Check, Close, 
  RefreshRight, Grid, Refresh, Download, ArrowUp, ArrowDown, 
  InfoFilled, Select, Switch, WarningFilled, CircleCloseFilled,
  Management
} from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { sensitiveWordApi } from '@/api/sensitive'

const loading = ref(false)
const sensitiveWords = ref([])
const showCreateDialog = ref(false)
const editingWord = ref(null)
const saving = ref(false)
const formRef = ref(null)
const tableRef = ref(null)
const selectedWords = ref([])
const allSelectedIds = ref([]) // 存储所有选中的ID（包括其他页的）
const batchLoading = ref(false)
const searchTimer = ref(null)
const isSearching = ref(false)
const exporting = ref(false)


const pagination = ref({
  page: 1,
  size: 10, // 默认显示30条，更适合现代屏幕
  total: 0
})

const searchForm = ref({
  keyword: '',
  category: '',
  action: '',
  isActive: null
})

const wordForm = ref({
  word: '',
  category: 'general',
  action: 'block',
  replacement: '',
  isActive: 1
})

const formRules = {
  word: [
    { required: true, message: '请输入敏感词', trigger: 'blur' },
    { min: 1, max: 64, message: '敏感词长度在1到64个字符之间', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  action: [
    { required: true, message: '请选择处理方式', trigger: 'change' }
  ],
  replacement: [
    { required: true, message: '替换方式必须填写替换词', trigger: 'blur' }
  ]
}

const handleSearchChange = () => {
  // 优化防抖搜索，更快响应
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
  isSearching.value = true
  searchTimer.value = setTimeout(() => {
    pagination.value.page = 1
    loadWords().finally(() => {
      isSearching.value = false
    })
  }, 150) // 减少到150ms，提升搜索体验
}

const loadWords = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size
    }
    if (searchForm.value.keyword) {
      params.keyword = searchForm.value.keyword
    }
    if (searchForm.value.category) {
      params.category = searchForm.value.category
    }
    if (searchForm.value.action) {
      params.action = searchForm.value.action
    }
    if (searchForm.value.isActive !== null) {
      params.isActive = searchForm.value.isActive
    }
    
    const data = await sensitiveWordApi.getSensitiveWords(params)
    sensitiveWords.value = data.list || []
    pagination.value.total = data.total || 0
    updateStats()
    
    // 恢复当前页的选中状态
    await nextTick()
    if (tableRef.value && tableRef.value.toggleRowSelection) {
      if (allSelectedIds.value.length > 0) {
        sensitiveWords.value.forEach(row => {
          try {
            if (allSelectedIds.value.includes(row.id)) {
              tableRef.value.toggleRowSelection(row, true)
            } else {
              tableRef.value.toggleRowSelection(row, false)
            }
          } catch (e) {
            // 忽略单个行的错误
            console.warn('Toggle selection error:', e)
          }
        })
      } else {
        // 如果没有选中项，确保所有行都未选中
        sensitiveWords.value.forEach(row => {
          try {
            tableRef.value.toggleRowSelection(row, false)
          } catch (e) {
            // 忽略单个行的错误
            console.warn('Toggle selection error:', e)
          }
        })
      }
    }
  } catch (error) {
    ElMessage.error('加载敏感词列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  editingWord.value = null
  resetForm()
  showCreateDialog.value = true
}

const handleEdit = (word) => {
  editingWord.value = word
  wordForm.value = {
    word: word.word || '',
    category: word.category || 'general',
    action: word.action || 'block',
    replacement: word.replacement || '',
    isActive: word.isActive !== undefined ? word.isActive : 1
  }
  showCreateDialog.value = true
}

const resetForm = () => {
  wordForm.value = {
    word: '',
    category: 'general',
    action: 'block',
    replacement: '',
    isActive: 1
  }
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleActionChange = () => {
  // 当处理方式不是replace时，清空替换词
  if (wordForm.value.action !== 'replace') {
    wordForm.value.replacement = ''
  }
}

const saveWord = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    saving.value = true
    try {
      const submitData = {
        word: wordForm.value.word?.trim(),
        category: wordForm.value.category,
        action: wordForm.value.action,
        isActive: wordForm.value.isActive
      }
      
      // 只有当action为replace时，才需要replacement字段
      if (wordForm.value.action === 'replace') {
        submitData.replacement = wordForm.value.replacement?.trim() || ''
      }
      
      // 验证必填字段
      if (!submitData.word) {
        ElMessage.error('敏感词不能为空')
        return
      }
      if (!submitData.category) {
        ElMessage.error('分类不能为空')
        return
      }
      if (!submitData.action) {
        ElMessage.error('处理动作不能为空')
        return
      }
      
      if (editingWord.value) {
        await sensitiveWordApi.updateSensitiveWord(editingWord.value.id, submitData)
        ElMessage.success('更新成功')
      } else {
        await sensitiveWordApi.createSensitiveWord(submitData)
        ElMessage.success('添加成功')
      }
      showCreateDialog.value = false
      resetForm()
      loadWords()
    } catch (error) {
      ElMessage.error(editingWord.value ? '更新失败' : '添加失败')
      console.error(error)
    } finally {
      saving.value = false
    }
  } catch (error) {
    console.error('表单验证失败', error)
  }
}

const deleteWord = async (word) => {
  try {
    await ElMessageBox.confirm('确定要删除这个敏感词吗？', '确认删除', {
      type: 'warning'
    })
    await sensitiveWordApi.deleteSensitiveWord(word.id)
    ElMessage.success('删除成功')
    loadWords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 批量操作相关函数
const handleSelectionChange = (selection) => {
  try {
    selectedWords.value = selection
    // 更新当前页选中的ID
    const currentPageIds = selection.map(item => item.id)
    // 移除当前页的ID，然后添加新的
    allSelectedIds.value = allSelectedIds.value.filter(id => 
      !sensitiveWords.value.some(word => word.id === id)
    )
    allSelectedIds.value.push(...currentPageIds)
  } catch (error) {
    console.warn('Selection change error:', error)
  }
}

const handleSelectAll = async (selection) => {
  // 使用 nextTick 确保 DOM 更新完成后再操作
  await nextTick()
  
  if (selection.length > 0) {
    // 全选所有符合筛选条件的数据（非仅当前页）
    try {
      // 获取所有符合当前筛选条件的数据ID
      const params = {
        page: 1,
        size: 99999 // 获取所有数据
      }
      
      // 应用当前的筛选条件
      if (searchForm.value.keyword?.trim()) {
        params.keyword = searchForm.value.keyword.trim()
      }
      if (searchForm.value.category) {
        params.category = searchForm.value.category
      }
      if (searchForm.value.action) {
        params.action = searchForm.value.action
      }
      if (searchForm.value.isActive !== null) {
        params.isActive = searchForm.value.isActive
      }
      
      const data = await sensitiveWordApi.getSensitiveWords(params)
      const allFilteredIds = (data.list || []).map(item => item.id)
      
      // 全选所有符合筛选条件的数据ID
      allSelectedIds.value = [...allFilteredIds]
      
      // 确保当前页的相关行都被选中（视觉反馈）
      await nextTick()
      if (tableRef.value?.toggleRowSelection) {
        sensitiveWords.value.forEach(row => {
          const isSelected = allFilteredIds.includes(row.id)
          try {
            tableRef.value.toggleRowSelection(row, isSelected)
          } catch (e) {
            console.warn('Toggle selection error:', e)
          }
        })
      }
      
      // 用户友好的提示信息
      const currentPageCount = sensitiveWords.value.length
      const totalSelectedCount = allFilteredIds.length
      
      if (totalSelectedCount > currentPageCount) {
        ElMessage({
          message: `已全选符合条件的所有 ${totalSelectedCount} 个敏感词（包含其他页面数据）`,
          type: 'success',
          duration: 4000,
          showClose: true
        })
      } else if (totalSelectedCount > 0) {
        ElMessage.success(`已全选 ${totalSelectedCount} 个敏感词`)
      } else {
        ElMessage.info('当前筛选条件下没有可选择的敏感词')
      }
      
    } catch (error) {
      ElMessage.error('全选操作失败，请重试')
      console.error('Select all error:', error)
      // 降级处理：至少选择当前页
      const currentPageIds = sensitiveWords.value.map(item => item.id)
      allSelectedIds.value = [...new Set([...allSelectedIds.value, ...currentPageIds])]
    }
  } else {
    // 取消全选：智能清除相关选择
    try {
      const params = {
        page: 1,
        size: 99999
      }
      
      // 应用当前的筛选条件
      if (searchForm.value.keyword?.trim()) {
        params.keyword = searchForm.value.keyword.trim()
      }
      if (searchForm.value.category) {
        params.category = searchForm.value.category
      }
      if (searchForm.value.action) {
        params.action = searchForm.value.action
      }
      if (searchForm.value.isActive !== null) {
        params.isActive = searchForm.value.isActive
      }
      
      const data = await sensitiveWordApi.getSensitiveWords(params)
      const allFilteredIds = (data.list || []).map(item => item.id)
      
      // 从选中列表中移除所有符合当前筛选条件的ID
      const removedCount = allSelectedIds.value.filter(id => allFilteredIds.includes(id)).length
      allSelectedIds.value = allSelectedIds.value.filter(id => !allFilteredIds.includes(id))
      
      // 更新当前页的选中状态
      await nextTick()
      if (tableRef.value?.toggleRowSelection) {
        sensitiveWords.value.forEach(row => {
          if (allFilteredIds.includes(row.id)) {
            try {
              tableRef.value.toggleRowSelection(row, false)
            } catch (e) {
              console.warn('Toggle selection error:', e)
            }
          }
        })
      }
      
      // 静默取消选择，无需提示
      
    } catch (error) {
      console.error('Deselect all error:', error)
      // 降级处理：至少取消当前页的选择
      const currentPageIds = sensitiveWords.value.map(item => item.id)
      const removedCount = allSelectedIds.value.filter(id => currentPageIds.includes(id)).length
      allSelectedIds.value = allSelectedIds.value.filter(id => !currentPageIds.includes(id))
      
      // 静默取消选择，无需提示
    }
  }
}

const clearSelection = () => {
  if (tableRef.value) {
    tableRef.value.clearSelection()
  }
  selectedWords.value = []
  allSelectedIds.value = []
}

const batchUpdateStatus = async (status) => {
  const selectedCount = allSelectedIds.value.length
  if (selectedCount === 0) {
    ElMessage.warning('请选择要操作的敏感词')
    return
  }
  
  try {
    const statusText = status === 1 ? '启用' : '禁用'
    await ElMessageBox.confirm(`确定要批量${statusText}选中的 ${selectedCount} 个敏感词吗？`, `确认${statusText}`, {
      type: 'warning'
    })
    
    batchLoading.value = true
    await sensitiveWordApi.batchUpdateStatus(allSelectedIds.value, status)
    ElMessage.success(`批量${statusText}成功`)
    clearSelection()
    loadWords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`批量${status === 1 ? '启用' : '禁用'}失败`)
      console.error(error)
    }
  } finally {
    batchLoading.value = false
  }
}

const batchDeleteWords = async () => {
  const selectedCount = allSelectedIds.value.length
  if (selectedCount === 0) {
    ElMessage.warning('请选择要删除的敏感词')
    return
  }
  
  try {
    await ElMessageBox.confirm(`确定要批量删除选中的 ${selectedCount} 个敏感词吗？`, '确认删除', {
      type: 'warning'
    })
    
    batchLoading.value = true
    await sensitiveWordApi.batchDelete(allSelectedIds.value)
    ElMessage.success('批量删除成功')
    clearSelection()
    loadWords()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('批量删除失败')
      console.error(error)
    }
  } finally {
    batchLoading.value = false
  }
}

// 复制敏感词
const duplicateWord = async (word) => {
  try {
    const newWordData = {
      word: `${word.word}_副本`,
      category: word.category,
      action: word.action,
      replacement: word.replacement,
      isActive: word.isActive
    }
    
    await sensitiveWordApi.createSensitiveWord(newWordData)
    ElMessage.success('复制成功')
    loadWords()
  } catch (error) {
    ElMessage.error('复制失败')
    console.error(error)
  }
}

// 切换敏感词状态
const toggleWordStatus = async (word) => {
  word._statusLoading = true
  try {
    await sensitiveWordApi.updateSensitiveWordStatus(word.id, word.isActive)
    ElMessage.success(word.isActive === 1 ? '已启用' : '已禁用')
  } catch (error) {
    // 回滚状态
    word.isActive = word.isActive === 1 ? 0 : 1
    ElMessage.error('状态更新失败')
    console.error(error)
  } finally {
    word._statusLoading = false
  }
}

const getActionLabel = (action) => {
  const map = {
    block: '拦截',
    warn: '警告',
    replace: '替换',
    intervention: '危机干预'
  }
  return map[action] || action
}

const getActionTagType = (action) => {
  const map = {
    block: 'danger',
    warn: 'warning',
    replace: 'info',
    intervention: 'danger'
  }
  return map[action] || ''
}

const getCategoryLabel = (category) => {
  const map = {
    general: '一般',
    crisis: '危机',
    prohibited: '禁止'
  }
  return map[category] || category
}

const getCategoryTagType = (category) => {
  const map = {
    general: 'info',
    crisis: 'danger',
    prohibited: 'warning'
  }
  return map[category] || ''
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// ============ 新增方法 ============


// 智能筛选相关
const quickFilter = (field, value) => {
  if (searchForm.value[field] === value) {
    searchForm.value[field] = field === 'isActive' ? null : ''
  } else {
    searchForm.value[field] = value
  }
  loadWords()
}

const clearFilters = () => {
  searchForm.value = {
    keyword: '',
    category: '',
    action: '',
    isActive: null
  }
  loadWords()
}

const hasActiveFilters = computed(() => {
  return searchForm.value.category || 
         searchForm.value.action || 
         searchForm.value.isActive !== null
})

// 增强的分页处理
const jumpToPage = (page) => {
  pagination.value.page = page
  loadWords()
}

const handlePageSizeChange = () => {
  pagination.value.page = 1
  loadWords()
}


// ============ 统计相关方法 ============

// 统计数据缓存
const categoryStats = ref({})
const actionStats = ref({})
const statusStats = ref({})

// 更新统计数据
const updateStats = () => {
  const stats = {
    category: {},
    action: {},
    status: { active: 0, inactive: 0 }
  }
  
  sensitiveWords.value.forEach(word => {
    // 分类统计
    stats.category[word.category] = (stats.category[word.category] || 0) + 1
    // 处理方式统计
    stats.action[word.action] = (stats.action[word.action] || 0) + 1
    // 状态统计
    if (word.isActive === 1) {
      stats.status.active++
    } else {
      stats.status.inactive++
    }
  })
  
  categoryStats.value = stats.category
  actionStats.value = stats.action
  statusStats.value = stats.status
}

// 获取分类统计数量
const getCategoryCount = (category) => {
  return categoryStats.value[category] || 0
}

// 获取处理方式统计数量  
const getActionCount = (action) => {
  return actionStats.value[action] || 0
}

// 获取状态统计数量
const getStatusCount = (status) => {
  return status === 1 ? statusStats.value.active || 0 : statusStats.value.inactive || 0
}

// 导出数据功能
const exportData = async () => {
  exporting.value = true
  try {
    // 获取所有数据
    const params = {
      page: 1,
      size: 9999, // 获取所有数据
      ...searchForm.value
    }
    const data = await sensitiveWordApi.getSensitiveWords(params)
    
    // 生成CSV内容
    const csvContent = generateCSV(data.list)
    downloadCSV(csvContent, '敏感词列表.csv')
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
    console.error(error)
  } finally {
    exporting.value = false
  }
}

const generateCSV = (data) => {
  const headers = ['敏感词', '分类', '处理方式', '替换词', '状态', '创建时间']
  const rows = data.map(item => [
    item.word,
    getCategoryLabel(item.category),
    getActionLabel(item.action),
    item.replacement || '',
    item.isActive ? '启用' : '禁用',
    item.createdAt || ''
  ])
  
  const csvContent = [headers, ...rows]
    .map(row => row.map(field => `"${field}"`).join(','))
    .join('\n')
  
  return '\uFEFF' + csvContent // 添加BOM以支持中文
}

const downloadCSV = (csvContent, filename) => {
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', filename)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

onMounted(() => {
  loadWords()
})
</script>

<style scoped>
.admin-sensitive-words-page {
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

.page-header h1 {
  font-size: 26px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.header-actions .el-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
}

.action-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
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

.search-row {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.search-input-wrapper {
  flex: 1;
  max-width: 400px;
}


.filters-wrapper {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.filter-select {
  width: 150px;
}

.filter-select :deep(.el-select__wrapper) {
  border-radius: 8px;
}

/* 批量操作区域 */
.batch-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 10px;
  margin-top: 16px;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.batch-info {
  font-size: 14px;
  font-weight: 500;
}

.selected-count {
  font-size: 16px;
  font-weight: 600;
  color: #ffd700;
  margin: 0 4px;
}

.batch-buttons {
  display: flex;
  gap: 8px;
}

.batch-buttons .el-button {
  border: 1px solid rgba(255, 255, 255, 0.3);
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border-radius: 6px;
}

.batch-buttons .el-button:hover {
  background: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.5);
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
  overflow: hidden;
}

.sensitive-words-table {
  border-radius: 8px;
  width: 100%;
}

.sensitive-words-table :deep(.el-table) {
  width: 100%;
}

.sensitive-words-table :deep(.el-table__inner-wrapper) {
  overflow-x: auto;
  overflow-y: visible;
}

.sensitive-words-table :deep(.el-table__body-wrapper) {
  overflow-x: auto;
  overflow-y: visible;
}

.sensitive-words-table :deep(.el-table__header-wrapper) {
  overflow-x: auto;
  overflow-y: visible;
}

.sensitive-words-table :deep(.el-table__body) {
  width: 100% !important;
}

.sensitive-words-table :deep(.el-table__header) {
  width: 100% !important;
}

.sensitive-words-table :deep(.el-table__header) {
  background: rgba(255, 255, 255, 0.95);
}

.sensitive-words-table :deep(.el-table__header th) {
  background: rgba(255, 255, 255, 0.95) !important;
  color: var(--el-text-color-primary);
  font-weight: 600;
  border-bottom: 2px solid var(--el-border-color-lighter);
}

/* 固定列的背景色 */
.sensitive-words-table :deep(.el-table__fixed),
.sensitive-words-table :deep(.el-table__fixed-right) {
  background-color: rgba(255, 255, 255, 0.95) !important;
  z-index: 100 !important;
}

/* 确保右侧固定列（操作列）始终在最上层 */
.sensitive-words-table :deep(.el-table__fixed-right) {
  z-index: 101 !important;
}

.sensitive-words-table :deep(.el-table__fixed th),
.sensitive-words-table :deep(.el-table__fixed-right th),
.sensitive-words-table :deep(.el-table__fixed td),
.sensitive-words-table :deep(.el-table__fixed-right td) {
  background-color: rgba(255, 255, 255, 0.95) !important;
  position: relative;
  z-index: 1;
}

.sensitive-words-table :deep(.el-table__fixed-right-patch) {
  background-color: rgba(255, 255, 255, 0.95) !important;
  z-index: 101 !important;
}

.sensitive-words-table :deep(.el-table__fixed-left) {
  z-index: 100 !important;
}

/* 确保操作列单元格和按钮始终可见 */
.sensitive-words-table :deep(.el-table__fixed-right .el-table__cell) {
  z-index: 102 !important;
  position: relative;
}

.sensitive-words-table :deep(.el-table__fixed-right .action-buttons) {
  position: relative;
  z-index: 103 !important;
}

.sensitive-words-table :deep(.el-table__row) {
  transition: all 0.3s;
}

.sensitive-words-table :deep(.el-table__row:hover) {
  background: #f8fafc;
}

.row-number {
  font-weight: 600;
  color: var(--el-text-color-secondary);
}

.word-cell {
  padding: 8px 0;
}

.word-text {
  font-weight: 500;
  font-size: 14px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #667eea20, #764ba220);
  border-radius: 6px;
  border-left: 3px solid var(--el-color-primary);
}

.category-tag, .action-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 4px 8px;
}

.replacement-text {
  font-style: italic;
  color: var(--el-text-color-regular);
  background: #f0f2f5;
  padding: 4px 8px;
  border-radius: 4px;
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

.pagination-wrapper {
  background: white;
  border-radius: 12px;
  padding: 16px 24px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.pagination-info {
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.table-pagination {
  flex-shrink: 0;
}

.table-pagination :deep(.el-pager li) {
  border-radius: 6px;
  margin: 0 2px;
}

.table-pagination :deep(.el-pagination__sizes .el-select .el-input) {
  border-radius: 6px;
}

/* 对话框样式 - 匹配全局样式 */
.admin-sensitive-words-page :deep(.el-dialog) {
  background-color: var(--el-bg-color) !important;
  border-radius: 12px !important;
  box-shadow: var(--el-box-shadow) !important;
}

.admin-sensitive-words-page :deep(.el-dialog__header) {
  padding: 20px 24px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  background-color: transparent;
}

.admin-sensitive-words-page :deep(.el-dialog__title) {
  color: var(--el-text-color-primary) !important;
  font-weight: 600;
  font-size: 18px;
}

.admin-sensitive-words-page :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: var(--el-text-color-regular);
}

.admin-sensitive-words-page :deep(.el-dialog__headerbtn:hover .el-dialog__close) {
  color: var(--el-color-primary);
}

.admin-sensitive-words-page :deep(.el-dialog__body) {
  padding: 24px;
  color: var(--el-text-color-regular) !important;
}

.admin-sensitive-words-page :deep(.el-form-item__label) {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.admin-sensitive-words-page :deep(.el-input__wrapper) {
  border-radius: 6px;
  background-color: #FFFFFF !important;
  box-shadow: 0 0 0 1px var(--el-border-color) inset !important;
}

.admin-sensitive-words-page :deep(.el-select .el-input .el-input__wrapper) {
  border-radius: 6px;
  background-color: #FFFFFF !important;
  box-shadow: 0 0 0 1px var(--el-border-color) inset !important;
}

.admin-sensitive-words-page :deep(.el-dialog__footer) {
  padding: 16px 24px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.admin-sensitive-words-page :deep(.el-dialog__footer .el-button) {
  border-radius: 6px;
  padding: 8px 16px;
}

.import-tips {
  margin-bottom: 16px;
}

.import-tips ul {
  margin: 8px 0;
  padding-left: 20px;
}

.import-tips li {
  margin: 4px 0;
  font-family: 'Monaco', 'Consolas', monospace;
  color: var(--el-text-color-regular);
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
  margin: 2px 0;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .admin-sensitive-words-page {
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
  
  .header-actions .el-button {
    flex: 1;
    min-width: 120px;
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
  .admin-sensitive-words-page {
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
    min-width: auto;
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
  
  .sensitive-words-table {
    min-width: 800px;
  }
  
  .batch-action-bar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
    padding: 16px;
  }
  
  .batch-info {
    justify-content: center;
    text-align: center;
  }
  
  .batch-operations {
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;
  }
  
  .batch-operations .el-button {
    flex: 1;
    min-width: 80px;
    padding: 8px 12px;
    font-size: 13px;
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
  .admin-sensitive-words-page {
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
  
  .table-container {
    padding: 8px;
  }
  
  .batch-action-bar {
    padding: 12px;
  }
  
  .batch-operations .el-button {
    font-size: 12px;
    padding: 6px 10px;
  }
}

/* 动画效果 */
.el-table :deep(.el-table__row) {
  transition: background-color 0.3s ease;
}

.category-tag, .action-tag {
  transition: all 0.3s ease;
}

.category-tag:hover, .action-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.word-text {
  transition: all 0.3s ease;
}

.word-cell:hover .word-text {
  background: linear-gradient(135deg, #667eea30, #764ba230);
  transform: translateX(2px);
}

/* 加载状态 */
.el-loading-spinner {
  margin-top: -25px;
}

.el-loading-spinner .circular {
  width: 42px;
  height: 42px;
}

/* ============ 简化样式优化 ============ */

/* 简洁现代的批量操作栏 */
.batch-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: var(--el-box-shadow-light);
  animation: slideInFromTop 0.3s ease-out;
}

.batch-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-primary);
  font-size: 14px;
}

.batch-icon {
  color: var(--el-color-primary);
  font-size: 16px;
}

.batch-text {
  font-weight: 500;
}

.batch-text strong {
  color: var(--el-color-primary);
  font-weight: 600;
  margin: 0 2px;
}

.cross-page-tag {
  margin-left: 8px;
}

.batch-operations {
  display: flex;
  gap: 8px;
  align-items: center;
}

.batch-operations .el-button {
  border-radius: 6px;
  font-weight: 500;
  padding: 8px 16px;
}

.clear-btn {
  color: var(--el-text-color-regular) !important;
  padding: 8px 12px !important;
}

.clear-btn:hover {
  color: var(--el-color-primary) !important;
  background-color: var(--el-color-primary-light-9) !important;
}

@keyframes slideInFromTop {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 表格选择框样式优化 */
.sensitive-words-table :deep(.el-table__column--selection .cell) {
  padding: 0 14px;
}

.sensitive-words-table :deep(.el-checkbox) {
  transform: scale(1.1);
}

.sensitive-words-table :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

/* 全选提示样式 */
.el-message.is-success {
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(103, 194, 58, 0.3);
}

/* 加载状态优化 */
.sensitive-words-table :deep(.el-loading-mask) {
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
}

/* 表格行悬停效果增强 */
.sensitive-words-table :deep(.el-table__row:hover > td) {
  background-color: #f8fafc !important;
  transition: background-color 0.3s ease;
}

/* 分页选择器优化 */
.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.selector-label {
  font-size: 13px;
  color: var(--el-text-color-regular);
  font-weight: 500;
  white-space: nowrap;
}

.filter-select-enhanced {
  min-width: 120px;
}

.filter-select-enhanced :deep(.el-select__wrapper) {
  border-radius: 6px;
  border: none;
  box-shadow: none;
  background: white;
}

/* 表格双击提示 */
.sensitive-words-table :deep(.el-table__row) {
  cursor: pointer;
  transition: background-color 0.3s;
}

.sensitive-words-table :deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}


/* 自定义滚动条 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 加载和交互动画 */
.el-loading-spinner {
  margin-top: -25px;
}

.el-loading-spinner .circular {
  width: 42px;
  height: 42px;
}

/* 表格动画 */
.sensitive-words-table-enhanced :deep(.el-table__body tr) {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 快速筛选标签动画 */
.filter-chip {
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-chip:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 选中状态样式 */
.filter-chip[effect="dark"] {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

/* 全选提示动画 */
.batch-action-bar {
  transition: all 0.3s ease;
}

.batch-action-bar:hover {
  box-shadow: var(--el-box-shadow);
  border-color: var(--el-border-color-hover);
}

/* 操作按钮状态 */
.batch-operations .el-button--success {
  background-color: var(--el-color-success);
  border-color: var(--el-color-success);
}

.batch-operations .el-button--warning {
  background-color: var(--el-color-warning);
  border-color: var(--el-color-warning);
}

.batch-operations .el-button--danger {
  background-color: var(--el-color-danger);
  border-color: var(--el-color-danger);
}

/* 加载状态优化 */
.batch-operations .el-button.is-loading {
  opacity: 0.7;
}

</style>

