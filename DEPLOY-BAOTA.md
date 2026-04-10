# 博物馆系统 - 宝塔面板部署指南

> 架构：宝塔 Nginx（宿主机）+ Docker Compose（MySQL + Redis + Spring Boot）

---

## 一、前置条件

- 服务器已安装宝塔面板（7.x / 9.x 均可）
- 宝塔面板已安装：**Nginx**、**Docker**（在软件商店安装）
- 项目文件已上传到服务器（建议路径：`/opt/museum/`）

---

## 二、上传项目文件

```bash
# 在服务器上创建目录
mkdir -p /opt/museum

# 将本地项目上传（可用宝塔文件管理器，或 scp）
# scp -r ./museum-server ./museum-ui ./docker-compose.yml root@你的IP:/opt/museum/
```

---

## 三、编译前端（本地完成后上传 dist）

```bash
# 本地执行
cd museum-ui
npm install
npm run build
# 将生成的 dist/ 目录上传到服务器 /opt/museum/museum-ui/dist/
```

---

## 四、用宝塔 Docker 部署后端服务

> 部署 MySQL + Redis + Spring Boot 三个容器（Nginx 由宝塔接管，不在此启动）

### 4.1 通过宝塔面板操作

1. 登录宝塔面板 → 左侧菜单 **Docker**
2. 点击 **Compose** 标签页
3. 点击 **添加** → 选择 `docker-compose.yml` 文件，或直接粘贴内容
4. 项目名填写：`museum`
5. 点击 **启动**

### 4.2 或直接 SSH 执行

```bash
cd /opt/museum

# 首次启动（会自动 build 后端镜像，需要几分钟）
docker compose up -d

# 查看启动状态
docker compose ps

# 查看后端日志
docker compose logs -f backend
```

### 4.3 确认容器正常

```bash
# 应该看到 3 个容器：museum-mysql、museum-redis、museum-backend
docker ps

# 测试后端接口
curl http://localhost:8080/api/v1/auth/captcha
```

---

## 五、在宝塔面板配置 Nginx 站点

### 5.1 新建站点

1. 宝塔面板 → **网站** → **添加站点**
2. 域名填你的域名或服务器 IP
3. 根目录填：`/opt/museum/museum-ui/dist`
4. PHP 版本选：**纯静态**
5. 点击提交

### 5.2 修改站点配置

1. 点击站点右侧 **设置**
2. 选择 **配置文件** 标签
3. **清空**原有内容，粘贴 `nginx.bt.conf` 的全部内容

```nginx
server {
    listen 80;
    server_name 你的域名或IP;

    root /opt/museum/museum-ui/dist;
    index index.html;

    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/json application/xml;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    location /doc.html {
        proxy_pass http://127.0.0.1:8080/doc.html;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /swagger-ui/ {
        proxy_pass http://127.0.0.1:8080/swagger-ui/;
    }

    location /v3/api-docs {
        proxy_pass http://127.0.0.1:8080/v3/api-docs;
    }

    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    access_log  /www/wwwlogs/museum.log;
    error_log   /www/wwwlogs/museum.error.log;
}
```

4. 点击 **保存** → Nginx 自动重载

### 5.3 申请 SSL 证书（可选）

1. 站点设置 → **SSL** 标签
2. 选 **Let's Encrypt** → 勾选域名 → 申请
3. 开启 **强制HTTPS**

---

## 六、开放防火墙端口

在宝塔面板 → **安全** → 放行以下端口：

| 端口 | 用途 |
|------|------|
| 80   | HTTP |
| 443  | HTTPS |
| 8080 | 后端API（如需外部访问） |

> **注意**：如果只对外暴露 80/443，8080 可以不放行（宝塔 Nginx 反向代理即可）

---

## 七、验证部署

```bash
# 1. 检查容器状态
docker compose -f /opt/museum/docker-compose.yml ps

# 2. 检查 Nginx 配置
nginx -t

# 3. 访问系统
# 前端：http://你的域名
# 后端文档：http://你的域名/doc.html

# 4. 默认账号
# 用户名：admin
# 密码：admin123
```

---

## 八、后续更新流程

### 只更新前端
```bash
# 本地 build 后，上传新的 dist 目录到 /opt/museum/museum-ui/dist/
# 无需重启任何服务
```

### 更新后端
```bash
cd /opt/museum
docker compose build backend   # 重新构建后端镜像
docker compose up -d backend   # 重启后端容器
```

### 全量重新部署
```bash
cd /opt/museum
docker compose down
docker compose up -d
```

---

## 九、常见问题

### 前端 API 请求 404
- 检查宝塔 Nginx 中 `/api/` 的 proxy_pass 是否为 `http://127.0.0.1:8080/`
- 注意 proxy_pass 末尾要有 `/`

### 后端容器启动失败
```bash
docker compose logs backend   # 查看错误日志
```
常见原因：MySQL 还没 ready，等几秒后再试
```bash
docker compose restart backend
```

### 页面刷新 404
- 确认 `location /` 里有 `try_files $uri $uri/ /index.html;`

### 宝塔 Nginx 与 Docker Nginx 端口冲突
- 本方案已去掉 Docker nginx 容器，不会冲突
- 确认 `docker compose ps` 里没有 `museum-nginx` 容器
