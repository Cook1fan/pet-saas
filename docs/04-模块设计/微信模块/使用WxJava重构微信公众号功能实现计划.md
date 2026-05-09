# 使用 WxJava 重构微信公众号功能实现计划

## 一、背景与目标

### 1.1 背景
- 当前微信公众号功能框架已搭建完成，接口定义和业务流程逻辑清晰
- 但 `WxMpServiceImpl` 所有方法都是 Mock 实现，未真正接入微信 SDK
- `MpMessageController` 中手动实现了签名验证逻辑，容易出错且维护成本高
- 消息处理未实现，`handleMessage` 方法仅返回空字符串
- pom.xml 中已定义 `wxjava.version 4.6.0`，但未在 dependencies 中实际引入

### 1.2 目标
- 使用 WxJava (binarywang/WxJava) 简化微信公众号开发
- 替换当前的 Mock 实现，接入真实的微信 API
- 提供稳定可靠的 SDK 支持，降低维护成本

---

## 二、重构方案

### 2.1 技术选型
- **SDK**: WxJava (binarywang/WxJava) v4.6.0
- **模块**: wx-java-mp-spring-boot-starter (微信公众号 Spring Boot Starter)

---

## 三、实施步骤

### Step 1: 更新 Maven 依赖

**文件**: `pom.xml`

在 `<dependencies>` 节点中添加 WxJava 微信公众号模块依赖：

```xml
<!-- WxJava 微信公众号 SDK -->
<dependency>
    <groupId>com.github.binarywang</groupId>
    <artifactId>wx-java-mp-spring-boot-starter</artifactId>
    <version>${wxjava.version}</version>
</dependency>
```

### Step 2: 调整配置属性类

**修改文件**: `src/main/java/com/pet/saas/config/properties/WxMpProperties.java`

调整内容：
- 兼容 WxJava 的标准命名（`app-id`、`aes-key`）和旧命名（`appid`、`aesKey`）
- 添加 `registerSuccessTemplateId` 配置项（注册成功模板消息 ID）
- 提供 `getEffectiveAppId()` 和 `getEffectiveAesKey()` 方法

### Step 3: 创建 WxJava 配置类

**新增文件**: `src/main/java/com/pet/saas/config/WxMpConfig.java`

功能：
- 基于 `WxMpProperties` 配置
- 初始化 `WxMpService` 单例 Bean
- 配置 `WxMpConfigStorage`
- 配置消息路由器 `WxMpMessageRouter`（用于处理不同类型的微信消息）

### Step 4: 创建消息处理器

**新增目录**: `src/main/java/com/pet/saas/handler/mp/`

创建以下处理器：

| 文件名 | 说明 |
|--------|------|
| `AbstractMpMessageHandler.java` | 消息处理器基类 |
| `MpLogHandler.java` | 日志记录处理器 |
| `MpNullHandler.java` | 默认处理器 |
| `MpSubscribeHandler.java` | 关注事件处理器 |
| `MpUnsubscribeHandler.java` | 取消关注事件处理器 |
| `MpMenuHandler.java` | 菜单事件处理器 |
| `MpMsgHandler.java` | 普通消息处理器 |

### Step 5: 重构 WxMpServiceImpl

**修改文件**: `src/main/java/com/pet/saas/service/impl/WxMpServiceImpl.java`

使用 WxJava SDK 替换 Mock 实现：

| 方法 | WxJava API |
|------|------------|
| `getUserInfo()` | `wxMpService.getOAuth2Service().getAccessToken(code)` + `getUserInfo()` |
| `decryptPhone()` | 根据实际情况选择：<br>1. 加密数据解密（需引入小程序模块）<br>2. 手机号动态码 API |
| `sendTemplateMessage()` | `wxMpService.getTemplateMsgService().sendTemplateMsg()` |
| `sendRegisterSuccessNotification()` | 构建模板消息数据并调用发送 |

### Step 6: 重构 MpMessageController

**修改文件**: `src/main/java/com/pet/saas/controller/mp/MpMessageController.java`

