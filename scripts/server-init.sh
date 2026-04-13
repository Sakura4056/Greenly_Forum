#!/bin/bash
###############################################################################
# Greenly 服务器初始化脚本（阿里云 Linux 3）
# 功能: 安装所有依赖、配置服务
# 用法: bash server-init.sh
###############################################################################
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

echo -e "${CYAN}===== Greenly 服务器初始化 =====${NC}"
echo ""

# 检查 root
if [ "$EUID" -ne 0 ]; then
    echo -e "${RED}请使用 root 运行此脚本${NC}"
    exit 1
fi

# === 1. 系统更新 ===
echo -e "${YELLOW}[1/8] 更新系统...${NC}"
dnf update -y -q 2>/dev/null || yum update -y -q 2>/dev/null
echo -e "${GREEN}✓ 系统更新完成${NC}"

# === 2. 安装 Java 21 ===
echo -e "${YELLOW}[2/8] 检查 Java...${NC}"
if java -version 2>&1 | grep -q "21"; then
    echo -e "${GREEN}✓ Java 21 已安装${NC}"
else
    echo "  安装 Java 21..."
    dnf install -y java-21-openjdk java-21-openjdk-devel 2>/dev/null || \
    yum install -y java-21-openjdk java-21-openjdk-devel 2>/dev/null
    echo -e "${GREEN}✓ Java 21 安装完成${NC}"
fi

# === 3. 安装 Maven ===
echo -e "${YELLOW}[3/8] 检查 Maven...${NC}"
if command -v mvn &>/dev/null; then
    echo -e "${GREEN}✓ Maven $(mvn -version 2>&1 | head -1 | awk '{print $3}') 已安装${NC}"
else
    echo "  安装 Maven..."
    MAVEN_VER="3.9.6"
    curl -fsSL "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VER}/binaries/apache-maven-${MAVEN_VER}-bin.tar.gz" -o /tmp/maven.tar.gz
    tar -xzf /tmp/maven.tar.gz -C /opt/
    ln -sf "/opt/apache-maven-${MAVEN_VER}/bin/mvn" /usr/local/bin/mvn
    rm -f /tmp/maven.tar.gz
    echo -e "${GREEN}✓ Maven ${MAVEN_VER} 安装完成${NC}"
fi

# === 4. 安装 Node.js 20 ===
echo -e "${YELLOW}[4/8] 检查 Node.js...${NC}"
if command -v node &>/dev/null && node -v | grep -q "^v2[0-9]"; then
    echo -e "${GREEN}✓ Node.js $(node -v) 已安装${NC}"
else
    echo "  安装 Node.js 20..."
    curl -fsSL https://rpm.nodesource.com/setup_20.x | bash -
    dnf install -y nodejs 2>/dev/null || yum install -y nodejs 2>/dev/null
    echo -e "${GREEN}✓ Node.js $(node -v) 安装完成${NC}"
fi

# === 5. 安装 MySQL 8.0 ===
echo -e "${YELLOW}[5/8] 检查 MySQL...${NC}"
if command -v mysql &>/dev/null; then
    echo -e "${GREEN}✓ MySQL $(mysql --version 2>&1 | awk '{print $3}') 已安装${NC}"
else
    echo "  安装 MySQL 8.0..."
    dnf install -y mysql-server mysql 2>/dev/null || yum install -y mysql-server mysql 2>/dev/null
    systemctl enable --now mysqld
    echo -e "${GREEN}✓ MySQL 安装完成${NC}"
fi

# === 6. 安装 Redis ===
echo -e "${YELLOW}[6/8] 检查 Redis...${NC}"
if command -v redis-server &>/dev/null; then
    echo -e "${GREEN}✓ Redis 已安装${NC}"
else
    echo "  安装 Redis..."
    dnf install -y redis 2>/dev/null || yum install -y redis 2>/dev/null
    systemctl enable --now redis
    echo -e "${GREEN}✓ Redis 安装完成${NC}"
fi

# === 7. 安装 Nginx ===
echo -e "${YELLOW}[7/8] 检查 Nginx...${NC}"
if command -v nginx &>/dev/null; then
    echo -e "${GREEN}✓ Nginx 已安装${NC}"
else
    echo "  安装 Nginx..."
    dnf install -y nginx 2>/dev/null || yum install -y nginx 2>/dev/null
    systemctl enable --now nginx
    echo -e "${GREEN}✓ Nginx 安装完成${NC}"
fi

# === 8. 配置防火墙 ===
echo -e "${YELLOW}[8/8] 配置防火墙...${NC}"
if command -v firewall-cmd &>/dev/null; then
    firewall-cmd --permanent --add-service=http 2>/dev/null || true
    firewall-cmd --permanent --add-service=https 2>/dev/null || true
    firewall-cmd --permanent --add-service=ssh 2>/dev/null || true
    firewall-cmd --reload 2>/dev/null || true
    echo -e "${GREEN}✓ 防火墙已配置 (HTTP/HTTPS/SSH)${NC}"
else
    echo -e "${YELLOW}⚠ 未检测到 firewalld，跳过防火墙配置${NC}"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  🎉 服务器初始化完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 验证
echo -e "${CYAN}环境验证:${NC}"
echo -e "  Java:    $(java -version 2>&1 | head -1)"
echo -e "  Maven:   $(mvn -version 2>&1 | head -1)"
echo -e "  Node.js: $(node -v)"
echo -e "  npm:     $(npm -v)"
echo -e "  MySQL:   $(mysql --version 2>&1 | awk '{print $1, $3}')"
echo -e "  Redis:   $(redis-cli --version 2>&1)"
echo -e "  Nginx:   $(nginx -v 2>&1)"
echo ""
echo -e "${CYAN}下一步: bash $PROJECT_DIR/scripts/deploy.sh${NC}"
