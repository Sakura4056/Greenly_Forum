# 📜 Greenly 脚本工具集

本目录包含项目的所有运维和初始化脚本。

---

## 📁 目录结构

```
scripts/
├── init-project.ps1           # ⭐ 项目一键初始化脚本（推荐）
├── database/                  # 数据库管理脚本
│   ├── db-backup.ps1          # 数据库备份
│   ├── db-backup-cron.ps1     # 定时备份配置
│   └── db-restore.ps1         # 数据库恢复
└── maintenance/               # 维护脚本（预留）
```

---

## 🚀 快速开始

### 新项目初始化（推荐）

```powershell
# 一键完成：环境检查 + 数据库初始化 + 配置生成
.\scripts\init-project.ps1
```

**功能：**
- ✅ 自动检查 Java、Maven、Node.js、MySQL
- ✅ 创建并初始化 greenly_db 数据库
- ✅ 生成 .env 配置文件
- ✅ 显示启动指南

---

## 🗄️ 数据库管理

### 备份数据库

```powershell
# 手动备份
.\scripts\database\db-backup.ps1

# 备份文件保存在: backups/greenly_db_YYYYMMDD_HHMMSS.sql
```

### 设置定时备份

```powershell
# 配置 Windows 任务计划程序，每天自动备份
.\scripts\database\db-backup-cron.ps1
```

### 恢复数据库

```powershell
# 从备份文件恢复
.\scripts\database\db-restore.ps1 -BackupFile "backups/greenly_db_20260406_120000.sql"
```

---

## ⚙️ 脚本说明

### init-project.ps1
**用途**: 新项目的一键初始化工具  
**适用场景**: 
- 首次部署项目
- 重新初始化开发环境
- 重置数据库到初始状态

**注意事项**:
- 需要 MySQL root 权限
- 会删除现有的 greenly_db 数据库
- 默认 MySQL 密码为 `123456`，如需修改请编辑脚本

### db-backup.ps1
**用途**: 手动备份数据库  
**特点**:
- 自动添加时间戳
- 压缩备份文件
- 保留最近 7 天的备份

### db-backup-cron.ps1
**用途**: 配置自动备份  
**功能**:
- 创建 Windows 任务计划
- 每天凌晨 2:00 自动备份
- 自动清理过期备份

### db-restore.ps1
**用途**: 从备份恢复数据库  
**参数**:
- `-BackupFile`: 备份文件路径（必需）

---

## 🔧 自定义配置

### 修改 MySQL 连接信息

编辑 `init-project.ps1`，修改以下变量：

```powershell
$mysqlUser = "root"      # MySQL 用户名
$mysqlPass = "123456"    # MySQL 密码
$dbName = "greenly_db"   # 数据库名称
```

### 修改备份策略

编辑 `db-backup-cron.ps1`，修改备份时间和保留天数：

```powershell
$backupTime = "02:00"    # 备份时间
$retentionDays = 7       # 保留天数
```

---

## ⚠️ 注意事项

1. **执行权限**: 首次运行可能需要设置 PowerShell 执行策略
   ```powershell
   Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
   ```

2. **MySQL 环境变量**: 确保 `mysql` 和 `mysqldump` 命令可在终端中直接调用

3. **管理员权限**: 定时备份配置需要管理员权限

4. **数据安全**: 
   - 备份前确认磁盘空间充足
   - 定期验证备份文件可用性
   - 重要操作前先备份

---

## 🆘 常见问题

### Q: 脚本执行失败，提示"无法加载文件"
**A**: 需要设置 PowerShell 执行策略
```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

### Q: MySQL 连接失败
**A**: 检查以下几点：
1. MySQL 服务是否运行
2. 用户名和密码是否正确
3. MySQL 是否添加到系统 PATH

### Q: 如何查看备份历史
**A**: 备份文件保存在 `backups/` 目录，按时间戳命名

---

## 📚 相关文档

- **快速启动**: [QUICK_START.md](../QUICK_START.md)
- **数据库配置**: [docs/guides/DATABASE_QUICK_REFERENCE.md](../docs/guides/DATABASE_QUICK_REFERENCE.md)
- **故障排查**: [docs/troubleshooting/common-issues.md](../docs/troubleshooting/common-issues.md)

---

*最后更新: 2026-04-06*  
*维护者: Greenly 开发团队*
