-- ============================================
-- 商品测试数据
-- tenant_id = 1 作为测试租户
-- ============================================

-- 商品分类数据（已在文档中定义，这里再次确保存在）
INSERT INTO goods_category (id, parent_id, category_name, sort, create_time, is_deleted) VALUES
(1, 0, '宠物食品类', 1, NOW(), 0),
(2, 1, '主粮', 1, NOW(), 0),
(3, 1, '湿粮', 2, NOW(), 0),
(4, 1, '零食', 3, NOW(), 0),
(5, 1, '保健品', 4, NOW(), 0),
(6, 1, '处方粮', 5, NOW(), 0),
(7, 0, '宠物用品类', 2, NOW(), 0),
(8, 7, '猫砂', 1, NOW(), 0),
(9, 7, '窝垫玩具', 2, NOW(), 0),
(10, 7, '出行清洁', 3, NOW(), 0),
(11, 7, '日用穿戴', 4, NOW(), 0),
(12, 0, '宠物服务类', 3, NOW(), 0),
(13, 12, '洗护美容', 1, NOW(), 0),
(14, 12, '寄养服务', 2, NOW(), 0),
(15, 12, '上门服务', 3, NOW(), 0),
(16, 12, '其他服务', 4, NOW(), 0),
(17, 0, '宠物医疗类', 4, NOW(), 0),
(18, 17, '疫苗驱虫', 1, NOW(), 0),
(19, 17, '体检服务', 2, NOW(), 0),
(20, 17, '药品耗材', 3, NOW(), 0)
ON DUPLICATE KEY UPDATE update_time = NOW();

-- ============================================
-- 商品基础数据 (goods)
-- ============================================
INSERT INTO goods (tenant_id, category_id, goods_name, main_image, is_service, status, create_time, is_deleted) VALUES
-- 主粮类 (category_id=2)
(1, 2, '皇家狗粮成犬通用型', 'https://example.com/images/goods/royal-dog-adult.jpg', 0, 1, NOW(), 0),
(1, 2, '皇家狗粮幼犬通用型', 'https://example.com/images/goods/royal-dog-puppy.jpg', 0, 1, NOW(), 0),
(1, 2, '渴望鸡肉猫粮', 'https://example.com/images/goods/orijen-cat-chicken.jpg', 0, 1, NOW(), 0),
(1, 2, '渴望六种鱼猫粮', 'https://example.com/images/goods/orijen-cat-6fish.jpg', 0, 1, NOW(), 0),
(1, 2, '巅峰牛肉狗粮', 'https://example.com/images/goods/ziwi-dog-beef.jpg', 0, 1, NOW(), 0),
(1, 2, '巅峰羊肉猫粮', 'https://example.com/images/goods/ziwi-cat-lamb.jpg', 0, 1, NOW(), 0),
(1, 2, '伯纳天纯小型犬成犬粮', 'https://example.com/images/goods/pure-small-adult.jpg', 0, 1, NOW(), 0),
(1, 2, '伯纳天纯中大型犬幼犬粮', 'https://example.com/images/goods/pure-large-puppy.jpg', 0, 1, NOW(), 0),
(1, 2, '卫仕猫粮全价成猫', 'https://example.com/images/goods/nourse-cat-adult.jpg', 0, 1, NOW(), 0),
(1, 2, '卫仕狗粮全价幼犬', 'https://example.com/images/goods/nourse-dog-puppy.jpg', 0, 1, NOW(), 0),

-- 湿粮类 (category_id=3)
(1, 3, '巅峰牛肉狗罐头', 'https://example.com/images/goods/ziwi-dog-can-beef.jpg', 0, 1, NOW(), 0),
(1, 3, '巅峰鸡肉猫罐头', 'https://example.com/images/goods/ziwi-cat-can-chicken.jpg', 0, 1, NOW(), 0),
(1, 3, 'K9 Natural狗湿粮包', 'https://example.com/images/goods/k9-dog-pouch.jpg', 0, 1, NOW(), 0),
(1, 3, 'K9 Natural猫湿粮包', 'https://example.com/images/goods/k9-cat-pouch.jpg', 0, 1, NOW(), 0),
(1, 3, '希尔斯AD处方罐头', 'https://example.com/images/goods/hills-ad-can.jpg', 0, 1, NOW(), 0),
(1, 3, '希尔斯KD处方罐头', 'https://example.com/images/goods/hills-kd-can.jpg', 0, 1, NOW(), 0),

