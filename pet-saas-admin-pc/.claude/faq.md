# 常见问题

## Q: 如何添加新页面？

A:
1. 在 `src/views/` 下创建页面文件
2. 在 `src/router/index.ts` 中添加路由
3. 在对应端的菜单中添加入口

## Q: 接口请求如何获取 tenant_id？

A: 不需要手动传，后端通过 Sa-Token Session 自动获取当前登录用户的 tenant_id。

## Q: 如何处理权限控制？

A:
- 路由层面：路由守卫 `requiresAuth`
- 按钮层面：`v-if="userStore.isShopAdmin`
- 后端层面：`@SaCheckRole` 注解

## Q: 表单验证怎么写？

A: 使用 Element Plus Form 组件的 rules 配置，参考 Element Plus 文档。
