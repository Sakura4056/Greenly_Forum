<template>
  <section 
    class="stat-card card-hover"
    :class="variantClass"
    role="region"
    :aria-label="`${label}统计`"
  >
    <div class="stat-icon" :class="iconBgClass">
      <el-icon :size="28" :color="iconColor">
        <slot name="icon">{{ icon }}</slot>
      </el-icon>
    </div>
    <div class="stat-info">
      <span class="stat-value" aria-live="polite">{{ displayValue }}</span>
      <span class="stat-label">{{ label }}</span>
      <span v-if="sublabel" class="stat-sublabel">{{ sublabel }}</span>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: {
    type: [Number, String],
    required: true
  },
  label: {
    type: String,
    required: true
  },
  sublabel: {
    type: String,
    default: ''
  },
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'success', 'warning', 'danger', 'info'].includes(value)
  },
  icon: {
    type: Object,
    default: null
  }
})

const variantClass = computed(() => `stat-card--${props.variant}`)

const iconBgClass = computed(() => {
  const bgMap = {
    primary: 'bg-primary-light',
    success: 'bg-success-light',
    warning: 'bg-warning-light',
    danger: 'bg-danger-light',
    info: 'bg-info-light'
  }
  return bgMap[props.variant] || 'bg-primary-light'
})

const iconColor = computed(() => {
  const colorMap = {
    primary: 'var(--color-primary)',
    success: 'var(--color-success)',
    warning: 'var(--color-warning)',
    danger: 'var(--color-danger)',
    info: 'var(--color-info)'
  }
  return colorMap[props.variant] || 'var(--color-primary)'
})

const displayValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString()
  }
  return props.value
})
</script>

<style scoped lang="scss">
@import '@/styles/responsive.scss';

.stat-card {
  background: var(--color-surface);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-xl);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
  cursor: default;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg);
  }
  
  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: var(--border-radius-full);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    
    &.bg-primary-light { 
      background-color: rgba(46, 204, 113, 0.1); 
    }
    &.bg-success-light { 
      background-color: rgba(46, 204, 113, 0.1); 
    }
    &.bg-warning-light { 
      background-color: rgba(243, 156, 18, 0.1); 
    }
    &.bg-danger-light { 
      background-color: rgba(231, 76, 60, 0.1); 
    }
    &.bg-info-light { 
      background-color: rgba(52, 152, 219, 0.1); 
    }
  }
  
  .stat-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    
    .stat-value {
      font-size: 32px;
      font-weight: 700;
      color: var(--color-text-main);
      line-height: 1.2;
    }
    
    .stat-label {
      font-size: 14px;
      color: var(--color-text-secondary);
    }
    
    .stat-sublabel {
      font-size: 12px;
      color: var(--color-text-tertiary);
      margin-top: 2px;
    }
  }
  
  // Variant-specific styles
  &--success {
    border-left: 4px solid var(--color-success);
  }
  
  &--warning {
    border-left: 4px solid var(--color-warning);
  }
  
  &--danger {
    border-left: 4px solid var(--color-danger);
  }
  
  &--info {
    border-left: 4px solid var(--color-info);
  }
}

@include respond-to(mobile) {
  .stat-card {
    padding: var(--spacing-md);
    gap: var(--spacing-md);
    
    .stat-icon {
      width: 50px;
      height: 50px;
    }
    
    .stat-info {
      .stat-value {
        font-size: 24px;
      }
    }
  }
}
</style>
