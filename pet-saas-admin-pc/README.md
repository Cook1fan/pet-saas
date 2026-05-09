# pet-saas-admin-pc

宠物门店私域运营一体化 SaaS - PC 管理端

## 技术栈

- Vue 3.4.x + TypeScript 5.x
- Vite 5.x
- Element Plus 2.x
- Pinia 2.x
- Vue Router 4.x
- Axios
- ECharts 5.x

## 项目结构

```
pet-saas-admin-pc/
├── src/
│   ├── api/              # 接口封装
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia 状态管理
│   ├── styles/           # 样式文件
│   ├── utils/            # 工具类
│   ├── views/            # 页面
│   │   ├── platform/     # 平台管理端
│   │   └── shop/         # 门店管理端
│   ├── App.vue
│   └── main.ts
├── docs/                 # SPEC 设计文档
├── .claude/              # Claude Code 快捷参考
├── CLAUDE.md
├── package.json
├── vite.config.ts
└── tsconfig.json
```

## 快速开始

### 安装依赖

```bash
npm install
# 或
pnpm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## 功能模块

### 平台管理端

- 数据看板
- 租户管理
- 系统配置

### 门店管理端

- 首页/数据概览
- 门店配置
- 会员管理
- 储值次卡
- 开单收银
- 订单管理
- 库存管理
- 营销活动
- AI 助手
- 对账中心

## 开发指南

详见 [docs/](docs/) 目录下的 SPEC 设计文档。
