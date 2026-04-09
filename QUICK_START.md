# 🚀 Greenly 快速启动指南

**预计时间**: 10-15 分钟

---

## 📋 前置要求

| 软件 | 版本 | 验证命令 |
|------|------|---------|
| Java JDK | 21+ | `java -version` |
| MySQL | 8.0+ | `mysql --version` |
| Node.js | 18+ | `node -v` |
| Maven | 3.6+ | `mvn -version` |
| Redis | 7.0+（可选） | `redis-cli --version` |

---

## 🗄️ 第一步：配置数据库

### 方法一：SQL 脚本导入（推荐）

```bash
mysql -u root -p < plant-backend/src/main/resources/db/greenly-init.sql
```

### 方法二：手动创建

```sql
CREATE DATABASE greenly_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE greenly_db;
SOURCE plant-backend/src/main/resources/db/greenly-init.sql;
```

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| user1 | admin123 | 普通用户 |
| user2 | admin123 | 普通用户 |
| gardener | admin123 | 园艺爱好者 |

> ⚠️ 生产环境请立即修改默认密码。

---

## 🔧 第二步：配置后端

```bash
cd plant-backend

# 复制环境变量模板
cp .env.example .env

# 编辑 .env，填写必要配置：
# DB_USERNAME=root
# DB_PASSWORD=your_password
# JWT_SECRET=your_jwt_secret
# BAIDU_AI_APP_ID=... (可选，用于 AI 识别)
# BAIDU_AI_API_KEY=...
# BAIDU_AI_SECRET_KEY=...
# MAIL_USERNAME=... (可选，用于提醒邮件)
# MAIL_PASSWORD=...
```

### 启动

```bash
mvn spring-boot:run
```

看到 `Started PlantBackendApplication` 表示成功。访问 http://localhost:9090/actuator 验证。

---

## 🎨 第三步：配置前端

```bash
cd plant-frontend
npm install
npm run dev
```

访问 http://localhost:5173

---

## ☁️ 部署到服务器

详见 [云部署指南](docs/guides/CLOUD_DEPLOYMENT_GUIDE.md)。

### 快速部署命令

```bash
# 在服务器上
scp scripts/deploy-to-cloud.sh root@your-server:/tmp/
ssh root@your-server
sudo bash /tmp/deploy-to-cloud.sh
```

---

## 🛠️ 故障排查

遇到问题？查看 [常见问题与故障排查](docs/troubleshooting/common-issues.md)。

常见问题：
- **数据库连接失败** → 检查 MySQL 是否启动，密码是否正确
- **中文乱码** → 确认数据库字符集为 utf8mb4
- **登录失败** → 检查密码是否为 BCrypt 加密存储

