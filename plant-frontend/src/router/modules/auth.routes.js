/**
 * 认证相关路由（登录、注册）
 * @module router/modules/auth
 */

export default [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/pages/login/index.vue'),
        meta: { 
            hidden: true,
            title: '登录'
        }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/pages/register/index.vue'),
        meta: { 
            hidden: true,
            title: '注册'
        }
    }
]
