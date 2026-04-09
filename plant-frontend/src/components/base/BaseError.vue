<template>
  <div class="base-error" :class="[`base-error--${type}`, { 'base-error--fullwidth': fullwidth }]">
    <div class="base-error__icon">
      <el-icon v-if="type === 'error'" :size="24"><CircleCloseFilled /></el-icon>
      <el-icon v-else-if="type === 'warning'" :size="24"><WarningFilled /></el-icon>
      <el-icon v-else-if="type === 'info'" :size="24"><InfoFilled /></el-icon>
      <el-icon v-else-if="type === 'success'" :size="24"><CheckCircleFilled /></el-icon>
    </div>
    <div class="base-error__content">
      <h3 v-if="title" class="base-error__title">{{ title }}</h3>
      <p v-if="message" class="base-error__message">{{ message }}</p>
      <div v-if="$slots.default" class="base-error__slot">
        <slot></slot>
      </div>
      <div v-if="actions" class="base-error__actions">
        <slot name="actions">
          <el-button v-if="showRetry" type="primary" size="small" @click="$emit('retry')">重试</el-button>
          <el-button v-if="showClose" type="text" size="small" @click="$emit('close')">关闭</el-button>
        </slot>
      </div>
    </div>
  </div>
</template>

<script setup>
import { CircleCloseFilled, WarningFilled, InfoFilled, CheckCircleFilled } from '@element-plus/icons-vue'

defineProps({
  type: {
    type: String,
    default: 'error',
    validator: (value) => ['error', 'warning', 'info', 'success'].includes(value)
  },
  title: {
    type: String,
    default: ''
  },
  message: {
    type: String,
    default: ''
  },
  fullwidth: {
    type: Boolean,
    default: false
  },
  showRetry: {
    type: Boolean,
    default: false
  },
  showClose: {
    type: Boolean,
    default: false
  },
  actions: {
    type: Boolean,
    default: false
  }
})

defineEmits(['retry', 'close'])
</script>

<style scoped lang="scss">
.base-error {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  border-radius: var(--border-radius-base);
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  transition: all var(--transition-fast);
  
  &--fullwidth {
    width: 100%;
  }
  
  &--error {
    border-color: var(--color-danger-light);
    background-color: var(--color-danger-light-9);
    
    .base-error__icon {
      color: var(--color-danger);
    }
    
    .base-error__title {
      color: var(--color-danger);
    }
  }
  
  &--warning {
    border-color: var(--color-warning-light);
    background-color: var(--color-warning-light-9);
    
    .base-error__icon {
      color: var(--color-warning);
    }
    
    .base-error__title {
      color: var(--color-warning);
    }
  }
  
  &--info {
    border-color: var(--color-info-light);
    background-color: var(--color-info-light-9);
    
    .base-error__icon {
      color: var(--color-info);
    }
    
    .base-error__title {
      color: var(--color-info);
    }
  }
  
  &--success {
    border-color: var(--color-success-light);
    background-color: var(--color-success-light-9);
    
    .base-error__icon {
      color: var(--color-success);
    }
    
    .base-error__title {
      color: var(--color-success);
    }
  }
  
  &__icon {
    flex-shrink: 0;
    margin-top: 2px;
  }
  
  &__content {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  &__title {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    line-height: 1.4;
  }
  
  &__message {
    margin: 0;
    font-size: 14px;
    color: var(--color-text-secondary);
    line-height: 1.5;
  }
  
  &__slot {
    font-size: 14px;
    color: var(--color-text-secondary);
    line-height: 1.5;
  }
  
  &__actions {
    display: flex;
    gap: 8px;
    margin-top: 8px;
  }
}
</style>