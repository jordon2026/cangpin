-- 修复菜单权限标识
-- 执行时间: 2026-04-13

-- 修复用户管理的权限标识
UPDATE sys_menu SET permission = 'system:user:list' WHERE id = 16;

-- 修复角色管理的权限标识
UPDATE sys_menu SET permission = 'system:role:list' WHERE id = 17;

-- 为系统管理子菜单添加按钮权限（如果不存在）
-- 用户管理按钮权限
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, permission, icon, sort, visible, status, create_time, deleted) VALUES
(200, 16, '用户新增', 3, '', '', 'system:user:add', '', 1, 1, 1, NOW(), 0),
(201, 16, '用户修改', 3, '', '', 'system:user:edit', '', 2, 1, 1, NOW(), 0),
(202, 16, '用户删除', 3, '', '', 'system:user:delete', '', 3, 1, 1, NOW(), 0),
(203, 16, '重置密码', 3, '', '', 'system:user:reset', '', 4, 1, 1, NOW(), 0),
(204, 16, '分配角色', 3, '', '', 'system:user:role', '', 5, 1, 1, NOW(), 0);

-- 角色管理按钮权限
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, permission, icon, sort, visible, status, create_time, deleted) VALUES
(210, 17, '角色新增', 3, '', '', 'system:role:add', '', 1, 1, 1, NOW(), 0),
(211, 17, '角色修改', 3, '', '', 'system:role:edit', '', 2, 1, 1, NOW(), 0),
(212, 17, '角色删除', 3, '', '', 'system:role:delete', '', 3, 1, 1, NOW(), 0),
(213, 17, '分配权限', 3, '', '', 'system:role:perm', '', 4, 1, 1, NOW(), 0);

-- 菜单管理按钮权限
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, permission, icon, sort, visible, status, create_time, deleted) VALUES
(220, 12, '菜单新增', 3, '', '', 'system:menu:add', '', 1, 1, 1, NOW(), 0),
(221, 12, '菜单修改', 3, '', '', 'system:menu:edit', '', 2, 1, 1, NOW(), 0),
(222, 12, '菜单删除', 3, '', '', 'system:menu:delete', '', 3, 1, 1, NOW(), 0);

-- 部门管理按钮权限
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, permission, icon, sort, visible, status, create_time, deleted) VALUES
(230, 13, '部门新增', 3, '', '', 'system:dept:add', '', 1, 1, 1, NOW(), 0),
(231, 13, '部门修改', 3, '', '', 'system:dept:edit', '', 2, 1, 1, NOW(), 0),
(232, 13, '部门删除', 3, '', '', 'system:dept:delete', '', 3, 1, 1, NOW(), 0);

-- 操作日志按钮权限
INSERT IGNORE INTO sys_menu (id, parent_id, name, type, path, component, permission, icon, sort, visible, status, create_time, deleted) VALUES
(240, 14, '日志删除', 3, '', '', 'system:operlog:delete', '', 1, 1, 1, NOW(), 0);

-- 为超级管理员角色分配所有权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id >= 200 AND id <= 240;
