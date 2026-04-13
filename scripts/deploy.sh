#!/bin/bash
###############################################################################
# Greenly 一键部署脚本
# 功能: 拉取最新代码 → 构建后端 → 构建前端 → 部署 → 健康检查
# 用法: bash deploy.sh [--no-pull] [--skip-build] [--no-backup]
###############################################################################
set -euo pipefail

# ===== 颜色 =====
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# ===== 配置 =====
PROJECT_DIR="/opt/new_new_Greenly"
BACKEND_DIR="$PROJECT_DIR/plant-backend"
FRONTEND_DIR="$PROJECT_DIR/plant-frontend"
BACKUP_DIR="$PROJECT_DIR/backups"
DEPLOY_LOG="$PROJECT_DIR/logs/deploy.log"

SERVICE_NAME="greenly-backend"
NGINX_CONF="/etc/nginx/conf.d/greenly.conf"
HEALTH_URL="http://127.0.0.1:9090/api/"
HEALTH_TIMEOUT=60  # 秒

# ===== 参数 =====
DO_PULL=true
DO_BUILD=true
DO_BACKUP=true

for arg in "$@"; do
    case $arg in
        --no-pull)     DO_PULL=false ;;
        --skip-build)  DO_BUILD=false ;;
        --no-backup)   DO_BACKUP=false ;;
        --help|-h)
            echo "用法: bash deploy.sh [选项]"
            echo ""
            echo "选项:"
            echo "  --no-pull     跳过 git pull（使用当前代码）"
            echo "  --skip-build  跳过构建（仅重启服务）"
            echo "  --no-backup   部署前不备份数据库"
            echo "  -h, --help    显示帮助"
            exit 0
            ;;
    esac
done

# ===== 工具函数 =====
log() {
    local msg="[$(date '+%Y-%m-%d %H:%M:%S')] $1"
    echo -e "$2$msg${NC}"
    echo "$msg" >> "$DEPLOY_LOG" 2>/dev/null || true
}

info()    { log "$1" "$CYAN"; }
success() { log "✓ $1" "$GREEN"; }
warn()    { log "⚠ $1" "$YELLOW"; }
fail()    { log "✗ $1" "$RED"; }

die() {
    fail "$1"
    echo ""
    echo -e "${RED}部署失败！查看日志: $DEPLOY_LOG${NC}"
    exit 1
}

# ===== 前置检查 =====
info "Greenly 部署开始..."
echo ""

mkdir -p "$(dirname "$DEPLOY_LOG")" "$BACKUP_DIR"

# 检查必要工具
for cmd in java mvn npm mysql nginx git; do
    if ! command -v "$cmd" &>/dev/null; then
        die "缺少必要工具: $cmd"
    fi
done

# ===== 步骤 1: 备份 =====
if $DO_BACKUP; then
    info "[1/6] 备份数据库..."
    
    TIMESTAMP=$(date '+%Y%m%d_%H%M%S')
    BACKUP_FILE="$BACKUP_DIR/greenly_db_${TIMESTAMP}.sql"
    
    # 从 .env 读取数据库密码
    DB_PASS=$(grep '^DB_PASSWORD=' "$BACKEND_DIR/.env" 2>/dev/null | cut -d= -f2- || echo "")
    
    if [ -n "$DB_PASS" ]; then
        mysqldump -u root -p"$DB_PASS" greenly_db --single-transaction --routines --triggers > "$BACKUP_FILE" 2>/dev/null && \
        gzip "$BACKUP_FILE" && \
        success "数据库已备份: ${BACKUP_FILE}.gz ($(du -h "${BACKUP_FILE}.gz" | cut -f1))"
    else
        warn "未找到 DB_PASSWORD，跳过备份"
    fi
    
    # 清理 7 天前的备份
    find "$BACKUP_DIR" -name "greenly_db_*.sql.gz" -mtime +7 -delete 2>/dev/null || true
    echo ""
fi

