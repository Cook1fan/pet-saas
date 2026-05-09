# 宠物门店私域运营一体化 SaaS MVP 最终技术方案

**文档版本**：V1.0 | **适用范围**：MVP 版本开发 | **适配**：Java 全栈技术栈，可直接用于 Claude Code 辅助开发

---

## 1. 整体架构设计

### 1.1 分层架构（单体架构，MVP 优先，后期可平滑拆微服务）

```
┌─────────────────────────────────────────────────────────────────┐
│                          前端层                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐ │
│  │  平台/门店PC后台 │  │  商家版小程序    │  │  C端小程序   │ │
│  │  (Vue3+Element+) │  │  (uniapp+Vue3)   │  │  (uniapp+Vue3)│ │
│  └──────────────────┘  └──────────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────────┘
                              ↓ HTTPS
┌─────────────────────────────────────────────────────────────────┐
│                        网关/接口层                                │
│              Spring Boot Controller + Knife4j接口文档            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                        业务逻辑层                                │
│  租户服务、会员服务、营销服务、AI服务、订单服务、支付服务、库存服务│
│  自研代码生成器生成CRUD，Claude Code辅助开发业务逻辑            │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                        数据访问层                                │
│          MyBatis-Plus + MySQL（主数据） + Redis（缓存/高并发）  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                        外部服务层                                │
│  豆包大模型API、即梦图片生成API、微信支付/小程序API、阿里云OSS  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 部署架构（Docker Compose 一键部署，阿里云轻量服务器）

```
                    阿里云轻量应用服务器（2核4G，Ubuntu 22.04）
                    ┌─────────────────────────────────────────┐
                    │   Docker Engine                          │
                    │  ┌───────────────────────────────────┐  │
                    │  │  Nginx 容器（反向代理+SSL）        │  │
                    │  └───────────────────────────────────┘  │
                    │  ┌───────────────────────────────────┐  │
                    │  │  Spring Boot 应用容器             │  │
                    │  └───────────────────────────────────┘  │
                    │  ┌───────────────────────────────────┐  │
                    │  │  MySQL 8.0 容器（数据持久化）      │  │
                    │  └───────────────────────────────────┘  │
                    │  ┌───────────────────────────────────┐  │
                    │  │  Redis 7.x 容器（缓存/分布式锁）  │  │
                    │  └───────────────────────────────────┘  │
                    └─────────────────────────────────────────┘
                              ↓
                    阿里云域名解析 + 免费SSL证书 + ICP备案
```

---

## 2. 工程结构设计

总计 4 个工程，结构清晰，便于 Claude Code 分模块开发，后期可平滑扩展：

| 工程名称 | 技术栈 | 工程用途 | 核心目录结构 |
|---------|--------|---------|-------------|
| **pet-saas-backend** | Spring Boot 3.2.x | 后端单体工程，所有业务接口与逻辑 | `src/main/java`<br>`├── com.pet.saas`<br>`│ ├── config // 配置类（多租户、Sa-Token、MyBatis-Plus）`<br>`│ ├── controller // 接口层`<br>`│ ├── service // 业务逻辑层`<br>`│ ├── mapper // 数据访问层`<br>`│ ├── entity // 实体类`<br>`│ ├── dto // 请求/响应DTO`<br>`│ ├── common // 通用工具类、常量、枚举`<br>`│ └── handler // 全局异常、拦截器`<br>`src/main/resources // 配置文件、SQL脚本` |
| **pet-saas-admin-pc** | Vue3 + Element Plus + Vite | PC 前端工程，包含平台管理端 + 门店管理端 | `src`<br>`├── api // 接口封装`<br>`├── assets // 静态资源`<br>`├── components // 公共组件`<br>`├── router // 路由配置（权限控制）`<br>`├── stores // Pinia状态管理`<br>`├── utils // 工具类`<br>`├── views // 页面`<br>`│ ├── platform // 平台管理端页面`<br>`│ └── shop // 门店管理端页面`<br>`└── App.vue` |
| **pet-saas-miniapp** | uniapp + Vue3 + uView UI | 小程序工程，一套代码编译商家版 + C 端 | `src`<br>`├── api // 接口封装`<br>`├── components // 公共组件`<br>`├── pages // 页面`<br>`│ ├── merchant // 商家版页面（条件编译）`<br>`│ └── user // C端页面`<br>`├── static // 静态资源`<br>`├── store // Pinia状态管理`<br>`├── utils // 工具类`<br>`├── manifest.json // 小程序配置`<br>`└── pages.json // 路由配置` |
| **pet-saas-common** | Java | 可选公共模块，存放通用工具类、枚举、常量 | 后期拆微服务时可直接复用，MVP 阶段可合并到 backend 工程 |

