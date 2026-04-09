# 博物馆藏品管理系统 - 云服务器部署指南

## 服务器要求

| 项目 | 最低配置 | 推荐配置 |
|------|---------|---------|
| CPU | 2核 | 4核 |
| 内存 | 4GB | 8GB |
| 系统盘 | 40GB SSD | 80GB SSD |
| 数据盘 | 100GB | 500GB SSD |
| 操作系统 | CentOS 7+ / Ubuntu 20.04+ | CentOS 8 / Ubuntu 22.04 |

## 端口规划

| 端口 | 服务 | 说明 |
|------|------|------|
| 80 | Nginx | 前端静态资源 |
| 443 | Nginx | HTTPS |
| 8080 | Spring Boot | 后端API |
| 3306 | MySQL | 数据库 |
| 6379 | Redis | 缓存 |

## 部署方式

### 方式一：Docker Compose 一键部署（推荐）

```bash
# 1. 安装 Docker 和 Docker Compose
curl -fsSL https://get.docker.com | bash
sudo systemctl enable docker
sudo systemctl start docker

# 2. 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.24.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 3. 创建项目目录
sudo mkdir -p /opt/museum
cd /opt/museum

# 4. 上传项目文件到服务器后，启动服务
sudo docker-compose up -d
```

### 方式二：传统方式部署

#### 1. 安装基础环境

```bash
# CentOS 7
sudo yum update -y
sudo yum install -y java-17-openjdk java-17-openjdk-devel nginx mysql-server redis git

# Ubuntu
sudo apt update -y
sudo apt install -y openjdk-17-jdk nginx mysql-server redis-server git
```

#### 2. 配置 MySQL

```bash
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 初始化数据库
mysql -u root -p < museum-server/src/main/resources/db/init.sql
```

#### 3. 配置 Redis

```bash
sudo systemctl start redis
sudo systemctl enable redis
```

#### 4. 部署后端

```bash
# 编译项目
cd /opt/museum/museum-server
./mvnw clean package -DskipTests

# 运行JAR
nohup java -jar target/museum-server-1.0.0-SNAPSHOT.jar --server.port=8080 &
```

#### 5. 部署前端

```bash
# 编译前端
cd /opt/museum/museum-ui
npm install
npm run build

# 配置 Nginx
sudo cp -r dist/* /usr/share/nginx/html/
sudo cp nginx.conf /etc/nginx/conf.d/museum.conf

# 重启 Nginx
sudo systemctl restart nginx
```

## Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;  # 替换为你的域名

    # 前端静态文件
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Swagger 文档
    location /doc.html {
        proxy_pass http://127.0.0.1:8080/doc.html;
    }
}
```

## SSL 证书配置（可选）

```bash
# 安装 Certbot
sudo yum install -y certbot python3-certbot-nginx

# 获取证书
sudo certbot --nginx -d your-domain.com

# 自动续期
sudo certbot renew --dry-run
```

## 环境变量配置

创建 `.env` 文件配置生产环境：

```env
# 数据库
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/museum?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
SPRING_DATASOURCE_USERNAME=museum
SPRING_DATASOURCE_PASSWORD=your_password

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# Sa-Token
SA_TOKEN_SECRET=your_random_secret_key_at_least_32_chars
```

## 防火墙配置

```bash
# CentOS
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload

# Ubuntu (UFW)
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
```

## 启动脚本

```bash
#!/bin/bash
# start.sh

# 启动 MySQL
systemctl start mysqld

# 启动 Redis
systemctl start redis

# 启动后端
cd /opt/museum/museum-server
nohup java -jar target/museum-server-1.0.0-SNAPSHOT.jar --server.port=8080 > logs/app.log 2>&1 &

# 重启 Nginx
systemctl restart nginx

echo "Museum system started successfully!"
```

## 访问地址

部署完成后，访问：

- 前端：http://your-server-ip
- 后端API：http://your-server-ip:8080
- API文档：http://your-server-ip:8080/doc.html

## 常见问题

### 1. 数据库连接失败
- 检查 MySQL 是否启动：`systemctl status mysqld`
- 检查数据库用户权限
- 检查防火墙端口

### 2. 前端无法访问后端API
- 检查 Nginx 代理配置
- 检查后端服务是否正常启动
- 检查 SELinux 设置：`setsebool -P httpd_can_network_connect 1`

### 3. 内存不足
- 调整 JVM 堆内存：`java -Xms512m -Xmx1024m -jar ...`
- 添加 Swap 分区

## 备份策略

```bash
# 数据库备份脚本
#!/bin/bash
# backup.sh

BACKUP_DIR="/opt/museum/backups"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR
mysqldump -u root -p museum > $BACKUP_DIR/museum_$DATE.sql

# 保留最近30天备份
find $BACKUP_DIR -name "museum_*.sql" -mtime +30 -delete
```

## 监控建议

- 使用 **Prometheus + Grafana** 监控服务器资源
- 使用 **PM2** 或 **Supervisor** 管理 Java 进程
- 配置 **日志管理** 定期清理日志文件
