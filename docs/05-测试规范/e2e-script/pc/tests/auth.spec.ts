import { test, expect } from '@playwright/test';
import { TestUtils } from '../utils/test-utils';

test.describe('登录认证', () => {
  let testUtils: TestUtils;

  test.beforeEach(async ({ page }) => {
    testUtils = new TestUtils(page);
  });

  test('正常登录流程', async ({ page }) => {
    console.log('🧪 测试正常登录流程');

    await testUtils.login();

    // 验证跳转成功
    await expect(page).toHaveURL(/\/dashboard/);
    await expect(page.getByText('控制台')).toBeVisible();
    await expect(page.getByText('欢迎使用')).toBeVisible();

    console.log('✅ 正常登录流程测试通过');
  });

  test('账号密码错误', async ({ page }) => {
    console.log('🧪 测试账号密码错误');

    await testUtils.login('invalid', 'wrong');

    // 验证错误提示
    await expect(page.getByText('账号或密码错误')).toBeVisible();
    await expect(page).not.toHaveURL(/\/dashboard/);

    console.log('✅ 账号密码错误测试通过');
  });

  test('账号为空', async ({ page }) => {
    console.log('🧪 测试账号为空');

    await testUtils.login('', '123456');

    await expect(page.getByText('请输入账号')).toBeVisible();
    await expect(page).not.toHaveURL(/\/dashboard/);

    console.log('✅ 账号为空测试通过');
  });

  test('密码为空', async ({ page }) => {
    console.log('🧪 测试密码为空');

    await testUtils.login('admin', '');

    await expect(page.getByText('请输入密码')).toBeVisible();
    await expect(page).not.toHaveURL(/\/dashboard/);

    console.log('✅ 密码为空测试通过');
  });

  test('退出登录', async ({ page }) => {
    console.log('🧪 测试退出登录');

    await testUtils.login();

    // 点击头像或用户菜单
    const userMenu = page.getByRole('button', { name: /admin/ });
    await userMenu.click();

    const logoutBtn = page.getByRole('menuitem', { name: '退出登录' });
    await logoutBtn.click();

    // 确认
    await testUtils.clickConfirmButton();

    await page.waitForURL(/\/login/);
    await expect(page).toHaveURL(/\/login/);

    console.log('✅ 退出登录测试通过');
  });
});
