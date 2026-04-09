# 🚀 Greenly 云部署快速参考卡

## 📦 一键部署（推荐）

```bash
# 1. 上传部署脚本到服务器
scp scripts/deploy-to-cloud.sh root@your-server-ip:/tmp/

# 2. SSH 登录服务器
ssh root@your-server-ip

# 3. 执行部署脚本
sudo bash /tmp/deploy-to-cloud.sh
```

---

## 🔧 手动部署步骤

### 1️⃣ 环境准备

```bash
# Alibaba Cloud Linux 3 / CentOS
yum install -y java-21-alibaba-dragonwell mysql-server redis nginx
systemctl start mysqld redis nginx
systemctl enable mysqld redis nginx

# Ubuntu / Debian
# apt update && apt install -y openjdk-21-jdk mysql-server redis-server nginx
```

### 2️⃣ 数据库初始化

```bash
mysql -u root -e "CREATE DATABASE greenly_db CHARACTER SET utf8mb4;"
mysql -u root greenly_db < plant-backend/src/main/resources/db/greenly-init.sql
```

### 3️⃣ 部署后端

```bash
cd plant-backend
mvn clean package -DskipTests

# 配置环境变量
cp .env.example .env
# 编辑 .env 填写数据库连接等配置

# 用 systemd 管理（推荐）
cat > /etc/systemd/system/greenly-backend.service << 'EOF'
[Unit]
Description=Greenly Plant Care Backend
After=network.target mysqld.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/new_new_Greenly/plant-backend
EnvironmentFile=/opt/new_new_Greenly/plant-backend/.env
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /opt/new_new_Greenly/plant-backend/target/plant-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable greenly-backend
systemctl start greenly-backend
```

### 4️⃣ 部署前端

```bash
cd plant-frontend
npm install
npm run build

# Nginx 直接从 dist 目录提供服务
# 无需单独拷贝，配置 root 指向 dist 即可
```

### 5️⃣ 配置 Nginx

```nginx
server {
    server_name your-domain.com;
    root /opt/new_new_Greenly/plant-frontend/dist;
    index index.html;

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
        client_max_body_size 50M;
    }

    # HTTPS（certbot 自动配置）
    listen 443 ssl;
    ssl_certificate /etc/letsencrypt/live/your-domain/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/your-domain/privkey.pem;
}
```

```bash
nginx -t && systemctl reload nginx
```

---

## 📊 服务器配置建议

| 用户数 | CPU | 内存 | 带宽 | 月费用 |
|--------|-----|------|------|--------|
| 10-30 人 | 2 核 | 4GB | 3Mbps | ¥100-200 |
| 30-100 人 | 4 核 | 8GB | 5Mbps | ¥300-500 |
| 100+ 人 | 8 核 | 16GB | 10Mbps | ¥800+ |

---

## 🔒 安全加固清单

- [ ] 修改默认密码（admin/admin123）
- [ ] 配置 HTTPS（Let's Encrypt 免费证书）
- [ ] 开启防火墙（只开放 22/80/443）
- [ ] 禁止 MySQL 远程访问
- [ ] 设置定期备份
- [ ] 更新系统补丁

---

## 🛠️ 常用命令

```bash
# 查看服务状态
systemctl status greenly-backend
systemctl status mysqld
systemctl status redis
systemctl status nginx

# 查看日志
journalctl -u greenly-backend -f
tail -f /var/log/nginx/error.log

# 重启服务
systemctl restart greenly-backend
systemctl reload nginx

# 备份数据库
mysqldump -u root -p greenly_db | gzip > backup_$(date +%Y%m%d).sql.gz
```

---

## 🆘 故障排查

**后端无法启动**：
```bash
journalctl -u greenly-backend -n 50 --no-pager
```

**前端无法访问后端**：
```bash
nginx -t
curl http://localhost:9090/actuator/health
```

**数据库连接失败**：
```bash
systemctl status mysqld
mysql -u root -p -e "SELECT 1"
```

---

## 📚 相关文档

- [完整部署指南](CLOUD_DEPLOYMENT_GUIDE.md)
- [部署指南](DEPLOYMENT.md)
- [常见问题](../troubleshooting/common-issues.md)
- [数据库备份](DATABASE_BACKUP_GUIDE.md)

---

*v2.0 — 2026-04-09 更新：适配阿里云 Linux，统一服务名和路径*
