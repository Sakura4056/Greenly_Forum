import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 创建 axios 实例
const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:9090',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求重试配置
const MAX_RETRY_COUNT = 2
const RETRY_DELAY = 1000

// 正在刷新 Token 的标志
let isRefreshingToken = false
// 重试队列
let refreshSubscribers = []

/**
 * 添加到重试队列
 */
function subscribeTokenRefresh(cb) {
    refreshSubscribers.push(cb)
}

/**
 * 执行重试队列
 */
function onRefreshed(token) {
    refreshSubscribers.forEach(cb => cb(token))
    refreshSubscribers = []
}

// Request Interceptor
service.interceptors.request.use(
    config => {
        const userStore = useUserStore()
        const token = userStore.token
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        
        // 添加请求时间戳（可选，用于防止缓存）
        if (config.method === 'get') {
            config.params = {
                ...config.params,
                _t: Date.now()
            }
        }
        
        return config
    },
    error => {
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

// Response Interceptor
service.interceptors.response.use(
    response => {
        const res = response.data
        
        console.log('=== 响应拦截器 ===')
        console.log('完整响应:', response)
        console.log('response.data:', res)
        
        // 处理后端响应的标准 Result 结构
        if (res.code !== undefined) {
            console.log('检测到标准响应格式，code:', res.code)
            
            if (res.code !== 200) {
                // Token 过期处理
                if (res.code === 401) {
                    const userStore = useUserStore()
                    
                    // 如果已经在刷新 Token，加入队列
                    if (isRefreshingToken) {
                        return new Promise((resolve) => {
                            subscribeTokenRefresh((token) => {
                                config.headers['Authorization'] = `Bearer ${token}`
                                resolve(service(config))
                            })
                        })
                    }
                    
                    // 判断是否需要刷新 Token
                    if (userStore.refreshToken) {
                        return handleTokenRefresh(userStore)
                    } else {
                        // 需要重新登录
                        showLoginDialog()
                    }
                }
                
                ElMessage({
                    message: res.msg || res.message || '请求失败',
                    type: 'error',
                    duration: 3000
                })
                
                // 创建包含完整响应数据的错误对象
                const error = new Error(res.msg || res.message || '请求失败')
                error.code = res.code
                error.data = res.data
                error.response = {
                    data: res,
                    status: res.code
                }
                return Promise.reject(error)
            }
            console.log('返回 res.data:', res.data)
            return res.data
        }
        
        // 非标准格式直接返回数据
        console.log('返回 res:', res)
        return res
    },
    error => {
        console.error('请求异常:', error)
        
        let message = error.message || '网络错误'
        
        if (error.response) {
            const status = error.response.status
            message = handleHttpError(status)
            
            // 401 未授权，清除用户信息并跳转登录页
            if (status === 401) {
                const userStore = useUserStore()
                userStore.logout()
                
                // 避免重复跳转
                if (router.currentRoute.value.path !== '/login') {
                    router.push({
                        path: '/login',
                        query: { redirect: router.currentRoute.value.fullPath }
                    })
                }
            }
        } else if (error.code === 'ECONNABORTED') {
            message = '请求超时，请稍后重试'
        } else if (!navigator.onLine) {
            message = '网络连接已断开'
        }
        
        ElMessage({
            message,
            type: 'error',
            duration: 3000,
            showClose: true
        })
        
        return Promise.reject(error)
    }
)

/**
 * 处理 HTTP 状态码错误
 */
function handleHttpError(status) {
    const messages = {
        400: '请求参数错误',
        401: '未登录或登录已过期，请重新登录',
        403: '拒绝访问：您没有权限执行此操作',
        404: '请求的资源不存在',
        408: '请求超时',
        500: '服务器内部错误',
        502: '网关错误',
        503: '服务不可用',
        504: '网关超时'
    }
    return messages[status] || `连接错误 (${status})`
}

/**
 * 处理 Token 刷新
 */
async function handleTokenRefresh(userStore) {
    isRefreshingToken = true
    
    try {
        // 调用刷新 Token 接口
        const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/user/refresh-token`, {
            refreshToken: userStore.refreshToken
        })
        
        if (response.data.code === 200) {
            const { token, refreshToken } = response.data.data
            userStore.updateToken(token, refreshToken)
            
            // 执行重试队列
            onRefreshed(token)
            
            // 重新发送原请求
            return service(userStore.lastRequestConfig)
        }
    } catch (error) {
        console.error('刷新 Token 失败:', error)
        showLoginDialog()
    } finally {
        isRefreshingToken = false
    }
    
    return Promise.reject(error)
}

/**
 * 显示登录对话框
 */
function showLoginDialog() {
    ElMessageBox.confirm(
        '登录已过期，请重新登录',
        '提示',
        {
            confirmButtonText: '去登录',
            cancelButtonText: '取消',
            type: 'warning'
        }
    ).then(() => {
        const userStore = useUserStore()
        userStore.logout()
        router.push({
            path: '/login',
            query: { redirect: router.currentRoute.value.fullPath }
        })
    }).catch(() => {})
}

/**
 * 导出 GET 请求方法
 */
export function get(url, params = {}) {
    return service.get(url, { params })
}

/**
 * 导出 POST 请求方法
 */
export function post(url, data = {}) {
    return service.post(url, data)
}

/**
 * 导出 PUT 请求方法
 */
export function put(url, data = {}) {
    return service.put(url, data)
}

/**
 * 导出 DELETE 请求方法
 */
export function del(url, params = {}) {
    return service.delete(url, { params })
}

export default service