-- 零食类 (category_id=4)
(1, 4, '洁齿骨狗狗磨牙棒', 'https://example.com/images/goods/dental-chew.jpg', 0, 1, NOW(), 0),
(1, 4, '鸡胸肉干宠物零食', 'https://example.com/images/goods/chicken-jerky.jpg', 0, 1, NOW(), 0),
(1, 4, '三文鱼冻干猫零食', 'https://example.com/images/goods/salmon-freeze-dried.jpg', 0, 1, NOW(), 0),
(1, 4, '蛋黄冻干猫狗通用', 'https://example.com/images/goods/egg-yolk-freeze-dried.jpg', 0, 1, NOW(), 0),
(1, 4, '猫条流质零食', 'https://example.com/images/goods/cat-strip.jpg', 0, 1, NOW(), 0),
(1, 4, '狗狗训练零食牛肉粒', 'https://example.com/images/goods/dog-train-beef.jpg', 0, 1, NOW(), 0),
(1, 4, '猫咪薄荷饼干', 'https://example.com/images/goods/cat-cookie.jpg', 0, 1, NOW(), 0),
(1, 4, '鹿角狗狗磨牙玩具', 'https://example.com/images/goods/antler-chew.jpg', 0, 1, NOW(), 0),

-- 保健品 (category_id=5)
(1, 5, '卫仕卵磷脂狗狗美毛', 'https://example.com/images/goods/nourse-lecithin-dog.jpg', 0, 1, NOW(), 0),
(1, 5, '卫仕化毛球片猫用', 'https://example.com/images/goods/nourse-hairball-cat.jpg', 0, 1, NOW(), 0),
(1, 5, '麦德氏羊奶粉幼犬', 'https://example.com/images/goods/mcdog-milk-puppy.jpg', 0, 1, NOW(), 0),
(1, 5, '麦德氏羊奶粉幼猫', 'https://example.com/images/goods/mcdog-milk-kitten.jpg', 0, 1, NOW(), 0),
(1, 5, '红狗营养膏猫狗通用', 'https://example.com/images/goods/reddog-nutrition-gel.jpg', 0, 1, NOW(), 0),
(1, 5, '红狗化毛膏猫用', 'https://example.com/images/goods/reddog-hairball-gel.jpg', 0, 1, NOW(), 0),
(1, 5, 'MAG关节生狗狗鲨鱼软骨素', 'https://example.com/images/goods/mag-joint-dog.jpg', 0, 1, NOW(), 0),
(1, 5, '布拉迪酵母益生菌猫狗', 'https://example.com/images/goods/probiotics.jpg', 0, 1, NOW(), 0),

-- 猫砂类 (category_id=8)
(1, 8, 'pidan混合猫砂', 'https://example.com/images/goods/pidan-cat-litter.jpg', 0, 1, NOW(), 0),
(1, 8, '小佩混合猫砂', 'https://example.com/images/goods/xiaopei-cat-litter.jpg', 0, 1, NOW(), 0),
(1, 8, '膨润土猫砂10kg', 'https://example.com/images/goods/bentonite-cat-litter.jpg', 0, 1, NOW(), 0),
(1, 8, '豆腐猫砂绿茶味', 'https://example.com/images/goods/tofu-cat-litter-green.jpg', 0, 1, NOW(), 0),
(1, 8, '水晶猫砂除臭', 'https://example.com/images/goods/silica-cat-litter.jpg', 0, 1, NOW(), 0),

