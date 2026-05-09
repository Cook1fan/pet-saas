# Pet SaaS Admin PC - 项目概况

> 📌 **重要说明**：本文档是快捷参考，**权威来源**是 `docs/` 目录下的 SPEC 文档。

## 项目简介

pet-saas-admin-pc 是宠物门店私域运营一体化 SaaS 系统的 PC 管理端前端，包含平台管理端和门店管理端两套界面。

## 技术栈

- **框架**: Vue 3.4.x + TypeScript
- **UI 组件**: Element Plus
- **构建工具**: Vite
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP**: Axios
- **图表**: ECharts

## 目录结构

```
src/
├── api/           # 接口封装
├── assets/        # 静态资源
├── components/    # 公共组件
├── router/        # 路由配置
├── stores/        # Pinia 状态
├── utils/         # 工具类
├── views/         # 页面
│   ├── platform/ # 平台端
│   └── shop/     # 门店端
└── App.vue
```

## 开发规范

详见 [.claude/guides/development.md](guides/development.md)

## 快捷链接

| 文档 | 说明 |
|-----|------|
| [docs/00-总览.md](../docs/00-总览.md) | 项目总览 |
| [docs/01-功能清单.md](../docs/01-功能清单.md) | 功能清单 |
| [docs/03-页面结构与路由.md](../docs/03-页面结构与路由.md) | 页面结构 |
| [docs/04-接口对接规范/](../docs/04-接口对接规范/) | 接口文档 |
