import request from './request'

// Update reminder config
export function updateConfig(data) {
    return request({
        url: '/reminder/config/update',
        method: 'put',
        data
    })
}

// Get reminder config
export function getConfig() {
    return request({
        url: '/reminder/config',
        method: 'get'
    })
}

// Get unread reminders
export function getUnread(userId) {
    return request({
        url: `/reminder/unread/${userId}`,
        method: 'get'
    })
}

// Mark reminder as read
export function markRead(id) {
    return request({
        url: `/reminder/read/${id}`,
        method: 'put'
    })
}

// Get unread count
export function getUnreadCount() {
    return request({
        url: '/reminder/unread-count',
        method: 'get'
    })
}

// Mark all reminders as read
export function markAllAsRead() {
    return request({
        url: '/reminder/read-all',
        method: 'put'
    })
}
