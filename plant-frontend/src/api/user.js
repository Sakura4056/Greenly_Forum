import request from './request'

/**
 * 用户注册
 * @param {Object} data - 注册信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 * @param {string} data.nickname - 昵称
 * @param {string} data.email - 邮箱
 * @param {string} data.phone - 手机号
 */
export function register(data) {
    return request({
        url: '/user/register',
        method: 'post',
        data
    })
}

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {string} data.username - 用户名
 * @param {string} data.password - 密码
 */
export function login(data) {
    return request({
        url: '/user/login',
        method: 'post',
        data
    })
}

/**
 * 获取当前登录用户信息
 */
export function getCurrentUserInfo() {
    return request({
        url: '/user/info',
        method: 'get'
    })
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息
 * @param {number} data.userId - 用户 ID
 * @param {string} data.nickname - 昵称
 * @param {string} data.email - 邮箱
 * @param {string} data.phone - 手机号
 * @param {number} data.gender - 性别
 * @param {string} data.birthday - 生日
 * @param {string} data.signature - 个性签名
 * @param {string} data.avatar - 头像 URL
 */
export function updateUserInfo(data) {
    return request({
        url: '/user/update',
        method: 'put',
        data
    })
}

/**
 * 修改密码
 * @param {Object} data - 密码信息
 * @param {string} data.oldPassword - 原密码
 * @param {string} data.newPassword - 新密码
 */
export function changePassword(data) {
    return request({
        url: '/user/change-password',
        method: 'put',
        data
    })
}

/**
 * 删除用户（管理员）
 * @param {number} id - 用户 ID
 */
export function deleteUser(id) {
    return request({
        url: `/user/delete/${id}`,
        method: 'delete'
    })
}

/**
 * 获取用户列表（管理员）
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.keyword - 搜索关键词
 */
export function getUserList(params) {
    return request({
        url: '/user/list',
        method: 'get',
        params
    })
}

/**
 * 获取用户详情（管理员）
 * @param {number} id - 用户 ID
 */
export function getUserDetail(id) {
    return request({
        url: `/user/${id}`,
        method: 'get'
    })
}

/**
 * 发送重置密码验证码
 * @param {Object} data
 * @param {string} data.email - 邮箱地址
 */
export function sendResetCode(data) {
    return request({
        url: '/user/send-reset-code',
        method: 'post',
        data
    })
}

/**
 * 通过邮箱验证码重置密码
 * @param {Object} data
 * @param {string} data.email - 邮箱地址
 * @param {string} data.code - 验证码
 * @param {string} data.newPassword - 新密码
 */
export function resetPassword(data) {
    return request({
        url: '/user/reset-password',
        method: 'post',
        data
    })
}

/**
 * 发送邮箱绑定验证码
 * @param {Object} data
 * @param {string} data.email - 邮箱地址
 */
export function sendBindEmailCode(data) {
    return request({
        url: '/user/send-bind-email-code',
        method: 'post',
        data
    })
}

/**
 * 绑定邮箱（需验证码）
 * @param {Object} data
 * @param {string} data.email - 邮箱地址
 * @param {string} data.code - 验证码
 */
export function bindEmail(data) {
    return request({
        url: '/user/bind-email',
        method: 'post',
        data
    })
}
