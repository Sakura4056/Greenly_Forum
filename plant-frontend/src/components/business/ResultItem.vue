<template>
  <div 
    class="result-item" 
    :class="variantClass"
    role="group"
    :aria-label="label"
  >
    <div class="item-label">
      <el-icon v-if="icon">
        <slot name="icon">{{ icon }}</slot>
      </el-icon>
      <span>{{ label }}</span>
    </div>
    <div class="item-value">
      <slot></slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  label: {
    type: String,
    required: true
  },
  variant: {
    type: String,
    default: 'default',
    validator: (value) => ['default', 'highlight', 'warning', 'info'].includes(value)
  },
  icon: {
    type: Object,
    default: null
  }
})

const variantClass = computed(() => `result-item--${props.variant}`)
</script>

<style scoped lang="scss">
.result-item {
  margin-bottom: var(--spacing-lg);
  padding: var(--spacing-md);
  background-color: #fafafa;
  border-radius: var(--border-radius-base);
  border-left: 4px solid var(--color-primary);
  
  &--highlight {
    background: linear-gradient(135deg, 
      rgba(var(--el-color-success-rgb), 0.05) 0%, 
      rgba(var(--el-color-success-rgb), 0.1) 100%);
    border-left-color: var(--el-color-success);
  }
  
  &--warning {
    background: linear-gradient(135deg, 
      rgba(var(--el-color-warning-rgb), 0.05) 0%, 
      rgba(var(--el-color-warning-rgb), 0.1) 100%);
    border-left-color: var(--el-color-warning);
  }
  
  &--info {
    background: linear-gradient(135deg, 
      rgba(var(--el-color-info-rgb), 0.05) 0%, 
      rgba(var(--el-color-info-rgb), 0.1) 100%);
    border-left-color: var(--el-color-info);
  }
  
  .item-label {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    margin-bottom: var(--spacing-sm);
    font-weight: 600;
    color: var(--el-text-color-primary);
    font-size: 14px;
    
    .el-icon {
      color: var(--el-color-primary);
    }
  }
  
  .item-value {
    color: var(--el-text-color-regular);
    line-height: 1.6;
    font-size: 15px;
  }
}
</style>
