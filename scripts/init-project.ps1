# ===========================================
# Greenly 项目一键初始化脚本
# 功能: 检查环境、初始化数据库、提供启动指南
# 用法: .\scripts\init-project.ps1
# ===========================================

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Greenly 项目一键初始化工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectRoot = Split-Path -Parent $PSScriptRoot
$backendDir = Join-Path $projectRoot "plant-backend"
$dbScript = Join-Path $backendDir "src\main\resources\db\greenly-init.sql"

# MySQL 配置（可根据实际情况修改）
$mysqlUser = "root"
$mysqlPass = "123456"
$dbName = "greenly_db"

# -------------------------------------------
# 步骤 1: 检查必需软件
# -------------------------------------------
Write-Host "[1/4] 检查必需软件..." -ForegroundColor Yellow

$checksPassed = $true

# 检查 Java
try {
    $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
    Write-Host "  ✓ Java: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Java 未安装 (需要 JDK 21+)" -ForegroundColor Red
    $checksPassed = $false
}

# 检查 Maven
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven" | Select-Object -First 1
    Write-Host "  ✓ Maven: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Maven 未安装 (需要 3.6+)" -ForegroundColor Red
    $checksPassed = $false
}

# 检查 Node.js
try {
    $nodeVersion = node -v
    Write-Host "  ✓ Node.js: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ Node.js 未安装 (需要 18+)" -ForegroundColor Red
    $checksPassed = $false
}

# 检查 MySQL
try {
    $mysqlVersion = mysql --version
    Write-Host "  ✓ MySQL: $mysqlVersion" -ForegroundColor Green
} catch {
    Write-Host "  ✗ MySQL 未安装 (需要 8.0+)" -ForegroundColor Red
    $checksPassed = $false
}

if (-not $checksPassed) {
    Write-Host ""
    Write-Host "✗ 缺少必需软件，请先安装后再运行此脚本" -ForegroundColor Red
    exit 1
}

Write-Host ""

# -------------------------------------------
# 步骤 2: 初始化数据库
# -------------------------------------------
Write-Host "[2/4] 初始化数据库..." -ForegroundColor Yellow

if (-not (Test-Path $dbScript)) {
    Write-Host "  ✗ 数据库脚本不存在: $dbScript" -ForegroundColor Red
    exit 1
}

Write-Host "  正在创建数据库 $dbName..." -ForegroundColor Gray

# 删除旧数据库并创建新数据库
$createDbCmd = "DROP DATABASE IF EXISTS $dbName; CREATE DATABASE $dbName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
& mysql -u $mysqlUser -p$mysqlPass -e $createDbCmd 2>&1 | Out-Null

if ($LASTEXITCODE -ne 0) {
    Write-Host "  ✗ 数据库创建失败，请检查 MySQL 用户名和密码" -ForegroundColor Red
    Write-Host "  提示: 修改脚本中的 `$mysqlUser 和 `$mysqlPass 变量" -ForegroundColor Yellow
    exit 1
}

Write-Host "  ✓ 数据库创建成功" -ForegroundColor Green
Write-Host "  正在导入表结构和数据..." -ForegroundColor Gray

# 导入 SQL 脚本
Get-Content $dbScript | & mysql -u $mysqlUser -p$mysqlPass $dbName 2>&1 | Out-Null

if ($LASTEXITCODE -ne 0) {
    Write-Host "  ✗ 数据库导入失败" -ForegroundColor Red
    exit 1
}

Write-Host "  ✓ 数据库初始化完成" -ForegroundColor Green

# 验证数据库
$tableCount = & mysql -u $mysqlUser -p$mysqlPass $dbName -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$dbName';" 2>$null
Write-Host "  ✓ 已创建 $tableCount 张表" -ForegroundColor Green

Write-Host ""

# -------------------------------------------
# 步骤 3: 检查配置文件
# -------------------------------------------
Write-Host "[3/4] 检查配置文件..." -ForegroundColor Yellow

$envFile = Join-Path $backendDir ".env"
$envExample = Join-Path $backendDir ".env.example"

if (Test-Path $envFile) {
    Write-Host "  ✓ .env 文件已存在" -ForegroundColor Green
} else {
    if (Test-Path $envExample) {
        Copy-Item $envExample $envFile
        Write-Host "  ✓ 已从模板创建 .env 文件" -ForegroundColor Green
        Write-Host "  ⚠ 请检查并修改 .env 中的配置" -ForegroundColor Yellow
    } else {
        Write-Host "  ⚠ .env.example 不存在，请手动创建 .env 文件" -ForegroundColor Yellow
    }
}

Write-Host ""

# -------------------------------------------
# 步骤 4: 显示启动指南
# -------------------------------------------
Write-Host "[4/4] 准备就绪！" -ForegroundColor Green
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  初始化完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📝 默认账号:" -ForegroundColor Yellow
Write-Host "  管理员: admin / admin123" -ForegroundColor White
Write-Host "  用户:   user1 / user123" -ForegroundColor White
Write-Host ""
Write-Host "🚀 启动应用:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  1️⃣  启动后端 (终端 1):" -ForegroundColor White
Write-Host "     cd plant-backend" -ForegroundColor Cyan
Write-Host "     mvn spring-boot:run" -ForegroundColor Cyan
Write-Host ""
Write-Host "  2️⃣  启动前端 (终端 2):" -ForegroundColor White
Write-Host "     cd plant-frontend" -ForegroundColor Cyan
Write-Host "     npm run dev" -ForegroundColor Cyan
Write-Host ""
Write-Host "  3️⃣  访问应用:" -ForegroundColor White
Write-Host "     http://localhost:5173" -ForegroundColor Cyan
Write-Host ""
Write-Host "  4️⃣  API 文档:" -ForegroundColor White
Write-Host "     http://localhost:9090/swagger-ui.html" -ForegroundColor Cyan
Write-Host ""
Write-Host "📚 更多文档:" -ForegroundColor Yellow
Write-Host "  - 快速启动: QUICK_START.md" -ForegroundColor Gray
Write-Host "  - 项目结构: PROJECT_STRUCTURE.md" -ForegroundColor Gray
Write-Host "  - 故障排查: docs/troubleshooting/common-issues.md" -ForegroundColor Gray
Write-Host ""
Write-Host "祝你使用愉快！🌿" -ForegroundColor Green
Write-Host ""
