#!/bin/bash
# 博物馆管理系统 - 服务器更新脚本
# 用法: ./deploy-update.sh

set -e

echo "=========================================="
echo "  博物馆管理系统 - 迭代更新脚本"
echo "=========================================="

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 项目目录
PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

echo -e "${YELLOW}[0/6] 修复 Git 权限...${NC}"
git config --global --add safe.directory "$PROJECT_DIR" 2>/dev/null || true
echo -e "${GREEN}✓ Git 权限已修复${NC}"

echo -e "${YELLOW}[1/6] 处理本地修改并拉取最新代码...${NC}"
# 放弃所有本地修改，强制使用远程版本
git checkout --theirs docker-compose.yml 2>/dev/null || true
git checkout --theirs . 2>/dev/null || true
git add -A
git reset --hard HEAD
git clean -fd

# 拉取最新代码
git pull origin main

if [ $? -ne 0 ]; then
    echo -e "${RED}代码拉取失败，请检查网络或权限${NC}"
    exit 1
fi
echo -e "${GREEN}✓ 代码更新成功${NC}"

echo -e "${YELLOW}[2/6] 备份当前数据...${NC}"
BACKUP_DIR="./backups/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

# 检查MySQL容器是否运行
if docker ps | grep -q museum-mysql; then
    docker exec museum-mysql mysqldump -u root -pmuseum_root_2024 museum_db > "$BACKUP_DIR/museum_db_backup.sql"
    echo -e "${GREEN}✓ 备份完成: $BACKUP_DIR${NC}"
else
    echo -e "${YELLOW}! MySQL未运行，跳过备份${NC}"
fi

echo -e "${YELLOW}[3/6] 停止并清理旧容器...${NC}"
docker-compose down --remove-orphans
# 删除可能存在的旧容器
docker rm -f museum-redis museum-mysql museum-backend 2>/dev/null || true
echo -e "${GREEN}✓ 旧容器已清理${NC}"

echo -e "${YELLOW}[4/6] 更新数据库结构...${NC}"
# 只启动MySQL
docker-compose up -d mysql

# 等待MySQL启动
echo -e "${YELLOW}等待MySQL启动...${NC}"
sleep 10

# 循环等待MySQL就绪
for i in {1..30}; do
    if docker exec museum-mysql mysqladmin ping -h localhost -u root -pmuseum_root_2024 --silent 2>/dev/null; then
        echo -e "${GREEN}✓ MySQL已就绪${NC}"
        break
    fi
    echo -n "."
    sleep 2
done

# 导入业务表 SQL（使用宿主机上的文件）
if [ -f "/www/wwwroot/bowuguan/db/collection_table.sql" ]; then
    echo -e "${YELLOW}导入业务表...${NC}"
    docker exec -i museum-mysql mysql -u root -pmuseum_root_2024 museum_db < /www/wwwroot/bowuguan/db/collection_table.sql 2>/dev/null || echo "表可能已存在"
fi

# 执行数据库更新
docker exec museum-mysql mysql -u root -pmuseum_root_2024 museum_db -e "
ALTER TABLE storage ADD COLUMN IF NOT EXISTS area DECIMAL(10,2) DEFAULT NULL COMMENT '面积(㎡)' AFTER location;
ALTER TABLE storage ADD COLUMN IF NOT EXISTS safety_level INT DEFAULT 1 COMMENT '安全等级：1=一级 2=二级 3=三级' AFTER area;
" 2>/dev/null || echo "字段可能已存在"

echo -e "${GREEN}✓ 数据库更新完成${NC}"

echo -e "${YELLOW}[5/6] 重新构建并启动服务...${NC}"
docker-compose up -d --build
echo -e "${GREEN}✓ 服务已启动${NC}"

echo -e "${YELLOW}[6/6] 检查服务状态...${NC}"
sleep 5
docker-compose ps

echo ""
echo "=========================================="
echo -e "${GREEN}  更新完成！${NC}"
echo "=========================================="
echo ""
echo "访问地址:"
echo "  - 前端: http://localhost"
echo "  - 后端API: http://localhost:8080"
echo "  - API文档: http://localhost:8080/doc.html"
echo ""
echo "查看日志:"
echo "  docker-compose logs -f"
echo ""
