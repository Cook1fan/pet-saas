-- =============================================
-- 商品成本与库存优化 - 数据库迁移脚本
-- 执行日期：2026-04-02
-- =============================================

USE pet_saas;

-- 1. 修改 cost_price 为 NOT NULL
ALTER TABLE goods_sku
    MODIFY COLUMN cost_price DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '成本价';

-- 2. 新增 is_unlimited_stock 字段
ALTER TABLE goods_sku
    ADD COLUMN is_unlimited_stock TINYINT NOT NULL DEFAULT 0 COMMENT '是否无限库存：0-否，1-是' AFTER stock;

-- 3. 新增 reserved_stock 字段
ALTER TABLE goods_sku
    ADD COLUMN reserved_stock INT NOT NULL DEFAULT 0 COMMENT '预留库存（在途库存）' AFTER is_unlimited_stock;

-- 4. 新增 allow_negative_stock 字段
ALTER TABLE goods_sku
    ADD COLUMN allow_negative_stock TINYINT NOT NULL DEFAULT 0 COMMENT '是否允许负库存：0-否，1-是' AFTER reserved_stock;

-- 5. 修改 barcode 字段并增加索引
ALTER TABLE goods_sku
    MODIFY COLUMN barcode VARCHAR(50) COMMENT '商品条码';
ALTER TABLE goods_sku
    ADD INDEX idx_barcode (barcode);

-- 6. 增加 sku_code 唯一索引（先检查是否存在，不存在则创建）
-- 注意：MySQL 没有 CREATE INDEX IF NOT EXISTS，所以我们用存储过程来处理
DELIMITER $$
DROP PROCEDURE IF EXISTS add_unique_index$$
CREATE PROCEDURE add_unique_index()
BEGIN
    DECLARE index_count INT;
    SELECT COUNT(*) INTO index_count
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'goods_sku'
      AND index_name = 'uk_tenant_sku_code';
    IF index_count = 0 THEN
        ALTER TABLE goods_sku ADD UNIQUE KEY uk_tenant_sku_code (tenant_id, sku_code);
    END IF;
END$$
DELIMITER ;
CALL add_unique_index();
DROP PROCEDURE IF EXISTS add_unique_index;

-- 7. 订单明细表增加成本价快照字段
ALTER TABLE order_item
    ADD COLUMN cost_price DECIMAL(10,2) COMMENT '成本价（快照）' AFTER price;

-- 8. 数据迁移：将 stock=-1 的旧数据迁移到 is_unlimited_stock=1
UPDATE goods_sku SET is_unlimited_stock = 1 WHERE stock = -1;
UPDATE goods_sku SET stock = 0 WHERE stock = -1;
