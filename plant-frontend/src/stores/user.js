import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCurrentUserInfo } from '@/api/user'

/**
 * 用户状态管理
 * 
 * @author Greenly Team
 * @date 2026-04-03
 */
export const useUserStore = defineStore('user', () => {
    // 状态 - 从 localStorage 初始化
    const token = ref(localStorage.getItem('token') || '')
    const userId = ref(Number(localStorage.getItem('userId')) || null)
    const username = ref(localStorage.getItem('username') || '')
    const nickname = ref(localStorage.getItem('nickname') || '')
    const role = ref(localStorage.getItem('role') || '')
    const avatar = ref(localStorage.getItem('avatar') || '')
    const email = ref(localStorage.getItem('email') || '')
    const phone = ref(localStorage.getItem('phone') || '')
    
    // 计算属性
    const isLoggedIn = computed(() => !!token.value && token.value !== '')
    const isAdmin = computed(() => role.value === 'ADMIN')
    const userInfo = computed(() => ({
        userId: userId.value,
        username: username.value,
        nickname: nickname.value,
        role: role.value,
        avatar: avatar.value,
        email: email.value,
        phone: phone.value
    }))

    /**
     * 设置登录信息
     * @param {Object} info - 登录响应数据
     */
    function setLoginInfo(info) {
        if (!info) {
            console.error('[UserStore] setLoginInfo: 登录响应数据为空')
            throw new Error('登录响应数据为空')
        }
        
        // 验证必要字段
        if (!info.token || !info.userId) {
            console.error('[UserStore] setLoginInfo: 缺少必要的登录字段', info)
            throw new Error('登录数据不完整')
        }
        
        // 更新状态
        token.value = info.token
        userId.value = Number(info.userId)
        username.value = info.username || ''
        nickname.value = info.nickname || info.username || ''
        role.value = info.role || 'USER'
        
        console.log('[UserStore] 登录成功', {
            userId: userId.value,
            username: username.value,
            role: role.value
        })
        
        // 持久化存储
        _persistToStorage()
    }

    /**
     * 获取并更新当前用户信息
     * @returns {Promise<Object>} 用户信息
     */
    async function fetchUserInfo() {
        if (!token.value) {
            console.warn('[UserStore] fetchUserInfo: 未登录，无法获取用户信息')
            throw new Error('未登录')
        }
        
        try {
            const res = await getCurrentUserInfo()
            // 拦截器已经返回 res.data，所以 res 就是用户信息对象
            const user = res
            
            // 更新状态
            userId.value = Number(user.userId)
            username.value = user.username
            nickname.value = user.nickname || user.username
            role.value = user.role
            avatar.value = user.avatar || ''
            email.value = user.email || ''
            phone.value = user.phone || ''
            
            // 同步到本地存储
            _persistToStorage()
            
            console.log('[UserStore] 用户信息已更新', userInfo.value)
            return user
        } catch (error) {
            console.error('[UserStore] 获取用户信息失败:', error)
            // 如果是 401 错误，自动退出登录
            if (error.response?.status === 401) {
                console.warn('[UserStore] Token 已失效，自动退出登录')
                logout()
            }
            throw error
        }
    }

    /**
     * 更新用户信息（局部更新）
     * @param {Object} info - 用户信息
     */
    function updateUserInfo(info) {
        if (!info) return
        
        let updated = false
        
        if (info.nickname !== undefined) {
            nickname.value = info.nickname
            updated = true
        }
        if (info.avatar !== undefined) {
            avatar.value = info.avatar
            updated = true
        }
        if (info.email !== undefined) {
            email.value = info.email
            updated = true
        }
        if (info.phone !== undefined) {
            phone.value = info.phone
            updated = true
        }
        
        // 如果有更新，同步到本地存储
        if (updated) {
            _persistToStorage()
            console.log('[UserStore] 用户信息已局部更新')
        }
    }

    /**
     * 退出登录
     */
    function logout() {
        console.log('[UserStore] 退出登录', {
            userId: userId.value,
            username: username.value
        })
        
        // 清空状态
        token.value = ''
        userId.value = null
        username.value = ''
        nickname.value = ''
        role.value = ''
        avatar.value = ''
        email.value = ''
        phone.value = ''
        
        // 清除本地存储
        _clearFromStorage()
        
        // 可选：跳转到登录页
        // window.location.href = '/login'
    }

    /**
     * 检查是否需要刷新用户信息
     * @returns {boolean} 是否需要刷新
     */
    function shouldRefreshUserInfo() {
        // 如果没有 token，不需要刷新
        if (!token.value) return false
        
        // 如果没有 userId，需要刷新
        if (!userId.value) return true
        
        // 如果没有 nickname（说明信息不完整），需要刷新
        if (!nickname.value) return true
        
        return false
    }

    /**
     * 将当前状态持久化到 localStorage
     * @private
     */
    function _persistToStorage() {
        if (token.value) localStorage.setItem('token', token.value)
        if (userId.value) localStorage.setItem('userId', String(userId.value))
        if (username.value) localStorage.setItem('username', username.value)
        if (nickname.value) localStorage.setItem('nickname', nickname.value)
        if (role.value) localStorage.setItem('role', role.value)
        if (avatar.value) localStorage.setItem('avatar', avatar.value)
        if (email.value) localStorage.setItem('email', email.value)
        if (phone.value) localStorage.setItem('phone', phone.value)
    }

    /**
     * 从 localStorage 清除所有用户数据
     * @private
     */
    function _clearFromStorage() {
        localStorage.removeItem('token')
        localStorage.removeItem('userId')
        localStorage.removeItem('username')
        localStorage.removeItem('nickname')
        localStorage.removeItem('role')
        localStorage.removeItem('avatar')
        localStorage.removeItem('email')
        localStorage.removeItem('phone')
    }

    return {
        // 状态
        token,
        userId,
        username,
        nickname,
        role,
        avatar,
        email,
        phone,
        // 计算属性
        isLoggedIn,
        isAdmin,
        userInfo,
        // 方法
        setLoginInfo,
        fetchUserInfo,
        updateUserInfo,
        logout,
        shouldRefreshUserInfo
    }
}, {
    // Pinia 配置
    persist: false // 我们手动管理持久化
})
