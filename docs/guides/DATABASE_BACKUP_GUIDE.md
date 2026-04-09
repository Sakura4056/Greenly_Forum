# Greenly 数据库备份与恢复指南

## 概述

本文档详细说明 Greenly 项目的数据库备份策略、自动化工具和恢复流程。

---

## 快速开始

### 1. 手动备份

```powershell
# 进入脚本目录
cd "C:\Users\Sun\Desktop\lunwen\new_new_Greenly-main - 1\scripts"

# 执行备份
.\db-backup.ps1

# 自定义参数
.\db-backup.ps1 -BackupDir "D:\Backups" -RetentionDays 60 -Compress $true
```

### 2. 设置自动备份（每天凌晨 2 点）

```powershell
# 以管理员身份运行 PowerShell
.\setup-scheduled-task.ps1

# 自定义备份时间（例如每天早上 6 点）
.\setup-scheduled-task.ps1 -ScheduleTime "06:00"

# 删除定时任务
.\setup-scheduled-task.ps1 -Remove
```

### 3. 恢复数据库

```powershell
# 恢复到最新备份
$latestBackup = Get-ChildItem "C:\GreenlyBackups" -Directory | 
                Sort-Object CreationTime -Descending | 
                Select-Object -First 1
.\db-restore.ps1 -BackupFile "$latestBackup\*.zip" -Force

# 恢复到指定备份
.\db-restore.ps1 -BackupFile "C:\GreenlyBackups\20260404_120000\greenly_db_20260404_120000.sql.zip"
```

---

## 备份策略

### 推荐配置

| 环境 | 频率 | 保留期 | 压缩 | 通知 |
|------|------|--------|------|------|
| 开发 | 每天 | 7 天 | 是 | 否 |
| 测试 | 每天 | 14 天 | 是 | 否 |
| 生产 | 每 6 小时 | 30 天 | 是 | 是 |

### 备份内容

- ✅ 所有表结构和数据
- ✅ 存储过程和函数
- ✅ 触发器
- ✅ 事件调度器
- ✅ 视图

### 备份文件命名规范

```
格式: {数据库名}_{YYYYMMDD_HHMMSS}.sql.zip
示例: greenly_db_20260404_020000.sql.zip
```

---

## 自动化脚本详解

### 1. db-backup.ps1（备份脚本）

**功能**:
- 使用 `mysqldump` 导出数据库
- 可选压缩备份文件
- 自动清理过期备份
- 验证备份完整性
- 发送通知（可选）

**参数**:

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| BackupDir | string | C:\GreenlyBackups | 备份存储目录 |
| DbHost | string | localhost | 数据库主机 |
| DbPort | string | 3306 | 数据库端口 |
| DbName | string | greenly_db | 数据库名称 |
| DbUser | string | root | 数据库用户 |
| DbPassword | string | 123456 | 数据库密码 |
| RetentionDays | int | 30 | 备份保留天数 |
| Compress | switch | true | 是否压缩 |
| SendNotification | switch | false | 是否发送通知 |

**使用示例**:

```powershell
# 基本用法
.\db-backup.ps1

# 生产环境配置
.\db-backup.ps1 `
    -BackupDir "D:\ProductionBackups" `
    -DbPassword "secure_password" `
    -RetentionDays 90 `
    -SendNotification $true

# 不压缩（快速备份）
.\db-backup.ps1 -Compress $false
```

### 2. db-restore.ps1（恢复脚本）

**功能**:
- 支持 ZIP 和 SQL 文件格式
- 自动删除并重建数据库
- 验证恢复结果
- 安全检查（需要确认）

**参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| BackupFile | string | 是 | - | 备份文件路径 |
| DbHost | string | 否 | localhost | 数据库主机 |
| DbPort | string | 否 | 3306 | 数据库端口 |
| DbName | string | 否 | greenly_db | 数据库名称 |
| DbUser | string | 否 | root | 数据库用户 |
| DbPassword | string | 否 | 123456 | 数据库密码 |
| Force | switch | 否 | false | 跳过确认提示 |