-- 窝垫玩具类 (category_id=9)
(1, 9, '冬季保暖狗窝', 'https://example.com/images/goods/dog-bed-winter.jpg', 0, 1, NOW(), 0),
(1, 9, '夏季凉席猫窝', 'https://example.com/images/goods/cat-bed-summer.jpg', 0, 1, NOW(), 0),
(1, 9, '猫爬架大型', 'https://example.com/images/goods/cat-tree-large.jpg', 0, 1, NOW(), 0),
(1, 9, '逗猫棒羽毛款', 'https://example.com/images/goods/cat-teaser-feather.jpg', 0, 1, NOW(), 0),
(1, 9, '狗狗发声玩具球', 'https://example.com/images/goods/dog-toy-ball.jpg', 0, 1, NOW(), 0),
(1, 9, '猫抓板瓦楞纸', 'https://example.com/images/goods/cat-scratcher.jpg', 0, 1, NOW(), 0),

-- 洗护美容服务 (category_id=13)
(1, 13, '狗狗洗澡-小型犬', 'https://example.com/images/goods/dog-bath-small.jpg', 1, 1, NOW(), 0),
(1, 13, '狗狗洗澡-中型犬', 'https://example.com/images/goods/dog-bath-medium.jpg', 1, 1, NOW(), 0),
(1, 13, '狗狗洗澡-大型犬', 'https://example.com/images/goods/dog-bath-large.jpg', 1, 1, NOW(), 0),
(1, 13, '猫咪精洗SPA', 'https://example.com/images/goods/cat-spa.jpg', 1, 1, NOW(), 0),
(1, 13, '狗狗造型修剪', 'https://example.com/images/goods/dog-grooming.jpg', 1, 1, NOW(), 0),
(1, 13, '猫咪剪毛服务', 'https://example.com/images/goods/cat-hair-cut.jpg', 1, 1, NOW(), 0),
(1, 13, '指甲修剪服务', 'https://example.com/images/goods/nail-trim.jpg', 1, 1, NOW(), 0),
(1, 13, '肛门腺清理', 'https://example.com/images/goods/anal-gland.jpg', 1, 1, NOW(), 0),

-- 寄养服务 (category_id=14)
(1, 14, '狗狗寄养-小型犬/天', 'https://example.com/images/goods/dog-boarding-small.jpg', 1, 1, NOW(), 0),
(1, 14, '狗狗寄养-中型犬/天', 'https://example.com/images/goods/dog-boarding-medium.jpg', 1, 1, NOW(), 0),
(1, 14, '狗狗寄养-大型犬/天', 'https://example.com/images/goods/dog-boarding-large.jpg', 1, 1, NOW(), 0),
(1, 14, '猫咪寄养/天', 'https://example.com/images/goods/cat-boarding.jpg', 1, 1, NOW(), 0),

-- 疫苗驱虫服务 (category_id=18)
(1, 18, '狂犬疫苗-狗狗', 'https://example.com/images/goods/vaccine-rabies-dog.jpg', 1, 1, NOW(), 0),
(1, 18, '狂犬疫苗-猫咪', 'https://example.com/images/goods/vaccine-rabies-cat.jpg', 1, 1, NOW(), 0),
(1, 18, '犬八联疫苗', 'https://example.com/images/goods/vaccine-dog-8in1.jpg', 1, 1, NOW(), 0),
(1, 18, '猫三联疫苗', 'https://example.com/images/goods/vaccine-cat-3in1.jpg', 1, 1, NOW(), 0),
(1, 18, '体内驱虫-狗狗', 'https://example.com/images/goods/deworming-dog-internal.jpg', 1, 1, NOW(), 0),
(1, 18, '体外驱虫-狗狗', 'https://example.com/images/goods/deworming-dog-external.jpg', 1, 1, NOW(), 0),
(1, 18, '体内外驱虫-猫咪', 'https://example.com/images/goods/deworming-cat-combo.jpg', 1, 1, NOW(), 0);

-- ============================================
-- 商品 SKU 数据 (goods_sku)
-- ============================================
-- 注意：goods_id 取决于上面插入后生成的自增ID
-- 这里假设 goods 表从 id=1 开始插入

-- 1. 皇家狗粮成犬通用型 (goods_id=1)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 1, '规格', '1.5kg', 68.00, 45.00, 100, 10, 1, NOW(), 0),
(1, 1, '规格', '4kg', 158.00, 105.00, 50, 5, 1, NOW(), 0),
(1, 1, '规格', '10kg', 328.00, 220.00, 30, 3, 1, NOW(), 0);

