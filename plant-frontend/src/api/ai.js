import request from './request'

/**
 * AI Chat
 */
export function aiChat(data) {
    return request({
        url: '/ai/chat',
        method: 'post',
        data
    })
}

/**
 * Image Diagnosis
 */
export function diagnoseImage(data) {
    return request({
        url: '/ai/diagnose-image',
        method: 'post',
        data
    })
}

/**
 * Identify Plant (Baidu AI)
 * @param {Object} data - 识别请求数据
 * @param {string} data.image - 图片 Base64 编码（与 imageUrl 二选一）
 * @param {string} data.imageUrl - 图片 URL（与 image 二选一）
 * @returns {Promise} 植物识别结果
 */
export function identifyPlant(data) {
    return request({
        url: '/ai/identify-plant',
        method: 'post',
        data
    })
}

/**
 * Get Conversation History
 */
export function getConversationHistory(sessionId, limit = 50) {
    return request({
        url: '/ai/conversation-history',
        method: 'get',
        params: { sessionId, limit }
    })
}

/**
 * Delete Session
 */
export function deleteSession(sessionId) {
    return request({
        url: `/ai/session/${sessionId}`,
        method: 'delete'
    })
}
