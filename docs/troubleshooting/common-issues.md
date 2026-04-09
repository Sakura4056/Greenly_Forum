# 🛠️ Greenly 常见问题与故障排查指南

本文档整合了项目开发过程中遇到的所有典型问题及其解决方案，按问题类型分类整理。

---

## 📋 快速导航

- [数据库相关问题](#-数据库相关问题)
- [前端相关问题](#-前端相关问题)
- [后端相关问题](#-后端相关问题)
- [登录认证问题](#-登录认证问题)
- [编译运行问题](#-编译运行问题)
- [部署相关问题](#-部署相关问题)
- [通用调试技巧](#-通用调试技巧)

---

## 💾 数据库相关问题

### 1.1 无法连接数据库

**错误现象**：
```
com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure
```

**可能原因**：
1. MySQL 服务未启动
2. 数据库名错误
3. 用户名或密码错误
4. 端口不正确

**解决方法**：

1. 检查 MySQL 服务
   ```bash
   # Windows
   net start | findstr MySQL
   
   # 或服务管理器
   services.msc
   ```

2. 验证数据库存在
   ```sql
   SHOW DATABASES;
   -- 应该包含 greenly_db
   ```

3. 测试连接
   ```bash
   mysql -u root -p123456 -e "USE greenly_db; SELECT 'OK';"
   ```

4. 检查配置文件
   ```yaml
   # application.yml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/greenly_db
       username: root
       password: 123456  # 确认密码正确
   ```

---

### 1.2 中文乱码问题

**错误现象**：
```
锟斤拷锟斤拷锟斤拷
??? 用户名或密码错误
```

**原因**: 数据库字符集配置不正确

**解决方法**：

1. 检查数据库字符集
   ```sql
   SHOW VARIABLES LIKE 'character%';
   -- character_set_server: utf8mb4
   -- character_set_database: utf8mb4
   ```

2. 检查连接 URL
   ```yaml
   url: jdbc:mysql://localhost:3306/greenly_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
   ```

3. 重新创建数据库（如需要）
   ```sql
   DROP DATABASE IF EXISTS greenly_db;
   CREATE DATABASE greenly_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

4. 重启 MySQL 服务

5. 检查文件编码
   - SQL 文件应该是 UTF-8 无 BOM 格式
   - Java 文件使用 UTF-8 编码

---

### 1.3 表不存在

**错误现象**：
```
Table 'greenly_db.sys_user' doesn't exist
```

**解决方法**：

1. 确认已导入表结构
   ```bash
   Get-Content plant-backend/target/classes/db/greenly-tables.sql | mysql -u root -p123456 greenly_db
   ```

2. 查看所有表
   ```sql
   USE greenly_db;
   SHOW TABLES;
   -- 应该显示 9 张表
   ```

3. 重新导入完整脚本
   ```bash
   mysql -u root -p123456 < plant-backend/src/main/resources/db/greenly-init.sql
   ```

---

### 1.4 数据库字段缺失

**问题**: 运行时报错提示某个字段不存在  
**原因**: 数据库表结构不完整  
**解决**: 
```bash
# 重新执行完整的数据库脚本
mysql -u root -p123456 < plant-backend/src/main/resources/db/greenly-init.sql
```

---

### 1.5 密码验证失败

**错误现象**：
```
Encoded password does not look like BCrypt
用户名或密码错误
```

**原因**: BCrypt 版本不兼容或密码格式错误

**解决方法**：

1. 确保使用统一的 BCrypt 实现
2. 密码长度不少于 8 位
3. 重新生成测试用户密码

快速修复：
```sql
UPDATE sys_user 
SET password = '$2a$10$IudmynuZAW0HWoYWQppdZOPHbp1JJ8lVWNPBd4KPhUyVHc1TuucDy'
WHERE username = 'admin';
```

详见 [密码加密修复指南](../PASSWORD_ENCRYPTION_FIX.md)（如该文档仍存在）

---

## 🎨 前端相关问题

### 2.1 图标显示异常

**问题**: Element Plus 图标不显示  
**原因**: 图标库未正确注册  
**解决**:
```javascript
// main.js 中全局注册
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
```

---

### 2.2 组件警告

**问题**: Vue 组件出现运行时警告  
**原因**: Props 类型不匹配或缺少 required 属性  
**解决**:
- 检查 Props 定义
- 添加默认值
- 确保传递正确的数据类型

---

### 2.3 构建失败 - 资源文件丢失

**问题**: `ENOENT: no such file or directory`  
**原因**: 引用的图片或静态资源不存在  
**解决**:
1. 检查文件路径是否正确
2. 创建缺失的资源文件
3. 或使用占位图替代

---

### 2.4 照片查看 403 错误

**问题**: 访问上传的照片时返回 403 Forbidden  
**原因**: 静态资源访问权限配置不当  
**解决**: 查看归档文档 [2026-04-03-photo-view-403-fix.md](fixes-archive/2026-04-03-photo-view-403-fix.md)

---

## 🔧 后端相关问题

### 3.1 编译错误 - 重复类定义

**问题**: `需要 class/interface/enum`  
**原因**: Java 文件中存在重复的 package 声明  
**解决**:
- 检查文件末尾是否有重复代码
- 清理并重新编译
```bash
cd plant-backend
mvn clean compile
```

---

### 3.2 Redis 连接失败

**问题**: `RedisConnectionFailureException`  
**原因**: Redis 服务未启动或配置错误  
**解决**:
```bash
# 检查 Redis 状态
redis-cli ping  # 应返回 PONG

# 启动 Redis（Windows）
redis-server

# 检查 application.yml 配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

---

### 3.3 AOP 切面不生效

**问题**: 缓存注解未生效  
**原因**: Spring AOP 配置不完整  
**解决**:
1. 确认已添加 `spring-boot-starter-aop` 依赖
2. 确保 Service 类被 Spring 管理
3. 检查方法访问修饰符为 public

---

### 3.4 养护计划添加错误

**问题**: 添加养护计划时失败  
**解决**: 查看归档文档 [2026-04-03-schedule-add-error-fix.md](fixes-archive/2026-04-03-schedule-add-error-fix.md)

---

### 3.5 我的植物模块问题

**问题**: 我的植物列表不显示或功能异常  
**解决**: 
- 查看归档文档 [2026-04-03-my-plant-module-fix.md](fixes-archive/2026-04-03-my-plant-module-fix.md)
- 查看归档文档 [2026-04-03-my-plant-list-not-showing-fix.md](fixes-archive/2026-04-03-my-plant-list-not-showing-fix.md)

---

### 3.6 记录添加错误

**问题**: 添加养护记录时失败  
**解决**: 查看归档文档 [2026-04-03-record-add-error-fix.md](fixes-archive/2026-04-03-record-add-error-fix.md)

---

## 🔐 登录认证问题

### 4.1 登录失败

**错误现象**：
```json
{
  "code": 400,
  "msg": "用户名或密码错误"
}
```

**排查步骤**：

1. 验证用户存在
   ```sql
   SELECT username, role FROM sys_user WHERE username='admin';
   ```

2. 检查密码哈希
   ```sql
   SELECT username, LENGTH(password), SUBSTRING(password, 1, 7) 
   FROM sys_user WHERE username='admin';
   -- 长度应该是 60，前缀是 $2a$10$
   ```

3. 查看后端日志
   ```
   搜索 "Business Exception"
   查看具体的验证过程
   ```

4. 测试 API
   ```bash
   curl -X POST http://localhost:8085/api/user/login \
     -H "Content-Type: application/json" \
     -d '{"username":"admin","password":"admin123"}'
   ```

---

### 4.2 Token 失效

**错误现象**：
```
401 Unauthorized
Token expired or invalid
```

**解决方法**：

1. 检查 Token 是否过期
   - 默认有效期：24 小时
   - 重新登录获取新 Token

2. 验证 JWT 密钥配置
   ```yaml
   # application.yml
   jwt:
     secret: your-secret-key-here
     expiration: 86400000  # 24 小时
   ```

3. 清除浏览器缓存
   - 删除 localStorage 中的 token
   - 重新登录

---

### 4.3 权限不足

**错误现象**：
```
403 Forbidden
Access Denied
```

**解决方法**：

1. 检查用户角色
   ```sql
   SELECT username, role FROM sys_user WHERE username='your-username';
   ```

2. 确认接口权限要求
   ```java
   @PreAuthorize("hasRole('ADMIN')")  // 需要管理员权限
   ```

3. 使用有权限的账号登录

---

## 💻 编译运行问题

### 5.1 Maven 编译失败

**错误现象**：
```
[ERROR] COMPILATION ERROR
cannot find symbol
```

**解决方法**：

1. 清理并重新编译
   ```bash
   cd plant-backend
   mvn clean compile
   ```

2. 更新依赖
   ```bash
   mvn dependency:purge-local-repository
   mvn clean install
   ```

3. 检查 Java 版本
   ```bash
   java -version
   # 应该是 Java 21
   ```

4. 查看具体错误信息
   ```bash
   mvn clean compile -X
   ```

---

### 5.2 端口被占用

**错误现象**：
```
Port 8085 was already in use
```

**解决方法**：

1. 查找占用进程
   ```bash
   # Windows
   netstat -ano | findstr :8085
   taskkill /F /PID <PID>
   
   # 或使用 PowerShell
   Get-Process -Id (Get-NetTCPConnection -LocalPort 8085).OwningProcess
   ```

2. 修改端口
   ```yaml
   # application.yml
   server:
     port: 8086  # 改用其他端口
   ```

3. 重启服务

---

### 5.3 依赖下载失败

**错误现象**：
```
Could not resolve dependencies
Failed to read artifact descriptor
```

**解决方法**：

1. 检查网络连接
   ```bash
   ping repo.maven.apache.org
   ```

2. 使用国内镜像
   ```xml
   <!-- pom.xml -->
   <mirror>
     <id>aliyun</id>
     <mirrorOf>central</mirrorOf>
     <url>https://maven.aliyun.com/repository/central</url>
   </mirror>
   ```

3. 强制更新
   ```bash
   mvn clean install -U
   ```

4. 离线模式（如已有依赖）
   ```bash
   mvn clean package -o
   ```

---

## 🚀 部署相关问题

### 6.1 跨域问题

**问题**: 前端调用后端 API 报 CORS 错误  
**解决**:
```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

---

### 6.2 Radio 组件警告

**问题**: Element Plus Radio 组件出现 label 警告  
**解决**: 查看归档文档 [2026-04-03-radio-label-warning-fix.md](fixes-archive/2026-04-03-radio-label-warning-fix.md)

---

## 🔍 通用调试技巧

### 1. 分步验证流程

```bash
# 1. 数据库连接
mysql -u root -p123456 -e "SELECT 'DB OK';"

# 2. 后端启动
cd plant-backend; mvn spring-boot:run

# 3. 前端启动
cd plant-frontend; npm run dev

# 4. API 测试
curl http://localhost:8085/actuator
```

---

### 2. 查看日志

**后端日志**：
```bash
# 控制台输出
# 或日志文件：plant-backend/logs/application.log
```

**前端日志**：
- 浏览器 F12 → Console
- Network 标签查看 API 请求

**MySQL 日志**：
```sql
SHOW VARIABLES LIKE 'log_error';
```

---

### 3. 环境变量检查

```bash
# Java 版本
java -version

# Maven 版本
mvn -version

# MySQL 版本
mysql --version

# Node.js 版本
node -v
```

---

### 4. 清理缓存

```bash
# Maven 清理
cd plant-backend
mvn clean

# npm 清理
cd plant-frontend
npm cache clean --force

# Redis 清理
redis-cli FLUSHALL

# 删除 target 和 node_modules（如需要）
rm -rf plant-backend/target
rm -rf plant-frontend/node_modules
npm install
```

---

## 📞 获取帮助

### 提问前准备

1. ✅ 详细的错误描述
2. ✅ 完整的错误日志
3. ✅ 已尝试的解决方法
4. ✅ 环境信息

### 环境信息模板

```markdown
- 操作系统：Windows 11
- Java 版本：21.0.10
- Maven 版本：3.9.6
- MySQL 版本：8.0.36
- Node.js 版本：v20.11.0
- 项目版本：Greenly v1.0
```

---

## 📚 相关文档

- [数据库快速参考](../DATABASE_QUICK_REFERENCE.md) - 数据库基本信息
- [数据库完整文档](../DATABASE_COMPLETE_GUIDE.md) - 详细的数据库使用指南
- [后端开发指南](../BACKEND_DEVELOPMENT.md) - Spring Boot 开发
- [前端开发指南](../FRONTEND_DEVELOPMENT.md) - Vue 3 开发

---

## 📝 历史修复记录

详细的故障修复过程已归档至 [fixes-archive/](fixes-archive/) 目录：

- [照片查看 403 修复](fixes-archive/2026-04-03-photo-view-403-fix.md)
- [养护计划添加错误修复](fixes-archive/2026-04-03-schedule-add-error-fix.md)
- [Radio 组件警告修复](fixes-archive/2026-04-03-radio-label-warning-fix.md)
- [养护记录添加错误修复](fixes-archive/2026-04-03-record-add-error-fix.md)
- [我的植物模块修复](fixes-archive/2026-04-03-my-plant-module-fix.md)
- [我的植物列表不显示修复](fixes-archive/2026-04-03-my-plant-list-not-showing-fix.md)

---

**最后更新**: 2026-04-03  
**维护者**: Greenly 开发团队
