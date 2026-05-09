# 项目概况

> **注意**: 这是 pet-saas-backend 后端工程的项目总览。
>
> **完整设计文档请查看 [docs/](../docs/) 目录**。

---

## 产品定位

宠物门店私域运营一体化 SaaS 后端服务，采用多租户架构，支持平台管理端、门店 PC 端、商家版小程序、C 端小程序四端。

## 快速导航

| 文档 | 路径 | 说明 |
|-----|------|------|
| 产品总览 | [docs/00-总览.md](../docs/00-总览.md) | 完整项目概述 |
| 技术栈 | [docs/02-技术栈与环境.md](../docs/02-技术栈与环境.md) | 技术选型与版本 |
| 功能清单 | [docs/01-功能清单.md](../docs/01-功能清单.md) | P0/P1/P2 功能列表 |
| 核心业务 | [docs/03-核心业务逻辑.md](../docs/03-核心业务逻辑.md) | 业务流程说明 |
| 测试规范 | [docs/09-测试规范.md](../docs/09-测试规范.md) | 测试要求与覆盖 |

## 工程结构

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
│   │       ├── application.yml  # 主配置文件
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       └── application-prod.yml
│   └── test/
├── docs/                         # SPEC 设计文档
├── .claude/                      # Claude 配置（本目录）
└── CLAUDE.md                     # 主索引文件
```
