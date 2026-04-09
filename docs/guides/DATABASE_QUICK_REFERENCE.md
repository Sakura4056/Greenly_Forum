# 🗄️ Greenly 数据库快速参考

最简洁的数据库使用指南，5 分钟快速上手。

---

## 💾 基本信息

| 项目 | 值 |
|------|-----|
| **数据库名** | `greenly_db` |
| **字符集** | utf8mb4 |
| **排序规则** | utf8mb4_unicode_ci |
| **时区** | Asia/Shanghai (+08:00) |
| **默认端口** | 3306 |

---

## 🔑 默认账号

所有账号密码均为 BCrypt 加密存储。

| 用户名   | 密码     | 角色   | 说明         |
|----------|----------|--------|--------------|
| admin    | admin123  | ADMIN  | 系统管理员   |
| user1    | admin123  | USER   | 新手用户     |
| user2    | admin123  | USER   | 资深花友     |
| gardener | admin123  | USER   | 园艺爱好者   |

> ⚠️ **安全提示**: 生产环境必须修改默认密码！

---

## 📊 数据表概览（15 张表）

### 核心业务表（5 张）
1. **sys_user** - 系统用户表
2. **official_plant** - 官方植物库（5 种植物）
3. **my_plant** - 我的植物表（3 盆示例）
4. **care_schedule** - 养护计划表（3 个示例）
5. **care_record** - 养护记录表（3 条示例）

### 辅助功能表（9 张）
6. **plant_photo** - 植物照片表（3 张示例）
7. **reminder_config** - 提醒配置表（2 个示例）
8. **reminder** - 消息提醒表（3 条示例）
9. **plant_diary** - 植物日记表（5 篇示例）✨
10. **ai_conversation** - AI 对话历史表（空表）🤖
11. **announcement** - 系统公告表
12. **forum_category** - 论坛分类表
13. **forum_post** - 论坛帖子表
14. **forum_comment** - 论坛评论表
15. **forum_like** - 论坛点赞表

---

## 🚀 一键安装

### 方式一：标准化初始化脚本（推荐）

```bash
mysql -u root -p123456 < plant-backend/src/main/resources/db/greenly-init.sql
```

执行后会：
1. 创建 `greenly_db` 数据库
2. 导入所有 15 张表结构
3. 插入初始基础数据
4. 验证安装结果并显示统计信息

### 方式二：PowerShell 自动修复脚本

```powershell
.\fix-database.ps1
```

适用于修复现有数据库的问题，会自动备份并清理测试数据。

### 方式三：手动分步执行

```bash
# 1. 创建数据库
mysql -u root -p123456 -e "CREATE DATABASE greenly_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 导入完整脚本
mysql -u root -p123456 greenly_db < plant-backend/src/main/resources/db/greenly-init.sql
```

---

## 🔧 常用 SQL 命令

### 查看数据库
```sql
SHOW DATABASES;
USE greenly_db;
SHOW TABLES;
```

### 查看数据量
```sql
SELECT 
    'sys_user' AS table_name, COUNT(*) AS count FROM sys_user
UNION ALL SELECT 'official_plant', COUNT(*) FROM official_plant
UNION ALL SELECT 'my_plant', COUNT(*) FROM my_plant
UNION ALL SELECT 'care_schedule', COUNT(*) FROM care_schedule
UNION ALL SELECT 'care_record', COUNT(*) FROM care_record;
```

### 重置管理员密码
```sql
UPDATE sys_user 
SET password = '$2a$10$IudmynuZAW0HWoYWQppdZOPHbp1JJ8lVWNPBd4KPhUyVHc1TuucDy'
WHERE username = 'admin';
```

### 清空所有数据（⚠️ 谨慎使用）
```sql
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE plant_diary;
TRUNCATE TABLE reminder;
TRUNCATE TABLE plant_photo;
TRUNCATE TABLE care_record;
TRUNCATE TABLE care_schedule;
TRUNCATE TABLE my_plant;
TRUNCATE TABLE official_plant;
TRUNCATE TABLE sys_user;
SET FOREIGN_KEY_CHECKS = 1;
```

---

## ⚙️ 后端配置

修改 `plant-backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/greenly_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
```

---

## 🆘 快速故障排查

### 无法连接数据库
```bash
# 检查 MySQL 服务
net start | findstr MySQL

# 检查数据库是否存在
mysql -u root -p123456 -e "SHOW DATABASES;"
```

### 中文乱码
```sql
-- 检查字符集
SHOW VARIABLES LIKE 'character%';
-- 应该是 utf8mb4
```

### 登录失败
```sql
-- 验证用户存在
SELECT username, role FROM sys_user WHERE username='admin';

-- 检查密码格式（应该以 $2a$10$ 开头，长度 60）
SELECT username, LENGTH(password) as len FROM sys_user;
```

### 表不存在
```bash
# 重新导入表结构
Get-Content plant-backend/target/classes/db/greenly-tables.sql | mysql -u root -p123456 greenly_db
```

---

## 📁 重要文件位置

### SQL 脚本
- `plant-backend/src/main/resources/db/greenly-init.sql` - 唯一权威的数据库初始化脚本 ⭐（15张表，完整版）
- `plant-backend/src/main/resources/db/greenly-init-zh.sql` - 中文版备份（与主版本一致）
- `plant-backend/src/main/resources/db/greenly-init-en.sql` - 英文版备份（与主版本一致）

### 安装脚本
- `fix-database.ps1` - PowerShell 自动修复脚本（推荐用于修复现有数据库）

### 配置文件
- `plant-backend/src/main/resources/application.yml` - 后端数据库配置
- `plant-frontend/.env.development` - 前端 API 配置

---

## 📞 获取帮助

### 更多信息
- **数据库设计**: [DATABASE_RESTRUCTURE_SUMMARY.md](../archive/refactoring/DATABASE_RESTRUCTURE_SUMMARY.md)
- **故障排查**: [常见问题](../troubleshooting/common-issues.md)
- **主版本脚本**: [greenly-init.sql](../../plant-backend/src/main/resources/db/greenly-init.sql) ⭐

### 归档文档
- **历史修复记录**: [archive/](archive/)

### 技术栈
- **Spring Boot**: 3.3.5
- **MyBatis Plus**: 3.5.5
- **MySQL**: 8.0+
- **BCrypt**: Spring Security

---

## 🎯 下一步

安装完成后：
1. ✅ 启动后端服务：`cd plant-backend && mvn spring-boot:run`
2. ✅ 启动前端服务：`cd plant-frontend && npm run dev`
3. ✅ 访问 http://localhost:5173
4. ✅ 使用 `admin/admin123` 登录

---

*最后更新：2026-04-09*  
*维护者：Greenly 开发团队*