**使用示例**:

```powershell
# 交互式恢复（需要确认）
.\db-restore.ps1 -BackupFile "C:\GreenlyBackups\20260404_020000\greenly_db_20260404_020000.sql.zip"

# 强制恢复（无确认）
.\db-restore.ps1 -BackupFile "..." -Force

# 恢复到不同数据库
.\db-restore.ps1 -BackupFile "..." -DbName "greenly_db_test" -Force
```

### 3. setup-scheduled-task.ps1（定时任务配置）

**功能**:
- 创建 Windows 计划任务
- 配置每日自动备份
- 管理系统账户权限

**使用示例**:

```powershell
# 创建每天凌晨 2 点的备份任务
.\setup-scheduled-task.ps1

# 创建每天早上 6 点的备份任务
.\setup-scheduled-task.ps1 -ScheduleTime "06:00"

# 创建每 6 小时的备份任务（生产环境）
# 注意：需要修改脚本中的触发器为多次执行
.\setup-scheduled-task.ps1 -TaskName "Greenly-Backup-Every-6h"

# 删除定时任务
.\setup-scheduled-task.ps1 -Remove
```

---

## 备份验证

### 自动验证

备份脚本会自动验证：
1. 文件大小不为零
2. ZIP 文件可正常打开
3. ZIP 包含至少一个条目

### 手动验证

```powershell
# 1. 检查备份文件
Get-ChildItem "C:\GreenlyBackups" -Recurse | 
    Select-Object FullName, Length, CreationTime | 
    Format-Table -AutoSize

# 2. 测试恢复（到临时数据库）
.\db-restore.ps1 `
    -BackupFile "C:\GreenlyBackups\latest\*.zip" `
    -DbName "greenly_db_verify" `
    -Force

# 3. 验证数据
mysql -u root -p123456 -e "
    USE greenly_db_verify;
    SELECT COUNT(*) as user_count FROM sys_user;
    SELECT COUNT(*) as plant_count FROM my_plant;
    SELECT COUNT(*) as schedule_count FROM care_schedule;
"

# 4. 清理验证数据库
mysql -u root -p123456 -e "DROP DATABASE greenly_db_verify;"
```

---

## 监控与告警

### 1. 检查备份状态

```powershell
# 查看最近的备份
Get-ChildItem "C:\GreenlyBackups" -Directory | 
    Sort-Object CreationTime -Descending | 
    Select-Object -First 5 |
    ForEach-Object {
        $size = (Get-ChildItem $_.FullName -Recurse | Measure-Object -Property Length -Sum).Sum / 1MB
        [PSCustomObject]@{
            Date = $_.Name
            Size_MB = [math]::Round($size, 2)
            Created = $_.CreationTime
        }
    } | Format-Table -AutoSize
```

### 2. 设置邮件通知

在 `db-backup.ps1` 中启用 `$SendNotification`，并配置 SMTP：

```powershell
# 在脚本末尾添加
if ($SendNotification) {
    $smtpServer = "smtp.company.com"
    $from = "backup@greenly.com"
    $to = "admin@greenly.com"
    $subject = "Greenly Database Backup - $Status"
    $body = $message
    
    Send-MailMessage -From $from `
                     -To $to `
                     -Subject $subject `
                     -Body $body `
                     -SmtpServer $smtpServer
}
```

### 3. 集成监控系统

**Prometheus + Grafana 示例**:

创建备份指标暴露端点：
```powershell
# backup-metrics.ps1
$latestBackup = Get-ChildItem "C:\GreenlyBackups" -Directory | 
                Sort-Object CreationTime -Descending | 
                Select-Object -First 1

$age = (Get-Date) - $latestBackup.CreationTime
$ageHours = [math]::Round($age.TotalHours, 2)

