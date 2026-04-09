#!/bin/bash

###############################################################################
# Greenly 一键部署脚本（Ubuntu 22.04）
# 用途: 在云服务器上自动部署 Greenly 植物养护系统
# 用法: sudo bash deploy.sh
###############################################################################

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 配置变量（请根据实际情况修改）
DB_PASSWORD="YourStrongPassword123!"
JWT_SECRET=$(openssl rand -hex 32)
DOMAIN_NAME=""  # 留空则使用IP访问
UPLOAD_DIR="/opt/greenly/data/uploads"
APP_DIR="/opt/greenly"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  Greenly 一键部署脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查是否以 root 权限运行
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}请使用 sudo 运行此脚本${NC}"
    exit 1
fi

# 步骤 1: 更新系统
echo -e "${YELLOW}[1/8] 更新系统...${NC}"
apt update && apt upgrade -y
echo -e "${GREEN}✓ 系统更新完成${NC}"
echo ""

# 步骤 2: 安装必需软件
echo -e "${YELLOW}[2/8] 安装必需软件...${NC}"
apt install -y openjdk-21-jdk mysql-server redis-server nginx curl wget git

# 验证安装
java -version
mysql --version
redis-cli --version
nginx -v

echo -e "${GREEN}✓ 软件安装完成${NC}"
echo ""

# 步骤 3: 配置数据库
echo -e "${YELLOW}[3/8] 配置数据库...${NC}"

# 启动 MySQL
systemctl start mysql
systemctl enable mysql

# 创建数据库和用户
mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS greenly_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'greenly'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON greenly_db.* TO 'greenly'@'localhost';
FLUSH PRIVILEGES;
EOF

echo -e "${GREEN}✓ 数据库配置完成${NC}"
echo ""

# 步骤 4: 配置 Redis
echo -e "${YELLOW}[4/8] 配置 Redis...${NC}"
systemctl start redis
systemctl enable redis
echo -e "${GREEN}✓ Redis 配置完成${NC}"
echo ""

# 步骤 5: 创建应用目录
echo -e "${YELLOW}[5/8] 创建应用目录...${NC}"
mkdir -p $APP_DIR
mkdir -p $UPLOAD_DIR
mkdir -p $APP_DIR/logs
mkdir -p $APP_DIR/frontend

chmod -R 755 $UPLOAD_DIR
echo -e "${GREEN}✓ 目录创建完成${NC}"
echo ""

# 步骤 6: 提示用户上传文件
echo -e "${YELLOW}[6/8] 请上传后端和前端文件${NC}"
echo ""
echo -e "请在本地执行以下命令："
echo -e "${GREEN}"
echo "  # 1. 打包后端"
echo "  cd plant-backend"
echo "  mvn clean package -DskipTests"
echo "  scp target/plant-backend-0.0.1-SNAPSHOT.jar root@YOUR_SERVER_IP:/opt/greenly/"
echo ""
echo "  # 2. 构建并上传前端"
echo "  cd plant-frontend"
echo "  npm run build"
echo "  tar -czf dist.tar.gz dist/"
echo "  scp dist.tar.gz root@YOUR_SERVER_IP:/opt/greenly/frontend/"
echo ""
echo "  # 3. 上传数据库初始化脚本"
echo "  scp plant-backend/src/main/resources/db/greenly-init.sql root@YOUR_SERVER_IP:/tmp/"
echo -e "${NC}"
echo ""
read -p "文件上传完成后按回车继续..."

# 导入数据库
echo -e "${YELLOW}正在导入数据库...${NC}"
mysql -u greenly -p${DB_PASSWORD} greenly_db < /tmp/greenly-init.sql
echo -e "${GREEN}✓ 数据库导入完成${NC}"
echo ""

# 解压前端文件
cd $APP_DIR/frontend
tar -xzf dist.tar.gz
echo -e "${GREEN}✓ 前端文件解压完成${NC}"
echo ""

# 步骤 7: 创建配置文件
echo -e "${YELLOW}[7/8] 创建配置文件...${NC}"

cat > $APP_DIR/application-prod.yml <<EOF
server:
  port: 8085

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/greenly_db?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: greenly
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  redis:
    host: localhost
    port: 6379
    database: 0
  
  servlet:
    multipart:
      max-file-size: 50MB
      max-request-size: 50MB

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

file:
  upload-path: ${UPLOAD_DIR}/

