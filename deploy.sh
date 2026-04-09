#!/bin/bash

# ==========================================
# 博物馆藏品管理系统 - 自动化部署脚本
# ==========================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_msg() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否以 root 权限运行
if [ "$EUID" -ne 0 ]; then
    print_error "请使用 root 权限运行此脚本"
    exit 1
fi

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

print_msg "=========================================="
print_msg "  博物馆藏品管理系统 - 部署脚本"
print_msg "=========================================="

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    print_warn "Docker 未安装，正在安装..."
    curl -fsSL https://get.docker.com | sh
    systemctl enable docker
    systemctl start docker
fi

# 检查 Docker Compose 是否安装
if ! command -v docker-compose &> /dev/null; then
    print_warn "Docker Compose 未安装，正在安装..."
    curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
fi

# 检查防火墙
print_msg "配置防火墙..."
if command -v firewall-cmd &> /dev/null; then
    firewall-cmd --permanent --add-port=80/tcp
    firewall-cmd --permanent --add-port=443/tcp
    firewall-cmd --reload
elif command -v ufw &> /dev/null; then
    ufw allow 80/tcp
    ufw allow 443/tcp
fi

# 编译前端
print_msg "编译前端..."
cd "$SCRIPT_DIR/museum-ui"
if [ ! -d "node_modules" ]; then
    npm install
fi
npm run build

# 拉取 Docker 镜像
print_msg "拉取 Docker 镜像..."
docker-compose pull

# 停止旧容器
print_msg "停止旧容器..."
docker-compose down

# 启动服务
print_msg "启动服务..."
docker-compose up -d --build

# 等待服务启动
print_msg "等待服务启动..."
sleep 30

# 检查服务状态
print_msg "检查服务状态..."
docker-compose ps

# 显示访问地址
print_msg "=========================================="
print_msg "  部署完成！"
print_msg "=========================================="
print_msg "前端地址: http://$(hostname -I | awk '{print $1}')"
print_msg "API文档:  http://$(hostname -I | awk '{print $1}'):8080/doc.html"
print_msg ""
print_msg "数据库初始化将在首次启动时自动完成"
print_msg "默认账号: admin / admin123"

# 查看日志命令
print_msg ""
print_msg "查看日志命令:"
echo "  docker-compose logs -f"
echo "  docker-compose logs -f backend"
