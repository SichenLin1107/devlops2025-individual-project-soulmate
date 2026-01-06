<template>
  <Layout>
    <div class="workflow-editor">
      <!-- 顶部工具栏 -->
      <header class="editor-toolbar">
        <!-- 第一行：基本操作 -->
        <div class="toolbar-row toolbar-main">
          <div class="toolbar-left">
            <button class="back-btn" @click="goBack">
              <el-icon><ArrowLeft /></el-icon>
              <span>返回</span>
            </button>
            <div class="workflow-name-wrapper">
              <input 
                v-model="workflowName" 
                class="workflow-name-input"
                placeholder="请输入工作流名称..."
                @blur="markChanged"
              />
              <span v-if="hasUnsavedChanges" class="unsaved-indicator">
                <span class="dot"></span>未保存
              </span>
            </div>
          </div>
          
          <div class="toolbar-center">
            <div class="toolbar-actions">
              <button class="tool-btn" :disabled="!canUndo" @click="undo" title="撤销 (Ctrl+Z)">
                <el-icon><RefreshLeft /></el-icon>
              </button>
              <button class="tool-btn" :disabled="!canRedo" @click="redo" title="重做 (Ctrl+Y)">
                <el-icon><RefreshRight /></el-icon>
              </button>
              <div class="divider"></div>
              <button class="tool-btn" @click="autoLayout" title="自动排列">
                <el-icon><Grid /></el-icon>
              </button>
              <button class="tool-btn" @click="fitViewAndCenter" title="居中">
                <el-icon><FullScreen /></el-icon>
              </button>
              <div class="divider"></div>
              <!-- 缩放控制 -->
              <div class="zoom-control">
                <button class="zoom-btn" @click="zoomOut" title="缩小" :disabled="viewportZoom <= 0.2">
                  <el-icon><Minus /></el-icon>
                </button>
                <el-dropdown trigger="click" @command="setZoom" class="zoom-dropdown">
                  <span class="zoom-display">{{ Math.round(viewportZoom * 100) }}%</span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item :command="0.5">50%</el-dropdown-item>
                      <el-dropdown-item :command="0.75">75%</el-dropdown-item>
                      <el-dropdown-item :command="1">100%</el-dropdown-item>
                      <el-dropdown-item :command="1.25">125%</el-dropdown-item>
                      <el-dropdown-item :command="1.5">150%</el-dropdown-item>
                      <el-dropdown-item :command="2">200%</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <button class="zoom-btn" @click="zoomIn" title="放大" :disabled="viewportZoom >= 2">
                  <el-icon><Plus /></el-icon>
                </button>
              </div>
            </div>
          </div>

          <div class="toolbar-right">
            <button class="action-btn secondary" @click="validateWorkflow" :disabled="validating">
              <el-icon v-if="!validating"><CircleCheck /></el-icon>
              <el-icon v-else class="is-loading"><Loading /></el-icon>
              <span>验证</span>
            </button>
            <button class="action-btn primary" @click="saveWorkflow" :disabled="saving">
              <el-icon v-if="!saving"><Check /></el-icon>
              <el-icon v-else class="is-loading"><Loading /></el-icon>
              <span>保存发布</span>
            </button>
          </div>
        </div>
      </header>

    <!-- 主编辑区域 -->
    <main class="editor-main">
      <!-- 左侧节点工具箱 -->
      <div class="node-toolbox-panel" :class="{ 'is-expanded': nodeToolboxExpanded }">
        <button class="toolbox-toggle-btn" @click="toggleNodeToolbox" :title="nodeToolboxExpanded ? '收起节点面板' : '展开节点面板'">
          <el-icon><Plus /></el-icon>
          <span v-if="!nodeToolboxExpanded">添加节点</span>
          <el-icon class="toggle-arrow" :class="{ 'is-expanded': nodeToolboxExpanded }">
            <ArrowDown />
          </el-icon>
        </button>
        
        <transition name="toolbox-slide">
          <div v-if="nodeToolboxExpanded" class="toolbox-content">
            <div class="toolbox-category" v-for="cat in filteredNodeCategories" :key="cat.name">
              <div class="category-header">{{ cat.label }}</div>
              <div class="category-nodes">
                <div 
                  v-for="node in cat.nodes" 
                  :key="node.type"
                  class="draggable-node"
                  :style="{ '--node-color': node.color }"
                  draggable="true"
                  @dragstart="onDragStart($event, node)"
                >
                  <div class="node-icon">
                    <el-icon><component :is="node.icon" /></el-icon>
                  </div>
                  <div class="node-info">
                    <span class="node-name">{{ node.name }}</span>
                    <span class="node-desc">{{ getNodeDescription(node.type) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>

      <!-- 画布区域 -->
      <section 
        class="canvas-container" 
        ref="canvasRef"
        @drop="onDrop" 
        @dragover.prevent
        @dragenter.prevent
      >
        <VueFlow
          :nodes="nodes"
          :edges="edges"
          :node-types="nodeTypes"
          :edge-types="edgeTypes"
          :default-edge-options="defaultEdgeOptions"
          :connection-line-style="connectionLineStyle"
          :snap-to-grid="true"
          :snap-grid="[20, 20]"
          :fit-view-on-init="false"
          :min-zoom="0.2"
          :max-zoom="2"
          :connection-mode="ConnectionMode.Loose"
          @connect="onConnect"
          @node-click="onNodeClick"
          @edge-click="onEdgeClick"
          @pane-click="onPaneClick"
          @nodes-change="onNodesChange"
          @edges-change="onEdgesChange"
          @viewport-change="onViewportChange"
        >
          <!-- 网格背景 - 使用点状网格 -->
          <Background 
            :variant="BackgroundVariant.Dots"
            :pattern-color="'rgba(74, 144, 164, 0.15)'" 
            :gap="20" 
            :size="2"
          />
          <MiniMap 
            position="bottom-right"
            :pannable="true"
            :zoomable="true"
            :node-color="getMiniMapNodeColor"
          />
          
          <!-- 空状态提示 -->
          <template v-if="nodes.length === 0">
            <div class="empty-canvas-hint">
              <el-icon :size="48"><Operation /></el-icon>
              <h3>开始构建工作流</h3>
              <p>从左侧拖拽节点到画布中，或点击下方快速开始</p>
              <button class="quick-start-btn" @click="initNewWorkflow">
                <el-icon><Plus /></el-icon>
                快速创建基础工作流
              </button>
            </div>
          </template>
        </VueFlow>
      </section>

      <!-- 右侧配置面板 - 仅用于节点配置 -->
      <transition name="slide-right">
        <aside v-if="selectedNode" class="config-panel">
          <div class="panel-header">
            <div class="panel-title">
              <div 
                class="title-icon"
                :style="{ backgroundColor: getSelectedNodeColor + '20', color: getSelectedNodeColor }"
              >
                <el-icon><component :is="getSelectedNodeIcon" /></el-icon>
              </div>
              <span>{{ selectedNode.data.label }}</span>
            </div>
            <button class="close-btn" @click="closePanel">
              <el-icon><Close /></el-icon>
            </button>
          </div>
          
          <div class="panel-body">
            <NodeConfig
              :node="selectedNode"
              :definition="getNodeDefinition(selectedNode.data.nodeType)"
              @update="onNodeUpdate"
              @delete="deleteSelectedNode"
            />
          </div>
        </aside>
      </transition>
    </main>

      <!-- 快捷键提示 -->
      <!-- 快捷键提示 -->
      <div class="shortcuts-hint" v-show="showShortcuts">
        <div class="shortcut-item"><kbd>Ctrl</kbd> + <kbd>Z</kbd> 撤销</div>
        <div class="shortcut-item"><kbd>Ctrl</kbd> + <kbd>Y</kbd> 重做</div>
        <div class="shortcut-item"><kbd>Ctrl</kbd> + <kbd>S</kbd> 保存</div>
        <div class="shortcut-item"><kbd>Delete</kbd> 删除选中</div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick, markRaw } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { VueFlow, useVueFlow, ConnectionMode } from '@vue-flow/core'
import { Background, BackgroundVariant } from '@vue-flow/background'
import { MiniMap } from '@vue-flow/minimap'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft, ArrowDown, RefreshLeft, RefreshRight, Grid, FullScreen,
  CircleCheck, Check, Search, Close, Delete, Plus, Minus,
  Loading, Operation, Setting, ChatDotRound, Filter, EditPen,
  Finished, Connection, Document, Warning, Promotion, Box
} from '@element-plus/icons-vue'

