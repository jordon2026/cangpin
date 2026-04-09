-- ============================================================
-- Museum Management System - Database Initialization Script
-- ============================================================

CREATE DATABASE IF NOT EXISTS `museum_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `museum_db`;

-- ----------------------------
-- 1. 系统用户表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(64)  NOT NULL                COMMENT '用户名',
    `password`    VARCHAR(128) NOT NULL                COMMENT '密码（BCrypt加密）',
    `real_name`   VARCHAR(64)  DEFAULT ''              COMMENT '真实姓名',
    `phone`       VARCHAR(20)  DEFAULT ''              COMMENT '手机号',
    `email`       VARCHAR(128) DEFAULT ''              COMMENT '邮箱',
    `avatar`      VARCHAR(512) DEFAULT ''              COMMENT '头像URL',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-正常 0-禁用',
    `dept_id`     BIGINT       DEFAULT NULL             COMMENT '所属部门ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户表';

-- ----------------------------
-- 2. 系统角色表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `role_name`   VARCHAR(64)  NOT NULL                COMMENT '角色名称',
    `role_code`   VARCHAR(64)  NOT NULL                COMMENT '角色编码',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-正常 0-禁用',
    `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统角色表';

-- ----------------------------
-- 3. 系统菜单表
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0      COMMENT '父菜单ID',
    `name`        VARCHAR(64)  NOT NULL                COMMENT '菜单名称',
    `type`        TINYINT      NOT NULL DEFAULT 1      COMMENT '类型：1-目录 2-菜单 3-按钮',
    `path`        VARCHAR(256) DEFAULT ''              COMMENT '路由路径',
    `component`   VARCHAR(256) DEFAULT ''              COMMENT '组件路径',
    `permission`  VARCHAR(128) DEFAULT ''              COMMENT '权限标识',
    `icon`        VARCHAR(64)  DEFAULT ''              COMMENT '菜单图标',
    `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
    `visible`     TINYINT      NOT NULL DEFAULT 1      COMMENT '是否可见：1-可见 0-隐藏',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-正常 0-禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统菜单表';

-- ----------------------------
-- 4. 用户角色关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`       BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`  BIGINT NOT NULL                COMMENT '用户ID',
    `role_id`  BIGINT NOT NULL                COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ----------------------------
-- 5. 角色菜单关联表
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
    `id`       BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id`  BIGINT NOT NULL                COMMENT '角色ID',
    `menu_id`  BIGINT NOT NULL                COMMENT '菜单ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- ----------------------------
-- 6. 部门表
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '部门ID',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0      COMMENT '父部门ID',
    `dept_name`   VARCHAR(128) NOT NULL                COMMENT '部门名称',
    `leader`      VARCHAR(64)  DEFAULT ''              COMMENT '负责人',
    `phone`       VARCHAR(20)  DEFAULT ''              COMMENT '联系电话',
    `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-正常 0-禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- ----------------------------
-- 7. 操作日志表
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `title`          VARCHAR(128)  DEFAULT ''              COMMENT '操作标题',
    `module`         VARCHAR(64)   DEFAULT ''              COMMENT '操作模块',
    `type`           TINYINT       DEFAULT 0               COMMENT '操作类型：0-其他 1-新增 2-修改 3-删除',
    `oper_user`      BIGINT        DEFAULT NULL             COMMENT '操作人ID',
    `oper_url`       VARCHAR(512)  DEFAULT ''              COMMENT '请求URL',
    `method`         VARCHAR(256)  DEFAULT ''              COMMENT 'Java方法名',
    `request_method` VARCHAR(10)   DEFAULT ''              COMMENT 'HTTP请求方法',
    `request_params` TEXT          DEFAULT NULL             COMMENT '请求参数',
    `response_result` TEXT         DEFAULT NULL             COMMENT '响应结果',
    `ip`             VARCHAR(64)   DEFAULT ''              COMMENT 'IP地址',
    `status`         TINYINT       DEFAULT 1               COMMENT '状态：1-正常 0-异常',
    `error_msg`      VARCHAR(2048) DEFAULT ''              COMMENT '错误消息',
    `oper_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_oper_user` (`oper_user`),
    KEY `idx_oper_time` (`oper_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ----------------------------
-- 8. 藏品分类表
-- ----------------------------
DROP TABLE IF EXISTS `collection_category`;
CREATE TABLE `collection_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0      COMMENT '父分类ID',
    `name`        VARCHAR(128) NOT NULL                COMMENT '分类名称',
    `code`        VARCHAR(64)  DEFAULT ''              COMMENT '分类编码',
    `sort`        INT          NOT NULL DEFAULT 0      COMMENT '排序',
    `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态：1-正常 0-禁用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='藏品分类表';

-- ----------------------------
-- 9. 藏品信息表
-- ----------------------------
DROP TABLE IF EXISTS `collection_info`;
CREATE TABLE `collection_info` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '藏品ID',
    `category_id`  BIGINT        DEFAULT NULL             COMMENT '分类ID',
    `name`         VARCHAR(256)  NOT NULL                COMMENT '藏品名称',
    `code`         VARCHAR(64)   NOT NULL                COMMENT '藏品编号',
    `era`          VARCHAR(128)  DEFAULT ''              COMMENT '年代',
    `material`     VARCHAR(256)  DEFAULT ''              COMMENT '材质',
    `dimensions`   VARCHAR(256)  DEFAULT ''              COMMENT '尺寸',
    `weight`       VARCHAR(64)   DEFAULT ''              COMMENT '重量',
    `description`  TEXT          DEFAULT NULL             COMMENT '描述',
    `image_url`    VARCHAR(512)  DEFAULT ''              COMMENT '图片URL',
    `status`       TINYINT       NOT NULL DEFAULT 1      COMMENT '状态：1-在库 2-展出 3-修复中 4-借出',
    `location`     VARCHAR(256)  DEFAULT ''              COMMENT '存放位置',
    `creator_id`   BIGINT        DEFAULT NULL             COMMENT '录入人ID',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT       NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='藏品信息表';

