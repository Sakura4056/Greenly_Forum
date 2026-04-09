<template>
  <main class="my-plant-container responsive-container" role="main">
    <!-- 页面头部 -->
    <PageHeader title="我的植物库" subtitle="管理您的植物收藏">
      <template #actions>
        <el-button type="primary" class="add-btn" @click="goAdd" aria-label="添加新植物">
          <el-icon><Plus /></el-icon>
          <span class="d-none d-sm-inline">添加植物</span>
        </el-button>
      </template>
    </PageHeader>

    <!-- 筛选栏 -->
    <FilterBar
      v-model:search-value="queryParams.keyword"
      v-model:status-value="queryParams.status"
      v-model:date-range-value="dateRange"
      search-placeholder="搜索昵称或品种..."
      @search="handleSearch"
      @reset="resetQuery"
      @change="handleSearch"
    />

    <!-- 内容区域 -->
    <section class="content-area" aria-label="植物列表" v-loading="loading">
      <transition-group name="list" tag="div" class="responsive-grid plant-grid">
        <PlantCard
          v-for="plant in plantList"
          :key="plant.id"
          :plant="plant"
          :show-delete="true"
          @click="goDetail"
          @delete="handleDelete"
        />
      </transition-group>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && plantList.length === 0"
        description="没有找到植物呢，马上添加一株吧！"
        class="empty-state"
      />
    </section>

    <!-- 分页 -->
    <nav v-if="total > 0" class="pagination-wrapper" aria-label="分页导航">
      <el-pagination
        v-model:current-page="queryParams.current"
        v-model:page-size="queryParams.size"
        :total="total"
        background
        layout="prev, pager, next"
        @current-change="loadData"
        aria-label="植物列表分页"
      />
    </nav>
  </main>
</template>

<script setup>
/**
 * 我的植物列表页面
 * 展示用户添加的植物，支持搜索、筛选和分页
 */
import { ref, onMounted, watch, onActivated } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { getMyPlantList, deleteMyPlant } from '@/api/my-plant'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/business/PageHeader.vue'
import FilterBar from '@/components/business/FilterBar.vue'
import PlantCard from '@/components/business/PlantCard.vue'

const router = useRouter()
const route = useRoute()

// 响应式状态
const loading = ref(false)
const plantList = ref([])
const total = ref(0)
const dateRange = ref([])

const defaultCover = 'https://images.unsplash.com/photo-1463320726281-696a485928c7?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80'

// 查询参数
const queryParams = ref({
  current: 1,
  size: 12,
  keyword: route.query.keyword || '',
  status: '',
  startDate: '',
  endDate: ''
})

/**
 * 处理图片 URL，将 /uploads/xxx.jpg 转换为完整的后端 URL
 * @param {string} url - 原始图片 URL
 * @returns {string} 处理后的完整 URL
 */
const getImageUrl = (url) => {
  if (!url) return defaultCover
  if (url.startsWith('http')) return url
  if (url.startsWith('/api/photo/view/')) {
    return url
  }
  if (url.startsWith('/uploads/')) {
    const filename = url.replace('/uploads/', '')
    return `/api/photo/view/${filename}`
  }
  return url
}

/**
 * 加载植物列表数据
 */
const loadData = async () => {
  loading.value = true

  try {
    // 处理日期范围
    if (dateRange.value && dateRange.value.length === 2) {
      queryParams.value.startDate = dateRange.value[0]
      queryParams.value.endDate = dateRange.value[1]
    } else {
      queryParams.value.startDate = ''
      queryParams.value.endDate = ''
    }

    const res = await getMyPlantList(queryParams.value)

    if (res && res.records) {
      plantList.value = (res.records || []).map((plant) => ({
        ...plant,
        coverUrl: getImageUrl(plant.coverUrl)
      }))
      total.value = res.total || 0
    } else {
      plantList.value = []
      total.value = 0
    }
  } catch (err) {
    ElMessage.error('加载植物列表失败：' + (err.message || '未知错误'))
    plantList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/**
 * 搜索处理
 */
const handleSearch = () => {
  queryParams.value.current = 1
  loadData()
}

/**
 * 重置筛选条件
 */
const resetQuery = () => {
  queryParams.value.keyword = ''
  queryParams.value.status = ''
  dateRange.value = []
  handleSearch()
}

/**
 * 跳转到添加页面
 */
const goAdd = () => {
  router.push('/plant/my-add')
}

/**
 * 跳转到详情页面
 * @param {number} id - 植物 ID
 */
const goDetail = (id) => {
  router.push(`/plant/my-detail/${id}`)
}

/**
 * 删除植物
 * @param {Object} plant - 植物对象
 */
const handleDelete = async (plant) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${plant.nickname}" 吗？此操作不可恢复！`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await deleteMyPlant(plant.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 生命周期
onMounted(() => {
  loadData()
})

onActivated(() => {
  loadData()
})

// 监听路由参数变化
watch(
  () => route.query.keyword,
  (newVal) => {
    if (newVal !== undefined) {
      queryParams.value.keyword = newVal
      handleSearch()
    }
  }
)
</script>

<style scoped lang="scss">
@import '@/styles/responsive.scss';

.my-plant-container {
  padding-bottom: var(--spacing-xl);
}

.add-btn {
  border-radius: var(--border-radius-large);
  padding: 10px 24px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(var(--color-primary), 0.3);
  transition: all var(--transition-normal);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(var(--color-primary), 0.4);
  }
}

.content-area {
  min-height: 400px;
}

.plant-grid {
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: var(--spacing-lg);
}

.empty-state {
  padding: var(--spacing-xxl) 0;
}

.pagination-wrapper {
  margin-top: var(--spacing-xl);
  display: flex;
  justify-content: center;
}

/* 列表过渡动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.5s ease;
}

.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateY(30px);
}

@include respond-to(mobile) {
  .plant-grid {
    grid-template-columns: 1fr;
  }
}

@include respond-to(sm) {
  .plant-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@include respond-to(lg) {
  .plant-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@include respond-to(xl) {
  .plant-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
</style>
