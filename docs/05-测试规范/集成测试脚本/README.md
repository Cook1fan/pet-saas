# 集成测试方案

## 概述

集成测试验证多组件、多服务之间的交互是否正常，覆盖API接口、数据库、缓存等。

## 测试框架选择

### 后端集成测试 - JUnit 5 + Spring Boot Test
- **优点**：
  - Spring官方支持，集成良好
  - 支持MockMvc测试API
  - 支持@DataJpaTest测试数据库层
  - 测试容器支持真实数据库测试

### 接口测试 - Jest + Supertest
- **优点**：
  - 简单易用
  - 支持异步测试
  - 丰富的断言库

## 测试项目结构

```
pet-saas-backend/src/test/java/com/pet/saas/
├── unit/                      # 单元测试
│   ├── service/
│   ├── mapper/
│   └── utils/
├── integration/               # 集成测试
│   ├── api/                   # API接口测试
│   │   ├── GroupActivityApiTest.java
│   │   ├── RechargeCardApiTest.java
│   │   └── AuthApiTest.java
│   ├── repository/            # 数据库测试
│   ├── cache/                 # Redis缓存测试
│   └── workflow/              # 业务流程测试
└── e2e/                       # 端到端测试
```

## 测试用例设计

### 1. API接口测试

#### 拼团活动API测试
- 创建活动
- 查询活动列表
- 查询活动详情
- 编辑活动
- 删除活动
- 发起拼团
- 加入拼团
- 支付回调

#### 储蓄卡次卡API测试
- 创建储蓄卡
- 创建次卡
- 购买卡
- 核销卡
- 查询卡余额
- 退款流程

### 2. 数据库集成测试

#### 事务处理测试
- 验证事务回滚
- 验证事务提交
- 验证分布式事务

#### 数据一致性测试
- Redis与数据库一致性
- 多表数据一致性

### 3. 缓存测试

#### Redis缓存测试
- 缓存命中
- 缓存失效
- 缓存更新
- 分布式锁

## 执行策略

```bash
# 运行所有测试
mvn test

# 只运行集成测试
mvn test -Dtest=**/integration/**/*Test.java

# 生成测试覆盖率报告
mvn clean test jacoco:report
```

## 持续集成配置

```yaml
name: Integration Tests

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

jobs:
  integration-test:
    runs-on: ubuntu-latest
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: pet_saas_test
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3
      redis:
        image: redis:7
        ports:
          - 6379:6379

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven

      - name: Run integration tests
        run: cd pet-saas-backend && mvn -Dtest=**/integration/**/*Test.java test

      - name: Upload coverage
        uses: codecov/codecov-action@v4
        with:
          files: pet-saas-backend/target/site/jacoco/jacoco.xml
```
