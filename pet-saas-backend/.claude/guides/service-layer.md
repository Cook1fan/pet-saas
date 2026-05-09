# Service 层规范

> **重要提醒**：以一个有15年编程经验的资深专家的态度去写代码，别写恶臭的代码出来

---

## Service 接口规范

### 必须定义 Service 接口

所有 Service 必须定义接口，接口命名规范为 `{业务名}Service`，放在 `service` 包下。

**为什么要定义接口**：
1. **解耦** - Controller 层依赖接口而非具体实现，便于替换和测试
2. **扩展性** - 可以有多个实现类，便于扩展不同的业务逻辑
3. **代理支持** - Spring AOP 等功能依赖接口代理
4. **规范清晰** - 接口定义了 Service 的公共契约，一目了然

### 目录结构规范

```
com.pet.saas.service/
├── ActivityService.java          (接口)
├── GoodsService.java             (接口)
├── MemberService.java            (接口)
├── OrderService.java             (接口)
├── TenantService.java            (接口)
├── AuthService.java              (接口)
├── FlowRecordService.java        (接口)
└── impl/                          (实现类子包)
    ├── ActivityServiceImpl.java    (实现类)
    ├── GoodsServiceImpl.java       (实现类)
    ├── MemberServiceImpl.java      (实现类)
    ├── OrderServiceImpl.java       (实现类)
    ├── TenantServiceImpl.java      (实现类)
    ├── AuthServiceImpl.java        (实现类)
    └── FlowRecordServiceImpl.java  (实现类)
```

### 命名规范

| 类型 | 命名规范 | 位置 | 示例 |
|-----|---------|------|------|
| 接口 | `{业务名}Service` | `service` 包 | `ActivityService` |
| 实现类 | `{业务名}ServiceImpl` | `service.impl` 包 | `ActivityServiceImpl` |

### 代码示例

```java
// 接口定义 - service 包下
public interface ActivityService {
    ActivityInfo createGroupActivity(ActivityController.CreateGroupActivityReq req, Long tenantId);
    Page<ActivityInfo> listActivities(ActivityQuery query, Long tenantId);
}

// 实现类 - service.impl 包下
package com.pet.saas.service.impl;

import com.pet.saas.service.ActivityService;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl extends ServiceImpl<ActivityInfoMapper, ActivityInfo>
        implements ActivityService {

    private final ActivityInfoMapper activityInfoMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ActivityInfo createGroupActivity(ActivityController.CreateGroupActivityReq req, Long tenantId) {
        // 实现逻辑
    }

    @Override
    public Page<ActivityInfo> listActivities(ActivityQuery query, Long tenantId) {
        // 实现逻辑
    }
}
```

---

## Service 方法参数规范

### 禁止写恶臭代码：方法参数严禁超过 2 个！

当方法参数超过 2 个时，必须将参数封装成对象传递。

**为什么不能有多个参数**：
1. **可读性差** - 参数太多时，调用方很难记住参数顺序
2. **难以维护** - 新增/删除参数时需要修改所有调用方
3. **容易出错** - 参数顺序错误编译器不会报错，导致隐蔽 bug
4. **代码恶臭** - 刚毕业的程序员才会写一长串参数

### 参数数量处理规则

| 参数数量 | 处理方式 |
|---------|---------|
| **0-2 个参数** | 直接传递 |
| **3 个及以上参数** | 必须封装为对象 |

### Service 层参数规范（简化版）

**重要规则**：单体项目简化架构，Service 层直接使用 Controller 层的 DTO！

- **查询操作**：直接使用 `dto.query` 包下的 Query DTO + `tenantId`
- **创建/更新操作**：直接使用 Controller 内部静态类 Req + `tenantId`
- **tenantId 传递**：从会话中获取，作为单独参数传递给 Service

### Controller 层请求命名规范

- **查询对象**：`{业务名}Query`（放在 `dto.query` 包）
- **创建/更新请求**：`Create{业务名}Req` / `Update{业务名}Req`（作为 Controller 内部静态类）