---

## 3. 核心技术栈选型

### 3.1 后端技术栈

| 技术 / 工具 | 版本 | 用途 | 核心配置说明 |
|------------|------|------|-------------|
| Spring Boot | 3.2.x | 核心后端框架 | 用官方稳定版，简化开发，自动配置 |
| MyBatis-Plus | 最新稳定版 | ORM 框架，简化 CRUD | 配合自研代码生成器，一键生成 Entity、Mapper、Service、Controller；启用多租户插件、分页插件 |
| MySQL | 8.0 | 主数据存储 | InnoDB 引擎，支持事务，行级锁；所有业务表加 tenant_id 字段 |
| Redis | 7.x | 缓存、分布式锁、高并发控制 | 用 Redisson 实现分布式锁；缓存热点数据、活动库存、登录态 |
| Sa-Token | 1.38.0 | 权限认证、多端登录 | 配置多端 token 前缀隔离（pc_/merchant_/user_）；用注解实现角色权限控制 |
| RabbitMQ | 最新稳定版 | 异步处理、削峰填谷 | 异步发送通知、订单后续逻辑处理；秒杀场景削峰 |
| Spring AI Alibaba | 最新稳定版 | 豆包大模型对接 | 原生支持豆包系列模型，简化 AI 功能开发 |
| Knife4j | 最新稳定版 | 接口文档 | 自动生成接口文档，前后端联调使用 |
| Hutool | 最新稳定版 | 工具类库 | Excel 批量导入、图片处理、加密解密等 |
| EasyExcel | 最新稳定版 | Excel 批量导入导出 | 备选，处理大文件 Excel 导入 |

### 3.2 前端技术栈

| 技术 / 工具 | 版本 | 用途 | 说明 |
|------------|------|------|------|
| Vue3 | 3.4.x | PC 端核心框架 | Composition API 开发，和现有技术栈完全匹配 |
| Element Plus | 最新稳定版 | PC 端 UI 组件库 | 开箱即用，适配后台管理系统开发 |
| Vite | 最新稳定版 | PC 端构建工具 | 比 Webpack 更快的热更新，提升开发效率 |
| Pinia | 最新稳定版 | 状态管理 | 替代 Vuex，更简洁的 API，适配 Vue3 |
| Axios | 最新稳定版 | HTTP 请求封装 | 统一添加 token、tenant_id 请求头，统一错误处理 |
| ECharts | 最新稳定版 | 数据可视化 | 数据看板、趋势图开发 |
| uniapp | 最新稳定版 | 小程序跨端框架 | 100% 兼容 Vue3 语法，一套代码编译商家版 + C 端微信小程序 |
| uView UI | 最新稳定版 | 小程序 UI 组件库 | 适配小程序的开箱即用组件，和 Element Plus 使用逻辑类似 |

### 3.3 第三方服务

| 服务 | 用途 | 对接说明 |
|-----|------|---------|
| 字节跳动火山引擎 | 豆包大模型 API 调用 | 开通服务，获取 API Key，用 Spring AI Alibaba 对接 |
| 即梦 AI | 活动海报生成 | 开通图文生成 API，获取 API Key，HTTP 调用 |
| 微信开放平台 | 小程序、微信支付对接 | 申请小程序账号、微信商户号，对接官方 API |
| 阿里云 | 服务器、OSS、短信服务 | 轻量应用服务器部署，OSS 存储图片 / 海报，短信发送提醒 |

---

## 4. 核心模块技术实现细节

### 4.1 多租户隔离实现

**方案**：共享数据库 + 共享数据表 + tenant_id 字段隔离，MyBatis-Plus 多租户插件自动处理

**表结构规范**：所有业务表（除平台全局表）必须包含 `tenant_id bigint not null` 字段，添加索引

