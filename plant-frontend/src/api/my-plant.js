import request from './request'

// 获取我的植物列表
export function getMyPlantList(params) {
    return request({
        url: '/my-plant',
        method: 'get',
        params
    })
}

// 获取单株植物实体信息
export function getMyPlantById(id) {
    return request({
        url: `/my-plant/${id}`,
        method: 'get'
    })
}

// 聚合获取最近动态及植物详情
export function getMyPlantDetail(id) {
    return request({
        url: `/my-plant/${id}/detail`,
        method: 'get'
    })
}

// 新增我的植物
export function addMyPlant(data) {
    return request({
        url: '/my-plant',
        method: 'post',
        data
    })
}

// 修改我的植物
export function updateMyPlant(id, data) {
    return request({
        url: `/my-plant/${id}`,
        method: 'put',
        data
    })
}

// 逻辑删除
export function deleteMyPlant(id) {
    return request({
        url: `/my-plant/${id}`,
        method: 'delete'
    })
}
