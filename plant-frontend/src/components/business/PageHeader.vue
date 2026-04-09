<template>
  <header class="page-header" role="banner">
    <div class="header-content">
      <h2 class="page-title">{{ title }}</h2>
      <p v-if="subtitle" class="page-subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="header-actions">
      <slot name="actions"></slot>
    </div>
  </header>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  subtitle: {
    type: String,
    default: ''
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/responsive.scss';

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-lg);
  gap: var(--spacing-md);
  flex-wrap: wrap;
  
  .header-content {
    flex: 1;
  }
  
  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: var(--color-text-main);
    margin: 0;
    letter-spacing: -0.5px;
  }
  
  .page-subtitle {
    margin: var(--spacing-xs) 0 0;
    font-size: 14px;
    color: var(--color-text-secondary);
  }
  
  .header-actions {
    display: flex;
    gap: var(--spacing-sm);
    flex-wrap: wrap;
  }
}

@include respond-to(mobile) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    
    .page-title {
      font-size: 24px;
    }
    
    .header-actions {
      width: 100%;
      
      :deep(.el-button) {
        flex: 1;
      }
    }
  }
}
</style>