-- 2. 皇家狗粮幼犬通用型 (goods_id=2)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 2, '规格', '1.5kg', 72.00, 48.00, 100, 10, 1, NOW(), 0),
(1, 2, '规格', '4kg', 168.00, 112.00, 50, 5, 1, NOW(), 0),
(1, 2, '规格', '10kg', 358.00, 240.00, 30, 3, 1, NOW(), 0);

-- 3. 渴望鸡肉猫粮 (goods_id=3)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 3, '规格', '1.8kg', 228.00, 150.00, 40, 5, 1, NOW(), 0),
(1, 3, '规格', '5.4kg', 568.00, 380.00, 20, 2, 1, NOW(), 0);

-- 4. 渴望六种鱼猫粮 (goods_id=4)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 4, '规格', '1.8kg', 248.00, 165.00, 40, 5, 1, NOW(), 0),
(1, 4, '规格', '5.4kg', 598.00, 400.00, 20, 2, 1, NOW(), 0);

-- 5. 巅峰牛肉狗粮 (goods_id=5)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 5, '规格', '1kg', 198.00, 130.00, 35, 5, 1, NOW(), 0),
(1, 5, '规格', '2.5kg', 428.00, 285.00, 15, 2, 1, NOW(), 0);

-- 6. 巅峰羊肉猫粮 (goods_id=6)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 6, '规格', '400g', 98.00, 65.00, 50, 8, 1, NOW(), 0),
(1, 6, '规格', '1kg', 198.00, 130.00, 30, 4, 1, NOW(), 0);

-- 7. 伯纳天纯小型犬成犬粮 (goods_id=7)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 7, '规格', '1.5kg', 58.00, 38.00, 120, 15, 1, NOW(), 0),
(1, 7, '规格', '10kg', 268.00, 178.00, 40, 5, 1, NOW(), 0);

-- 8. 伯纳天纯中大型犬幼犬粮 (goods_id=8)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 8, '规格', '1.5kg', 62.00, 40.00, 100, 12, 1, NOW(), 0),
(1, 8, '规格', '15kg', 368.00, 245.00, 25, 3, 1, NOW(), 0);

-- 9. 卫仕猫粮全价成猫 (goods_id=9)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 9, '规格', '1.8kg', 89.00, 58.00, 80, 10, 1, NOW(), 0),
(1, 9, '规格', '5kg', 198.00, 130.00, 40, 5, 1, NOW(), 0);

-- 10. 卫仕狗粮全价幼犬 (goods_id=10)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 10, '规格', '1.8kg', 95.00, 62.00, 80, 10, 1, NOW(), 0),
(1, 10, '规格', '5kg', 208.00, 138.00, 40, 5, 1, NOW(), 0);

-- 11. 巅峰牛肉狗罐头 (goods_id=11)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 11, '规格', '170g', 28.00, 18.00, 200, 30, 1, NOW(), 0),
(1, 11, '规格', '390g', 48.00, 32.00, 100, 15, 1, NOW(), 0);

-- 12. 巅峰鸡肉猫罐头 (goods_id=12)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 12, '规格', '85g', 18.00, 12.00, 200, 30, 1, NOW(), 0),
(1, 12, '规格', '185g', 32.00, 21.00, 150, 20, 1, NOW(), 0);

-- 13. K9 Natural狗湿粮包 (goods_id=13)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 13, '口味', '牛肉', 25.00, 16.00, 150, 20, 1, NOW(), 0),
(1, 13, '口味', '鸡肉', 25.00, 16.00, 150, 20, 1, NOW(), 0),
(1, 13, '口味', '羊肉', 25.00, 16.00, 120, 15, 1, NOW(), 0);

-- 14. K9 Natural猫湿粮包 (goods_id=14)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 14, '口味', '鸡肉', 22.00, 14.00, 150, 20, 1, NOW(), 0),
(1, 14, '口味', '鱼肉', 22.00, 14.00, 150, 20, 1, NOW(), 0),
(1, 14, '口味', '羊肉', 22.00, 14.00, 120, 15, 1, NOW(), 0);

