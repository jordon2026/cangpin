-- 库房表结构更新脚本
-- 执行时间：2026-04-11
-- 说明：为 storage 表添加面积和安全等级字段
-- MySQL 8.0 兼容版本

-- 检查并添加面积字段
SET @exist := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_NAME = 'storage' AND COLUMN_NAME = 'area' AND TABLE_SCHEMA = 'museum_db');
SET @sql := IF(@exist = 0, 
               'ALTER TABLE storage ADD COLUMN area DECIMAL(10,2) DEFAULT NULL COMMENT "面积(㎡)" AFTER location', 
               'SELECT "area column already exists"');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加安全等级字段
SET @exist2 := (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_NAME = 'storage' AND COLUMN_NAME = 'safety_level' AND TABLE_SCHEMA = 'museum_db');
SET @sql2 := IF(@exist2 = 0, 
                'ALTER TABLE storage ADD COLUMN safety_level INT DEFAULT 1 COMMENT "安全等级：1=一级 2=二级 3=三级" AFTER area', 
                'SELECT "safety_level column already exists"');
PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- 显示更新后的表结构
DESCRIBE storage;
