/**
 * 社区论坛路由模块
 */

const forumRoutes = [
    {
        path: '/forum',
        name: 'Forum',
        component: () => import('@/layout/index.vue'),
        meta: { title: '社区交流', icon: 'ChatDotRound' },
        redirect: '/forum/list',
        children: [
            {
                path: 'list',
                name: 'ForumList',
                component: () => import('@/pages/forum/index.vue'),
                meta: { title: '帖子列表', hidden: false }
            },
            {
                path: 'detail/:id',
                name: 'ForumDetail',
                component: () => import('@/pages/forum/detail.vue'),
                meta: { title: '帖子详情', hidden: true }
            },
            {
                path: 'publish',
                name: 'ForumPublish',
                component: () => import('@/pages/forum/publish.vue'),
                meta: { title: '发布帖子', hidden: false }
            }
        ]
    }
]

export default forumRoutes
