<template>
  <Layout>
    <section class="home">
      <!-- 主要欢迎区域 -->
      <div class="hero-section">
        <!-- 简洁装饰元素 -->
        <div class="hero-decorations">
          <div class="decoration-circle circle-1"></div>
          <div class="decoration-circle circle-2"></div>
        </div>
        
        <div class="hero-content">
          <div class="welcome-badge">
            <span class="badge-icon">🌟</span>
            <span>欢迎来到心灵港湾</span>
          </div>
          <h1 class="hero-title">
            <span class="gradient-text">SoulMate 心伴</span>
          </h1>
          <p class="hero-subtitle">您的专属AI心理陪伴伙伴</p>
          <p class="hero-description">
            在这里，您可以找到温暖的倾听者，获得专业的情绪疏导与心理支持。
            无论是日常的情感困扰，还是人生的重要抉择，我们都愿意陪伴您一起面对。
          </p>
          <div class="hero-actions">
            <el-button type="primary" size="large" @click="$router.push('/agents')" class="primary-btn">
              <el-icon class="mr-2"><ChatDotRound /></el-icon>
              开始心理陪伴
            </el-button>
            <el-button size="large" @click="scrollToFeatures" class="secondary-btn">
              <el-icon class="mr-2"><Sunny /></el-icon>
              了解更多
            </el-button>
          </div>
        </div>
        <div class="hero-image">
          <div class="floating-card warm-card">
            <div class="card-glow"></div>
            <el-icon class="card-icon"><Star /></el-icon>
            <p>温暖陪伴</p>
          </div>
          <div class="floating-card warm-card delay-1">
            <div class="card-glow"></div>
            <el-icon class="card-icon"><ChatDotRound /></el-icon>
            <p>专业倾听</p>
          </div>
          <div class="floating-card warm-card delay-2">
            <div class="card-glow"></div>
            <el-icon class="card-icon"><Sunny /></el-icon>
            <p>情绪疏导</p>
          </div>
        </div>
      </div>

      <!-- 服务特色 -->
      <div class="features-section" ref="featuresRef">
        <h2 class="section-title">我们的服务特色</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <h3>24/7 在线陪伴</h3>
            <p>随时随地为您提供温暖的心理陪伴，无需预约，即时响应您的情感需求</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><Lock /></el-icon>
            </div>
            <h3>隐私安全保护</h3>
            <p>严格保护您的隐私信息，所有对话内容均采用加密存储，让您安心倾诉</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><Star /></el-icon>
            </div>
            <h3>个性化智能体</h3>
            <p>多种专业心理陪伴智能体，针对不同场景和需求，提供个性化的陪伴体验</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">
              <el-icon><Sunny /></el-icon>
            </div>
            <h3>专业心理支持</h3>
            <p>基于心理学理论和实践经验，为您提供科学、专业的情绪疏导和心理支持</p>
          </div>
        </div>
      </div>

      <!-- 免责声明 -->
      <div class="disclaimer-wrapper">
        <div class="disclaimer">
          <el-icon class="disclaimer-icon"><InfoFilled /></el-icon>
          <div class="disclaimer-content">
            <strong class="disclaimer-title">免责声明</strong>
            <p class="disclaimer-text">
              本平台提供情绪陪伴服务，不替代专业心理咨询或医疗诊断。
              如有严重心理困扰，请及时寻求专业心理医生或心理咨询师的帮助。
            </p>
          </div>
        </div>
      </div>
    </section>
  </Layout>
</template>

<script setup>
import Layout from '@/components/Layout.vue'
import { ref } from 'vue'
import { 
  ChatDotRound, 
  Sunny, 
  Lock, 
  Star, 
  InfoFilled 
} from '@element-plus/icons-vue'

const featuresRef = ref(null)

const scrollToFeatures = () => {
  if (!featuresRef.value) return
  // 考虑导航栏高度(64px) + 额外间距(32px) = 96px
  const navbarHeight = 64
  const extraPadding = 32
  const offset = featuresRef.value.getBoundingClientRect().top + window.pageYOffset - (navbarHeight + extraPadding)
  window.scrollTo({ top: offset, behavior: 'smooth' })
}
</script>

<style scoped>
.home {
  padding: 0;
  width: 100%;
  margin: 0;
  background: transparent;
  min-height: calc(100vh - 64px);
  padding-bottom: 1rem;
  box-sizing: border-box;
  position: relative;
  overflow: hidden;
}

/* 确保页面底部没有深色区域 */
.home::after {
  content: '';
  display: block;
  height: 0;
  clear: both;
}

/* 主要欢迎区域 - 增强温暖感 */
.hero-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4rem;
  align-items: center;
  padding: 4rem 2rem;
  min-height: 60vh;
  background: transparent;
  max-width: 1400px;
  margin: 0 auto;
  box-sizing: border-box;
  position: relative;
  z-index: 1;
}

/* 装饰性背景元素 */
.hero-decorations {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: -1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--el-color-primary-light-7), var(--el-color-primary-light-8));
  opacity: 0.6;
  animation: gentle-float 6s ease-in-out infinite;
}

