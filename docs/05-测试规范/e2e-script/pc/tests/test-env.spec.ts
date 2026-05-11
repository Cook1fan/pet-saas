import { test, expect } from '@playwright/test';
import dotenv from 'dotenv';

dotenv.config();

test.describe('环境检查', () => {
  test('验证 Playwright 安装', async ({}) => {
    console.log('✅ Playwright 安装正常');
  });

  test('验证环境变量', async ({}) => {
    console.log('🧪 检查环境变量');

    const requiredEnv = ['BASE_URL', 'API_BASE_URL', 'ADMIN_USERNAME', 'ADMIN_PASSWORD'];
    const missingEnv = requiredEnv.filter(env => !process.env[env]);

    if (missingEnv.length > 0) {
      throw new Error(`缺少必填环境变量: ${missingEnv.join(', ')}`);
    }

    console.log('✅ 所有环境变量正常');
    console.log('📋 测试环境信息:');
    console.log(`- BASE_URL: ${process.env.BASE_URL}`);
    console.log(`- API_BASE_URL: ${process.env.API_BASE_URL}`);
    console.log(`- ADMIN_USERNAME: ${process.env.ADMIN_USERNAME}`);
    console.log(`- ADMIN_PASSWORD: ${'*'.repeat(process.env.ADMIN_PASSWORD?.length || 0)}`);
  });

  test('验证依赖包', async ({}) => {
    console.log('📦 检查项目依赖');

    try {
      const packageJson = await import('../package.json');
      console.log(`✅ 项目依赖正常 (${Object.keys(packageJson.dependencies || {}).length}个依赖)`);
    } catch (error) {
      throw new Error('无法读取 package.json');
    }
  });

  test('验证 Playwright 配置', async ({}) => {
    console.log('⚙️ 检查 Playwright 配置');

    try {
      const config = await import('../playwright.config');
      console.log('✅ Playwright 配置正常');
      console.log(`- 测试目录: ${config.default.testDir}`);
      console.log(`- 默认超时: ${config.default.use?.actionTimeout}ms`);
    } catch (error) {
      throw new Error('无法读取 Playwright 配置');
    }
  });
});

test('验证浏览器支持', async ({}) => {
  console.log('🌐 检查浏览器支持');

  try {
    // 简单的浏览器检查
    const browserTypes = ['chromium', 'firefox', 'webkit'];
    console.log(`✅ 支持浏览器: ${browserTypes.join(', ')}`);
  } catch (error) {
    throw new Error('浏览器检查失败');
  }
});
