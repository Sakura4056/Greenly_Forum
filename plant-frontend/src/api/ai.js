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

/**
 * Get identify history list
 */
export function getIdentifyHistory(limit = 20) {
    return request({
        url: '/ai/identify-history',
        method: 'get',
        params: { limit }
    })
}

/**
 * Delete a single identify history record
 */
export function deleteIdentifyHistory(id) {
    return request({
        url: `/ai/identify-history/${id}`,
        method: 'delete'
    })
}

/**
 * Clear all identify history
 */
export function clearIdentifyHistory() {
    return request({
        url: '/ai/identify-history',
        method: 'delete'
    })
}

/**
 * Get AI chat session list
 */
export function getSessionList(limit = 20) {
    return request({
        url: '/ai/sessions',
        method: 'get',
        params: { limit }
    })
}
