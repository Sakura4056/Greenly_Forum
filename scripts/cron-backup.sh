#!/bin/bash
###############################################################################
# Greenly 定时备份配置
# 功能: 设置 crontab 每天凌晨 2:00 自动备份数据库
# 用法: bash cron-backup.sh [--time HH:MM] [--remove]
###############################################################################
set -u

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

PROJECT_DIR="/opt/new_new_Greenly"
BACKUP_SCRIPT="$PROJECT_DIR/scripts/backup.sh"
CRON_TIME="0 2 * * *"
CRON_TAG="# greenly-db-backup"
REMOVE=false

for arg in "$@"; do
    case $arg in
        --time=*)  TIME="${arg#*=}"; HOUR=${TIME%%:*}; MIN=${TIME##*:}
                   CRON_TIME="$MIN $HOUR * * *" ;;
        --remove)  REMOVE=true ;;
        --help|-h)
            echo "用法: bash cron-backup.sh [选项]"
            echo ""
            echo "选项:"
            echo "  --time=HH:MM  设置备份时间（默认 02:00）"
            echo "  --remove      移除定时备份"
            exit 0
            ;;
    esac
done

if $REMOVE; then
    crontab -l 2>/dev/null | grep -v "$CRON_TAG" | crontab -
    echo -e "${GREEN}✓ 定时备份已移除${NC}"
    exit 0
fi

# 确保备份脚本可执行
chmod +x "$BACKUP_SCRIPT"

# 添加 cron job（幂等）
EXISTING=$(crontab -l 2>/dev/null | grep "$CRON_TAG" || true)
if [ -n "$EXISTING" ]; then
    echo -e "${YELLOW}⚠ 定时备份已存在: $EXISTING${NC}"
    echo -e "${CYAN}如需修改，请先运行: bash cron-backup.sh --remove${NC}"
else
    (crontab -l 2>/dev/null || true; echo "$CRON_TIME /bin/bash $BACKUP_SCRIPT --keep-days=7 $CRON_TAG") | crontab -
    echo -e "${GREEN}✓ 定时备份已配置${NC}"
    echo -e "  时间: $(echo $CRON_TIME | awk '{print $2":"$1}') 每天"
    echo -e "  脚本: $BACKUP_SCRIPT"
    echo -e "  保留: 7 天"
fi

echo ""
echo -e "${CYAN}当前 crontab:${NC}"
crontab -l 2>/dev/null || echo "  (空)"
