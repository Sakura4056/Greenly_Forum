<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>成长相册</span>
          <el-button type="primary" icon="Upload" @click="$router.push('/photo/upload')">上传照片</el-button>
        </div>
      </template>

      <el-timeline>
        <el-timeline-item v-for="(activity, index) in photoList" :key="index" :timestamp="activity.createTime"
          placement="top">
          <el-card>
            <div style="display: flex; justify-content: space-between; align-items: start;">
              <h4>{{ getPlantDisplayName(activity) }}</h4>
              <el-button type="danger" link icon="Delete" @click="handleDelete(activity.id)">删除</el-button>
            </div>
            <p>{{ activity.remarks || '暂无备注' }}</p>
            <el-image style="width: 200px; height: 200px; border-radius: 4px;" :src="getImageUrl(activity.filePath)"
              :preview-src-list="[getImageUrl(activity.filePath)]" fit="cover" />
          </el-card>
        </el-timeline-item>
        <el-timeline-item v-if="photoList.length === 0" timestamp="暂无数据" placement="top">
          <p>还没有上传过照片哦</p>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { queryPhotos, getPhotoViewUrl, deletePhoto } from '@/api/photo'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const photoList = ref([])

/**
 * 获取植物显示名称
 * 优先显示植物名称，如果没有则显示植物ID
 */
const getPlantDisplayName = (photo) => {
  if (!photo) return '未知植物'
  
  // 如果有植物名称，直接显示名称
  if (photo.plantName) {
    return photo.plantName
  }
  
  // 如果没有名称，显示植物ID
  return photo.plantId ? `植物ID: ${photo.plantId}` : '未知植物'
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除这张照片吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deletePhoto(id)
      ElMessage.success('删除成功')
      fetchPhotos()
    } catch (e) { }
  })
}

const getImageUrl = (path) => {
  return getPhotoViewUrl(path)
}

const fetchPhotos = async () => {
  try {
    const res = await queryPhotos({ userId: userStore.userId })
    console.log('照片列表原始数据:', res)
    console.log('照片记录:', res.records || res)
    
    // 检查每条记录的 plantName
    const records = res.records || res
    if (records && records.length > 0) {
      records.forEach((photo, index) => {
        console.log(`照片 ${index + 1}:`, {
          id: photo.id,
          plantId: photo.plantId,
          plantSource: photo.plantSource,
          plantName: photo.plantName,
          remarks: photo.remarks
        })
      })
    }
    
    photoList.value = records
  } catch (e) {
    console.error('获取照片列表失败:', e)
  }
}

onMounted(() => {
  fetchPhotos()
})
</script>
