<template>
  <article 
    class="plant-card-wrapper"
    @click="handleCardClick"
    role="button"
    :aria-label="`查看 ${plant.nickname} 的详情`"
    tabindex="0"
    @keydown.enter="handleCardClick"
    @keydown.space.prevent="handleCardClick"
  >
    <el-card class="plant-card" shadow="hover" :body-style="{ padding: '0px' }">
      <div class="card-image-wrapper">
        <el-image 
          :src="plant.coverUrl || defaultCover" 
          class="plant-image"
          fit="cover"
          loading="lazy"
        >
          <template #error>
            <div class="image-placeholder">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-image>
        <div 
          class="status-badge" 
          :class="plant.status?.toLowerCase()"
          role="status"
          :aria-label="`状态: ${getStatusText(plant.status)}`"
        >
          {{ getStatusText(plant.status) }}
        </div>
      </div>
      
      <div class="card-content">
        <div class="card-header">
          <h3 class="plant-name">{{ plant.nickname }}</h3>
          <slot name="actions">
            <el-button
              v-if="showDelete"
              type="danger"
              link
              size="small"
              @click.stop="handleDelete"
              aria-label="删除植物"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </slot>
        </div>
        <div class="plant-meta">
          <div class="meta-item">
            <el-icon><Location /></el-icon>
            <span>{{ plant.location || '未设置位置' }}</span>
          </div>
          <div class="meta-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ plant.acquiredDate ? formatAcquiredDate(plant.acquiredDate) : '未知时间' }} 入手</span>
          </div>
        </div>
      </div>
    </el-card>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { Location, Calendar, Picture, Delete } from '@element-plus/icons-vue'

const props = defineProps({
  plant: {
    type: Object,
    required: true,
    validator: (value) => {
      return value.nickname && value.id
    }
  },
  showDelete: {
    type: Boolean,
    default: false
  },
  defaultCover: {
    type: String,
    default: 'https://images.unsplash.com/photo-1463320726281-696a485928c7?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'
  }
})

const emit = defineEmits(['click', 'delete'])

const handleCardClick = () => {
  emit('click', props.plant.id)
}

const handleDelete = () => {
  emit('delete', props.plant)
}

const getStatusText = (status) => {
  const statusMap = {
    HEALTHY: '健康',
    SICK: '生病',
    DEAD: '阵亡',
    GIFTED: '送人'
  }
  return statusMap[status] || status || '未知'
}

const formatAcquiredDate = (date) => {
  if (!date) return '未知时间'
  // 如果已经是格式化字符串，直接返回
  if (typeof date === 'string') return date
  // 如果是数组格式 [year, month, day]
  if (Array.isArray(date)) {
    return `${date[0]}-${String(date[1]).padStart(2, '0')}-${String(date[2]).padStart(2, '0')}`
  }
  return date
}
</script>

<style scoped lang="scss">
@import '@/styles/responsive.scss';

.plant-card-wrapper {
  cursor: pointer;
  outline: none;
  
  &:focus-visible {
    .plant-card {
      box-shadow: 0 0 0 3px var(--color-primary);
    }
  }
}

.plant-card {
  border-radius: var(--border-radius-large);
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border: none;
  background: var(--color-surface);
  box-shadow: var(--shadow-md);
  
  &:hover {
    transform: translateY(-8px) scale(1.02);
    box-shadow: var(--shadow-xl);
  }
}

.card-image-wrapper {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.plant-image {
  width: 100%;
  height: 100%;
  transition: transform 0.5s ease;
  
  .plant-card:hover & {
    transform: scale(1.08);
  }
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-background);
  color: var(--color-text-tertiary);
  font-size: 32px;
}

.status-badge {
  position: absolute;
  top: var(--spacing-sm);
  right: var(--spacing-sm);
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: white;
  backdrop-filter: blur(4px);
  box-shadow: var(--shadow-sm);
  
  &.healthy { 
    background: rgba(var(--color-success), 0.85); 
  }
  &.sick { 
    background: rgba(var(--color-warning), 0.85); 
  }
  &.dead { 
    background: rgba(var(--color-danger), 0.85); 
  }
  &.gifted { 
    background: rgba(144, 147, 153, 0.85); 
  }
}

.card-content {
  padding: var(--spacing-lg);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--spacing-sm);
  margin-bottom: var(--spacing-sm);
}

.plant-name {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--color-text-main);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
}

.plant-meta {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text-secondary);
  
  .el-icon {
    color: var(--color-primary);
  }
}

@include respond-to(mobile) {
  .card-image-wrapper {
    height: 160px;
  }
  
  .card-content {
    padding: var(--spacing-md);
  }
  
  .plant-name {
    font-size: 16px;
  }
}
</style>
