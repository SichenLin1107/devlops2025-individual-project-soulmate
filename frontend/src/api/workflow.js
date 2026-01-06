import { http } from '@/utils/http'

// 工作流 API
export const workflowApi = {
  // 获取工作流列表
  getWorkflows(params = {}) {
    return http.get('/v1/workflows', { params })
  },

  // 获取工作流详情
  getWorkflow(id) {
    return http.get(`/v1/workflows/${id}`)
  },

  // 创建工作流
  createWorkflow(data) {
    return http.post('/v1/workflows', data)
  },

  // 更新工作流
  updateWorkflow(id, data) {
    return http.put(`/v1/workflows/${id}`, data)
  },

  // 删除工作流
  deleteWorkflow(id) {
    return http.delete(`/v1/workflows/${id}`)
  },

  // 更新工作流状态
  updateStatus(id, isActive, disableRelatedAgents = false) {
    return http.put(`/v1/workflows/${id}/status`, { isActive, disableRelatedAgents })
  },

  // 查询关联的智能体数量
  countRelatedAgents(id) {
    return http.get(`/v1/workflows/${id}/related-agents-count`)
  },

  // 执行工作流（调试）
  executeWorkflow(id, data) {
    return http.post(`/v1/workflows/${id}/execute`, data)
  },

  // 验证工作流配置
  validateWorkflow(nodesConfig) {
    return http.post('/v1/workflows/validate', nodesConfig)
  },

  // 获取所有节点定义
  getNodeDefinitions() {
    return http.get('/v1/workflows/node-definitions')
  }
}

