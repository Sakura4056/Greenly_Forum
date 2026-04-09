<template>
  <el-card
    :shadow="shadow"
    :body-style="bodyStyle"
    :class="[
      'base-card',
      `base-card--${shadow}`,
      { 'base-card--bordered': bordered },
      { 'base-card--hover': hoverable },
      { 'base-card--glass': glass },
      customClass
    ]"
  >
    <template #header v-if="$slots.header || title || extra">
      <div class="base-card__header">
        <div class="base-card__title" v-if="title || $slots.header">
          <slot name="header">
            <h3 v-if="title" class="base-card__title-text">{{ title }}</h3>
          </slot>
        </div>
        <div class="base-card__extra" v-if="extra || $slots.extra">
          <slot name="extra">{{ extra }}</slot>
        </div>
      </div>
    </template>
    
    <div class="base-card__body">
      <slot></slot>
    </div>
    
    <template #footer v-if="$slots.footer">
      <div class="base-card__footer">
        <slot name="footer"></slot>
      </div>
    </template>
  </el-card>
</template>

<script setup>
defineProps({
  shadow: {
    type: String,
    default: 'never',
    validator: (value) => ['always', 'hover', 'never'].includes(value)
  },
  bordered: {
    type: Boolean,
    default: false
  },
  hoverable: {
    type: Boolean,
    default: false
  },
  glass: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  extra: {
    type: String,
    default: ''
  },
  bodyStyle: {
    type: Object,
    default: () => ({})
  },
  customClass: {
    type: String,
    default: ''
  }
})
</script>

<style scoped lang="scss">
.base-card {
  border-radius: var(--border-radius-base);
  transition: all var(--transition-fast);
  overflow: hidden;
  
  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background-color: var(--color-surface);
    border-bottom: 1px solid var(--color-border);
  }
  
  &__title {
    flex: 1;
    
    &-text {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: var(--color-text-main);
    }
  }
  
  &__extra {
    font-size: 14px;
    color: var(--color-text-secondary);
  }
  
  &__body {
    padding: 20px;
    background-color: var(--color-background);
  }
  
  &__footer {
    padding: 16px 20px;
    background-color: var(--color-surface);
    border-top: 1px solid var(--color-border);
    text-align: right;
  }
  
  &--bordered {
    border: 1px solid var(--color-border);
  }
  
  &--hover {
    cursor: pointer;
    
    &:hover {
      box-shadow: var(--shadow-md);
      transform: translateY(-2px);
    }
  }
  
  &--glass {
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(8px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    
    .base-card__header,
    .base-card__footer {
      background: rgba(255, 255, 255, 0.6);
    }
    
    .base-card__body {
      background: rgba(255, 255, 255, 0.4);
    }
  }
  
  &--always {
    box-shadow: var(--shadow-sm);
  }
  
  &--hover {
    &:hover {
      box-shadow: var(--shadow-md);
    }
  }
  
  &--never {
    box-shadow: none;
  }
}
</style>