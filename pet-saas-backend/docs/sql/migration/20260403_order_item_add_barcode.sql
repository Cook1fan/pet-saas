-- =============================================
-- order_item 表增加 barcode 字段
-- 创建日期：2026-04-03
-- =============================================

ALTER TABLE order_item ADD COLUMN barcode VARCHAR(64) COMMENT '商品条码' AFTER sku_id;
ALTER TABLE order_item ADD INDEX idx_barcode (barcode);
