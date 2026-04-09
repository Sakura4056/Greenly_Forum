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
# 安装必需软件（Ubuntu）
sudo apt update
sudo apt install -y openjdk-21-jdk mysql-server redis-server nginx

# 启动服务
sudo systemctl start mysql redis nginx
sudo systemctl enable mysql redis nginx
```

### 2️⃣ 数据库初始化

```bash
# 创建数据库
mysql -u root -e "CREATE DATABASE greenly_db CHARACTER SET utf8mb4;"

# 导入数据
mysql -u root greenly_db < plant-backend/src/main/resources/db/greenly-init.sql
```

### 3️⃣ 部署后端

```bash
# 打包
cd plant-backend
mvn clean package -DskipTests

# 上传到服务器
scp target/plant-backend-0.0.1-SNAPSHOT.jar root@server:/opt/greenly/

# 启动
nohup java -jar /opt/greenly/plant-backend-0.0.1-SNAPSHOT.jar &
```

### 4️⃣ 部署前端

```bash
# 构建
cd plant-frontend
npm run build

# 上传
scp -r dist/* root@server:/var/www/html/
```

### 5️⃣ 配置 Nginx

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    location /api/ {
        proxy_pass http://localhost:9090/api/;
    }
    
    location / {
        root /var/www/html;
        try_files $uri $uri/ /index.html;
    }
}
```

---

## 📊 服务器配置建议

| 用户数 | CPU | 内存 | 带宽 | 月费用 |
|--------|-----|------|------|--------|
| 10-30人 | 2核 | 4GB | 3Mbps | ¥100-200 |
| 30-100人 | 4核 | 8GB | 5Mbps | ¥300-500 |
| 100+人 | 8核 | 16GB | 10Mbps | ¥800+ |

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
sudo systemctl status greenly
sudo systemctl status mysql
sudo systemctl status redis
sudo systemctl status nginx

# 查看日志
sudo journalctl -u greenly -f
sudo tail -f /var/log/nginx/error.log

# 重启服务
sudo systemctl restart greenly
sudo systemctl restart nginx

# 备份数据库
mysqldump -u greenly -p greenly_db > backup_$(date +%Y%m%d).sql
```

---

## 🆘 故障排查

### 后端无法启动
```bash
sudo journalctl -u greenly -n 50
```

### 前端无法访问后端
```bash
# 检查 Nginx 配置
sudo nginx -t
sudo systemctl reload nginx

# 测试后端
curl http://localhost:9090/actuator/health
```

### 数据库连接失败
```bash
# 检查 MySQL 状态
sudo systemctl status mysql

# 测试连接
mysql -u greenly -p greenly_db -e "SELECT 1"
```

---

## 📞 获取帮助

- 📖 [完整部署指南](CLOUD_DEPLOYMENT_GUIDE.md)
- 🔍 [常见问题](../troubleshooting/common-issues.md)
- 🐛 提交 Issue

---

*快速参考卡 - 最后更新: 2026-04-09*
