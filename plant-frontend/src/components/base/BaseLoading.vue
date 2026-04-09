<template>
  <div class="base-loading" :class="{ 'base-loading--fullscreen': fullscreen, 'base-loading--inline': inline }">
    <div class="base-loading__overlay" v-if="fullscreen"></div>
    <div class="base-loading__content" :class="`base-loading__content--${size}`">
      <div class="base-loading__spinner" :class="`base-loading__spinner--${type}`">
        <div v-if="type === 'pulse'" class="spinner-pulse"></div>
        <div v-else-if="type === 'dots'" class="spinner-dots">
          <div class="dot"></div>
          <div class="dot"></div>
          <div class="dot"></div>
        </div>
        <div v-else-if="type === 'ring'" class="spinner-ring">
          <div class="ring"></div>
        </div>
        <div v-else-if="type === 'flower'" class="spinner-flower">
          <div class="petal"></div>
          <div class="petal"></div>
          <div class="petal"></div>
          <div class="petal"></div>
          <div class="petal"></div>
        </div>
      </div>
      <div v-if="text" class="base-loading__text">{{ text }}</div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  fullscreen: {
    type: Boolean,
    default: false
  },
  inline: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'medium',
    validator: (value) => ['small', 'medium', 'large'].includes(value)
  },
  type: {
    type: String,
    default: 'pulse',
    validator: (value) => ['pulse', 'dots', 'ring', 'flower'].includes(value)
  },
  text: {
    type: String,
    default: ''
  }
})
</script>

<style scoped lang="scss">
.base-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  
  &--fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: var(--z-index-modal);
  }
  
  &--inline {
    display: inline-flex;
  }
  
  &__overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(2px);
  }
  
  &__content {
    position: relative;
    z-index: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    
    &--small {
      .base-loading__spinner {
        width: 24px;
        height: 24px;
      }
      
      .base-loading__text {
        font-size: 12px;
      }
    }
    
    &--medium {
      .base-loading__spinner {
        width: 32px;
        height: 32px;
      }
      
      .base-loading__text {
        font-size: 14px;
      }
    }
    
    &--large {
      .base-loading__spinner {
        width: 48px;
        height: 48px;
      }
      
      .base-loading__text {
        font-size: 16px;
      }
    }
  }
  
  &__spinner {
    position: relative;
    
    &--pulse {
      .spinner-pulse {
        width: 100%;
        height: 100%;
        border: 3px solid var(--color-border);
        border-top: 3px solid var(--color-primary);
        border-radius: 50%;
        animation: pulse 1s linear infinite;
      }
    }
    
    &--dots {
      display: flex;
      align-items: center;
      justify-content: space-between;
      width: 100%;
      height: 100%;
      
      .dot {
        width: 8px;
        height: 8px;
        background-color: var(--color-primary);
        border-radius: 50%;
        animation: dot-bounce 1.4s ease-in-out infinite both;
        
        &:nth-child(1) {
          animation-delay: -0.32s;
        }
        
        &:nth-child(2) {
          animation-delay: -0.16s;
        }
      }
    }
    
    &--ring {
      .ring {
        width: 100%;
        height: 100%;
        border: 3px solid var(--color-border);
        border-top: 3px solid var(--color-primary);
        border-radius: 50%;
        animation: spin 1s linear infinite;
      }
    }
    
    &--flower {
      position: relative;
      
      .petal {
        position: absolute;
        width: 10px;
        height: 20px;
        background-color: var(--color-primary);
        border-radius: 5px;
        top: -5px;
        left: 50%;
        transform-origin: bottom center;
        animation: flower 2s ease-in-out infinite;
        
        &:nth-child(1) {
          transform: translateX(-50%) rotate(0deg);
          animation-delay: 0s;
        }
        
        &:nth-child(2) {
          transform: translateX(-50%) rotate(72deg);
          animation-delay: 0.4s;
        }
        
        &:nth-child(3) {
          transform: translateX(-50%) rotate(144deg);
          animation-delay: 0.8s;
        }
        
        &:nth-child(4) {
          transform: translateX(-50%) rotate(216deg);
          animation-delay: 1.2s;
        }
        
        &:nth-child(5) {
          transform: translateX(-50%) rotate(288deg);
          animation-delay: 1.6s;
        }
      }
    }
  }
  
  &__text {
    color: var(--color-text-secondary);
    font-weight: 500;
  }
}

@keyframes pulse {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes dot-bounce {
  0%, 80%, 100% {
    transform: scale(0);
  }
  40% {
    transform: scale(1);
  }
}

@keyframes flower {
  0%, 100% {
    transform: translateX(-50%) rotate(var(--initial-rotation, 0deg)) scale(0.8);
    opacity: 0.5;
  }
  50% {
    transform: translateX(-50%) rotate(var(--initial-rotation, 0deg)) scale(1);
    opacity: 1;
  }
}
</style>