<template>
    <div class="app-container">
        <el-card>
            <template #header>
                <div class="card-header">
                    <span>未读消息</span>
                    <el-button link type="primary" @click="fetchAll">刷新</el-button>
                </div>
            </template>

            <el-tabs v-model="activeTab">
                <el-tab-pane label="养护提醒" name="care">
                    <template v-if="careReminders.length > 0">
                        <el-alert v-for="item in careReminders" :key="item.id" :title="item.title"
                            :description="item.content" type="warning" show-icon style="margin-bottom: 10px"
                            @close="markRead(item.id)" />
                    </template>
                    <el-empty v-else description="暂无养护提醒" />
                </el-tab-pane>

                <el-tab-pane label="系统公告" name="announcement">
                    <template v-if="announcementReminders.length > 0">
                        <el-alert v-for="item in announcementReminders" :key="item.id" :title="item.title"
                            :description="item.content" type="info" show-icon style="margin-bottom: 10px"
                            @close="markRead(item.id)" />
                    </template>
                    <el-empty v-else description="暂无系统公告" />
                </el-tab-pane>

                <el-tab-pane label="系统通知" name="system">
                    <template v-if="systemReminders.length > 0">
                        <el-alert v-for="item in systemReminders" :key="item.id" :title="item.title"
                            :description="item.content" type="info" show-icon style="margin-bottom: 10px"
                            @close="markRead(item.id)" />
                    </template>
                    <el-empty v-else description="暂无系统通知" />
                </el-tab-pane>
            </el-tabs>
        </el-card>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getUnread, markRead as markReadAPI } from '@/api/reminder'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const allReminders = ref([])
const activeTab = ref('care')

const careReminders = computed(() => allReminders.value.filter(r => r.scene === 'careSchedule'))
const announcementReminders = computed(() => allReminders.value.filter(r => r.scene === 'announcement'))
const systemReminders = computed(() => allReminders.value.filter(r => r.scene !== 'careSchedule' && r.scene !== 'announcement'))

const fetchAll = async () => {
    try {
        const res = await getUnread(userStore.userId)
        // Backend returns: { totalUnread: X, details: { "careSchedule": [...], "announcement": [...], "plantAudit": [...] } }
        if (res && res.details) {
            let list = []
            // 处理养护提醒
            if (res.details.careSchedule) {
                res.details.careSchedule.forEach(item => { item.scene = 'careSchedule'; list.push(item) })
            }
            // 处理系统公告
            if (res.details.announcement) {
                res.details.announcement.forEach(item => { item.scene = 'announcement'; list.push(item) })
            }
            // 处理其他系统通知（如 plantAudit）
            if (res.details.plantAudit) {
                res.details.plantAudit.forEach(item => { item.scene = 'plantAudit'; list.push(item) })
            }
            allReminders.value = list
        }
    } catch (e) {
        console.error(e)
    }
}

const markRead = async (id) => {
    try {
        await markReadAPI(id)
        // Optimistic remove
        allReminders.value = allReminders.value.filter(i => i.id !== id)
    } catch (e) { }
}

onMounted(() => {
    fetchAll()
})
</script>