**插件配置**：

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 多租户插件
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            // 获取当前登录用户的tenant_id
            @Override
            public Expression getTenantId() {
                Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
                return new LongValue(tenantId == null ? 0 : tenantId);
            }
            // 租户字段名
            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }
            // 忽略租户隔离的表
            @Override
            public boolean ignoreTable(String tableName) {
                List<String> ignoreTables = Arrays.asList("sys_platform_admin", "sys_dict", "sys_config");
                return ignoreTables.contains(tableName) || StpUtil.hasRole("platform_admin");
            }
        }));
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

**租户上下文**：登录时将 tenant_id 存入 Sa-Token Session，全链路自动获取，无需手动传参

### 4.2 权限系统实现

**方案**：Sa-Token + 角色权限控制，多端登录隔离

**多端登录配置（application.yml）**：

```yaml
sa-token:
  token-name: satoken
  timeout: 2592000 # 30天有效期
  is-concurrent: true
  token-prefix:
    pc: pc_
    merchant: merchant_
    user: user_
```

**登录逻辑**：登录时指定设备类型，存入租户、用户、角色信息

```java
// PC端门店管理员登录
@PostMapping("/pc/login")
public R login(String username, String password, Long tenantId) {
    // 1. 验证账号密码与租户权限
    ShopAdmin admin = adminService.lambdaQuery()
            .eq(ShopAdmin::getUsername, username)
            .eq(ShopAdmin::getTenantId, tenantId)
            .one();
    // 2. 密码校验
    if (!PasswordUtil.matches(password, admin.getPassword())) {
        return R.error("账号或密码错误");
    }
    // 3. 登录，指定设备类型为pc
    StpUtil.login(admin.getId(), "pc");
    StpUtil.getSession().set("tenant_id", tenantId);
    StpUtil.getSession().set("admin_id", admin.getId());
    StpUtil.getSession().set("role", admin.getRole());
    return R.ok().put("token", StpUtil.getTokenValue());
}
```

**权限校验**：用注解实现角色拦截

```java
// 仅门店管理员可访问
@SaCheckRole("shop_admin")
@PostMapping("/activity/add")
public R addActivity(Activity activity) {
    activityService.save(activity);
    return R.ok();
}
```

### 4.3 高并发拼团 / 秒杀实现

**核心目标**：防止超卖、系统不崩溃、数据一致

**活动预热**：活动创建时，将库存同步到 Redis：`set activity:stock:{activity_id} {库存数}`

**Redis 预扣库存**：下单时先通过 Redis 原子操作扣减库存，失败直接返回

```java
// 预扣库存
Long stock = redisTemplate.opsForValue().decrement("activity:stock:" + activityId);
if (stock < 0) {
    // 库存不足，回补
    redisTemplate.opsForValue().increment("activity:stock:" + activityId);
    return R.error("活动库存不足");
}
```

**分布式锁防重复下单**：Redisson 分布式锁，防止同一用户重复下单

```java
RLock lock = redissonClient.getLock("order:user:" + userId + ":activity:" + activityId);
try {
    if (!lock.tryLock(3, 10, TimeUnit.SECONDS)) {
        return R.error("请勿重复下单");
    }
    // 订单创建逻辑
} finally {
    lock.unlock();
}
```

**异步处理**：订单创建成功后，通过 RabbitMQ 异步发送通知、更新销量，不阻塞主流程

### 4.4 AI 功能对接（Spring AI Alibaba + 豆包）

**依赖引入**：

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-ai-alibaba-starter</artifactId>
    <version>最新稳定版</version>
</dependency>
```

**配置文件**：

```yaml
spring:
  ai:
    alibaba:
      api-key: 你的豆包API Key
      chat:
        options:
          model: doubao-seed-2.0-pro
          temperature: 0.7
          max-tokens: 2048
```

**核心服务实现**：

```java
@Service
public class AIService {

    private final ChatClient chatClient;
    @Value("${spring.ai.alibaba.chat.options.model}")
    private String model;

    public AIService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    // AI文案生成
    public List<String> generateCopy(String type, String shopName, String keywords) {
        // 构建Prompt
        String prompt = String.format("""
            你是专业的宠物门店运营专家，根据以下信息生成3条不同风格的%s文案，每条不超过200字：
            门店名称：%s
            关键词：%s
            要求：风格分别为温馨治愈型、活泼促销型、干货科普型，适合宠物主人阅读
            """, type, shopName, keywords);

        // 调用大模型
        String content = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 拆分3条文案返回
        return Arrays.asList(content.split("\n\n"));
    }

