<template>
  <div class="layout">
    <!-- 顶部悬浮栏 -->
    <header class="topbar">
      <div class="topbar-inner">
        <div class="left">
          <el-icon class="brand-icon"><Star /></el-icon>
          <h1>SoulMate 心伴</h1>
          <div class="tag">您的AI心灵伙伴</div>
        </div>

        <div class="right">
          <el-dropdown @command="handleCommand" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="32" :src="userAvatar" class="user-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-details">
                <span class="user-name">{{ username }}</span>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="history">
                  <el-icon><Clock /></el-icon>
                  历史记录
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          
          <!-- 侧边栏折叠按钮 -->
          <el-button 
            type="text" 
            class="sidebar-toggle"
            @click="toggleSidebar"
            :icon="sidebarCollapsed ? Expand : Fold"
          />
        </div>
      </div>
    </header>

    <!-- 主体区域 -->
    <div class="main">
      <aside 
        class="sidebar" 
        :class="{ 'collapsed': sidebarCollapsed }"
        :style="{ width: sidebarWidth + 'px' }"
      >
        <el-menu 
          :default-active="route.path" 
          background-color="transparent"
          text-color="var(--el-text-color-regular)" 
          active-text-color="var(--el-color-primary)"
          @select="handleMenuSelect"
          :collapse="sidebarCollapsed"
        >
          <el-menu-item v-for="m in baseMenus" :key="m.id" :index="m.path">
            <el-icon><component :is="m.icon" /></el-icon>
            <template #title>{{ m.name }}</template>
          </el-menu-item>
          <el-sub-menu v-if="isAdmin" index="admin" popper-class="admin-submenu">
            <template #title>
              <el-icon><Tools /></el-icon>
              <span>管理功能</span>
            </template>
            <el-menu-item 
              v-for="m in adminMenus" 
              :key="m.id" 
              :index="m.path"
            >
              <el-icon><component :is="m.icon" /></el-icon>
              <template #title>{{ m.name }}</template>
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </aside>

      <main 
        class="content"
        :style="{ 
          marginLeft: sidebarWidth + 'px',
          width: `calc(100% - ${sidebarWidth}px)`
        }"
      >
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { 
  User, 
  ArrowDown, 
  SwitchButton, 
  Star, 
  Clock,
  HomeFilled,
  ChatLineRound,
  Tools,
  Avatar,
  Link,
  Monitor,
  RefreshRight,
  Reading,
  Lock,
  Fold,
  Expand
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const username = computed(() => userStore.username)
const isAdmin = computed(() => userStore.isAdmin)
const isSuperAdmin = computed(() => userStore.isSuperAdmin)

// 侧边栏折叠状态
const sidebarCollapsed = ref(false)

// 切换侧边栏折叠状态
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 计算侧边栏宽度
const sidebarWidth = computed(() => sidebarCollapsed.value ? 80 : 220)

// 用户头像
const userAvatar = computed(() => {
  const userInfo = userStore.userInfo
  if (userInfo?.avatar) {
    return userInfo.avatar
  }
  // 使用默认头像生成器
  return `https://api.dicebear.com/7.x/avataaars/svg?seed=${username.value || 'user'}`
})

// 基础菜单（所有用户可见）
const baseMenus = Object.freeze([
  { id: 1, name: '首页', path: '/home', icon: HomeFilled },
  { id: 2, name: '智能体广场', path: '/agents', icon: ChatLineRound }
])

// 管理员菜单（Admin和SuperAdmin都可以看到）
const adminMenus = computed(() => {
  const menus = [
    { id: 6, name: '智能体管理', path: '/admin/agents', icon: Link },
    { id: 7, name: '模型管理', path: '/admin/models', icon: Monitor }, 
    { id: 9, name: '工作流管理', path: '/admin/workflows', icon: RefreshRight },
    { id: 10, name: '知识库管理', path: '/admin/knowledge-bases', icon: Reading },
    { id: 11, name: '敏感词管理', path: '/admin/sensitive-words', icon: Lock }
  ]
  // 只有SuperAdmin可以看到用户管理
  if (isSuperAdmin.value) {
    menus.unshift({ id: 5, name: '用户管理', path: '/admin/users', icon: Avatar })
  }
  return menus
})

const handleMenuSelect = (index) => {
  if (!index || typeof index !== 'string') return
  const targetPath = index.startsWith('/') ? index : `/${index}`
    router.push(targetPath).catch(err => {
      if (err.name !== 'NavigationDuplicated') {
        console.error('Navigation failed:', err)
      }
    })
}

const logout = async () => {
  // 统一使用 store 的 logout 方法，它会处理后端登出和本地清除
  await userStore.logout()
  router.replace('/login')
}

const handleCommand = (command) => {
  const routes = { profile: '/profile', history: '/history' }
  if (routes[command]) {
    router.push(routes[command])
  } else if (command === 'logout') {
    logout()
  }
}
</script>

<style scoped>
.layout {
  min-height: 100vh;
  max-width: 1920px;
  width: 100%;
  margin: 0 auto;
  background: transparent;
  color: var(--el-text-color-primary);
  font-family: 'Nunito', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow-x: hidden;
  position: relative;
}

.topbar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
  background: var(--app-primary-gradient);
  backdrop-filter: blur(20px);
}

.topbar-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  padding: 16px 32px;
  min-height: 64px;
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: var(--el-box-shadow-light);
  transition: all 0.3s ease;
  position: relative;
}

