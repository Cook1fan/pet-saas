# 商品SKU编码与条形码设计方案

> **文档导航：** [返回总览](./00-总览.md) | [核心业务逻辑](./03-核心业务逻辑.md)

---

## 1. 问题背景

### 1.1 现有问题
- `goods_sku` 表虽预留了 `barcode` 字段，但未明确使用规范
- 缺少系统内部 SKU 编码，无法唯一标识无条形码商品
- 未明确条形码与 SKU 的对应关系

### 1.2 业务场景
1. **实物商品录入**：店家使用扫码枪扫描商品条形码录入
2. **服务类商品**：无条形码，需要系统自动生成唯一标识
3. **开单收银**：支持扫码枪快速识别商品，也支持手动选择

---

## 2. 设计方案

### 2.1 双码制设计

| 字段 | 位置 | 必需 | 说明 | 生成方式 | 索引 |
|-----|------|------|------|---------|------|
| `sku_code` | `goods_sku` | ✅ | 系统内部 SKU 编码 | 系统自动生成 | UNIQUE (tenant_id, sku_code) |
| `barcode` | `goods_sku` | ❌ | 商品条形码（扫码枪用） | 商家录入/扫码 | INDEX (tenant_id, barcode) |

### 2.2 SKU 编码生成规则

使用 **雪花算法** 生成 19 位纯数字 ID，作为 `sku_code`。

**优点：**
- 分布式环境下保证全局唯一
- 纯数字，便于收银员手动输入
- 无序，不暴露业务信息

**格式示例：** `1774567890123456789`

---

## 3. 数据库设计

### 3.1 表结构变更

```sql
-- 给 goods_sku 表添加内部 SKU 编码字段
ALTER TABLE goods_sku
    ADD COLUMN sku_code VARCHAR(32) NOT NULL COMMENT '系统内部SKU编码' AFTER goods_id;

-- 添加唯一索引（同一租户下 sku_code 唯一）
ALTER TABLE goods_sku
    ADD UNIQUE KEY uk_tenant_sku_code (tenant_id, sku_code);

-- 给 barcode 字段添加普通索引（用于扫码查询）
ALTER TABLE goods_sku
    ADD INDEX idx_barcode (tenant_id, barcode);

-- 可选：给 barcode 添加唯一约束（防止同一租户下重复条码）
-- 根据业务需求决定是否添加：如果允许不同 SKU 使用相同条码，则不加
-- ALTER TABLE goods_sku
--     ADD UNIQUE KEY uk_tenant_barcode (tenant_id, barcode);
```

### 3.2 字段说明

| 字段 | 类型 | 约束 | 说明 |
|-----|------|------|------|
| `sku_code` | VARCHAR(32) | NOT NULL | 系统内部 SKU 编码，创建时自动生成，不可修改 |
| `barcode` | VARCHAR(50) | NULL | 商品条形码（EAN-13、UPC、Code 128 等），商家可手动录入或扫码枪录入 |

---

## 4. 核心业务逻辑

### 4.1 SKU 创建流程

```java
// 1. 生成 sku_code（使用雪花算法）
String skuCode = snowflake.nextIdStr();

// 2. 设置到 GoodsSku 对象
GoodsSku sku = new GoodsSku();
sku.setSkuCode(skuCode);
sku.setBarcode(req.getBarcode()); // 可能为 null
// ... 其他字段

// 3. 保存
goodsSkuService.save(sku);
```

### 4.2 扫码查询流程

```java
/**
 * 根据条码查询 SKU（优先查 barcode，查不到再查 sku_code）
 */
public GoodsSku getByCode(String code, Long tenantId) {
    // 1. 先按 barcode 查询
    GoodsSku sku = goodsSkuMapper.selectOne(
        new LambdaQueryWrapper<GoodsSku>()
            .eq(GoodsSku::getTenantId, tenantId)
            .eq(GoodsSku::getBarcode, code)
            .eq(GoodsSku::getStatus, 1)
            .last("LIMIT 1")
    );

    if (sku != null) {
        return sku;
    }

    // 2. 再按 sku_code 查询
    return goodsSkuMapper.selectOne(
        new LambdaQueryWrapper<GoodsSku>()
            .eq(GoodsSku::getTenantId, tenantId)
            .eq(GoodsSku::getSkuCode, code)
            .eq(GoodsSku::getStatus, 1)
            .last("LIMIT 1")
    );
}
```

