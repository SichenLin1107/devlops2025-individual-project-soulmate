import axios from 'axios'
import { useUserStore } from '@/stores/user'

// 常量定义
const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'false') !== 'false'
const BASE_API = import.meta.env.VITE_BASE_API || '/api'
const LOGIN_PATH = '/login'
const REDIRECT_DELAY = 100
const AUTH_ERROR_CODES = [40101, 40102, 40103]

// 清除 token 并跳转到登录页
function clearTokenAndRedirect() {
  const userStore = useUserStore()
  userStore.logout()
  setTimeout(() => {
    window.location.href = LOGIN_PATH
  }, REDIRECT_DELAY)
}

const http = axios.create({
  baseURL: BASE_API,
  timeout: 120000,  // 增加到 120 秒，给 agent/LLM 回复足够的时间
  withCredentials: false,
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  }
})

// 添加 PATCH 方法
http.patch = (url, data, config) => {
  return http.request({
    ...config,
    url,
    method: 'PATCH',
    data,
    params: config?.params
  })
}

// 请求拦截：注入 Token
http.interceptors.request.use((config) => {
  const userStore = useUserStore()
  const token = userStore.token || localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截：统一处理后端响应格式
http.interceptors.response.use(
  (response) => {
    const rt = response.request?.responseType
    const ct = response.headers?.['content-type'] || ''
    if (rt === 'blob' || rt === 'arraybuffer' || ct.includes('octet-stream')) {
      return response
    }

    const payload = response.data
    if (payload && typeof payload === 'object' && 'code' in payload) {
      if (payload.code === 0) {
        return payload.data
      }
      const err = new Error(payload.message || '操作失败')
      err.code = payload.code
      err.response = response
      if (err.code === 401 || err.code === 2001) {
        clearTokenAndRedirect()
      }
      throw err
    }
    return payload
  },
  (error) => {
    if (error.response) {
      const status = error.response.status
      const p = error.response.data
      let msg = p?.message
      
      if (!msg && typeof p === 'string') {
        msg = p
      }
      if (!msg) {
        msg = `HTTP ${status}`
      }

      if (status === 401) {
        clearTokenAndRedirect()
        return Promise.reject(new Error('未登录或登录已过期'))
      }

      if (status === 403) {
        if (p?.code && AUTH_ERROR_CODES.includes(p.code)) {
          clearTokenAndRedirect()
          return Promise.reject(new Error('未登录或登录已过期'))
        }
        const err = new Error(msg || '权限不足，无法访问此资源')
        err.code = p?.code ?? status
        err.response = error.response
        throw err
      }

      const err = new Error(msg)
      err.code = p?.code ?? status
      err.response = error.response
      throw err
    } else if (error.request) {
      throw new Error('网络不可用或服务器无响应')
    } else {
      throw new Error(error.message || '请求发生错误')
    }
  }
)

export { http, USE_MOCK, BASE_API }

