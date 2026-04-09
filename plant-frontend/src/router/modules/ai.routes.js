/**
 * AI 顾问模块路由
 * @module router/modules/ai
 */

export default [
    {
        path: '/ai',
        redirect: '/ai/chat',
        meta: { 
            title: 'AI 功能', 
            icon: 'ChatDotRound' 
        },
        children: [
            {
                path: 'chat',
                name: 'AIChat',
                component: () => import('@/pages/ai/index.vue'),
                meta: { 
                    title: '智能问答', 
                    icon: 'ChatLineRound' 
                }
            },
            {
                path: 'identification',
                name: 'PlantIdentification',
                component: () => import('@/pages/identification/index.vue'),
                meta: { 
                    title: '图片识别', 
                    icon: 'Picture' 
                }
            }
        ]
    }
]
