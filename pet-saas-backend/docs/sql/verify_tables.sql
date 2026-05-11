-- 验证所有储蓄卡次相关表是否创建成功
USE pet_saas;

-- 显示所有表
SHOW TABLES;

-- 查看每个表的结构
DESC recharge_order;
DESC card_order;
DESC recharge_consume_record;
DESC card_verify_record;

-- 完成信息
SELECT '所有储蓄卡次相关表创建成功！' AS message;
