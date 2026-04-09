import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Layout from '@/layout/index.vue'
import routes from './modules'

/**
 * Greenly 项目路由配置
 * 
 * @author Greenly Team
 * @date 2026-04-03
 * 
 * 路由模块化说明：
 * - 各功能模块路由定义在 router/modules/ 目录下
 * - 按业务领域拆分，便于维护和协作开发
 * - 主路由文件仅负责组装和全局守卫
 */

// 为需要 Layout 的路由包裹 Layout 组件
const wrappedRoutes = routes.map(route => {
    // 认证路由（登录、注册）不需要 Layout
    if (route.path === '/login' || route.path === '/register') {
        return route
    }
    
    // 其他路由都需要 Layout
    return {
        ...route,
        component: route.component || Layout
    }
})

const router = createRouter({
    history: createWebHistory(),
    routes: wrappedRoutes,
    // 滚动行为
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition
        } else {
            return { top: 0 }
        }
    }
})

/**
 * 全局前置守卫
 * 职责：
 * 1. 检查登录状态
 * 2. 验证角色权限
 * 3. 重定向未授权访问
 */
router.beforeEach((to, from, next) => {
    const userStore = useUserStore()
    const token = userStore.token
    const role = userStore.role

    // 设置页面标题
    if (to.meta.title) {
        document.title = `${to.meta.title} - Greenly`
    }

    // 登录和注册页无需认证
    if (to.path === '/login' || to.path === '/register') {
        // 如果已登录，跳转到首页
        if (token) {
            next('/')
        } else {
            next()
        }
        return
    }

    // 检查登录状态
    if (!token) {
        console.warn('[Router] 未登录，重定向到登录页', to.path)
        next(`/login?redirect=${to.fullPath}`)
        return
    }

    // 检查角色权限
    if (to.meta.roles && to.meta.roles.length > 0) {
        if (!role || !to.meta.roles.includes(role)) {
            console.warn('[Router] 权限不足', {
                path: to.path,
                requiredRoles: to.meta.roles,
                userRole: role
            })
            // 如果有 403 页面则跳转，否则回到首页
            next('/dashboard')
            return
        }
    }

    // 通过验证，允许导航
    next()
})

/**
 * 全局后置钩子
 * 可用于埋点统计、进度条关闭等
 */
router.afterEach((to, from) => {
    // 这里可以添加页面访问统计、进度条关闭等逻辑
    // console.log('[Router] 页面切换', from.path, '->', to.path)
})

export default router