    // AI客服问答
    public String aiChat(String question, ShopConfig shopConfig) {
        String prompt = String.format("""
            你是%s的AI客服，也是专业的宠物养护专家。
            门店信息：地址%s，营业时间%s，联系电话%s
            客户问题：%s
            要求：门店相关问题按信息回答，养护问题专业易懂，不知道的引导联系门店客服
            """, shopConfig.getShopName(), shopConfig.getAddress(),
            shopConfig.getBusinessHours(), shopConfig.getPhone(), question);

        return chatClient.prompt().user(prompt).call().content();
    }
}
```

**额度控制**：Redis 记录每个门店每日调用次数，超出后拦截

```java
// 检查AI调用额度
public boolean checkAiLimit(Long tenantId) {
    String key = "ai:limit:" + tenantId + ":" + LocalDate.now();
    Long count = redisTemplate.opsForValue().increment(key);
    if (count == 1) {
        redisTemplate.expire(key, 24, TimeUnit.HOURS);
    }
    // 免费额度50次/天
    return count <= 50;
}
```

### 4.5 即梦海报生成实现

**API 调用封装**：

```java
@Service
public class PosterService {

    @Value("${jimeng.api-key}")
    private String apiKey;
    @Value("${jimeng.api-url}")
    private String apiUrl;
    private final RestTemplate restTemplate;

    // 生成活动海报
    public String generatePoster(Activity activity, ShopConfig shopConfig, String style) {
        // 构建Prompt
        String prompt = String.format("""
            生成竖版750x1334的宠物门店活动海报，风格%s，画面干净协调。
            固定元素：
            1. 左上角放logo：%s
            2. 居中大字标题：%s
            3. 标题下方小字：活动时间%s
            4. 右下角预留150x150空白区域放小程序码
            5. 底部小字：%s | %s
            要求：文字清晰无错字，布局合理，符合宠物门店调性
            """, style, shopConfig.getLogoUrl(), activity.getTitle(),
            activity.getStartTime() + "至" + activity.getEndTime(),
            shopConfig.getShopName(), shopConfig.getAddress());

        // 调用即梦API
        JSONObject request = new JSONObject();
        request.put("model", "jimeng-general-v2");
        request.put("prompt", prompt);
        request.put("width", 750);
        request.put("height", 1334);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<JSONObject> entity = new HttpEntity<>(request, headers);

        JSONObject response = restTemplate.postForObject(apiUrl, entity, JSONObject.class);
        // 解析返回的图片URL
        String imageUrl = response.getJSONArray("data").getJSONObject(0).getString("url");

        // 下载图片，合成小程序码，上传到OSS返回最终URL
        return mergeQrCode(imageUrl, activity.getQrCodeUrl());
    }

