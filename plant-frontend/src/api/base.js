/**
 * API 基础类
 * 提供通用的 CRUD 操作方法
 * 
 * @param {string} baseUrl - API 基础路径
 */
export function createApi(baseUrl) {
    const request = (await import('./request')).default
    
    return {
        /**
         * 获取列表
         * @param {Object} params - 查询参数
         */
        async list(params = {}) {
            return request.get(`${baseUrl}`, { params })
        },

        /**
         * 获取详情
         * @param {number|string} id - 资源 ID
         */
        async get(id) {
            return request.get(`${baseUrl}/${id}`)
        },

        /**
         * 创建资源
         * @param {Object} data - 创建数据
         */
        async create(data) {
            return request.post(`${baseUrl}`, data)
        },

        /**
         * 更新资源
         * @param {number|string} id - 资源 ID
         * @param {Object} data - 更新数据
         */
        async update(id, data) {
            return request.put(`${baseUrl}/${id}`, data)
        },

        /**
         * 删除资源
         * @param {number|string} id - 资源 ID
         */
        async delete(id) {
            return request.delete(`${baseUrl}/${id}`)
        },

        /**
         * 分页查询
         * @param {Object} params - 分页参数 { pageNum, pageSize, ... }
         */
        async page(params = {}) {
            return request.get(`${baseUrl}/list`, { params })
        }
    }
}

/**
 * 文件上传相关方法
 */
export const fileApi = {
    /**
     * 单文件上传
     * @param {File} file - 文件对象
     * @param {string} dir - 上传目录
     */
    async upload(file, dir = 'uploads') {
        const request = (await import('./request')).default
        const formData = new FormData()
        formData.append('file', file)
        formData.append('dir', dir)
        
        return request.post('/file/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })
    },

    /**
     * 多文件批量上传
     * @param {File[]} files - 文件数组
     * @param {string} dir - 上传目录
     */
    async uploadBatch(files, dir = 'uploads') {
        const request = (await import('./request')).default
        const formData = new FormData()
        files.forEach(file => {
            formData.append('files', file)
        })
        formData.append('dir', dir)
        
        return request.post('/file/upload/batch', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        })
    }
}

/**
 * 通用统计查询
 */
export const statsApi = {
    /**
     * 获取统计数据
     * @param {string} module - 模块名称
     * @param {Object} params - 统计参数
     */
    async getStats(module, params = {}) {
        const request = (await import('./request')).default
        return request.get(`/stats/${module}`, { params })
    }
}

export default {
    createApi,
    fileApi,
    statsApi
}