.circle-1 {
  width: 80px;
  height: 80px;
  top: 15%;
  right: 20%;
  animation-delay: 0s;
}

.circle-2 {
  width: 60px;
  height: 60px;
  bottom: 25%;
  left: 10%;
  animation-delay: -3s;
}


@keyframes gentle-float {
  0%, 100% { 
    transform: translateY(0px) rotate(0deg); 
    opacity: 0.1;
  }
  50% { 
    transform: translateY(-20px) rotate(180deg); 
    opacity: 0.15;
  }
}


/* 欢迎徽章 */
.welcome-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-5);
  border-radius: 24px;
  padding: 0.5rem 1rem;
  margin-bottom: 1.5rem;
  font-size: 0.85rem;
  font-weight: 500;
  color: var(--el-color-primary);
  box-shadow: var(--el-box-shadow-light);
  transition: all 0.3s ease;
}

.welcome-badge:hover {
  transform: translateY(-1px);
  box-shadow: var(--warm-shadow);
}

.badge-icon {
  font-size: 1.1rem;
  animation: emoji-rotate 2.5s ease-in-out infinite;
}

@keyframes emoji-rotate {
  0%, 100% { 
    transform: rotate(-10deg); 
  }
  50% { 
    transform: rotate(10deg); 
  }
}


.hero-content {
  max-width: 500px;
  padding-left: 2rem; /* 向右移动文字内容 */
}

.hero-title {
  font-size: clamp(2.5rem, 5vw, 4rem);
  font-weight: 800;
  margin: 0 0 1rem;
  line-height: 1.1;
}

.gradient-text {
  background: linear-gradient(135deg, var(--el-color-primary) 0%, #F8BBD9 50%, #FFCCCB 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  position: relative;
}

.gradient-text::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--el-color-primary) 0%, #F8BBD9 50%, #FFCCCB 100%);
  -webkit-background-clip: text;
  background-clip: text;
  opacity: 0;
  animation: text-glow 3s ease-in-out infinite;
}

.hero-subtitle {
  font-size: 1.5rem;
  color: var(--el-text-color-regular);
  margin: 0 0 1.5rem;
  font-weight: 500;
}

.hero-description {
  font-size: 1.1rem;
  color: var(--el-text-color-secondary);
  line-height: 1.7;
  margin: 0 0 2.5rem;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.primary-btn {
  background: var(--app-primary-gradient);
  border: none;
  padding: 1rem 2.5rem;
  font-size: 1.1rem;
  font-weight: 600;
  border-radius: 50px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  box-shadow: var(--warm-shadow);
}

.primary-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  transition: left 0.6s ease;
}

.primary-btn:hover::before {
  left: 100%;
}

.primary-btn:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: var(--floating-shadow);
}

.secondary-btn {
  padding: 1rem 2.5rem;
  font-size: 1.1rem;
  border-radius: 32px;
  border: 2px solid var(--el-color-primary-light-5);
  background: var(--app-card-bg);
  transition: all 0.3s ease;
  color: var(--el-color-primary);
  font-weight: 500;
}

.secondary-btn:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
  transform: translateY(-2px);
  box-shadow: var(--warm-shadow);
}

/* 右侧浮动卡片 */
.hero-image {
  position: relative;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.floating-card {
  position: absolute;
  background: var(--app-card-bg);
  border: 1px solid var(--el-border-color-light);
  border-radius: 24px;
  padding: 1.5rem;
  box-shadow: var(--el-box-shadow);
  text-align: center;
  min-width: 120px;
  animation: float 6s ease-in-out infinite;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.warm-card {
  background: var(--app-warm-card-bg);
  border: 1px solid var(--warm-rose);
  backdrop-filter: blur(12px);
  box-shadow: var(--warm-shadow);
}

.warm-card:hover {
  transform: translateY(-8px) scale(1.05);
  box-shadow: var(--floating-shadow);
}

.card-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, var(--warm-rose) 0%, transparent 70%);
  opacity: 0.1;
  animation: rotate-glow 8s linear infinite;
  pointer-events: none;
}


@keyframes rotate-glow {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}


@keyframes text-glow {
  0%, 100% { opacity: 0; }
  50% { opacity: 0.3; }
}

.floating-card:nth-child(1) {
  top: 15%;
  right: 8%;
}

.floating-card:nth-child(2) {
  top: 45%;
  right: 2%;
}

.floating-card:nth-child(3) {
  bottom: 15%;
  right: 18%;
}

.floating-card.delay-1 {
  animation-delay: -2s;
}

.floating-card.delay-2 {
  animation-delay: -4s;
}

.card-icon {
  font-size: 2rem;
  color: var(--el-color-primary);
  margin-bottom: 0.5rem;
}

