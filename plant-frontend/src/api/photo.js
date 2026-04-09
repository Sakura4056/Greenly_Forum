import request from './request'

// Upload photo
export function uploadPhoto(formData) {
    return request({
        url: '/photo/upload',
        method: 'post',
        data: formData,  // 直接传递 FormData 对象
        headers: { 
            'Content-Type': 'multipart/form-data' 
        }
    })
}

// Query photos
export function queryPhotos(params) {
    return request({
        url: '/photo/query',
        method: 'get',
        params
    })
}

// Delete photo
export function deletePhoto(id) {
    return request({
        url: `/photo/delete/${id}`,
        method: 'delete'
    })
}

// Get photo view URL (helper, not async request)
export function getPhotoViewUrl(filename) {
    if (!filename) return ''
    // Ensure filename is clean
    const name = filename.replace(/\\/g, '/').split('/').pop()
    // 使用完整 URL
    return `/api/photo/view/${name}`
}

// Get photo as blob (for authenticated access)
export async function getPhotoBlob(filename) {
    if (!filename) return null
    const name = filename.replace(/\\/g, '/').split('/').pop()
    const response = await request({
        url: `/photo/view/${name}`,
        method: 'get',
        responseType: 'blob'
    })
    return response
}
