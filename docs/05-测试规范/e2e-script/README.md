# 端到端测试方案

## 概述

本方案为宠物门店SaaS项目提供完整的端到端（E2E）测试框架，覆盖PC商家后台和微信小程序C端的核心业务流程。

## 测试框架选型

### PC端测试框架 - Playwright
- **优点**：
  - 支持多浏览器（Chrome、Firefox、Safari）
  - 自动等待机制，减少flaky测试
  - 强大的调试工具
  - 支持Headless和Headful模式
- **配置**：`playwright.config.ts`

### 小程序测试框架 - UniApp Automator
- **优点**：
  - 官方维护，与UniApp深度集成
  - 支持微信开发者工具自动化
  - 提供真实设备测试能力
- **配置**：`uni-automator.config.ts`

## 项目结构

```
docs/05-测试规范/e2e-script/
├── pc/                    # PC端E2E测试
│   ├── playwright.config.ts
│   ├── package.json
│   ├── tests/             # 测试用例
│   │   ├── auth.spec.ts   # 登录认证
│   │   ├── dashboard.spec.ts
│   │   ├── groupActivity.spec.ts  # 拼团活动
│   │   └── rechargeCard.spec.ts   # 储蓄卡次卡
│   └── utils/             # 工具函数
├── miniapp/               # 小程序E2E测试
│   ├── uni-automator.config.ts
│   ├── package.json
│   ├── tests/             # 测试用例
│   │   ├── auth.spec.ts   # 登录认证
│   │   ├── groupActivity.spec.ts  # 拼团活动
│   │   └── rechargeCard.spec.ts   # 储蓄卡次卡
│   └── utils/             # 工具函数
└── README.md
```

## 环境准备

### 1. 安装依赖

```bash
# PC端测试依赖
cd docs/05-测试规范/e2e-script/pc
npm install

# 小程序测试依赖
cd ../miniapp
npm install
```

### 2. 安装Playwright浏览器

```bash
cd ../pc
npx playwright install
```

### 3. 测试环境配置

创建 `.env` 文件：

```env
# 测试环境配置
BASE_URL=http://localhost:3000
API_BASE_URL=http://localhost:8080

# 测试账号
ADMIN_USERNAME=admin
ADMIN_PASSWORD=123456
TEST_PHONE=13800138000
TEST_PASSWORD=123456
```

## 测试执行

### PC端测试

```bash
# 运行所有测试（Headless模式）
cd pc
npm run test

# 运行指定测试文件
npm run test -- groupActivity

# 调试模式（Headful模式）
npm run test:debug

# 生成测试报告
npm run test:report
```

### 小程序测试

```bash
# 运行所有测试
cd miniapp
npm run test

# 运行指定测试文件
npm run test -- groupActivity

# 调试模式
npm run test:debug

# 生成测试报告
npm run test:report
```

## 持续集成

### GitHub Actions 配置

在项目根目录创建 `.github/workflows/e2e-test.yml`：

```yaml
name: E2E Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  pc-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Install Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          
      - name: Install dependencies
        run: cd docs/05-测试规范/e2e-script/pc && npm install
        
      - name: Install Playwright browsers
        run: cd docs/05-测试规范/e2e-script/pc && npx playwright install --with-deps
        
      - name: Run PC tests
        run: cd docs/05-测试规范/e2e-script/pc && npm run test:ci
        
      - name: Upload test report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: pc-test-report
          path: docs/05-测试规范/e2e-script/pc/playwright-report/

  miniapp-test:
    runs-on: windows-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Install Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          
      - name: Install dependencies
        run: cd docs/05-测试规范/e2e-script/miniapp && npm install
        
      - name: Run miniapp tests
        run: cd docs/05-测试规范/e2e-script/miniapp && npm run test:ci
        
      - name: Upload test report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: miniapp-test-report
          path: docs/05-测试规范/e2e-script/miniapp/report/
```

## 测试用例设计

### 核心业务流程

1. **登录认证流程**
2. **拼团活动全流程**（商家端创建 → 小程序端参与 → 支付 → 成团/失败 → 退款）
3. **储蓄卡次卡流程**（购买 → 核销 → 查询）
4. **会员管理流程**（开卡 → 充值 → 查询余额）

### 异常场景

- 登录失败（密码错误、账号锁定）
- 库存不足
- 支付失败
- 网络异常
- 权限不足

## 报告与监控

### 测试报告

- **Playwright报告**：详细的测试执行记录和失败截图
- **Allure报告**：美观的测试报告，支持趋势分析
- **测试覆盖率**：集成测试覆盖率统计

### 缺陷管理

使用GitHub Issues跟踪缺陷：

```markdown
## 缺陷报告模板

### 缺陷标题
[模块名] 功能描述 - 预期结果与实际结果不符

### 缺陷级别
- [ ] 致命 (阻断性错误)
- [ ] 严重 (功能不可用)
- [ ] 一般 (功能缺陷但不阻断)
- [ ] 建议 (优化建议)

### 测试环境
- 环境：开发/测试/生产
- 浏览器：Chrome 120/微信版本 8.0
- 设备：Windows 10/iPhone 14

### 复现步骤
1. 进入页面...
2. 点击按钮...
3. 输入参数...
4. 观察结果...

### 预期结果
描述预期的正确行为

### 实际结果
描述实际发生的问题

### 附件
- 截图
- 录屏
- 日志文件
```

## 维护与更新

1. **定期更新**：根据业务需求变化更新测试用例
2. **回归测试**：每次代码变更后执行相关测试
3. **性能测试**：定期进行性能基准测试
4. **兼容性测试**：新增机型/浏览器版本测试

## 最佳实践

1. **稳定的测试元素**：使用data-testid属性作为测试选择器
2. **测试数据隔离**：每个测试用例使用独立的测试数据
3. **异步处理**：使用明确的等待机制，避免flaky测试
4. **错误处理**：为每个测试用例添加错误截图和日志
5. **持续改进**：定期分析测试执行结果，优化测试策略
