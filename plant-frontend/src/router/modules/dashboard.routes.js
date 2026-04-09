/**
 * 仪表盘路由
 * @module router/modules/dashboard
 */

export default [
    {
        path: '/',
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/pages/dashboard/index.vue'),
                meta: { 
                    title: '首页', 
                    icon: 'House',
                    affix: true // 固定在标签页
                }
            }
        ]
    }
]
