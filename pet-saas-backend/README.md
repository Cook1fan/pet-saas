# 宠物门店私域运营一体化 SaaS - 后端服务

## 项目简介

专为社区宠物单门店打造的「多租户隔离 + 全端覆盖 + AI 赋能」轻量化私域运营 SaaS 后端服务。

## 技术栈

- **框架**: Spring Boot 3.2.x
- **ORM**: MyBatis-Plus 3.5.x
- **数据库**: MySQL 8.0
- **缓存/分布式锁**: Redis 7.x + Redisson
- **权限认证**: Sa-Token 1.38.0
- **接口文档**: Knife4j
- **测试**: JUnit 5 + Mockito + H2 Database

## 项目结构

```
pet-saas-backend/
├── src/
│   ├── main/
│   │   ├── java/com/pet/saas/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 接口层
│   │   │   ├── service/         # 业务逻辑层
│   │   │   ├── mapper/          # 数据访问层
│   │   │   ├── entity/          # 实体类
│   │   │   ├── dto/             # 请求/响应 DTO
│   │   │   ├── common/          # 通用工具类、常量、枚举
│   │   │   └── handler/         # 全局异常、拦截器
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       └── application-prod.yml
│   └── test/
│       ├── java/com/pet/saas/
│       │   ├── base/            # 测试基类
│       │   ├── unit/            # 单元测试
│       │   └── util/            # 测试工具类
│       └── resources/
│           ├── application-test.yml
│           └── sql/
├── docs/                         # 设计文档
│   ├── 00-总览.md
│   ├── 01-功能清单.md
│   ├── ...
│   └── sql/schema.sql
└── pom.xml
```

## 快速开始

### 1. 数据库初始化

```bash
# 创建数据库
mysql -u root -p

# 执行建表脚本
source docs/sql/schema.sql
```

### 2. 配置修改

修改 `application-dev.yml` 中的数据库和 Redis 连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pet_saas
    username: root
    password: your_password

  data:
    redis:
      host: localhost
      port: 6379
```

### 3. 启动项目

```bash
mvn spring-boot:run
```

### 4. 访问接口文档

启动成功后，访问：http://localhost:8080/doc.html

## 核心功能

### 平台管理端
- 租户创建与管理
- 全平台数据概览

### 门店 PC 管理端
- 登录认证
- 会员与宠物管理
- 储值与次卡管理
- 开单收银
- 库存管理
- 营销活动（拼团/秒杀）
- 对账中心
- AI 运营助手

### 商家版小程序
- 数据概览
- 今日订单
- 次卡核销

### C 端小程序
- 微信登录
- 活动参与
- 个人中心

## 数据库表设计

### 全局表（无 tenant_id）
- `sys_platform_admin` - 平台管理员表
- `sys_tenant` - 租户表
- `sys_ai_package` - AI 增值套餐表
- `sys_config` - 系统配置表

### 业务表（带 tenant_id）
- `shop_admin` - 门店管理员/店员表
- `shop_config` - 门店基础配置表
- `member` - 会员表
- `pet_info` - 宠物档案表
- `recharge_rule` - 储值规则表
- `card_rule` - 次卡规则表
- `member_account` - 会员储值账户表
- `member_card` - 会员次卡表
- `goods` - 商品/服务表
- `stock_record` - 出入库记录表
- `order_info` - 订单表
- `order_item` - 订单明细表
- `activity_info` - 拼团/秒杀活动表
- `activity_order` - 活动订单表
- `flow_record` - 流水记录表
- `ai_call_record` - AI 调用记录表

## 测试

### 运行单元测试

```bash
mvn test
```

### 查看测试覆盖率

```bash
mvn jacoco:report
```

测试报告位置：`target/site/jacoco/index.html`

## 设计文档

详细设计文档请查看 `docs/` 目录：

- [00-总览.md](docs/00-总览.md) - 项目概述与文档导航
- [01-功能清单.md](docs/01-功能清单.md) - 核心功能清单
- [02-技术栈与环境.md](docs/02-技术栈与环境.md) - 技术栈说明
- [03-核心业务逻辑.md](docs/03-核心业务逻辑.md) - 业务流程说明
- [04-接口规范/](docs/04-接口规范/) - 接口文档
- [05-数据库设计/](docs/05-数据库设计/) - 数据库表设计
- [09-测试规范.md](docs/09-测试规范.md) - 测试规范

## License

MIT