.topbar-inner:hover {
  box-shadow: var(--warm-shadow);
}

.left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-icon {
  font-size: 24px;
  color: #FFFFFF;
  margin-right: 8px;
  transition: all 0.3s ease;
  animation: star-float 3s ease-in-out infinite;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.brand-icon:hover {
  transform: scale(1.1);
  color: rgba(255, 255, 255, 0.9);
  animation-play-state: paused;
}

@keyframes star-float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-4px) rotate(5deg);
  }
}

.left h1 {
  font-size: 20px;
  font-weight: 600;
  color: #FFFFFF;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.tag {
  display: inline-block;
  font-size: 11px;
  color: #FFFFFF;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 12px;
  padding: 2px 8px;
  font-weight: 500;
  margin-left: 8px;
  backdrop-filter: blur(10px);
}


.right {
  display: flex;
  align-items: center;
  gap: 10px;
  white-space: nowrap;
  margin-left: auto;
}

.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 16px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.25);
  border-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  border: 2px solid rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.user-name {
  font-weight: 500;
  color: #FFFFFF;
  font-size: 14px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.dropdown-icon {
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

.user-info:hover .dropdown-icon {
  transform: rotate(180deg);
  color: #FFFFFF;
}

.main {
  flex: 1;
  margin-top: 64px;
  display: flex;
  min-height: calc(100vh - 64px);
  background: var(--el-bg-color-page);
  position: relative;
}

.sidebar {
  position: fixed;
  top: 64px;
  left: 0;
  height: calc(100vh - 64px);
  backdrop-filter: blur(20px);
  background: var(--app-primary-gradient);
  border-right: 1px solid var(--el-border-color-light);
  padding: 20px 12px;
  overflow-y: auto;
  overflow-x: hidden;
  box-shadow: var(--warm-shadow);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 999;
}

.sidebar.collapsed {
  width: 80px !important;
}

.content {
  background: var(--el-bg-color-page);
  padding: 0;
  color: var(--el-text-color-primary);
  flex: 1;
  min-height: calc(100vh - 64px);
  box-sizing: border-box;
  overflow-x: hidden;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 菜单基础样式 */
:deep(.el-menu) {
  border-right: none !important;
  background-color: transparent !important;
}

/* 菜单项基础样式 */
:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.95) !important;
  border-radius: 12px;
  margin-bottom: 6px;
  transition: all 0.3s ease;
  font-weight: 500;
  font-size: 14px;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.2) !important;
  color: #FFFFFF !important;
  transform: translateX(2px);
  box-shadow: 0 2px 8px rgba(255, 255, 255, 0.1);
}

:deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.3) !important;
  color: #FFFFFF !important;
  font-weight: 600;
  box-shadow: 0 2px 12px rgba(255, 255, 255, 0.2);
}

/* 子菜单样式 */
:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.95) !important;
  font-weight: 500;
  transition: all 0.3s ease;
  border-radius: 12px;
  margin-bottom: 6px;
  font-size: 14px;
}

:deep(.el-sub-menu__title:hover) {
  background-color: rgba(255, 255, 255, 0.2) !important;
  color: #FFFFFF !important;
}

:deep(.el-sub-menu .el-menu-item) {
  color: rgba(255, 255, 255, 0.9) !important;
  padding-left: 36px !important;
  font-size: 13px;
}

:deep(.el-sub-menu .el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.2) !important;
  color: #FFFFFF !important;
}

:deep(.el-sub-menu .el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.3) !important;
  color: #FFFFFF !important;
}

/* 图标样式 */
:deep(.el-menu-item .el-icon),
:deep(.el-sub-menu__title .el-icon) {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  margin-right: 8px;
}

:deep(.el-menu-item:hover .el-icon),
:deep(.el-sub-menu__title:hover .el-icon) {
  color: #FFFFFF;
  transform: scale(1.1);
}

:deep(.el-menu-item.is-active .el-icon) {
  color: #FFFFFF;
  transform: scale(1.05);
}

/* 折叠状态样式 - 清理并重构 */
.sidebar.collapsed :deep(.el-menu-item:hover) {
  transform: none !important;
}

.sidebar.collapsed :deep(.el-sub-menu .el-menu-item) {
  padding-left: 0 !important;
}

/* 侧边栏折叠按钮样式 */
.sidebar-toggle {
  color: rgba(255, 255, 255, 0.8) !important;
  font-size: 18px;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
  margin-left: 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  color: #FFFFFF !important;
  border-color: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
}

:deep(.admin-submenu) {
  max-height: 400px;
  overflow-y: auto;
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
}

@media (max-width: 768px) {
  .layout {
    max-width: 100%;
  }
  
  .topbar-inner {
    padding: 16px 20px;
    min-height: 64px;
  }
  
  .main {
    grid-template-columns: 1fr;
    padding: 0;
    flex-direction: column;
    margin-top: 64px;
    min-height: calc(100vh - 64px);
  }
  
  .sidebar {
    position: fixed;
    top: 64px;
    left: -220px;
    transition: left 0.3s ease;
    z-index: 999;
    height: calc(100vh - 64px);
  }
  
  .sidebar.mobile-open {
    left: 0;
  }
  
  .content {
    margin-left: 0 !important;
    width: 100% !important;
    min-height: calc(100vh - 64px);
  }
  
  .sidebar-toggle {
    display: none;
  }
}
</style>


