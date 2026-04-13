#!/bin/bash
###############################################################################
# Greenly 服务状态总览
# 用法: bash status.sh
###############################################################################
set -euo pipefail
shopt -s nullglob

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

PROJECT_DIR="/opt/new_new_Greenly"
SERVICE_NAME="greenly-backend"

# 辅助函数
status_icon() {
    if [ "$1" = "active" ]; then
        echo -e "${GREEN}● 运行中${NC}"
    else
        echo -e "${RED}○ 已停止${NC}"
    fi
}

http_icon() {
    local code=$1
    if [ "$code" = "200" ]; then
        echo -e "${GREEN}HTTP $code${NC}"
    elif [ "$code" = "000" ]; then
        echo -e "${RED}无响应${NC}"
    else
        echo -e "${YELLOW}HTTP $code${NC}"
    fi
}

echo ""
echo -e "${BOLD}${CYAN}  ╔══════════════════════════════════════╗${NC}"
echo -e "${BOLD}${CYAN}  ║     🌿 Greenly 服务状态总览        ║${NC}"
echo -e "${BOLD}${CYAN}  ╚══════════════════════════════════════╝${NC}"
echo ""

# === 系统资源 ===
echo -e "${BOLD}📊 系统资源${NC}"

CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | awk '{printf "%.1f", $2+$4}')
MEM_TOTAL=$(free -m | awk '/Mem:/ {print $2}')
MEM_USED=$(free -m | awk '/Mem:/ {print $3}')
MEM_PCT=$(awk "BEGIN {printf \"%.1f\", $MEM_USED/$MEM_TOTAL*100}")
MEM_INFO="${MEM_USED}Mi / ${MEM_TOTAL}Mi (${MEM_PCT}%)"
DISK_INFO=$(df -h / | awk 'NR==2 {printf "%s / %s (%s)", $3, $2, $5}')
LOAD=$(uptime | awk -F'load average:' '{print $2}' | xargs)
UPTIME=$(uptime -p 2>/dev/null || uptime | awk -F'up ' '{print $2}' | awk -F',' '{print $1$2}')

echo -e "  CPU:    ${CPU_USAGE}%"
echo -e "  内存:   ${MEM_INFO}"
echo -e "  磁盘:   ${DISK_INFO}"
echo -e "  负载:   ${LOAD}"
echo -e "  运行:   ${UPTIME}"
echo ""

# === 服务状态 ===
echo -e "${BOLD}🔧 服务状态${NC}"

# 后端
BACKEND_STATUS=$(systemctl is-active "$SERVICE_NAME" 2>/dev/null || echo "inactive")
BACKEND_PID=$(systemctl show "$SERVICE_NAME" --property=MainPID 2>/dev/null | cut -d= -f2)
BACKEND_UPTIME=""
if [ "$BACKEND_PID" != "0" ] && [ -n "$BACKEND_PID" ]; then
    BACKEND_UPTIME=$(ps -o etime= -p "$BACKEND_PID" 2>/dev/null | xargs || echo "")
fi
echo -e "  后端:   $(status_icon "$BACKEND_STATUS")  PID: $BACKEND_PID  运行时间: ${BACKEND_UPTIME:-N/A}"

# MySQL
MYSQL_STATUS=$(systemctl is-active mysqld 2>/dev/null || echo "inactive")
MYSQL_CONN=$(mysql -u root -e "SELECT 1" 2>/dev/null && echo "可连接" || echo "需要密码")
echo -e "  MySQL:  $(status_icon "$MYSQL_STATUS")  连接: $MYSQL_CONN"

# Redis
REDIS_STATUS=$(systemctl is-active redis 2>/dev/null || echo "inactive")
REDIS_PONG=$(redis-cli ping 2>/dev/null || echo "FAIL")
echo -e "  Redis:  $(status_icon "$REDIS_STATUS")  响应: $REDIS_PONG"

# Nginx
NGINX_STATUS=$(systemctl is-active nginx 2>/dev/null || echo "inactive")
echo -e "  Nginx:  $(status_icon "$NGINX_STATUS")"
echo ""

# === HTTP 检查 ===
echo -e "${BOLD}🌐 HTTP 端点${NC}"

BACKEND_HTTP=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:9090/api/" 2>/dev/null || echo "000")
FRONTEND_HTTP=$(curl -s -o /dev/null -w "%{http_code}" "https://zhang0903.top" 2>/dev/null || echo "000")
API_HTTP=$(curl -s -o /dev/null -w "%{http_code}" "https://zhang0903.top/api/" 2>/dev/null || echo "000")

echo -e "  后端 API:    $(http_icon "$BACKEND_HTTP")  http://127.0.0.1:9090"
echo -e "  前端:        $(http_icon "$FRONTEND_HTTP")  https://zhang0903.top"
echo -e "  API 代理:    $(http_icon "$API_HTTP")  https://zhang0903.top/api/"

# 端口监听检查
BACKEND_PORT=$(ss -tlnp | grep ':9090' | head -1)
if [ -n "$BACKEND_PORT" ]; then
    echo -e "  后端端口:    ${GREEN}9090 监听中${NC}"
fi
echo ""

# === Git 信息 ===
echo -e "${BOLD}📦 版本信息${NC}"
cd "$PROJECT_DIR"
BRANCH=$(git branch --show-current 2>/dev/null || echo "unknown")
COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
LAST_COMMIT_DATE=$(git log -1 --format="%ci" 2>/dev/null | cut -d' ' -f1)
LAST_COMMIT_MSG=$(git log -1 --format="%s" 2>/dev/null | head -c 60)

echo -e "  分支:   $BRANCH"
echo -e "  提交:   $COMMIT ($LAST_COMMIT_DATE)"
echo -e "  最新:   $LAST_COMMIT_MSG"
echo ""

# === 最近日志 ===
echo -e "${BOLD}📋 最近日志 (后端)${NC}"
journalctl -u "$SERVICE_NAME" --no-pager -n 5 2>/dev/null | while read -r line; do
    echo -e "  ${line}"
done
echo ""

# === 磁盘/备份 ===
echo -e "${BOLD}💾 备份${NC}"
BACKUP_COUNT=$(find "$PROJECT_DIR/backups" -name "greenly_*.sql.gz" 2>/dev/null | wc -l)
LATEST_BACKUP=$(find "$PROJECT_DIR/backups" -name "greenly_*.sql.gz" -printf "%T@ %p\n" 2>/dev/null | sort -rn | head -1 | cut -d' ' -f2-)
if [ -n "$LATEST_BACKUP" ]; then
    BACKUP_DATE=$(stat -c %y "$LATEST_BACKUP" 2>/dev/null | cut -d. -f1)
    BACKUP_SIZE=$(du -h "$LATEST_BACKUP" | cut -f1)
    echo -e "  最近:   $(basename "$LATEST_BACKUP") ($BACKUP_SIZE) - $BACKUP_DATE"
else
    echo -e "  ${YELLOW}暂无备份${NC}"
fi
echo -e "  总数:   $BACKUP_COUNT 个备份"
echo ""

echo -e "${CYAN}  ────────────────────────────────────────${NC}"
echo -e "  部署:  ${GREEN}bash $PROJECT_DIR/scripts/deploy.sh${NC}"
echo -e "  备份:  ${GREEN}bash $PROJECT_DIR/scripts/backup.sh${NC}"
echo -e "  回滚:  ${GREEN}bash $PROJECT_DIR/scripts/rollback.sh${NC}"
echo -e "  日志:  ${GREEN}journalctl -u $SERVICE_NAME -f${NC}"
echo ""
