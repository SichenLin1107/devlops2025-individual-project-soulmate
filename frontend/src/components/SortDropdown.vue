<template>
  <div class="sort-bar">
    <el-select v-model="selectedSort" @change="onSortChange" class="select" :teleported="false" placeholder=" ">
      <template #prefix><span class="desc">排序方式：</span></template>
      <el-option label="默认" value="default" />
      <el-option label="评分" value="rating" />
      <el-option label="下载量" value="downloads" />
    </el-select>
    <el-select v-model="selectedSortOrder" @change="onSortChange" class="select" :teleported="false" placeholder=" ">
      <template #prefix><span class="desc">顺序：</span></template>
      <el-option label="升序" value="ASC" />
      <el-option label="降序" value="DESC" />
    </el-select>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['sortChange'])

const selectedSort = ref('default')
const selectedSortOrder = ref('DESC')

const onSortChange = () => {
  emit('sortChange', {
    sortBy: selectedSort.value === 'default' ? 'id' : selectedSort.value,
    sortOrder: selectedSortOrder.value
  })
}
</script>

<style scoped>
.sort-bar {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.select {
  flex: 1 1 0;
  --el-input-width: 100%;
}

.desc {
  color: var(--el-text-color-secondary);
  font-size: 14px;
  margin-right: 4px;
  white-space: nowrap;
}

:deep(.el-select__wrapper) {
  min-height: 36px;
  border-radius: 8px;
  padding: 0 10px;
  box-sizing: border-box;
}

:deep(.el-select__suffix) {
  right: 6px;
}

:deep(.el-select__selected-item),
:deep(.el-select__placeholder) {
  display: none;
}
</style>

