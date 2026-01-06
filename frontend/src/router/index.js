import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 路径常量
const LOGIN_PATH = '/login'
const REGISTER_PATH = '/register'
const HOME_PATH = '/home'

// 组件懒加载
const Login = () => import('@/views/Login.vue')
const Register = () => import('@/views/Register.vue')
const Home = () => import('@/views/Home.vue')
const Agents = () => import('@/views/Agents.vue')
const AgentDetail = () => import('@/views/AgentDetail.vue')
const Profile = () => import('@/views/Profile.vue')
const History = () => import('@/views/History.vue')
const AdminAgents = () => import('@/views/admin/agent/AgentsManage.vue')
const AdminAgentsDetail = () => import('@/views/admin/agent/AgentsManageDetail.vue')
const AdminKnowledgeBases = () => import('@/views/admin/KnowledgeBases.vue')
const AdminKnowledgeBaseDetail = () => import('@/views/admin/KnowledgeBaseDetail.vue')
const AdminWorkflows = () => import('@/views/admin/Workflows.vue')
const AdminWorkflowEditor = () => import('@/views/admin/workflow/Editor.vue')
const AdminSensitiveWords = () => import('@/views/admin/SensitiveWords.vue')
const AdminModels = () => import('@/views/admin/Models.vue')
const AdminUserManagement = () => import('@/views/admin/UserManagement.vue')

// 路由定义
const routes = [
  { path: '/', redirect: LOGIN_PATH },
  { path: LOGIN_PATH, name: 'login', component: Login, meta: { requiresAuth: false } },
  { path: REGISTER_PATH, name: 'register', component: Register, meta: { requiresAuth: false } },
  { path: HOME_PATH, name: 'home', component: Home, meta: { requiresAuth: true } },
  { path: '/agents', name: 'agents', component: Agents, meta: { requiresAuth: true } },
  { path: '/agents/:id', name: 'agent-detail', component: AgentDetail, meta: { requiresAuth: true } },
  { path: '/profile', name: 'profile', component: Profile, meta: { requiresAuth: true } },
  { path: '/history', name: 'history', component: History, meta: { requiresAuth: true } },
  {
    path: '/admin/agents',
    name: 'admin-agents',
    component: AdminAgents,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/agents/detail',
    name: 'admin-agents-detail',
    component: AdminAgentsDetail,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/knowledge-bases',
    name: 'admin-kbs',
    component: AdminKnowledgeBases,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/knowledge-bases/:id',
    name: 'admin-kb-detail',
    component: AdminKnowledgeBaseDetail,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/workflows',
    name: 'admin-workflows',
    component: AdminWorkflows,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/workflows/editor/:id',
    name: 'admin-workflow-editor',
    component: AdminWorkflowEditor,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/workflows/editor',
    name: 'admin-workflow-editor-new',
    component: AdminWorkflowEditor,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/sensitive-words',
    name: 'admin-sensitive-words',
    component: AdminSensitiveWords,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/models',
    name: 'admin-models',
    component: AdminModels,
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'admin-users',
    component: AdminUserManagement,
    meta: { requiresAuth: true, requiresSuperAdmin: true }
  },
  { path: '/:pathMatch(.*)*', redirect: LOGIN_PATH }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthPage = to.path === LOGIN_PATH || to.path === REGISTER_PATH
  const requiresAdmin = to.meta.requiresAdmin
  const requiresAuth = to.meta.requiresAuth !== false
  
  // 检查 token
  const token = userStore.token || localStorage.getItem('token')
  const hasToken = !!token
  const userAuthenticated = userStore.isLoggedIn || hasToken

  // 不需要认证的页面
  if (to.meta.requiresAuth === false) {
    if (userAuthenticated && isAuthPage) {
      return next(HOME_PATH)
    }
    return next()
  }

  // 需要认证但未登录
  if (!userAuthenticated) {
    return next(LOGIN_PATH)
  }

  // 检查管理员权限
  if (requiresAdmin && !userStore.isAdmin) {
    return next(HOME_PATH)
  }

  // 检查超级管理员权限
  const requiresSuperAdmin = to.meta.requiresSuperAdmin
  if (requiresSuperAdmin && !userStore.isSuperAdmin) {
    return next(HOME_PATH)
  }

  next()
})

export default router

