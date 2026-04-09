-- ============================================================
-- 数据备份功能菜单初始化
-- 执行方式: mysql -uroot -p museum_db < backup_menu.sql
-- 或在 Navicat/DBeaver 中执行
-- ============================================================

-- 插入备份管理菜单
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, permission, icon, sort, visible, status) VALUES
(15, 1, '数据备份', 2, '/system/backup', 'system/backup/index', 'system:backup:list', 'backup', 6, 1, 1);

-- 插入备份管理按钮
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, permission, icon, sort, visible, status) VALUES
(110, 15, '创建备份', 3, '', '', 'system:backup:create', '', 1, 1, 1),
(111, 15, '下载备份', 3, '', '', 'system:backup:download', '', 2, 1, 1),
(112, 15, '恢复备份', 3, '', '', 'system:backup:restore', '', 3, 1, 1),
(113, 15, '删除备份', 3, '', '', 'system:backup:delete', '', 4, 1, 1);

-- 给超级管理员分配权限（如果需要）
-- INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 15), (1, 110), (1, 111), (1, 112), (1, 113);

-- 验证插入结果
SELECT id, parent_id, name, type, permission FROM sys_menu WHERE id >= 15 AND id <= 20 OR id >= 110;
