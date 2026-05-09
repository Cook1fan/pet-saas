-- =============================================
-- 宠物门店私域运营一体化 SaaS - 数据库初始化脚本
-- 数据库版本: MySQL 8.0+
-- 创建时间: 2026-03-25
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS pet_saas DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pet_saas;

-- =============================================
-- 6.1 全局表（无 tenant_id）
-- =============================================

-- 6.1.1 sys_platform_admin（平台超级管理员表）
CREATE TABLE IF NOT EXISTS sys_platform_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '账号',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    role VARCHAR(20) NOT NULL COMMENT '角色',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='平台超级管理员表';

-- 6.1.2 sys_tenant（租户表）
CREATE TABLE IF NOT EXISTS sys_tenant (
    tenant_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '租户 ID',
    shop_name VARCHAR(100) NOT NULL COMMENT '门店名称',
    admin_phone VARCHAR(20) NOT NULL COMMENT '老板手机号',
    address VARCHAR(200) COMMENT '门店地址',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_shop_name (shop_name),
    INDEX idx_admin_phone (admin_phone),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户表';

-- 6.1.3 sys_ai_package（AI 增值套餐表）
CREATE TABLE IF NOT EXISTS sys_ai_package (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    name VARCHAR(50) NOT NULL COMMENT '套餐名称',
    times INT NOT NULL COMMENT '次数',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 增值套餐表';

-- 6.1.4 sys_config（系统全局配置表）
CREATE TABLE IF NOT EXISTS sys_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    config_key VARCHAR(50) NOT NULL UNIQUE COMMENT '配置键',
    config_value VARCHAR(500) COMMENT '配置值',
    description VARCHAR(200) COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统全局配置表';

-- 6.1.5 goods_category（商品分类表）
CREATE TABLE IF NOT EXISTS goods_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    parent_id BIGINT NOT NULL COMMENT '父分类 ID，0 表示一级大类',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    sort INT NOT NULL COMMENT '排序，数字越小越靠前',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- =============================================
-- 6.2 业务表（带 tenant_id）
-- =============================================

-- 6.2.1 shop_admin（门店管理员/店员表）
CREATE TABLE IF NOT EXISTS shop_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    username VARCHAR(50) NOT NULL COMMENT '账号',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    role VARCHAR(20) NOT NULL COMMENT '角色：shop_admin-管理员，shop_staff-店员',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_tenant_username (tenant_id, username),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店管理员/店员表';

-- 6.2.2 shop_config（门店基础配置表）
CREATE TABLE IF NOT EXISTS shop_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL UNIQUE COMMENT '租户 ID',
    shop_name VARCHAR(100) NOT NULL COMMENT '门店名称',
    address VARCHAR(200) COMMENT '地址',
    phone VARCHAR(20) COMMENT '联系电话',
    business_hours VARCHAR(100) COMMENT '营业时间',
    logo VARCHAR(500) COMMENT 'logo URL',
    pay_config TEXT COMMENT '微信支付配置（JSON）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='门店基础配置表';

-- 6.2.3 member（会员表）
CREATE TABLE IF NOT EXISTS member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    name VARCHAR(50) COMMENT '姓名',
    tags VARCHAR(500) COMMENT '标签（逗号分隔）',
    openid VARCHAR(100) COMMENT '微信 openid',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_tenant_phone (tenant_id, phone),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';

-- 6.2.4 pet_info（宠物档案表）
CREATE TABLE IF NOT EXISTS pet_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    member_id BIGINT NOT NULL COMMENT '会员 ID',
    name VARCHAR(50) NOT NULL COMMENT '宠物名',
    breed VARCHAR(50) COMMENT '品种',
    birthday DATE COMMENT '生日',
    gender TINYINT COMMENT '性别：0-未知，1-公，2-母',
    vaccine_time DATE COMMENT '疫苗日期',
    deworm_time DATE COMMENT '驱虫日期',
    wash_time DATE COMMENT '洗护日期',
    next_remind_time DATETIME COMMENT '下次提醒时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_member_id (member_id),
    INDEX idx_next_remind_time (next_remind_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='宠物档案表';

-- 6.2.5 recharge_rule（储值规则表）
CREATE TABLE IF NOT EXISTS recharge_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    name VARCHAR(50) NOT NULL COMMENT '规则名称',
    recharge_amount DECIMAL(10,2) NOT NULL COMMENT '储值金额',
    give_amount DECIMAL(10,2) NOT NULL COMMENT '赠送金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='储值规则表';

-- 6.2.6 card_rule（次卡规则表）
CREATE TABLE IF NOT EXISTS card_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    name VARCHAR(50) NOT NULL COMMENT '规则名称',
    times INT NOT NULL COMMENT '次数',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    valid_days INT NOT NULL COMMENT '有效期（天）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='次卡规则表';

-- 6.2.7 member_account（会员储值账户表）
CREATE TABLE IF NOT EXISTS member_account (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    member_id BIGINT NOT NULL UNIQUE COMMENT '会员 ID',
    balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '余额',
    total_recharge DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计储值',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_tenant_member (tenant_id, member_id),
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员储值账户表';

-- 6.2.8 member_card（会员次卡表）
CREATE TABLE IF NOT EXISTS member_card (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    member_id BIGINT NOT NULL COMMENT '会员 ID',
    card_rule_id BIGINT NOT NULL COMMENT '次卡规则 ID',
    remain_times INT NOT NULL COMMENT '剩余次数',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_member_id (member_id),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员次卡表';

-- 6.2.9 goods（商品基础信息表）
CREATE TABLE IF NOT EXISTS goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    category_id BIGINT NOT NULL COMMENT '分类 ID，关联 goods_category',
    goods_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    main_image VARCHAR(255) COMMENT '商品主图 URL',
    is_service TINYINT NOT NULL DEFAULT 0 COMMENT '是否服务类商品：0-实物商品，1-服务商品',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品基础信息表';

-- 6.2.10 goods_sku（商品规格表）
CREATE TABLE IF NOT EXISTS goods_sku (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    goods_id BIGINT NOT NULL COMMENT '商品 ID，关联 goods',
    spec_name VARCHAR(50) NOT NULL COMMENT '规格名称（比如「规格」「重量」「体型」）',
    spec_value VARCHAR(50) NOT NULL COMMENT '规格值（比如「1.5kg」「5kg」「小型犬」）',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    cost_price DECIMAL(10,2) COMMENT '成本价（预留）',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存：-1 表示不限库存（服务类商品用）',
    warn_stock INT COMMENT '库存预警值（实物商品用）',
    barcode VARCHAR(50) COMMENT '商品条码（预留）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_goods_id (goods_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格表';

-- 6.2.11 stock_record（出入库记录表）
CREATE TABLE IF NOT EXISTS stock_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    sku_id BIGINT NOT NULL COMMENT '商品 SKU ID',
    type TINYINT NOT NULL COMMENT '类型：1-入库，2-出库',
    num INT NOT NULL COMMENT '数量',
    remark VARCHAR(200) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT NOT NULL COMMENT '操作人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_sku_id (sku_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出入库记录表';

-- 6.2.12 order_info（订单表）
CREATE TABLE IF NOT EXISTS order_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    member_id BIGINT COMMENT '会员 ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    pay_type TINYINT NOT NULL COMMENT '支付方式：1-微信，2-现金，3-储值，4-次卡',
    pay_status TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-待支付，1-已支付，2-已退款',
    pay_time DATETIME COMMENT '支付时间',
    transaction_id VARCHAR(64) COMMENT '微信支付单号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_member_id (member_id),
    INDEX idx_pay_status (pay_status),
    INDEX idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 6.2.13 order_item（订单明细表）
CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    order_id BIGINT NOT NULL COMMENT '订单 ID',
    goods_id BIGINT NOT NULL COMMENT '商品 ID',
    sku_id BIGINT COMMENT '商品 SKU ID',
    goods_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    num INT NOT NULL COMMENT '数量',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- 6.2.14 activity_info（拼团/秒杀活动表）
CREATE TABLE IF NOT EXISTS activity_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    type TINYINT NOT NULL COMMENT '活动类型：1-拼团，2-秒杀',
    title VARCHAR(100) NOT NULL COMMENT '活动标题',
    goods_id BIGINT NOT NULL COMMENT '商品/服务 ID',
    price DECIMAL(10,2) NOT NULL COMMENT '活动价格',
    origin_price DECIMAL(10,2) NOT NULL COMMENT '原价',
    group_count INT COMMENT '成团人数（拼团用）',
    stock INT NOT NULL COMMENT '活动库存',
    limit_num INT NOT NULL COMMENT '限购数量',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
    qr_code_url VARCHAR(500) COMMENT '活动小程序码 URL',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_type (type),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拼团/秒杀活动表';

-- 6.2.15 activity_order（活动订单表）
CREATE TABLE IF NOT EXISTS activity_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    activity_id BIGINT NOT NULL COMMENT '活动 ID',
    order_id BIGINT NOT NULL COMMENT '订单 ID',
    member_id BIGINT NOT NULL COMMENT '会员 ID',
    group_id BIGINT COMMENT '拼团 ID（拼团用）',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待支付，1-已支付，2-拼团成功，3-拼团失败',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_activity_id (activity_id),
    INDEX idx_group_id (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动订单表';

-- 6.2.16 flow_record（流水记录表）
CREATE TABLE IF NOT EXISTS flow_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    flow_no VARCHAR(32) NOT NULL UNIQUE COMMENT '流水号',
    order_id BIGINT COMMENT '订单 ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    type TINYINT NOT NULL COMMENT '流水类型：1-充值，2-消费，3-退款',
    pay_status TINYINT NOT NULL COMMENT '支付状态',
    transaction_id VARCHAR(64) COMMENT '微信支付单号',
    reconcile_status TINYINT NOT NULL DEFAULT 0 COMMENT '对账状态：0-未对账，1-已对账，2-异常',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_flow_no (flow_no),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_order_id (order_id),
    INDEX idx_type (type),
    INDEX idx_transaction_id (transaction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='流水记录表';

-- 6.2.17 ai_call_record（AI 调用记录表）
CREATE TABLE IF NOT EXISTS ai_call_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    type VARCHAR(20) NOT NULL COMMENT '类型：copy-文案生成，chat-客服问答，remind-智能提醒',
    prompt TEXT COMMENT '提示词',
    result TEXT COMMENT '结果',
    cost_times INT NOT NULL DEFAULT 1 COMMENT '消耗次数',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 调用记录表';

-- 6.2.18 member_shop_bind（会员门店绑定表）
CREATE TABLE IF NOT EXISTS member_shop_bind (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    openid VARCHAR(100) NOT NULL COMMENT '微信 openid',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    member_id BIGINT NOT NULL COMMENT '会员 ID（该门店下的会员）',
    last_visit_time DATETIME COMMENT '最后访问时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（绑定时间）',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_openid_tenant (openid, tenant_id),
    INDEX idx_openid (openid),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员门店绑定表';

-- 6.2.19 verify_code_record（核销码记录表）
CREATE TABLE IF NOT EXISTS verify_code_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    verify_code VARCHAR(10) NOT NULL COMMENT '核销码（6位数字）',
    member_card_id BIGINT NOT NULL COMMENT '会员次卡 ID',
    member_id BIGINT NOT NULL COMMENT '会员 ID',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已失效',
    verify_time DATETIME COMMENT '核销时间',
    verify_user_id BIGINT COMMENT '核销人 ID（商家管理员/店员 ID）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    UNIQUE KEY uk_verify_code (verify_code),
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_member_card_id (member_card_id),
    INDEX idx_member_id (member_id),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='核销码记录表';

-- =============================================
-- 初始化数据
-- =============================================

-- 初始化平台管理员（密码: admin123，使用 BCrypt 加密）
INSERT INTO sys_platform_admin (username, password, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6zFO', 'platform_admin');

-- 初始化系统配置
INSERT INTO sys_config (config_key, config_value, description) VALUES
('ai.free.daily.limit', '50', 'AI 每日免费调用次数');

-- 初始化 AI 套餐
INSERT INTO sys_ai_package (name, times, price, status) VALUES
('基础套餐', 100, 9.90, 1),
('进阶套餐', 500, 39.90, 1),
('高级套餐', 2000, 149.90, 1);

-- 初始化商品分类
INSERT INTO goods_category (id, parent_id, category_name, sort, create_time) VALUES
(1, 0, '宠物食品类', 1, NOW()),
(2, 1, '主粮', 1, NOW()),
(3, 1, '湿粮', 2, NOW()),
(4, 1, '零食', 3, NOW()),
(5, 1, '保健品', 4, NOW()),
(6, 1, '处方粮', 5, NOW()),
(7, 0, '宠物用品类', 2, NOW()),
(8, 7, '猫砂', 1, NOW()),
(9, 7, '窝垫玩具', 2, NOW()),
(10, 7, '出行清洁', 3, NOW()),
(11, 7, '日用穿戴', 4, NOW()),
(12, 0, '宠物服务类', 3, NOW()),
(13, 12, '洗护美容', 1, NOW()),
(14, 12, '寄养服务', 2, NOW()),
(15, 12, '上门服务', 3, NOW()),
(16, 12, '其他服务', 4, NOW()),
(17, 0, '宠物医疗类', 4, NOW()),
(18, 17, '疫苗驱虫', 1, NOW()),
(19, 17, '体检服务', 2, NOW()),
(20, 17, '药品耗材', 3, NOW());
