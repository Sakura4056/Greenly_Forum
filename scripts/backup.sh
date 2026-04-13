#!/bin/bash
###############################################################################
# Greenly 数据库备份脚本
# 功能: 自动备份 MySQL，支持轮转清理
# 用法: bash backup.sh [--full] [--keep-days 7]
###############################################################################
set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
CYAN='\033[0;36m'
NC='\033[0m'

PROJECT_DIR="/opt/new_new_Greenly"
BACKEND_DIR="$PROJECT_DIR/plant-backend"
BACKUP_DIR="$PROJECT_DIR/backups"
KEEP_DAYS=7
FULL_BACKUP=false

for arg in "$@"; do
    case $arg in
        --full)         FULL_BACKUP=true ;;
        --keep-days=*)  KEEP_DAYS="${arg#*=}" ;;
        --help|-h)
            echo "用法: bash backup.sh [选项]"
            echo ""
            echo "选项:"
            echo "  --full             完整备份（含存储过程、触发器）"
            echo "  --keep-days=N      保留最近 N 天的备份（默认 7）"
            echo "  -h, --help         显示帮助"
            exit 0
            ;;
    esac
done

mkdir -p "$BACKUP_DIR"

# 读取数据库密码
DB_PASS=$(grep '^DB_PASSWORD=' "$BACKEND_DIR/.env" 2>/dev/null | cut -d= -f2- || echo "")
if [ -z "$DB_PASS" ]; then
    echo -e "${RED}✗ 未找到 DB_PASSWORD，请检查 $BACKEND_DIR/.env${NC}"
    exit 1
fi

TIMESTAMP=$(date '+%Y%m%d_%H%M%S')
BACKUP_FILE="$BACKUP_DIR/greenly_db_${TIMESTAMP}.sql"

echo -e "${CYAN}[备份] 开始备份 greenly_db...${NC}"

# 构建 mysqldump 参数
DUMP_OPTS="--single-transaction --routines --triggers --set-gtid-purged=OFF"
if $FULL_BACKUP; then
    DUMP_OPTS="$DUMP_OPTS --events --all-databases"
    BACKUP_FILE="$BACKUP_DIR/greenly_full_${TIMESTAMP}.sql"
fi

# 执行备份
if mysqldump -u root -p"$DB_PASS" greenly_db $DUMP_OPTS > "$BACKUP_FILE" 2>/dev/null; then
    # 压缩
    gzip "$BACKUP_FILE"
    BACKUP_SIZE=$(du -h "${BACKUP_FILE}.gz" | cut -f1)
    echo -e "${GREEN}✓ 备份完成: $(basename "${BACKUP_FILE}.gz") ($BACKUP_SIZE)${NC}"
    
    # 记录备份信息
    echo "${TIMESTAMP} ${BACKUP_SIZE}" >> "$BACKUP_DIR/backup_history.log"
else
    echo -e "${RED}✗ 备份失败${NC}"
    rm -f "$BACKUP_FILE"
    exit 1
fi

# 清理旧备份
DELETED=$(find "$BACKUP_DIR" -name "greenly_*.sql.gz" -mtime +$KEEP_DAYS -delete -print 2>/dev/null | wc -l)
if [ "$DELETED" -gt 0 ]; then
    echo -e "${YELLOW}⚠ 已清理 $DELETED 个过期备份（>${KEEP_DAYS} 天）${NC}"
fi

# 显示备份列表
echo ""
echo -e "${CYAN}当前备份:${NC}"
ls -lh "$BACKUP_DIR"/greenly_*.sql.gz 2>/dev/null | tail -5 | while read -r line; do
    echo "  $line"
done

TOTAL_SIZE=$(du -sh "$BACKUP_DIR" 2>/dev/null | cut -f1)
BACKUP_COUNT=$(ls "$BACKUP_DIR"/greenly_*.sql.gz 2>/dev/null | wc -l)
echo ""
echo -e "  共 ${BACKUP_COUNT} 个备份，总大小 ${TOTAL_SIZE}"
