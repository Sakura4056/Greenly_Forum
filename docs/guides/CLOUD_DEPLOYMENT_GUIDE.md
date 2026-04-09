# 🚀 Greenly 云服务器部署完整指南（支持多人在线）

**适用场景**: 阿里云/腾讯云/华为云等云服务器  
**预计时间**: 30-60 分钟  
**支持并发**: 50-200+ 用户（取决于服务器配置）

---

## 📋 目录

1. [云服务器选购建议](#云服务器选购建议)
2. [环境准备](#环境准备)
3. [快速部署步骤](#快速部署步骤)
4. [配置域名与HTTPS](#配置域名与https)
5. [性能优化](#性能优化)
6. [安全加固](#安全加固)
7. [监控与维护](#监控与维护)
8. [常见问题](#常见问题)

---

## 💻 云服务器选购建议

### 推荐配置（按用户规模）

| 用户规模 | CPU | 内存 | 带宽 | 硬盘 | 月费用估算 |
|---------|-----|------|------|------|-----------|
| **小型** (10-30人) | 2核 | 4GB | 3-5Mbps | 50GB SSD | ¥100-200 |
| **中型** (30-100人) | 4核 | 8GB | 5-10Mbps | 100GB SSD | ¥300-500 |
| **大型** (100-500人) | 8核 | 16GB | 10-20Mbps | 200GB SSD | ¥800-1500 |

### 推荐云服务商

- **阿里云**: ECS 实例，稳定性好，国内访问快
- **腾讯云**: CVM 实例，性价比高
- **华为云**: ECS 实例，企业级服务
- **AWS**: EC2 实例，国际化项目首选

### 操作系统选择

✅ **推荐**: Ubuntu 22.04 LTS / CentOS 8  
❌ **不推荐**: Windows Server（资源占用高）

---

## 🔧 环境准备

### 1. 连接服务器

```bash
# SSH 登录（替换为你的服务器IP）
ssh root@your-server-ip

# 首次登录建议修改密码
passwd
```

### 2. 安装必需软件

#### Ubuntu/Debian

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装 Java 21
sudo apt install openjdk-21-jdk -y
java -version  # 验证安装

# 安装 MySQL 8.0
sudo apt install mysql-server -y
sudo systemctl start mysql
sudo systemctl enable mysql

# 安装 Redis
sudo apt install redis-server -y
sudo systemctl start redis
sudo systemctl enable redis

# 安装 Nginx
sudo apt install nginx -y
sudo systemctl start nginx
sudo systemctl enable nginx

# 安装 Node.js（仅构建时需要）
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y
node -v  # 验证安装
```

#### CentOS/RHEL

```bash
# 安装 EPEL 源
sudo yum install epel-release -y

# 安装 Java 21
sudo yum install java-21-openjdk-devel -y

# 安装 MySQL 8.0
sudo yum install mysql-server -y
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 安装 Redis
sudo yum install redis -y
sudo systemctl start redis
sudo systemctl enable redis

# 安装 Nginx
sudo yum install nginx -y
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 3. 配置防火墙

```bash
# Ubuntu (UFW)
sudo ufw allow 22/tcp    # SSH
sudo ufw allow 80/tcp    # HTTP
sudo ufw allow 443/tcp   # HTTPS
sudo ufw enable

# CentOS (firewalld)
sudo firewall-cmd --permanent --add-service=ssh
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --reload
```

---

## 🚀 快速部署步骤

### 第一步：数据库初始化

```bash
# 1. 登录 MySQL
sudo mysql

# 2. 创建数据库和用户
CREATE DATABASE greenly_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'greenly'@'localhost' IDENTIFIED BY 'YourStrongPassword123!';
GRANT ALL PRIVILEGES ON greenly_db.* TO 'greenly'@'localhost';
FLUSH PRIVILEGES;
EXIT;

# 3. 上传 SQL 文件到服务器
# 在本地执行：
scp plant-backend/src/main/resources/db/greenly-init.sql root@your-server-ip:/tmp/

# 4. 在服务器上导入
mysql -u greenly -p greenly_db < /tmp/greenly-init.sql

# 5. 验证
mysql -u greenly -p -e "USE greenly_db; SHOW TABLES;"
```

### 第二步：部署后端

```bash
# 1. 创建应用目录
sudo mkdir -p /opt/greenly
cd /opt/greenly

# 2. 上传后端 JAR 包
# 在本地执行：
cd plant-backend
mvn clean package -DskipTests
scp target/plant-backend-0.0.1-SNAPSHOT.jar root@your-server-ip:/opt/greenly/

# 3. 创建配置文件
sudo nano /opt/greenly/application-prod.yml
```

粘贴以下内容（**修改敏感信息**）:

```yaml
server:
  port: 9090

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/greenly_db?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: greenly
    password: YourStrongPassword123!
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  redis:
    host: localhost
    port: 6379
    database: 0
  
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

# JWT 配置（生成随机字符串）
jwt:
  secret: $(openssl rand -hex 32)  # 替换为实际生成的值
  expiration: 86400000  # 24小时

# 文件上传路径
file:
  upload-path: /opt/greenly/data/uploads/

# 百度 AI 配置（如使用）
baidu:
  ai:
    app-id: your_app_id
    api-key: your_api_key
    secret-key: your_secret_key

# 邮件配置（如使用）
spring:
  mail:
    host: smtp.163.com
    port: 465
    username: your_email@163.com
    password: your_smtp_auth_code
    properties:
      mail:
        smtp:
          ssl:
            enable: true

logging:
  file:
    name: /opt/greenly/logs/application.log
  level:
    com.plant.backend: INFO
```

```bash
# 4. 创建上传目录
sudo mkdir -p /opt/greenly/data/uploads
sudo chmod -R 755 /opt/greenly/data/uploads

# 5. 创建日志目录
sudo mkdir -p /opt/greenly/logs
```

### 第三步：创建 systemd 服务

```bash
sudo nano /etc/systemd/system/greenly.service
```

粘贴以下内容:

```ini
[Unit]
Description=Greenly Plant Management System
After=syslog.target network.target mysql.service redis.service

[Service]
Type=simple
User=root
Group=root
WorkingDirectory=/opt/greenly
ExecStart=/usr/bin/java -Xms512m -Xmx2g -jar /opt/greenly/plant-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
ExecStop=/bin/kill -SIGTERM $MAINPID
Restart=on-failure
RestartSec=10

StandardOutput=journal
StandardError=journal
SyslogIdentifier=greenly

[Install]
WantedBy=multi-user.target
```

启动服务:

```bash
sudo systemctl daemon-reload
sudo systemctl enable greenly
sudo systemctl start greenly

# 查看状态
sudo systemctl status greenly

# 查看日志
sudo journalctl -u greenly -f
```

### 第四步：部署前端

```bash
# 1. 在本地构建前端
cd plant-frontend

# 创建生产环境配置
cat > .env.production << EOF
VITE_API_BASE_URL=https://your-domain.com/api
EOF

# 构建
npm install
npm run build

# 2. 上传到服务器
tar -czf dist.tar.gz dist/
scp dist.tar.gz root@your-server-ip:/opt/greenly/

# 3. 在服务器上解压
cd /opt/greenly
sudo mkdir -p frontend
cd frontend
sudo tar -xzf ../dist.tar.gz
```

### 第五步：配置 Nginx

```bash
sudo nano /etc/nginx/sites-available/greenly
```

粘贴以下内容（**替换 `your-domain.com` 为你的域名或服务器IP**）:

```nginx
# HTTP 配置（如果使用域名，稍后会自动跳转到HTTPS）
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    
    # 临时允许HTTP访问（配置HTTPS后再注释掉）
    root /opt/greenly/frontend/dist;
    index index.html;

    # API 反向代理
    location /api/ {
        proxy_pass http://localhost:9090/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }

    # 静态文件
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 上传文件访问
    location /uploads/ {
        alias /opt/greenly/data/uploads/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
```

启用配置:

```bash
sudo ln -s /etc/nginx/sites-available/greenly /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 第六步：测试访问

```bash
# 1. 检查后端健康状态
curl http://localhost:9090/actuator/health

# 2. 浏览器访问
# http://your-server-ip 或 http://your-domain.com
```

---

## 🔒 配置域名与HTTPS

### 1. 绑定域名

在云服务控制台：
1. 购买域名（如 `greenly.example.com`）
2. 添加 A 记录，指向服务器 IP
3. 等待 DNS 生效（通常几分钟到几小时）

### 2. 申请 SSL 证书（Let's Encrypt 免费）

```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx -y

# 获取证书
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# 按提示操作：
# 1. 输入邮箱
# 2. 同意条款
# 3. 选择是否重定向到HTTPS（选2：强制HTTPS）
```

自动续期测试:

```bash
sudo certbot renew --dry-run
```

### 3. 验证 HTTPS

访问 `https://your-domain.com`，检查浏览器地址栏是否显示🔒锁图标。

---

## ⚡ 性能优化

### 1. JVM 参数优化

编辑 `/etc/systemd/system/greenly.service`:

```ini
# 根据服务器内存调整
ExecStart=/usr/bin/java \
  -Xms1g \
  -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/opt/greenly/logs/heapdump.hprof \
  -jar /opt/greenly/plant-backend-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

重启服务:

```bash
sudo systemctl daemon-reload
sudo systemctl restart greenly
```

### 2. MySQL 优化

编辑 `/etc/mysql/mysql.conf.d/mysqld.cnf`:

```ini
[mysqld]
# 字符集
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci

# 连接数（根据并发用户数调整）
max_connections = 200

# InnoDB 缓冲池（建议为物理内存的50-70%）
innodb_buffer_pool_size = 2G

# 日志文件
innodb_log_file_size = 512M

# 慢查询日志
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
```

重启 MySQL:

```bash
sudo systemctl restart mysql
```

### 3. Nginx 优化

编辑 `/etc/nginx/nginx.conf`:

```nginx
http {
    # 开启 Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml+rss application/json application/javascript;

    # 客户端最大上传大小
    client_max_body_size 50M;

    # 缓存配置
    proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=my_cache:10m max_size=1g inactive=60m use_temp_path=off;
}
```

重启 Nginx:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

### 4. Redis 缓存优化

确保 Redis 已启用，后端会自动使用缓存提升性能。

---

## 🛡️ 安全加固

### 1. 修改默认密码

```sql
-- 登录 MySQL
mysql -u root -p

-- 修改 greenly 用户密码
ALTER USER 'greenly'@'localhost' IDENTIFIED BY 'NewStrongPassword123!';
FLUSH PRIVILEGES;
```

同步更新后端配置:

```bash
sudo nano /opt/greenly/application-prod.yml
# 修改 spring.datasource.password
sudo systemctl restart greenly
```

### 2. 禁止 root 远程登录 MySQL

```sql
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');
FLUSH PRIVILEGES;
```

### 3. 配置防火墙规则

```bash
# 只开放必要端口
sudo ufw allow 22/tcp   # SSH
sudo ufw allow 80/tcp   # HTTP
sudo ufw allow 443/tcp  # HTTPS
sudo ufw deny 3306/tcp  # 禁止外部访问MySQL
sudo ufw deny 6379/tcp  # 禁止外部访问Redis
sudo ufw enable
```

### 4. 定期备份

创建备份脚本 `/opt/greenly/scripts/backup.sh`:

```bash
#!/bin/bash

BACKUP_DIR="/opt/greenly/backups"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="greenly_db"
DB_USER="greenly"
DB_PASS="YourStrongPassword123!"

mkdir -p $BACKUP_DIR

# 备份数据库
mysqldump -u $DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/db_$DATE.sql

# 备份上传文件
tar -czf $BACKUP_DIR/uploads_$DATE.tar.gz /opt/greenly/data/uploads/

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +7 -delete

echo "Backup completed: $DATE"
```

设置定时任务:

```bash
chmod +x /opt/greenly/scripts/backup.sh

# 每天凌晨2点备份
crontab -e
# 添加: 0 2 * * * /opt/greenly/scripts/backup.sh >> /opt/greenly/logs/backup.log 2>&1
```

### 5. 更新系统

```bash
# 每周更新一次
sudo apt update && sudo apt upgrade -y
sudo reboot  # 如有内核更新
```

---

## 📊 监控与维护

### 1. 查看服务状态

```bash
# 后端服务
sudo systemctl status greenly

# 数据库
sudo systemctl status mysql

# Redis
sudo systemctl status redis

# Nginx
sudo systemctl status nginx
```

### 2. 查看日志

```bash
# 后端日志
sudo journalctl -u greenly -f

# Nginx 访问日志
sudo tail -f /var/log/nginx/access.log

# Nginx 错误日志
sudo tail -f /var/log/nginx/error.log

# MySQL 慢查询日志
sudo tail -f /var/log/mysql/slow.log
```

### 3. 监控系统资源

```bash
# 安装 htop
sudo apt install htop -y
htop

# 查看磁盘空间
df -h

# 查看内存使用
free -h
```

### 4. 数据库监控

```sql
-- 当前连接数
SHOW STATUS LIKE 'Threads_connected';

-- 慢查询数量
SHOW STATUS LIKE 'Slow_queries';

-- 表大小
SELECT table_name, ROUND(data_length/1024/1024, 2) AS size_mb
FROM information_schema.tables
WHERE table_schema = 'greenly_db'
ORDER BY data_length DESC;
```

---

## 🆘 常见问题

### Q1: 后端启动失败

```bash
# 查看详细错误
sudo journalctl -u greenly -n 50

# 常见原因：
# 1. 端口被占用
sudo lsof -i :9090

# 2. 数据库连接失败
mysql -u greenly -p -e "SELECT 1"

# 3. 配置文件错误
cat /opt/greenly/application-prod.yml
```

### Q2: 前端无法访问后端API

检查 Nginx 配置:

```bash
sudo nginx -t
sudo systemctl reload nginx

# 检查后端是否正常
curl http://localhost:9090/actuator/health
```

### Q3: 图片上传失败

```bash
# 检查目录权限
ls -la /opt/greenly/data/uploads/
sudo chown -R root:root /opt/greenly/data/uploads/
sudo chmod -R 755 /opt/greenly/data/uploads/

# 检查磁盘空间
df -h

# 检查 Nginx 上传限制
grep client_max_body_size /etc/nginx/nginx.conf
```

### Q4: 网站访问慢

```bash
# 检查服务器负载
htop

# 检查网络带宽
iftop

# 优化建议：
# 1. 增加服务器带宽
# 2. 启用 CDN
# 3. 优化数据库查询
# 4. 增加 Redis 缓存
```

### Q5: HTTPS 证书过期

```bash
# 手动续期
sudo certbot renew

# 检查证书有效期
sudo certbot certificates
```

---

## ✅ 部署检查清单

部署完成后，请逐项检查：

- [ ] 服务器防火墙已配置（只开放 22/80/443）
- [ ] MySQL 数据库已创建并初始化
- [ ] Redis 服务已启动
- [ ] 后端服务正常运行（`systemctl status greenly`）
- [ ] 前端页面可以访问
- [ ] API 接口正常响应
- [ ] HTTPS 证书已配置且有效
- [ ] 数据库密码已修改为强密码
- [ ] 备份脚本已配置并测试
- [ ] 日志监控正常
- [ ] 默认账号密码已修改（admin/admin123）
- [ ] 上传文件功能正常
- [ ] 多人同时访问测试通过

---

## 📞 技术支持

遇到问题时：

1. 📖 查看 [完整部署文档](DEPLOYMENT.md)
2. 🔍 搜索 [常见问题](../troubleshooting/common-issues.md)
3. 📊 查看应用日志：`sudo journalctl -u greenly -f`
4. 🐛 提交 Issue

---

## 🎉 部署完成！

现在您的 Greenly 系统已经部署到云服务器，可以供多人同时在线使用了！

**访问地址**: `https://your-domain.com`  
**默认账号**: `admin / admin123`（**请立即修改密码！**）

---

*最后更新: 2026-04-09*  
*维护者: Greenly 开发团队*
