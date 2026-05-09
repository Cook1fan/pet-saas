-- =============================================
-- 订单操作日志表补充字段
-- 创建日期：2026-04-03
-- 说明：
--   补充 create_user 和 update_user 字段，与 BaseEntity 保持一致
-- =============================================

ALTER TABLE order_operation_log ADD COLUMN create_user BIGINT COMMENT '创建人 ID' AFTER create_time;
ALTER TABLE order_operation_log ADD COLUMN update_user BIGINT COMMENT '修改人 ID' AFTER update_time;
