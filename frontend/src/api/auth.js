import { http } from '@/utils/http'

// 认证相关 API
export const authApi = {
  // 用户登录
  login(username, password) {
    return http.post('/v1/auth/login', { username, password })
  },

  // 用户注册
  register(username, password, nickname) {
    return http.post('/v1/auth/register', { username, password, nickname })
  },

  // 获取当前用户信息
  getCurrentUser() {
    return http.get('/v1/auth/me')
  },

  // 登出
  logout() {
    return http.post('/v1/auth/logout')
  }
}
