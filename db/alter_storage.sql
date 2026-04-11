-- 库房表结构更新脚本
-- 执行时间：2026-04-11
-- 说明：为 storage 表添加面积和安全等级字段

-- 添加面积字段
ALTER TABLE `storage` 
ADD COLUMN IF NOT EXISTS `area` DECIMAL(10,2) DEFAULT NULL COMMENT '面积(㎡)' AFTER `location`;

-- 添加安全等级字段
ALTER TABLE `storage` 
ADD COLUMN IF NOT EXISTS `safety_level` INT DEFAULT 1 COMMENT '安全等级：1=一级 2=二级 3=三级' AFTER `area`;

-- 更新注释
ALTER TABLE `storage` 
MODIFY COLUMN `manager` VARCHAR(50) DEFAULT NULL COMMENT '负责人';

-- 查看更新后的表结构
DESCRIBE `storage`;
