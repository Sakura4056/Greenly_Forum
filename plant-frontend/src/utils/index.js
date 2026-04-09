/**
 * 通用工具函数
 */

/**
 * 格式化日期时间
 * @param {Date|string|number} date - 日期对象、字符串或时间戳
 * @param {string} format - 格式化模板，默认 'YYYY-MM-DD HH:mm:ss'
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
    if (!date) return ''
    
    const d = new Date(date)
    if (isNaN(d.getTime())) return ''
    
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    const hours = String(d.getHours()).padStart(2, '0')
    const minutes = String(d.getMinutes()).padStart(2, '0')
    const seconds = String(d.getSeconds()).padStart(2, '0')
    
    return format
        .replace('YYYY', year)
        .replace('MM', month)
        .replace('DD', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds)
}

/**
 * 格式化相对时间（如：3 分钟前、1 小时前）
 * @param {Date|string|number} date - 日期
 */
export function formatRelativeTime(date) {
    if (!date) return ''
    
    const d = new Date(date)
    const now = new Date()
    const diff = now - d
    
    const seconds = Math.floor(diff / 1000)
    const minutes = Math.floor(seconds / 60)
    const hours = Math.floor(minutes / 60)
    const days = Math.floor(hours / 24)
    const months = Math.floor(days / 30)
    const years = Math.floor(months / 12)
    
    if (years > 0) return `${years}年前`
    if (months > 0) return `${months}个月前`
    if (days > 0) return `${days}天前`
    if (hours > 0) return `${hours}小时前`
    if (minutes > 0) return `${minutes}分钟前`
    if (seconds > 0) return `${seconds}秒前`
    return '刚刚'
}

/**
 * 格式化文件大小
 * @param {number} bytes - 字节数
 */
export function formatFileSize(bytes) {
    if (!bytes) return '0 B'
    
    const units = ['B', 'KB', 'MB', 'GB', 'TB']
    let i = 0
    let size = bytes
    
    while (size >= 1024 && i < units.length - 1) {
        size /= 1024
        i++
    }
    
    return `${size.toFixed(2)} ${units[i]}`
}

/**
 * 格式化数字（添加千分位）
 * @param {number} num - 数字
 */
export function formatNumber(num) {
    if (!num && num !== 0) return '0'
    return String(num).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 防抖函数
 * @param {Function} fn - 需要防抖的函数
 * @param {number} delay - 延迟时间（毫秒）
 */
export function debounce(fn, delay = 300) {
    let timer = null
    return function(...args) {
        if (timer) clearTimeout(timer)
        timer = setTimeout(() => {
            fn.apply(this, args)
        }, delay)
    }
}

/**
 * 节流函数
 * @param {Function} fn - 需要节流的函数
 * @param {number} delay - 延迟时间（毫秒）
 */
export function throttle(fn, delay = 300) {
    let lastTime = 0
    return function(...args) {
        const now = Date.now()
        if (now - lastTime >= delay) {
            fn.apply(this, args)
            lastTime = now
        }
    }
}

/**
 * 深拷贝（简单实现）
 * @param {*} obj - 需要拷贝的对象
 */
export function deepClone(obj) {
    if (obj === null || typeof obj !== 'object') return obj
    return JSON.parse(JSON.stringify(obj))
}

/**
 * 生成 UUID
 */
export function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        const r = Math.random() * 16 | 0
        const v = c === 'x' ? r : (r & 0x3 | 0x8)
        return v.toString(16)
    })
}

/**
 * 获取 URL 参数
 * @param {string} name - 参数名
 */
export function getUrlParam(name) {
    const params = new URLSearchParams(window.location.search)
    return params.get(name)
}

/**
 * 验证邮箱格式
 * @param {string} email - 邮箱地址
 */
export function isValidEmail(email) {
    const reg = /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/
    return reg.test(email)
}

/**
 * 验证手机号格式（中国大陆）
 * @param {string} phone - 手机号
 */
export function isValidPhone(phone) {
    const reg = /^1[3-9]\d{9}$/
    return reg.test(phone)
}

/**
 * 睡眠函数
 * @param {number} ms - 毫秒数
 */
export function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms))
}

/**
 * 下载文件
 * @param {string} url - 文件 URL
 * @param {string} filename - 文件名
 */
export function downloadFile(url, filename) {
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    link.style.display = 'none'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
}

/**
 * 复制到剪贴板
 * @param {string} text - 要复制的文本
 */
export async function copyToClipboard(text) {
    try {
        await navigator.clipboard.writeText(text)
        return true
    } catch (err) {
        // 降级方案
        const textarea = document.createElement('textarea')
        textarea.value = text
        textarea.style.position = 'fixed'
        textarea.style.opacity = '0'
        document.body.appendChild(textarea)
        textarea.select()
        try {
            document.execCommand('copy')
            return true
        } catch {
            return false
        } finally {
            document.body.removeChild(textarea)
        }
    }
}

/**
 * 本地存储封装
 */
export const storage = {
    set(key, value) {
        localStorage.setItem(key, JSON.stringify(value))
    },
    get(key, defaultValue = null) {
        const value = localStorage.getItem(key)
        return value ? JSON.parse(value) : defaultValue
    },
    remove(key) {
        localStorage.removeItem(key)
    },
    clear() {
        localStorage.clear()
    }
}

/**
 * 会话存储封装
 */
export const sessionStorage = {
    set(key, value) {
        window.sessionStorage.setItem(key, JSON.stringify(value))
    },
    get(key, defaultValue = null) {
        const value = window.sessionStorage.getItem(key)
        return value ? JSON.parse(value) : defaultValue
    },
    remove(key) {
        window.sessionStorage.removeItem(key)
    },
    clear() {
        window.sessionStorage.clear()
    }
}

export default {
    formatDate,
    formatRelativeTime,
    formatFileSize,
    formatNumber,
    debounce,
    throttle,
    deepClone,
    generateUUID,
    getUrlParam,
    isValidEmail,
    isValidPhone,
    sleep,
    downloadFile,
    copyToClipboard,
    storage,
    sessionStorage
}