import Layout from '@/components/Layout.vue'
import CustomNode from './nodes/CustomNode.vue'
import SmoothEdge from './edges/SmoothEdge.vue'
import NodeConfig from './components/NodeConfig.vue'
import { workflowApi } from '@/api/workflow'

// Vue Flow 实例
const { 
  fitView: flowFitView, 
  project, 
  setViewport,
  getViewport,
  onPaneReady,
  setNodes,
  setEdges,
  addNodes,
  addEdges,
  applyNodeChanges,
  applyEdgeChanges,
  nodes,
  edges
} = useVueFlow()

const route = useRoute()
const router = useRouter()
const canvasRef = ref(null)

// 基础状态
const workflowId = computed(() => route.params.id)
const workflowName = ref('新建工作流')
const saving = ref(false)
const validating = ref(false)
const showShortcuts = ref(false)
const hasUnsavedChanges = ref(false)
const viewportZoom = ref(1)
const nodeSearchQuery = ref('')
const nodeToolboxExpanded = ref(false) // 节点工具箱默认收起

// 切换节点工具箱展开/收起
const toggleNodeToolbox = () => {
  nodeToolboxExpanded.value = !nodeToolboxExpanded.value
}

// 节点和边数据（从 useVueFlow 获取）
// const nodes 和 const edges 已从 useVueFlow() 获取
const nodeDefinitions = ref([])
const selectedNodeId = ref(null)
const selectedEdgeId = ref(null)

// 节点类型组件映射
const nodeTypes = {
  custom: markRaw(CustomNode)
}

// 边类型组件映射
const edgeTypes = {
  smoothstep: markRaw(SmoothEdge)
}

// 默认边选项
const defaultEdgeOptions = {
  type: 'smoothstep',
  animated: true,
  style: { strokeWidth: 2 }
}

// 连接线样式
const connectionLineStyle = {
  stroke: '#4A90A4',
  strokeWidth: 2,
  strokeDasharray: '5 5'
}

// 选中的实体
const selectedNode = computed(() => nodes.value.find(n => n.id === selectedNodeId.value))
const selectedEdge = computed(() => edges.value.find(e => e.id === selectedEdgeId.value))

// 获取选中节点的颜色
const getSelectedNodeColor = computed(() => {
  if (!selectedNode.value) return '#4A90A4'
  return selectedNode.value.data.color || '#4A90A4'
})

// 获取选中节点的图标
const getSelectedNodeIcon = computed(() => {
  if (!selectedNode.value) return 'Setting'
  return selectedNode.value.data.icon || 'Setting'
})

// 历史记录（撤回重做）- 参照参考项目实现
const history = ref([])
const historyIndex = ref(-1)
const maxHistorySize = 50
const isUndoRedo = ref(false) // 标记是否正在执行撤销/重做操作
const isInitialized = ref(false) // 标记历史记录是否已初始化
let saveHistoryTimer = null // 防抖定时器

// 是否可以撤销（不能撤销到初始状态之前）
const canUndo = computed(() => isInitialized.value && historyIndex.value > 0)
// 是否可以重做
const canRedo = computed(() => historyIndex.value < history.value.length - 1)

// 节点图标映射
const iconMap = {
  start: 'Promotion',
  text_process: 'Document',
  safety_check: 'Warning',
  sensitive_word_check: 'Filter',
  emotion_recognition: 'ChatDotRound',
  crisis_intervention: 'Warning',
  rag_retrieval: 'Search',
  llm_process: 'EditPen',
  llm_generation: 'EditPen',
  post_process: 'Setting',
  end: 'Finished'
}

// 节点颜色映射 - 使用柔和浅色调
const colorMap = {
  start: '#8FBC8F',        // 柔和绿
  text_process: '#9CB4CC', // 柔和蓝
  safety_check: '#E8C49A', // 柔和橙
  sensitive_word_check: '#E8C49A',
  emotion_recognition: '#7EB8C9', // 柔和青
  crisis_intervention: '#D4A5A5', // 柔和红
  rag_retrieval: '#A8C5D8',  // 浅蓝
  llm_process: '#7EB8C9',    // 柔和青
  llm_generation: '#7EB8C9',
  post_process: '#B8AFA0',   // 柔和灰
  end: '#D4A5A5'             // 柔和红
}

// 节点分类
const nodeCategories = ref([
  { name: 'control', label: '流程控制', collapsed: false, nodes: [] },
  { name: 'safety', label: '安全检测', collapsed: false, nodes: [] },
  { name: 'llm', label: 'LLM处理', collapsed: false, nodes: [] },
  { name: 'retrieval', label: '知识检索', collapsed: false, nodes: [] },
  { name: 'process', label: '数据处理', collapsed: false, nodes: [] }
])

