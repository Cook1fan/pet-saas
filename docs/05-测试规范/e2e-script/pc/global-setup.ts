import { FullConfig } from '@playwright/test';
import dotenv from 'dotenv';
import fs from 'fs';
import path from 'path';

dotenv.config();

export default async function globalSetup(config: FullConfig) {
  console.log('🧪 开始 E2E 测试全局准备');

  // 检查环境变量
  const requiredEnv = ['BASE_URL', 'API_BASE_URL', 'ADMIN_USERNAME', 'ADMIN_PASSWORD'];
  requiredEnv.forEach(env => {
    if (!process.env[env]) {
      throw new Error(`❌ 缺少必填环境变量: ${env}`);
    }
  });

  // 检查测试数据目录
  const testDataDir = path.join(__dirname, 'test-data');
  if (!fs.existsSync(testDataDir)) {
    fs.mkdirSync(testDataDir, { recursive: true });
  }

  console.log('✅ 全局准备完成');
}
