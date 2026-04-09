# Greenly 数据库备份定时任务配置脚本 (Windows)
# 使用方法（以管理员身份运行）: .\setup-scheduled-task.ps1

param(
    [string]$BackupScript = "C:\Users\Sun\Desktop\lunwen\new_new_Greenly-main - 1\scripts\db-backup.ps1",
    [string]$TaskName = "Greenly-Database-Backup",
    [string]$ScheduleTime = "02:00",  # 每天凌晨 2 点
    [int]$RetentionDays = 30,
    [switch]$Remove = $false  # 删除定时任务
)

# 检查管理员权限
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (!$isAdmin) {
    Write-Host "错误: 请以管理员身份运行此脚本" -ForegroundColor Red
    exit 1
}

if ($Remove) {
    # 删除定时任务
    Write-Host "正在删除定时任务: $TaskName"
    Unregister-ScheduledTask -TaskName $TaskName -Confirm:$false
    Write-Host "✓ 定时任务已删除" -ForegroundColor Green
    exit 0
}

# 创建定时任务
Write-Host "=" * 60
Write-Host "Greenly 数据库备份定时任务配置"
Write-Host "=" * 60

# 1. 验证备份脚本存在
if (!(Test-Path $BackupScript)) {
    Write-Host "错误: 备份脚本不存在: $BackupScript" -ForegroundColor Red
    exit 1
}

# 2. 创建触发器（每天指定时间）
$trigger = New-ScheduledTaskTrigger -Daily -At $ScheduleTime

# 3. 创建操作
$action = New-ScheduledTaskAction -Execute "PowerShell.exe" `
    -Argument "-NoProfile -ExecutionPolicy Bypass -File `"$BackupScript`" -RetentionDays $RetentionDays"

# 4. 创建设置
$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -StartWhenAvailable `
    -RunOnlyIfNetworkAvailable `
    -RestartCount 3 `
    -RestartInterval (New-TimeSpan -Minutes 5)

# 5. 创建主体（使用 SYSTEM 账户）
$principal = New-ScheduledTaskPrincipal -UserId "SYSTEM" -LogonType ServiceAccount -RunLevel Highest

# 6. 注册任务
try {
    Register-ScheduledTask -TaskName $TaskName `
                           -Trigger $trigger `
                           -Action $action `
                           -Settings $settings `
                           -Principal $principal `
                           -Description "Greenly 项目数据库自动备份任务，每天 $ScheduleTime 执行" `
                           -Force | Out-Null
    
    Write-Host ""
    Write-Host "✓ 定时任务创建成功！" -ForegroundColor Green
    Write-Host ""
    Write-Host "任务名称: $TaskName" -ForegroundColor Cyan
    Write-Host "执行时间: 每天 $ScheduleTime" -ForegroundColor Cyan
    Write-Host "保留天数: $RetentionDays 天" -ForegroundColor Cyan
    Write-Host "备份脚本: $BackupScript" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "查看任务:" -ForegroundColor Yellow
    Write-Host "  Get-ScheduledTask -TaskName '$TaskName'" -ForegroundColor Gray
    Write-Host ""
    Write-Host "手动执行测试:" -ForegroundColor Yellow
    Write-Host "  Start-ScheduledTask -TaskName '$TaskName'" -ForegroundColor Gray
    Write-Host ""
    Write-Host "删除任务:" -ForegroundColor Yellow
    Write-Host "  .\setup-scheduled-task.ps1 -Remove" -ForegroundColor Gray
    Write-Host ""
    
} catch {
    Write-Host "错误: 创建定时任务失败" -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
    exit 1
}