logging:
  file:
    name: ${APP_DIR}/logs/application.log
  level:
    com.plant.backend: INFO
EOF

echo -e "${GREEN}✓ 配置文件创建完成${NC}"
echo ""

# 步骤 8: 创建 systemd 服务
echo -e "${YELLOW}[8/8] 创建系统服务...${NC}"

cat > /etc/systemd/system/greenly.service <<EOF
[Unit]
Description=Greenly Plant Management System
After=syslog.target network.target mysql.service redis.service

[Service]
Type=simple
User=root
Group=root
WorkingDirectory=${APP_DIR}
ExecStart=/usr/bin/java -Xms512m -Xmx2g -jar ${APP_DIR}/plant-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
ExecStop=/bin/kill -SIGTERM \$MAINPID
Restart=on-failure
RestartSec=10

StandardOutput=journal
StandardError=journal
SyslogIdentifier=greenly

[Install]
WantedBy=multi-user.target
EOF

# 重载 systemd
systemctl daemon-reload
systemctl enable greenly
systemctl start greenly

echo -e "${GREEN}✓ 系统服务创建完成${NC}"
echo ""

# 配置 Nginx
echo -e "${YELLOW}配置 Nginx...${NC}"

if [ -z "$DOMAIN_NAME" ]; then
    # 使用 IP 访问
    cat > /etc/nginx/sites-available/greenly <<EOF
server {
    listen 80;
    server_name _;
    
    root ${APP_DIR}/frontend/dist;
    index index.html;

    location /api/ {
        proxy_pass http://localhost:8085/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }

    location / {
        try_files \$uri \$uri/ /index.html;
    }

    location /uploads/ {
        alias ${UPLOAD_DIR}/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
EOF
else
    # 使用域名访问
    cat > /etc/nginx/sites-available/greenly <<EOF
server {
    listen 80;
    server_name ${DOMAIN_NAME} www.${DOMAIN_NAME};
    
    root ${APP_DIR}/frontend/dist;
    index index.html;

    location /api/ {
        proxy_pass http://localhost:8085/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_read_timeout 300s;
        proxy_send_timeout 300s;
    }

    location / {
        try_files \$uri \$uri/ /index.html;
    }

    location /uploads/ {
        alias ${UPLOAD_DIR}/;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
EOF
fi

# 启用 Nginx 配置
ln -sf /etc/nginx/sites-available/greenly /etc/nginx/sites-enabled/
nginx -t
systemctl reload nginx

echo -e "${GREEN}✓ Nginx 配置完成${NC}"
echo ""

# 配置防火墙
echo -e "${YELLOW}配置防火墙...${NC}"
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw --force enable
echo -e "${GREEN}✓ 防火墙配置完成${NC}"
echo ""

# 等待后端启动
echo -e "${YELLOW}等待后端服务启动...${NC}"
sleep 10

# 检查服务状态
if systemctl is-active --quiet greenly; then
    echo -e "${GREEN}✓ 后端服务运行正常${NC}"
else
    echo -e "${RED}✗ 后端服务启动失败，请查看日志：sudo journalctl -u greenly -f${NC}"
    exit 1
fi

# 显示部署信息
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  🎉 部署完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

if [ -z "$DOMAIN_NAME" ]; then
    SERVER_IP=$(curl -s ifconfig.me)
    echo -e "访问地址: ${YELLOW}http://${SERVER_IP}${NC}"
else
    echo -e "访问地址: ${YELLOW}http://${DOMAIN_NAME}${NC}"
fi

echo ""
echo -e "${YELLOW}默认账号:${NC}"
echo -e "  用户名: admin"
echo -e "  密码: user123"
echo ""
echo -e "${RED}⚠️  重要提示:${NC}"
echo -e "  1. 请立即登录并修改默认密码"
echo -e "  2. 数据库密码: ${DB_PASSWORD}"
echo -e "  3. JWT Secret: ${JWT_SECRET}"
echo -e "  4. 请妥善保存以上敏感信息"
echo ""
echo -e "${YELLOW}常用命令:${NC}"
echo -e "  查看后端状态: sudo systemctl status greenly"
echo -e "  查看后端日志: sudo journalctl -u greenly -f"
echo -e "  重启后端: sudo systemctl restart greenly"
echo -e "  查看 Nginx 状态: sudo systemctl status nginx"
echo ""
echo -e "${GREEN}祝使用愉快！🌿${NC}"
