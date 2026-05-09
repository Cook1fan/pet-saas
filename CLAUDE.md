# Pet SaaS 项目 - Claude 配置

## 项目专属 Agent

使用 `Agent` 工具调用以下项目专属角色：

| Agent | 用途 |
|-------|------|
| **pet-saas-architect** | 架构师 - 技术方案、数据库设计、API契约评审 |
| **pet-saas-backend** | 后端工程师 - Java/SpringBoot 开发 |
| **pet-saas-frontend** | 前端工程师 - Vue 3 管理后台开发 |
| **pet-saas-miniapp** | 小程序工程师 - UniApp 微信小程序开发 |
| **pet-saas-tester** | 测试工程师 - 测试验收、缺陷闭环 |

## 调用示例

```typescript
// 召唤架构师
Agent({
  name: "pet-saas-architect",
  description: "设计新模块",
  prompt: "帮我设计活动营销模块的技术方案..."
});

// 召唤后端开发
Agent({
  name: "pet-saas-backend",
  description: "开发后端 API",
  prompt: "根据 API 契约实现活动管理接口..."
});

// 召唤测试验收
Agent({
  name: "pet-saas-tester",
  description: "测试验收",
  prompt: "对活动管理模块进行集成测试和 E2E 测试..."
});
```

## 核心铁律（所有 Agent 必须遵守）

### 1. 方案先行原则
- **任何代码编写前，必须确保对应文档已更新并达成共识**
- 先写设计文档，再写代码
- 文档是唯一可信源，禁止仅凭记忆编码

### 2. API 优先原则
- 所有接口必须先在 `docs/01-API契约/` 中定义 YAML/OpenAPI 文档
- 契约未定义前，禁止写 Controller 或页面调用
- 全端枚举、DTO 结构必须以 `docs/01-API契约/types.ts` 为唯一基准

### 3. 分层测试责任分明
- **单元测试**：开发自治（后端/前端/小程序各自负责）
- **集成测试**：@Tester 主导，@Backend 配合
- **E2E 测试**：@Tester 全权负责

## 文档索引（Source of Truth）

| 文档类型 | 路径 |
|---------|------|
| 产品需求 (PRD) | docs/00-总览/宠物门店私域运营一体化 SaaS MVP 最终需求文档（PRD）.md |
| 全局技术方案 | docs/00-总览/宠物门店私域运营一体化 SaaS MVP 最终技术方案.md |
| API 契约 & 类型 | docs/01-API契约/ |
| 数据库设计 | docs/02-数据库设计/ |
| 开发指南 | docs/03-开发指南/ |
| 模块设计 | docs/04-模块设计/ |
| 测试规范&用例 | docs/05-测试规范/ |

## 冲突解决机制

当出现需求不清、技术方案分歧、测试标准不统一时：
1. 查阅 `docs/00-总览/` 下的 PRD 和技术方案
2. 查阅对应模块设计、测试规范
3. 如果文档未覆盖，由 **pet-saas-architect** 主导更新文档并记录决策，同步 @Tester 对齐测试标准
4. 文档更新后，各端、测试严格按照新文档执行

**禁止**：在未更新文档的情况下，各端私自妥协开发临时方案、测试私自放宽验收标准。
