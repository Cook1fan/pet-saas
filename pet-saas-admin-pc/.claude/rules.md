# 前端强制编码规则

## 类型安全

- ✅ **禁止使用 `any` 类型**，必须定义具体类型
- ✅ 所有 API 响应必须定义类型接口
- ✅ 所有 Props 必须定义类型接口

## API 请求

- ✅ **所有 API 请求必须封装在 `/src/api/` 目录下**
- ✅ 页面组件禁止直接调用 axios，必须通过 API 封装层
- ✅ 请求/响应拦截器统一处理 token、错误码

## 组件规范

- ✅ 组件文件使用 PascalCase 命名（`UserProfile.vue`）
- ✅ 组合式函数使用 `use` 前缀（`useUserInfo.ts`）
- ✅ 禁止在组件内写超过 200 行的逻辑，抽取为 composables

## 状态管理

- ✅ 全局状态使用 Pinia store
- ✅ 页面级状态放在组件内
- ✅ 禁止直接修改 store state，必须通过 action

## 样式规范

- ✅ 使用 scoped style 避免样式污染
- ✅ 通用样式抽离到 `styles/` 目录
- ✅ 使用 CSS 变量定义主题色、间距等 Design Token

## 性能优化

- ✅ 列表使用虚拟滚动
- ✅ 图片使用懒加载
- ✅ 大组件使用动态导入 `defineAsyncComponent`
