# 环境变量配置指南

本文档说明如何配置 Greenly 后端所需的环境变量。

---

## 📋 目录

- [快速开始](#快速开始)
- [配置方法](#配置方法)
- [必需的环境变量](#必需的环境变量)
- [常见问题](#常见问题)

---

## 🚀 快速开始

### 1. 复制环境变量模板

```bash
cd plant-backend
cp .env.example .env
```

### 2. 编辑 .env 文件

打开 `.env` 文件，根据您的实际情况修改配置值：

```env
DB_USERNAME=root
DB_PASSWORD=your_actual_password
MAIL_USERNAME=your_email@163.com
MAIL_PASSWORD=your_smtp_auth_code
# ... 其他配置
```

### 3. 启动应用

```bash
mvn spring-boot:run
```

应用会自动加载 `.env` 文件中的配置。

---

## 🔧 配置方法

### 方法一：使用 .env 文件（推荐用于本地开发）

**优点**：
- 配置简单，一目了然
- 易于管理和备份
- 支持不同环境的不同配置

**步骤**：
1. 在 `plant-backend/` 目录下创建 `.env` 文件
2. 参考 `.env.example` 填写配置
3. 启动应用时会自动加载

**注意**：`.env` 文件已添加到 `.gitignore`，不会被提交到版本控制。

---

### 方法二：设置系统环境变量（推荐用于生产环境）

#### Windows 系统

**临时设置（当前命令行窗口）：**

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:MAIL_USERNAME="your_email@163.com"
$env:MAIL_PASSWORD="your_auth_code"
$env:JWT_SECRET="your_jwt_secret"
$env:BAIDU_AI_APP_ID="7602357"
$env:BAIDU_AI_API_KEY="your_api_key"
$env:BAIDU_AI_SECRET_KEY="your_secret_key"
```

**永久设置：**

1. 右键"此电脑" → "属性" → "高级系统设置"
2. 点击"环境变量"
3. 在"系统变量"中新建变量

#### Linux/macOS 系统

**临时设置：**

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export MAIL_USERNAME=your_email@163.com
export MAIL_PASSWORD=your_auth_code
export JWT_SECRET=your_jwt_secret
export BAIDU_AI_APP_ID=7602357
export BAIDU_AI_API_KEY=your_api_key
export BAIDU_AI_SECRET_KEY=your_secret_key
```

**永久设置（添加到 `~/.bashrc` 或 `~/.zshrc`）：**

```bash
echo 'export DB_USERNAME=root' >> ~/.bashrc
echo 'export DB_PASSWORD=your_password' >> ~/.bashrc
# ... 其他变量
source ~/.bashrc
```

---

### 方法三：IDEA 运行配置

1. 点击 **Run** → **Edit Configurations**
2. 找到 `PlantBackendApplication` 配置
3. 在 **Environment variables** 字段中添加：

```
DB_USERNAME=root;DB_PASSWORD=your_password;MAIL_USERNAME=your_email@163.com;...
```

或者安装 **EnvFile** 插件：
1. File → Settings → Plugins
2. 搜索并安装 "EnvFile"
3. 在运行配置的 "EnvFile" 标签页中勾选 "Enable EnvFile"
4. 选择 `.env` 文件路径

---

### 方法四：Maven 命令行参数

```bash
cd plant-backend

# 方式 1：直接指定环境变量
DB_USERNAME=root DB_PASSWORD=your_password mvn spring-boot:run

# 方式 2：使用 Spring Boot 的 -D 参数
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-DDB_USERNAME=root -DDB_PASSWORD=your_password"
```

---

## 📝 必需的环境变量

### 数据库配置

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `DB_USERNAME` | MySQL 用户名 | `root` |
| `DB_PASSWORD` | MySQL 密码 | `your_password` |
| `DB_URL` | MySQL 连接 URL | `jdbc:mysql://localhost:3306/greenly_db?...` |

### 邮件配置

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `MAIL_HOST` | SMTP 服务器地址 | `smtp.163.com` |
| `MAIL_PORT` | SMTP 端口 | `465` |
| `MAIL_USERNAME` | 发件人邮箱 | `your_email@163.com` |
| `MAIL_PASSWORD` | SMTP 授权码（不是登录密码） | `your_auth_code` |

**获取 SMTP 授权码**：
1. 登录 163 邮箱
2. 进入"设置" → "POP3/SMTP/IMAP"
3. 开启 SMTP 服务
4. 生成授权码

### JWT 配置

| 变量名 | 说明 | 要求 |
|--------|------|------|
| `JWT_SECRET` | JWT 签名密钥 | 至少 32 个字符的随机字符串 |

**生成 JWT Secret**：
```bash
# Linux/macOS
openssl rand -hex 32

# 在线生成：https://generate-random.org/password-generator
```

### 百度 AI 配置

| 变量名 | 说明 | 获取方式 |
|--------|------|----------|
| `BAIDU_AI_APP_ID` | 应用 ID | 百度智能云控制台 |
| `BAIDU_AI_API_KEY` | API Key | 百度智能云控制台 |
| `BAIDU_AI_SECRET_KEY` | Secret Key | 百度智能云控制台 |

**获取百度 AI 凭据**：
1. 访问 [百度智能云](https://console.bce.baidu.com/ai/)
2. 创建应用
3. 复制 App ID、API Key、Secret Key

### Redis 配置（可选）

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `REDIS_HOST` | Redis 服务器地址 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密码 | 空 |

---

## ❓ 常见问题

### Q1: 启动时报错 "Could not resolve placeholder 'DB_PASSWORD'"

**原因**：环境变量未正确加载

**解决方案**：
1. 检查 `.env` 文件是否在 `plant-backend/` 目录下
2. 确认 `.env` 文件格式正确（无多余空格、引号）
3. 如果使用 IDEA，确认已安装 EnvFile 插件并启用
4. 尝试手动设置环境变量后重新启动

---

### Q2: .env 文件没有被自动加载

**原因**：DotenvConfig 未正确执行

**解决方案**：
1. 检查 `pom.xml` 中是否包含 `dotenv-java` 依赖
2. 确认 `DotenvConfig.java` 在 `com.plant.backend.config` 包下
3. 查看启动日志，确认是否有 ".env file loaded successfully" 消息

---

### Q3: Redis 连接失败

**原因**：Redis 未启动或配置错误

**解决方案**：
```bash
# 检查 Redis 是否运行
redis-cli ping

# 如果未运行，启动 Redis
redis-server

# Windows 用户可以使用 WSL 或下载 Redis for Windows
```

---

### Q4: 邮件发送失败

**原因**：SMTP 授权码错误或 SMTP 服务未开启

**解决方案**：
1. 登录邮箱，确认 SMTP 服务已开启
2. 重新生成授权码
3. 更新 `.env` 文件中的 `MAIL_PASSWORD`
4. 重启应用

---

### Q5: 如何验证配置是否生效？

**方法 1：查看启动日志**

启动应用后，应该看到类似日志：
```
INFO --- [.backend.config.DotenvConfig] : .env file loaded successfully from: /path/to/plant-backend/.env
INFO --- [com.zaxxer.hikari.HikariDataSource] : HikariPool-1 - Start completed.
```

**方法 2：测试 API**

```bash
# 测试登录接口
curl -X POST http://localhost:8085/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

如果返回正常响应，说明配置成功。

---

## 🔐 安全建议

1. **不要将 `.env` 文件提交到 Git**
   - 已添加到 `.gitignore`，请定期检查

2. **定期更换敏感凭据**
   - 数据库密码每 90 天更换一次
   - API Key 每 6 个月轮换一次

3. **生产环境使用密钥管理服务**
   - AWS Secrets Manager
   - Azure Key Vault
   - HashiCorp Vault

4. **限制文件权限**
   ```bash
   # Linux/macOS
   chmod 600 plant-backend/.env
   ```

---

## 📚 相关文档

- [项目快速开始](../QUICK_START.md)
- [技术栈说明](../docs/TECH_STACK.md)
- [代码规范](../docs/CODING_STANDARDS.md)

---

*最后更新：2026-04-05*
