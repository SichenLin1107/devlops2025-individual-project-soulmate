import { http } from '@/utils/http'

// 敏感词管理 API
export const sensitiveWordApi = {
  // 获取敏感词列表
  getSensitiveWords(params = {}) {
    return http.get('/v1/sensitive-words', { params })
  },

  // 创建敏感词
  createSensitiveWord(data) {
    return http.post('/v1/sensitive-words', data)
  },

  // 更新敏感词
  updateSensitiveWord(id, data) {
    return http.put(`/v1/sensitive-words/${id}`, data)
  },

  // 删除敏感词
  deleteSensitiveWord(id) {
    return http.delete(`/v1/sensitive-words/${id}`)
  },

  // 批量更新敏感词状态
  batchUpdateStatus(ids, isActive) {
    return http.put('/v1/sensitive-words/batch/status', { ids, isActive })
  },

  // 更新敏感词状态
  updateSensitiveWordStatus(id, isActive) {
    return http.put(`/v1/sensitive-words/${id}/status`, { isActive })
  },

  // 批量删除敏感词
  batchDelete(ids) {
    return http.delete('/v1/sensitive-words/batch', { data: { ids } })
  }
}
