<template>
    <div class="watering-notification">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <el-button circle @click="togglePanel">
                <el-icon><Bell /></el-icon>
            </el-button>
        </el-badge>

        <!-- 通知面板 -->
        <transition name="slide-fade">
            <div v-if="panelVisible" class="notification-panel">
                <div class="panel-header">
                    <h3>浇水提醒</h3>
                    <el-button text @click="markAllAsRead">全部已读</el-button>
                </div>

                <div class="panel-body">
                    <el-skeleton v-if="loading" :rows="3" animated />

                    <el-empty v-else-if="reminders.length === 0" description="暂无浇水提醒" />

                    <div v-else class="reminder-list">
                        <div
                            v-for="reminder in reminders"
                            :key="reminder.myPlantId"
                            class="reminder-item"
                            :class="{ urgent: reminder.urgency === 'urgent' }"
                        >
                            <div class="reminder-icon">
                                <el-icon v-if="reminder.urgency === 'urgent'" color="#F44336"><Warning /></el-icon>
                                <el-icon v-else-if="reminder.urgency === 'today'" color="#FF9800"><Opportunity /></el-icon>
                                <el-icon v-else color="#4CAF50"><Bell /></el-icon>
                            </div>
                            <div class="reminder-content">
                                <div class="plant-name">{{ reminder.plantName }}</div>
                                <div class="reminder-text">
                                    <span v-if="reminder.daysUntilNextWater < 0">
                                        已逾期 {{ Math.abs(reminder.daysUntilNextWater) }} 天
                                    </span>
                                    <span v-else-if="reminder.daysUntilNextWater === 0">
                                        今天需要浇水
                                    </span>
                                    <span v-else>
                                        {{ reminder.daysUntilNextWater }} 天后需要浇水
                                    </span>
                                </div>
                                <div class="weather-tip" v-if="reminder.weatherAdvice">
                                    <el-icon><Cloudy /></el-icon>
                                    {{ getWeatherTip(reminder.weatherAdvice) }}
                                </div>
                            </div>
                            <el-button
                                size="small"
                                type="primary"
                                @click="goToPlant(reminder.myPlantId)"
                            >
                                查看
                            </el-button>
                        </div>
                    </div>
                </div>

                <div class="panel-footer">
                    <el-button type="primary" link @click="refreshReminders">
                        <el-icon><Refresh /></el-icon>
                        刷新
                    </el-button>
                </div>
            </div>
        </transition>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { getWateringReminders } from '@/api/care'
import { ElNotification } from 'element-plus'
import { Bell, Warning, Opportunity, Cloudy, Refresh } from '@element-plus/icons-vue'

const router = useRouter()
const panelVisible = ref(false)
const loading = ref(false)
const reminders = ref([])
let pollTimer = null

// 未读数量
const unreadCount = computed(() => {
    return reminders.value.filter(r => r.daysUntilNextWater <= 0).length
})

// 切换面板
const togglePanel = () => {
    panelVisible.value = !panelVisible.value
    if (panelVisible.value) {
        loadReminders()
    }
}

// 加载提醒列表
const loadReminders = async () => {
    loading.value = true
    try {
        const res = await getWateringReminders()
        if (res.code === 200) {
            reminders.value = res.data || []

            // 如果有紧急提醒，显示桌面通知
            showUrgentNotifications(reminders.value)
        }
    } catch (error) {
        console.error('加载浇水提醒失败:', error)
    } finally {
        loading.value = false
    }
}

// 显示紧急通知
const showUrgentNotifications = (list) => {
    const urgent = list.filter(r => r.daysUntilNextWater <= 0)
    if (urgent.length > 0) {
        ElNotification({
            title: '浇水提醒',
            message: `您有 ${urgent.length} 株植物需要浇水`,
            type: 'warning',
            duration: 5000
        })
    }
}

// 获取天气提示文本
const getWeatherTip = (weatherAdvice) => {
    if (!weatherAdvice.shouldWater) {
        const reasons = weatherAdvice.reasons || []
        return reasons[0] || '今日不适合浇水'
    }
    return '天气适宜浇水'
}

// 标记全部已读（简化：刷新列表）
const markAllAsRead = () => {
    loadReminders()
}

// 刷新提醒
const refreshReminders = () => {
    loadReminders()
}

// 跳转到植物详情
const goToPlant = (myPlantId) => {
    router.push(`/plant/my-detail/${myPlantId}`)
    panelVisible.value = false
}

// 启动轮询（每30分钟检查一次）
const startPolling = () => {
    pollTimer = setInterval(() => {
        loadReminders()
    }, 30 * 60 * 1000) // 30分钟
}

// 停止轮询
const stopPolling = () => {
    if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
    }
}

// 组件挂载
onMounted(() => {
    loadReminders()
    startPolling()
})

// 组件卸载
onUnmounted(() => {
    stopPolling()
})
</script>

<style scoped lang="scss">
.watering-notification {
    position: relative;

    .notification-badge {
        :deep(.el-badge__content) {
            background-color: #F44336;
        }
    }

    .notification-panel {
        position: absolute;
        top: 50px;
        right: 0;
        width: 360px;
        max-height: 500px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        z-index: 1000;
        display: flex;
        flex-direction: column;

        .panel-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 15px 20px;
            border-bottom: 1px solid #EBEEF5;

            h3 {
                margin: 0;
                font-size: 16px;
                color: #303133;
            }
        }

        .panel-body {
            flex: 1;
            overflow-y: auto;
            padding: 10px 0;

            .reminder-list {
                .reminder-item {
                    display: flex;
                    align-items: flex-start;
                    gap: 12px;
                    padding: 12px 20px;
                    transition: background-color 0.3s;

                    &:hover {
                        background-color: #F5F7FA;
                    }

                    &.urgent {
                        background-color: #FFF3E0;
                        border-left: 3px solid #F44336;
                    }

                    .reminder-icon {
                        flex-shrink: 0;
                        width: 36px;
                        height: 36px;
                        border-radius: 50%;
                        background: #F5F7FA;
                        display: flex;
                        align-items: center;
                        justify-content: center;

                        .el-icon {
                            font-size: 20px;
                        }
                    }

                    .reminder-content {
                        flex: 1;
                        min-width: 0;

                        .plant-name {
                            font-weight: bold;
                            color: #303133;
                            margin-bottom: 4px;
                        }

                        .reminder-text {
                            font-size: 13px;
                            color: #606266;
                            margin-bottom: 6px;
                        }

                        .weather-tip {
                            display: flex;
                            align-items: center;
                            gap: 4px;
                            font-size: 12px;
                            color: #909399;

                            .el-icon {
                                font-size: 14px;
                            }
                        }
                    }
                }
            }
        }

        .panel-footer {
            padding: 12px 20px;
            border-top: 1px solid #EBEEF5;
            text-align: center;
        }
    }
}

// 过渡动画
.slide-fade-enter-active,
.slide-fade-leave-active {
    transition: all 0.3s ease;
}

.slide-fade-enter-from,
.slide-fade-leave-to {
    transform: translateY(-10px);
    opacity: 0;
}
</style>
