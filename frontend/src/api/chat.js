import { http } from '@/utils/http'

// 聊天会话相关 API
export const chatApi = {
  // 获取会话列表
  getSessions(params = {}) {
    return http.get('/v1/sessions', { params })
  },

  // 获取会话详情
  getSession(id) {
    return http.get(`/v1/sessions/${id}`)
  },

  // 创建会话
  createSession(data) {
    return http.post('/v1/sessions', data)
  },

  // 获取会话消息列表
  getMessages(sessionId, params = {}) {
    return http.get(`/v1/sessions/${sessionId}/messages`, { params })
  },

  // 删除会话
  deleteSession(id) {
    return http.delete(`/v1/sessions/${id}`)
  },

  // 更新会话标题
  updateSessionTitle(id, title) {
    return http.put(`/v1/sessions/${id}/title`, { title })
  },

  // 置顶/取消置顶会话
  pinSession(id, isPinned) {
    return http.put(`/v1/sessions/${id}/pin`, { isPinned })
  },

  // 发送消息
  sendMessage(sessionId, data) {
    return http.post(`/v1/sessions/${sessionId}/messages`, data)
  }
}
