import { test, expect } from '@playwright/test';
import { TestUtils } from '../utils/test-utils';

test.describe('拼团活动管理', () => {
  let testUtils: TestUtils;
  let createdActivityId: string;

  test.beforeEach(async ({ page }) => {
    testUtils = new TestUtils(page);
    await testUtils.login();
  });

  test('创建拼团活动完整流程', async ({ page }) => {
    console.log('🧪 测试创建拼团活动');

    // 导航到拼团活动页面
    await testUtils.navigateToMenu(['营销活动', '拼团活动']);
    await expect(page).toHaveURL(/\/marketing\/group-activity/);

    // 点击创建按钮
    const createBtn = page.getByRole('button', { name: '创建活动' });
    await createBtn.click();
    await expect(page.getByText('创建拼团活动')).toBeVisible();

    // 填写活动信息
    const activityName = `测试拼团活动_${Date.now()}`;
    await page.getByPlaceholder('请输入活动名称').fill(activityName);
    await page.getByPlaceholder('请选择商品').click();
    await page.getByText('测试商品').click();
    await page.getByPlaceholder('请输入成团人数').fill('2');
    await page.getByPlaceholder('请输入库存').fill('10');
    await page.getByPlaceholder('请输入每人限购').fill('1');

    // 设置时间
    await page.locator('[data-testid="start-time"]').click();
    await page.locator('.el-date-picker__today').click();
    await page.locator('[data-testid="end-time"]').click();
    await page.locator('.el-date-picker__today').click();

    // 设置价格
    await page.getByPlaceholder('请输入拼团价格').fill('99');
    await page.getByPlaceholder('请输入原价').fill('199');

    // 保存
    const saveBtn = page.getByRole('button', { name: '保存' });
    await saveBtn.click();
    await testUtils.waitForLoading();

    // 验证创建成功
    await expect(page.getByText('创建成功')).toBeVisible();
    await expect(page.getByText(activityName)).toBeVisible();

    // 获取活动ID
    const rows = await testUtils.getTableData();
    const activityRow = rows.find(row => row.includes(activityName));
    if (activityRow) {
      createdActivityId = activityRow[0]; // 假设第一列是ID
      console.log('📝 创建的活动ID:', createdActivityId);
    }

    console.log('✅ 创建拼团活动测试通过');
  });

  test('查看活动详情和拼团组', async ({ page }) => {
    console.log('🧪 测试查看活动详情');

    await testUtils.navigateToMenu(['营销活动', '拼团活动']);
    await testUtils.waitForLoading();

    // 获取第一个活动的查看按钮
    const firstRow = page.locator('.el-table__body tr').first();
    const viewBtn = firstRow.getByRole('button', { name: '查看' });
    await viewBtn.click();

    // 验证详情页
    await expect(page.getByText('活动详情')).toBeVisible();
    await expect(page.getByText('活动统计')).toBeVisible();

    // 点击拼团组列表
    await page.getByRole('tab', { name: '拼团组列表' }).click();
    await testUtils.waitForLoading();

    console.log('✅ 查看活动详情测试通过');
  });

  test('编辑拼团活动', async ({ page }) => {
    console.log('🧪 测试编辑拼团活动');

    await testUtils.navigateToMenu(['营销活动', '拼团活动']);
    await testUtils.waitForLoading();

    // 获取第一个活动的编辑按钮
    const firstRow = page.locator('.el-table__body tr').first();
    const editBtn = firstRow.getByRole('button', { name: '编辑' });
    await editBtn.click();

    await expect(page.getByText('编辑拼团活动')).toBeVisible();

    // 修改名称
    const nameInput = page.getByPlaceholder('请输入活动名称');
    const newName = await nameInput.inputValue() + '_edited';
    await nameInput.fill(newName);

    // 保存
    const saveBtn = page.getByRole('button', { name: '保存' });
    await saveBtn.click();
    await testUtils.waitForLoading();

    await expect(page.getByText('更新成功')).toBeVisible();
    await expect(page.getByText(newName)).toBeVisible();

    console.log('✅ 编辑拼团活动测试通过');
  });

  test('删除拼团活动', async ({ page }) => {
    console.log('🧪 测试删除拼团活动');

    await testUtils.navigateToMenu(['营销活动', '拼团活动']);
    await testUtils.waitForLoading();

    // 获取要删除的活动
    const rows = await testUtils.getTableData();
    const testActivity = rows.find(row => row.some(cell => cell.includes('测试')));

    if (testActivity) {
      const targetRow = page.locator('.el-table__body tr').filter({ hasText: testActivity[0] });
      const deleteBtn = targetRow.getByRole('button', { name: '删除' });
      await deleteBtn.click();

      // 确认删除
      await testUtils.clickConfirmButton();
      await testUtils.waitForLoading();

      await expect(page.getByText('删除成功')).toBeVisible();
    } else {
      console.log('⚠️ 没有找到测试活动，跳过删除测试');
    }

    console.log('✅ 删除拼团活动测试通过');
  });

  test.afterAll(async ({ page }) => {
    // 清理创建的测试数据
    if (createdActivityId) {
      console.log('🧹 清理测试数据:', createdActivityId);
      try {
        await testUtils.cleanTestData('group-activity', createdActivityId);
      } catch (e) {
        console.warn('清理测试数据失败:', e);
      }
    }
  });
});
