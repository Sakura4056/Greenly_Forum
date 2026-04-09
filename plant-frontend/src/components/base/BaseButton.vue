<template>
  <el-button
    :type="type"
    :size="size"
    :plain="plain"
    :round="round"
    :circle="circle"
    :disabled="disabled"
    :loading="loading"
    :icon="icon"
    :class="[
      'base-button',
      `base-button--${type}`,
      `base-button--${size}`,
      { 'base-button--plain': plain },
      { 'base-button--round': round },
      { 'base-button--circle': circle },
      { 'base-button--disabled': disabled },
      { 'base-button--loading': loading },
      customClass
    ]"
    @click="$emit('click', $event)"
    @mouseenter="$emit('mouseenter', $event)"
    @mouseleave="$emit('mouseleave', $event)"
    @focus="$emit('focus', $event)"
    @blur="$emit('blur', $event)"
  >
    <slot></slot>
  </el-button>
</template>

<script setup>
defineProps({
  type: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'success', 'warning', 'danger', 'info', 'text'].includes(value)
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['large', 'medium', 'small', 'mini'].includes(value)
  },
  plain: {
    type: Boolean,
    default: false
  },
  round: {
    type: Boolean,
    default: false
  },
  circle: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  icon: {
    type: String,
    default: ''
  },
  customClass: {
    type: String,
    default: ''
  }
})

defineEmits([
  'click',
  'mouseenter',
  'mouseleave',
  'focus',
  'blur'
])
</script>

<style scoped lang="scss">
.base-button {
  border-radius: var(--border-radius-small);
  font-weight: 500;
  transition: all var(--transition-fast);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
    transition: left var(--transition-slow);
  }
  
  &:hover::before {
    left: 100%;
  }
  
  &--primary {
    background-color: var(--color-primary);
    border-color: var(--color-primary);
    color: #fff;
    
    &:hover {
      background-color: var(--color-primary-dark);
      border-color: var(--color-primary-dark);
    }
  }
  
  &--success {
    background-color: var(--color-success);
    border-color: var(--color-success);
    color: #fff;
    
    &:hover {
      background-color: var(--color-success-dark, #27ae60);
      border-color: var(--color-success-dark, #27ae60);
    }
  }
  
  &--warning {
    background-color: var(--color-warning);
    border-color: var(--color-warning);
    color: #fff;
    
    &:hover {
      background-color: var(--color-warning-dark, #e67e22);
      border-color: var(--color-warning-dark, #e67e22);
    }
  }
  
  &--danger {
    background-color: var(--color-danger);
    border-color: var(--color-danger);
    color: #fff;
    
    &:hover {
      background-color: var(--color-danger-dark, #c0392b);
      border-color: var(--color-danger-dark, #c0392b);
    }
  }
  
  &--info {
    background-color: var(--color-info);
    border-color: var(--color-info);
    color: #fff;
    
    &:hover {
      background-color: var(--color-info-dark, #2980b9);
      border-color: var(--color-info-dark, #2980b9);
    }
  }
  
  &--text {
    color: var(--color-text-main);
    
    &:hover {
      color: var(--color-primary);
      background-color: var(--color-primary-light-9);
    }
  }
  
  &--plain {
    background-color: transparent;
    
    &.base-button--primary {
      color: var(--color-primary);
      border-color: var(--color-primary);
      
      &:hover {
        background-color: var(--color-primary-light-9);
      }
    }
    
    &.base-button--success {
      color: var(--color-success);
      border-color: var(--color-success);
      
      &:hover {
        background-color: var(--color-success-light-9);
      }
    }
    
    &.base-button--warning {
      color: var(--color-warning);
      border-color: var(--color-warning);
      
      &:hover {
        background-color: var(--color-warning-light-9);
      }
    }
    
    &.base-button--danger {
      color: var(--color-danger);
      border-color: var(--color-danger);
      
      &:hover {
        background-color: var(--color-danger-light-9);
      }
    }
    
    &.base-button--info {
      color: var(--color-info);
      border-color: var(--color-info);
      
      &:hover {
        background-color: var(--color-info-light-9);
      }
    }
  }
  
  &--round {
    border-radius: 20px;
  }
  
  &--circle {
    border-radius: 50%;
  }
  
  &--disabled {
    opacity: 0.6;
    cursor: not-allowed;
    
    &::before {
      display: none;
    }
  }
  
  &--loading {
    position: relative;
    
    &::before {
      display: none;
    }
  }
  
  &--large {
    font-size: 16px;
    padding: 12px 24px;
  }
  
  &--medium {
    font-size: 14px;
    padding: 10px 20px;
  }
  
  &--small {
    font-size: 12px;
    padding: 8px 16px;
  }
  
  &--mini {
    font-size: 12px;
    padding: 6px 12px;
  }
}
</style>