-- 15. 希尔斯AD处方罐头 (goods_id=15)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 15, '规格', '156g', 35.00, 23.00, 60, 8, 1, NOW(), 0);

-- 16. 希尔斯KD处方罐头 (goods_id=16)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 16, '规格', '156g', 38.00, 25.00, 50, 6, 1, NOW(), 0);

-- 17. 洁齿骨狗狗磨牙棒 (goods_id=17)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 17, '规格', '小型犬', 25.00, 15.00, 100, 15, 1, NOW(), 0),
(1, 17, '规格', '中型犬', 35.00, 22.00, 80, 12, 1, NOW(), 0),
(1, 17, '规格', '大型犬', 45.00, 28.00, 60, 8, 1, NOW(), 0);

-- 18. 鸡胸肉干宠物零食 (goods_id=18)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 18, '规格', '100g', 18.00, 10.00, 200, 30, 1, NOW(), 0),
(1, 18, '规格', '400g', 45.00, 28.00, 100, 15, 1, NOW(), 0);

-- 19. 三文鱼冻干猫零食 (goods_id=19)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 19, '规格', '50g', 35.00, 22.00, 80, 12, 1, NOW(), 0),
(1, 19, '规格', '200g', 98.00, 65.00, 40, 5, 1, NOW(), 0);

-- 20. 蛋黄冻干猫狗通用 (goods_id=20)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 20, '规格', '60g', 28.00, 18.00, 100, 15, 1, NOW(), 0),
(1, 20, '规格', '150g', 58.00, 38.00, 60, 8, 1, NOW(), 0);

-- 21. 猫条流质零食 (goods_id=21)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 21, '口味', '鸡肉', 15.00, 8.00, 200, 30, 1, NOW(), 0),
(1, 21, '口味', '三文鱼', 15.00, 8.00, 200, 30, 1, NOW(), 0),
(1, 21, '口味', '金枪鱼', 15.00, 8.00, 180, 25, 1, NOW(), 0);

-- 22. 狗狗训练零食牛肉粒 (goods_id=22)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 22, '规格', '100g', 12.00, 7.00, 200, 30, 1, NOW(), 0),
(1, 22, '规格', '500g', 38.00, 25.00, 100, 15, 1, NOW(), 0);

-- 23. 猫咪薄荷饼干 (goods_id=23)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 23, '规格', '100g', 15.00, 9.00, 150, 20, 1, NOW(), 0);

-- 24. 鹿角狗狗磨牙玩具 (goods_id=24)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 24, '尺寸', '小号', 35.00, 20.00, 50, 8, 1, NOW(), 0),
(1, 24, '尺寸', '中号', 55.00, 32.00, 40, 6, 1, NOW(), 0),
(1, 24, '尺寸', '大号', 75.00, 45.00, 30, 4, 1, NOW(), 0);

-- 25. 卫仕卵磷脂狗狗美毛 (goods_id=25)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 25, '规格', '300g', 68.00, 45.00, 80, 10, 1, NOW(), 0),
(1, 25, '规格', '500g', 98.00, 65.00, 60, 8, 1, NOW(), 0);

-- 26. 卫仕化毛球片猫用 (goods_id=26)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 26, '规格', '200片', 58.00, 38.00, 100, 12, 1, NOW(), 0);

-- 27. 麦德氏羊奶粉幼犬 (goods_id=27)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 27, '规格', '280g', 68.00, 45.00, 80, 10, 1, NOW(), 0),
(1, 27, '规格', '500g', 108.00, 72.00, 50, 6, 1, NOW(), 0);

-- 28. 麦德氏羊奶粉幼猫 (goods_id=28)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 28, '规格', '200g', 58.00, 38.00, 80, 10, 1, NOW(), 0);

-- 29. 红狗营养膏猫狗通用 (goods_id=29)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 29, '规格', '120g', 78.00, 52.00, 100, 15, 1, NOW(), 0);

-- 30. 红狗化毛膏猫用 (goods_id=30)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 30, '规格', '120g', 78.00, 52.00, 100, 15, 1, NOW(), 0);