    // 合成小程序码
    private String mergeQrCode(String backgroundUrl, String qrCodeUrl) {
        // 用Hutool ImageUtil合成图片，将小程序码放到预留的空白区域
        // 合成后上传到阿里云OSS，返回公网URL
        return ossService.uploadImage(mergedImage);
    }
}
```

### 4.6 支付与对账实现

- **资金流向**：客户支付直接调用门店自有的微信商户号，资金 T+1 自动提现到门店银行卡，平台不碰资金
- **支付回调处理**：微信支付成功后，回调后端接口，更新订单状态，保证幂等性
- **自动对账逻辑**：
  1. 每日凌晨通过微信支付 API 下载门店前一日的交易账单
  2. 遍历账单，与系统流水表通过微信支付单号匹配
  3. 金额、状态一致的标记为已对账，不一致的标记为异常，触发告警

---

## 5. 核心数据库表设计（MVP 必做）

### 5.1 全局表（无 tenant_id）

| 表名 | 核心字段 | 用途 |
|-----|---------|------|
| sys_platform_admin | id, username, password, role, create_time | 平台超级管理员表 |
| sys_tenant | tenant_id, shop_name, admin_phone, address, status, create_time | 租户表，记录所有门店 |
| sys_ai_package | id, name, times, price, status | AI 增值套餐表 |
| sys_config | id, config_key, config_value, description | 系统全局配置表 |

### 5.2 业务表（带 tenant_id）

| 表名 | 核心字段 | 用途 |
|-----|---------|------|
| shop_admin | id, tenant_id, username, password, role, status | 门店管理员 / 店员表 |
| shop_config | id, tenant_id, shop_name, address, phone, business_hours, logo, pay_config | 门店基础配置表 |
| member | id, tenant_id, phone, name, tags, create_time | 会员表 |
| pet_info | id, tenant_id, member_id, name, breed, birthday, vaccine_time, deworm_time, wash_time | 宠物档案表 |
| recharge_rule | id, tenant_id, name, recharge_amount, give_amount, status | 储值规则表 |
| card_rule | id, tenant_id, name, times, price, valid_days, status | 次卡规则表 |
| member_account | id, tenant_id, member_id, balance, total_recharge, create_time | 会员储值账户表 |
| member_card | id, tenant_id, member_id, card_rule_id, remain_times, expire_time | 会员次卡表 |
| goods | id, tenant_id, name, category, price, stock, warn_stock, is_service, status | 商品 / 服务表 |
| stock_record | id, tenant_id, goods_id, type, num, remark, create_user, create_time | 出入库记录表 |
| order_info | id, tenant_id, order_no, member_id, total_amount, pay_amount, pay_type, pay_status, pay_time, transaction_id | 订单表 |
| order_item | id, tenant_id, order_id, goods_id, goods_name, num, price | 订单明细表 |
| activity_info | id, tenant_id, type, title, goods_id, price, origin_price, stock, start_time, end_time, status | 拼团 / 秒杀活动表 |
| activity_order | id, tenant_id, activity_id, order_id, member_id, group_id, status | 活动订单表 |
| flow_record | id, tenant_id, flow_no, order_id, amount, type, pay_status, transaction_id, create_time | 流水记录表 |
| ai_call_record | id, tenant_id, type, prompt, result, cost_times, create_time | AI 调用记录表 |

---

## 6. 部署上线方案

### 6.1 环境准备

- **服务器**：阿里云 2 核 4G 轻量应用服务器，Ubuntu 22.04 系统，开放 80/443/22 端口
- **域名与备案**：注册域名，完成 ICP 备案，配置 DNS 解析到服务器 IP
- **SSL 证书**：申请阿里云免费 SSL 证书，配置 HTTPS
- **资质准备**：完成营业执照注册、微信小程序 / 商户号申请、第三方服务 API 开通

### 6.2 Docker Compose 部署配置

```yaml
version: '3.8'
services:
  nginx:
    image: nginx:latest
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/conf:/etc/nginx/conf.d
      - ./nginx/ssl:/etc/nginx/ssl
      - ./static:/usr/share/nginx/html
    restart: always
    networks:
      - pet-saas-network

  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    volumes:
      - ./mysql/data:/var/lib/mysql
      - ./mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
    environment:
      MYSQL_ROOT_PASSWORD: 你的数据库密码
      MYSQL_DATABASE: pet_saas
    restart: always
    networks:
      - pet-saas-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - ./redis/data:/data
    command: redis-server --requirepass 你的Redis密码
    restart: always
    networks:
      - pet-saas-network

  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: 你的MQ密码
    restart: always
    networks:
      - pet-saas-network

  pet-saas-backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
      - rabbitmq
    environment:
      SPRING_PROFILES_ACTIVE: prod
    restart: always
    networks:
      - pet-saas-network

networks:
  pet-saas-network:
    driver: bridge
```

### 6.3 上线流程

1. 本地完成开发、单元测试、前后端联调
2. 打包后端 JAR 包、前端静态文件，上传到服务器
3. 执行 `docker-compose up -d` 一键启动所有服务
4. 验证接口、页面、支付流程是否正常
5. 提交小程序审核，审核通过后上线
6. 招募种子客户，开通租户账号，开始试用

---

## 7. 风险与兜底方案

| 风险点 | 兜底方案 |
|-------|---------|
| 微信小程序审核驳回 | 提前准备资质，简化小程序功能，先上线核心功能，后续迭代补充 |
| 即梦 API 生成海报效果不佳 | 降级为「即梦生成背景 + 后端合成文字 /logo/ 小程序码」的方案 |
| 高并发活动系统崩溃 | 前置限流，超过 QPS 阈值直接拒绝请求，保障核心流程可用 |
| 对账异常 | 实时告警，平台可手动处理异常订单，保证门店资金对账准确 |
| AI 调用成本超支 | 严格控制免费额度，通过增值套餐覆盖成本，大流量时切换性价比更高的模型 |
