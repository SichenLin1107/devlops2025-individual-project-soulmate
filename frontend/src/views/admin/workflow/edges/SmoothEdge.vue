<template>
  <g class="custom-edge-group">
    <!-- 边路径背景（用于增加点击区域） -->
    <path
      :d="path"
      fill="none"
      stroke="transparent"
      stroke-width="20"
      class="edge-hit-area"
    />
    
    <!-- 主边路径 -->
    <path
      :id="id"
      :d="path"
      fill="none"
      :stroke="edgeColor"
      :stroke-width="strokeWidth"
      :class="['edge-path', { selected, animated }]"
      :style="pathStyle"
    />
    
    <!-- 边标签 -->
    <EdgeLabelRenderer>
    </EdgeLabelRenderer>
    
    <!-- 动画粒子 -->
    <circle
      v-if="animated"
      r="3"
      :fill="particleColor"
      class="edge-particle"
    >
      <animateMotion
        :dur="animationDuration"
        repeatCount="indefinite"
      >
        <mpath :href="`#${id}`" />
      </animateMotion>
    </circle>
  </g>
</template>

<script setup>
import { computed } from 'vue'
import { EdgeLabelRenderer, getSmoothStepPath } from '@vue-flow/core'

const props = defineProps({
  id: { type: String, required: true },
  sourceX: { type: Number, required: true },
  sourceY: { type: Number, required: true },
  targetX: { type: Number, required: true },
  targetY: { type: Number, required: true },
  sourcePosition: { type: String, default: 'right' },
  targetPosition: { type: String, default: 'left' },
  data: { type: Object, default: () => ({}) },
  selected: { type: Boolean, default: false },
  animated: { type: Boolean, default: true },
  style: { type: Object, default: () => ({}) }
})

// 计算边路径 - 使用更大的圆角使转弯更平滑
const path = computed(() => {
  const [edgePath] = getSmoothStepPath({
    sourceX: props.sourceX,
    sourceY: props.sourceY,
    sourcePosition: props.sourcePosition,
    targetX: props.targetX,
    targetY: props.targetY,
    targetPosition: props.targetPosition,
    borderRadius: 24,  // 增大圆角半径
    offset: 30         // 增加偏移量使连线更分散
  })
  return edgePath
})

// 边颜色 - 使用柔和浅色调
const edgeColor = computed(() => {
  if (props.selected) return '#7EB8C9'  // 柔和青
  return '#C4BDB0'  // 柔和灰
})

// 粒子颜色
const particleColor = computed(() => {
  if (props.selected) return '#7EB8C9'
  return '#A8C5D8'  // 浅蓝
})

// 线宽
const strokeWidth = computed(() => props.selected ? 3 : 2)

// 路径样式
const pathStyle = computed(() => ({
  ...props.style,
  strokeLinecap: 'round',
  strokeLinejoin: 'round'
}))

// 动画持续时间
const animationDuration = computed(() => {
  const distance = Math.sqrt(
    Math.pow(props.targetX - props.sourceX, 2) +
    Math.pow(props.targetY - props.sourceY, 2)
  )
  return `${Math.max(1.2, distance / 120)}s`
})
</script>

<style scoped>
.custom-edge-group {
  cursor: pointer;
}

.edge-hit-area {
  cursor: pointer;
}

.edge-path {
  transition: stroke 0.25s, stroke-width 0.25s;
}

.edge-path.selected {
  filter: drop-shadow(0 0 4px rgba(74, 144, 164, 0.4));
}

.edge-path.animated {
  stroke-dasharray: none;
}

.edge-particle {
  opacity: 0.85;
  filter: drop-shadow(0 0 3px currentColor);
}

</style>
