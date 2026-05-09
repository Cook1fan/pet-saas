-- =============================================
-- 删除 tenant_id=4 的商品相关数据
-- 注意：请谨慎执行，删除前请先备份！
-- =============================================

USE pet_saas;

-- 1. 删除商品变更日志
DELETE FROM goods_change_log WHERE tenant_id = 4;

-- 2. 删除库存出入库记录
DELETE FROM stock_record WHERE tenant_id = 4;

-- 3. 删除订单明细表中的商品相关数据（可选，如果需要保留订单数据请注释掉）
-- 如果要保留订单数据，只删除商品引用，可以用 UPDATE 把 goods_id 和 sku_id 设为 null
-- UPDATE order_item SET goods_id = NULL, sku_id = NULL WHERE tenant_id = 4;
DELETE FROM order_item WHERE tenant_id = 4;

-- 4. 删除商品 SKU
DELETE FROM goods_sku WHERE tenant_id = 4;

-- 5. 删除商品基础信息
DELETE FROM goods WHERE tenant_id = 4;

-- =============================================
-- 操作完成提示
-- =============================================
SELECT 'tenant_id=4 的商品相关数据已删除' AS message;
