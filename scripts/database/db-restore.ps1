# Greenly 数据库恢复脚本 (Windows)
# 使用方法: .\db-restore.ps1 -BackupFile "C:\GreenlyBackups\20260404_120000\greenly_db_20260404_120000.sql.zip"

param(
    [Parameter(Mandatory=$true)]
    [string]$BackupFile,
    
    [string]$DbHost = "localhost",
    [string]$DbPort = "3306",
    [string]$DbName = "greenly_db",
    [string]$DbUser = "root",
    [string]$DbPassword = "123456",
    
    [switch]$Force = $false
)

# 设置错误处理
$ErrorActionPreference = "Stop"

# 日志函数
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $(if($Level -eq "ERROR"){"Red"}elseif($Level -eq "WARN"){"Yellow"}else{"Green"})
}

# 确认操作
function Confirm-Restore {
    if (!$Force) {
        Write-Log "警告: 此操作将覆盖当前数据库 '$DbName' 的所有数据！" "WARN"
        $confirm = Read-Host "确定要继续吗？(输入 YES 确认)"
        
        if ($confirm -ne "YES") {
            Write-Log "操作已取消"
            exit 0
        }
    }
}

# 解压备份文件
function Extract-Backup {
    param([string]$ZipFile)
    
    Write-Log "正在解压备份文件..."
    $extractPath = Join-Path (Split-Path $ZipFile -Parent) "temp_restore"
    
    if (Test-Path $extractPath) {
        Remove-Item -Path $extractPath -Recurse -Force
    }
    
    Expand-Archive -Path $ZipFile -DestinationPath $extractPath
    
    # 查找 SQL 文件
    $sqlFile = Get-ChildItem -Path $extractPath -Filter "*.sql" | Select-Object -First 1
    
    if (!$sqlFile) {
        throw "在 ZIP 文件中未找到 SQL 文件"
    }
    
    Write-Log "SQL 文件: $($sqlFile.FullName)"
    return $sqlFile.FullName
}

# 恢复数据库
function Restore-Database {
    param([string]$SqlFile)
    
    Write-Log "开始恢复数据库..."
    
    # 检查 mysql 命令
    $mysql = Get-Command mysql -ErrorAction SilentlyContinue
    if (!$mysql) {
        throw "mysql 命令未找到，请确保 MySQL bin 目录已添加到 PATH"
    }
    
    # 删除并重新创建数据库
    Write-Log "正在删除旧数据库..."
    $env:MYSQL_PWD = $DbPassword
    
    & mysql --host=$DbHost --port=$DbPort --user=$DbUser -e "DROP DATABASE IF EXISTS $DbName;"
    if ($LASTEXITCODE -ne 0) {
        throw "删除数据库失败"
    }
    
    Write-Log "正在创建新数据库..."
    & mysql --host=$DbHost --port=$DbPort --user=$DbUser -e "CREATE DATABASE $DbName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    if ($LASTEXITCODE -ne 0) {
        throw "创建数据库失败"
    }
    
    # 导入 SQL 文件
    Write-Log "正在导入数据（这可能需要几分钟）..."
    & mysql --host=$DbHost --port=$DbPort --user=$DbUser $DbName < $SqlFile
    
    if ($LASTEXITCODE -ne 0) {
        throw "数据导入失败"
    }
    
    Write-Log "✓ 数据导入完成"
}

# 验证恢复结果
function Verify-Restore {
    Write-Log "验证恢复结果..."
    
    $env:MYSQL_PWD = $DbPassword
    
    # 检查表数量
    $tableCount = & mysql --host=$DbHost --port=$DbPort --user=$DbUser -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DbName';"
    Write-Log "表数量: $tableCount"
    
    if ([int]$tableCount -eq 0) {
        throw "数据库中没有任何表"
    }
    
    # 检查关键表
    $requiredTables = @('sys_user', 'my_plant', 'care_schedule', 'care_record')
    foreach ($table in $requiredTables) {
        $exists = & mysql --host=$DbHost --port=$DbPort --user=$DbUser -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DbName' AND table_name='$table';"
        if ([int]$exists -eq 0) {
            Write-Log "警告: 表 '$table' 不存在" "WARN"
        } else {
            Write-Log "✓ 表 '$table' 存在"
        }
    }
    
    # 检查用户数据
    $userCount = & mysql --host=$DbHost --port=$DbPort --user=$DbUser -N -e "SELECT COUNT(*) FROM $DbName.sys_user;"
    Write-Log "用户数量: $userCount"
    
    Write-Log "✓ 恢复验证通过"
}

# ==================== 主流程 ====================

Write-Log "=" * 60
Write-Log "Greenly 数据库恢复工具"
Write-Log "备份文件: $BackupFile"
Write-Log "目标数据库: $DbName"
Write-Log "=" * 60

try {
    # 1. 确认操作
    Confirm-Restore
    
    # 2. 检查备份文件
    if (!(Test-Path $BackupFile)) {
        throw "备份文件不存在: $BackupFile"
    }
    
    # 3. 解压（如果是 ZIP）
    $sqlFile = $BackupFile
    if ($BackupFile.EndsWith('.zip')) {
        $sqlFile = Extract-Backup -ZipFile $BackupFile
    }
    
    # 4. 恢复数据库
    Restore-Database -SqlFile $sqlFile
    
    # 5. 验证恢复
    Verify-Restore
    
    # 6. 清理临时文件
    if ($BackupFile.EndsWith('.zip')) {
        $tempDir = Split-Path $sqlFile -Parent
        if (Test-Path $tempDir) {
            Remove-Item -Path $tempDir -Recurse -Force
            Write-Log "已清理临时文件"
        }
    }
    
    Write-Log "=" * 60
    Write-Log "✓ 数据库恢复成功！" "SUCCESS"
    Write-Log "=" * 60
    
    exit 0
    
} catch {
    Write-Log "=" * 60
    Write-Log "✗ 数据库恢复失败: $_" "ERROR"
    Write-Log "=" * 60
    exit 1
}
