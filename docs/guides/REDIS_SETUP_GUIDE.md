# Redis 安装与配置指南

## 概述

Greenly 项目使用 Redis 实现缓存功能，包括：
- 用户会话缓存
- 植物数据缓存
- 养护计划缓存
- API 响应缓存

## Windows 系统安装步骤

### 方法一：使用 Chocolatey（推荐）

```powershell
# 1. 以管理员身份运行 PowerShell
# 2. 安装 Chocolatey（如果尚未安装）
Set-ExecutionPolicy Bypass -Scope Process -Force; [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072; iex ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))

# 3. 安装 Redis
choco install redis-64

# 4. 启动 Redis 服务
redis-server --service-start

# 5. 验证安装
redis-cli ping
# 应返回: PONG
```

### 方法二：手动安装

1. **下载 Redis for Windows**
   - 访问: https://github.com/microsoftarchive/redis/releases
   - 下载: `Redis-x64-3.0.504.msi`

2. **安装**
   - 运行 MSI 安装程序
   - 选择安装路径（默认: `C:\Program Files\Redis`）
   - 勾选"Add Redis to PATH"

3. **启动服务**
   ```powershell
   # 以管理员身份运行
   redis-server --service-install
   redis-server --service-start
   ```

### 方法三：使用 WSL2（适合开发者）

```bash
# 在 WSL2 Ubuntu 中
sudo apt update
sudo apt install redis-server
sudo service redis-server start

# 验证
redis-cli ping
```

## 配置说明

### 后端配置检查

文件: `plant-backend/src/main/resources/application.yml`

```yaml
spring:
  data:
    redis:
      host: localhost      # Redis 服务器地址
      port: 6379           # Redis 端口
      password:            # 密码（如果有）
      database: 0          # 数据库索引
      timeout: 5000ms      # 连接超时
      lettuce:
        pool:
          max-active: 8    # 最大活跃连接数
          max-idle: 8      # 最大空闲连接数
          min-idle: 2      # 最小空闲连接数
          max-wait: 3000ms # 最大等待时间
```

### 验证配置

启动后端后，检查日志：
```
INFO  o.s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in XX ms. Found 0 Redis repository interfaces.
```

如果没有错误信息，说明 Redis 连接成功。

## 常用命令

```powershell
# 启动 Redis 服务
redis-server --service-start

# 停止 Redis 服务
redis-server --service-stop

# 重启 Redis 服务
redis-server --service-restart

# 查看 Redis 状态
redis-server --service-status

# 连接到 Redis CLI
redis-cli

# 测试连接
redis-cli ping
# 返回: PONG

# 查看所有键
redis-cli keys *

# 清空当前数据库
redis-cli flushdb

# 清空所有数据库
redis-cli flushall
```

## 故障排查

### 问题 1: 连接被拒绝

```
io.lettuce.core.RedisConnectionException: Unable to connect to localhost:6379
```

**解决方案**:
1. 检查 Redis 服务是否运行
   ```powershell
   redis-server --service-status
   ```
2. 检查防火墙设置
3. 确认端口未被占用
   ```powershell
   netstat -ano | findstr :6379
   ```

### 问题 2: 认证失败

```
ERR Client sent AUTH, but no password is set
```

**解决方案**:
确保 `application.yml` 中的 `password` 字段为空或与 Redis 配置一致。

### 问题 3: 连接超时

```
io.lettuce.core.RedisCommandTimeoutException: Command timed out
```

**解决方案**:
1. 增加超时时间
   ```yaml
   spring:
     data:
       redis:
         timeout: 10000ms  # 增加到 10 秒
   ```
2. 检查系统资源使用情况

## 缓存功能验证

启动后端后，可以通过以下方式验证缓存是否工作：

1. **查看日志**
   ```
   DEBUG c.p.b.aspect.CacheAspect : Cache hit for key: plant_list_1
   ```

2. **API 测试**
   ```bash
   # 第一次请求（缓存未命中）
   curl http://localhost:9090/api/plant/official/query?keyword=test

   # 第二次请求（应该从缓存读取）
   curl http://localhost:9090/api/plant/official/query?keyword=test
   ```

3. **Redis CLI 监控**
   ```bash
   redis-cli monitor
   # 观察缓存读写操作
   ```

## 生产环境建议

1. **设置密码**
   ```yaml
   spring:
     data:
       redis:
         password: ${REDIS_PASSWORD}  # 使用环境变量
   ```

2. **启用持久化**
   - RDB 快照（默认启用）
   - AOF 日志（可选）

3. **配置主从复制**
   - 提高可用性
   - 读写分离

4. **监控指标**
   - 内存使用率
   - 命中率
   - 连接数

---

**最后更新: 2026-04-09
**维护者**: Greenly 开发团队
