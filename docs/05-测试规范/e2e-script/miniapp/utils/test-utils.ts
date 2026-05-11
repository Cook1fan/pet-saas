import miniprogram from 'miniprogram-automator';
import dotenv from 'dotenv';
import axios from 'axios';

dotenv.config();

export class MiniProgramTestUtils {
  private miniProgram: any;
  private page: any;

  async launch() {
    console.log('🚀 启动微信小程序');

    this.miniProgram = await miniprogram.launch({
      projectPath: process.env.MINIPROGRAM_PROJECT_PATH,
      cliPath: process.env.WECHAT_DEVTOOLS_PATH,
    });

    return this.miniProgram;
  }

  async getCurrentPage() {
    const pages = await this.miniProgram.page();
    this.page = pages[0];
    return this.page;
  }

  async goToPage(pagePath: string, params: any = {}) {
    const query = new URLSearchParams(params).toString();
    await this.miniProgram.navigateTo({
      url: pagePath + (query ? `?${query}` : ''),
    });
  }

  async waitForSelector(selector: string, timeout = 5000) {
    const startTime = Date.now();
    while (Date.now() - startTime < timeout) {
      try {
        const element = await this.page.$(selector);
        if (element) return element;
      } catch (e) {
        // 忽略错误
      }
      await new Promise(resolve => setTimeout(resolve, 500));
    }
    throw new Error(`超时等待元素: ${selector}`);
  }

  async click(selector: string) {
    const element = await this.waitForSelector(selector);
    await element.tap();
    await this.waitForTimeout(1000);
  }

  async input(selector: string, text: string) {
    const element = await this.waitForSelector(selector);
    await element.input(text);
  }

  async getText(selector: string) {
    const element = await this.waitForSelector(selector);
    return element.text();
  }

  async waitForText(text: string, timeout = 5000) {
    const startTime = Date.now();
    while (Date.now() - startTime < timeout) {
      try {
        const pages = await this.miniProgram.page();
        const currentPage = pages[0];
        const pageSource = await currentPage.source();
        if (pageSource.includes(text)) {
          return true;
        }
      } catch (e) {
        // 忽略错误
      }
      await this.waitForTimeout(500);
    }
    return false;
  }

  async waitForTimeout(ms: number) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  async login(phone: string, code: string) {
    console.log('🔐 小程序登录');

    await this.goToPage('/pages/login');
    await this.waitForText('登录');

    await this.input('[data-testid="phone"]', phone);
    await this.input('[data-testid="code"]', code);
    await this.click('[data-testid="login-btn"]');

    await this.waitForText('我的', 10000);
  }

  async getStorage(key: string) {
    return this.miniProgram.storageGet(key);
  }

  async setStorage(key: string, value: any) {
    await this.miniProgram.storageSet({
      key,
      data: value,
    });
  }

  async clearStorage() {
    await this.miniProgram.clearStorage();
  }

  async screenshot(filePath: string) {
    const page = await this.getCurrentPage();
    await page.screenshot({ path: filePath });
  }

  async requestApi(url: string, options: any = {}) {
    const apiUrl = process.env.API_BASE_URL + url;
    try {
      const response = await axios({
        url: apiUrl,
        method: options.method || 'GET',
        data: options.data,
        params: options.params,
      });
      return response.data;
    } catch (error: any) {
      console.error('API请求失败:', error.response?.data || error.message);
      throw error;
    }
  }

  async quit() {
    await this.miniProgram.close();
  }
}
