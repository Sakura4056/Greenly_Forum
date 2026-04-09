/**
 * 养护管理模块路由
 * @module router/modules/care
 */

export default [
    {
        path: '/care',
        meta: { 
            title: '养护管理', 
            icon: 'Calendar' 
        },
        children: [
            {
                path: 'schedule-list',
                name: 'CareSchedule',
                component: () => import('@/pages/care/schedule-list.vue'),
                meta: { 
                    title: '养护计划',
                    keepAlive: true
                }
            },
            {
                path: 'schedule-add',
                name: 'CareScheduleAdd',
                component: () => import('@/pages/care/schedule-add.vue'),
                meta: { 
                    title: '新建计划', 
                    hidden: true 
                }
            },
            {
                path: 'record-add',
                name: 'CareRecordAdd',
                component: () => import('@/pages/care/record-add.vue'),
                meta: { 
                    title: '添加记录' 
                }
            },
            {
                path: 'record-statistic',
                name: 'CareStatistic',
                component: () => import('@/pages/care/record-statistic.vue'),
                meta: { 
                    title: '养护统计' 
                }
            },
            {
                path: 'calendar',
                name: 'CareCalendar',
                component: () => import('@/pages/care/calendar.vue'),
                meta: { 
                    title: '养护日历' 
                }
            }
        ]
    }
]
