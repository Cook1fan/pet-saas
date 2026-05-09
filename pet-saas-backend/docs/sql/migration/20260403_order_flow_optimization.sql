-- =============================================
-- 订单流程与库存优化 SQL 迁移脚本
-- 创建日期：2026-04-03
-- 说明：
--   1. 补充 goods_sku 表字段
--   2. 补充 order_item 表 sku_id 字段
--   3. order_info 表新增订单状态相关字段
--   4. 新增订单操作日志表 order_operation_log
-- =============================================

-- =============================================
-- 1. goods_sku 表补充字段
-- =============================================
ALTER TABLE goods_sku ADD COLUMN reserved_stock INT NOT NULL DEFAULT 0 COMMENT '预留库存（待支付订单占用）' AFTER stock;
ALTER TABLE goods_sku ADD COLUMN is_unlimited_stock TINYINT NOT NULL DEFAULT 0 COMMENT '是否无限库存：0-否，1-是' AFTER reserved_stock;
ALTER TABLE goods_sku ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER status;

-- =============================================
-- 2. order_item 表补充 sku_id 字段
-- =============================================
ALTER TABLE order_item ADD COLUMN sku_id BIGINT COMMENT '商品SKU ID' AFTER goods_id;
ALTER TABLE order_item ADD INDEX idx_sku_id (sku_id);

-- =============================================
-- 3. order_info 表新增订单状态字段
-- =============================================
ALTER TABLE order_info ADD COLUMN order_source TINYINT NOT NULL DEFAULT 1 COMMENT '订单来源：1-PC端开单收银，2-C端小程序，3-商家端' AFTER pay_status;
ALTER TABLE order_info ADD COLUMN order_status TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消，5-已退款' AFTER order_source;
ALTER TABLE order_info ADD COLUMN cancel_time DATETIME COMMENT '取消时间' AFTER order_status;
ALTER TABLE order_info ADD COLUMN cancel_reason VARCHAR(200) COMMENT '取消原因' AFTER cancel_time;
ALTER TABLE order_info ADD COLUMN expire_time DATETIME COMMENT '订单过期时间' AFTER cancel_reason;

ALTER TABLE order_info ADD INDEX idx_order_source (order_source);
ALTER TABLE order_info ADD INDEX idx_order_status (order_status);
ALTER TABLE order_info ADD INDEX idx_expire_time (expire_time);

-- 初始化历史数据的 order_status（基于 pay_status）
UPDATE order_info SET order_status = pay_status WHERE order_status = 0;

-- =============================================
-- 4. 新增订单操作日志表 order_operation_log
-- =============================================
CREATE TABLE IF NOT EXISTS order_operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    order_id BIGINT NOT NULL COMMENT '订单 ID',
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    before_status TINYINT COMMENT '变更前状态',
    after_status TINYINT NOT NULL COMMENT '变更后状态',
    operation_type TINYINT NOT NULL COMMENT '操作类型：1-创建，2-支付，3-发货，4-完成，5-取消，6-退款',
    operation_desc VARCHAR(200) COMMENT '操作描述',
    operator_id BIGINT COMMENT '操作人 ID',
    operator_name VARCHAR(50) COMMENT '操作人姓名',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_tenant_id (tenant_id),
    INDEX idx_order_id (order_id),
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单操作日志表';
