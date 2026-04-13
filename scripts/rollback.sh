#!/bin/bash
###############################################################################
# Greenly 回滚脚本
# 功能: 回退到上一个部署版本（git + 数据库）
# 用法: bash rollback.sh [--commit HASH] [--db-only] [--code-only]
###############################################################################
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

PROJECT_DIR="/opt/new_new_Greenly"
BACKEND_DIR="$PROJECT_DIR/plant-backend"
FRONTEND_DIR="$PROJECT_DIR/plant-frontend"
BACKUP_DIR="$PROJECT_DIR/backups"
SERVICE_NAME="greenly-backend"

TARGET_COMMIT=""
DB_ONLY=false
CODE_ONLY=false

for arg in "$@"; do
    case $arg in
        --commit=*)   TARGET_COMMIT="${arg#*=}" ;;
        --db-only)    DB_ONLY=true ;;
        --code-only)  CODE_ONLY=true ;;
        --help|-h)
            echo "用法: bash rollback.sh [选项]"
            echo ""
            echo "选项:"
            echo "  --commit=HASH  回退到指定 commit"
            echo "  --db-only      仅恢复数据库"
            echo "  --code-only    仅回退代码"
            echo "  -h, --help     显示帮助"
            echo ""
            echo "不加参数: 回退到上一次部署的 commit"
            exit 0
            ;;
    esac
done

echo -e "${CYAN}===== Greenly 回滚 =====${NC}"
echo ""

# === 代码回滚 ===
if ! $DB_ONLY; then
    cd "$PROJECT_DIR"
    
    # 确定目标 commit
    if [ -n "$TARGET_COMMIT" ]; then
        ROLLBACK_TO="$TARGET_COMMIT"
    elif [ -f ".last_deploy_commit" ]; then
        ROLLBACK_TO=$(cat .last_deploy_commit)
    else
        # 回退到上一个 commit
        ROLLBACK_TO=$(git rev-parse HEAD~1 2>/dev/null || echo "")
        if [ -z "$ROLLBACK_TO" ]; then
            echo -e "${RED}✗ 无法确定回滚目标${NC}"
            exit 1
        fi
    fi
    
    SHORT=$(git rev-parse --short "$ROLLBACK_TO" 2>/dev/null || echo "$ROLLBACK_TO")
    CURRENT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
    
    echo -e "${YELLOW}代码回滚: $CURRENT → $SHORT${NC}"
    
    # 显示将丢失的 commit
    LOST_COMMITS=$(git log --oneline "$ROLLBACK_TO..HEAD" 2>/dev/null | head -5)
    if [ -n "$LOST_COMMITS" ]; then
        echo -e "${YELLOW}将丢失的提交:${NC}"
        echo "$LOST_COMMITS" | while read -r line; do
            echo -e "  ${RED}× $line${NC}"
        done
        echo ""
    fi
    
    # 保存当前 HEAD 以便反悔
    echo "$(git rev-parse HEAD)" > "$PROJECT_DIR/.before_rollback_commit"
    
    read -p "确认回滚代码? [y/N] " confirm
    if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
        echo "已取消"
        exit 0
    fi
    
    git stash 2>/dev/null || true
    git checkout "$ROLLBACK_TO" -- . 2>&1
    
    # 重新构建后端
    echo -e "${CYAN}重新构建后端...${NC}"
    cd "$BACKEND_DIR"
    mvn clean package -DskipTests -q 2>&1
    
    # 重启服务
    systemctl restart "$SERVICE_NAME"
    echo -e "${GREEN}✓ 后端已回滚并重启${NC}"
    
    # Nginx
    nginx -t 2>/dev/null && systemctl reload nginx
    echo -e "${GREEN}✓ Nginx 已重载${NC}"
fi

# === 数据库回滚 ===
if ! $CODE_ONLY; then
    echo ""
    echo -e "${CYAN}可用的数据库备份:${NC}"
    
    BACKUPS=($(ls -t "$BACKUP_DIR"/greenly_db_*.sql.gz 2>/dev/null || true))
    
    if [ ${#BACKUPS[@]} -eq 0 ]; then
        echo -e "${YELLOW}⚠ 没有找到数据库备份${NC}"
    else
        for i in "${!BACKUPS[@]}"; do
            FNAME=$(basename "${BACKUPS[$i]}")
            FSIZE=$(du -h "${BACKUPS[$i]}" | cut -f1)
            FDATE=$(stat -c %y "${BACKUPS[$i]}" 2>/dev/null | cut -d. -f1)
            echo -e "  [$i] $FNAME ($FSIZE) - $FDATE"
        done
        
        echo ""
        read -p "选择要恢复的备份编号 [0]: " choice
        choice=${choice:-0}
        
        if [ -z "${BACKUPS[$choice]+x}" ]; then
            echo -e "${RED}✗ 无效的选择${NC}"
        else
            RESTORE_FILE="${BACKUPS[$choice]}"
            echo -e "${YELLOW}将从 $(basename "$RESTORE_FILE") 恢复数据库${NC}"
            read -p "确认? [y/N] " confirm
            
            if [[ "$confirm" =~ ^[Yy]$ ]]; then
                DB_PASS=$(grep '^DB_PASSWORD=' "$BACKEND_DIR/.env" 2>/dev/null | cut -d= -f2-)
                
                # 先备份当前库
                BEFORE_RESTORE="$BACKUP_DIR/before_rollback_$(date '+%Y%m%d_%H%M%S').sql"
                mysqldump -u root -p"$DB_PASS" greenly_db --single-transaction > "$BEFORE_RESTORE" 2>/dev/null
                gzip "$BEFORE_RESTORE"
                echo -e "${GREEN}✓ 当前库已备份: $(basename "${BEFORE_RESTORE}.gz")${NC}"
                
                # 恢复
                gunzip -c "$RESTORE_FILE" | mysql -u root -p"$DB_PASS" greenly_db 2>/dev/null
                echo -e "${GREEN}✓ 数据库已恢复${NC}"
                
                # 重启后端以刷新连接
                systemctl restart "$SERVICE_NAME"
                echo -e "${GREEN}✓ 后端已重启${NC}"
            else
                echo "已取消数据库恢复"
            fi
        fi
    fi
fi

echo ""
echo -e "${GREEN}回滚完成 ✓${NC}"
echo -e "如需撤销回滚: git checkout \$(cat $PROJECT_DIR/.before_rollback_commit)"
