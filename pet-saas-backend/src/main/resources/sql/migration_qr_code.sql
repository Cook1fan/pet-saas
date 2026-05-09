-- =============================================
-- 宠物门店私域运营 SaaS - 二维码相关表 DDL
-- 版本: MySQL 8.0+
-- 创建时间: 2026-05-09
-- =============================================

USE pet_saas;

-- =============================================
-- 1. 店铺二维码表
-- =============================================
CREATE TABLE IF NOT EXISTS shop_qr_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    qr_type TINYINT NOT NULL DEFAULT 1 COMMENT '二维码类型：1-店铺码，2-商品码，3-活动码',
    qr_name VARCHAR(100) COMMENT '二维码名称/描述',
    scene VARCHAR(128) COMMENT '场景参数（如 tenant_123）',
    qr_url VARCHAR(500) COMMENT '二维码图片URL',
    qr_ticket VARCHAR(256) COMMENT '微信返回的ticket',
    expire_time DATETIME COMMENT '过期时间（永久二维码则为空）',
    is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认二维码：0-否，1-是',
    scan_count INT NOT NULL DEFAULT 0 COMMENT '扫码次数',
    create_user BIGINT COMMENT '创建人ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_user BIGINT COMMENT '修改人ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_qr_type (qr_type),
    INDEX idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺二维码表';

-- =============================================
-- 2. 二维码扫码日志表
-- =============================================
CREATE TABLE IF NOT EXISTS qr_scan_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    qr_id BIGINT NOT NULL COMMENT '二维码ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    openid VARCHAR(100) COMMENT '扫码人微信openid（如已关注公众号）',
    unionid VARCHAR(100) COMMENT '微信unionid',
    member_id BIGINT COMMENT '会员ID（如已注册）',
    device_type VARCHAR(20) COMMENT '设备类型：ios/android/h5/miniapp',
    scan_result TINYINT COMMENT '扫码结果：1-新用户注册，2-老用户登录，3-游客访问，4-失败',
    fail_reason VARCHAR(200) COMMENT '失败原因',
    ip VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_qr_id (qr_id),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_member_id (member_id),
    INDEX idx_openid (openid),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二维码扫码日志表';