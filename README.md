# 🌿 Greenly - 植物养护管理系统

一个现代化的植物养护管理平台，帮助您轻松管理日常植物养护工作。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue.js-3.5-brightgreen.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)

---

## ✨ 特性

- 🌱 **植物管理** - 官方植物库 + 个人植物收藏，跟踪生长状态
- 📅 **养护计划** - 智能养护提醒，不再错过任何一次浇水施肥
- 📸 **成长记录** - 拍照记录植物的成长历程
- 📝 **养护日记** - 分享养护心得和经验
- 💬 **论坛交流** - 发帖、评论、点赞，与花友互动
- 🔔 **智能提醒** - 邮件 + 站内通知，及时提醒养护任务
- 📊 **统计分析** - 养护习惯和成果的可视化统计
- 🤖 **AI 识别** - 基于百度 AI 的植物识别与病虫害诊断
- 📢 **系统公告** - 管理员发布站内公告

---

## 🚀 快速开始

### 前置要求

- Java 21+
- MySQL 8.0+
- Redis 7.0+（可选，用于缓存）
- Node.js 18+
- Maven 3.6+

### 1. 安装数据库

```bash
# 使用项目初始化脚本（推荐）
mysql -u root -p < plant-backend/src/main/resources/db/greenly-init.sql
```

**默认账号**（密码均为 BCrypt 加密存储）：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| user1 | admin123 | 普通用户 |
| user2 | admin123 | 普通用户 |
| gardener | admin123 | 园艺爱好者 |

### 2. 启动后端

```bash
cd plant-backend
cp .env.example .env   # 编辑 .env 填入数据库/Redis/AI 等配置
mvn spring-boot:run
```

访问 http://localhost:9090/actuator 验证

### 3. 启动前端

```bash
cd plant-frontend
npm install
npm run dev
```

访问 http://localhost:5173

---

## 📚 文档

### 入门
- **[数据库快速参考](docs/guides/DATABASE_QUICK_REFERENCE.md)** - 5 分钟上手 ⭐
- **[快速启动指南](QUICK_START.md)** - 完整的本地开发环境搭建
- **[常见问题](docs/troubleshooting/common-issues.md)** - 故障排查

### 开发
- **[技术栈](docs/development/TECH_STACK.md)** - 框架与依赖版本
- **[代码规范](docs/development/CODING_STANDARDS.md)** - Java / Vue / SQL / Git
- **[AI 功能](docs/development/AI_FEATURES_SUMMARY.md)** - 百度 AI 植物识别实现
- **[并发评估](docs/development/CONCURRENCY_ASSESSMENT_REPORT.md)** - 多用户并发能力分析

### 部署
- **[云部署指南](docs/guides/CLOUD_DEPLOYMENT_GUIDE.md)** - 阿里云 / 腾讯云部署 ⭐
- **[部署速查](docs/guides/CLOUD_DEPLOYMENT_QUICK_REF.md)** - 一键部署命令
- **[数据库备份](docs/guides/DATABASE_BACKUP_GUIDE.md)** - 自动备份与恢复
- **[Redis 配置](docs/guides/REDIS_SETUP_GUIDE.md)** - 安装与连接配置
- **[百度 AI 配置](docs/guides/BAIDU_AI_SETUP.md)** - 图像识别 API 配置

---

## 🛠️ 技术栈

### 后端
- Spring Boot 3.3.5 · MyBatis Plus 3.5.5 · Spring Security + JWT · BCrypt · MySQL 8.0 · Redis 7.0

### 前端
- Vue 3.5 · Vite 7.2 · Pinia · Vue Router 5 · Element Plus · Axios · ECharts · SCSS

---

## 📁 项目结构

```
Greenly/
├── plant-backend/         # Spring Boot 后端
├── plant-frontend/        # Vue 3 前端
├── docs/                  # 项目文档
├── scripts/               # 运维脚本
├── data/uploads/          # 用户上传文件
└── README.md
```

详见 **[项目结构说明](PROJECT_STRUCTURE.md)**。

---

## 在线访问

🔗 **https://zhang0903.top**

