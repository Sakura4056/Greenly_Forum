import request from './request'

/**
 * 发布公告
 */
export function publishAnnouncement(data) {
    return request({
        url: '/admin/announcements',
        method: 'post',
        data
    })
}

/**
 * 查询公告列表（公开接口）
 */
export function getAnnouncementList(params) {
    return request({
        url: '/admin/announcements/public/list',
        method: 'get',
        params
    })
}
