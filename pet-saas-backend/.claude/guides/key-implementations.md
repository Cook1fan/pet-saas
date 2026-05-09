# 关键模块实现

---

## 多租户隔离配置

**文件**: `config/MybatisPlusConfig.java`

```java
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 多租户插件
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
            @Override
            public Expression getTenantId() {
                Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
                return new LongValue(tenantId == null ? 0 : tenantId);
            }
            @Override
            public String getTenantIdColumn() {
                return "tenant_id";
            }
            @Override
            public boolean ignoreTable(String tableName) {
                List<String> ignoreTables = Arrays.asList("sys_platform_admin", "sys_config");
                return ignoreTables.contains(tableName) || StpUtil.hasRole("platform_admin");
            }
        }));
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

## Sa-Token 多端登录配置

**文件**: `application.yml`

```yaml
sa-token:
  token-name: satoken
  timeout: 2592000  # 30天
  is-concurrent: true
  token-prefix:
    pc: pc_
    merchant: merchant_
    user: user_
```

## 统一响应封装 R<T>

```java
@Data
public class R<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> R<T> ok(T data) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> R<T> error(String message) {
        R<T> r = new R<>();
        r.setCode(500);
        r.setMessage(message);
        return r;
    }
}
```
