# 🛠️ Greenly 常见问题与故障排查指南

**最后更新**: 2026-04-09

---

## 📋 快速导航

- [数据库相关问题](#数据库相关问题)
- [前端相关问题](#前端相关问题)
- [后端相关问题](#后端相关问题)
- [登录认证问题](#登录认证问题)
- [编译运行问题](#编译运行问题)
- [部署相关问题](#部署相关问题)
- [通用调试技巧](#通用调试技巧)

---

## 💾 数据库相关问题

### 1.1 无法连接数据库

**错误现象**：
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**排查步骤**：

```bash
# 1. 检查 MySQL 是否运行
systemctl status mysqld          # 阿里云 Linux
# systemctl status mysql         # Ubuntu/Debian

# 2. 验证数据库存在
mysql -u root -p -e "SHOW DATABASES;" | grep greenly_db

# 3. 测试连接
mysql -u root -p -e "USE greenly_db; SELECT 'OK';"

# 4. 检查端口
ss -tlnp | grep 3306
```

**常见原因**：
- MySQL 服务未启动
- 数据库名错误
- 用户名或密码错误
- 端口不正确

---

### 1.2 中文乱码问题

**错误现象**：
```
锟斤拷锟斤拷锟斤拷
??? 用户名或密码错误
```

**解决方法**：

```bash
# 1. 检查字符集
mysql -e "SHOW VARIABLES LIKE 'character%';"
# character_set_server 应为 utf8mb4

# 2. 检查连接 URL（application.yml）
# 确认包含: useUnicode=true&characterEncoding=utf-8

# 3. 如需重建数据库
mysql -u root -p -e "
DROP DATABASE IF EXISTS greenly_db;
CREATE DATABASE greenly_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
"
# 然后重新导入数据
mysql -u root -p greenly_db < plant-backend/src/main/resources/db/greenly-init.sql
```

---

### 1.3 表不存在

**错误现象**：
```
Table 'greenly_db.sys_user' doesn't exist
```

**解决方法**：

```bash
# 重新导入完整脚本
mysql -u root -p greenly_db < plant-backend/src/main/resources/db/greenly-init.sql

# 验证表数量
mysql -u root -p -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='greenly_db';"
# 应返回 15
```

---

### 1.4 密码验证失败

**错误现象**：
```
Encoded password does not look like BCrypt
```

**解决方法**：

1. 确保密码使用 BCrypt 加密存储（60 位，以 `$2a$10$` 开头）
2. 检查数据库中的密码格式：
   ```sql
   SELECT username, LENGTH(password), SUBSTRING(password, 1, 7) FROM sys_user;
   ```

---

## 🎨 前端相关问题

### 2.1 图标显示异常

**问题**：Element Plus 图标不显示

**解决**：确认 `main.js` 中全局注册了图标：

```javascript
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
```

---

### 2.2 构建失败 - 资源文件丢失

**问题**：`ENOENT: no such file or directory`

**解决**：
```bash
# 检查文件路径
ls -la plant-frontend/src/assets/

# 重新安装依赖
cd plant-frontend && rm -rf node_modules && npm install
```

---

### 2.3 照片查看 403 错误

**问题**：访问上传的照片返回 403

**解决**：检查 Nginx 的 `uploads` 配置：

```nginx
location /uploads/ {
    alias /opt/new_new_Greenly/data/uploads/;
    expires 7d;
}
```

确保目录权限正确：

```bash
chmod 755 /opt/new_new_Greenly/data/uploads/
```

---

## 🔧 后端相关问题

### 3.1 编译错误

```bash
# 清理并重新编译
cd plant-backend
mvn clean compile
```

---

### 3.2 Redis 连接失败

**问题**：`RedisConnectionFailureException`

```bash
# 检查 Redis 状态
redis-cli ping    # 应返回 PONG

# 启动 Redis
systemctl start redis
```

检查 `application.yml`：
```yaml
spring.data.redis.host: localhost
spring.data.redis.port: 6379
```

---

### 3.3 AOP 切面不生效

1. 确认 `spring-boot-starter-aop` 依赖已添加
2. Service 类需被 Spring 管理
3. 方法访问修饰符应为 `public`

---

### 3.4 SLF4J 多绑定警告

**现象**：
```
SLF4J(W): Class path contains multiple SLF4J providers
```

**影响**：无功能影响，但日志可能不一致。

**解决**：在 `pom.xml` 中排除多余的 SLF4J 实现。

---

## 🔐 登录认证问题

### 4.1 登录失败

```json
{"code": 400, "msg": "用户名或密码错误"}
```

**排查**：

```bash
# 1. 验证用户存在
mysql -u root -p -e "SELECT username, role FROM greenly_db.sys_user WHERE username='admin';"

# 2. 测试 API
curl -X POST http://localhost:9090/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 3. 查看日志
journalctl -u greenly-backend -n 20 --no-pager
```

---

### 4.2 Token 失效 (401)

- 默认有效期：24 小时
- 清除浏览器 localStorage 中的 token，重新登录
- 检查 JWT 密钥配置是否一致

---

### 4.3 权限不足 (403)

```sql
-- 检查用户角色
SELECT username, role FROM sys_user WHERE username='your-username';
```

管理员接口需要 `ADMIN` 角色。

---

## 💻 编译运行问题

### 5.1 Maven 编译失败

```bash
cd plant-backend
mvn clean install -U          # 强制更新依赖
# 或离线模式
mvn clean package -o
```

检查 Java 版本：
```bash
java -version    # 应为 Java 21
```

---

### 5.2 端口被占用

```
Port 9090 was already in use
```

```bash
# 查找占用进程
ss -tlnp | grep 9090

# 杀掉进程
kill -9 $(lsof -t -i:9090)
```

---

### 5.3 依赖下载失败

```bash
# 使用阿里云 Maven 镜像
# 在 ~/.m2/settings.xml 中配置：
```

```xml
<mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/central</url>
</mirror>
```

---

## 🚀 部署相关问题

### 6.1 跨域问题 (CORS)

确认后端 CORS 配置正确。检查 `CorsConfig.java` 是否允许前端域名。

---

### 6.2 文件上传超过限制

```yaml
# application.yml
spring.servlet.multipart:
  max-file-size: 10MB
  max-request-size: 50MB
```

```nginx
# nginx.conf
client_max_body_size 50M;
```

---

## 🔍 通用调试技巧

### 分步验证

```bash
# 1. 数据库
mysql -u root -p -e "SELECT 'DB OK';"

# 2. Redis
redis-cli ping

# 3. 后端
curl -s http://localhost:9090/actuator/health

# 4. 前端
curl -s -o /dev/null -w "%{http_code}" https://zhang0903.top
```

### 清理缓存

```bash
# Maven
cd plant-backend && mvn clean

# npm
cd plant-frontend && rm -rf node_modules && npm install

# Redis
redis-cli FLUSHALL
```

---

## 📚 相关文档

- [数据库快速参考](../guides/DATABASE_QUICK_REFERENCE.md)
- [部署指南](../guides/DEPLOYMENT.md)
- [云部署指南](../guides/CLOUD_DEPLOYMENT_GUIDE.md)
- [数据库备份指南](../guides/DATABASE_BACKUP_GUIDE.md)
- [Redis 配置](../guides/REDIS_SETUP_GUIDE.md)

---

*v2.0 — 2026-04-09 更新：移除失效归档引用，统一 Linux 命令，适配阿里云环境*
