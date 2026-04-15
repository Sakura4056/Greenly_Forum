/**
 * 路由模块统一导出
 * @module router/modules
 */

import authRoutes from './auth.routes'
import dashboardRoutes from './dashboard.routes'
import plantRoutes from './plant.routes'
import careRoutes from './care.routes'
import photoRoutes from './photo.routes'
import reminderRoutes from './reminder.routes'
import userRoutes from './user.routes'
import aiRoutes from './ai.routes'
import adminRoutes from './admin.routes'
import diaryRoutes from './diary.routes'
import forumRoutes from './forum.routes'
import announcementRoutes from './announcement.routes'

export {
    announcementRoutes,
    authRoutes,
    dashboardRoutes,
    plantRoutes,
    careRoutes,
    photoRoutes,
    reminderRoutes,
    userRoutes,
    aiRoutes,
    adminRoutes,
    diaryRoutes,
    forumRoutes
}

// 导出所有路由（按顺序合并）
export default [
    ...authRoutes,
    ...dashboardRoutes,
    ...plantRoutes,
    ...careRoutes,
    ...photoRoutes,
    ...reminderRoutes,
    ...userRoutes,
    ...aiRoutes,
    ...diaryRoutes,
    ...forumRoutes,
    ...adminRoutes
]
