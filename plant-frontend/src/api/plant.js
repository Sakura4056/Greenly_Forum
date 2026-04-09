import request from './request'

// Get official plant list
export function getOfficialPlantList(params) {
    return request({
        url: '/plant/official/query',
        method: 'get',
        params
    })
}

// Get official plant detail
export function getOfficialPlantDetail(id) {
    return request({
        url: `/plant/official/${id}`,
        method: 'get'
    })
}
