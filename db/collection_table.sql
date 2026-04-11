-- 藏品表
CREATE TABLE IF NOT EXISTS `collection` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `collection_no` VARCHAR(50) NOT NULL COMMENT '藏品编号',
  `name` VARCHAR(200) NOT NULL COMMENT '藏品名称',
  `category` VARCHAR(100) DEFAULT NULL COMMENT '藏品分类',
  `era` VARCHAR(100) DEFAULT NULL COMMENT '年代',
  `material` VARCHAR(100) DEFAULT NULL COMMENT '材质',
  `dimensions` VARCHAR(100) DEFAULT NULL COMMENT '尺寸',
  `weight` VARCHAR(50) DEFAULT NULL COMMENT '重量',
  `status` INT DEFAULT 1 COMMENT '收藏状态：1=正常 2=借出 3=修复中 4=展出',
  `storage_id` BIGINT DEFAULT NULL COMMENT '存放位置（库房ID）',
  `description` TEXT COMMENT '藏品描述',
  `image_url` VARCHAR(500) DEFAULT NULL COMMENT '藏品图片',
  `creator_id` BIGINT DEFAULT NULL COMMENT '登记人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `status2` INT DEFAULT 1 COMMENT '状态：1=正常 0=禁用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_collection_no` (`collection_no`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`),
  KEY `idx_storage_id` (`storage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='藏品表';

-- 库房表
CREATE TABLE IF NOT EXISTS `storage` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `storage_no` VARCHAR(50) NOT NULL COMMENT '库房编号',
  `name` VARCHAR(100) NOT NULL COMMENT '库房名称',
  `location` VARCHAR(200) DEFAULT NULL COMMENT '位置',
  `area` DECIMAL(10,2) DEFAULT NULL COMMENT '面积(㎡)',
  `safety_level` INT DEFAULT 1 COMMENT '安全等级：1=一级 2=二级 3=三级',
  `capacity` INT DEFAULT NULL COMMENT '容量',
  `current_count` INT DEFAULT 0 COMMENT '当前藏品数',
  `manager` VARCHAR(50) DEFAULT NULL COMMENT '负责人',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `temperature` DECIMAL(5,2) DEFAULT NULL COMMENT '温度(℃)',
  `humidity` DECIMAL(5,2) DEFAULT NULL COMMENT '湿度(%)',
  `status` INT DEFAULT 1 COMMENT '状态：1=正常 0=禁用',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_storage_no` (`storage_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库房表';

-- 藏品分类表
CREATE TABLE IF NOT EXISTS `collection_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父级ID',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `status` INT DEFAULT 1 COMMENT '状态：1=正常 0=禁用',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='藏品分类表';

-- 入库记录表
CREATE TABLE IF NOT EXISTS `inbound` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `inbound_no` VARCHAR(50) NOT NULL COMMENT '入库单号',
  `collection_id` BIGINT DEFAULT NULL COMMENT '藏品ID',
  `collection_name` VARCHAR(200) DEFAULT NULL COMMENT '藏品名称',
  `collection_no` VARCHAR(50) DEFAULT NULL COMMENT '藏品编号',
  `quantity` INT DEFAULT 1 COMMENT '数量',
  `source` VARCHAR(200) DEFAULT NULL COMMENT '来源',
  `storage_id` BIGINT DEFAULT NULL COMMENT '入库库房',
  `handler` VARCHAR(50) DEFAULT NULL COMMENT '经手人',
  `inbound_date` DATE DEFAULT NULL COMMENT '入库日期',
  `status` INT DEFAULT 0 COMMENT '状态：0=待审核 1=已入库 2=已拒绝',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `applicant_id` BIGINT DEFAULT NULL COMMENT '申请人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_inbound_no` (`inbound_no`),
  KEY `idx_status` (`status`),
  KEY `idx_storage_id` (`storage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库记录表';

-- 出库记录表
CREATE TABLE IF NOT EXISTS `outbound` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `outbound_no` VARCHAR(50) NOT NULL COMMENT '出库单号',
  `collection_id` BIGINT DEFAULT NULL COMMENT '藏品ID',
  `collection_name` VARCHAR(200) DEFAULT NULL COMMENT '藏品名称',
  `collection_no` VARCHAR(50) DEFAULT NULL COMMENT '藏品编号',
  `quantity` INT DEFAULT 1 COMMENT '数量',
  `destination` VARCHAR(200) DEFAULT NULL COMMENT '目的地',
  `reason` VARCHAR(200) DEFAULT NULL COMMENT '出库原因',
  `storage_id` BIGINT DEFAULT NULL COMMENT '出库库房',
  `handler` VARCHAR(50) DEFAULT NULL COMMENT '经手人',
  `outbound_date` DATE DEFAULT NULL COMMENT '出库日期',
  `expected_return_date` DATE DEFAULT NULL COMMENT '预计归还日期',
  `status` INT DEFAULT 0 COMMENT '状态：0=待审核 1=已出库 2=已拒绝 3=已归还',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `applicant_id` BIGINT DEFAULT NULL COMMENT '申请人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_outbound_no` (`outbound_no`),
  KEY `idx_status` (`status`),
  KEY `idx_collection_id` (`collection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库记录表';

-- RFID标签表
CREATE TABLE IF NOT EXISTS `rfid_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tag_no` VARCHAR(100) NOT NULL COMMENT '标签编号(EPC)',
  `collection_id` BIGINT DEFAULT NULL COMMENT '关联藏品ID',
  `collection_no` VARCHAR(50) DEFAULT NULL COMMENT '藏品编号',
  `status` INT DEFAULT 1 COMMENT '状态：1=未绑定 2=已绑定 3=已挂失',
  `bind_time` DATETIME DEFAULT NULL COMMENT '绑定时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_no` (`tag_no`),
  KEY `idx_collection_id` (`collection_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RFID标签表';

-- RFID设备表
CREATE TABLE IF NOT EXISTS `rfid_device` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_no` VARCHAR(100) NOT NULL COMMENT '设备编号',
  `device_name` VARCHAR(100) NOT NULL COMMENT '设备名称',
  `device_type` VARCHAR(50) DEFAULT NULL COMMENT '设备类型：reader=阅读器 antenna=天线',
  `location` VARCHAR(200) DEFAULT NULL COMMENT '安装位置',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `status` INT DEFAULT 1 COMMENT '状态：1=在线 0=离线',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_no` (`device_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RFID设备表';

-- 盘点任务表
CREATE TABLE IF NOT EXISTS `inventory_task` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_no` VARCHAR(50) NOT NULL COMMENT '任务编号',
  `task_name` VARCHAR(200) NOT NULL COMMENT '任务名称',
  `storage_id` BIGINT DEFAULT NULL COMMENT '盘点库房',
  `storage_name` VARCHAR(100) DEFAULT NULL COMMENT '库房名称',
  `plan_date` DATE DEFAULT NULL COMMENT '计划日期',
  `actual_date` DATE DEFAULT NULL COMMENT '实际日期',
  `total_count` INT DEFAULT 0 COMMENT '藏品总数',
  `checked_count` INT DEFAULT 0 COMMENT '已盘点数量',
  `missing_count` INT DEFAULT 0 COMMENT '缺失数量',
  `status` INT DEFAULT 0 COMMENT '状态：0=待盘点 1=盘点中 2=已完成',
  `result` VARCHAR(500) DEFAULT NULL COMMENT '盘点结果',
  `handler` VARCHAR(50) DEFAULT NULL COMMENT '负责人',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_no` (`task_no`),
  KEY `idx_status` (`status`),
  KEY `idx_storage_id` (`storage_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='盘点任务表';

-- 盘点记录表
CREATE TABLE IF NOT EXISTS `inventory_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` BIGINT NOT NULL COMMENT '盘点任务ID',
  `collection_id` BIGINT DEFAULT NULL COMMENT '藏品ID',
  `collection_no` VARCHAR(50) DEFAULT NULL COMMENT '藏品编号',
  `collection_name` VARCHAR(200) DEFAULT NULL COMMENT '藏品名称',
  `is_found` INT DEFAULT 1 COMMENT '是否找到：1=找到 0=缺失',
  `rfid_status` INT DEFAULT NULL COMMENT 'RFID状态：1=正常 2=损坏 3=缺失',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `check_time` DATETIME DEFAULT NULL COMMENT '盘点时间',
  `checker_id` BIGINT DEFAULT NULL COMMENT '盘点人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_collection_id` (`collection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='盘点记录表';

-- 修复工单表
CREATE TABLE IF NOT EXISTS `repair_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '工单编号',
  `collection_id` BIGINT DEFAULT NULL COMMENT '藏品ID',
  `collection_no` VARCHAR(50) DEFAULT NULL COMMENT '藏品编号',
  `collection_name` VARCHAR(200) DEFAULT NULL COMMENT '藏品名称',
  `damage_level` VARCHAR(50) DEFAULT NULL COMMENT '损坏等级：1=轻微 2=中等 3=严重',
  `damage_desc` TEXT COMMENT '损坏描述',
  `repair_type` VARCHAR(100) DEFAULT NULL COMMENT '修复类型',
  `repair_content` TEXT COMMENT '修复内容',
  `repair_cost` DECIMAL(10,2) DEFAULT NULL COMMENT '修复费用',
  `status` INT DEFAULT 0 COMMENT '状态：0=待受理 1=修复中 2=已完成 3=已验收',
  `accept_time` DATETIME DEFAULT NULL COMMENT '受理时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `accept_remark` VARCHAR(500) DEFAULT NULL COMMENT '受理备注',
  `applicant_id` BIGINT DEFAULT NULL COMMENT '申请人',
  `repairer` VARCHAR(50) DEFAULT NULL COMMENT '修复人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_status` (`status`),
  KEY `idx_collection_id` (`collection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='修复工单表';

-- 外借申请表
CREATE TABLE IF NOT EXISTS `loan_apply` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `apply_no` VARCHAR(50) NOT NULL COMMENT '申请编号',
  `collection_id` BIGINT DEFAULT NULL COMMENT '藏品ID',
  `collection_no` VARCHAR(50) DEFAULT NULL COMMENT '藏品编号',
  `collection_name` VARCHAR(200) DEFAULT NULL COMMENT '藏品名称',
  `borrower` VARCHAR(100) NOT NULL COMMENT '借展单位/个人',
  `borrower_contact` VARCHAR(100) DEFAULT NULL COMMENT '联系人',
  `borrower_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `loan_purpose` VARCHAR(200) DEFAULT NULL COMMENT '借展目的',
  `exhibition_name` VARCHAR(200) DEFAULT NULL COMMENT '展览名称',
  `exhibition_venue` VARCHAR(200) DEFAULT NULL COMMENT '展览地点',
  `start_date` DATE DEFAULT NULL COMMENT '借展开始日期',
  `end_date` DATE DEFAULT NULL COMMENT '借展结束日期',
  `insurance_value` DECIMAL(12,2) DEFAULT NULL COMMENT '保险金额',
  `insurance_company` VARCHAR(100) DEFAULT NULL COMMENT '保险公司',
  `status` INT DEFAULT 0 COMMENT '状态：0=待审批 1=审批通过 2=已拒绝 3=已借出 4=已归还',
  `approver` VARCHAR(50) DEFAULT NULL COMMENT '审批人',
  `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
  `approve_remark` VARCHAR(500) DEFAULT NULL COMMENT '审批备注',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `applicant_id` BIGINT DEFAULT NULL COMMENT '申请人',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_apply_no` (`apply_no`),
  KEY `idx_status` (`status`),
  KEY `idx_collection_id` (`collection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外借申请表';

-- 环境监测数据表
CREATE TABLE IF NOT EXISTS `environment_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `storage_id` BIGINT NOT NULL COMMENT '库房ID',
  `storage_name` VARCHAR(100) DEFAULT NULL COMMENT '库房名称',
  `temperature` DECIMAL(5,2) DEFAULT NULL COMMENT '温度(℃)',
  `humidity` DECIMAL(5,2) DEFAULT NULL COMMENT '湿度(%)',
  `light` DECIMAL(10,2) DEFAULT NULL COMMENT '光照度(lux)',
  `uv` DECIMAL(10,2) DEFAULT NULL COMMENT '紫外线指数',
  `co2` DECIMAL(10,2) DEFAULT NULL COMMENT 'CO2浓度(ppm)',
  `pm25` DECIMAL(10,2) DEFAULT NULL COMMENT 'PM2.5(μg/m³)',
  `formaldehyde` DECIMAL(10,2) DEFAULT NULL COMMENT '甲醛浓度(mg/m³)',
  `alert_level` INT DEFAULT 0 COMMENT '告警等级：0=正常 1=提醒 2=警告 3=严重',
  `record_time` DATETIME DEFAULT NULL COMMENT '记录时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_storage_id` (`storage_id`),
  KEY `idx_record_time` (`record_time`),
  KEY `idx_alert_level` (`alert_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='环境监测数据表';

-- 插入默认数据
INSERT INTO `collection_category` (`name`, `parent_id`, `sort`) VALUES
('陶瓷', 0, 1),
('青铜器', 0, 2),
('书画', 0, 3),
('玉器', 0, 4),
('金银器', 0, 5),
('杂项', 0, 6);

INSERT INTO `storage` (`storage_no`, `name`, `location`, `capacity`, `manager`, `phone`) VALUES
('KC001', '主库房A区', '博物馆地下一层A区', 500, '张保管', '13800138001'),
('KC002', '主库房B区', '博物馆地下一层B区', 500, '李保管', '13800138002'),
('KC003', '书画库房', '博物馆二层东侧', 200, '王保管', '13800138003'),
('KC004', '修复室', '博物馆地下一层修复区', 50, '赵修复', '13800138004');

-- 插入测试藏品
INSERT INTO `collection` (`collection_no`, `name`, `category`, `era`, `material`, `dimensions`, `status`, `description`) VALUES
('C20260001', '青花缠枝莲纹瓶', '陶瓷', '清代', '瓷器', '高45cm 口径12cm', 1, '清代青花瓷器，釉色莹润，纹饰精美'),
('C20260002', '青铜饕餮纹鼎', '青铜器', '商代', '青铜', '高62cm 口径48cm', 1, '商代青铜礼器，铸工精良，纹饰神秘'),
('C20260003', '王羲之兰亭序卷', '书画', '唐代', '绢本', '长245cm 宽45cm', 1, '唐代摹本，书法飘逸，为书法史上珍品'),
('C20260004', '白玉螭龙佩', '玉器', '清代', '和田玉', '直径6cm 厚0.8cm', 1, '清代宫廷玉器，玉质温润，雕工精美'),
('C20260005', '鎏金镂空银香囊', '金银器', '唐代', '银鎏金', '直径8cm', 2, '唐代宫廷器物，制作精巧，可随身携带');