// 节点描述
const nodeDescriptions = {
  start: '接收用户输入消息',
  text_process: '文本预处理和清洗',
  safety_check: '内容安全检测与过滤',
  sensitive_word_check: '敏感词过滤检测',
  emotion_recognition: '分析用户情绪状态',
  crisis_intervention: '危机干预处理',
  rag_retrieval: '从知识库检索相关内容',
  llm_process: '调用AI模型生成回复',
  llm_generation: 'LLM生成回复',
  post_process: '优化和美化AI回复',
  end: '输出最终回复给用户'
}

// 过滤后的节点分类
const filteredNodeCategories = computed(() => {
  if (!nodeSearchQuery.value) return nodeCategories.value
  
  const query = nodeSearchQuery.value.toLowerCase()
  return nodeCategories.value.map(cat => ({
    ...cat,
    collapsed: false,
    nodes: cat.nodes.filter(node => 
      node.name.toLowerCase().includes(query) ||
      (node.description || '').toLowerCase().includes(query)
    )
  })).filter(cat => cat.nodes.length > 0)
})

// 所有可拖拽的节点（用于横向工具栏）
const allDraggableNodes = computed(() => {
  const nodes = []
  nodeCategories.value.forEach(cat => {
    nodes.push(...cat.nodes)
  })
  return nodes
})

// 获取节点描述
const getNodeDescription = (type) => nodeDescriptions[type] || '节点描述'

// 获取节点标签
const getNodeLabel = (nodeId) => {
  const node = nodes.value.find(n => n.id === nodeId)
  return node?.data?.label || nodeId
}

// 获取 MiniMap 节点颜色
const getMiniMapNodeColor = (node) => {
  return node.data?.color || '#4A90A4'
}

// 分类节点到对应分类
const categorizeNodes = (defs) => {
  const categoryMap = {
    start: 'control',
    end: 'control',
    text_process: 'process',
    post_process: 'process',
    safety_check: 'safety',
    sensitive_word_check: 'safety',
    crisis_intervention: 'safety',
    llm_process: 'llm',
    llm_generation: 'llm',
    rag_retrieval: 'retrieval'
    // 注意：移除了 emotion_recognition，如需要可添加到其他分类
  }
  
  // 清空现有节点
  nodeCategories.value.forEach(cat => cat.nodes = [])
  
  defs.forEach(def => {
    const catName = def.category || categoryMap[def.type] || 'process'
    const category = nodeCategories.value.find(c => c.name === catName)
    if (category) {
      category.nodes.push({
        ...def,
        icon: iconMap[def.type] || 'Setting',
        color: colorMap[def.type] || '#4A90A4'
      })
    }
  })
}

// 初始化
onMounted(async () => {
  await fetchDefinitions()
  if (workflowId.value) {
    await loadWorkflow()
  } else {
    initNewWorkflow()
  }
  
  window.addEventListener('keydown', handleKeyDown)
  
  // 初始化历史记录
  await nextTick()
  saveHistory()
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown)
  // 清理历史记录保存定时器
  if (saveHistoryTimer) {
    clearTimeout(saveHistoryTimer)
    saveHistoryTimer = null
  }
})

// 监听路由参数变化
watch(workflowId, async (newId, oldId) => {
  if (oldId === undefined && newId !== undefined) return
  if (newId !== oldId) {
    await fetchDefinitions()
    if (newId) {
      await loadWorkflow()
    } else {
      initNewWorkflow()
    }
    saveHistory()
  }
})

const handleKeyDown = (e) => {
  // 检查事件目标是否是输入框、文本域或其他可编辑元素
  const target = e.target
  const isEditableElement = target && (
    target.tagName === 'INPUT' ||
    target.tagName === 'TEXTAREA' ||
    target.tagName === 'SELECT' ||
    target.isContentEditable ||
    target.closest('input, textarea, select, [contenteditable="true"]')
  )
  
  // 如果在可编辑元素中，只处理保存快捷键，其他快捷键让浏览器默认处理
  if (isEditableElement) {
    // 保存快捷键仍然可以工作（即使是在输入框中）
    if ((e.ctrlKey || e.metaKey) && e.key === 's') {
      e.preventDefault()
      saveWorkflow()
    }
    // 其他快捷键在可编辑元素中不处理，让浏览器默认行为生效
    return
  }
  
  // 撤销
  if ((e.ctrlKey || e.metaKey) && e.key === 'z' && !e.shiftKey) {
    e.preventDefault()
    undo()
  }
  // 重做
  else if ((e.ctrlKey || e.metaKey) && (e.key === 'y' || (e.key === 'z' && e.shiftKey))) {
    e.preventDefault()
    redo()
  }
  // 保存
  else if ((e.ctrlKey || e.metaKey) && e.key === 's') {
    e.preventDefault()
    saveWorkflow()
  }
  // 删除 - 只有在非可编辑元素中才触发删除节点
  else if (e.key === 'Delete' || e.key === 'Backspace') {
    if (selectedNodeId.value && !['start', 'end'].includes(selectedNodeId.value)) {
      deleteSelectedNode()
    } else if (selectedEdgeId.value) {
      deleteSelectedEdge()
    }
  }
  // 显示快捷键
  else if (e.key === '?') {
    showShortcuts.value = !showShortcuts.value
  }
}

// 获取节点定义
const fetchDefinitions = async () => {
  try {
    const res = await workflowApi.getNodeDefinitions()
    nodeDefinitions.value = res.map(def => ({
      ...def,
      icon: iconMap[def.type] || 'Setting',
      color: colorMap[def.type] || '#4A90A4'
    }))
    categorizeNodes(nodeDefinitions.value)
  } catch (err) {
    console.error('加载节点定义失败:', err)
    ElMessage.error('加载节点定义失败')
  }
}

// 加载已有工作流
const loadWorkflow = async () => {
  try {
    const res = await workflowApi.getWorkflow(workflowId.value)
    workflowName.value = res.name
    
    const config = typeof res.nodesConfig === 'string' 
      ? JSON.parse(res.nodesConfig) 
      : res.nodesConfig
    
    // 转换节点格式
    const loadedNodes = config.nodes.map(n => {
      const def = getNodeDefinition(n.type)
      return {
        id: n.id,
        type: 'custom',
        position: n.position || { x: 100, y: 100 },
        data: {
          label: n.name,
          nodeType: n.type,
          icon: def?.icon || iconMap[n.type] || 'Setting',
          color: def?.color || colorMap[n.type] || '#4A90A4',
          inputs: def?.inputs || [],
          outputs: def?.outputs || [],
          config: n.config || {},
          configured: Object.keys(n.config || {}).length > 0,
          description: def?.description || nodeDescriptions[n.type]
        }
      }
    })
    
    const loadedEdges = config.edges.map(e => ({
      id: e.id || `edge-${e.source}-${e.target}`,
      source: e.source,
      target: e.target,
      sourceHandle: e.sourcePort,
      targetHandle: e.targetPort,
      type: 'smoothstep',
      animated: true,
      data: {}
    }))
    
    // 使用 setNodes/setEdges 设置节点和边
    setNodes(loadedNodes)
    setEdges(loadedEdges)
    
    // 重置历史记录状态
    history.value = []
    historyIndex.value = -1
    isInitialized.value = false
    
    await nextTick()
    // 加载后自适应居中并设置100%缩放
    setTimeout(() => fitViewAndCenter(), 100)
  } catch (err) {
    console.error('加载工作流详情失败:', err)
    ElMessage.error('加载工作流详情失败')
  }
}

