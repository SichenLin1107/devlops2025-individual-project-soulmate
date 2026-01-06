import { http } from '@/utils/http'

// 用户管理相关 API（个人中心）
export const userApi = {
  // 获取个人信息
  getProfile() {
    return http.get('/v1/profile')
  },

  // 更新个人信息
  updateProfile(data) {
    return http.put('/v1/profile', data)
  },

  // 修改密码
  changePassword(oldPassword, newPassword) {
    return http.put('/v1/profile/password', { oldPassword, newPassword })
  }
}

// 管理员用户管理 API
export const adminUserApi = {
  // 获取用户列表
  getUsers(params = {}) {
    return http.get('/v1/users', { params })
  },

  // 获取用户详情
  getUserById(id) {
    return http.get(`/v1/users/${id}`)
  },

  // 更新用户信息
  updateUser(id, data) {
    return http.put(`/v1/users/${id}`, data)
  },

  // 删除用户
  deleteUser(id) {
    return http.delete(`/v1/users/${id}`)
  },

  // 更新用户状态
  updateUserStatus(id, status) {
    return http.put(`/v1/users/${id}/status`, { status })
  }
}
