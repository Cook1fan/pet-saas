# 宠物门店SaaS - 小程序端

## 项目介绍

宠物门店私域运营一体化 SaaS - 微信小程序端，基于 UniApp + Vue3 + TypeScript 开发。

## 技术栈

- UniApp 3.x
- Vue 3.x
- TypeScript
- Pinia
- Vite
- SCSS

## 项目结构

```
pet-saas-miniapp/
├── src/
│   ├── api/              # API 接口
│   │   └── group-activity.ts  # 拼团活动接口
│   ├── components/       # 公共组件
│   │   ├── countdown.vue       # 倒计时组件
│   │   ├── group-progress.vue  # 拼团进度组件
│   │   └── joinable-group-card.vue  # 可加入拼团卡片组件
│   ├── pages/            # 页面
│   │   ├── index/        # 首页
│   │   ├── user/         # 个人中心
│   │   └── group-activity/  # 拼团活动
│   │       ├── list.vue        # 拼团活动列表
│   │       ├── detail.vue      # 拼团活动详情
│   │       ├── group-detail.vue  # 拼团组详情（分享页）
│   │       ├── confirm.vue     # 订单确认
│   │       ├── success.vue     # 拼团成功
│   │       └── my-orders.vue   # 我的拼团订单
│   ├── static/           # 静态资源
│   │   └── images/       # 图片资源
│   ├── store/            # Pinia 状态管理
│   ├── utils/            # 工具函数
│   │   └── request.ts    # 请求封装
│   ├── App.vue           # 应用入口组件
│   ├── main.ts           # 应用入口文件
│   ├── pages.json        # 页面配置
│   └── manifest.json     # 应用配置
├── package.json
├── vite.config.ts
├── tsconfig.json
└── README.md
```

## 功能模块

### 拼团活动

1. **拼团活动列表** - 展示门店所有进行中的拼团活动
2. **拼团活动详情** - 展示活动详情、可加入的拼团
3. **拼团组详情** - 分享页面，展示拼团进度
4. **订单确认** - 确认订单、发起/加入拼团、微信支付
5. **拼团成功** - 支付成功后展示结果
6. **我的拼团** - 查看个人拼团订单列表

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
# 微信小程序
npm run dev:mp-weixin

# H5
npm run dev:h5
```

### 生产构建

```bash
# 微信小程序
npm run build:mp-weixin

# H5
npm run build:h5
```

## 开发说明

### API 接口配置

在 `src/utils/request.ts` 中配置 API 基础地址：

```typescript
const BASE_URL = 'http://localhost:8080'
```

### 页面路由配置

页面路由配置在 `src/pages.json` 中。

### 微信小程序开发

1. 执行 `npm run dev:mp-weixin` 生成小程序代码
2. 打开微信开发者工具
3. 导入项目，选择 `dist/dev/mp-weixin` 目录
4. 配置小程序 AppID（需要在 `src/manifest.json` 中配置）
5. 开始开发调试

## 注意事项

1. 图片资源需要自行添加到 `src/static/images/` 目录
2. 需要在微信公众平台配置服务器域名白名单
3. 支付功能需要配置微信商户号和支付参数
4. 多租户功能由后端处理，前端只需要携带 tenant_id

## 相关文档

- [UniApp 官方文档](https://uniapp.dcloud.net.cn/)
- [Vue 3 官方文档](https://cn.vuejs.org/)
- [微信小程序官方文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)
