<template>
  <div class="base-table">
    <!-- 表格工具栏 -->
    <div v-if="$slots.toolbar" class="table-toolbar mb-4">
      <slot name="toolbar"></slot>
    </div>

    <!-- 表格主体 -->
    <el-table
      v-loading="loading"
      :data="data"
      :border="border"
      :stripe="stripe"
      :height="height"
      :max-height="maxHeight"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      class="base-table-body"
    >
      <!-- 选择列 -->
      <el-table-column
        v-if="showSelection"
        type="selection"
        width="55"
        align="center"
      />

      <!-- 序号列 -->
      <el-table-column
        v-if="showIndex"
        type="index"
        label="序号"
        width="60"
        align="center"
      />

      <!-- 自定义列插槽 -->
      <slot></slot>

      <!-- 操作列 -->
      <el-table-column
        v-if="$slots.operation"
        label="操作"
        :width="operationWidth"
        align="center"
        fixed="right"
      >
        <template #default="{ row, $index }">
          <slot name="operation" :row="row" :$index="$index"></slot>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div v-if="pagination" class="table-pagination mt-4">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="pageSizes"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

// Props
const props = defineProps({
  // 表格数据
  data: {
    type: Array,
    default: () => []
  },
  // 是否显示边框
  border: {
    type: Boolean,
    default: true
  },
  // 是否斑马纹
  stripe: {
    type: Boolean,
    default: false
  },
  // 表格高度
  height: {
    type: [Number, String],
    default: undefined
  },
  // 最大高度
  maxHeight: {
    type: [Number, String],
    default: undefined
  },
  // 是否显示选择列
  showSelection: {
    type: Boolean,
    default: false
  },
  // 是否显示序号列
  showIndex: {
    type: Boolean,
    default: false
  },
  // 操作列宽度
  operationWidth: {
    type: Number,
    default: 200
  },
  // 是否显示分页
  pagination: {
    type: Boolean,
    default: true
  },
  // 总条数
  total: {
    type: Number,
    default: 0
  },
  // 当前页码
  currentPage: {
    type: Number,
    default: 1
  },
  // 每页数量
  pageSize: {
    type: Number,
    default: 10
  },
  // 每页数量选项
  pageSizes: {
    type: Array,
    default: () => [10, 20, 50, 100]
  },
  // 加载中
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits([
  'update:currentPage',
  'update:pageSize',
  'selection-change',
  'sort-change',
  'page-change'
])

// 内部状态
const currentPageInternal = computed({
  get: () => props.currentPage,
  set: (val) => emit('update:currentPage', val)
})

const pageSizeInternal = computed({
  get: () => props.pageSize,
  set: (val) => emit('update:pageSize', val)
})

// 方法
const handleSelectionChange = (selection) => {
  emit('selection-change', selection)
}

const handleSortChange = ({ prop, order }) => {
  emit('sort-change', { prop, order })
}

const handleSizeChange = (size) => {
  pageSizeInternal.value = size
  emit('page-change', {
    pageNum: currentPageInternal.value,
    pageSize: size
  })
}

const handleCurrentChange = (page) => {
  currentPageInternal.value = page
  emit('page-change', {
    pageNum: page,
    pageSize: pageSizeInternal.value
  })
}

// 暴露方法给父组件
defineExpose({
  // 可以在这里添加暴露的方法
})
</script>

<style lang="scss" scoped>
.base-table {
  .table-toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .table-pagination {
    display: flex;
    justify-content: flex-end;
  }

  :deep(.el-table) {
    font-size: 14px;

    .el-table__header th {
      background-color: #f5f7fa;
      color: #606266;
      font-weight: 600;
    }
  }
}
</style>