简化实现：
- GET 请求（服务器验证）：使用 `wxMpService.checkSignature()` 替代手动实现
- POST 请求（消息接收）：使用 `wxMpMessageRouter.route()` 处理消息
- 支持明文和 AES 加密消息
- **删除**: 手动实现的 `checkSignature()` 和 `bytesToHexString()` 方法

### Step 7: 配置文件调整

**文件**: `application.yml` (或 application-local.yml 等)

配置示例：

```yaml
wx:
  mp:
    app-id: ${WX_MP_APPID:your_appid}
    secret: ${WX_MP_SECRET:your_secret}
    token: ${WX_MP_TOKEN:your_token}
    aes-key: ${WX_MP_AES_KEY:your_aeskey}
    msg-data-format: JSON
    register-success-template-id: ${WX_MP_REGISTER_TEMPLATE_ID:}
```

---

## 四、文件清单

### 4.1 新增文件

| 文件路径 | 说明 |
|---------|------|
| `src/main/java/com/pet/saas/config/WxMpConfig.java` | WxJava 配置类 |
| `src/main/java/com/pet/saas/handler/mp/AbstractMpMessageHandler.java` | 消息处理器基类 |
| `src/main/java/com/pet/saas/handler/mp/MpLogHandler.java` | 日志处理器 |
| `src/main/java/com/pet/saas/handler/mp/MpNullHandler.java` | 默认处理器 |
| `src/main/java/com/pet/saas/handler/mp/MpSubscribeHandler.java` | 关注事件处理器 |
| `src/main/java/com/pet/saas/handler/mp/MpUnsubscribeHandler.java` | 取消关注事件处理器 |
| `src/main/java/com/pet/saas/handler/mp/MpMenuHandler.java` | 菜单事件处理器 |
| `src/main/java/com/pet/saas/handler/mp/MpMsgHandler.java` | 普通消息处理器 |

### 4.2 修改文件

| 文件路径 | 说明 |
|---------|------|
| `pom.xml` | 添加 WxJava 依赖 |
| `src/main/java/com/pet/saas/config/properties/WxMpProperties.java` | 调整配置属性命名 |
| `src/main/java/com/pet/saas/service/impl/WxMpServiceImpl.java` | 接入真实 SDK |
| `src/main/java/com/pet/saas/controller/mp/MpMessageController.java` | 简化消息处理 |

---

## 五、验证步骤

### 5.1 编译检查
执行 `mvn clean compile` 确保代码编译通过

### 5.2 配置检查
确保 `application.yml` 中微信公众号配置正确

### 5.3 功能测试
- 微信服务器验证接口测试
- 微信授权回调流程测试
- 手机号解密功能测试（根据实际 API 调整）
- 模板消息发送测试

### 5.4 日志检查
确认所有微信 API 调用都有正确的日志记录

---

## 六、注意事项

### 6.1 向后兼容
- 保持 `WxMpService` 接口不变，只修改实现类
- 保持现有 Controller 接口签名不变

### 6.2 配置兼容
- `WxMpProperties` 同时支持新旧配置命名
- 优先使用标准命名（`app-id`、`aes-key`）

### 6.3 手机号解密
- 微信公众号获取手机号有两种方式：
  1. 旧方式：`encryptedData + iv + sessionKey` 解密（与小程序一致）
  2. 新方式：使用手机号动态码 code 调用 `getPhoneNumber` API
- 需要根据实际需求选择并调整 `decryptPhone()` 方法

### 6.4 模板消息
- 需要在微信公众号后台配置模板消息
- 配置完成后设置 `register-success-template-id`

### 6.5 错误处理
- WxJava 会抛出 `WxErrorException`，需要妥善处理
- 记录详细的错误日志，便于排查问题

---

## 七、开发阶段划分

### Phase 1: 基础接入
- [ ] 更新 Maven 依赖
- [ ] 调整配置属性类
- [ ] 创建 WxMpConfig 配置类

### Phase 2: 消息处理
- [ ] 创建消息处理器
- [ ] 重构 MpMessageController
- [ ] 测试消息接收和验证

### Phase 3: 业务功能
- [ ] 重构 WxMpServiceImpl
- [ ] 接入 OAuth2 授权流程
- [ ] 接入模板消息发送

### Phase 4: 测试验证
- [ ] 编译验证
- [ ] 功能测试
- [ ] 日志检查
