import { Page } from '@playwright/test';
import dotenv from 'dotenv';

dotenv.config();

export class TestUtils {
  constructor(private page: Page) {}

  /**
   * 登录商家后台
   */
  async login(username?: string, password?: string) {
    const user = username || process.env.ADMIN_USERNAME!;
    const pass = password || process.env.ADMIN_PASSWORD!;

    await this.page.goto('/');
    await this.page.waitForURL(/\/login/);

    await this.page.getByPlaceholder('请输入账号').fill(user);
    await this.page.getByPlaceholder('请输入密码').fill(pass);
    await this.page.getByRole('button', { name: '登录' }).click();

    await this.page.waitForURL(/\/dashboard/);
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 导航到指定菜单
   */
  async navigateToMenu(menuPath: string[]) {
    // 展开一级菜单
    for (let i = 0; i < menuPath.length - 1; i++) {
      const menu = this.page.getByRole('menuitem', { name: menuPath[i] });
      if (await menu.isVisible()) {
        await menu.click();
        await this.page.waitForTimeout(500);
      }
    }

    // 点击目标菜单
    const targetMenu = this.page.getByRole('menuitem', { name: menuPath[menuPath.length - 1] });
    await targetMenu.click();
    await this.page.waitForLoadState('networkidle');
  }

  /**
   * 创建测试数据
   */
  async createTestData(type: string, data: any) {
    const response = await fetch(`${process.env.API_BASE_URL}/api/test-data/${type}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    });

    if (!response.ok) {
      throw new Error(`创建测试数据失败: ${response.statusText}`);
    }

    return await response.json();
  }

  /**
   * 清理测试数据
   */
  async cleanTestData(type: string, id: string) {
    const response = await fetch(`${process.env.API_BASE_URL}/api/test-data/${type}/${id}`, {
      method: 'DELETE',
    });

    if (!response.ok) {
      console.warn(`清理测试数据失败: ${response.statusText}`);
    }
  }

  /**
   * 等待加载状态
   */
  async waitForLoading() {
    const loading = this.page.locator('.el-loading-mask');
    if (await loading.isVisible()) {
      await loading.waitFor({ state: 'hidden', timeout: 10000 });
    }
  }

  /**
   * 等待消息提示
   */
  async waitForMessage() {
    const message = this.page.locator('.el-message');
    if (await message.isVisible()) {
      await message.waitFor({ state: 'hidden', timeout: 3000 });
    }
  }

  /**
   * 获取表格数据
   */
  async getTableData(): Promise<any[]> {
    const rows = this.page.locator('.el-table__body tr');
    const count = await rows.count();

    const data = [];
    for (let i = 0; i < count; i++) {
      const cells = rows.nth(i).locator('td');
      const cellCount = await cells.count();
      const rowData = [];

      for (let j = 0; j < cellCount; j++) {
        rowData.push(await cells.nth(j).innerText());
      }

      if (rowData.length > 0) {
        data.push(rowData);
      }
    }

    return data;
  }

  /**
   * 点击确认按钮
   */
  async clickConfirmButton() {
    const confirmBtn = this.page.getByRole('button', { name: '确定' });
    if (await confirmBtn.isVisible()) {
      await confirmBtn.click();
    }
  }

  /**
   * 上传文件
   */
  async uploadFile(selector: string, filePath: string) {
    const fileInput = this.page.locator(selector);
    await fileInput.setInputFiles(filePath);
    await this.waitForLoading();
  }
}
