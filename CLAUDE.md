# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## 项目概览

宠物门店私域运营一体化 SaaS，包含三个端：
- **pet-saas-backend**: Java/Spring Boot 后端
- **pet-saas-admin-pc**: Vue 3 + Element Plus PC 管理后台
- **pet-saas-miniapp**: UniApp 微信小程序

## 常用命令

### pet-saas-backend (Java/Spring Boot)
```bash
# 进入后端目录
cd pet-saas-backend

# 编译项目
mvn clean compile

# 运行单元测试
mvn test

# 运行单个测试类
mvn test -Dtest=ClassNameTest

# 运行单个测试方法
mvn test -Dtest=ClassNameTest#methodName

# 查看测试覆盖率
mvn jacoco:report

# 启动开发服务器
mvn spring-boot:run

# 打包
mvn clean package
```

### pet-saas-admin-pc (Vue 3/Vite)
```bash
# 进入前端目录
cd pet-saas-admin-pc

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

### pet-saas-miniapp (UniApp)
```bash
# 进入小程序目录
cd pet-saas-miniapp

# 安装依赖
npm install

# 微信小程序开发模式
npm run dev:mp-weixin

# 微信小程序生产构建
npm run build:mp-weixin

# H5 开发模式
npm run dev:h5

# H5 生产构建
npm run build:h5
```
构建产物位于 `dist/dev/mp-weixin/`，可在微信开发者工具中导入此目录。

## 项目架构

### 整体架构
```
pet-saas/
├── docs/                          # 统一文档中心（SSOT）
│   ├── 00-总览/                   # PRD 和技术方案
│   ├── 01-API契约/                # API 契约和共用类型
│   ├── 02-数据库设计/             # 数据库表设计
│   ├── 03-开发指南/               # 开发流程指南
│   ├── 04-模块设计/               # 各功能模块详细设计
│   └── 05-测试规范/               # 测试用例和规范
├── pet-saas-backend/              # Java/Spring Boot 后端
│   ├── src/main/java/com/pet/saas/
│   │   ├── config/                # 配置类
│   │   ├── controller/            # 接口层
│   │   ├── service/               # 业务逻辑层
│   │   ├── mapper/                # 数据访问层
│   │   ├── entity/                # 实体类
│   │   ├── dto/                   # 请求/响应 DTO
│   │   └── common/                # 通用工具类
│   └── src/test/                  # 测试代码
├── pet-saas-admin-pc/             # Vue 3 管理后台
│   └── src/
│       ├── api/                   # API 封装
│       ├── components/            # 公共组件
│       ├── views/                 # 页面组件
│       ├── stores/                # Pinia 状态管理
│       └── router/                # 路由配置
└── pet-saas-miniapp/              # UniApp 小程序
    └── src/
        ├── api/                   # API 封装
        ├── components/            # 公共组件
        ├── pages/                 # 页面
        ├── stores/                # Pinia 状态管理
        └── utils/                 # 工具函数
```

### 多租户架构
- 基于 `tenant_id` 实现租户隔离
- 全局表（无 tenant_id）：`sys_platform_admin`, `sys_tenant`, `sys_ai_package`, `sys_config`
- 业务表（带 tenant_id）：所有业务表

### 技术栈
- **后端**: Spring Boot 3.5.12 + MyBatis-Plus 3.5.5 + Sa-Token + MySQL 8.0 + Redis 7.x
- **前端**: Vue 3.4 + TypeScript 5.3 + Vite 5.0 + Element Plus 2.4 + Pinia 2.1
- **小程序**: UniApp (Vue 3) + TypeScript

## 开发工作流

### 1. 统一规划阶段（在项目根目录操作）
1. 阅读 `docs/00-总览/` 下的 PRD 和技术方案
2. 在 `docs/01-API契约/` 定义接口和 `types.ts`
3. 使用 pet-saas-architect 进行技术方案设计

### 2. 并行开发阶段
- **后端**: 在 `pet-saas-backend/` 目录操作，基于 API 契约实现
- **前端**: 在 `pet-saas-admin-pc/` 目录操作，基于 API 契约用 Mock 开发
- **小程序**: 在 `pet-saas-miniapp/` 目录操作

### 3. 联调对接阶段
- 后端提供 Knife4j 文档 (http://localhost:8080/doc.html)
- 前端替换 Mock 对接真实接口
- 全链路测试验证

## 核心铁律

### 1. 方案先行原则
- **任何代码编写前，必须确保对应文档已更新并达成共识**
- 先写设计文档，再写代码
- 文档是唯一可信源，禁止仅凭记忆编码

### 2. API 优先原则
- 所有接口必须先在 `docs/01-API契约/` 中定义
- 契约未定义前，禁止写 Controller 或页面调用
- 全端枚举、DTO 结构必须以 `docs/01-API契约/types.ts` 为唯一基准

### 3. 分层测试责任分明
- **单元测试**: 开发自治（后端/前端/小程序各自负责）
- **集成测试**: @Tester 主导，@Backend 配合
- **E2E 测试**: @Tester 全权负责

## 文档索引

| 文档类型 | 路径 |
|---------|------|
| 产品需求 (PRD) | docs/00-总览/ |
| 全局技术方案 | docs/00-总览/ |
| API 契约 & 类型 | docs/01-API契约/ |
| 数据库设计 | docs/02-数据库设计/ |
| 开发指南 | docs/03-开发指南/ |
| 模块设计 | docs/04-模块设计/ |
| 测试规范&用例 | docs/05-测试规范/ |

## 项目专属 Agent

使用 `Agent` 工具调用以下项目专属角色：

| Agent | 用途 |
|-------|------|
| **pet-saas-architect** | 架构师 - 技术方案、数据库设计、API契约评审 |
| **pet-saas-backend** | 后端工程师 - Java/SpringBoot 开发 |
| **pet-saas-frontend** | 前端工程师 - Vue 3 管理后台开发 |
| **pet-saas-miniapp** | 小程序工程师 - UniApp 微信小程序开发 |
| **pet-saas-tester** | 测试工程师 - 测试验收、缺陷闭环 |

## 冲突解决机制

当出现需求不清、技术方案分歧、测试标准不统一时：
1. 查阅 `docs/00-总览/` 下的 PRD 和技术方案
2. 查阅对应模块设计、测试规范
3. 如果文档未覆盖，由 **pet-saas-architect** 主导更新文档并记录决策，同步 @Tester 对齐测试标准
4. 文档更新后，各端、测试严格按照新文档执行

**禁止**：在未更新文档的情况下，各端私自妥协开发临时方案、测试私自放宽验收标准。
