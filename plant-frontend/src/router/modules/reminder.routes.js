/**
 * 提醒通知模块路由
 * @module router/modules/reminder
 */

export default [
    {
        path: '/reminder',
        meta: { 
            title: '提醒通知', 
            icon: 'Bell' 
        },
        children: [
            {
                path: 'unread',
                name: 'UnreadReminder',
                component: () => import('@/pages/reminder/unread.vue'),
                meta: { 
                    title: '未读消息' 
                }
            },
            {
                path: 'config',
                name: 'ReminderConfig',
                component: () => import('@/pages/reminder/config.vue'),
                meta: { 
                    title: '提醒配置' 
                }
            }
        ]
    }
]
