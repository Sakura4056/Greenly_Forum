# 🌿 Greenly — 植物养护管理系统

> 一个帮助你轻松管理日常植物养护的全栈 Web 应用。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue.js-3.5-brightgreen.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-red.svg)](https://redis.io/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

🔗 **在线体验**: <https://zhang0903.top>

---

## 功能模块

### 🌱 植物管理
- **官方植物库** — 浏览系统收录的植物百科信息
- **我的植物** — 收录个人养护的植物，记录来源、位置和状态
- **植物照片** — 为每盆植物拍摄成长照片，建立图册

### 📅 养护管理
- **养护计划** — 为植物制定浇水、施肥、换盆等周期计划
- **养护记录** — 记录每次养护操作（完成、跳过、备注）
- **养护统计** — 可视化展示养护频率、完成率等数据

### 💬 社区论坛
- **帖子发布** — 支持图文帖，按分类浏览
- **评论互动** — 回复帖子、与其他花友交流
- **点赞收藏** — 为喜欢的帖子点赞

### 🤖 AI 智能
- **AI 问答** — 基于百度 AI 的植物养护智能对话
- **图像识别** — 拍照识别植物种类（2 万+ 植物）

### 🔔 提醒与通知
- **提醒配置** — 按养护计划设置邮件提醒
- **站内通知** — 系统消息实时推送
- **系统公告** — 管理员发布公告

### 👤 用户系统
- 注册 / 登录（JWT 认证）
- 个人资料编辑（头像、昵称、签名）
- 邮箱绑定（需邮箱验证码）
- 角色权限（普通用户 / 管理员）
- 管理员后台（用户管理、公告管理）

### 📝 养护日记
- 撰写养护心得、种植笔记
- 按时间线浏览个人日记

---

## 快速开始

### 环境要求

| 依赖 | 版本 |
|------|------|
| Java | 21+ |
| MySQL | 8.0+ |
| Node.js | 18+ |
| Maven | 3.6+ |
| Redis | 7.0+（可选） |

### 1. 初始化数据库

```bash
mysql -u root -p < plant-backend/src/main/resources/db/greenly-init.sql
```

**默认账号**（所有用户密码均为 `admin123`）：

| 用户名 | 角色 |
|--------|------|
| admin | 管理员 |
| user1 | 普通用户 |
| user2 | 普通用户 |
| gardener | 园艺爱好者 |

### 2. 启动后端

```bash
cd plant-backend
cp .env.example .env    # 编辑 .env 配置数据库连接等
mvn spring-boot:run     # 默认端口 9090
```

### 3. 启动前端

```bash
cd plant-frontend
npm install
npm run dev             # 访问 http://localhost:5173
```

详细步骤见 **[快速启动指南](QUICK_START.md)**。

---

## 项目结构

```
Greenly/
├── plant-backend/              # Spring Boot 后端
│   ├── src/main/java/          #   Java 源码 (13 控制器 / 19 服务 / 15 实体)
│   └── src/main/resources/     #   配置文件 + SQL 初始化脚本
├── plant-frontend/             # Vue 3 前端
│   ├── src/pages/              #   13 个功能模块页面
│   ├── src/api/                #   API 请求封装
│   ├── src/stores/             #   Pinia 状态管理
│   └── src/router/             #   路由配置（模块化）
├── docs/                       # 项目文档
├── scripts/                    # 运维脚本
├── data/uploads/               # 用户上传文件
├── PROJECT_STRUCTURE.md        # 完整项目结构说明
└── QUICK_START.md              # 快速启动指南
```

---

## 技术栈

| 层级 | 技术 |
|------|------|
| **后端框架** | Spring Boot 3.3.5 |
| **ORM** | MyBatis Plus 3.5.5 |
| **安全** | Spring Security + JWT + BCrypt |
| **数据库** | MySQL 8.0 |
| **缓存** | Redis 7.0 |
| **前端框架** | Vue 3.5 + Vite 7.2 |
| **UI 组件** | Element Plus 2.13 |
| **状态管理** | Pinia |
| **图表** | ECharts |
| **AI 服务** | 百度 AI（植物识别 + 图像诊断） |
| **反向代理** | Nginx + Let's Encrypt HTTPS |

---

## 文档

### 入门
- [快速启动指南](QUICK_START.md)
- [项目结构说明](PROJECT_STRUCTURE.md)
- [常见问题与故障排查](docs/troubleshooting/common-issues.md)

### 部署
- [云服务器部署指南](docs/guides/CLOUD_DEPLOYMENT_GUIDE.md)
- [部署速查卡](docs/guides/CLOUD_DEPLOYMENT_QUICK_REF.md)
- [数据库备份与恢复](docs/guides/DATABASE_BACKUP_GUIDE.md)
- [数据库快速参考](docs/guides/DATABASE_QUICK_REFERENCE.md)
- [Redis 配置](docs/guides/REDIS_SETUP_GUIDE.md)
- [百度 AI 配置](docs/guides/BAIDU_AI_SETUP.md)

### 开发
- [技术栈说明](docs/development/TECH_STACK.md)
- [代码规范](docs/development/CODING_STANDARDS.md)
- [并发能力评估](docs/development/CONCURRENCY_ASSESSMENT_REPORT.md)
- [生产环境配置优化](docs/development/PRODUCTION_CONFIG_OPTIMIZATION.md)
- [用户模块指南](docs/modules/USER_MODULE_GUIDE.md)

### 环境配置
- [后端环境变量](plant-backend/ENV_SETUP.md)

---

## 贡献

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m "feat: add AmazingFeature"`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

请遵循 [代码规范](docs/development/CODING_STANDARDS.md)。

---

## License

[MIT](LICENSE)

---

_让植物养护变得更简单 🌱_
