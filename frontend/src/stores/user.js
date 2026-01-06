import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'

// 常量定义
const STORAGE_KEYS = {
  TOKEN: 'token',
  USER_INFO: 'userInfo',
  USERNAME: 'username'
}

const TOKEN_EXPIRY_BUFFER = 300

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 恢复 token
  const savedToken = localStorage.getItem(STORAGE_KEYS.TOKEN) || ''
  const token = ref(savedToken)
  
  // 从 localStorage 恢复用户信息
  let savedUserInfo = null
  try {
    const savedUserInfoStr = localStorage.getItem(STORAGE_KEYS.USER_INFO)
    if (savedUserInfoStr) {
      savedUserInfo = JSON.parse(savedUserInfoStr)
    }
  } catch (e) {
    localStorage.removeItem(STORAGE_KEYS.USER_INFO)
  }
  const userInfo = ref(savedUserInfo)

  // 检查 token 是否过期
  function checkTokenExpired(tokenValue) {
    if (!tokenValue) return true
    
    try {
      const parts = tokenValue.split('.')
      if (parts.length !== 3) return true
      
      const payload = JSON.parse(atob(parts[1]))
      const exp = payload.exp
      
      if (!exp) return false
      
      const now = Math.floor(Date.now() / 1000)
      return exp < (now + TOKEN_EXPIRY_BUFFER)
    } catch (e) {
      return true
    }
  }

  const isLoggedIn = computed(() => {
    return !!token.value && !checkTokenExpired(token.value)
  })
  const isAdmin = computed(() => {
    const role = userInfo.value?.role
    return role === 'admin' || role === 'superadmin'
  })
  const isSuperAdmin = computed(() => userInfo.value?.role === 'superadmin')
  const username = computed(() => userInfo.value?.username || userInfo.value?.nickname || '用户')

  // 设置 token
  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem(STORAGE_KEYS.TOKEN, newToken)
    } else {
      localStorage.removeItem(STORAGE_KEYS.TOKEN)
    }
  }

  // 设置用户信息
  function setUserInfo(info) {
    userInfo.value = info
    if (info) {
      localStorage.setItem(STORAGE_KEYS.USER_INFO, JSON.stringify(info))
    } else {
      localStorage.removeItem(STORAGE_KEYS.USER_INFO)
    }
  }

  // 获取用户信息
  async function fetchUserInfo() {
    try {
      const data = await authApi.getCurrentUser()
      setUserInfo(data)
      return data
    } catch (error) {
      throw error
    }
  }

  // 保存认证信息
  async function saveAuthData(data) {
    if (data.token) {
      setToken(data.token)
    }
    
    const basicUser = {
      id: data.userId,
      username: data.username,
      nickname: data.username,
      role: data.role,
      avatar: null,
      bio: null,
      status: 1
    }
    setUserInfo(basicUser)
    
    fetchUserInfo().catch(() => {
      // 获取失败时保持已设置的基本信息
    })
  }

  // 登录
  async function login(username, password) {
    try {
      const data = await authApi.login(username, password)
      await saveAuthData(data)
      return data
    } catch (error) {
      throw error
    }
  }

  // 注册
  async function register(username, password, nickname) {
    try {
      const data = await authApi.register(username, password, nickname)
      await saveAuthData(data)
      return data
    } catch (error) {
      throw error
    }
  }

  // 登出
  async function logout() {
    try {
      await authApi.logout().catch(() => {})
    } catch (error) {
      // 忽略错误
    } finally {
      setToken('')
      setUserInfo(null)
      localStorage.removeItem(STORAGE_KEYS.USERNAME)
    }
  }
  
  // 检查当前 token 是否过期
  function isTokenExpired() {
    return checkTokenExpired(token.value)
  }

  // 初始化用户信息
  async function init() {
    if (!token.value) {
      if (userInfo.value) {
        setUserInfo(null)
      }
      return
    }

    if (isTokenExpired()) {
      logout()
      return
    }
    
    try {
      await fetchUserInfo()
    } catch (error) {
      logout()
    }
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    isSuperAdmin,
    username,
    setToken,
    setUserInfo,
    fetchUserInfo,
    login,
    register,
    logout,
    init,
    isTokenExpired
  }
})

