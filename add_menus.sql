-- 添加缺失的顶级菜单
INSERT INTO sys_menu (id, parent_id, name, path, icon, type, sort, visible, status, deleted, create_time, update_time) VALUES
(6, 0, '库房管理', '/storage', 'House', 1, 6, 1, 1, 0, NOW(), NOW()),
(7, 0, '入出库管理', '/inout', 'Sort', 1, 7, 1, 1, 0, NOW(), NOW()),
(8, 0, '盘点管理', '/inventory', 'Tickets', 1, 8, 1, 1, 0, NOW(), NOW()),
(9, 0, '修复管理', '/repair', 'SetUp', 1, 9, 1, 1, 0, NOW(), NOW()),
(10, 0, '外借管理', '/loan', 'Promotion', 1, 10, 1, 1, 0, NOW(), NOW()),
(11, 0, '环境监测', '/environment', 'Sunny', 1, 11, 1, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name), path=VALUES(path), icon=VALUES(icon), sort=VALUES(sort);

-- 添加库房管理子菜单
INSERT INTO sys_menu (parent_id, name, path, icon, type, sort, visible, status, deleted, create_time, update_time) VALUES
(6, '库房信息', '/storage/info', 'House', 2, 1, 1, 1, 0, NOW(), NOW()),
(6, '库位管理', '/storage/location', 'Grid', 2, 2, 1, 1, 0, NOW(), NOW());

-- 添加入出库管理子菜单
INSERT INTO sys_menu (parent_id, name, path, icon, type, sort, visible, status, deleted, create_time, update_time) VALUES
(7, '入库登记', '/inout/inbound', 'Bottom', 2, 1, 1, 1, 0, NOW(), NOW()),
(7, '出库申请', '/inout/outbound', 'Top', 2, 2, 1, 1, 0, NOW(), NOW()),
(7, '出库审批', '/inout/approve', 'Select', 2, 3, 1, 1, 0, NOW(), NOW());

-- 添加盘点管理子菜单
INSERT INTO sys_menu (parent_id, name, path, icon, type, sort, visible, status, deleted, create_time, update_time) VALUES
(8, '盘点任务', '/inventory/task', 'List', 2, 1, 1, 1, 0, NOW(), NOW()),
(8, '盘点记录', '/inventory/record', 'Finished', 2, 2, 1, 1, 0, NOW(), NOW());

-- 添加修复管理子菜单
INSERT INTO sys_menu (parent_id, name, path, icon, type, sort, visible, status, deleted, create_time, update_time) VALUES
(9, '修复工单', '/repair/order', 'Tickets', 2, 1, 1, 1, 0, NOW(), NOW());

-- 添加外借管理子菜单
INSERT INTO sys_menu (parent_id, name, path, icon, type, sort, visible, status, deleted, create_time, update_time) VALUES
(10, '外借申请', '/loan/apply', 'DocumentAdd', 2, 1, 1, 1, 0, NOW(), NOW());

-- 添加环境监测子菜单
INSERT INTO sys_menu (parent_id, name, path, icon, type, sort, visible, status, deleted, create_time, update_time) VALUES
(11, '环境数据', '/environment/data', 'DataLine', 2, 1, 1, 1, 0, NOW(), NOW());
