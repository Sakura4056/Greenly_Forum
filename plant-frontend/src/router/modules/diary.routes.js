/**
 * 植物日记路由模块
 */

const diaryRoutes = [
    {
        path: '/diary',
        name: 'Diary',
        component: () => import('@/layout/index.vue'),
        meta: { title: '植物日记', icon: 'Notebook' },
        redirect: '/diary/list',
        children: [
            {
                path: 'list',
                name: 'DiaryList',
                component: () => import('@/pages/diary/list.vue'),
                meta: { title: '我的日记', hidden: false }
            }
        ]
    }
]

export default diaryRoutes
