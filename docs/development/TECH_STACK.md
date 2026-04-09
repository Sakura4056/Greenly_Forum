# Greenly 技术栈说明

**最后更新**: 2026-04-09

---

## 🔧 后端

### 核心框架

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.3.5 | Web 应用框架 |
| Java | 21 | 编程语言 |
| Maven | 3.6+ | 构建工具 |

### 数据持久层

| 技术 | 版本 | 用途 |
|------|------|------|
| MyBatis Plus | 3.5.5 | ORM（增强 MyBatis） |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.0 | 缓存（可选） |

### 安全与认证

| 技术 | 用途 |
|------|------|
| Spring Security 6.x | 权限控制 |
| JWT (jjwt 0.12.x) | Token 认证 |
| BCrypt | 密码加密 |

### 第三方服务

| 服务 | 用途 |
|------|------|
| 百度 AI (baidu-aip-java-sdk) | 植物图像识别 |
| JavaMailSender (SMTP) | 邮件发送 |
| 和风天气 API | 天气数据 |
| 高德地图 API | 地理位置 |

### 工具类

- Lombok · Hutool · Apache Commons · Jackson · Jakarta Annotation

---

## 🎨 前端

### 核心框架

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue.js | 3.5.24 | 渐进式框架 |
| Vite | 7.2.4 | 构建工具 |
| TypeScript | 5.3+ | 类型支持 |

### UI 与组件

| 技术 | 版本 | 用途 |
|------|------|------|
| Element Plus | 2.13.2 | UI 组件库 |
| @element-plus/icons-vue | 2.x | 图标 |

### 状态管理与路由

| 技术 | 版本 | 用途 |
|------|------|------|
| Pinia | 3.0.4 | 状态管理 |
| Vue Router | 5.0.2 | 路由 |

### HTTP 与工具

| 技术 | 用途 |
|------|------|
| Axios | HTTP 客户端 |
| ECharts | 数据可视化 |
| dayjs | 日期处理 |

### 样式

- SCSS / CSS Variables

---

## 🏗️ 部署环境

| 组件 | 用途 |
|------|------|
| Nginx | 反向代理 + 静态文件 |
| Alibaba Cloud Linux 3 | 服务器操作系统 |
| Let's Encrypt (Certbot) | HTTPS 证书 |

---

## 📊 项目规模

| 类别 | 数量 |
|------|------|
| Controllers | 13 |
| Services | 19 |
| Entities | 15 |
| Mappers | 15 |
| DTOs | 13 |
| 前端页面 | 13 个模块 |
| 数据表 | 15 张 |

