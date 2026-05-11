# Controller 层规范

---

## 接口文档规范

**重要规则**：所有 Controller 接口的请求参数和响应参数的属性说明必须每次都写上！

### OpenAPI 3 注解使用规范

| 注解 | 应用位置 | 说明 |
|-----|---------|------|
| `@Tag` | Controller 类 | 接口分组名称 |
| `@Operation` | 接口方法 | 接口摘要说明 |
| `@Parameter` | `@RequestParam` 参数 | 请求参数说明 |
| `@Schema` | DTO/Entity 类 | 模型描述 |
| `@Schema` | 字段 | 字段属性说明 |

### 示例代码

```java
// Controller 类
@Tag(name = "门店PC端-会员管理")
@RestController
@RequestMapping("/api/pc/member")
public class MemberController {

    // 接口方法
    @Operation(summary = "会员列表")
    @GetMapping("/list")
    public R<PageResult<MemberVO>> listMembers(
            @Parameter(description = "手机号/姓名（模糊搜索）") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum) {
        ...
    }
}

// 请求/响应 DTO
@Data
@Schema(description = "会员录入请求")
public static class CreateMemberReq {
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    private String name;
}

// 响应 VO
@Data
@Schema(description = "会员信息响应")
public class MemberVO {
    @Schema(description = "会员ID")
    private Long id;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "姓名")
    private String name;
}
```

### 枚举值说明规范

对于枚举类型的字段，必须在 `@Schema` 中明确枚举值说明：

```java
@Schema(description = "支付方式：1-微信，2-现金，3-储值，4-次卡")
private Integer payType;

@Schema(description = "支付状态：0-待支付，1-已支付，2-已退款")
private Integer payStatus;

@Schema(description = "分类：1-宠物食品类，2-宠物用品类，3-宠物服务类，4-宠物医疗类")
private Integer category;
```

---

## GET 请求参数规范

**重要规则**：既想保留 GET 的优势，又不想写一长串 @RequestParam，最好的办法是将参数封装成一个 Java 对象；如果只有一个参数可以使用 url Path 的方式！

### 规则说明

| 参数数量 | 处理方式 | 示例 |
|---------|---------|------|
| **1个参数** | 使用 `@PathVariable` (URL Path 方式) | `GET /account/{memberId}` |
| **2个及以上参数** | 封装为 Query DTO，使用 `@ParameterObject` | `GET /list?keyword=xxx&pageNum=1` |

### 包结构规范

| 包 | 说明 | 示例 |
|----|------|------|
| `com.pet.saas.dto.query` | 查询条件 DTO，用于 GET 请求多参数封装 | `MemberQuery`, `OrderQuery` |

### Query DTO 编写规范

Query DTO 类应该：
1. 使用 Lombok 的 `@Data` 注解
2. 添加完整的 `@Schema` 注解说明
3. 给分页参数设置默认值（pageNum=1, pageSize=10）

```java
@Data
@Schema(description = "会员列表查询条件")
public class MemberQuery {
    @Schema(description = "手机号/姓名（模糊搜索）")
    private String keyword;

    @Schema(description = "标签")
    private String tag;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;
}
```

### Controller 代码示例

```java
// ==================== 错误示例 ====================
// 不要这样写！太多 @RequestParam
@GetMapping("/list")
public R<PageResult<MemberVO>> listMembers(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String tag,
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize) {
    ...
}

// 单参数也不要用 @RequestParam
@GetMapping("/account")
public R<MemberAccountVO> getMemberAccount(@RequestParam @NotNull Long memberId) {
    ...
}

// ==================== 正确示例 ====================
// 多参数用 Query DTO + @ParameterObject
@GetMapping("/list")
public R<PageResult<MemberVO>> listMembers(@ParameterObject @Valid MemberQuery query) {
    ...
}

// 单参数用 @PathVariable
@GetMapping("/account/{memberId}")
public R<MemberAccountVO> getMemberAccount(
        @Parameter(description = "会员ID", required = true) @PathVariable @NotNull Long memberId) {
    ...
}
```

---

## 分页响应规范

**重要规则**：禁止直接返回 MyBatis-Plus 的 Page 类型给前端，必须使用 PageResult！

### 为什么不能直接返回 Page

1. **框架耦合** - Page 是 MyBatis-Plus 框架类，不应该暴露给前端
2. **字段过多** - Page 包含很多内部字段，前端不需要
3. **不符合规范** - 应该使用自定义的分页响应对象

### PageResult 使用规范

使用 `com.pet.saas.dto.resp.PageResult<T>` 替代 `com.baomidou.mybatisplus.extension.plugins.pagination.Page<T>`

### PageResult 结构

```java
@Data
@Schema(description = "分页响应")
public class PageResult<T> {
    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总条数")
    private long total;

    @Schema(description = "当前页")
    private long current;

    @Schema(description = "每页条数")
    private long size;

    @Schema(description = "总页数")
    private long pages;
}
```

### BeanConverter 转换方法

使用 `BeanConverter.convertToPageResult(page, targetClass)` 进行转换：

```java
// ==================== 错误示例 ====================
// 不要这样做！直接返回 MyBatis-Plus Page
@GetMapping("/list")
public R<Page<MemberVO>> listMembers(...) {
    Page<Member> page = memberService.listMembers(...);
    return R.ok(BeanConverter.convertPage(page, MemberVO.class));
}

// ==================== 正确示例 ====================
// 使用 PageResult
@GetMapping("/list")
public R<PageResult<MemberVO>> listMembers(...) {
    Page<Member> page = memberService.listMembers(...);
    return R.ok(BeanConverter.convertToPageResult(page, MemberVO.class));
}
```

---

## Controller 层与 Service 层交互规范

**重要规则**：简化架构，直接传递 Controller 层 DTO 给 Service 层！

### 参数传递规范

1. **Query DTO**：直接传递给 Service，不做转换
2. **Req DTO**：直接传递给 Service，不做转换
3. **tenantId**：从会话获取，作为单独参数传递
4. **使用常量**：必须使用 `RedisKeyConstants.TENANT_ID_KEY`，禁止硬编码 `"tenant_id"`

### 代码示例

```java
@Operation(summary = "活动列表")
@GetMapping("/list")
public R<PageResult<ActivityInfoVO>> listActivities(@ParameterObject @Valid ActivityQuery query) {
    Long tenantId = (Long) StpUtil.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
    Page<ActivityInfo> page = activityService.listActivities(query, tenantId);
    return R.ok(BeanConverter.convertToPageResult(page, ActivityInfoVO.class));
}

@Operation(summary = "创建拼团活动")
@PostMapping("/group/create")
public R<ActivityInfoVO> createGroupActivity(@Valid @RequestBody CreateGroupActivityReq req) {
    Long tenantId = (Long) StpUtil.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
    ActivityInfo activity = activityService.createGroupActivity(req, tenantId);
    return R.ok(BeanConverter.convert(activity, ActivityInfoVO.class));
}
```