// 初始化新工作流
const initNewWorkflow = () => {
  const startDef = getNodeDefinition('start')
  const endDef = getNodeDefinition('end')
  
  const initialNodes = [
    {
      id: 'start',
      type: 'custom',
      position: { x: 150, y: 300 },
      data: {
        label: '开始',
        nodeType: 'start',
        icon: iconMap.start,
        color: colorMap.start,
        inputs: [],
        outputs: startDef?.outputs || [{ id: 'output', name: '输出' }],
        config: {},
        configured: true,
        description: nodeDescriptions.start
      }
    },
    {
      id: 'end',
      type: 'custom',
      position: { x: 950, y: 300 },
      data: {
        label: '结束',
        nodeType: 'end',
        icon: iconMap.end,
        color: colorMap.end,
        inputs: endDef?.inputs || [{ id: 'input', name: '输入' }],
        outputs: [],
        config: {},
        configured: true,
        description: nodeDescriptions.end
      }
    }
  ]
  
  // 使用 setNodes/setEdges 设置节点和边
  setNodes(initialNodes)
  setEdges([])
  
  // 初始化历史记录
  history.value = [{ nodes: JSON.parse(JSON.stringify(initialNodes)), edges: [] }]
  historyIndex.value = 0
  isInitialized.value = true
  
  // 初始化时先自适应居中，然后设置100%缩放
  nextTick(() => fitViewAndCenter())
}

// 节点拖拽
const onDragStart = (event, nodeDef) => {
  event.dataTransfer.setData('application/node-def', JSON.stringify(nodeDef))
  event.dataTransfer.effectAllowed = 'move'
}

const onDrop = (event) => {
  const defStr = event.dataTransfer.getData('application/node-def')
  if (!defStr) return
  
  const def = JSON.parse(defStr)
  
  // 计算画布位置
  const canvas = canvasRef.value
  if (!canvas) return
  
  const rect = canvas.getBoundingClientRect()
  const position = project({
    x: event.clientX - rect.left,
    y: event.clientY - rect.top
  })
  
  const newNode = {
    id: `node_${Date.now()}`,
    type: 'custom',
    position,
    data: {
      label: def.name,
      nodeType: def.type,
      icon: def.icon || iconMap[def.type] || 'Setting',
      color: def.color || colorMap[def.type] || '#4A90A4',
      inputs: def.inputs || [],
      outputs: def.outputs || [],
      config: {},
      configured: false,
      description: def.description || nodeDescriptions[def.type]
    }
  }
  
  addNodes([newNode])
  selectedNodeId.value = newNode.id
  nextTick(() => saveHistory())
}

// 连接处理
const onConnect = (params) => {
  // 检查是否已存在相同的连接
  const exists = edges.value.some(
    e => e.source === params.source && e.target === params.target
  )
  if (exists) return
  
  const newEdge = {
    ...params,
    id: `edge-${params.source}-${params.target}-${Date.now()}`,
    type: 'smoothstep',
    animated: true,
    data: {}
  }
  addEdges([newEdge])
  nextTick(() => saveHistory())
}

// 配置面板逻辑
const onNodeClick = ({ node }) => {
  selectedNodeId.value = node.id
  selectedEdgeId.value = null
}

const onEdgeClick = ({ edge }) => {
  // 点击边时只选中，不打开配置面板
  selectedEdgeId.value = edge.id
  selectedNodeId.value = null
  // 不需要边的配置面板
}

const onPaneClick = () => {
  closePanel()
}

// 处理节点变化 - 参照参考项目实现
const onNodesChange = (changes) => {
  applyNodeChanges(changes)
  // 保存到历史记录
  saveHistory()
  hasUnsavedChanges.value = true
}

// 处理边变化 - 参照参考项目实现
const onEdgesChange = (changes) => {
  applyEdgeChanges(changes)
  // 保存到历史记录
  saveHistory()
  hasUnsavedChanges.value = true
}

const onViewportChange = (viewport) => {
  viewportZoom.value = viewport.zoom
}

const closePanel = () => {
  selectedNodeId.value = null
  selectedEdgeId.value = null
}

const onNodeUpdate = (data) => {
  if (selectedNode.value) {
    selectedNode.value.data.label = data.name
    selectedNode.value.data.config = data.config
    selectedNode.value.data.configured = true
    saveHistory()
  }
}

const deleteSelectedNode = () => {
  if (!selectedNodeId.value) return
  if (selectedNodeId.value === 'start' || selectedNodeId.value === 'end') {
    ElMessage.warning('开始和结束节点不能删除')
    return
  }
  
  const newNodes = nodes.value.filter(n => n.id !== selectedNodeId.value)
  const newEdges = edges.value.filter(
    e => e.source !== selectedNodeId.value && e.target !== selectedNodeId.value
  )
  setNodes(newNodes)
  setEdges(newEdges)
  closePanel()
  saveHistory()
}

const deleteSelectedEdge = () => {
  if (!selectedEdgeId.value) return
  const newEdges = edges.value.filter(e => e.id !== selectedEdgeId.value)
  setEdges(newEdges)
  closePanel()
  saveHistory()
}

// 保存当前状态到历史记录 - 参照参考项目实现
const saveHistory = () => {
  if (isUndoRedo.value) return // 如果正在执行撤销/重做，不保存历史
  
  // 如果历史记录还未初始化，先初始化
  if (!isInitialized.value) {
    const currentNodes = JSON.parse(JSON.stringify(nodes.value || []))
    const currentEdges = JSON.parse(JSON.stringify(edges.value || []))
    history.value = [{ nodes: currentNodes, edges: currentEdges }]
    historyIndex.value = 0
    isInitialized.value = true
    return
  }
  
  // 清除防抖定时器
  if (saveHistoryTimer) {
    clearTimeout(saveHistoryTimer)
  }
  
  // 使用防抖，延迟保存历史记录
  saveHistoryTimer = setTimeout(() => {
    const currentNodes = JSON.parse(JSON.stringify(nodes.value || []))
    const currentEdges = JSON.parse(JSON.stringify(edges.value || []))
    
    // 如果当前不在历史记录的末尾，删除后面的记录
    if (historyIndex.value < history.value.length - 1) {
      history.value = history.value.slice(0, historyIndex.value + 1)
    }
    
    // 添加新的历史记录
    history.value.push({ nodes: currentNodes, edges: currentEdges })
    
    // 限制历史记录数量
    if (history.value.length > maxHistorySize + 1) {
      const firstState = history.value[0]
      const remainingStates = history.value.slice(2)
      history.value = [firstState, ...remainingStates]
      historyIndex.value = history.value.length - 1
    } else {
      historyIndex.value = history.value.length - 1
    }
    
    hasUnsavedChanges.value = true
  }, 300)
}

