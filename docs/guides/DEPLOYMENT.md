# Greenly 生产环境部署指南

**文档版本**: v2.0
**最后更新**: 2026-04-09
**适用环境**: 阿里云 ECS (Alibaba Cloud Linux 3)

---

## 📋 目录

- [前置要求](#前置要求)
- [服务器环境](#服务器环境)
- [数据库部署](#数据库部署)
- [后端部署](#后端部署)
- [前端部署](#前端部署)
- [HTTPS 配置](#https-配置)
- [维护与监控](#维护与监控)
- [常见问题](#常见问题)

---

## 🎯 前置要求

### 必需软件

| 软件 | 版本 | 用途 |
|------|------|------|
| Java JDK | 21 (Alibaba Dragonwell) | 运行 Spring Boot |
| MySQL | 8.0 | 数据存储 |
| Redis | 7.0 | 缓存服务 |
| Node.js | 18+ | 前端构建 |
| Nginx | 1.20+ | 反向代理 |
| Maven | 3.6+ | 后端打包 |

### 端口规划

| 服务 | 端口 | 说明 |
|------|------|------|
| Spring Boot | 9090 | 后端 API |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nginx | 80 / 443 | HTTP / HTTPS |

---

## 🖥️ 服务器环境

### 当前生产环境

| 项目 | 值 |
|------|-----|
| 实例 | 阿里云 ECS (2 vCPU / 2 GiB) |
| OS | Alibaba Cloud Linux 3 (x64) |
| Java | Dragonwell 21.0.5 |
| 域名 | zhang0903.top |
| 项目路径 | `/opt/new_new_Greenly/` |
| 后端 JAR | `plant-backend/target/plant-backend-0.0.1-SNAPSHOT.jar` |
| 前端 dist | `plant-frontend/dist/` |
| 上传目录 | `data/uploads/` |

### 安装软件（Alibaba Cloud Linux 3）

```bash
# Java 21
yum install -y java-21-alibaba-dragonwell

# MySQL 8.0
yum install -y mysql-server
systemctl enable mysqld && systemctl start mysqld

# Redis
yum install -y redis
systemctl enable redis && systemctl start redis

# Nginx
yum install -y nginx
systemctl enable nginx && systemctl start nginx

# Node.js 18 (用于前端构建)
curl -fsSL https://rpm.nodesource.com/setup_18.x | bash -
yum install -y nodejs

# Maven 3.9
cd /opt && curl -fsSL https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz | tar xz
echo 'export PATH=/opt/apache-maven-3.9.6/bin:$PATH' >> /etc/profile.d/maven.sh
source /etc/profile.d/maven.sh
```

---

## 🗄️ 数据库部署

### 1. 安全初始化

```bash
mysql_secure_installation
```

### 2. 创建数据库

```bash
mysql -u root -p
```

```sql
CREATE DATABASE greenly_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- 如需创建专用用户：
-- CREATE USER 'greenly'@'localhost' IDENTIFIED BY 'your_secure_password';
-- GRANT ALL PRIVILEGES ON greenly_db.* TO 'greenly'@'localhost';
-- FLUSH PRIVILEGES;
```

### 3. 导入数据

```bash
mysql -u root -p greenly_db < /opt/new_new_Greenly/plant-backend/src/main/resources/db/greenly-init.sql
```

### 4. MySQL 性能调优

编辑 `/etc/my.cnf`，在 `[mysqld]` 段添加：

```ini
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
max_connections = 200
innodb_buffer_pool_size = 1G    # 2G 内存机器建议 1G
innodb_log_file_size = 256M
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
```

```bash
systemctl restart mysqld
```

---

## 🔧 后端部署

### 1. 打包

```bash
cd /opt/new_new_Greenly/plant-backend
mvn clean package -DskipTests
```

### 2. 配置环境变量

编辑 `/opt/new_new_Greenly/plant-backend/.env`：

```bash
# 数据库
DB_USERNAME=root
DB_PASSWORD=your_password
DB_URL=jdbc:mysql://localhost:3306/greenly_db?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
DB_HOST=localhost
DB_PORT=3306

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# 邮件
MAIL_HOST=smtp.163.com
MAIL_PORT=465
MAIL_USERNAME=your_email@163.com
MAIL_PASSWORD=your_smtp_auth_code

# JWT（务必更换为随机字符串）
JWT_SECRET=$(openssl rand -hex 32)

# 百度 AI
BAIDU_AI_APP_ID=your_app_id
BAIDU_AI_API_KEY=your_api_key
BAIDU_AI_SECRET_KEY=your_secret_key

# 应用
SERVER_PORT=9090
FILE_UPLOAD_PATH=/opt/new_new_Greenly/data/uploads/
UPLOAD_PATH=/opt/new_new_Greenly/data/uploads/
```

> ⚠️ `.env` 包含敏感信息，不要提交到 Git。

### 3. 配置 Spring Profile

生产环境使用 `application-prod.yml`（位于 `src/main/resources/`），关键配置：

```yaml
server:
  port: 9090

logging:
  level:
    root: WARN
    com.plant.backend: INFO
```

### 4. Systemd 服务

`/etc/systemd/system/greenly-backend.service`：

```ini
[Unit]
Description=Greenly Plant Care Backend
After=network.target mysql.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/new_new_Greenly/plant-backend
EnvironmentFile=/opt/new_new_Greenly/plant-backend/.env
ExecStart=/usr/lib/jvm/java-21-alibaba-dragonwell-21.0.5.0.5-1.1.al8.x86_64/bin/java -Xms256m -Xmx512m -jar /opt/new_new_Greenly/plant-backend/target/plant-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
```

> **注意**：Java 路径根据实际安装位置调整，用 `readlink -f $(which java)` 查询。

### 5. 启动服务

```bash
systemctl daemon-reload
systemctl enable greenly-backend
systemctl start greenly-backend
systemctl status greenly-backend
```

查看日志：

```bash
journalctl -u greenly-backend -f
# 或查看最近 50 行
journalctl -u greenly-backend -n 50 --no-pager
```

### 6. 创建上传目录

```bash
mkdir -p /opt/new_new_Greenly/data/uploads
chmod 755 /opt/new_new_Greenly/data/uploads
```

---

## 🎨 前端部署

### 1. 构建

```bash
cd /opt/new_new_Greenly/plant-frontend

# 生产环境 API 地址
echo "VITE_API_BASE_URL=https://zhang0903.top/api" > .env.production

npm install
npm run build
```

构建产物在 `dist/` 目录。

### 2. Nginx 配置

`/etc/nginx/conf.d/greenly.conf`：

```nginx
limit_req_zone $binary_remote_addr zone=login_limit:10m rate=10r/m;

server {
    server_name zhang0903.top _;

    # 前端静态文件
    root /opt/new_new_Greenly/plant-frontend/dist;
    index index.html;

    # HTML 不缓存
    location = /index.html {
        add_header Cache-Control "no-cache, no-store, must-revalidate";
        add_header Pragma "no-cache";
        add_header Expires "0";
        try_files $uri =404;
    }

    # 静态资源长缓存（Vite 使用 content hash）
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Vue Router history mode
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:9090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 50M;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }

    # 登录限流
    location = /api/user/login {
        limit_req zone=login_limit burst=5 nodelay;
        proxy_pass http://127.0.0.1:9090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }

    # Actuator（仅本地访问）
    location /actuator/ {
        allow 127.0.0.1;
        deny all;
        proxy_pass http://127.0.0.1:9090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # Swagger
    location /swagger-ui/ {
        proxy_pass http://127.0.0.1:9090;
        proxy_set_header Host $host;
    }

    location /v3/api-docs {
        proxy_pass http://127.0.0.1:9090;
        proxy_set_header Host $host;
    }

    # 上传文件
    location /uploads/ {
        alias /opt/new_new_Greenly/data/uploads/;
        expires 7d;
    }

    # HTTPS（Let's Encrypt）
    listen 443 ssl;
    ssl_certificate /etc/letsencrypt/live/zhang0903.top/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/zhang0903.top/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;
}

# HTTP -> HTTPS 重定向
server {
    listen 80 default_server;
    server_name zhang0903.top _;
    if ($host = zhang0903.top) {
        return 301 https://$host$request_uri;
    }
    return 404;
}
```

### 3. 测试并重载

```bash
nginx -t && systemctl reload nginx
```

---

## 🔒 HTTPS 配置

### 申请 Let's Encrypt 证书

```bash
# 安装 certbot
yum install -y certbot python3-certbot-nginx

# 申请证书
certbot --nginx -d zhang0903.top

# 自动续期
echo "0 3 * * * certbot renew --quiet && systemctl reload nginx" | crontab -
```

### 验证

```bash
curl -I https://zhang0903.top
# 应返回 HTTP/2 200
```

---

## 📊 维护与监控

### 健康检查

```bash
# 后端健康
curl -s http://127.0.0.1:9090/actuator/health
# {"status":"UP"}

# 服务状态
systemctl is-active greenly-backend
systemctl is-active nginx
systemctl is-active mysqld
systemctl is-active redis
```

### 日志查看

```bash
# 后端日志
journalctl -u greenly-backend -f

# 应用日志文件
tail -f /opt/new_new_Greenly/plant-backend/logs/plant-backend.log

# Nginx 访问日志
tail -f /var/log/nginx/access.log

# Nginx 错误日志
tail -f /var/log/nginx/error.log
```

### 数据库监控

```sql
-- 当前连接数
SHOW STATUS LIKE 'Threads_connected';

-- 表大小
SELECT table_name, ROUND(data_length/1024/1024, 2) AS size_mb
FROM information_schema.tables
WHERE table_schema = 'greenly_db'
ORDER BY data_length DESC;
```

### 数据备份

参考 [数据库备份指南](DATABASE_BACKUP_GUIDE.md)。

手动备份：

```bash
mysqldump -u root -p greenly_db | gzip > /opt/new_new_Greenly/backups/db-$(date +%Y%m%d).sql.gz
```

---

## 🆘 常见问题

### 1. 应用启动失败

```bash
# 查看详细日志
journalctl -u greenly-backend -n 100 --no-pager

# 检查端口
ss -tlnp | grep 9090

# 检查 Java
java -version
```

### 2. 数据库连接失败

```bash
systemctl status mysqld
mysql -u root -p -e "SELECT 1"
```

### 3. Nginx 502 Bad Gateway

```bash
# 后端是否运行
systemctl status greenly-backend

# Nginx 配置测试
nginx -t
```

### 4. 图片上传失败

```bash
# 目录权限
ls -la /opt/new_new_Greenly/data/uploads/

# 磁盘空间
df -h

# Nginx body size
grep client_max_body_size /etc/nginx/conf.d/greenly.conf
```

---

## ✅ 部署检查清单

- [ ] 数据库已创建并初始化
- [ ] Redis 已启动
- [ ] `.env` 已配置且密码已修改
- [ ] JWT Secret 已更换为强随机字符串
- [ ] SSL 证书已配置
- [ ] Nginx 配置通过 `nginx -t`
- [ ] 后端服务正常启动
- [ ] 健康检查返回 `{"status":"UP"}`
- [ ] 前端页面可正常访问
- [ ] API 接口可正常调用

---

*v2.0 — 2026-04-09 更新：适配阿里云 ECS 实际生产环境*