### 4.3 开单收银场景

| 用户操作 | 系统行为 |
|---------|---------|
| 扫码枪扫描商品条码 | 用 `barcode` 字段查询，找到对应 SKU 加入购物车 |
| 手动输入 SKU 编码 | 用 `sku_code` 字段查询，找到对应 SKU 加入购物车 |
| 手动选择商品 | 展示商品列表，选择后取对应 SKU 的 `sku_code` |

---

## 5. 代码实现

### 5.1 实体类修改

**GoodsSku.java**：
```java
/**
 * 系统内部SKU编码
 */
private String skuCode;

/**
 * 商品条码（扫码枪用）
 */
private String barcode;
```

### 5.2 DTO 修改

**GoodsSkuSaveReq.java**：
```java
/**
 * 商品条码（扫码枪录入时传）
 */
private String barcode;
```

**GoodsSkuVO.java**：
```java
/**
 * 系统内部SKU编码
 */
private String skuCode;

/**
 * 商品条码
 */
private String barcode;
```

### 5.3 Service 层逻辑

**GoodsSkuService.java**：
```java
/**
 * 根据条码查询 SKU（优先查 barcode，查不到再查 sku_code）
 */
GoodsSku getByCode(String code, Long tenantId);
```

---

## 6. API 接口

### 6.1 扫码查询接口

```java
@Tag(name = "商品管理")
@RestController
@RequestMapping("/pc/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsSkuService goodsSkuService;

    @Operation(summary = "扫码查询商品SKU")
    @GetMapping("/sku/scan/{code}")
    public R<GoodsSkuVO> scanSku(@PathVariable String code) {
        Long tenantId = StpKit.SHOP.getTenantId();
        GoodsSku sku = goodsSkuService.getByCode(code, tenantId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }
        return R.ok(BeanConverter.convert(sku, GoodsSkuVO.class));
    }
}
```

---

## 7. 数据迁移脚本

### 7.1 给现有数据生成 sku_code

```sql
-- 假设使用 Java 代码批量生成，这里仅示意
-- 需要使用雪花算法为每条记录生成唯一 sku_code

-- 示例（实际需在代码中执行）：
-- UPDATE goods_sku SET sku_code = 雪花算法生成值 WHERE sku_code IS NULL;
```

---

## 8. 注意事项

### 8.1 条形码重复问题

- **方案 A**：允许重复（不设唯一索引）
  - 适用场景：同一商品不同规格可能误用相同条码，或者商家自己打印的内码
  - 查询时返回第一条，或提示"找到多个商品"

- **方案 B**：禁止重复（设唯一索引）
  - 适用场景：严格管理，正规商品条码
  - 录入时提示"条码已存在"

**建议**：先不设唯一索引，只建普通索引，后续根据业务反馈调整。

### 8.2 sku_code 不可修改

- `sku_code` 创建后永久不变，作为系统内部唯一标识
- `barcode` 可以修改，商家可能需要重新绑定条码

### 8.3 条码格式

- 支持多种格式：EAN-13（13位）、EAN-8（8位）、UPC-A（12位）、Code 128（任意长度）
- 数据库字段 `VARCHAR(50)` 足够容纳

---

## 9. 后续优化

### 9.1 条码解析
- 支持 EAN-13 校验位验证
- 支持 GS1 条码解析（包含商品信息、批号、有效期等）

### 9.2 条码打印
- 支持生成并打印 SKU 内部条码
- 支持打印价格标签

---

**文档版本**：V1.0
**创建日期**：2026-04-02
**作者**：Claude Code
