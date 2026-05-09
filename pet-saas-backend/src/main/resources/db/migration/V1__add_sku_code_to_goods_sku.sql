-- =============================================
-- 商品 SKU 编码与条形码功能数据库迁移脚本
-- 创建日期: 2026-04-02
-- =============================================

-- 1. 给 goods_sku 表添加内部 SKU 编码字段
ALTER TABLE goods_sku
    ADD COLUMN sku_code VARCHAR(32) NOT NULL COMMENT '系统内部SKU编码' AFTER goods_id;

-- 2. 添加唯一索引（同一租户下 sku_code 唯一）
ALTER TABLE goods_sku
    ADD UNIQUE KEY uk_tenant_sku_code (tenant_id, sku_code);

-- 3. 给 barcode 字段添加普通索引（用于扫码查询）
ALTER TABLE goods_sku
    ADD INDEX idx_barcode (tenant_id, barcode);

-- 注意：
-- 4. 现有数据需要使用 Java 代码批量生成 sku_code
-- 示例代码（在 Java Service 中执行）：
-- List<GoodsSku> skuList = goodsSkuService.list();
-- for (GoodsSku sku : skuList) {
--     if (sku.getSkuCode() == null) {
--         sku.setSkuCode(String.valueOf(IdUtil.getSnowflakeNextId()));
--         goodsSkuService.updateById(sku);
--     }
-- }