Write-Output "backup_age_hours $ageHours"
Write-Output "backup_success 1"
```

---

## 灾难恢复流程

### 场景 1: 数据误删除

**步骤**:
1. 停止应用服务
   ```powershell
   Stop-Service -Name "plant-backend"
   ```

2. 找到最近的备份
   ```powershell
   Get-ChildItem "C:\GreenlyBackups" -Directory | 
       Sort-Object CreationTime -Descending | 
       Select-Object -First 1
   ```

3. 恢复数据库
   ```powershell
   .\db-restore.ps1 -BackupFile "path\to\backup.zip" -Force
   ```

4. 验证数据
   ```powershell
   mysql -u root -p123456 -e "SELECT COUNT(*) FROM greenly_db.sys_user;"
   ```

5. 重启应用服务
   ```powershell
   Start-Service -Name "plant-backend"
   ```

### 场景 2: 数据库损坏

**步骤**:
1. 停止 MySQL 服务
2. 备份当前数据目录（用于后续分析）
3. 重新安装 MySQL（如必要）
4. 使用备份恢复
5. 验证完整性

### 场景 3: 服务器完全故障

**步骤**:
1. 在新服务器上安装 MySQL
2. 从备份服务器/云存储下载最新备份
3. 恢复数据库
4. 更新应用配置
5. 启动应用

---

## 最佳实践

### 1. 3-2-1 备份原则

- **3** 份数据副本（生产 + 2 个备份）
- **2** 种不同介质（本地磁盘 + 云存储）
- **1** 个异地备份（不同地理位置）

### 2. 定期演练

- 每月进行一次恢复演练
- 验证备份文件可用性
- 记录恢复时间目标（RTO）

### 3. 加密备份

对于生产环境，建议加密备份文件：

```powershell
# 使用 7-Zip 加密压缩
& "C:\Program Files\7-Zip\7z.exe" a `
    -t7z `
    -mhe=on `
    -p"MySecurePassword" `
    "backup.7z" `
    "backup.sql"
```

### 4. 云存储同步

```powershell
# 同步到 AWS S3
aws s3 sync "C:\GreenlyBackups" "s3://greenly-backups/prod/"

# 同步到阿里云 OSS
ossutil sync "C:\GreenlyBackups" "oss://greenly-backups/prod/"
```

---

## 故障排查

### 问题 1: mysqldump 未找到

**解决方案**:
```powershell
# 添加 MySQL bin 到 PATH
$env:Path += ";C:\Program Files\MySQL\MySQL Server 8.0\bin"

# 或永久添加
[Environment]::SetEnvironmentVariable(
    "Path",
    $env:Path + ";C:\Program Files\MySQL\MySQL Server 8.0\bin",
    "Machine"
)
```

### 问题 2: 访问被拒绝

**解决方案**:
- 确保以管理员身份运行 PowerShell
- 检查备份目录权限
- 验证数据库用户权限

### 问题 3: 备份文件过大

**解决方案**:
```powershell
# 排除大表
.\db-backup.ps1 -ExcludeTables "logs,audit_trail"

# 或使用增量备份（需要额外工具）
```

### 问题 4: 恢复速度慢

**优化建议**:
```sql
-- 恢复前执行
SET GLOBAL innodb_flush_log_at_trx_commit = 2;
SET GLOBAL sync_binlog = 0;

-- 恢复后恢复默认值
SET GLOBAL innodb_flush_log_at_trx_commit = 1;
SET GLOBAL sync_binlog = 1;
```

---

## 相关文档

- [Redis 安装与配置](REDIS_SETUP_GUIDE.md)
- [E2E 测试指南](../plant-frontend/e2e/README.md)
- [生产环境日志清理](../plant-frontend/LOG_CLEANUP_GUIDE.md)
- [常见问题排查](troubleshooting/common-issues.md)

---

**最后更新**: 2026-04-04  
**维护者**: Greenly 开发团队  
**版本**: v1.0
