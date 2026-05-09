-- =============================================
-- 宠物门店私域运营 SaaS - 二维码相关表字段扩展
-- 版本: MySQL 8.0+
-- 创建时间: 2026-05-09
-- =============================================

USE pet_saas;

-- =============================================
-- 1. member_shop_bind 表新增字段
-- =============================================
ALTER TABLE member_shop_bind
    ADD COLUMN IF NOT EXISTS bind_source TINYINT DEFAULT 1 COMMENT '绑定来源：1-扫码注册，2-店员添加，3-推荐注册' AFTER is_deleted,
    ADD COLUMN IF NOT EXISTS referrer_member_id BIGINT COMMENT '推荐人会员ID' AFTER bind_source;

-- =============================================
-- 2. member 表新增字段
-- =============================================
ALTER TABLE member
    ADD COLUMN IF NOT EXISTS register_source TINYINT DEFAULT 1 COMMENT '注册来源：1-扫码注册，2-店员添加，3-推荐注册' AFTER openid,
    ADD COLUMN IF NOT EXISTS from_tenant_id BIGINT COMMENT '来源租户ID（扫码注册时记录）' AFTER register_source;