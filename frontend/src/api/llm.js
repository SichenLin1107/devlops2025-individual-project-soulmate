import { http } from '@/utils/http'

// LLM 模型管理 API
export const llmModelApi = {
  // 获取模型列表
  getModels(params = {}) {
    return http.get('/v1/llm/models', { params })
  },

  // 创建模型
  createModel(data) {
    return http.post('/v1/llm/models', data)
  },

  // 更新模型
  updateModel(id, data) {
    return http.put(`/v1/llm/models/${id}`, data)
  },

  // 删除模型
  deleteModel(id) {
    return http.delete(`/v1/llm/models/${id}`)
  },

  // 测试对话
  testChat(data) {
    return http.post('/v1/llm/models/test-chat', data)
  },

  // 查询关联的智能体数量
  countRelatedAgents(id) {
    return http.get(`/v1/llm/models/${id}/related-agents-count`)
  }
}

// LLM 提供商管理 API
export const llmProviderApi = {
  // 获取提供商列表
  getProviders() {
    return http.get('/v1/llm/providers')
  }
}

