# ===========================================
# Greenly 后端环境变量配置检查脚本
# ===========================================
# 使用方法: .\check-env.ps1
# ===========================================

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Greenly 后端环境变量配置检查工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$envFile = ".env"
$errors = @()
$warnings = @()

# 检查 .env 文件是否存在
Write-Host "[1/6] 检查 .env 文件..." -ForegroundColor Yellow
if (Test-Path $envFile) {
    Write-Host "  ✓ .env 文件存在" -ForegroundColor Green
} else {
    Write-Host "  ✗ .env 文件不存在" -ForegroundColor Red
    $errors += ".env 文件不存在，请复制 .env.example 为 .env 并填写配置"
}

# 读取 .env 文件内容
if (Test-Path $envFile) {
    $envContent = Get-Content $envFile | Where-Object { $_ -notmatch '^\s*#' -and $_ -notmatch '^\s*$' }

    # 检查必需的环境变量
    Write-Host ""
    Write-Host "[2/6] 检查数据库配置..." -ForegroundColor Yellow
    if ($envContent -match '^DB_USERNAME=.+') {
        Write-Host "  ✓ DB_USERNAME 已配置" -ForegroundColor Green
    } else {
        Write-Host "  ✗ DB_USERNAME 未配置" -ForegroundColor Red
        $errors += "缺少 DB_USERNAME 配置"
    }

    if ($envContent -match '^DB_PASSWORD=.+') {
        $dbPassword = ($envContent -match '^DB_PASSWORD=').Split('=')[1]
        if ($dbPassword -eq '123456') {
            Write-Host "  ⚠ DB_PASSWORD 使用默认值（建议修改）" -ForegroundColor Yellow
            $warnings += "数据库密码使用默认值，存在安全风险"
        } else {
            Write-Host "  ✓ DB_PASSWORD 已配置" -ForegroundColor Green
        }
    } else {
        Write-Host "  ✗ DB_PASSWORD 未配置" -ForegroundColor Red
        $errors += "缺少 DB_PASSWORD 配置"
    }

    if ($envContent -match '^DB_URL=.+') {
        Write-Host "  ✓ DB_URL 已配置" -ForegroundColor Green
    } else {
        Write-Host "  ✗ DB_URL 未配置" -ForegroundColor Red
        $errors += "缺少 DB_URL 配置"
    }

    Write-Host ""
    Write-Host "[3/6] 检查邮件配置..." -ForegroundColor Yellow
    if ($envContent -match '^MAIL_USERNAME=.+') {
        Write-Host "  ✓ MAIL_USERNAME 已配置" -ForegroundColor Green
    } else {
        Write-Host "  ✗ MAIL_USERNAME 未配置" -ForegroundColor Red
        $errors += "缺少 MAIL_USERNAME 配置"
    }

    if ($envContent -match '^MAIL_PASSWORD=.+') {
        Write-Host "  ✓ MAIL_PASSWORD 已配置" -ForegroundColor Green
    } else {
        Write-Host "  ✗ MAIL_PASSWORD 未配置" -ForegroundColor Red
        $errors += "缺少 MAIL_PASSWORD 配置"
    }

    Write-Host ""
    Write-Host "[4/6] 检查 JWT 配置..." -ForegroundColor Yellow
    if ($envContent -match '^JWT_SECRET=.+') {
        $jwtSecret = ($envContent -match '^JWT_SECRET=').Split('=')[1]
        if ($jwtSecret.Length -lt 32) {
            Write-Host "  ✗ JWT_SECRET 长度不足 32 个字符" -ForegroundColor Red
            $errors += "JWT_SECRET 长度不足，至少需要 32 个字符"
        } else {
            Write-Host "  ✓ JWT_SECRET 已配置（长度: $($jwtSecret.Length)）" -ForegroundColor Green
        }
    } else {
        Write-Host "  ✗ JWT_SECRET 未配置" -ForegroundColor Red
        $errors += "缺少 JWT_SECRET 配置"
    }

    Write-Host ""
    Write-Host "[5/6] 检查百度 AI 配置..." -ForegroundColor Yellow
    if ($envContent -match '^BAIDU_AI_APP_ID=.+') {
        Write-Host "  ✓ BAIDU_AI_APP_ID 已配置" -ForegroundColor Green
    } else {
        Write-Host "  ✗ BAIDU_AI_APP_ID 未配置" -ForegroundColor Red
        $errors += "缺少 BAIDU_AI_APP_ID 配置"
    }

    if ($envContent -match '^BAIDU_AI_API_KEY=.+') {
        Write-Host "  ✓ BAIDU_AI_API_KEY 已配置" -ForegroundColor Green
    } else {
        Write-Host "  ✗ BAIDU_AI_API_KEY 未配置" -ForegroundColor Red
        $errors += "缺少 BAIDU_AI_API_KEY 配置"
    }

    if ($envContent -match '^BAIDU_AI_SECRET_KEY=.+') {
        Write-Host "  ✓ BAIDU_AI_SECRET_KEY 已配置" -ForegroundColor Green
    } else {
        Write-Host "  ✗ BAIDU_AI_SECRET_KEY 未配置" -ForegroundColor Red
        $errors += "缺少 BAIDU_AI_SECRET_KEY 配置"
    }

    Write-Host ""
    Write-Host "[6/6] 检查 Redis 配置..." -ForegroundColor Yellow
    if ($envContent -match '^REDIS_HOST=.+') {
        Write-Host "  ✓ REDIS_HOST 已配置" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ REDIS_HOST 未配置（使用默认值 localhost）" -ForegroundColor Yellow
        $warnings += "REDIS_HOST 未配置，将使用默认值 localhost"
    }
}

# 显示检查结果
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  检查结果汇总" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($errors.Count -eq 0 -and $warnings.Count -eq 0) {
    Write-Host "✓ 所有配置检查通过！" -ForegroundColor Green
    Write-Host ""
    Write-Host "您可以启动应用了：" -ForegroundColor Cyan
    Write-Host "  mvn spring-boot:run" -ForegroundColor White
} else {
    if ($errors.Count -gt 0) {
        Write-Host "✗ 发现 $($errors.Count) 个错误：" -ForegroundColor Red
        foreach ($error in $errors) {
            Write-Host "  - $error" -ForegroundColor Red
        }
        Write-Host ""
    }

    if ($warnings.Count -gt 0) {
        Write-Host "⚠ 发现 $($warnings.Count) 个警告：" -ForegroundColor Yellow
        foreach ($warning in $warnings) {
            Write-Host "  - $warning" -ForegroundColor Yellow
        }
        Write-Host ""
    }

    if ($errors.Count -gt 0) {
        Write-Host "请先修复上述错误后再启动应用。" -ForegroundColor Red
        exit 1
    } else {
        Write-Host "警告不影响启动，但建议处理以提升安全性。" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "详细配置说明请参考: plant-backend/ENV_SETUP.md" -ForegroundColor Cyan
Write-Host ""