// 撤销操作 - 参照参考项目实现
const undo = () => {
  if (!canUndo.value) return
  
  isUndoRedo.value = true
  historyIndex.value--
  
  const state = history.value[historyIndex.value]
  if (state) {
    setNodes(JSON.parse(JSON.stringify(state.nodes)))
    setEdges(JSON.parse(JSON.stringify(state.edges)))
  }
  
  nextTick(() => {
    isUndoRedo.value = false
  })
}

// 重做操作 - 参照参考项目实现
const redo = () => {
  if (!canRedo.value) return
  
  isUndoRedo.value = true
  historyIndex.value++
  
  const state = history.value[historyIndex.value]
  if (state) {
    setNodes(JSON.parse(JSON.stringify(state.nodes)))
    setEdges(JSON.parse(JSON.stringify(state.edges)))
  }
  
  nextTick(() => {
    isUndoRedo.value = false
  })
}

const markChanged = () => {
  hasUnsavedChanges.value = true
}

// 辅助函数
const getNodeDefinition = (type) => nodeDefinitions.value.find(d => d.type === type)

const autoLayout = () => {
  const currentNodes = nodes.value || []
  const currentEdges = edges.value || []
  
  if (currentNodes.length === 0) {
    ElMessage.info('画布为空')
    return
  }
  
  // 查找开始和结束节点
  const startNodes = currentNodes.filter(n => n.id === 'start' || n.data?.nodeType === 'start')
  const endNodes = currentNodes.filter(n => n.id === 'end' || n.data?.nodeType === 'end')
  
  if (startNodes.length === 0) {
    ElMessage.warning('请先添加开始节点')
    return
  }
  
  // 构建边的映射
  const edgeMap = new Map() // source -> targets
  const reverseEdgeMap = new Map() // target -> sources
  
  currentEdges.forEach(e => {
    if (!edgeMap.has(e.source)) edgeMap.set(e.source, [])
    edgeMap.get(e.source).push(e.target)
    
    if (!reverseEdgeMap.has(e.target)) reverseEdgeMap.set(e.target, [])
    reverseEdgeMap.get(e.target).push(e.source)
  })
  
  // 计算每个节点的层级（从开始节点开始）
  const nodeLayer = new Map()
  const visited = new Set()
  
  // 第一层：开始节点（固定在最左边）
  startNodes.forEach(n => {
    nodeLayer.set(n.id, 0)
    visited.add(n.id)
  })
  
  // BFS 计算其他节点的层级
  let queue = startNodes.map(n => n.id)
  
  while (queue.length > 0) {
    const nodeId = queue.shift()
    const currentLayer = nodeLayer.get(nodeId)
    const neighbors = edgeMap.get(nodeId) || []
    
    neighbors.forEach((neighborId) => {
      const newLayer = currentLayer + 1
      // 更新层级（取最大值，确保节点在所有前驱节点之后）
      if (!nodeLayer.has(neighborId) || nodeLayer.get(neighborId) < newLayer) {
        nodeLayer.set(neighborId, newLayer)
      }
      
      if (!visited.has(neighborId)) {
        visited.add(neighborId)
        queue.push(neighborId)
      }
    })
  }
  
  // 处理结束节点：如果有前驱节点，放在前驱节点的下一层
  if (endNodes.length > 0) {
    const maxLayer = Math.max(...Array.from(nodeLayer.values()), 0)
    endNodes.forEach(endNode => {
      const predecessors = reverseEdgeMap.get(endNode.id) || []
      
      if (predecessors.length > 0) {
        let maxPredecessorLayer = -1
        predecessors.forEach((predId) => {
          const predLayer = nodeLayer.get(predId)
          if (predLayer !== undefined && predLayer > maxPredecessorLayer) {
            maxPredecessorLayer = predLayer
          }
        })
        
        if (maxPredecessorLayer >= 0) {
          nodeLayer.set(endNode.id, maxPredecessorLayer + 1)
        } else {
          nodeLayer.set(endNode.id, maxLayer)
        }
      } else if (!nodeLayer.has(endNode.id)) {
        nodeLayer.set(endNode.id, maxLayer)
      }
    })
  }
  
  // 处理孤立节点（没有连接到开始节点的节点）
  const isolatedNodes = currentNodes.filter(node => !nodeLayer.has(node.id))
  if (isolatedNodes.length > 0) {
    const maxLayer = nodeLayer.size > 0 ? Math.max(...Array.from(nodeLayer.values()), 0) : -1
    const isolatedLayer = maxLayer + 1
    
    isolatedNodes.forEach((node) => {
      nodeLayer.set(node.id, isolatedLayer)
    })
  }
  
  // 按层级分组节点
  const layers = []
  nodeLayer.forEach((layer, nodeId) => {
    if (!layers[layer]) layers[layer] = []
    layers[layer].push(nodeId)
  })
  
  // 布局参数
  const horizontalGap = 350  // 水平间距
  const verticalGap = 180    // 垂直间距
  const startX = 150
  const startY = 200
  
  // 特殊处理：如果只有开始和结束节点，让它们水平排列
  const onlyStartAndEnd = currentNodes.length === 2 && 
    startNodes.length === 1 && 
    endNodes.length === 1 &&
    currentNodes.every(n => n.data?.nodeType === 'start' || n.data?.nodeType === 'end')
  
  // 创建新的节点数组（触发响应式更新）
  const updatedNodes = currentNodes.map(node => {
    if (nodeLayer.has(node.id)) {
      const layerIndex = nodeLayer.get(node.id)
      const layerNodes = layers[layerIndex] || []
      const nodeIndex = layerNodes.indexOf(node.id)
      
      let newPosition
      if (onlyStartAndEnd) {
        // 只有开始和结束节点时，水平排列
        let actualX = startX
        if (node.data?.nodeType === 'end' || node.id === 'end') {
          actualX = startX + horizontalGap
        }
        newPosition = { x: actualX, y: startY }
      } else {
        // 计算这一层的总高度
        const totalHeight = (layerNodes.length - 1) * verticalGap
        // 计算起始Y坐标使整层垂直居中
        const layerStartY = startY - totalHeight / 2
        
        newPosition = {
          x: startX + layerIndex * horizontalGap,
          y: layerStartY + nodeIndex * verticalGap
        }
      }
      
      return { ...node, position: newPosition }
    }
    return node
  })
  
  // 使用 setNodes 更新节点
  setNodes(updatedNodes)
  
  hasUnsavedChanges.value = true
  
  // 自动排列是明确的用户操作，立即保存历史记录（不使用防抖）
  // 清除可能存在的防抖定时器
  if (saveHistoryTimer) {
    clearTimeout(saveHistoryTimer)
    saveHistoryTimer = null
  }
  
  // 立即保存历史记录
  if (isUndoRedo.value) {
    isUndoRedo.value = false
  }
  
  if (!isInitialized.value) {
    const currentNodes = JSON.parse(JSON.stringify(nodes.value || []))
    const currentEdges = JSON.parse(JSON.stringify(edges.value || []))
    history.value = [{ nodes: currentNodes, edges: currentEdges }]
    historyIndex.value = 0
    isInitialized.value = true
  } else {
    const currentNodes = JSON.parse(JSON.stringify(nodes.value || []))
    const currentEdges = JSON.parse(JSON.stringify(edges.value || []))
    
    if (historyIndex.value < history.value.length - 1) {
      history.value = history.value.slice(0, historyIndex.value + 1)
    }
    
    history.value.push({ nodes: currentNodes, edges: currentEdges })
    
    if (history.value.length > maxHistorySize + 1) {
      const firstState = history.value[0]
      const remainingStates = history.value.slice(2)
      history.value = [firstState, ...remainingStates]
      historyIndex.value = history.value.length - 1
    } else {
      historyIndex.value = history.value.length - 1
    }
  }
  
  // 等待 Vue Flow 更新后再调整视口
  nextTick(() => {
    setTimeout(() => {
      fitView()
      ElMessage.success('自动排列完成')
    }, 100) // 给 Vue Flow 一些时间更新节点位置
  })
}

