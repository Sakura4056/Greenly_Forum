<template>
  <div class="global-search">
    <el-autocomplete
      v-model="keyword"
      :fetch-suggestions="querySearchAsync"
      placeholder="搜索我的植物、计划或记录..."
      @select="handleSelect"
      @keyup.enter="handleEnter"
      clearable
      class="search-input"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
      <template #default="{ item }">
        <div class="suggestion-item">
          <span class="type-tag" :class="item.type">{{ getTypeName(item.type) }}</span>
          <span class="content" v-html="highlightKeyword(item.value)"></span>
        </div>
      </template>
    </el-autocomplete>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getMyPlantList } from '@/api/my-plant'
import { getScheduleList, getRecordList } from '@/api/care'

const router = useRouter()
const route = useRoute()
const keyword = ref('')

// 模拟或调用真实聚合搜索接口
const querySearchAsync = async (queryString, cb) => {
  if (!queryString) {
    cb([])
    return
  }

  try {
    const results = []
    
    // 并行查询三个模块，为了体验可设定较小的 size
    const [plantRes, scheduleRes, recordRes] = await Promise.all([
      getMyPlantList({ keyword: queryString, current: 1, size: 3 }),
      getScheduleList({ keyword: queryString, pageNum: 1, pageSize: 2 }),
      getRecordList({ keyword: queryString, pageNum: 1, pageSize: 2 })
    ])

    if (plantRes?.data?.records) {
      plantRes.data.records.forEach(p => {
        results.push({ value: p.nickname, type: 'plant', id: p.id })
      })
    }

    if (scheduleRes?.data?.records) {
      scheduleRes.data.records.forEach(s => {
        results.push({ value: s.taskName, type: 'schedule', id: s.id })
      })
    }

    if (recordRes?.data?.records) {
      recordRes.data.records.forEach(r => {
        // Find a suitable display string for the record. Remarks is usually used for keyword
        let text = r.remarks || '养护记录'
        if (text.length > 20) text = text.substring(0, 20) + '...'
        results.push({ value: text, type: 'record', id: r.id })
      })
    }

    cb(results)
  } catch (err) {
    console.error('搜索失败', err)
    cb([])
  }
}

const getTypeName = (type) => {
  const map = {
    plant: '植物',
    schedule: '计划',
    record: '记录'
  }
  return map[type] || '其他'
}

const highlightKeyword = (text) => {
  if (!keyword.value) return text
  const reg = new RegExp(keyword.value, 'gi')
  return text.replace(reg, `<span style="color: var(--el-color-primary)">${keyword.value}</span>`)
}

const handleSelect = (item) => {
  if (item.type === 'plant') {
    router.push(`/plant/my-detail/${item.id}`)
  } else if (item.type === 'schedule') {
    router.push({ path: '/care/schedule', query: { keyword: item.value } })
  } else if (item.type === 'record') {
    router.push({ path: '/care/record', query: { keyword: item.value } })
  }
}

const handleEnter = () => {
    // Determine the target module based on current route or default to plant
    let targetPath = '/plant/my-list'
    if (route.path.includes('/care/schedule')) targetPath = '/care/schedule'
    if (route.path.includes('/care/record')) targetPath = '/care/record'
    
    router.push({ path: targetPath, query: { keyword: keyword.value } })
}
</script>

<style scoped>
.global-search {
  width: 300px;
}
.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  background-color: var(--el-fill-color-light);
  box-shadow: none;
}
.search-input :deep(.el-input__wrapper:hover),
.search-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset;
  background-color: var(--el-bg-color);
}
.suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.type-tag {
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
  color: white;
}
.type-tag.plant { background-color: var(--el-color-success); }
.type-tag.schedule { background-color: var(--el-color-warning); }
.type-tag.record { background-color: var(--el-color-primary); }
</style>