# ===== 步骤 2: 拉取代码 =====
if $DO_PULL; then
    info "[2/6] 拉取最新代码..."
    
    cd "$PROJECT_DIR"
    
    # 保存当前 commit 用于回滚
    OLD_COMMIT=$(git rev-parse HEAD 2>/dev/null || echo "unknown")
    echo "$OLD_COMMIT" > "$PROJECT_DIR/.last_deploy_commit"
    
    # Stash 本地修改（.env 等）
    git stash --include-untracked 2>/dev/null || true
    
    if ! git pull origin "$(git branch --show-current)" 2>&1; then
        die "git pull 失败"
    fi
    
    NEW_COMMIT=$(git rev-parse HEAD)
    SHORT_COMMIT=$(git rev-parse --short HEAD)
    
    if [ "$OLD_COMMIT" = "$NEW_COMMIT" ]; then
        success "代码已是最新 ($SHORT_COMMIT)"
    else
        success "代码已更新: $(git rev-parse --short "$OLD_COMMIT") → $SHORT_COMMIT"
        CHANGES=$(git log --oneline "$OLD_COMMIT..$NEW_COMMIT" 2>/dev/null || echo "")
        if [ -n "$CHANGES" ]; then
            echo -e "${CYAN}  更新内容:${NC}"
            echo "$CHANGES" | head -10 | while read -r line; do
                echo -e "    $line"
            done
        fi
    fi
    
    # 恢复 stash
    git stash pop 2>/dev/null || true
    echo ""
else
    info "[2/6] 跳过 git pull (--no-pull)"
    echo ""
fi

# ===== 步骤 3: 构建后端 =====
if $DO_BUILD; then
    info "[3/6] 构建后端..."
    
    cd "$BACKEND_DIR"
    
    if ! mvn clean package -DskipTests -q 2>&1; then
        die "后端构建失败"
    fi
    
    JAR_FILE=$(find "$BACKEND_DIR/target" -name "*.jar" -not -name "*original*" | head -1)
    if [ -z "$JAR_FILE" ]; then
        die "找不到构建产物 JAR 文件"
    fi
    
    JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
    success "后端构建完成: $(basename "$JAR_FILE") ($JAR_SIZE)"
    echo ""
else
    info "[3/6] 跳过后端构建 (--skip-build)"
    echo ""
fi

# ===== 步骤 4: 构建前端 =====
if $DO_BUILD; then
    info "[4/6] 构建前端..."
    
    cd "$FRONTEND_DIR"
    
    # 检查 node_modules
    if [ ! -d "node_modules" ] || [ ! -f "node_modules/.package-lock.json" ]; then
        info "  安装前端依赖..."
        npm ci --prefer-offline 2>&1 || npm install 2>&1
    fi
    
    if ! npm run build 2>&1; then
        die "前端构建失败"
    fi
    
    DIST_SIZE=$(du -sh "$FRONTEND_DIR/dist" | cut -f1)
    success "前端构建完成 ($DIST_SIZE)"
    echo ""
else
    info "[4/6] 跳过前端构建 (--skip-build)"
    echo ""
fi

# ===== 步骤 5: 重启服务 =====
info "[5/6] 重启服务..."

# 重启后端
systemctl restart "$SERVICE_NAME" 2>&1 || die "后端服务重启失败"
success "后端服务已重启"

# 重载 Nginx（前端文件已直接替换，无需重启）
if nginx -t 2>/dev/null; then
    systemctl reload nginx 2>&1 || true
    success "Nginx 已重载"
else
    warn "Nginx 配置检查失败，跳过重载"
fi

echo ""

# ===== 步骤 6: 健康检查 =====
info "[6/6] 健康检查..."

HEALTH_OK=false
ELAPSED=0

while [ $ELAPSED -lt $HEALTH_TIMEOUT ]; do
    sleep 2
    ELAPSED=$((ELAPSED + 2))
    
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL" 2>/dev/null || echo "000")
    
    if [ "$HTTP_CODE" = "200" ]; then
        HEALTH_OK=true
        break
    fi
    
    # 显示进度
    printf "\r  等待后端启动... %ds / %ds (HTTP $HTTP_CODE)" "$ELAPSED" "$HEALTH_TIMEOUT"
done
echo ""

if $HEALTH_OK; then
    success "后端健康检查通过 ✅"
else
    # 端口检查作为兜底
    if ss -tlnp | grep -q ':9090'; then
        HEALTH_OK=true
        success "后端端口 9090 已监听 ✅"
    else
        warn "健康检查超时 (端口 9090 未监听)"
        warn "查看后端日志: journalctl -u $SERVICE_NAME -f"
    fi
fi

# 检查前端
FRONTEND_CODE=$(curl -s -o /dev/null -w "%{http_code}" "https://zhang0903.top" 2>/dev/null || echo "000")
if [ "$FRONTEND_CODE" = "200" ]; then
    success "前端访问正常 ✅ (HTTP $FRONTEND_CODE)"
else
    warn "前端访问异常 (HTTP $FRONTEND_CODE)"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  🎉 部署完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "  服务: https://zhang0903.top"
echo -e "  API:  https://zhang0903.top/api/"
echo -e "  日志: journalctl -u $SERVICE_NAME -f"
echo -e "  回滚: bash $PROJECT_DIR/scripts/rollback.sh"
echo ""
