/**
 * 系统管理模块路由（仅管理员）
 * @module router/modules/admin
 */

export default [
    {
        path: '/admin',
        meta: { 
            title: '系统管理', 
            icon: 'Setting', 
            roles: ['ADMIN'] // 仅管理员可访问
        },
        children: [
            {
                path: 'user-list',
                name: 'UserList',
                component: () => import('@/pages/admin/user-list.vue'),
                meta: { 
                    title: '用户管理' 
                }
            },
            {
                path: 'announcement-manage',
                name: 'AnnouncementManage',
                component: () => import('@/pages/admin/announcement-manage.vue'),
                meta: { 
                    title: '公告管理' 
                }
            }
        ]
    }
]
