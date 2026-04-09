# Greenly 数据库自动备份脚本 (Windows)
# 使用方法: .\db-backup.ps1

param(
    [string]$BackupDir = "C:\GreenlyBackups",
    [string]$DbHost = "localhost",
    [string]$DbPort = "3306",
    [string]$DbName = "greenly_db",
    [string]$DbUser = "root",
    [string]$DbPassword = "123456",
    [int]$RetentionDays = 30,
    [switch]$Compress = $true,
    [switch]$SendNotification = $false
)

# 设置错误处理
$ErrorActionPreference = "Stop"

# 日志函数
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host "[$timestamp] [$Level] $Message"
}

# 主备份函数
function Backup-Database {
    try {
        Write-Log "开始数据库备份..."
        
        # 创建备份目录
        $dateStr = Get-Date -Format "yyyyMMdd_HHmmss"
        $backupPath = Join-Path $BackupDir $dateStr
        if (!(Test-Path $backupPath)) {
            New-Item -ItemType Directory -Path $backupPath | Out-Null
        }
        
        $sqlFile = Join-Path $backupPath "$($DbName)_$dateStr.sql"
        
        # 检查 mysqldump 是否存在
        $mysqldump = Get-Command mysqldump -ErrorAction SilentlyContinue
        if (!$mysqldump) {
            throw "mysqldump 未找到，请确保 MySQL bin 目录已添加到 PATH"
        }
        
        # 执行备份
        Write-Log "正在导出数据库: $DbName"
        $env:MYSQL_PWD = $DbPassword
        
        $arguments = @(
            "--host=$DbHost",
            "--port=$DbPort",
            "--user=$DbUser",
            "--single-transaction",
            "--routines",
            "--triggers",
            "--events",
            "--databases",
            $DbName
        )
        
        & mysqldump $arguments | Out-File -FilePath $sqlFile -Encoding utf8
        
        if ($LASTEXITCODE -ne 0) {
            throw "mysqldump 执行失败，退出码: $LASTEXITCODE"
        }
        
        Write-Log "SQL 文件已生成: $sqlFile"
        
        # 压缩备份
        if ($Compress) {
            Write-Log "正在压缩备份文件..."
            $zipFile = "$sqlFile.zip"
            Compress-Archive -Path $sqlFile -DestinationPath $zipFile -Force
            Remove-Item $sqlFile  # 删除原始 SQL 文件
            Write-Log "压缩完成: $zipFile"
            return $zipFile
        }
        
        return $sqlFile
        
    } catch {
        Write-Log "备份失败: $_" "ERROR"
        throw
    }
}

# 清理旧备份
function Cleanup-OldBackups {
    Write-Log "清理 $RetentionDays 天前的备份..."
    
    $cutoffDate = (Get-Date).AddDays(-$RetentionDays)
    $oldBackups = Get-ChildItem -Path $BackupDir -Directory | 
                  Where-Object { $_.CreationTime -lt $cutoffDate }
    
    $deletedCount = 0
    foreach ($backup in $oldBackups) {
        Write-Log "删除旧备份: $($backup.Name)"
        Remove-Item -Path $backup.FullName -Recurse -Force
        $deletedCount++
    }
    
    Write-Log "已删除 $deletedCount 个旧备份"
}

# 验证备份完整性
function Verify-Backup {
    param([string]$BackupFile)
    
    Write-Log "验证备份文件完整性..."
    
    if (!(Test-Path $BackupFile)) {
        throw "备份文件不存在: $BackupFile"
    }
    
    $fileSize = (Get-Item $BackupFile).Length
    Write-Log "备份文件大小: $([math]::Round($fileSize / 1MB, 2)) MB"
    
    if ($fileSize -eq 0) {
        throw "备份文件为空"
    }
    
    # 如果是 zip 文件，尝试验证
    if ($BackupFile.EndsWith('.zip')) {
        try {
            Add-Type -AssemblyName System.IO.Compression.FileSystem
            $zip = [System.IO.Compression.ZipFile]::OpenRead($BackupFile)
            $entryCount = $zip.Entries.Count
            $zip.Dispose()
            Write-Log "ZIP 文件包含 $entryCount 个条目"
            
            if ($entryCount -eq 0) {
                throw "ZIP 文件为空"
            }
        } catch {
            throw "ZIP 文件损坏: $_"
        }
    }
    
    Write-Log "✓ 备份验证通过"
}

# 发送通知（可选）
function Send-BackupNotification {
    param([string]$Status, [string]$Message)
    
    if (!$SendNotification) { return }
    
    # 这里可以集成邮件、钉钉、企业微信等通知方式
    Write-Log "发送通知: [$Status] $Message"
    
    # 示例：发送邮件（需要配置 SMTP）
    # Send-MailMessage -From "backup@greenly.com" `
    #                  -To "admin@greenly.com" `
    #                  -Subject "Greenly 数据库备份 $Status" `
    #                  -Body $Message `
    #                  -SmtpServer "smtp.example.com"
}

# ==================== 主流程 ====================

Write-Log "=" * 50
Write-Log "Greenly 数据库备份开始"
Write-Log "备份目录: $BackupDir"
Write-Log "保留天数: $RetentionDays"
Write-Log "=" * 50

try {
    # 1. 执行备份
    $backupFile = Backup-Database
    
    # 2. 验证备份
    Verify-Backup -BackupFile $backupFile
    
    # 3. 清理旧备份
    Cleanup-OldBackups
    
    # 4. 发送成功通知
    $message = "备份文件: $backupFile`n文件大小: $([math]::Round((Get-Item $backupFile).Length / 1MB, 2)) MB"
    Send-BackupNotification -Status "成功" -Message $message
    
    Write-Log "✓ 数据库备份完成！" "SUCCESS"
    exit 0
    
} catch {
    # 5. 发送失败通知
    Send-BackupNotification -Status "失败" -Message $_.Exception.Message
    
    Write-Log "✗ 数据库备份失败: $_" "ERROR"
    exit 1
}
