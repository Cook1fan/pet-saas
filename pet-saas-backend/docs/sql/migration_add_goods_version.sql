-- =============================================
-- 商品表新增乐观锁版本号字段
-- 迁移版本: V1.0
-- 创建时间: 2026-03-26
-- =============================================

USE pet_saas;

-- 新增 version 字段
ALTER TABLE goods
ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号'
AFTER status;

-- 初始化现有数据的版本号
UPDATE goods SET version = 0 WHERE version IS NULL;
