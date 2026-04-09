/**
 * 用户相关路由
 * @module router/modules/user
 */

export default [
    {
        path: '/user',
        meta: { 
            hidden: true 
        },
        children: [
            {
                path: 'update',
                name: 'UserUpdate',
                component: () => import('@/pages/user/update.vue'),
                meta: { 
                    title: '个人信息' 
                }
            }
        ]
    }
]
