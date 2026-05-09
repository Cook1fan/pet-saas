# 拼团模块 - 快速开始指南

## 🚀 快速启动

### 前置条件
- Node.js 16+
- npm 7+

### 1. 安装依赖

```bash
cd pet-saas-miniapp
npm install
```

### 2. 启动开发服务器

```bash
npm run dev:h5
```

### 3. 访问应用

打开浏览器访问: http://localhost:3000 (或控制台显示的其他端口)

---

## 📱 功能演示

### 演示流程 1 - 发起拼团

1. 打开首页
2. 点击底部导航"拼团"进入活动列表
3. 选择一个活动，点击进入活动详情
4. 点击"发起拼团"按钮
5. 在订单确认页，可以选择数量
6. 点击"立即支付"
7. 在弹出的确认框中，点击"取消"
8. 然后在提示框中选择"确定"使用测试模式
9. 进入拼团成功页
10. 点击"查看订单"查看刚才的订单

### 演示流程 2 - 加入拼团

1. 在拼团活动列表中，找到有"可加入"团的活动
2. 点击活动卡片中的"去参团"按钮
3. 确认订单信息
4. 点击"立即支付"
5. 使用测试模式完成支付

### 演示流程 3 - 我的订单

1. 点击底部导航"我的"进入个人中心
2. 点击"我的拼团"进入订单列表
3. 查看不同状态的订单
4. 点击"邀请好友"测试分享功能

---

## 📦 项目说明

### 文件位置

**小程序项目**: `pet-saas-miniapp/`

**主要文件**:
- 活动列表: `src/pages/group-activity/list.vue`
- 活动详情: `src/pages/group-activity/detail.vue`
- 拼团详情: `src/pages/group-activity/group-detail.vue`
- 订单确认: `src/pages/group-activity/confirm.vue`
- 成功页: `src/pages/group-activity/success.vue`
- 我的订单: `src/pages/group-activity/my-orders.vue`
- API 接口: `src/api/group-activity.ts`

### 配置修改

**API 地址**: `src/utils/request.ts`

```typescript
const BASE_URL = 'http://localhost:8080'  // 修改为实际后端地址
```

**小程序 AppID**: `src/manifest.json`

```json
{
  "mp-weixin": {
    "appid": "你的微信小程序AppID"
  }
}
```

---

## 🔧 开发模式

### H5 开发
```bash
npm run dev:h5
```

### 微信小程序开发
```bash
npm run dev:mp-weixin
```
然后用微信开发者工具打开 `dist/dev/mp-weixin`

### 构建生产版本

```bash
# H5
npm run build:h5

# 微信小程序
npm run build:mp-weixin
```

---

## 📝 测试说明

### 测试模式

项目已内置模拟数据，无需后端也可以测试完整流程：

1. API 请求会优先尝试连接后端
2. 如果连接失败，自动切换到模拟数据
3. 支付功能支持"测试模式"，跳过实际支付

### 测试数据

**拼团活动**:
1. 皇家猫粮2人拼团 - ¥49.9
2. 宠物零食3人拼团 - ¥29.9
3. 宠物沐浴露2人拼团 - ¥39.9

**我的订单**:
1. 皇家猫粮 - 拼团中
2. 宠物零食 - 拼团成功

---

## 🎯 下一步

1. 在 H5 环境中完成完整流程测试
2. 微信小程序开发者工具中测试小程序特性
3. 对接后端 API，替换模拟数据
4. 配置微信支付参数

---

## 📚 相关文档

- `README.md` - 项目说明文档
- `TESTING_CHECKLIST.md` - 详细测试清单
- `docs/PIN_TUAN_MODULE_SUMMARY.md` - 拼团模块详细文档

---

**准备好了吗？现在就运行 `npm run dev:h5` 开始测试吧！**
