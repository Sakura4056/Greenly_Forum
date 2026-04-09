/**
 * 成长相册模块路由
 * @module router/modules/photo
 */

export default [
    {
        path: '/photo',
        meta: { 
            title: '成长相册', 
            icon: 'Camera' 
        },
        children: [
            {
                path: 'list',
                name: 'PhotoList',
                component: () => import('@/pages/photo/list.vue'),
                meta: { 
                    title: '相册列表',
                    keepAlive: true
                }
            },
            {
                path: 'upload',
                name: 'PhotoUpload',
                component: () => import('@/pages/photo/upload.vue'),
                meta: { 
                    title: '上传照片' 
                }
            }
        ]
    }
]
