# 后端强制编码规则

## 安全与权限

- ✅ 所有入参必须做校验（`@Valid`、`@Validated`）
- ✅ 禁止在 Controller 层写业务逻辑

## 分层架构

- ✅ Controller 层：只接收请求、参数校验、调用 Service、返回 VO
- ✅ Service 层：业务逻辑、事务控制、权限校验
- ✅ Mapper 层：数据访问，继承 `BaseMapper<Entity>`

## 禁止事项

- ❌ 禁止直接返回 Entity 给前端 → 必须转 VO
- ❌ 禁止使用 `@Value` 读取配置 → 用 `@ConfigurationProperties`
- ❌ 禁止使用 `System.out.println` → 用 `@Slf4j` + `log`
- ❌ **禁止超过 2 个方法参数 → 封装为对象**
- ❌ 禁止私有方法调用不加 `this.`
- ❌ **禁止使用类的全限定名直接 new 对象 → 必须 import 后使用简短类名**

## DTO 规范

| 类型 | 位置 | 命名 |
|-----|------|------|
| 请求 DTO | `dto/req/` | `XxxReq` |
| 查询 DTO | `dto/query/` | `XxxQuery` |
| 响应 VO | `dto/resp/` | `XxxVO` |
| 分页响应 | `dto/resp/` | `PageResult<T>` |

## 注释规范

- ✅ **Service 层**：所有 public 方法必须写 JavaDoc 注释
- ✅ **复杂业务逻辑**：关键业务步骤必须写行内注释，说明「为什么这么做」
- ✅ **Controller 层**：使用 `@Tag`/`@Operation` 注解写接口文档
- ❌ 禁止写无意义注释（如 `i++; // i 加 1`）