-- 31. MAG关节生狗狗鲨鱼软骨素 (goods_id=31)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 31, '规格', '60片', 158.00, 105.00, 50, 6, 1, NOW(), 0);

-- 32. 布拉迪酵母益生菌猫狗 (goods_id=32)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 32, '规格', '14袋', 168.00, 110.00, 40, 5, 1, NOW(), 0);

-- 33. pidan混合猫砂 (goods_id=33)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 33, '规格', '2.4kg', 35.00, 22.00, 100, 15, 1, NOW(), 0),
(1, 33, '规格', '7.2kg', 89.00, 58.00, 60, 8, 1, NOW(), 0);

-- 34. 小佩混合猫砂 (goods_id=34)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 34, '规格', '2.5kg', 32.00, 20.00, 100, 15, 1, NOW(), 0),
(1, 34, '规格', '10kg', 99.00, 65.00, 50, 6, 1, NOW(), 0);

-- 35. 膨润土猫砂10kg (goods_id=35)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 35, '规格', '10kg', 45.00, 28.00, 80, 10, 1, NOW(), 0);

-- 36. 豆腐猫砂绿茶味 (goods_id=36)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 36, '规格', '2.5kg', 28.00, 18.00, 120, 18, 1, NOW(), 0);

-- 37. 水晶猫砂除臭 (goods_id=37)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 37, '规格', '3.8L', 35.00, 22.00, 60, 8, 1, NOW(), 0);

-- 38. 冬季保暖狗窝 (goods_id=38)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 38, '尺寸', 'S-小型', 88.00, 55.00, 30, 5, 1, NOW(), 0),
(1, 38, '尺寸', 'M-中型', 138.00, 88.00, 25, 4, 1, NOW(), 0),
(1, 38, '尺寸', 'L-大型', 188.00, 120.00, 20, 3, 1, NOW(), 0);

-- 39. 夏季凉席猫窝 (goods_id=39)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 39, '尺寸', '小号', 68.00, 42.00, 40, 6, 1, NOW(), 0),
(1, 39, '尺寸', '大号', 98.00, 62.00, 35, 5, 1, NOW(), 0);

-- 40. 猫爬架大型 (goods_id=40)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 40, '款式', '基础款', 268.00, 175.00, 15, 2, 1, NOW(), 0),
(1, 40, '款式', '豪华款', 468.00, 305.00, 10, 1, 1, NOW(), 0);

-- 41. 逗猫棒羽毛款 (goods_id=41)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 41, '颜色', '蓝色', 18.00, 10.00, 100, 15, 1, NOW(), 0),
(1, 41, '颜色', '粉色', 18.00, 10.00, 100, 15, 1, NOW(), 0),
(1, 41, '颜色', '绿色', 18.00, 10.00, 80, 12, 1, NOW(), 0);

-- 42. 狗狗发声玩具球 (goods_id=42)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 42, '尺寸', '小号', 25.00, 15.00, 80, 12, 1, NOW(), 0),
(1, 42, '尺寸', '中号', 35.00, 22.00, 70, 10, 1, NOW(), 0),
(1, 42, '尺寸', '大号', 45.00, 28.00, 50, 8, 1, NOW(), 0);

-- 43. 猫抓板瓦楞纸 (goods_id=43)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 43, '款式', '平板', 25.00, 15.00, 100, 15, 1, NOW(), 0),
(1, 43, '款式', 'L型', 45.00, 28.00, 80, 12, 1, NOW(), 0),
(1, 43, '款式', '猫窝型', 68.00, 42.00, 60, 8, 1, NOW(), 0);

-- 44-51. 洗护美容服务 (goods_id=44-51) - 服务类商品，stock=-1表示不限库存
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 44, '服务', '精洗', 58.00, 35.00, -1, NULL, 1, NOW(), 0),
(1, 44, '服务', 'SPA', 128.00, 78.00, -1, NULL, 1, NOW(), 0),

(1, 45, '服务', '精洗', 78.00, 48.00, -1, NULL, 1, NOW(), 0),
(1, 45, '服务', 'SPA', 168.00, 102.00, -1, NULL, 1, NOW(), 0),

