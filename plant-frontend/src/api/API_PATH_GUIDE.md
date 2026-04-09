# API 路径配置说明

## 🐛 问题：重复的 `/api` 前缀

### 错误现象
```
请求 URL: /api/api/plant/official/1  ❌
实际应该：/api/plant/official/1     ✅
```

### 错误日志
```
responseURL: "http://localhost:5173/api/api/plant/official/1"
data: {code: 500, msg: '系统错误', data: null}
```

---

## 🔍 原因分析

### 1. Vite 代理配置
**文件**: `plant-frontend/vite.config.js`

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8085',  // 后端服务地址
      changeOrigin: true,
      rewrite: (path) => path  // 不重写路径
    }
  }
}
```

**作用**：
- 前端请求 `/api/xxx` → Vite 代理转发到 `http://localhost:8085/api/xxx`
- 自动添加 `/api` 前缀

### 2. 环境变量
**文件**: `plant-frontend/.env.development`

```env
VITE_API_BASE_URL = /api
```

这个配置被 `request.js` 使用作为 `baseURL`。

### 3. Axios 配置
**文件**: `plant-frontend/src/api/request.js`

```javascript
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL, // '/api'
  timeout: 15000
})
```

---

## ✅ 正确的使用方式

### ❌ 错误示例（会导致 /api/api/xxx）
```javascript
// 错误！不要手动添加 /api
await request.get('/api/plant/official/1')
await request.get('/api/my-plant/list')
```

### ✅ 正确示例
```javascript
// 正确！直接使用相对路径
await request.get('/plant/official/1')
await request.get('/my-plant/list')
await request.get('/user/login')
```

---

## 📋 路径拼接流程

### 完整流程
```
1. 前端代码调用
   request.get('/plant/official/1')
   
2. Axios 添加 baseURL
   baseURL: '/api' + url: '/plant/official/1'
   = '/api/plant/official/1'
   
3. Vite 代理拦截
   检测到 /api 前缀 → 转发到 http://localhost:8085
   
4. 后端接收
   http://localhost:8085/api/plant/official/1
```

### 错误流程（重复 /api）
```
1. 前端代码调用（错误）
   request.get('/api/plant/official/1')
   
2. Axios 添加 baseURL
   baseURL: '/api' + url: '/api/plant/official/1'
   = '/api/api/plant/official/1'  ❌
   
3. Vite 代理拦截
   转发到 http://localhost:8085/api/api/plant/official/1
   
4. 后端接收
   路径不存在 → 500 错误
```

---

## 🔧 修复内容

### 修复文件
**`plant-frontend/src/pages/plant/official-detail.vue`**

**修复前**：
```javascript
const res = await request.get(`/api/plant/official/${route.params.id}`)
```

**修复后**：
```javascript
console.log('请求 URL:', `/plant/official/${route.params.id}`)
const res = await request.get(`/plant/official/${route.params.id}`)
```

---

## 🎯 验证方法

### 1. 检查浏览器控制台
打开开发者工具 → Network 标签，应该看到：

**正确的请求**：
```
Request URL: http://localhost:5173/api/plant/official/1
```

**错误的请求**：
```
Request URL: http://localhost:5173/api/api/plant/official/1  ❌
```

### 2. 检查后端日志
后端应该收到正确的请求：
```
GET /api/plant/official/1
```

---

## 📁 相关文件清单

### 配置文件
- **Vite 配置**: `plant-frontend/vite.config.js`
- **环境变量**: `plant-frontend/.env.development`
- **Axios 配置**: `plant-frontend/src/api/request.js`

### 使用文件
- **官方植物详情**: `plant-frontend/src/pages/plant/official-detail.vue` ✅ 已修复
- **官方植物列表**: `plant-frontend/src/pages/plant/official.vue` ✅ 正确
- **我的植物列表**: `plant-frontend/src/pages/plant/my-list.vue` ✅ 正确

---

## 💡 最佳实践

### 1. 统一规范
所有 API 调用都**不要**手动添加 `/api` 前缀：

```javascript
// ✅ 好
await request.get('/user/login')
await request.get('/plant/official/query')

// ❌ 不好
await request.get('/api/user/login')
await request.get('/api/plant/official/query')
```

### 2. 环境变量管理
```env
# .env.development
VITE_API_BASE_URL = /api

# .env.production
VITE_API_BASE_URL = https://api.greenly.com
```

### 3. 调试技巧
在 `request.js` 中添加日志：
```javascript
service.interceptors.request.use(
  config => {
    console.log('=== 请求配置 ===')
    console.log('baseURL:', config.baseURL)
    console.log('url:', config.url)
    console.log('完整路径:', config.baseURL + config.url)
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)
```

---

## 🔗 相关文档

- [Vite Proxy 配置文档](https://vitejs.dev/config/server-options.html#server-proxy)
- [Axios baseURL 文档](https://axios-http.com/docs/req_config)
- [项目 API 路径配置总结](./API_PATH_GUIDE.md)

---

**修复时间**: 2026-04-03  
**影响范围**: 官方植物详情接口调用  
**向后兼容**: ✅ 完全兼容
