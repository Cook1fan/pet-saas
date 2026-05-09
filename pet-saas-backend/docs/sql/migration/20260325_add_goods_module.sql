-- =============================================
-- 宠物门店商品模块 MVP - 数据库迁移脚本
-- 执行时间: 2026-03-25
-- 说明: 新增商品分类表、商品规格表，修改商品表、出入库记录表、订单明细表
-- =============================================

USE pet_saas;

-- =============================================
-- 1. 新增商品分类表
-- =============================================
CREATE TABLE goods_category (
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
-- 2. 修改商品表（无数据，直接重建）
-- =============================================
DROP TABLE IF EXISTS goods;

CREATE TABLE goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    category_id BIGINT NOT NULL COMMENT '分类 ID，关联 goods_category',
    goods_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    main_image VARCHAR(255) COMMENT '商品主图 URL',
    is_service TINYINT NOT NULL DEFAULT 0 COMMENT '是否服务类商品：0-实物商品，1-服务商品',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_user BIGINT COMMENT '创建人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    update_user BIGINT COMMENT '修改人 ID',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品基础信息表';

-- =============================================
-- 3. 新增商品规格表
-- =============================================
CREATE TABLE goods_sku (
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

-- =============================================
-- 4. 修改出入库记录表
-- =============================================
ALTER TABLE stock_record ADD COLUMN sku_id BIGINT COMMENT '商品 SKU ID';
ALTER TABLE stock_record ADD INDEX idx_sku_id (sku_id);

-- =============================================
-- 5. 修改订单明细表
-- =============================================
ALTER TABLE order_item ADD COLUMN sku_id BIGINT COMMENT '商品 SKU ID';

-- =============================================
-- 6. 初始化商品分类数据
-- =============================================
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
