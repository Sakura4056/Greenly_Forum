<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>系统公告</span>
          <el-button link type="primary" @click="fetchList">刷新</el-button>
        </div>
      </template>

      <div v-if="loading" class="loading-wrapper">
        <el-skeleton :rows="5" animated />
      </div>

      <div v-else-if="list.length === 0" class="empty-wrapper">
        <el-empty description="暂无公告" />
      </div>

      <div v-else class="announcement-list">
        <div
          v-for="item in list"
          :key="item.id"
          class="announcement-item"
          @click="showDetail(item)"
        >
          <div class="announcement-header">
            <el-icon class="announcement-icon" :size="18"><Bell /></el-icon>
            <h4 class="announcement-title">{{ item.title }}</h4>
          </div>
          <p class="announcement-preview">{{ truncate(item.content, 120) }}</p>
          <time class="announcement-time">
            {{ formatTime(item.publishTime) }}
          </time>
        </div>
      </div>

      <div v-if="total > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchList"
          @size-change="fetchList"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="currentItem?.title || '公告详情'"
      width="600px"
    >
      <div class="detail-meta">
        <time>{{ formatTime(currentItem?.publishTime) }}</time>
      </div>
      <div class="detail-content" v-html="currentItem?.content"></div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAnnouncementList } from '@/api/announcement'
import { Bell } from '@element-plus/icons-vue'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const currentItem = ref(null)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList(queryParams)
    if (res) {
      list.value = res.records || []
      total.value = res.total || 0
    }
  } catch (e) {
    console.error('加载公告失败:', e)
  } finally {
    loading.value = false
  }
}

const showDetail = (item) => {
  currentItem.value = item
  dialogVisible.value = true
}

const truncate = (text, maxLen) => {
  if (!text) return ''
  return text.length > maxLen ? text.substring(0, maxLen) + '...' : text
}

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  if (Array.isArray(timeStr)) {
    return timeStr[0] + '-' +
      String(timeStr[1]).padStart(2, '0') + '-' +
      String(timeStr[2]).padStart(2, '0') + ' ' +
      String(timeStr[3]).padStart(2, '0') + ':' +
      String(timeStr[4]).padStart(2, '0')
  }
  return String(timeStr).replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.loading-wrapper,
.empty-wrapper {
  padding: 40px 0;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  background: var(--color-background);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #f0faf4;
    border-color: rgba(46, 204, 113, 0.3);
    transform: translateX(4px);
  }

  .announcement-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;

    .announcement-icon {
      color: var(--color-primary);
      flex-shrink: 0;
    }

    .announcement-title {
      margin: 0;
      font-size: 15px;
      font-weight: 600;
      color: var(--color-text-main);
    }
  }

  .announcement-preview {
    margin: 0 0 8px 26px;
    font-size: 13px;
    color: var(--color-text-secondary);
    line-height: 1.6;
  }

  .announcement-time {
    margin-left: 26px;
    font-size: 12px;
    color: var(--color-text-tertiary);
  }
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-meta {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border);

  time {
    font-size: 14px;
    color: var(--color-text-secondary);
  }
}

.detail-content {
  font-size: 15px;
  line-height: 1.8;
  color: var(--color-text-main);
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
