# 用户系统模块使用指南

> 📅 最后更新：2026-04-09  
> 👨‍💻 维护者：Greenly 开发团队

---

## 📋 目录

- [功能概述](#功能概述)
- [技术架构](#技术架构)
- [数据库设计](#数据库设计)
- [API 接口文档](#api 接口文档)
- [前端使用说明](#前端使用说明)
- [常见问题](#常见问题)
- [开发扩展](#开发扩展)

---

## 功能概述

用户系统模块是 Greenly 植物养护管理系统的基础核心模块，提供完整的用户身份认证和信息管理功能。

### 核心功能

✅ **用户注册** - 支持用户名、密码、邮箱、手机号注册  
✅ **用户登录** - JWT Token 认证，支持记住登录状态  
✅ **信息管理** - 修改昵称、头像、性别、生日、个性签名等  
✅ **密码管理** - 修改密码功能  
✅ **权限控制** - USER（普通用户）和 ADMIN（管理员）角色权限  
✅ **用户列表** - 管理员可查看、搜索、删除用户  

---

## 技术架构

### 后端技术栈

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 框架 | Spring Boot | 3.3.5 | 核心框架 |
| 安全 | Spring Security | 6.3.4 | 权限控制 |
| 认证 | JWT (jjwt) | 0.11.5 | Token 认证 |
| 加密 | BCrypt | - | 密码加密 |
| ORM | MyBatis Plus | 3.5.5 | 数据持久化 |
| 数据库 | MySQL | 8.0+ | 数据存储 |

### 前端技术栈

| 组件 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 框架 | Vue 3 | 3.5+ | 核心框架 |
| 状态管理 | Pinia | 3.x | 用户状态管理 |
| UI 组件 | Element Plus | 2.13+ | 界面组件 |
| HTTP | Axios | 1.13+ | 请求封装 |
| 路由 | Vue Router | 5.x | 页面路由 |

---

## 数据库设计

### sys_user 表结构

```sql
CREATE TABLE `sys_user` (
    `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码 (BCrypt 加密)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像 URL',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    `gender` TINYINT DEFAULT 0 COMMENT '性别',
    `birthday` DATE DEFAULT NULL COMMENT '生日',
    `signature` VARCHAR(200) DEFAULT NULL COMMENT '个性签名',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录 IP',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 字段说明

| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| user_id | BIGINT | 是 | 自增 | 主键 ID |
| username | VARCHAR(50) | 是 | - | 用户名（唯一） |
| password | VARCHAR(100) | 是 | - | BCrypt 加密密码 |
| nickname | VARCHAR(50) | 否 | - | 显示昵称 |
| email | VARCHAR(100) | 否 | NULL | 邮箱地址 |
| phone | VARCHAR(20) | 否 | NULL | 手机号码 |
| avatar | VARCHAR(255) | 否 | NULL | 头像 URL |
| role | VARCHAR(20) | 是 | USER | USER/ADMIN |
| status | TINYINT | 是 | 1 | 1 正常/0 禁用 |
| gender | TINYINT | 否 | 0 | 0 未知/1 男/2 女 |
| birthday | DATE | 否 | NULL | 出生日期 |
| signature | VARCHAR(200) | 否 | NULL | 个性签名 |
| last_login_time | DATETIME | 否 | NULL | 最后登录时间 |
| last_login_ip | VARCHAR(50) | 否 | NULL | 最后登录 IP |
| create_time | DATETIME | 是 | NOW | 创建时间 |
| update_time | DATETIME | 是 | NOW | 更新时间 |
| deleted | TINYINT | 是 | 0 | 0 未删除/1 已删除 |

---

## API 接口文档

### 基础信息

- **Base URL**: `http://localhost:9090/api`
- **认证方式**: JWT Bearer Token
- **请求格式**: JSON
- **响应格式**: 

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1712131200000
}
```

---

### 1. 用户注册

**POST** `/user/register`

#### 请求参数

```json
{
  "username": "zhangsan",
  "password": "123456",
  "nickname": "张三",
  "email": "zhangsan@example.com",
  "phone": "13800138000"
}
```

#### 响应示例

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "role": "USER",
    "expireTime": 1712217600000
  }
}
```

#### 错误码

| 错误码 | 说明 |
|--------|------|
| 400 | 用户名已存在/参数错误 |

---

### 2. 用户登录

**POST** `/user/login`

#### 请求参数

```json
{
  "username": "zhangsan",
  "password": "123456"
}
```

#### 响应示例

同注册接口

#### 错误码

| 错误码 | 说明 |
|--------|------|
| 400 | 用户名或密码错误 |
| 403 | 账号已被禁用 |

---

### 3. 获取当前用户信息

**GET** `/user/info`

#### 请求头

```
Authorization: Bearer <token>
```

#### 响应示例

```json
{
  "code": 200,
  "data": {
    "userId": 1,
    "username": "zhangsan",
    "nickname": "张三",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "avatar": "https://example.com/avatar.jpg",
    "role": "USER",
    "status": 1,
    "gender": 1,
    "birthday": "1990-01-01",
    "signature": "热爱生活",
    "createTime": "2026-01-01 00:00:00"
  }
}
```

---

### 4. 更新用户信息

**PUT** `/user/update`

#### 请求参数

```json
{
  "userId": 1,
  "nickname": "新昵称",
  "email": "newemail@example.com",
  "phone": "13900139000",
  "gender": 1,
  "birthday": "1990-01-01",
  "signature": "新的个性签名",
  "avatar": "https://example.com/new-avatar.jpg"
}
```

#### 权限说明

- 普通用户只能修改自己的信息
- 管理员可以修改任意用户信息
- 只有管理员可以修改角色和状态

---

### 5. 修改密码

**PUT** `/user/change-password`

#### 请求参数

```json
{
  "oldPassword": "123456",
  "newPassword": "new123456"
}
```

---

### 6. 用户列表（管理员）

**GET** `/user/list?pageNum=1&pageSize=10&keyword=张三`

#### 查询参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pageNum | Integer | 否 | 页码，默认 1 |
| pageSize | Integer | 否 | 每页数量，默认 10 |
| keyword | String | 否 | 搜索关键词 |

#### 响应示例

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "userId": 1,
        "username": "zhangsan",
        "nickname": "张三",
        "email": "zhangsan@example.com",
        "role": "USER",
        "status": 1,
        "createTime": "2026-01-01 00:00:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1
  }
}
```

---

### 7. 删除用户（管理员）

**DELETE** `/user/delete/{userId}`

#### 路径参数

| 参数 | 类型 | 说明 |
|------|------|------|
| userId | Long | 用户 ID |

---

## 前端使用说明

### 页面路由

| 路由 | 组件 | 说明 |
|------|------|------|
| `/login` | pages/login/index.vue | 登录页 |
| `/register` | pages/register/index.vue | 注册页 |
| `/user/update` | pages/user/update.vue | 个人信息页 |
| `/admin/user-list` | pages/admin/user-list.vue | 用户管理页（管理员） |

### Pinia Store 使用

```javascript
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 获取状态
console.log(userStore.isLoggedIn) // 是否登录
console.log(userStore.isAdmin)    // 是否管理员
console.log(userStore.userInfo)   // 完整用户信息

// 设置登录信息
userStore.setLoginInfo(response.data)

// 获取用户信息
await userStore.fetchUserInfo()

// 退出登录
userStore.logout()
```

### API 调用示例

```javascript
import { login, register, getCurrentUserInfo, updateUserInfo } from '@/api/user'

// 登录
const res = await login({ username: 'zhangsan', password: '123456' })

// 注册
const res = await register({ 
  username: 'zhangsan', 
  password: '123456',
  email: 'zhangsan@example.com'
})

// 获取当前用户信息
const res = await getCurrentUserInfo()

// 更新用户信息
await updateUserInfo({
  userId: 1,
  nickname: '新昵称',
  email: 'new@example.com'
})
```

---

## 常见问题
A: 当前版本暂不支持在线找回密码，请联系管理员重置密码。

### Q2: 用户名可以修改吗？

A: 出于安全考虑，用户名注册后不可修改。

### Q3: 如何成为管理员？

A: 需要联系现有管理员在后台修改角色权限。

### Q4: Token 有效期多久？

A: Token 有效期为 24 小时，过期后需要重新登录。

### Q5: 如何保持登录状态？

A: 登录时勾选"记住我"选项，系统会自动刷新 Token。

### Q6: 账号被禁用了怎么办？

A: 请联系管理员了解禁用原因并申请解封。

---

## 开发扩展

### 添加新的用户字段

1. **修改数据库表**

```sql
ALTER TABLE sys_user ADD COLUMN new_field VARCHAR(100) DEFAULT NULL COMMENT '新字段';
```

2. **更新 Entity**

```java
@TableField
private String newField;
```

3. **更新 DTO**

```java
public static class UpdateRequest {
    private String newField;
}

public static class Info {
    private String newField;
}
```

4. **更新 Service**

```java
if (request.getNewField() != null) {
    user.setNewField(request.getNewField());
}
```

### 集成第三方登录

预留扩展点，可集成：
- 微信登录
- QQ 登录
- GitHub 登录
- Google 登录
---

## 测试用例

运行测试：

```bash
cd plant-backend
mvn test -Dtest=UserServiceTest
```

测试覆盖率目标：**80%+**

---

## 更新日志

### v1.0.0 (2026-04-03)

- ✅ 完成用户注册功能
- ✅ 完成用户登录功能
- ✅ 完成信息管理功能
- ✅ 完成密码修改功能
- ✅ 完成管理员用户管理功能
- ✅ 完善单元测试
- ✅ 编写使用文档

---

## 相关文档

- [数据库设计文档](./DATABASE_DESIGN.md)
- [后端开发指南](./BACKEND_DEVELOPMENT.md)
- [前端开发指南](./FRONTEND_DEVELOPMENT.md)
- [API 接口规范](./API_SPECIFICATION.md)

---

**📞 技术支持**: 如有问题请查看项目文档或联系开发团队
