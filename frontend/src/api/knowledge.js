import { http } from '@/utils/http'

// 知识库管理 API
export const knowledgeApi = {
  // 获取知识库列表
  getKnowledgeBases(params = {}) {
    return http.get('/v1/knowledge-bases', { params })
  },

  // 获取知识库详情
  getKnowledgeBase(id) {
    return http.get(`/v1/knowledge-bases/${id}`)
  },

  // 创建知识库
  createKnowledgeBase(data) {
    return http.post('/v1/knowledge-bases', data)
  },

  // 更新知识库
  updateKnowledgeBase(id, data) {
    return http.put(`/v1/knowledge-bases/${id}`, data)
  },

  // 删除知识库
  deleteKnowledgeBase(id) {
    return http.delete(`/v1/knowledge-bases/${id}`)
  },

  // 上传文档
  uploadDocument(kbId, file) {
    const formData = new FormData()
    formData.append('file', file)
    return http.post(`/v1/knowledge-bases/${kbId}/documents`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 获取文档列表
  getDocuments(kbId, params = {}) {
    return http.get(`/v1/knowledge-bases/${kbId}/documents`, { params })
  },

  // 删除文档
  deleteDocument(kbId, docId) {
    return http.delete(`/v1/knowledge-bases/${kbId}/documents/${docId}`)
  },

  // 查询文档状态
  getDocumentStatus(kbId, docId) {
    return http.get(`/v1/knowledge-bases/${kbId}/documents/${docId}/status`)
  },

  // 重试文档处理
  retryDocument(kbId, docId) {
    return http.post(`/v1/knowledge-bases/${kbId}/documents/${docId}/retry`)
  },

  // 检索测试
  testRetrieval(kbId, data) {
    const requestData = {
      query: data.query,
      topK: data.topK || 3,
      scoreThreshold: data.scoreThreshold || 0.5
    }
    return http.post(`/v1/knowledge-bases/${kbId}/test`, requestData)
  },

  // 更新知识库状态
  updateStatus(id, isActive, disableRelatedAgents = false) {
    return http.put(`/v1/knowledge-bases/${id}/status`, { isActive, disableRelatedAgents })
  },

  // 查询关联的智能体数量
  countRelatedAgents(id) {
    return http.get(`/v1/knowledge-bases/${id}/related-agents-count`)
  }
}

