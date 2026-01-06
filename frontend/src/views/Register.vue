<template>
  <div class="register-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
    </div>
    
    <!-- 主要内容 -->
    <div class="register-content">
      <!-- 左侧品牌区域 -->
      <div class="brand-section">
        <!-- 装饰性几何图形 -->
        <div class="brand-decoration">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
          <div class="decoration-circle circle-3"></div>
          <div class="decoration-line line-1"></div>
          <div class="decoration-line line-2"></div>
        </div>
        <div class="brand-content">
          <div class="logo">
            <el-icon size="48" color="#FFFFFF">
              <ChatDotRound />
            </el-icon>
          </div>
          <h1 class="brand-title">SoulMate 心伴</h1>
          <p class="brand-subtitle">创建您的专属账户</p>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon color="#FFFFFF"><CircleCheck /></el-icon>
              <span>个性化智能体</span>
            </div>
            <div class="feature-item">
              <el-icon color="#FFFFFF"><CircleCheck /></el-icon>
              <span>专业心理支持</span>
            </div>
            <div class="feature-item">
              <el-icon color="#FFFFFF"><CircleCheck /></el-icon>
              <span>隐私安全保护</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 右侧注册表单 -->
      <div class="form-section">
        <div class="form-container">
          <div class="form-header">
            <h2>注册账户</h2>
            <p>创建您的专属账号</p>
          </div>
          
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="register-form"
            autocomplete="off"
            @submit.prevent="handleRegister"
          >
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名（3-20位字母数字）"
                size="large"
                class="form-input"
                autocomplete="off"
                maxlength="20"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="nickname">
              <el-input
                v-model="form.nickname"
                placeholder="请输入昵称（可选，默认使用用户名）"
                size="large"
                class="form-input"
                autocomplete="off"
                maxlength="20"
              >
                <template #prefix>
                  <el-icon><UserFilled /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="至少8位，需包含字母和数字"
                size="large"
                show-password
                class="form-input"
                autocomplete="new-password"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请再次输入密码确认"
                size="large"
                show-password
                class="form-input"
                autocomplete="new-password"
                @keyup.enter="handleRegister"
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
            
            <div v-if="errorMsg" class="error-message">{{ errorMsg }}</div>
            
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="register-btn"
                :loading="submitting"
                @click="handleRegister"
              >
                <span v-if="!submitting">注册</span>
                <span v-else>注册中...</span>
              </el-button>
            </el-form-item>
          </el-form>
          
          <div class="form-footer">
            <p>已有账户？ <el-link type="primary" @click="$router.push('/login')">立即登录</el-link></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { ChatDotRound, CircleCheck, User, UserFilled, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const submitting = ref(false)
const errorMsg = ref('')

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度必须在3-20位之间', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '用户名只能包含字母、数字、下划线和连字符', trigger: 'blur' }
  ],
  nickname: [
    { max: 20, message: '昵称长度不能超过20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码长度不能少于8位', trigger: 'blur' },
    { 
      pattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d@$!%*?&_-]{8,}$/, 
      message: '密码必须包含字母和数字', 
      trigger: 'blur' 
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  
  errorMsg.value = ''
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await userStore.register(
          form.username, 
          form.password, 
          form.nickname || form.username
        )
        ElMessage.success('注册成功，请登录')
        
        // 注册成功后跳转到登录页面
        router.push('/login')
      } catch (error) {
        // 提取错误消息
        let errorMessage = error?.message
        
        if (!errorMessage && error?.response?.data?.message) {
          errorMessage = error.response.data.message
        }
        
        if (!errorMessage) {
          errorMessage = '注册失败，请重试'
        }
        
        errorMsg.value = errorMessage
        ElMessage.error(errorMessage)
      } finally {
        submitting.value = false
      }
    }
  })
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: var(--app-primary-gradient);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

