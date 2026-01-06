import { http } from '@/utils/http'

// 智能体管理 API
export const agentApi = {
  // 获取智能体列表
  getAgents(params = {}) {
    return http.get('/v1/agents', { params })
  },

  // 获取智能体详情
  getAgent(id) {
    return http.get(`/v1/agents/${id}`)
  },

  // 创建智能体
  createAgent(data) {
    return http.post('/v1/agents', data)
  },

  // 更新智能体
  updateAgent(id, data) {
    return http.put(`/v1/agents/${id}`, data)
  },

  // 删除智能体
  deleteAgent(id) {
    return http.delete(`/v1/agents/${id}`)
  },

  // 更新智能体状态
  updateStatus(id, status) {
    return http.put(`/v1/agents/${id}/status`, { status })
  },

  // 绑定知识库
  bindKnowledgeBases(id, kbIds) {
    return http.post(`/v1/agents/${id}/knowledge`, { kbIds })
  },

  // 解绑知识库
  unbindKnowledgeBase(id, kbId) {
    return http.delete(`/v1/agents/${id}/knowledge/${kbId}`)
  },

  // 获取智能体绑定的知识库列表
  getAgentKnowledgeBases(id) {
    return http.get(`/v1/agents/${id}/knowledge`)
  }
}

