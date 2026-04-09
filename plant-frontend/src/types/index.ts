/**
 * 通用类型定义
 */

// API 响应基础结构
export interface ApiResponse<T = any> {
    code: number
    message: string
    data: T
}

// 分页参数
export interface PageParams {
    pageNum: number
    pageSize: number
}

// 分页响应
export interface PageResult<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

// 用户信息
export interface UserInfo {
    userId: number
    username: string
    nickname?: string
    email?: string
    phone?: string
    avatar?: string
    role: string
    createTime?: string
    lastLoginTime?: string
    lastLoginIp?: string
}

// 植物信息
export interface PlantInfo {
    id: number
    plantName: string
    scientificName?: string
    category?: string
    difficulty?: string
    description?: string
    careTips?: string
    imageUrl?: string
}

// 我的植物
export interface MyPlant {
    id: number
    userId: number
    plantId?: number
    plantName: string
    nickname?: string
    acquisitionDate?: string
    location?: string
    status?: number
    healthScore?: number
    createTime: string
    updateTime: string
}

// 养护记录
export interface CareRecord {
    id: number
    myPlantId: number
    userId: number
    careType: string
    careDate: string
    description?: string
    photoIds?: string
    createTime: string
}

// 养护计划
export interface CareSchedule {
    id: number
    userId: number
    plantId?: number
    plantSource?: string
    taskName: string
    dueTime: string
    recurrenceType?: string
    recurrenceInterval?: number
    status: number
    createTime: string
}

// 提醒配置
export interface ReminderConfig {
    id: number
    userId: number
    scene?: string
    sceneConfig?: string
    email?: string
    pushEnabled?: number
    emailEnabled?: number
}

// 照片信息
export interface PlantPhoto {
    id: number
    myPlantId: number
    userId: number
    photoUrl: string
    description?: string
    uploadTime: string
}

// 日记信息
export interface PlantDiary {
    id: number
    myPlantId: number
    userId: number
    diaryDate: string
    content: string
    photoIds?: string
    mood?: string
    createTime: string
}
