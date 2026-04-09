/**
 * 植物管理模块路由
 * @module router/modules/plant
 */

export default [
    {
        path: '/plant',
        meta: { 
            title: '植物管理', 
            icon: 'Plant' 
        },
        children: [
            {
                path: 'my-list',
                name: 'MyPlantList',
                component: () => import('@/pages/plant/my-list.vue'),
                meta: { 
                    title: '我的植物库',
                    keepAlive: true // 缓存列表页
                }
            },
            {
                path: 'my-add',
                name: 'MyPlantAdd',
                component: () => import('@/pages/plant/my-add.vue'),
                meta: { 
                    title: '添加植物', 
                    hidden: true 
                }
            },
            {
                path: 'my-detail/:id',
                name: 'MyPlantDetail',
                component: () => import('@/pages/plant/my-detail.vue'),
                meta: { 
                    title: '植物详情', 
                    hidden: true 
                }
            },
            {
                path: 'official',
                name: 'OfficialPlant',
                component: () => import('@/pages/plant/official.vue'),
                meta: { 
                    title: '官方植物库',
                    keepAlive: true
                }
            },
            {
                path: 'official/:id',
                name: 'OfficialPlantDetail',
                component: () => import('@/pages/plant/official-detail.vue'),
                meta: { 
                    title: '植物详情', 
                    hidden: true 
                }
            }
        ]
    }
]
