/**
 * 公告模块路由
 * @module router/modules/announcement
 */

export default [
    {
        path: '/announcement',
        meta: { 
            title: '系统公告', 
            icon: 'Bell' 
        },
        children: [
            {
                path: 'list',
                name: 'AnnouncementList',
                component: () => import('@/pages/announcement/list.vue'),
                meta: { 
                    title: '历史公告' 
                }
            }
        ]
    }
]