(1, 46, '服务', '精洗', 98.00, 58.00, -1, NULL, 1, NOW(), 0),
(1, 46, '服务', 'SPA', 198.00, 120.00, -1, NULL, 1, NOW(), 0),

(1, 47, '服务', '基础洗', 68.00, 42.00, -1, NULL, 1, NOW(), 0),
(1, 47, '服务', '精洗SPA', 168.00, 102.00, -1, NULL, 1, NOW(), 0),

(1, 48, '服务', '造型', 128.00, 78.00, -1, NULL, 1, NOW(), 0),
(1, 48, '服务', '洗澡+造型', 188.00, 115.00, -1, NULL, 1, NOW(), 0),

(1, 49, '服务', '短毛猫', 88.00, 55.00, -1, NULL, 1, NOW(), 0),
(1, 49, '服务', '长毛猫', 118.00, 72.00, -1, NULL, 1, NOW(), 0),

(1, 50, '服务', '指甲修剪', 20.00, 12.00, -1, NULL, 1, NOW(), 0),

(1, 51, '服务', '肛门腺清理', 30.00, 18.00, -1, NULL, 1, NOW(), 0);

-- 52-55. 寄养服务 (goods_id=52-55)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 52, '服务', '基础寄养', 68.00, 42.00, -1, NULL, 1, NOW(), 0),
(1, 52, '服务', '豪华寄养', 128.00, 78.00, -1, NULL, 1, NOW(), 0),

(1, 53, '服务', '基础寄养', 88.00, 55.00, -1, NULL, 1, NOW(), 0),
(1, 53, '服务', '豪华寄养', 158.00, 98.00, -1, NULL, 1, NOW(), 0),

(1, 54, '服务', '基础寄养', 108.00, 68.00, -1, NULL, 1, NOW(), 0),
(1, 54, '服务', '豪华寄养', 188.00, 118.00, -1, NULL, 1, NOW(), 0),

(1, 55, '服务', '基础寄养', 58.00, 35.00, -1, NULL, 1, NOW(), 0),
(1, 55, '服务', '豪华寄养', 98.00, 62.00, -1, NULL, 1, NOW(), 0);

-- 56-62. 疫苗驱虫服务 (goods_id=56-62)
INSERT INTO goods_sku (tenant_id, goods_id, spec_name, spec_value, price, cost_price, stock, warn_stock, status, create_time, is_deleted) VALUES
(1, 56, '服务', '狂犬疫苗', 80.00, 50.00, -1, NULL, 1, NOW(), 0),

(1, 57, '服务', '狂犬疫苗', 70.00, 42.00, -1, NULL, 1, NOW(), 0),

(1, 58, '服务', '犬八联', 120.00, 75.00, -1, NULL, 1, NOW(), 0),
(1, 58, '服务', '犬六联', 90.00, 55.00, -1, NULL, 1, NOW(), 0),

(1, 59, '服务', '猫三联', 110.00, 68.00, -1, NULL, 1, NOW(), 0),
(1, 59, '服务', '妙三多', 150.00, 92.00, -1, NULL, 1, NOW(), 0),

(1, 60, '服务', '体内驱虫', 50.00, 30.00, -1, NULL, 1, NOW(), 0),
(1, 60, '服务', '体外驱虫', 80.00, 48.00, -1, NULL, 1, NOW(), 0),

(1, 61, '服务', '体内驱虫', 40.00, 25.00, -1, NULL, 1, NOW(), 0),
(1, 61, '服务', '体外驱虫', 70.00, 42.00, -1, NULL, 1, NOW(), 0),

(1, 62, '服务', '体内外同驱', 120.00, 75.00, -1, NULL, 1, NOW(), 0);

-- ============================================
-- 数据统计
-- ============================================
-- 商品总数：约 62 个
-- 商品 SKU 总数：约 150+ 个
-- 覆盖分类：主粮、湿粮、零食、保健品、猫砂、窝垫玩具、洗护美容、寄养、疫苗驱虫
-- ============================================
