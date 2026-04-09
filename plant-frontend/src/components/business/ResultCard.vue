<template>
  <el-card shadow="hover" class="result-card">
    <template #header>
      <div class="result-header">
        <div class="header-left">
          <el-icon :size="20" :color="iconColor">
            <slot name="icon"><component v-if="icon" :is="icon" /></slot>
          </el-icon>
          <h3>{{ title }}</h3>
        </div>
        <el-tag v-if="statusTag" :type="statusTagType" size="large">
          {{ statusTag }}
        </el-tag>
      </div>
    </template>
    <div class="result-content">
      <slot></slot>
    </div>
  </el-card>
</template>
<script setup>
import { CircleCheckFilled } from '@element-plus/icons-vue'
defineProps({
  title: { type: String, required: true },
  statusTag: { type: String, default: '' },
  statusTagType: { type: String, default: 'success', validator: v => ['success','warning','danger','info'].includes(v) },
  icon: { type: Object, default: () => CircleCheckFilled },
  iconColor: { type: String, default: 'var(--el-color-success)' }
})
</script>
<style scoped lang="scss">
.result-card {
  border-radius: var(--border-radius-large);
  border: 1px solid rgba(0, 0, 0, 0.05);
  :deep(.el-card__header) {
    background: linear-gradient(135deg, rgba(var(--el-color-success-rgb), 0.05) 0%, rgba(var(--el-color-success-rgb), 0.1) 100%);
    border-bottom: 1px solid rgba(var(--el-color-success-rgb), 0.2);
  }
}
.result-header { display: flex; justify-content: space-between; align-items: center;
  .header-left { display: flex; align-items: center; gap: var(--spacing-sm);
    h3 { margin: 0; font-size: 18px; font-weight: 600; color: var(--el-text-color-primary); }
  }
}
.result-content { margin-top: var(--spacing-sm); }
</style>
