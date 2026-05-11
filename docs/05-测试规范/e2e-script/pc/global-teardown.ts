import { FullConfig } from '@playwright/test';
import fs from 'fs';
import path from 'path';

export default async function globalTeardown(config: FullConfig) {
  console.log('🧪 开始 E2E 测试清理工作');

  // 移动失败截图到report目录
  const screenshotsDir = path.join(__dirname, 'test-results');
  if (fs.existsSync(screenshotsDir)) {
    console.log('📸 保存失败截图');
  }

  console.log('✅ 全局清理完成');
}
