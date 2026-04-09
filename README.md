# RFID智能博物馆藏品管理系统

基于Spring Boot 3 + Vue 3的RFID智能博物馆藏品管理系统，采用文博色系设计，支持藏品管理、库房管理、入出库管理、工作流审批等功能。

## 技术栈

### 后端
- Spring Boot 3.2.5
- Sa-Token 1.37 (认证鉴权)
- MyBatis-Plus 3.5.6
- MySQL 8 / 达梦DM8
- Redis 7
- Knife4j 4.4 (API文档)

### 前端
- Vue 3 + TypeScript
- Element Plus
- Pinia (状态管理)
- Vite 5
- ECharts (可视化)

## 系统功能

### 已完成模块
- ✅ 用户认证（登录/验证码/密码加密）
- ✅ 系统管理（用户/角色/菜单/部门/操作日志）
- ✅ 藏品管理（CRUD + 状态管理）
- ✅ 库房管理（库房/库区/货架）
- ✅ 入出库管理

### 待开发模块
- 🔄 RFID标签管理
- 🔄 盘点管理
- 🔄 工作流审批
- 🔄 数据可视化大屏

## 项目结构

```
museum-server/    # Spring Boot 后端
museum-ui/        # Vue 3 前端
```

## 快速开始

### 后端启动

```bash
cd museum-server
./mvnw.cmd spring-boot:run  # Windows
# 或
./mvnw spring-boot:run      # Linux/Mac
```

### 前端启动

```bash
cd museum-ui
npm install
npm run dev
```

### 默认账号
- 用户名: admin
- 密码: admin123

## 部署指南

详见 [DEPLOY.md](./DEPLOY.md)

## 许可证

MIT License
