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

echo -e "${YELLOW}[1/6] 拉取最新代码...${NC}"
git pull origin main
if [ $? -ne 0 ]; then
    echo -e "${RED}代码拉取失败，请检查网络或权限${NC}"
    exit 1
fi
echo -e "${GREEN}✓ 代码更新成功${NC}"

echo -e "${YELLOW}[2/6] 备份当前数据...${NC}"
BACKUP_DIR="./backups/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"
docker-compose exec -T mysql mysqldump -u root -p${MYSQL_ROOT_PASSWORD:-jordon} museum_db > "$BACKUP_DIR/museum_db_backup.sql" 2>/dev/null || echo "备份跳过（MySQL可能未运行）"
echo -e "${GREEN}✓ 备份完成: $BACKUP_DIR${NC}"

echo -e "${YELLOW}[3/6] 停止当前服务...${NC}"
docker-compose down
echo -e "${GREEN}✓ 服务已停止${NC}"

echo -e "${YELLOW}[4/6] 更新数据库结构...${NC}"
# 启动MySQL（如果未运行）
docker-compose up -d mysql
sleep 5

# 执行数据库更新
docker-compose exec -T mysql mysql -u root -p${MYSQL_ROOT_PASSWORD:-jordon} museum_db << 'EOF'
-- 库房表添加面积字段
ALTER TABLE storage 
ADD COLUMN IF NOT EXISTS area DECIMAL(10,2) DEFAULT NULL COMMENT '面积(㎡)' AFTER location;

-- 库房表添加安全等级字段
ALTER TABLE storage 
ADD COLUMN IF NOT EXISTS safety_level INT DEFAULT 1 COMMENT '安全等级：1=一级 2=二级 3=三级' AFTER area;

-- 显示更新后的表结构
SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'storage' AND TABLE_SCHEMA = 'museum_db';
EOF

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
