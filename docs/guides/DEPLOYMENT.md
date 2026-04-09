# Greenly 生产环境部署指南

**文档版本**: v1.0  
**最后更新**: 2026-04-05  
**维护者**: Greenly 开发团队

---

## 📋 目录

- [前置要求](#前置要求)
- [服务器配置建议](#服务器配置建议)
- [数据库部署](#数据库部署)
- [后端部署](#后端部署)
- [前端部署](#前端部署)
- [HTTPS配置](#https配置)
- [监控与维护](#监控与维护)
- [常见问题](#常见问题)

---

## 🎯 前置要求

### 必需软件

| 软件 | 版本 | 用途 |
|------|------|------|
| **Java JDK** | 21+ | 运行Spring Boot应用 |
| **MySQL** | 8.0+ | 数据存储 |
| **Redis** | 7.0+ | 缓存服务 |
| **Node.js** | 18+ | 前端构建（仅构建时需要） |
| **Nginx** | 1.20+ | Web服务器/反向代理 |
| **Maven** | 3.6+ | 后端打包（可选，可使用预编译jar） |

### 端口规划

| 服务 | 默认端口 | 说明 |
|------|---------|------|
| Spring Boot | 8085 | 后端API服务 |
| MySQL | 3306 | 数据库 |
| Redis | 6379 | 缓存 |
| Nginx | 80/443 | Web服务器 |

---

## 💻 服务器配置建议

### 最小配置（开发/测试环境）

- **CPU**: 2核
- **内存**: 4GB
- **硬盘**: 50GB SSD
- **带宽**: 5Mbps

### 推荐配置（生产环境）

- **CPU**: 4核
- **内存**: 8GB
- **硬盘**: 100GB SSD
- **带宽**: 10Mbps+

### 高可用配置（大型项目）

- **应用服务器**: 2台（负载均衡）
- **数据库**: 主从复制
- **Redis**: 哨兵模式或集群
- **Nginx**: Keepalived双机热备

---

## 🗄️ 数据库部署

### MySQL 安装与配置

#### 1. 安装 MySQL（Ubuntu示例）

```bash
sudo apt update
sudo apt install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql
```

#### 2. 安全初始化

```bash
sudo mysql_secure_installation
```

按提示操作：
- 设置root密码
- 移除匿名用户
- 禁止root远程登录
- 删除test数据库

#### 3. 创建数据库和用户

```sql
-- 登录MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE greenly_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建专用用户（不要使用root）
CREATE USER 'greenly'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON greenly_db.* TO 'greenly'@'localhost';
FLUSH PRIVILEGES;
```

#### 4. 性能优化配置

编辑 `/etc/mysql/mysql.conf.d/mysqld.cnf`:

```ini
[mysqld]
# 字符集
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 连接数
max_connections = 200

# 缓冲池大小（根据内存调整，建议为物理内存的50-70%）
innodb_buffer_pool_size = 2G

# 日志文件大小
innodb_log_file_size = 512M

# 查询缓存（MySQL 8.0已移除）
# query_cache_size = 64M

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
```

重启MySQL:
```bash
sudo systemctl restart mysql
```

#### 5. 数据库初始化

```bash
# 上传SQL文件到服务器
scp plant-backend/src/main/resources/db/greenly-init.sql user@server:/tmp/

# 执行SQL
mysql -u greenly -p greenly_db < /tmp/greenly-init.sql
```

验证:
```sql
USE greenly_db;
SHOW TABLES;
SELECT COUNT(*) FROM sys_user;
SELECT COUNT(*) FROM official_plant;
```

---

## 🔧 后端部署

### 1. 打包应用

在开发机器上:

```bash
cd plant-backend
mvn clean package -DskipTests
```

生成的jar文件位于: `target/plant-backend-0.0.1-SNAPSHOT.jar`

### 2. 上传到服务器

```bash
scp target/plant-backend-0.0.1-SNAPSHOT.jar user@server:/opt/greenly/
```

### 3. 配置环境变量

创建 `.env` 文件（**重要：不要提交到版本控制**）:

```bash
# /opt/greenly/.env

# 数据库配置
DB_USERNAME=greenly
DB_PASSWORD=your_secure_password
DB_URL=jdbc:mysql://localhost:3306/greenly_db?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# 邮件配置
MAIL_HOST=smtp.163.com
MAIL_PORT=465
MAIL_USERNAME=your_email@163.com
MAIL_PASSWORD=your_smtp_auth_code

# JWT配置（生成随机字符串）
JWT_SECRET=$(openssl rand -hex 32)

# 百度AI配置
BAIDU_AI_APP_ID=your_app_id
BAIDU_AI_API_KEY=your_api_key
BAIDU_AI_SECRET_KEY=your_secret_key

# 应用配置
SERVER_PORT=8085
FILE_UPLOAD_PATH=/opt/greenly/data/uploads/
```

**生成安全的JWT Secret**:
```bash
# Linux/Mac
openssl rand -hex 32

# Windows PowerShell
-join ((48..57) + (65..90) + (97..122) | Get-Random -Count 64 | ForEach-Object {[char]$_})
```

### 4. 创建启动脚本

创建 `/opt/greenly/start.sh`:

```bash
#!/bin/bash

APP_NAME="plant-backend"
APP_JAR="/opt/greenly/plant-backend-0.0.1-SNAPSHOT.jar"
APP_LOG="/opt/greenly/logs/application.log"
APP_PID="/opt/greenly/app.pid"

# 加载环境变量
set -a
source /opt/greenly/.env
set +a

# JVM参数
JVM_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

start() {
    if [ -f "$APP_PID" ]; then
        echo "$APP_NAME is already running (PID: $(cat $APP_PID))"
        return 1
    fi

    echo "Starting $APP_NAME..."
    nohup java $JVM_OPTS -jar $APP_JAR \
        --spring.profiles.active=prod \
        > $APP_LOG 2>&1 &

    echo $! > $APP_PID
    echo "$APP_NAME started (PID: $!)"
}

stop() {
    if [ ! -f "$APP_PID" ]; then
        echo "$APP_NAME is not running"
        return 1
    fi

    PID=$(cat $APP_PID)
    echo "Stopping $APP_NAME (PID: $PID)..."
    kill $PID

    # 等待进程结束
    for i in {1..30}; do
        if ! ps -p $PID > /dev/null; then
            break
        fi
        sleep 1
    done

    # 强制杀死
    if ps -p $PID > /dev/null; then
        echo "Force killing $APP_NAME..."
        kill -9 $PID
    fi

    rm -f $APP_PID
    echo "$APP_NAME stopped"
}

restart() {
    stop
    sleep 2
    start
}

status() {
    if [ -f "$APP_PID" ]; then
        PID=$(cat $APP_PID)
        if ps -p $PID > /dev/null; then
            echo "$APP_NAME is running (PID: $PID)"
        else
            echo "$APP_NAME is not running (stale PID file)"
        fi
    else
        echo "$APP_NAME is not running"
    fi
}

case "$1" in
    start)   start ;;
    stop)    stop ;;
    restart) restart ;;
    status)  status ;;
    *)       echo "Usage: $0 {start|stop|restart|status}" ;;
esac
```

赋予执行权限:
```bash
chmod +x /opt/greenly/start.sh
```

### 5. 创建systemd服务（推荐）

创建 `/etc/systemd/system/greenly.service`:

```ini
[Unit]
Description=Greenly Plant Management System
After=syslog.target network.target mysql.service redis.service

[Service]
Type=simple
User=www-data
Group=www-data
WorkingDirectory=/opt/greenly
EnvironmentFile=/opt/greenly/.env
ExecStart=/usr/bin/java -Xms512m -Xmx2g -jar /opt/greenly/plant-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
ExecStop=/bin/kill -SIGTERM $MAINPID
Restart=on-failure
RestartSec=10

# 日志
StandardOutput=journal
StandardError=journal
SyslogIdentifier=greenly

# 安全限制
NoNewPrivileges=true
PrivateTmp=true

[Install]
WantedBy=multi-user.target
```

启动服务:
```bash
sudo systemctl daemon-reload
sudo systemctl enable greenly
sudo systemctl start greenly
sudo systemctl status greenly
```

查看日志:
```bash
sudo journalctl -u greenly -f
```

### 6. 创建上传目录

```bash
mkdir -p /opt/greenly/data/uploads
chown -R www-data:www-data /opt/greenly/data/uploads
chmod -R 755 /opt/greenly/data/uploads
```

---

## 🎨 前端部署

### 1. 构建生产版本

在开发机器上:

```bash
cd plant-frontend

# 安装依赖
npm install

# 配置生产环境变量
# 创建 .env.production
echo "VITE_API_BASE_URL=https://your-domain.com/api" > .env.production

# 构建
npm run build
```

生成的文件位于: `dist/` 目录

### 2. 上传到服务器

```bash
# 压缩
tar -czf dist.tar.gz dist/

# 上传
scp dist.tar.gz user@server:/opt/greenly/frontend/

# 解压
cd /opt/greenly/frontend
tar -xzf dist.tar.gz
```

### 3. Nginx配置

创建 `/etc/nginx/sites-available/greenly`:

```nginx
# HTTP -> HTTPS 重定向
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS 配置
server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;

    # SSL证书
    ssl_certificate /etc/nginx/ssl/your-domain.crt;
    ssl_certificate_key /etc/nginx/ssl/your-domain.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # 根目录
    root /opt/greenly/frontend/dist;
    index index.html;

    # Gzip压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml+rss application/json application/javascript;

    # 静态资源缓存
    location ~* \.(jpg|jpeg|png|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    # API反向代理
    location /api/ {
        proxy_pass http://localhost:8085/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }

    # Vue Router History模式支持
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}
```

启用配置:
```bash
sudo ln -s /etc/nginx/sites-available/greenly /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

---

## 🔒 HTTPS配置

### 1. 申请SSL证书

#### 方式一：Let's Encrypt（免费，推荐）

```bash
# 安装Certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取证书
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# 自动续期测试
sudo certbot renew --dry-run
```

#### 方式二：购买商业证书

从阿里云、腾讯云等购买SSL证书，下载后上传到服务器:
```bash
sudo mkdir -p /etc/nginx/ssl
sudo cp your-domain.crt /etc/nginx/ssl/
sudo cp your-domain.key /etc/nginx/ssl/
sudo chmod 600 /etc/nginx/ssl/*
```

### 2. 验证HTTPS

访问 https://your-domain.com，检查浏览器地址栏是否显示锁图标。

在线测试: https://www.ssllabs.com/ssltest/

---

## 📊 监控与维护

### 健康检查

后端提供Actuator端点:

```bash
curl http://localhost:8085/actuator/health
```

响应:
```json
{
    "status": "UP"
}
```

### 日志查看

```bash
# systemd日志
sudo journalctl -u greenly -f

# 应用日志（如果配置了文件输出）
tail -f /opt/greenly/logs/application.log

# Nginx日志
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

### 性能监控

#### 使用 htop 监控系统资源

```bash
sudo apt install htop
htop
```

#### 数据库监控

```sql
-- 查看当前连接数
SHOW STATUS LIKE 'Threads_connected';

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log%';

-- 查看表大小
SELECT table_name, ROUND(data_length/1024/1024, 2) AS size_mb
FROM information_schema.tables
WHERE table_schema = 'greenly_db'
ORDER BY data_length DESC;
```

#### Redis监控

```bash
redis-cli info
redis-cli info memory
redis-cli info stats
```

### 定期备份

参考 [数据库备份指南](DATABASE_BACKUP_GUIDE.md)

```bash
# 手动备份
/opt/greenly/scripts/db-backup.ps1

# 设置定时任务（Windows）
/opt/greenly/scripts/setup-scheduled-task.ps1
```

---

## 🆘 常见问题

### 1. 应用启动失败

**症状**: `systemctl status greenly` 显示 failed

**排查步骤**:
```bash
# 查看详细日志
sudo journalctl -u greenly -n 100 --no-pager

# 检查端口占用
sudo lsof -i :8085

# 检查Java版本
java -version

# 检查.env文件权限
ls -la /opt/greenly/.env
```

**常见原因**:
- 端口被占用
- 数据库连接失败
- 环境变量未正确加载
- Java版本不匹配

### 2. 数据库连接失败

**症状**: `Communications link failure`

**解决方案**:
```bash
# 检查MySQL是否运行
sudo systemctl status mysql

# 测试连接
mysql -u greenly -p greenly_db -e "SELECT 1"

# 检查防火墙
sudo ufw status
sudo ufw allow 3306/tcp  # 如果需要远程连接
```

### 3. 内存溢出 (OOM)

**症状**: 应用突然停止，日志显示 `OutOfMemoryError`

**解决方案**:
```bash
# 增加JVM堆内存
# 编辑 /etc/systemd/system/greenly.service
ExecStart=/usr/bin/java -Xms1g -Xmx4g -jar ...

sudo systemctl daemon-reload
sudo systemctl restart greenly
```

### 4. Nginx 502 Bad Gateway

**症状**: 访问网站显示 502

**排查**:
```bash
# 检查后端是否运行
sudo systemctl status greenly

# 检查Nginx配置
sudo nginx -t

# 检查防火墙
sudo ufw status
```

### 5. 图片上传失败

**检查项**:
```bash
# 目录权限
ls -la /opt/greenly/data/uploads/

# 磁盘空间
df -h

# 文件大小限制（Nginx）
# 在nginx.conf中添加:
client_max_body_size 50M;
```

---

## 📚 相关文档

- [数据库备份指南](DATABASE_BACKUP_GUIDE.md)
- [Redis配置指南](REDIS_SETUP_GUIDE.md)
- [常见问题排查](troubleshooting/common-issues.md)
- [技术栈说明](TECH_STACK.md)

---

## ✅ 部署检查清单

部署前请确认:

- [ ] 服务器已安装所有必需软件
- [ ] 数据库已创建并初始化
- [ ] Redis服务已启动
- [ ] .env配置文件已创建且权限正确
- [ ] JWT Secret已更换为强随机字符串
- [ ] 数据库密码已更改为非默认值
- [ ] SSL证书已配置
- [ ] Nginx配置已测试通过
- [ ] 防火墙规则已配置
- [ ] 备份脚本已测试
- [ ] 健康检查端点返回正常
- [ ] 前端页面可以正常访问
- [ ] API接口可以正常调用

---

*本文档将随部署流程优化持续更新*
