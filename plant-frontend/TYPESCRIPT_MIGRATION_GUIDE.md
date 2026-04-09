# TypeScript 迁移指南

本文档说明如何将 Greenly 前端项目从 JavaScript 渐进式迁移到 TypeScript。

---

## 📋 目录

- [已完成配置](#已完成配置)
- [安装依赖](#安装依赖)
- [迁移步骤](#迁移步骤)
- [常见问题](#常见问题)

---

## ✅ 已完成配置

### 1. 已创建的文件

- `tsconfig.json` - TypeScript 编译器配置
- `tsconfig.node.json` - Node 环境配置（用于 Vite 配置）
- `src/types/index.ts` - 通用类型定义

### 2. tsconfig.json 说明

```json
{
  "compilerOptions": {
    "strict": false,           // 初始设为 false，允许 JS/TS 共存
    "noEmit": true,            // 不输出文件，由 Vite 处理
    "paths": { "@/*": ["src/*"] }  // 路径别名
  }
}
```

---

## 📦 安装依赖

在项目根目录执行：

```bash
cd plant-frontend

# 安装 TypeScript 和相关类型定义
npm install --save-dev typescript vue-tsc @types/node

# 如果使用 Element Plus，安装其类型定义
npm install --save-dev @element-plus/icons-vue

# 验证安装
npx tsc --version
```

---

## 🚀 迁移步骤

### Phase 1: API 模块迁移（优先级：高）

API 模块是迁移的最佳起点，因为它们逻辑简单、依赖少。

#### 步骤 1: 重命名文件

```bash
cd plant-frontend/src/api

# 重命名示例
mv request.js request.ts
mv user.js user.ts
mv plant.js plant.ts
# ... 其他模块
```

#### 步骤 2: 添加类型注解

**迁移前 (request.js)**:
```javascript
import axios from 'axios'

const service = axios.create({
    baseURL: '/api',
    timeout: 15000
})

export default service
```

**迁移后 (request.ts)**:
```typescript
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import type { ApiResponse } from '@/types'

const service: AxiosInstance = axios.create({
    baseURL: '/api',
    timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
    (response: AxiosResponse<ApiResponse>) => {
        const res = response.data
        if (res.code !== 200) {
            return Promise.reject(new Error(res.message || 'Error'))
        }
        return res
    },
    (error) => Promise.reject(error)
)

export default service
```

#### 步骤 3: 为每个 API 模块添加类型

**user.ts**:
```typescript
import request from './request'
import type { UserInfo, PageResult } from '@/types'

export interface LoginParams {
    username: string
    password: string
}

export interface LoginResponse {
    userId: number
    username: string
    token: string
    role: string
}

export interface RegisterParams {
    username: string
    password: string
    nickname?: string
    email?: string
    phone?: string
}

// API 函数
export function login(data: LoginParams) {
    return request<any, LoginResponse>({
        url: '/user/login',
        method: 'post',
        data
    })
}

export function register(data: RegisterParams) {
    return request<any, LoginResponse>({
        url: '/user/register',
        method: 'post',
        data
    })
}

export function getUserInfo() {
    return request<any, UserInfo>({
        url: '/user/info',
        method: 'get'
    })
}
```

---

### Phase 2: Pinia Stores 迁移（优先级：高）

#### 迁移 user store

**迁移前 (stores/user.js)**:
```javascript
import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        userInfo: null
    }),
    actions: {
        async login(loginForm) {
            const res = await login(loginForm)
            this.token = res.token
            localStorage.setItem('token', res.token)
        }
    }
})
```

**迁移后 (stores/user.ts)**:
```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/user'
import type { UserInfo, LoginParams } from '@/types'

export const useUserStore = defineStore('user', () => {
    // State
    const token = ref<string>(localStorage.getItem('token') || '')
    const userInfo = ref<UserInfo | null>(null)

    // Getters
    const isLoggedIn = computed(() => !!token.value)
    const userRole = computed(() => userInfo.value?.role || '')

    // Actions
    async function loginAction(loginForm: LoginParams) {
        const res = await login(loginForm)
        token.value = res.token
        userInfo.value = res
        localStorage.setItem('token', res.token)
    }

    async function logoutAction() {
        await logout()
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
    }

    async function fetchUserInfo() {
        const res = await getUserInfo()
        userInfo.value = res
    }

    return {
        token,
        userInfo,
        isLoggedIn,
        userRole,
        login: loginAction,
        logout: logoutAction,
        fetchUserInfo
    }
})
```

---

### Phase 3: Router 迁移（优先级：中）

**迁移前 (router/modules/auth.routes.js)**:
```javascript
export default [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/pages/login/index.vue'),
        meta: { title: '登录' }
    }
]
```

**迁移后 (router/modules/auth.routes.ts)**:
```typescript
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/pages/login/index.vue'),
        meta: { title: '登录' }
    }
]

export default routes
```

---

### Phase 4: Vue 组件迁移（优先级：低，逐步进行）

使用 `<script setup lang="ts">` 语法：

**迁移前**:
```vue
<script setup>
import { ref, onMounted } from 'vue'
import { getPlantList } from '@/api/plant'

const plants = ref([])
const loading = ref(false)

const loadPlants = async () => {
    loading.value = true
    try {
        const res = await getPlantList()
        plants.value = res.data
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    loadPlants()
})
</script>
```

**迁移后**:
```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPlantList } from '@/api/plant'
import type { PlantInfo } from '@/types'

const plants = ref<PlantInfo[]>([])
const loading = ref<boolean>(false)

const loadPlants = async (): Promise<void> => {
    loading.value = true
    try {
        const res = await getPlantList()
        plants.value = res.data
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    loadPlants()
})
</script>
```

---

## 🔧 VS Code 配置

创建 `.vscode/settings.json`:

```json
{
    "typescript.tsdk": "node_modules/typescript/lib",
    "editor.formatOnSave": true,
    "[typescript]": {
        "editor.defaultFormatter": "esbenp.prettier-vscode"
    },
    "[vue]": {
        "editor.defaultFormatter": "esbenp.prettier-vscode"
    }
}
```

---

## ❓ 常见问题

### Q1: 如何处理第三方库缺少类型定义？

**方案 1**: 安装社区维护的类型包
```bash
npm install --save-dev @types/lodash
```

**方案 2**: 创建声明文件 `src/types/shims.d.ts`
```typescript
declare module '*.vue' {
    import type { DefineComponent } from 'vue'
    const component: DefineComponent<{}, {}, any>
    export default component
}

declare module 'some-untyped-lib' {
    const lib: any
    export default lib
}
```

### Q2: 如何处理渐进式迁移中的 JS/TS 混用？

tsconfig.json 中设置 `"strict": false`，允许 JS 文件不遵循严格类型检查。随着迁移进度逐步提高严格程度。

### Q3: 如何验证类型是否正确？

```bash
# 检查类型错误（不生成文件）
npx vue-tsc --noEmit

# 或者
npx tsc --noEmit
```

---

## 📊 迁移进度追踪

| 模块 | 状态 | 备注 |
|------|------|------|
| src/types | ✅ 完成 | 基础类型定义已创建 |
| src/api | 🔄 进行中 | 需要逐个迁移 |
| src/stores | ⏳ 待开始 | 建议第二个迁移 |
| src/router | ⏳ 待开始 | 相对简单 |
| src/pages | ⏳ 待开始 | 工作量最大，逐步迁移 |
| src/components | ⏳ 待开始 | 逐步迁移 |

---

*最后更新：2026-04-05*
