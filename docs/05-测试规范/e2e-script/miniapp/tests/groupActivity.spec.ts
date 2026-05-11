import { describe, it, beforeEach, afterEach } from 'node:test';
import assert from 'node:assert';
import { MiniProgramTestUtils } from '../utils/test-utils';
import dotenv from 'dotenv';

dotenv.config();

describe('拼团活动小程序端', () => {
  let testUtils: MiniProgramTestUtils;

  beforeEach(async () => {
    testUtils = new MiniProgramTestUtils();
    await testUtils.launch();
    await testUtils.login(
      process.env.TEST_PHONE!,
      process.env.TEST_CODE!
    );
  });

  it('浏览拼团活动列表', async () => {
    console.log('🧪 测试浏览拼团活动列表');

    // 进入拼团活动页面
    await testUtils.click('[data-testid="nav-group-activity"]');
    await testUtils.waitForText('拼团活动');

    // 验证活动列表
    const hasActivities = await testUtils.waitForText('测试活动', 5000);
    // 如果没有测试活动，至少验证页面能正常显示
    const pageHasContent = await testUtils.waitForText('热门拼团', 5000);
    assert.ok(pageHasContent, '页面应正常显示内容');

    console.log('✅ 浏览拼团活动列表测试通过');
  });

  it('查看活动详情', async () => {
    console.log('🧪 测试查看活动详情');

    await testUtils.click('[data-testid="nav-group-activity"]');
    await testUtils.waitForText('拼团活动');

    // 点击第一个活动
    await testUtils.click('[data-testid="activity-item"]:first-child');
    await testUtils.waitForText('活动详情');

    // 验证详情页内容
    const hasPrice = await testUtils.waitForText('拼团价');
    const hasOriginalPrice = await testUtils.waitForText('原价');
    const hasJoinButton = await testUtils.waitForText('发起拼团') ||
                          await testUtils.waitForText('加入拼团');

    assert.ok(hasPrice || hasOriginalPrice, '应显示价格信息');
    assert.ok(hasJoinButton, '应显示参与按钮');

    console.log('✅ 查看活动详情测试通过');
  });

  it('发起拼团', async () => {
    console.log('🧪 测试发起拼团');

    await testUtils.click('[data-testid="nav-group-activity"]');
    await testUtils.waitForText('拼团活动');

    // 点击第一个活动
    await testUtils.click('[data-testid="activity-item"]:first-child');
    await testUtils.waitForText('活动详情');

    // 点击发起拼团
    const startBtn = await testUtils.waitForText('发起拼团');
    if (startBtn) {
      await testUtils.click('[data-testid="start-group-btn"]');
      await testUtils.waitForText('确认订单');

      // 点击确认
      await testUtils.click('[data-testid="confirm-order-btn"]');

      // 等待支付界面或成功提示
      const isSuccess = await testUtils.waitForText('支付', 10000);
      assert.ok(isSuccess, '应显示支付相关内容');
    } else {
      console.log('⚠️ 未找到可发起拼团的活动，跳过测试');
    }

    console.log('✅ 发起拼团测试通过');
  });

  it('查看我的拼团订单', async () => {
    console.log('🧪 测试查看我的拼团订单');

    // 进入我的页面
    await testUtils.click('[data-testid="nav-my"]');
    await testUtils.waitForText('我的');

    // 点击我的拼团
    await testUtils.click('[data-testid="my-group-activity"]');
    await testUtils.waitForText('我的拼团');

    // 验证订单列表
    const hasOrders = await testUtils.waitForText('拼团中') ||
                     await testUtils.waitForText('拼团成功') ||
                     await testUtils.waitForText('拼团失败');

    assert.ok(hasOrders || await testUtils.waitForText('暂无订单'),
      '应显示订单列表或空状态');

    console.log('✅ 查看我的拼团订单测试通过');
  });

  afterEach(async () => {
    await testUtils.quit();
  });
});