/* 主要内容布局 */
.register-content {
  display: flex;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

/* 左侧品牌区域 */
.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  position: relative;
  overflow: hidden;
  background: 
    radial-gradient(circle at 20% 50%, rgba(74, 144, 164, 0.4) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(107, 164, 184, 0.5) 0%, transparent 50%),
    var(--app-primary-gradient);
  backdrop-filter: blur(10px);
  border-right: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: inset -2px 0 20px rgba(0, 0, 0, 0.05);
}

.brand-section::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -20%;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 8s ease-in-out infinite;
}

.brand-section::after {
  content: '';
  position: absolute;
  bottom: -30%;
  left: -10%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(107, 164, 184, 0.3) 0%, transparent 70%);
  border-radius: 50%;
  animation: pulse 10s ease-in-out infinite reverse;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
}

/* 装饰性几何图形 */
.brand-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.2);
  animation: float-circle 12s ease-in-out infinite;
}

.circle-1 {
  width: 120px;
  height: 120px;
  top: 15%;
  right: 15%;
  animation-delay: 0s;
}

.circle-2 {
  width: 80px;
  height: 80px;
  bottom: 25%;
  right: 25%;
  animation-delay: 2s;
  border-width: 1.5px;
}

.circle-3 {
  width: 60px;
  height: 60px;
  top: 60%;
  left: 20%;
  animation-delay: 4s;
  border-width: 1px;
}

.decoration-line {
  position: absolute;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  animation: float-line 15s ease-in-out infinite;
}

.line-1 {
  width: 200px;
  height: 2px;
  top: 30%;
  left: 10%;
  transform: rotate(45deg);
  animation-delay: 1s;
}

.line-2 {
  width: 150px;
  height: 1.5px;
  bottom: 40%;
  right: 20%;
  transform: rotate(-30deg);
  animation-delay: 3s;
}

@keyframes float-circle {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 0.6;
  }
  33% {
    transform: translate(20px, -20px) scale(1.1);
    opacity: 0.8;
  }
  66% {
    transform: translate(-15px, 15px) scale(0.9);
    opacity: 0.7;
  }
}

@keyframes float-line {
  0%, 100% {
    transform: translateX(0) rotate(45deg);
    opacity: 0.3;
  }
  50% {
    transform: translateX(30px) rotate(45deg);
    opacity: 0.6;
  }
}

.brand-content {
  text-align: center;
  color: white;
  max-width: 400px;
  position: relative;
  z-index: 1;
}

.logo {
  margin-bottom: 30px;
}

.brand-title {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 16px;
  background: linear-gradient(45deg, #fff, #e3f2fd);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-subtitle {
  font-size: 1.2rem;
  margin-bottom: 40px;
  opacity: 0.9;
  font-weight: 300;
}

.feature-list {
  text-align: left;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  font-size: 1rem;
}

.feature-item .el-icon {
  margin-right: 12px;
  font-size: 1.2rem;
}

/* 右侧表单区域 */
.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
}

.form-container {
  width: 100%;
  max-width: 400px;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-header h2 {
  font-size: 2rem;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.form-header p {
  color: #666;
  font-size: 1rem;
}

.register-form {
  margin-bottom: 24px;
}

.form-input {
  height: 48px;
}

.error-message {
  color: #f56c6c;
  font-size: 14px;
  margin-bottom: 16px;
  text-align: center;
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 12px;
  background: var(--app-primary-gradient);
  border: none;
  transition: all 0.3s ease;
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(74, 144, 164, 0.3);
}

.form-footer {
  text-align: center;
  margin-top: 24px;
}

.form-footer p {
  color: #666;
  font-size: 0.9rem;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-content {
    flex-direction: column;
  }
  
  .brand-section {
    padding: 40px 20px;
    border-right: none;
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  }
  
  .form-section {
    padding: 40px 20px;
  }
  
  .brand-title {
    font-size: 2rem;
  }
  
  .form-header h2 {
    font-size: 1.5rem;
  }
}

@media (max-width: 480px) {
  .brand-section,
  .form-section {
    padding: 20px;
  }
  
  .brand-title {
    font-size: 1.8rem;
  }
  
  .form-container {
    max-width: 100%;
  }
}
</style>