-- ----------------------------
-- 10. RFID标签表
-- ----------------------------
DROP TABLE IF EXISTS `rfid_tag`;
CREATE TABLE `rfid_tag` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `tag_code`     VARCHAR(64)  NOT NULL                COMMENT 'RFID标签编码',
    `collection_id` BIGINT      DEFAULT NULL             COMMENT '关联藏品ID',
    `status`       TINYINT      NOT NULL DEFAULT 0      COMMENT '状态：0-空闲 1-已绑定 2-已解绑',
    `bind_time`    DATETIME     DEFAULT NULL             COMMENT '绑定时间',
    `unbind_time`  DATETIME     DEFAULT NULL             COMMENT '解绑时间',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted`      TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tag_code` (`tag_code`),
    KEY `idx_collection_id` (`collection_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RFID标签表';


-- ============================================================
-- Initial Data
-- ============================================================

-- 默认管理员密码: admin123 (BCrypt加密)
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `email`, `status`, `dept_id`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '超级管理员', '13800000001', 'admin@museum.com', 1, 1);

-- 默认角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `status`, `sort`) VALUES
(1, '超级管理员', 'super_admin', 1, 1),
(2, '馆长',      'curator',     1, 2),
(3, '策展员',    'exhibitor',   1, 3),
(4, '库管员',    'storekeeper', 1, 4),
(5, '修复师',    'restorer',    1, 5),
(6, '只读人员',  'viewer',      1, 6);

-- 管理员关联超级管理员角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 默认部门
INSERT INTO `sys_dept` (`id`, `parent_id`, `dept_name`, `leader`, `sort`, `status`) VALUES
(1, 0, '博物馆总馆',  '管理员', 1, 1),
(2, 1, '藏品管理部',  '库管员', 1, 1),
(3, 1, '展览策划部',  '策展员', 2, 1),
(4, 1, '文物保护部',  '修复师', 3, 1),
(5, 1, '信息技术部',  '技术员', 4, 1);

-- 基础菜单结构
INSERT INTO `sys_menu` (`id`, `parent_id`, `name`, `type`, `path`, `component`, `permission`, `icon`, `sort`, `visible`, `status`) VALUES
-- 一级目录
(1,  0, '系统管理', 1, '/system',    '',                       '',             'setting',    1, 1, 1),
(2,  0, '藏品管理', 1, '/collection','',                       '',             'box',        2, 1, 1),
(3,  0, 'RFID管理', 1, '/rfid',      '',                       '',             'cpu',        3, 1, 1),
(4,  0, '统计分析', 1, '/statistics','',                       '',             'chart',      4, 1, 1),
(5,  0, '系统监控', 1, '/monitor',   '',                       '',             'monitor',    5, 1, 1),

-- 系统管理子菜单
(10, 1, '用户管理', 2, '/system/user',     'system/user/index',     'system:user:list',     'user',       1, 1, 1),
(11, 1, '角色管理', 2, '/system/role',     'system/role/index',     'system:role:list',     'peoples',    2, 1, 1),
(12, 1, '菜单管理', 2, '/system/menu',     'system/menu/index',     'system:menu:list',     'tree-table', 3, 1, 1),
(13, 1, '部门管理', 2, '/system/dept',     'system/dept/index',     'system:dept:list',     'tree',       4, 1, 1),
(14, 1, '操作日志', 2, '/system/log',      'system/log/index',      'system:log:list',      'log',        5, 1, 1),

-- 藏品管理子菜单
(20, 2, '藏品分类', 2, '/collection/category', 'collection/category/index', 'collection:category:list', 'nested',   1, 1, 1),
(21, 2, '藏品信息', 2, '/collection/info',     'collection/info/index',     'collection:info:list',     'documentation', 2, 1, 1),
(22, 2, '藏品借出', 2, '/collection/borrow',   'collection/borrow/index',   'collection:borrow:list',   'shopping',  3, 1, 1),

-- RFID管理子菜单
(30, 3, '标签管理', 2, '/rfid/tag',      'rfid/tag/index',      'rfid:tag:list',      'eye-open',  1, 1, 1),
(31, 3, '标签盘点', 2, '/rfid/inventory', 'rfid/inventory/index', 'rfid:inventory:list', 'clipboard',  2, 1, 1),

-- 用户管理按钮
(100, 10, '用户新增', 3, '', '', 'system:user:add',    '', 1, 1, 1),
(101, 10, '用户修改', 3, '', '', 'system:user:edit',   '', 2, 1, 1),
(102, 10, '用户删除', 3, '', '', 'system:user:delete', '', 3, 1, 1),
(103, 10, '重置密码', 3, '', '', 'system:user:reset',  '', 4, 1, 1),

-- 数据备份菜单（位于系统管理下）
(15, 1, '数据备份', 2, '/system/backup', 'system/backup/index', 'system:backup:list', 'backup', 6, 1, 1),

-- 数据备份按钮
(110, 15, '创建备份', 3, '', '', 'system:backup:create', '', 1, 1, 1),
(111, 15, '下载备份', 3, '', '', 'system:backup:download', '', 2, 1, 1),
(112, 15, '恢复备份', 3, '', '', 'system:backup:restore', '', 3, 1, 1),
(113, 15, '删除备份', 3, '', '', 'system:backup:delete', '', 4, 1, 1);

-- 超级管理员拥有所有菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `id` FROM `sys_menu`;