// 自适应画布并居中，然后设置缩放比例
const fitView = (targetZoom = null) => {
  // 使用 padding 确保节点周围有足够空间
  flowFitView({ padding: 0.2, duration: 300 }).then(() => {
    // 如果指定了目标缩放比例，则设置为指定值
    if (targetZoom !== null) {
      setTimeout(() => {
        const viewport = getViewport()
        setViewport({ ...viewport, zoom: targetZoom }, { duration: 200 })
        viewportZoom.value = targetZoom
      }, 350)
    } else {
      // 更新当前缩放值
      setTimeout(() => {
        const viewport = getViewport()
        viewportZoom.value = viewport.zoom
      }, 350)
    }
  })
}

// 居中显示 - 将整个工作流居中到画布正中心
const fitViewAndCenter = () => {
  const currentNodes = nodes.value || []
  if (currentNodes.length === 0) {
    ElMessage.info('画布为空')
    return
  }
  
  // 计算所有节点的边界框
  let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity
  currentNodes.forEach(node => {
    const x = node.position?.x || 0
    const y = node.position?.y || 0
    // 假设节点大小约为 220x100
    minX = Math.min(minX, x)
    minY = Math.min(minY, y)
    maxX = Math.max(maxX, x + 220)
    maxY = Math.max(maxY, y + 100)
  })
  
  // 计算工作流的中心点
  const workflowCenterX = (minX + maxX) / 2
  const workflowCenterY = (minY + maxY) / 2
  
  // 获取画布尺寸
  const canvas = canvasRef.value
  if (!canvas) {
    fitView(1)
    return
  }
  
  const canvasRect = canvas.getBoundingClientRect()
  const canvasCenterX = canvasRect.width / 2
  const canvasCenterY = canvasRect.height / 2
  
  // 计算需要的视口偏移，使工作流中心与画布中心对齐
  const zoom = 1 // 使用 100% 缩放
  const x = canvasCenterX - workflowCenterX * zoom
  const y = canvasCenterY - workflowCenterY * zoom
  
  setViewport({ x, y, zoom }, { duration: 300 })
  viewportZoom.value = zoom
  
  ElMessage.success('已居中显示')
}

// 缩放控制
const zoomIn = () => {
  const viewport = getViewport()
  const newZoom = Math.min(viewport.zoom + 0.25, 2)
  setViewport({ ...viewport, zoom: newZoom }, { duration: 200 })
  viewportZoom.value = newZoom
}

const zoomOut = () => {
  const viewport = getViewport()
  const newZoom = Math.max(viewport.zoom - 0.25, 0.2)
  setViewport({ ...viewport, zoom: newZoom }, { duration: 200 })
  viewportZoom.value = newZoom
}

const setZoom = (zoom) => {
  const viewport = getViewport()
  setViewport({ ...viewport, zoom }, { duration: 200 })
  viewportZoom.value = zoom
}

// 通过下拉菜单添加节点
const handleAddNode = (nodeDef) => {
  // 在画布中心位置添加节点
  const viewport = getViewport()
  const canvasRect = canvasRef.value?.getBoundingClientRect()
  
  // 计算画布中心点的实际坐标
  const centerX = canvasRect ? (canvasRect.width / 2 - viewport.x) / viewport.zoom : 400
  const centerY = canvasRect ? (canvasRect.height / 2 - viewport.y) / viewport.zoom : 300
  
  const newNode = {
    id: `node_${Date.now()}`,
    type: 'custom',
    position: { x: centerX - 110, y: centerY - 50 },
    data: {
      label: nodeDef.name,
      nodeType: nodeDef.type,
      icon: nodeDef.icon || iconMap[nodeDef.type] || 'Setting',
      color: nodeDef.color || colorMap[nodeDef.type] || '#4A90A4',
      inputs: nodeDef.inputs || [],
      outputs: nodeDef.outputs || [],
      config: {},
      configured: false,
      description: nodeDef.description || nodeDescriptions[nodeDef.type]
    }
  }
  
  addNodes([newNode])
  selectedNodeId.value = newNode.id
  nextTick(() => saveHistory())
}

const goBack = () => {
  if (hasUnsavedChanges.value) {
    ElMessageBox.confirm('您有未保存的更改，确定离开吗？', '提示', {
      type: 'warning',
      confirmButtonText: '离开',
      cancelButtonText: '取消'
    })
      .then(() => router.push('/admin/workflows'))
      .catch(() => {})
  } else {
    router.push('/admin/workflows')
  }
}

// API 调用
const validateWorkflow = async () => {
  validating.value = true
  try {
    const config = buildNodesConfig()
    const res = await workflowApi.validateWorkflow(config)
    if (res.valid) {
      ElMessage.success('验证通过！工作流配置正确。')
    } else {
      const errors = res.errors || []
      const firstError = errors[0]?.message || '配置有误'
      ElMessage.error(`验证失败：${firstError}`)
    }
  } catch (err) {
    console.error('验证失败:', err)
    ElMessage.error('验证过程出错')
  } finally {
    validating.value = false
  }
}

