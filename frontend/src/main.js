import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import { useUserStore } from './stores/user'

// 引入样式文件
import './styles/tailwind.css'
import 'element-plus/dist/index.css'
import './styles/theme.css'

// Vue Flow 样式
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 全局错误处理：忽略浏览器扩展和媒体播放相关的无害错误
function setupGlobalErrorHandlers() {
  // 处理未捕获的 Promise rejection
  window.addEventListener('unhandledrejection', (event) => {
    const error = event.reason
    const errorMessage = error?.message || String(error)
    
    // 忽略浏览器扩展相关的错误
    if (
      errorMessage.includes('message port closed') ||
      errorMessage.includes('Extension context invalidated') ||
      errorMessage.includes('message port closed before a response was received')
    ) {
      event.preventDefault()
      return
    }
    
    // 忽略媒体播放相关的 AbortError
    if (
      error?.name === 'AbortError' &&
      (errorMessage.includes('play()') || 
       errorMessage.includes('pause()') ||
       errorMessage.includes('interrupted by a call to'))
    ) {
      event.preventDefault()
      return
    }
    
    // 其他错误正常处理（开发环境显示，生产环境可选择性记录）
    if (import.meta.env.DEV) {
      console.warn('Unhandled promise rejection:', error)
    }
  })
  
  // 处理全局错误（可选，主要用于记录）
  window.addEventListener('error', (event) => {
    const errorMessage = event.message || ''
    const error = event.error
    
    // 忽略浏览器扩展相关的错误
    if (
      errorMessage.includes('message port closed') ||
      errorMessage.includes('Extension context invalidated')
    ) {
      event.preventDefault()
      return
    }
    
    // 忽略媒体播放相关的错误
    if (
      error?.name === 'AbortError' &&
      errorMessage.includes('play()')
    ) {
      event.preventDefault()
      return
    }
  })
}

// 初始化用户信息后挂载应用
async function initApp() {
  // 设置全局错误处理
  setupGlobalErrorHandlers()
  
  const userStore = useUserStore()
  await userStore.init()
  app.mount('#app')
}

initApp()