### DTO 编写规范

DTO 类应该：
1. 使用 Lombok 的 `@Data` 注解
2. 添加完整的 `@Schema` 注解说明
3. 添加必要的校验注解（`@NotNull`, `@NotBlank` 等）
4. 分页参数设置默认值（pageNum=1, pageSize=10）

```java
@Data
@Schema(description = "活动列表查询条件")
public class ActivityQuery {
    @Schema(description = "活动类型：1-拼团，2-秒杀")
    private Integer type;

    @Schema(description = "状态：0-未开始，1-进行中，2-已结束")
    private Integer status;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;
}
```

### Service 方法签名示例

```java
// ==================== 错误示例 ====================
// 恶臭代码！10 个参数！
public ActivityInfo createGroupActivity(Long tenantId, String title, Long goodsId,
        BigDecimal price, BigDecimal originPrice, Integer groupCount, Integer stock,
        Integer limitNum, LocalDateTime startTime, LocalDateTime endTime) {
    // ...
}

// ==================== 正确示例 ====================
// 直接使用 Controller 层 Req + tenantId
public ActivityInfo createGroupActivity(ActivityController.CreateGroupActivityReq req, Long tenantId) {
    // ...
}

// 查询使用 Query DTO + tenantId
public Page<ActivityInfo> listActivities(ActivityQuery query, Long tenantId) {
    // ...
}
```

---

## 分层架构规范

### Entity 仅用于 Service 和 Mapper 层

**重要规则**：禁止直接将数据库 Entity 类作为接口返回值或请求参数！

### 为什么不能直接返回 Entity

1. **安全风险** - Entity 可能包含敏感字段（如密码、租户ID、内部标识、isDeleted 等）
2. **耦合度高** - 前端直接依赖数据库结构，数据库变更会影响前端
3. **维护困难** - 无法灵活控制返回给前端的字段
4. **不符合规范** - 标准的分层架构应该使用 VO/DTO 分离内外层

### 包结构规范

| 包 | 说明 | 示例 |
|----|------|------|
| `com.pet.saas.entity` | 数据库实体类，仅用于 Service 和 Mapper 层 | `Member`, `Goods` |
| `com.pet.saas.dto.req` | 请求 DTO，用于接收前端参数（独立文件） | `PcLoginReq`, `GoodsSaveReq` |
| `com.pet.saas.dto.query` | 查询条件 DTO，用于 GET 请求多参数封装 | `MemberQuery`, `OrderQuery` |
| `com.pet.saas.dto.resp` | 响应 VO，用于返回给前端 | `MemberVO`, `OrderInfoVO` |

### 使用规范

1. **Controller 层**：只接收 Req/Query DTO，只返回 Resp VO
2. **Service 层**：内部使用 Entity 进行业务处理，直接使用 Controller 层的 Req/Query
3. **转换**：使用 `BeanConverter` 工具类进行 Entity 与 VO/DTO 之间的转换
4. **租户ID**：使用 `RedisKeyConstants.TENANT_ID_KEY` 常量，禁止硬编码 `"tenant_id"`
5. **代码注释**：如果业务逻辑相对比较复杂的时候，需要在代码里写上注释，说明业务逻辑的意图和关键点

### 代码示例

```java
// ==================== 错误示例 ====================
// 不要这样做！直接返回 Entity
@GetMapping("/list")
public R<Page<Member>> listMembers(...) {
    Page<Member> page = memberService.listMembers(...);
    return R.ok(page);
}

// ==================== 正确示例 ====================
// Controller 层
@GetMapping("/list")
public R<PageResult<MemberVO>> listMembers(@ParameterObject @Valid MemberQuery query) {
    Long tenantId = (Long) StpUtil.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
    Page<Member> page = memberService.listMembers(query, tenantId);
    return R.ok(BeanConverter.convertToPageResult(page, MemberVO.class));
}
```