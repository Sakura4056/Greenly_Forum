# 🗂️ Greenly 项目结构说明

**最后更新**: 2026-04-15

---

## 📁 项目根目录

```
Greenly/
├── plant-backend/             # 🔧 Spring Boot 后端
├── plant-frontend/            # 🎨 Vue 3 前端
├── docs/                      # 📚 项目文档
│   ├── guides/                # 部署与配置指南
│   ├── development/           # 开发文档
│   ├── modules/               # 模块文档
│   ├── troubleshooting/       # 故障排查
│   └── archive/               # 历史归档
├── scripts/                   # 🔨 运维脚本
├── data/uploads/              # 📸 用户上传文件
├── .vscode/                   # VS Code 配置
├── README.md
├── QUICK_START.md
└── PROJECT_STRUCTURE.md
```

---

## 🔧 后端结构 (`plant-backend`)

### 包结构 (`com.plant.backend`)

```
├── annotation/          # 自定义注解 (1)
│   └── Cacheable
├── aspect/              # AOP 切面 (1)
│   └── CacheAspect
├── base/                # 基础类 (2)
│   ├── BaseController
│   └── Result
├── config/              # 配置类 (9)
│   ├── BaiduAiConfig        # 百度 AI
│   ├── CorsConfig           # CORS 跨域
│   ├── HttpClientConfig     # HTTP 客户端
│   ├── MyBatisPlusConfig    # MyBatis Plus
│   ├── MyMetaObjectHandler  # 自动填充
│   ├── RedisConfig          # Redis
│   ├── SecurityConfig       # Spring Security
│   ├── SwaggerConfig        # Swagger API 文档
│   └── WebMvcConfig         # Web MVC
├── controller/          # REST 控制器 (13)
│   ├── AiController             # AI 识别
│   ├── AnnouncementController   # 系统公告
│   ├── CareRecordController     # 养护记录
│   ├── CareScheduleController   # 养护计划
│   ├── ForumController          # 论坛
│   ├── MyPlantController        # 我的植物
│   ├── PhotoController          # 照片
│   ├── PlantController          # 官方植物库
│   ├── PlantDiaryController     # 养护日记
│   ├── ReminderController       # 提醒
│   ├── SmartWateringController  # 智能浇水
│   ├── TestController           # 测试接口
│   └── UserController           # 用户
├── dto/                 # 数据传输对象 (13)
├── entity/              # 实体类 (16)
├── mapper/              # MyBatis Mapper (16)
├── service/             # 服务层 (20)
│   └── impl/            # 服务实现 (19)
├── event/               # 事件
│   └── UserDeletedEvent
├── task/                # 定时任务
│   └── CareTask
└── PlantBackendApplication  # 启动类
```

### 数据表 (16 张)

| 分类 | 表名 | 说明 |
|------|------|------|
| **用户** | `sys_user` | 系统用户 |
| **植物** | `official_plant` / `my_plant` | 官方植物库 / 我的植物 |
| **养护** | `care_schedule` / `care_record` | 养护计划 / 养护记录 |
| **内容** | `plant_photo` / `plant_diary` | 照片 / 日记 |
| **论坛** | `forum_post` / `forum_comment` / `forum_like` / `forum_category` | 帖子 / 评论 / 点赞 / 分类 |
| **提醒** | `reminder_config` / `reminder` | 提醒配置 / 消息提醒 |
| **AI** | `ai_conversation` / `identify_history` | AI 对话记录 / 识别历史 |
| **系统** | `announcement` | 公告 |

---

## 🎨 前端结构 (`plant-frontend`)

### 页面目录 (`src/pages`)

| 目录 | 页面 | 说明 |
|------|------|------|
| `login/` | 登录页 | 用户登录 |
| `register/` | 注册页 | 用户注册 |
| `dashboard/` | 首页 | 数据概览 |
| `plant/` | 植物管理 | 官方库 + 我的植物 |
| `care/` | 养护管理 | 计划 + 记录 + 统计 |
| `diary/` | 养护日记 | 日记列表 + 详情 |
| `photo/` | 照片管理 | 上传 + 浏览 |
| `forum/` | 论坛 | 帖子列表 + 详情 + 发布 |
| `ai/` | AI 诊断 | 植物识别 |
| `identification/` | 植物识别 | 图像识别 |
| `reminder/` | 提醒设置 | 提醒配置 |
| `user/` | 个人中心 | 资料编辑 |
| `announcement/` | 公告浏览 | 系统公告列表 |
| `admin/` | 后台管理 | 用户管理 + 公告 |

### 技术栈

- Vue 3.5 · Vite 7.2 · Pinia · Vue Router 5 · Element Plus · Axios · ECharts · SCSS

---

## 📚 文档结构 (`docs`)

```
docs/
├── guides/                        # 部署与配置
│   ├── DATABASE_QUICK_REFERENCE   # 数据库快速参考
│   ├── DATABASE_BACKUP_GUIDE      # 备份与恢复
│   ├── DEPLOYMENT                 # 生产部署
│   ├── CLOUD_DEPLOYMENT_GUIDE     # 云服务器部署
│   ├── CLOUD_DEPLOYMENT_QUICK_REF # 部署速查
│   ├── REDIS_SETUP_GUIDE          # Redis 配置
│   └── BAIDU_AI_SETUP             # 百度 AI 配置
├── development/                   # 开发文档
│   ├── TECH_STACK                 # 技术栈
│   ├── CODING_STANDARDS           # 代码规范
│   ├── AI_FEATURES_SUMMARY        # AI 功能
│   ├── CONCURRENCY_ASSESSMENT     # 并发评估
│   ├── PRODUCTION_CONFIG          # 生产配置优化
│   ├── SDK_VS_HTTP_COMPARISON     # 接入方案对比
│   └── BAIDU_API_UPDATE           # API 更新记录
├── modules/                       # 模块文档
│   └── USER_MODULE_GUIDE          # 用户模块
├── troubleshooting/               # 故障排查
│   └── common-issues              # 常见问题
└── archive/                       # 历史归档
    ├── legacy/                    # 遗留文档
    └── refactoring/               # 重构记录
```

