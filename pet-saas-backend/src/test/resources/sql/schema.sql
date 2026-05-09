-- =============================================
-- 测试数据库表结构 - H2 数据库
-- =============================================

-- 全局表（无 tenant_id）
CREATE TABLE IF NOT EXISTS sys_platform_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_tenant (
    tenant_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    shop_name VARCHAR(100) NOT NULL,
    admin_phone VARCHAR(20) NOT NULL,
    address VARCHAR(200),
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_ai_package (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    times INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(50) NOT NULL UNIQUE,
    config_value VARCHAR(500),
    description VARCHAR(200),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS goods_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    parent_id BIGINT NOT NULL,
    category_name VARCHAR(50) NOT NULL,
    sort INT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

-- 业务表（带 tenant_id）
CREATE TABLE IF NOT EXISTS shop_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_tenant_username (tenant_id, username)
);

CREATE TABLE IF NOT EXISTS shop_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL UNIQUE,
    shop_name VARCHAR(100) NOT NULL,
    address VARCHAR(200),
    phone VARCHAR(20),
    business_hours VARCHAR(100),
    logo VARCHAR(500),
    pay_config TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    phone VARCHAR(20) NOT NULL,
    name VARCHAR(50),
    tags VARCHAR(500),
    openid VARCHAR(100),
    register_source TINYINT DEFAULT 1,
    from_tenant_id BIGINT,
    avatar VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_tenant_phone (tenant_id, phone)
);

CREATE TABLE IF NOT EXISTS pet_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    breed VARCHAR(50),
    birthday DATE,
    gender TINYINT,
    vaccine_time DATE,
    deworm_time DATE,
    wash_time DATE,
    next_remind_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS recharge_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    recharge_amount DECIMAL(10,2) NOT NULL,
    give_amount DECIMAL(10,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS card_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    times INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    valid_days INT NOT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS member_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_recharge DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_tenant_member (tenant_id, member_id)
);

CREATE TABLE IF NOT EXISTS member_card (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    card_rule_id BIGINT NOT NULL,
    remain_times INT NOT NULL,
    expire_time TIMESTAMP NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    goods_name VARCHAR(100) NOT NULL,
    main_image VARCHAR(255),
    is_service TINYINT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS goods_sku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    sku_code VARCHAR(32) NOT NULL,
    spec_name VARCHAR(50) NOT NULL,
    spec_value VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    cost_price DECIMAL(10,2),
    stock INT NOT NULL DEFAULT 0,
    warn_stock INT,
    barcode VARCHAR(50),
    status TINYINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_tenant_sku_code (tenant_id, sku_code),
    INDEX idx_barcode (tenant_id, barcode)
);

CREATE TABLE IF NOT EXISTS stock_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    type TINYINT NOT NULL,
    num INT NOT NULL,
    remark VARCHAR(200),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT NOT NULL,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS order_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    member_id BIGINT,
    total_amount DECIMAL(10,2) NOT NULL,
    pay_amount DECIMAL(10,2) NOT NULL,
    pay_type TINYINT NOT NULL,
    pay_status TINYINT NOT NULL DEFAULT 0,
    pay_time TIMESTAMP,
    transaction_id VARCHAR(64),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    sku_id BIGINT,
    goods_name VARCHAR(100) NOT NULL,
    num INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS activity_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    type TINYINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    goods_id BIGINT NOT NULL,
    sku_id BIGINT COMMENT 'SKU ID',
    price DECIMAL(10,2) NOT NULL,
    origin_price DECIMAL(10,2) NOT NULL,
    group_count INT,
    group_valid_hours INT NOT NULL DEFAULT 24 COMMENT '拼团有效期（小时）',
    stock INT NOT NULL,
    limit_num INT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    qr_code_url VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS activity_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    group_id BIGINT,
    status TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_group_id (group_id)
);

CREATE TABLE IF NOT EXISTS activity_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    tenant_id BIGINT NOT NULL COMMENT '租户ID',
    activity_id BIGINT NOT NULL COMMENT '活动ID',
    group_no VARCHAR(32) NOT NULL COMMENT '拼团组号（唯一）',
    leader_member_id BIGINT NOT NULL COMMENT '团长会员ID',
    current_num INT NOT NULL DEFAULT 1 COMMENT '当前人数',
    target_num INT NOT NULL COMMENT '目标成团人数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-拼团中，2-拼团成功，3-拼团失败',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    success_time DATETIME COMMENT '成团时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_group_no (group_no),
    INDEX idx_tenant_activity (tenant_id, activity_id),
    INDEX idx_status_expire (status, expire_time),
    INDEX idx_leader_member (leader_member_id)
) COMMENT '拼团组表';

CREATE TABLE IF NOT EXISTS flow_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    flow_no VARCHAR(32) NOT NULL UNIQUE,
    order_id BIGINT,
    amount DECIMAL(10,2) NOT NULL,
    type TINYINT NOT NULL,
    pay_status TINYINT NOT NULL,
    transaction_id VARCHAR(64),
    reconcile_status TINYINT NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS ai_call_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    prompt TEXT,
    result TEXT,
    cost_times INT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS member_shop_bind (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    openid VARCHAR(100) NOT NULL,
    tenant_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    last_visit_time TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    bind_source TINYINT DEFAULT 1,
    referrer_member_id BIGINT,
    UNIQUE KEY uk_openid_tenant (openid, tenant_id)
);

CREATE TABLE IF NOT EXISTS verify_code_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    verify_code VARCHAR(10) NOT NULL UNIQUE,
    member_card_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    expire_time TIMESTAMP NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    verify_time TIMESTAMP,
    verify_user_id BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

-- 二维码相关表
CREATE TABLE IF NOT EXISTS shop_qr_code (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    qr_type TINYINT NOT NULL DEFAULT 1,
    qr_name VARCHAR(100),
    scene VARCHAR(128),
    qr_url VARCHAR(500),
    qr_ticket VARCHAR(256),
    expire_time TIMESTAMP,
    is_default TINYINT NOT NULL DEFAULT 0,
    scan_count INT NOT NULL DEFAULT 0,
    create_user BIGINT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_user BIGINT,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS qr_scan_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    qr_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    openid VARCHAR(100),
    unionid VARCHAR(100),
    member_id BIGINT,
    device_type VARCHAR(20),
    scan_result TINYINT,
    fail_reason VARCHAR(200),
    ip VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
