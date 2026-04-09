# 🚀 Greenly 生产环境配置优化工具

**用途**: 根据不同用户规模自动生成优化的配置文件  
**使用方法**: 选择适合您的场景，复制对应配置到 `application-prod.yml`

---

## 📊 场景选择器

### 您期望支持多少用户同时在线？

- **A. 10-30人** (个人/小团队) → 查看 [配置方案 A](#配置方案-a-小型应用)
- **B. 30-100人** (社区/学校) ⭐推荐 → 查看 [配置方案 B](#配置方案-b-中型应用)
- **C. 100-500人** (企业级) → 查看 [配置方案 C](#配置方案-c-大型应用)

---

## 🔧 配置方案 A：小型应用（10-30人）

**服务器要求**: 2核 CPU / 4GB 内存 / 3-5Mbps 带宽

### application-prod.yml

```yaml
server:
  port: 9090
  tomcat:
    threads:
      max: 200
      min-spare: 10

spring:
  application:
    name: plant-backend

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/greenly_db?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    
    hikari:
      maximum-pool-size: 15
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

  mail:
    host: ${MAIL_HOST:smtp.163.com}
    port: ${MAIL_PORT:465}
    protocol: smtp
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 15
          max-idle: 8
          min-idle: 3
          max-wait: 3000ms

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
  header: Authorization

logging:
  level:
    root: WARN
    com.plant.backend: INFO
  file:
    name: logs/plant-backend.log
    max-size: 50MB
    max-history: 15

app:
  file-upload-path: ${UPLOAD_PATH:/data/uploads/}

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### JVM 参数

```bash
-Xms512m -Xmx2g -XX:+UseG1GC
```

### MySQL 优化

```ini
[mysqld]
max_connections = 50
innodb_buffer_pool_size = 2G
```

---

## 🔧 配置方案 B：中型应用（30-100人）⭐ 推荐

**服务器要求**: 4核 CPU / 8GB 内存 / 5-10Mbps 带宽

### application-prod.yml

```yaml
server:
  port: 9090
  tomcat:
    threads:
      max: 300
      min-spare: 20
    max-connections: 8192
    accept-count: 100

spring:
  application:
    name: plant-backend

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/greenly_db?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

  mail:
    host: ${MAIL_HOST:smtp.163.com}
    port: ${MAIL_PORT:465}
    protocol: smtp
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 30
          max-idle: 15
          min-idle: 10
          max-wait: 3000ms

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
  header: Authorization

logging:
  level:
    root: WARN
    com.plant.backend: INFO
    org.springframework: WARN
  file:
    name: logs/plant-backend.log
    max-size: 100MB
    max-history: 30
    total-size-cap: 5GB
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

app:
  file-upload-path: ${UPLOAD_PATH:/data/uploads/}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
```

### JVM 参数

```bash
-Xms1g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200
```

### MySQL 优化

```ini
[mysqld]
max_connections = 100
innodb_buffer_pool_size = 4G
innodb_log_file_size = 512M
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 2
```

### Nginx 优化

```nginx
http {
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css application/json application/javascript;
    
    client_max_body_size 50M;
    
    # 缓存配置
    proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=my_cache:10m max_size=1g inactive=60m;
}
```

---

## 🔧 配置方案 C：大型应用（100-500人）

**服务器要求**: 8核 CPU / 16GB 内存 / 10-20Mbps 带宽  
**高级架构**: 负载均衡 + 读写分离 + Redis 集群

### application-prod.yml

```yaml
server:
  port: 9090
  tomcat:
    threads:
      max: 500
      min-spare: 50
    max-connections: 10000
    accept-count: 200

spring:
  application:
    name: plant-backend

  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/greenly_db?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    
    hikari:
      maximum-pool-size: 50
      minimum-idle: 20
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

  mail:
    host: ${MAIL_HOST:smtp.163.com}
    port: ${MAIL_PORT:465}
    protocol: smtp
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      database: 0
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 50
          max-idle: 25
          min-idle: 15
          max-wait: 3000ms

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.nologging.NoLoggingImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
  header: Authorization

logging:
  level:
    root: WARN
    com.plant.backend: INFO
    org.springframework: WARN
  file:
    name: logs/plant-backend.log
    max-size: 200MB
    max-history: 60
    total-size-cap: 20GB
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

app:
  file-upload-path: ${UPLOAD_PATH:/data/uploads/}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
  metrics:
    export:
      prometheus:
        enabled: true
```

### JVM 参数

```bash
-Xms2g -Xmx8g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=/opt/greenly/logs/heapdump.hprof
```

### MySQL 优化（主从复制）

```ini
[mysqld]
# 主库配置
max_connections = 200
innodb_buffer_pool_size = 12G
innodb_log_file_size = 1G
innodb_flush_log_at_trx_commit = 1
sync_binlog = 1

# 慢查询
slow_query_log = 1
slow_query_log_file = /var/log/mysql/slow.log
long_query_time = 1
```

### 高级架构建议

1. **负载均衡**: Nginx + 2-3台应用服务器
2. **数据库读写分离**: MySQL 主从复制
3. **Redis 哨兵模式**: 高可用缓存
4. **CDN 加速**: 静态资源和图片
5. **监控告警**: Prometheus + Grafana + AlertManager

---

## 🛠️ 快速应用配置

### 步骤 1: 选择配置方案

根据您的预期用户数，选择上面的配置方案。

### 步骤 2: 更新 application-prod.yml

```bash
# 备份原配置
cp application-prod.yml application-prod.yml.bak

# 编辑配置文件
nano application-prod.yml

# 粘贴选中的配置内容
```

### 步骤 3: 更新 systemd 服务

编辑 `/etc/systemd/system/greenly.service`:

```ini
[Service]
# 根据方案修改 JVM 参数
ExecStart=/usr/bin/java -Xms1g -Xmx4g -XX:+UseG1GC -jar /opt/greenly/plant-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 步骤 4: 重启服务

```bash
sudo systemctl daemon-reload
sudo systemctl restart greenly
sudo systemctl status greenly
```

### 步骤 5: 验证配置

```bash
# 检查健康状态
curl http://localhost:9090/actuator/health

# 查看日志
sudo journalctl -u greenly -f

# 压力测试（可选）
ab -n 100 -c 10 http://localhost:9090/api/plant/list
```

---

## 📊 性能对比表

| 指标 | 方案 A | 方案 B ⭐ | 方案 C |
|------|--------|----------|--------|
| **同时在线用户** | 10-30 | 30-100 | 100-500 |
| **并发请求/秒** | 50-100 | 150-300 | 500-1000 |
| **平均响应时间** | < 200ms | < 150ms | < 100ms |
| **数据库连接池** | 15 | 30 | 50 |
| **Tomcat 线程** | 200 | 300 | 500 |
| **JVM 堆内存** | 2GB | 4GB | 8GB |
| **月费用估算** | ¥100-200 | ¥300-500 | ¥800-1500 |

---

## 🎯 推荐行动

### 立即执行

1. ✅ 选择 **方案 B**（30-100人）作为起点
2. ✅ 按照 [`CLOUD_DEPLOYMENT_GUIDE.md`](../guides/CLOUD_DEPLOYMENT_GUIDE.md) 部署
3. ✅ 使用上述配置替换默认配置

### 监控与调优

1. 📊 部署后观察 1-2 周
2. 📈 收集性能数据（响应时间、CPU、内存）
3. 🔧 根据实际负载微调参数
4. 📝 记录优化效果

### 扩展计划

- 用户增长到 100+ → 升级到方案 C
- 用户增长到 500+ → 考虑微服务架构
- 出现性能瓶颈 → 分析具体原因（数据库/CPU/带宽）

---

## 🆘 常见问题

### Q1: 如何判断当前配置是否足够？

**监控指标**:
```bash
# CPU 使用率 > 70% → 需要升级
htop

# 内存使用率 > 80% → 增加 JVM 堆内存
free -h

# 数据库连接池耗尽 → 增加 maximum-pool-size
mysql -e "SHOW STATUS LIKE 'Threads_connected';"

# 响应时间 > 500ms → 优化查询或增加资源
curl -w "@curl-format.txt" -o /dev/null -s http://localhost:9090/api/plant/list
```

### Q2: 配置改错了怎么办？

```bash
# 恢复备份
cp application-prod.yml.bak application-prod.yml
sudo systemctl restart greenly
```

### Q3: 如何测试配置效果？

使用 Apache Bench:
```bash
# 安装
sudo apt install apache2-utils

# 测试
ab -n 1000 -c 50 http://localhost:9090/api/plant/list

# 关注指标：
# Requests per second: 越高越好
# Time per request: 越低越好
# Failed requests: 应为 0
```

---

**💡 提示**: 大多数场景下，**方案 B** 已经足够。建议从方案 B 开始，根据实际使用情况再调整。

*最后更新: 2026-04-09*
