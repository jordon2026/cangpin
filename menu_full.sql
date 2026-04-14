-- 完整的菜单数据（43条）
TRUNCATE TABLE sys_menu;
TRUNCATE TABLE sys_role_menu;

INSERT INTO sys_menu (id, parent_id, name, type, path, component, permission, icon, sort, visible, status, deleted) VALUES
-- 顶级菜单
(1, 0, '系统管理', 1, '/system', 'Layout', NULL, 'Setting', 99, 1, 1, 0),
(2, 0, '藏品管理', 1, '/collection', 'Layout', NULL, 'Collection', 1, 1, 1, 0),
(3, 0, 'RFID管理', 1, '/rfid', 'Layout', NULL, 'Rfid', 2, 1, 1, 0),
(4, 0, '统计分析', 1, '/statistics', 'Layout', NULL, 'Statistics', 3, 1, 1, 0),
(5, 0, '系统监控', 1, '/monitor', 'Layout', NULL, 'Monitor', 4, 1, 1, 0),
(6, 0, '库房管理', 1, '/storage', 'Layout', NULL, 'Storage', 5, 1, 1, 0),
(7, 0, '入出库管理', 1, '/inout', 'Layout', NULL, 'InOut', 6, 1, 1, 0),
(8, 0, '盘点管理', 1, '/inventory', 'Layout', NULL, 'Inventory', 7, 1, 1, 0),
(9, 0, '修复管理', 1, '/repair', 'Layout', NULL, 'Repair', 8, 1, 1, 0),
(10, 0, '外借管理', 1, '/loan', 'Layout', NULL, 'Loan', 9, 1, 1, 0),
(11, 0, '环境监测', 1, '/environment', 'Layout', NULL, 'Environment', 10, 1, 1, 0),

-- 系统管理子菜单
(12, 1, '用户管理', 2, '/system/user', 'system/user/index', 'system:user:list', 'User', 1, 1, 1, 0),
(13, 1, '角色管理', 2, '/system/role', 'system/role/index', 'system:role:list', 'Role', 2, 1, 1, 0),
(14, 1, '菜单管理', 2, '/system/menu', 'system/menu/index', 'system:menu:list', 'Menu', 3, 1, 1, 0),
(15, 1, '部门管理', 2, '/system/dept', 'system/dept/index', 'system:dept:list', 'Dept', 4, 1, 1, 0),
(16, 1, '操作日志', 2, '/system/log', 'system/log/index', 'system:log:list', 'Log', 5, 1, 1, 0),
(17, 1, '数据备份', 2, '/system/backup', 'system/backup/index', 'system:backup:list', 'Backup', 6, 1, 1, 0),

-- 藏品管理子菜单
(18, 2, '藏品分类', 2, '/collection/category', 'collection/category/index', 'collection:category:list', 'Category', 1, 1, 1, 0),
(19, 2, '藏品信息', 2, '/collection/info', 'collection/info/index', 'collection:info:list', 'Info', 2, 1, 1, 0),
(20, 2, '藏品借出', 2, '/collection/borrow', 'collection/borrow/index', 'collection:borrow:list', 'Borrow', 3, 1, 1, 0),

-- RFID管理子菜单
(21, 3, '标签管理', 2, '/rfid/tag', 'rfid/tag/index', 'rfid:tag:list', 'Tag', 1, 1, 1, 0),
(22, 3, '标签盘点', 2, '/rfid/inventory', 'rfid/inventory/index', 'rfid:inventory:list', 'Inventory', 2, 1, 1, 0),
(23, 3, '设备管理', 2, '/rfid/device', 'rfid/device/index', 'rfid:device:list', 'Device', 3, 1, 1, 0),

-- 库房管理子菜单
(24, 6, '库房信息', 2, '/storage/info', 'storage/info/index', 'storage:info:list', 'StorageInfo', 1, 1, 1, 0),
(25, 6, '库位管理', 2, '/storage/location', 'storage/location/index', 'storage:location:list', 'Location', 2, 1, 1, 0),

-- 入出库管理子菜单
(26, 7, '入库登记', 2, '/inout/inbound', 'inout/inbound/index', 'inout:inbound:list', 'Inbound', 1, 1, 1, 0),
(27, 7, '出库申请', 2, '/inout/outbound', 'inout/outbound/index', 'inout:outbound:list', 'Outbound', 2, 1, 1, 0),
(28, 7, '出库审批', 2, '/inout/approve', 'inout/approve/index', 'inout:approve:list', 'Approve', 3, 1, 1, 0),

-- 盘点管理子菜单
(29, 8, '盘点任务', 2, '/inventory/task', 'inventory/task/index', 'inventory:task:list', 'Task', 1, 1, 1, 0),
(30, 8, '盘点记录', 2, '/inventory/record', 'inventory/record/index', 'inventory:record:list', 'Record', 2, 1, 1, 0),

-- 修复管理子菜单
(31, 9, '修复工单', 2, '/repair/order', 'repair/order/index', 'repair:order:list', 'Order', 1, 1, 1, 0),

-- 外借管理子菜单
(32, 10, '外借申请', 2, '/loan/apply', 'loan/apply/index', 'loan:apply:list', 'Apply', 1, 1, 1, 0),

-- 环境监测子菜单
(33, 11, '环境数据', 2, '/environment/data', 'environment/data/index', 'environment:data:list', 'Data', 1, 1, 1, 0),

-- 按钮权限（用户管理）
(34, 12, '用户新增', 3, NULL, NULL, 'system:user:add', NULL, 1, 0, 1, 0),
(35, 12, '用户修改', 3, NULL, NULL, 'system:user:edit', NULL, 2, 0, 1, 0),
(36, 12, '用户删除', 3, NULL, NULL, 'system:user:delete', NULL, 3, 0, 1, 0),
(37, 12, '重置密码', 3, NULL, NULL, 'system:user:resetPwd', NULL, 4, 0, 1, 0),

-- 按钮权限（角色管理）
(38, 13, '角色新增', 3, NULL, NULL, 'system:role:add', NULL, 1, 0, 1, 0),
(39, 13, '角色修改', 3, NULL, NULL, 'system:role:edit', NULL, 2, 0, 1, 0),
(40, 13, '角色删除', 3, NULL, NULL, 'system:role:delete', NULL, 3, 0, 1, 0),

-- 按钮权限（数据备份）
(41, 17, '创建备份', 3, NULL, NULL, 'system:backup:create', NULL, 1, 0, 1, 0),
(42, 17, '下载备份', 3, NULL, NULL, 'system:backup:download', NULL, 2, 0, 1, 0),
(43, 17, '恢复备份', 3, NULL, NULL, 'system:backup:restore', NULL, 3, 0, 1, 0),
(44, 17, '删除备份', 3, NULL, NULL, 'system:backup:delete', NULL, 4, 0, 1, 0);

-- 给管理员角色分配所有菜单权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11),
(1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17),
(1, 18), (1, 19), (1, 20),
(1, 21), (1, 22), (1, 23),
(1, 24), (1, 25),
(1, 26), (1, 27), (1, 28),
(1, 29), (1, 30),
(1, 31),
(1, 32),
(1, 33),
(1, 34), (1, 35), (1, 36), (1, 37),
(1, 38), (1, 39), (1, 40),
(1, 41), (1, 42), (1, 43), (1, 44);
