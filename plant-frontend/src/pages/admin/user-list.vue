<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>



      <el-table :data="userList" border style="width: 100%" v-loading="loading">
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'info'">{{ scope.row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" />
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-popconfirm title="确认删除该用户吗？" @confirm="handleDelete(scope.row)">
              <template #reference>
                <el-button type="danger" size="small" :disabled="scope.row.role === 'ADMIN'">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-dialog v-model="editDialogVisible" title="修改用户信息" width="30%">
        <el-form :model="editForm" label-width="80px">
          <el-form-item label="用户名">
            <el-input v-model="editForm.username" disabled />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="editForm.role">
              <el-option label="普通用户" value="USER" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="editDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitEdit">保存</el-button>
          </span>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList, deleteUser, updateUserInfo } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const userList = ref([])
const editDialogVisible = ref(false)
const editForm = reactive({
  id: null,
  username: '',
  role: ''
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getUserList({ pageNum: 1, pageSize: 100 }) // Simple fetch all for now
    userList.value = res.records
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleEdit = (row) => {
  editForm.id = row.userId
  editForm.username = row.username
  editForm.role = row.role
  editDialogVisible.value = true
}

const submitEdit = async () => {
  try {
    await updateUserInfo({ userId: editForm.id, role: editForm.role })
    ElMessage.success('修改成功')
    editDialogVisible.value = false
    fetchList()
  } catch (e) { }
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确认删除该用户吗？此操作不可恢复！', '警告', {
    type: 'warning'
  }).then(async () => {
    try {
      await deleteUser(row.userId)
      ElMessage.success('删除成功')
      fetchList()
    } catch (e) { }
  })
}

onMounted(() => {
  fetchList()
})
</script>