const saveWorkflow = async () => {
  if (!workflowName.value.trim()) {
    ElMessage.warning('请输入工作流名称')
    return
  }
  
  saving.value = true
  try {
    const nodesConfig = buildNodesConfig()
    const data = {
      name: workflowName.value,
      description: workflowName.value,
      nodesConfig,
      isActive: 1
    }
    
    if (workflowId.value) {
      await workflowApi.updateWorkflow(workflowId.value, data)
      ElMessage.success('保存成功')
    } else {
      const res = await workflowApi.createWorkflow(data)
      router.replace(`/admin/workflows/editor/${res.id}`)
      ElMessage.success('创建成功')
    }
    hasUnsavedChanges.value = false
  } catch (err) {
    console.error('保存失败:', err)
    ElMessage.error('保存失败: ' + (err.message || '系统错误'))
  } finally {
    saving.value = false
  }
}

const buildNodesConfig = () => {
  return {
    nodes: nodes.value.map(n => ({
      id: n.id,
      type: n.data.nodeType,
      name: n.data.label,
      config: n.data.config,
      position: n.position
    })),
    edges: edges.value.map(e => ({
      id: e.id,
      source: e.source,
      sourcePort: e.sourceHandle,
      target: e.target,
      targetPort: e.targetHandle
    }))
  }
}
</script>

