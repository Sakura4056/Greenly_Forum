import request from './request'

/**
 * Create diary entry
 */
export function createDiary(data) {
    return request({
        url: '/diary',
        method: 'post',
        data
    })
}

/**
 * Update diary entry
 */
export function updateDiary(data) {
    return request({
        url: '/diary',
        method: 'put',
        data
    })
}

/**
 * Delete diary entry
 */
export function deleteDiary(id) {
    return request({
        url: `/diary/${id}`,
        method: 'delete'
    })
}

/**
 * Get diary by ID
 */
export function getDiaryById(id) {
    return request({
        url: `/diary/${id}`,
        method: 'get'
    })
}

/**
 * Query diaries with pagination
 */
export function queryDiaries(params) {
    return request({
        url: '/diary/query',
        method: 'get',
        params
    })
}

/**
 * Get diaries by plant ID
 */
export function getDiariesByPlant(plantId) {
    return request({
        url: `/diary/plant/${plantId}`,
        method: 'get'
    })
}

/**
 * Get mood statistics
 */
export function getMoodStatistics(params) {
    return request({
        url: '/diary/mood-stats',
        method: 'get',
        params
    })
}
