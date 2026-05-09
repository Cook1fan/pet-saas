-- =============================================
-- 商品库存管理与SKU优化 - 数据库迁移脚本
-- 创建时间: 2026-04-01
-- =============================================

USE pet_saas;

-- =============================================
-- 1. 扩建 stock_record 表（库存流水表）
-- =============================================

-- 先删除原表（如果有数据，需要先备份）
-- DROP TABLE IF EXISTS stock_record;

-- 重新创建 stock_record 表
CREATE TABLE IF NOT EXISTS stock_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    sku_id BIGINT NOT NULL COMMENT '商品 SKU ID',

    -- 库存变动核心信息
    type TINYINT NOT NULL COMMENT '变动类型：1-采购入库，2-手动入库，3-销售出库，4-手动出库，5-盘点调整，6-退货入库，7-领用出库',
    change_num INT NOT NULL COMMENT '变动数量（正数入库，负数出库）',
    before_stock INT NOT NULL COMMENT '变动前库存',
    after_stock INT NOT NULL COMMENT '变动后库存',

    -- 关联信息
    batch_no VARCHAR(32) COMMENT '批次号（同一批次操作共享）',
    related_type VARCHAR(50) COMMENT '关联单据类型：order-订单，purchase-采购单，check-盘点单',
    related_id BIGINT COMMENT '关联单据 ID',
    related_no VARCHAR(64) COMMENT '关联单据号',

    -- 审核信息
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变动时间',
    create_user BIGINT NOT NULL COMMENT '操作人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

    INDEX idx_tenant_id (tenant_id),
    INDEX idx_sku_id (sku_id),
    INDEX idx_type (type),
    INDEX idx_batch_no (batch_no),
    INDEX idx_related (related_type, related_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存流水表';

-- =============================================
-- 2. 新增 goods_change_log 表（商品变更日志表）
-- =============================================

CREATE TABLE IF NOT EXISTS goods_change_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',

    -- 变更主体
    data_type TINYINT NOT NULL COMMENT '数据类型：1-goods商品，2-goods_sku商品规格',
    data_id BIGINT NOT NULL COMMENT '数据 ID（goods.id 或 goods_sku.id）',

    -- 变更内容
    change_type TINYINT NOT NULL COMMENT '变更类型：1-新增，2-修改，3-删除',
    field_name VARCHAR(50) COMMENT '变更字段名（修改时有值）',
    before_value TEXT COMMENT '变更前值',
    after_value TEXT COMMENT '变更后值',

    -- 变更批次
    batch_no VARCHAR(32) COMMENT '批次号（同一次保存操作共享）',

    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    create_user BIGINT NOT NULL COMMENT '操作人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

    INDEX idx_tenant_id (tenant_id),
    INDEX idx_data (data_type, data_id),
    INDEX idx_batch_no (batch_no),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品变更日志表';

-- =============================================
-- 3. goods_sku 表补充字段
-- =============================================

-- 给 goods_sku 表增加乐观锁版本号（用于并发控制）
ALTER TABLE goods_sku ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER update_user;

-- =============================================
-- 迁移完成
-- =============================================