.floating-card p {
  margin: 0;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

/* 服务特色区域 */
.features-section {
  padding: 1.5rem 2rem 2rem 2rem;
  background: transparent;
  margin: 1rem auto;
  border-radius: 20px;
  max-width: 1400px;
  box-sizing: border-box;
}

.section-title {
  text-align: center;
  font-size: 2.2rem;
  font-weight: 700;
  margin: 0 0 2rem;
  color: var(--el-text-color-primary);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.feature-card {
  background: var(--app-card-bg);
  border: 1px solid var(--el-border-color-light);
  border-radius: 16px;
  padding: 1.8rem;
  text-align: center;
  transition: all 0.3s ease;
  box-shadow: var(--el-box-shadow-light);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--warm-shadow);
  border-color: var(--el-color-primary-light-5);
}

.feature-icon {
  width: 64px;
  height: 64px;
  background: var(--app-primary-gradient);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.2rem;
  font-size: 1.6rem;
  color: #FFFFFF;
  box-shadow: var(--warm-shadow);
  transition: all 0.3s ease;
}

.feature-card:hover .feature-icon {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(74, 144, 164, 0.25);
}

.feature-card h3 {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 0.6rem;
  color: var(--el-text-color-primary);
}

.feature-card p {
  color: var(--el-text-color-regular);
  line-height: 1.4;
  margin: 0;
  font-size: 0.9rem;
}

/* 免责声明容器 - 底部不起眼位置 */
.disclaimer-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0 2rem 2rem;
  margin-top: 2rem;
  box-sizing: border-box;
}

/* 免责声明 - 粉色配色，与主题统一 */
.disclaimer {
  background: linear-gradient(135deg, #FFF8FA 0%, #FFF5F7 100%);
  border: 1px solid rgba(255, 182, 193, 0.25);
  border-radius: 16px;
  padding: 1.2rem 1.8rem;
  max-width: 1000px;
  width: 100%;
  box-sizing: border-box;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 1.2rem;
  opacity: 0.92;
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(255, 182, 193, 0.08);
}

.disclaimer:hover {
  opacity: 1;
  border-color: rgba(255, 182, 193, 0.35);
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(255, 182, 193, 0.12);
}

.disclaimer-icon {
  color: #FFB6C1;
  font-size: 1.3rem;
  flex-shrink: 0;
  margin-top: 0.1rem;
}

.disclaimer-content {
  flex: 1;
}

.disclaimer-title {
  display: block;
  color: #D87093;
  font-size: 0.95rem;
  font-weight: 600;
  margin: 0 0 0.5rem 0;
  letter-spacing: 0.3px;
}

.disclaimer-text {
  color: #C98A8A;
  font-size: 0.85rem;
  line-height: 1.6;
  margin: 0;
  text-align: left;
}

.mr-1 { margin-right: 0.25rem; }
.mr-2 { margin-right: 0.5rem; }

/* 响应式设计 */
@media (max-width: 1200px) {
  .hero-section,
  .features-section,
  .disclaimer {
    max-width: 95%;
    padding-left: 1.5rem;
    padding-right: 1.5rem;
  }
}

@media (max-width: 768px) {
  .home {
    padding-bottom: 1rem;
  }
  
  .hero-section {
    grid-template-columns: 1fr;
    gap: 2rem;
    padding: 2rem 1rem;
    text-align: center;
    max-width: 100%;
  }
  
  .hero-content {
    padding-left: 0;
    max-width: 100%;
  }
  
  .hero-image {
    height: 300px;
  }
  
  .features-section {
    padding: 2rem 1rem;
    margin: 1rem auto;
    max-width: 100%;
  }
  
  .disclaimer-wrapper {
    padding: 0 1rem;
    margin: 1rem 0 1rem;
  }
  
  .disclaimer {
    padding: 0.875rem 1.75rem;
    max-width: 100%;
  }
  
  .disclaimer-text {
    font-size: 0.875rem;
    line-height: 1.75;
  }
  
  .section-title {
    font-size: 2rem;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
}

@media (max-width: 480px) {
  .hero-actions {
    flex-direction: column;
    align-items: stretch;
  }
  
  .home {
    padding-bottom: 1rem;
  }
  
  .hero-section {
    padding: 1.5rem 1rem;
    min-height: auto;
  }
  
  .hero-title {
    font-size: 2rem;
  }
  
  .hero-subtitle {
    font-size: 1.2rem;
  }
  
  .hero-description {
    font-size: 1rem;
  }
  
  .features-section {
    padding: 1.5rem 1rem;
  }
  
  .disclaimer-wrapper {
    padding: 0 0.75rem;
    margin: 1rem 0 1rem;
  }
  
  .disclaimer {
    padding: 0.875rem 1.25rem;
    gap: 0.875rem;
  }
  
  .disclaimer-icon {
    font-size: 1.25rem;
  }
  
  .disclaimer-title {
    font-size: 0.95rem;
    margin-bottom: 0.625rem;
  }
  
  .disclaimer-text {
    font-size: 0.8rem;
    line-height: 1.7;
  }
  
  .floating-card {
    min-width: 100px;
    padding: 1rem;
  }
}
</style>