<style scoped>
/* 编辑器主容器 - 嵌入 Layout 中 */
.workflow-editor {
  width: 100%;
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  background: var(--app-bg-gradient, linear-gradient(135deg, #FAF7F2 0%, #F5F1EB 100%));
  font-family: 'Nunito', 'Inter', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow: hidden;
  position: relative;
}

/* 顶部工具栏 */
.editor-toolbar {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--el-border-color-light, #F0F0F0);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  z-index: 100;
  flex-shrink: 0;
  margin-top: 18px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.toolbar-main {
  height: 56px;
  border-bottom: 1px solid var(--el-border-color-lighter, #F8F8F8);
}

.toolbar-nodes {
  height: 52px;
  background: linear-gradient(135deg, #F8F6F3 0%, #F5F1EB 100%);
  justify-content: flex-start;
}

.toolbar-left,
.toolbar-center,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar-left {
  flex: 1;
  min-width: 0;
}

.toolbar-right {
  flex-shrink: 0;
  justify-content: flex-end;
  margin-left: 24px;
  padding-left: 16px;
  border-left: 1px solid var(--el-border-color-lighter, #E8E8E8);
}

/* 节点工具栏 */
.node-toolbar {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 16px;
}

.toolbar-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #7A7A7A;
  font-weight: 600;
  white-space: nowrap;
  padding-right: 12px;
  border-right: 1px solid var(--el-border-color, #E8E8E8);
}

.node-buttons {
  flex: 1;
  display: flex;
  gap: 8px;
  flex-wrap: nowrap;
  align-items: center;
  overflow-x: auto;
  padding: 4px 0;
}

.node-buttons::-webkit-scrollbar {
  height: 4px;
}

.node-buttons::-webkit-scrollbar-thumb {
  background: #D4C4B0;
  border-radius: 2px;
}

.node-add-btn {
  padding: 0;
  border: none;
  background: transparent;
  cursor: grab;
  flex-shrink: 0;
}

.node-add-btn:active {
  cursor: grabbing;
}

.node-add-btn .btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 14px;
  background: rgba(255, 255, 255, 0.95);
  border: 1.5px solid var(--el-border-color-light, #F0F0F0);
  border-left: 4px solid var(--btn-color, #4A90A4);
  border-radius: 10px;
  transition: all 0.2s ease;
  user-select: none;
}

.node-add-btn:hover .btn-content {
  border-color: var(--btn-color, #4A90A4);
  box-shadow: 0 3px 12px rgba(74, 144, 164, 0.15);
  transform: translateY(-2px);
  background: #ffffff;
}

.node-add-btn .btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 7px;
  flex-shrink: 0;
}

.node-add-btn .btn-icon .el-icon {
  font-size: 15px;
}

.node-add-btn .btn-label {
  font-size: 13px;
  font-weight: 600;
  color: #3A3A3A;
  white-space: nowrap;
}

/* 返回按钮 */
.back-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 5px 10px;
  background: transparent;
  border: none;
  color: var(--el-text-color-regular, #5A5A5A);
  font-size: 12px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.back-btn:hover {
  background: var(--el-color-primary-light-9, #EFF4F8);
  color: var(--el-color-primary, #4A90A4);
}

/* 工作流名称 */
.workflow-name-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 4px;
}

.workflow-name-input {
  border: none;
  background: transparent;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary, #3A3A3A);
  width: 160px;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.workflow-name-input:hover {
  background: var(--warm-beige, #F5F1EB);
}

.workflow-name-input:focus {
  outline: none;
  background: var(--gentle-sand, #E8E2D5);
}

.unsaved-indicator {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 10px;
  color: var(--el-color-warning, #D4A574);
  background: rgba(212, 165, 116, 0.15);
  padding: 2px 8px;
  border-radius: 10px;
}

.unsaved-indicator .dot {
  width: 6px;
  height: 6px;
  background: var(--el-color-warning, #D4A574);
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* 工具栏操作按钮 */
.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 4px;
  background: var(--warm-beige, #F5F1EB);
  border-radius: 8px;
}

.tool-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: var(--el-text-color-secondary, #7A7A7A);
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
}

.tool-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.8);
  color: var(--el-color-primary, #4A90A4);
}

.tool-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.divider {
  width: 1px;
  height: 16px;
  background: var(--cozy-taupe, #D4C4B0);
  margin: 0 2px;
}

.zoom-display {
  font-size: 12px;
  color: var(--el-text-color-secondary, #7A7A7A);
  min-width: 40px;
  text-align: center;
  font-weight: 500;
}

/* 操作按钮 */
.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.action-btn.secondary {
  background: var(--warm-beige, #F5F1EB);
  color: var(--el-text-color-regular, #5A5A5A);
  border: 1px solid var(--gentle-sand, #E8E2D5);
}

.action-btn.secondary:hover:not(:disabled) {
  background: var(--gentle-sand, #E8E2D5);
  border-color: var(--cozy-taupe, #D4C4B0);
}

.action-btn.success {
  background: linear-gradient(135deg, var(--el-color-success, #7BA05B) 0%, #6B8F4B 100%);
  color: white;
}

.action-btn.success:hover {
  box-shadow: 0 4px 16px rgba(123, 160, 91, 0.35);
  transform: translateY(-1px);
}

.action-btn.primary {
  background: var(--app-primary-gradient, linear-gradient(135deg, #4A90A4 0%, #6BA4B8 100%));
  color: white;
}

.action-btn.primary:hover:not(:disabled) {
  box-shadow: 0 4px 16px rgba(74, 144, 164, 0.35);
  transform: translateY(-1px);
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none !important;
}

.is-loading {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 主编辑区域 */
.editor-main {
  flex: 1;
  display: flex;
  overflow: hidden;
  min-height: 0; /* 重要：允许flex子项收缩 */
  position: relative;
}

/* 左侧节点工具箱面板 */
.node-toolbox-panel {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 50;
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(12px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid var(--el-border-color-light, #F0F0F0);
  overflow: hidden;
  transition: all 0.3s ease;
}

.node-toolbox-panel.is-expanded {
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12), 0 4px 16px rgba(0, 0, 0, 0.06);
}

.toolbox-toggle-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  background: transparent;
  border: none;
  color: var(--el-color-primary, #4A90A4);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  width: 100%;
  text-align: left;
}

.toolbox-toggle-btn:hover {
  background: var(--el-color-primary-light-9, #EFF4F8);
}

.toolbox-toggle-btn .el-icon {
  font-size: 16px;
}

.toggle-arrow {
  margin-left: auto;
  transition: transform 0.3s ease;
  font-size: 12px !important;
  color: var(--el-text-color-secondary, #7A7A7A);
}

.toggle-arrow.is-expanded {
  transform: rotate(180deg);
}

.toolbox-content {
  padding: 8px 12px 16px;
  max-height: 60vh;
  overflow-y: auto;
  border-top: 1px solid var(--el-border-color-lighter, #F8F8F8);
}

.toolbox-content::-webkit-scrollbar {
  width: 5px;
}

.toolbox-content::-webkit-scrollbar-thumb {
  background: #D4C4B0;
  border-radius: 3px;
}

.toolbox-category {
  margin-bottom: 12px;
}

.toolbox-category:last-child {
  margin-bottom: 0;
}

.category-header {
  font-size: 11px;
  font-weight: 700;
  color: #7A7A7A;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 8px 8px 6px;
}

.category-nodes {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.draggable-node {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid transparent;
  background: var(--warm-beige, #F5F1EB);
  cursor: grab;
  transition: all 0.2s ease;
  user-select: none;
}

.draggable-node:hover {
  border-color: var(--node-color, #4A90A4);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transform: translateX(2px);
}

.draggable-node:active {
  cursor: grabbing;
  transform: scale(0.98);
}

.draggable-node .node-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  background: var(--node-color, #4A90A4)20;
  color: var(--node-color, #4A90A4);
  border-radius: 7px;
  flex-shrink: 0;
}

.draggable-node .node-icon .el-icon {
  font-size: 15px;
}

.draggable-node .node-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.draggable-node .node-name {
  font-size: 13px;
  font-weight: 600;
  color: #3A3A3A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.draggable-node .node-desc {
  font-size: 11px;
  color: #7A7A7A;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 节点工具箱展开动画 */
.toolbox-slide-enter-active,
.toolbox-slide-leave-active {
  transition: all 0.3s ease;
}

.toolbox-slide-enter-from,
.toolbox-slide-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
}

/* 画布区域 */
.canvas-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, rgba(250, 247, 242, 0.5) 0%, rgba(245, 241, 235, 0.5) 100%);
  min-height: 400px;
}

/* Vue Flow 样式覆盖 */
.canvas-container :deep(.vue-flow) {
  background: transparent;
}

.canvas-container :deep(.vue-flow__background) {
  background: transparent;
}

.canvas-container :deep(.vue-flow__minimap) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border: 1px solid var(--el-border-color-light, #F0F0F0);
  border-radius: 12px;
  box-shadow: var(--el-box-shadow-light);
  overflow: hidden;
}

/* 空画布提示 */
.empty-canvas-hint {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  color: var(--el-text-color-secondary, #7A7A7A);
  pointer-events: auto;
  z-index: 10;
}

.empty-canvas-hint .el-icon {
  color: var(--cozy-taupe, #D4C4B0);
  margin-bottom: 16px;
}

.empty-canvas-hint h3 {
  font-size: 20px;
  font-weight: 700;
  color: var(--el-text-color-primary, #3A3A3A);
  margin: 0 0 10px 0;
}

.empty-canvas-hint p {
  font-size: 14px;
  margin: 0 0 24px 0;
}

.quick-start-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--app-primary-gradient, linear-gradient(135deg, #4A90A4 0%, #6BA4B8 100%));
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-start-btn:hover {
  box-shadow: 0 6px 20px rgba(74, 144, 164, 0.35);
  transform: translateY(-2px);
}

/* 右侧配置面板 */
.config-panel {
  width: 380px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-left: 1px solid var(--el-border-color-light, #F0F0F0);
  display: flex;
  flex-direction: column;
  z-index: 10;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 22px;
  border-bottom: 1px solid var(--el-border-color-lighter, #F8F8F8);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  font-weight: 700;
  color: var(--el-text-color-primary, #3A3A3A);
}

.title-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
}

.title-icon .el-icon {
  font-size: 18px;
}

.close-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  color: var(--el-text-color-placeholder, #B5B5B5);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.close-btn:hover {
  background: var(--warm-beige, #F5F1EB);
  color: var(--el-text-color-secondary, #7A7A7A);
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 22px;
}

/* 面板滑入动画 */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s ease;
}

.slide-right-enter-from,
.slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* 快捷键提示 */
.shortcuts-hint {
  position: fixed;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(58, 58, 58, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 14px;
  padding: 16px 28px;
  display: flex;
  gap: 28px;
  z-index: 1000;
}

.shortcut-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.85);
}

.shortcut-item kbd {
  display: inline-block;
  padding: 3px 8px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 5px;
  font-family: inherit;
  font-size: 11px;
}


/* 缩放控制 */
.zoom-control {
  display: flex;
  align-items: center;
  gap: 2px;
  background: transparent;
  border-radius: 6px;
  padding: 0;
}

.zoom-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: var(--el-text-color-secondary, #7A7A7A);
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 12px;
}

.zoom-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.8);
  color: var(--el-color-primary, #4A90A4);
}

.zoom-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.zoom-dropdown {
  cursor: pointer;
}

.zoom-display {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  padding: 2px 6px;
  font-size: 11px;
  font-weight: 600;
  color: var(--el-text-color-primary, #3A3A3A);
  background: rgba(255, 255, 255, 0.8);
  border-radius: 3px;
  cursor: pointer;
  transition: all 0.2s;
}

.zoom-display:hover {
  background: #ffffff;
  color: var(--el-color-primary, #4A90A4);
}
</style>
