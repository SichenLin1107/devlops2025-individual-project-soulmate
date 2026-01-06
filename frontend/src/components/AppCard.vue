<script setup>
import { computed } from 'vue'
import { Download, ChatDotSquare } from '@element-plus/icons-vue'

const props = defineProps({ item: { type: Object, required: true } })

const isFree = computed(() => Number(props.item.price) === 0)
const priceLabel = computed(() => isFree.value ? '免费' : `¥${Number(props.item.price).toLocaleString()}`)
const statusText = computed(() => props.item.status === 1 ? '已启用' : '已停用')
const statusClass = computed(() => 
  props.item.status === 1 
    ? 'bg-green-50 text-green-600 ring-green-200' 
    : 'bg-gray-50 text-gray-500 ring-gray-200'
)
const priceClass = computed(() => 
  isFree.value 
    ? 'bg-green-50 text-green-600 ring-green-200' 
    : 'bg-orange-50 text-orange-600 ring-orange-200'
)
</script>

<template>
  <article
    class="common-card app-card-content"
    role="button" tabindex="0" :aria-label="item.name"
  >
    <div class="app-card-content">
      <img :src="item.avatar" :alt="item.name" loading="lazy"
           class="app-card-avatar" />

      <div class="app-card-info">
        <div class="common-card-header">
          <h3 class="common-card-title">{{ item.name }}</h3>
          <div class="app-card-badges">
            <!-- 状态徽标 -->
            <span class="app-badge" :class="statusClass">{{ statusText }}</span>
            <!-- 价格标签 -->
            <span class="app-badge" :class="priceClass">
              {{ priceLabel }}
            </span>
          </div>
        </div>

        <p class="app-card-description">{{ item.description }}</p>

        <div class="app-card-stats">
          <el-rate :model-value="item.rating" disabled show-score score-template="{value}" class="rating" />
          <div class="stat-item" aria-label="下载量">
            <el-icon><Download /></el-icon>{{ item.downloads }}
          </div>
          <div class="stat-item" aria-label="评价数">
            <el-icon><ChatDotSquare /></el-icon>{{ item.reviews }}
          </div>
        </div>

        <div class="common-card-tags">
          <el-tag v-for="t in item.tags" :key="t" size="small" effect="plain" type="info" round>{{ t }}</el-tag>
        </div>
      </div>
    </div>
  </article>
</template>

<style scoped>
/* 使用通用卡片样式，只定义特有的样式 */

.app-card-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.app-card-avatar {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  object-fit: cover;
  border: 2px solid var(--el-color-primary-light-5);
  flex-shrink: 0;
  box-shadow: var(--el-box-shadow-light);
  transition: all 0.3s ease;
}

.common-card:hover .app-card-avatar {
  border-color: var(--el-color-primary);
  box-shadow: var(--warm-shadow);
  transform: scale(1.03);
}

.app-card-info {
  flex: 1;
  min-width: 0;
}

.app-card-badges {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.app-badge {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  border: 1px solid;
  white-space: nowrap;
}

.bg-green-50.text-green-600.ring-green-200 {
  background-color: var(--el-color-success-light-9);
  color: var(--el-color-success);
  border-color: var(--el-color-success-light-5);
}

.bg-gray-50.text-gray-500.ring-gray-200 {
  background-color: var(--el-fill-color-lighter);
  color: var(--el-text-color-secondary);
  border-color: var(--el-border-color-lighter);
}

.bg-orange-50.text-orange-600.ring-orange-200 {
  background-color: var(--el-color-warning-light-9);
  color: var(--el-color-warning);
  border-color: var(--el-color-warning-light-5);
}

.app-card-description {
  font-size: 14px;
  color: var(--el-text-color-regular);
  line-height: 1.5;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}

.app-card-stats {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.rating :deep(.el-rate__text) {
  color: var(--el-text-color-regular);
  font-size: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

/* 标签样式继承自 common-card-tags */
</style>