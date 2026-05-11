-- =============================================
-- 储蓄卡次模块 - 完整数据库表创建脚本
-- 创建时间: 2026-05-11
-- 说明: 此脚本包含所有储蓄卡次相关的表
-- =============================================

USE pet_saas;

-- 1. 储值购买记录表
CREATE TABLE IF NOT EXISTS recharge_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    member_id BIGINT NOT NULL COMMENT '会员 ID',
    recharge_rule_id BIGINT NOT NULL COMMENT '储值规则 ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    recharge_amount DECIMAL(10,2) NOT NULL COMMENT '储值金额',
    give_amount DECIMAL(10,2) NOT NULL COMMENT '赠送金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    pay_type TINYINT NOT NULL COMMENT '支付方式：1-微信，2-现金',
    pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-已支付，2-已退款',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    transaction_id VARCHAR(64) DEFAULT NULL COMMENT '微信支付单号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT DEFAULT NULL COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT DEFAULT NULL COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_tenant_member (tenant_id, member_id),
    INDEX idx_pay_status (pay_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='储值购买记录表';

-- 2. 次卡购买记录表
CREATE TABLE IF NOT EXISTS card_order (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    member_id BIGINT NOT NULL COMMENT '会员 ID',
    card_rule_id BIGINT NOT NULL COMMENT '次卡规则 ID',
    member_card_id BIGINT DEFAULT NULL COMMENT '会员次卡 ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    card_name VARCHAR(50) NOT NULL COMMENT '次卡名称',
    times INT NOT NULL COMMENT '次数',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    valid_days INT NOT NULL COMMENT '有效期天数',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    pay_type TINYINT NOT NULL COMMENT '支付方式：1-微信，2-现金',
    pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-已支付，2-已退款',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    transaction_id VARCHAR(64) DEFAULT NULL COMMENT '微信支付单号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT DEFAULT NULL COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT DEFAULT NULL COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_tenant_member (tenant_id, member_id),
    INDEX idx_pay_status (pay_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='次卡购买记录表';

-- 3. 储值消费记录表
CREATE TABLE IF NOT EXISTS recharge_consume_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    member_id BIGINT NOT NULL COMMENT '会员 ID',
    member_account_id BIGINT NOT NULL COMMENT '会员储值账户 ID',
    order_id BIGINT DEFAULT NULL COMMENT '关联订单 ID',
    type TINYINT NOT NULL COMMENT '类型：1-储值，2-消费，3-退款',
    amount DECIMAL(10,2) NOT NULL COMMENT '变动金额（正为增加，负为减少）',
    balance_before DECIMAL(10,2) NOT NULL COMMENT '变动前余额',
    balance_after DECIMAL(10,2) NOT NULL COMMENT '变动后余额',
    remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT DEFAULT NULL COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT DEFAULT NULL COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    INDEX idx_tenant_member (tenant_id, member_id),
    INDEX idx_member_account (member_account_id),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='储值消费记录表';

-- 4. 次卡核销记录表
CREATE TABLE IF NOT EXISTS card_verify_record (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    member_id BIGINT NOT NULL COMMENT '会员 ID',
    member_card_id BIGINT NOT NULL COMMENT '会员次卡 ID',
    card_rule_id BIGINT NOT NULL COMMENT '次卡规则 ID',
    order_id BIGINT DEFAULT NULL COMMENT '关联订单 ID',
    verify_code_record_id BIGINT DEFAULT NULL COMMENT '核销码记录 ID',
    card_name VARCHAR(50) NOT NULL COMMENT '次卡名称',
    verify_times INT NOT NULL DEFAULT 1 COMMENT '核销次数',
    remain_times_before INT NOT NULL COMMENT '核销前剩余次数',
    remain_times_after INT NOT NULL COMMENT '核销后剩余次数',
    remark VARCHAR(200) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT DEFAULT NULL COMMENT '创建人 ID（核销人）',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT DEFAULT NULL COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (id),
    INDEX idx_tenant_member (tenant_id, member_id),
    INDEX idx_member_card (member_card_id),
    INDEX idx_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='次卡核销记录表';

-- =============================================
-- 执行完成提示
-- =============================================
SELECT '储蓄卡次模块表创建完成！' AS message;
