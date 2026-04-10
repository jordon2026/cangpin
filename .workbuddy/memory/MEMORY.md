# 项目长期记忆

## 博物馆管理系统（2026）

- 项目路径：`D:/聚景科技/2026/博物馆/`，源码在 `源码/` 子目录
- 管理端需求文档：`管理端.csv`（原始）、`管理端_完善版.csv`（完善版）、`管理端_需求完善说明.md`
- 系统定位：RFID智能博物馆藏品管理系统，面向中大型博物馆，含Web管理端+移动端+RFID手持终端
- 技术建议：Three.js/BabylonJS（3D库房）、Elasticsearch（全文检索）、Activiti/Flowable（工作流）、等保2.0三级安全标准
- 技术架构方案已确定：模块化单体 Spring Boot3 / Vue3+TS / PostgreSQL15（可替达梦DM8）/ Redis / MinIO / ES / Docker Compose本地部署
- 架构文档：`D:/聚景科技/2026/博物馆/技术架构方案.md`，架构图：`D:/聚景科技/2026/博物馆/技术架构图.html`
- 前端源码：`源码/museum-ui/`（Vue3 + Element Plus + Pinia），后端源码：`源码/museum-server/`（Spring Boot 3 + Sa-Token）
- JDK 17（Corretto），Maven Wrapper（无全局 Maven）
- UI 风格：文博色系（古铜 #8B4513、金 #C9A96E、朱红 #C23531）
- MySQL root 密码：jordon，安装路径 `C:\Program Files\MySQL\MySQL Server 8.0\`
- Redis 6379 运行中（非 Windows 服务，直接运行）
- 后端启动方式：`cd museum-server && .\mvnw.cmd spring-boot:run`
- 前端访问地址：`http://localhost:3000`（不是5173）
- 默认账号 admin/admin123，安全：验证码 + 5次失败锁定 + BCrypt + Sa-Token

## 后端项目（museum-server）- 2026-04-07 搭建

- 路径：`源码/museum-server/`
- 技术栈：Spring Boot 3.2.5 / Sa-Token 1.37 / MyBatis-Plus 3.5.6 / MySQL 8 / Redis / Knife4j 4.4 / Hutool 5.8
- 包名：com.jujing.museum，JDK 17，Maven Wrapper
- 已完成模块：启动类、4个Config（SaToken/Cors/MP/Redis）、通用响应/异常处理、BaseEntity、安全拦截器、验证码工具、BCrypt密码工具、操作日志注解+切面、认证模块（5个Entity、5个Mapper、3个DTO、AuthService、AuthController）
- 数据库脚本：10张表 + 默认admin/admin123 + 6角色 + 菜单树 + 5部门
- 安全特性：验证码Redis存储、登录失败5次锁定30分钟、接口限流10次/分钟、XSS防护、BCrypt加密
- Maven 安装路径：`C:\ProgramData\chocolatey\lib\maven\apache-maven-3.9.14\bin\mvn.cmd`（通过 choco 安装）
- status 统一定义：1=正常 0=禁用（数据库+代码已统一）

## 系统管理模块 - 2026-04-07 完成

### 后端新增
- 新 Entity：SysDept、SysRoleMenu
- 新 Mapper：SysDeptMapper、SysRoleMenuMapper
- 新模块 `modules/system/`：5个 Controller（用户/角色/菜单/部门/操作日志）
- DTO：UserAddDTO、UserUpdateDTO、RoleDTO、MenuDTO、DeptDTO
- VO：SysUserVO、MenuTreeVO、DeptTreeVO
- 用户管理：CRUD + 重置密码 + 状态切换 + 角色分配
- 角色管理：CRUD + 菜单权限分配
- 菜单管理：CRUD + 树形结构
- 部门管理：CRUD + 树形结构
- 操作日志：分页查询 + 批量删除

### 前端新增
- API 模块：`src/api/system/`（user.js, role.js, menu.js, dept.js, operlog.js）
- 页面：`src/views/system/`（user, role, menu, dept, operlog）
- 路由已更新指向真实组件
- 前端 API baseURL 改为 `/api/v1`（走 Vite proxy）

### 前后端对接 - 2026-04-07
- 系统管理 5 个页面已从 mock 改为调用后端 API
- 认证模块（登录/验证码/用户信息）前后端接口已对齐
- 个人中心 /profile + 修改密码弹窗 + 头像 localStorage 持久化
- 业务模块（藏品、库房、入出库等）仍为 mock 模式，待后端业务接口开发后对接

## 数据备份模块 - 2026-04-09 完成

### 功能特性
- 数据库备份（使用 mysqldump）
- 备份文件列表查询
- 备份文件下载到本地
- 从备份文件恢复数据库
- 上传备份文件恢复
- 自动清理超过30天的旧备份

### 后端实现
- `BackupController.java`: REST API 控制器
- `BackupService.java`: 核心备份/恢复逻辑
- `BackupFileDTO.java`: 备份文件信息
- `RestoreDTO.java`: 恢复参数

### 前端实现
- `src/api/system/backup.js`: API 调用
- `src/views/system/backup/index.vue`: 备份管理页面
- 路由已添加到 router/index.js

### 权限配置
- system:backup:list - 查看备份列表
- system:backup:create - 创建备份
- system:backup:download - 下载备份
- system:backup:restore - 恢复备份
- system:backup:delete - 删除备份

### 部署注意
- 备份文件存储路径：`/data/backup`（可通过 BACKUP_PATH 环境变量配置）
- 需要确保 MySQL 服务器可访问（读取 mysqldump）
- 首次使用需执行 `db/backup_menu.sql` 添加菜单数据

### 2026-04-09 修复
- **分配权限问题**：数据库缺少角色管理的按钮权限（system:role:query等），已添加
- **备份URL解析**：parseDbUrl 方法数组越界，已重写支持多种URL格式
- **菜单动态加载**：MainLayout.vue 从静态菜单改为从后端动态获取菜单
  - 修改了 `src/stores/user.js`：添加 `fetchMenus()` 方法，登录时获取菜单
  - 修改了 `src/layout/MainLayout.vue`：菜单数据从 `userStore.menus` 获取
- **备份路径问题**：备份目录 `D:\聚景科技\2026\博物馆\备份` 应只包含 .sql 文件，不应有子目录
- **菜单排序调整**：系统管理 sort=99 排最后
- **菜单结构完善**：添加了缺失的菜单
  - 顶级菜单（11个）：藏品管理、RFID管理、统计分析、系统监控、库房管理、入出库管理、盘点管理、修复管理、外借管理、环境监测、系统管理
  - 系统管理子菜单（6个）：用户管理、角色管理、菜单管理、部门管理、操作日志、数据备份
- **路由缺失修复**：添加了 `collection/borrow` 和 `rfid/inventory` 路由
- **新增组件**：`views/collection/borrow/index.vue`（藏品借出）、`views/rfid/inventory/index.vue`（标签盘点）
