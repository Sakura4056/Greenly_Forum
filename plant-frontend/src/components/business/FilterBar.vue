<template>
  <el-card class="filter-card" shadow="never">
    <div class="filter-bar" role="search" aria-label="筛选搜索">
      <!-- 搜索输入框 -->
      <el-input 
        v-if="showSearch"
        :model-value="searchValue"
        :placeholder="searchPlaceholder"
        class="filter-item search-input"
        clearable
        @update:model-value="handleSearchUpdate"
        @keyup.enter="handleSearch"
        aria-label="搜索关键词"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <!-- 状态下拉框 -->
      <el-select 
        v-if="showStatus"
        :model-value="statusValue"
        :placeholder="statusPlaceholder"
        class="filter-item"
        clearable
        @update:model-value="handleStatusUpdate"
        @change="handleChange"
        aria-label="选择状态"
      >
        <slot name="status-options">
          <el-option label="健康" value="HEALTHY" />
          <el-option label="生病" value="SICK" />
          <el-option label="阵亡" value="DEAD" />
          <el-option label="已送人" value="GIFTED" />
        </slot>
      </el-select>
      
      <!-- 日期范围选择器 -->
      <el-date-picker
        v-if="showDateRange"
        :model-value="dateRangeValue"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="YYYY-MM-DD"
        class="filter-item date-picker"
        @update:model-value="handleDateRangeUpdate"
        @change="handleChange"
        aria-label="选择日期范围"
      />
      
      <!-- 自定义插槽 -->
      <slot name="extra"></slot>

      <!-- 重置按钮 -->
      <el-button 
        v-if="showReset"
        @click="handleReset" 
        class="reset-btn"
        aria-label="重置筛选条件"
      >
        重置
      </el-button>
    </div>
  </el-card>
</template>

<script setup>
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  showSearch: {
    type: Boolean,
    default: true
  },
  searchValue: {
    type: String,
    default: ''
  },
  searchPlaceholder: {
    type: String,
    default: '搜索...'
  },
  showStatus: {
    type: Boolean,
    default: true
  },
  statusValue: {
    type: String,
    default: ''
  },
  statusPlaceholder: {
    type: String,
    default: '全部状态'
  },
  showDateRange: {
    type: Boolean,
    default: true
  },
  dateRangeValue: {
    type: Array,
    default: () => []
  },
  showReset: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits([
  'update:searchValue',
  'update:statusValue',
  'update:dateRangeValue',
  'search',
  'reset',
  'change'
])

const handleSearchUpdate = (value) => {
  emit('update:searchValue', value)
}

const handleStatusUpdate = (value) => {
  emit('update:statusValue', value)
}

const handleDateRangeUpdate = (value) => {
  emit('update:dateRangeValue', value)
}

const handleSearch = () => {
  emit('search')
}

const handleReset = () => {
  emit('reset')
}

const handleChange = () => {
  emit('change')
}
</script>

<style scoped lang="scss">
@import '@/styles/responsive.scss';

.filter-card {
  margin-bottom: var(--spacing-lg);
  border-radius: var(--border-radius-base);
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: var(--spacing-md);
  align-items: center;
}

.filter-item {
  width: 200px;
  
  @include respond-to(mobile) {
    width: 100%;
  }
}

.search-input {
  flex: 1;
  min-width: 200px;
  
  @include respond-to(mobile) {
    width: 100%;
  }
}

.date-picker {
  width: 260px !important;
  
  @include respond-to(mobile) {
    width: 100% !important;
  }
}

.reset-btn {
  border-radius: var(--border-radius-small);
}

@include respond-to(mobile) {
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-item,
  .search-input,
  .date-picker {
    width: 100% !important;
  }
}
</style>
