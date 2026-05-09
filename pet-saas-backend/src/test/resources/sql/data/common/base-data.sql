-- =============================================
-- 测试基础数据
-- =============================================

-- 平台管理员（密码: admin123）
INSERT INTO sys_platform_admin (username, password, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6zFO', 'platform_admin');

-- 系统配置
INSERT INTO sys_config (config_key, config_value, description) VALUES
('ai.free.daily.limit', '50', 'AI 每日免费调用次数');

-- 租户数据
INSERT INTO sys_tenant (tenant_id, shop_name, admin_phone, address, status) VALUES
(1, '测试宠物店1', '13800138001', '北京市朝阳区测试路1号', 1),
(2, '测试宠物店2', '13800138002', '北京市海淀区测试路2号', 1);

-- 门店管理员（密码: 123456）
INSERT INTO shop_admin (id, tenant_id, username, password, role, status) VALUES
(1, 1, 'shop1_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6zFO', 'shop_admin', 1),
(2, 2, 'shop2_admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6zFO', 'shop_admin', 1);

-- 门店配置
INSERT INTO shop_config (tenant_id, shop_name, address, phone, business_hours) VALUES
(1, '测试宠物店1', '北京市朝阳区测试路1号', '010-12345678', '09:00-21:00'),
(2, '测试宠物店2', '北京市海淀区测试路2号', '010-87654321', '10:00-22:00');

-- 会员数据
INSERT INTO member (id, tenant_id, phone, name, openid) VALUES
(1, 1, '13900139001', '张三', 'openid_001'),
(2, 1, '13900139002', '李四', 'openid_002'),
(3, 2, '13900139003', '王五', 'openid_003');

-- 会员储值账户
INSERT INTO member_account (tenant_id, member_id, balance, total_recharge) VALUES
(1, 1, 1000.00, 1000.00),
(1, 2, 0.00, 0.00),
(2, 3, 500.00, 500.00);

-- 商品数据
INSERT INTO goods (id, tenant_id, name, category, sub_category, price, stock, warn_stock, is_service, status) VALUES
(1, 1, '皇家狗粮', 1, 1, 199.00, 100, 10, 0, 1),
(2, 1, '宠物洗澡', 3, 1, 50.00, 9999, 0, 1, 1),
(3, 1, '宠物用品', 2, 1, 29.90, 50, 5, 0, 1),
(4, 2, '猫粮', 1, 2, 159.00, 80, 8, 0, 1);

-- 储值规则
INSERT INTO recharge_rule (tenant_id, name, recharge_amount, give_amount, status) VALUES
(1, '充1000送200', 1000.00, 200.00, 1),
(1, '充500送80', 500.00, 80.00, 1),
(2, '充1000送150', 1000.00, 150.00, 1);

-- 次卡规则
INSERT INTO card_rule (tenant_id, name, times, price, valid_days, status) VALUES
(1, '洗澡10次卡', 10, 400.00, 365, 1),
(1, '美容5次卡', 5, 500.00, 180, 1),
(2, '洗澡8次卡', 8, 350.00, 365, 1);
