# 商品库存管理与SKU优化设计方案

> **文档导航：** [返回总览](./00-总览.md) | [核心业务逻辑](./03-核心业务逻辑.md)

---

## 1. 问题背景

### 1.1 现有问题分析

#### 问题一：SKU "全删全插"策略导致数据膨胀
**当前实现（`GoodsServiceImpl.saveGoods()` 第 147 行）：**
```java
// SKU采用"全删全插"策略：比对比更新更简单，且不会出现残留数据
goodsSkuService.deleteByGoodsId(goods.getId());
```

**痛点：**
- 每次编辑商品，所有 SKU 都被逻辑删除再重新插入
- 只是修改价格或库存等单个属性，也会产生新的 SKU 记录
- `goods_sku` 表会快速膨胀，历史数据无意义
- SKU ID 频繁变化，影响关联表（如 `order_item`、`stock_record`）的关联性

#### 问题二：库存字段缺乏业务逻辑
**当前现状：**
- `goods_sku.stock` 字段可以随意修改，想写多少就是多少
- 没有库存变动的审批流程
- 无法追溯库存来源（是采购入库？还是手动调整？）

#### 问题三：缺少库存变动快照记录
**当前现状：**
- 虽有 `stock_record` 表，但结构过于简单
- 没有记录变动前后的库存快照
- 没有库存变动类型的细分（采购入库、销售出库、盘点调整等）
- 无法审计库存变化轨迹

#### 问题四：库存变动缺少并发控制
**当前风险：**
- 多个用户同时操作同一 SKU 库存，可能导致数据不一致
- 高并发场景下（如秒杀活动），可能出现超卖

---

## 2. 设计目标

1. **SKU 增量更新**：修改商品时，只更新变化的 SKU，保留不变的 SKU ID
2. **库存操作规范化**：禁止直接修改库存字段，必须通过库存变动流程
3. **库存变动可追溯**：完整记录每次库存变动的快照、类型、操作人
4. **并发安全**：使用分布式锁保证库存变动的线程安全
5. **数据变更日志**：使用日志表记录商品和 SKU 的变更历史，而非逻辑删除

---

## 3. 总体架构设计

### 3.1 核心模块划分

| 模块 | 职责 | 说明 |
|-----|------|------|
| SKU 管理 | 增量更新 SKU | 对比差异，只更新变化的字段 |
| 库存操作 | 库存出入库 | 统一入口，禁止直接修改 stock 字段 |
| 库存流水 | 记录变动快照 | 每次变动记录前后库存、类型、关联单据 |
| 变更日志 | 记录商品/SKU 变更 | 记录字段级变更历史 |
| 并发控制 | 分布式锁 | 库存变动、SKU 编辑加锁 |

### 3.2 库存变动类型

| 类型码 | 类型名称 | 说明 | 库存方向 |
|-------|---------|------|---------|
| 1 | 采购入库 | 供应商采购到货 | + |
| 2 | 手动入库 | 人工手动增加库存 | + |
| 3 | 销售出库 | 开单收银销售出库 | - |
| 4 | 手动出库 | 人工手动减少库存 | - |
| 5 | 盘点调整 | 库存盘点盈亏调整 | ± |
| 6 | 退货入库 | 顾客退货退回 | + |
| 7 | 领用出库 | 内部领用出库 | - |

---

## 4. 数据库设计

### 4.1 表结构变更

#### 4.1.1 扩建 stock_record 表（库存流水表）

**原表问题：**
- 缺少变动前后库存快照
- 缺少关联单据号
- 缺少变动类型细分
- 缺少批次号（用于追溯）

**新表结构：**

```sql
CREATE TABLE IF NOT EXISTS stock_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',
    sku_id BIGINT NOT NULL COMMENT '商品 SKU ID',

    -- 库存变动核心信息
    type TINYINT NOT NULL COMMENT '变动类型：1-采购入库，2-手动入库，3-销售出库，4-手动出库，5-盘点调整，6-退货入库，7-领用出库',
    change_num INT NOT NULL COMMENT '变动数量（正数入库，负数出库）',
    before_stock INT NOT NULL COMMENT '变动前库存',
    after_stock INT NOT NULL COMMENT '变动后库存',

    -- 关联信息
    batch_no VARCHAR(32) COMMENT '批次号（同一批次操作共享）',
    related_type VARCHAR(50) COMMENT '关联单据类型：order-订单，purchase-采购单，check-盘点单',
    related_id BIGINT COMMENT '关联单据 ID',
    related_no VARCHAR(64) COMMENT '关联单据号',

    -- 审核信息
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变动时间',
    create_user BIGINT NOT NULL COMMENT '操作人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

    INDEX idx_tenant_id (tenant_id),
    INDEX idx_sku_id (sku_id),
    INDEX idx_type (type),
    INDEX idx_batch_no (batch_no),
    INDEX idx_related (related_type, related_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';
```

#### 4.1.2 新增 goods_change_log 表（商品变更日志表）

用于记录商品和 SKU 的字段级变更历史，替代原有的"逻辑删除 SKU"方案。

```sql
CREATE TABLE IF NOT EXISTS goods_change_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    tenant_id BIGINT NOT NULL COMMENT '租户 ID',

    -- 变更主体
    data_type TINYINT NOT NULL COMMENT '数据类型：1-goods商品，2-goods_sku商品规格',
    data_id BIGINT NOT NULL COMMENT '数据 ID（goods.id 或 goods_sku.id）',

    -- 变更内容
    change_type TINYINT NOT NULL COMMENT '变更类型：1-新增，2-修改，3-删除',
    field_name VARCHAR(50) COMMENT '变更字段名（修改时有值）',
    before_value TEXT COMMENT '变更前值',
    after_value TEXT COMMENT '变更后值',

    -- 变更批次
    batch_no VARCHAR(32) COMMENT '批次号（同一次保存操作共享）',

    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    create_user BIGINT NOT NULL COMMENT '操作人 ID',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_user BIGINT,
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',

    INDEX idx_tenant_id (tenant_id),
    INDEX idx_data (data_type, data_id),
    INDEX idx_batch_no (batch_no),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品变更日志表';
```

#### 4.1.3 goods_sku 表补充字段

```sql
-- 给 goods_sku 表增加乐观锁版本号（用于并发控制）
ALTER TABLE goods_sku ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER update_user;
```

### 4.2 数据库迁移脚本

```sql
-- 1. 扩建 stock_record 表
ALTER TABLE stock_record
    ADD COLUMN before_stock INT NOT NULL DEFAULT 0 COMMENT '变动前库存' AFTER change_num,
    ADD COLUMN after_stock INT NOT NULL DEFAULT 0 COMMENT '变动后库存' AFTER before_stock,
    ADD COLUMN batch_no VARCHAR(32) COMMENT '批次号' AFTER after_stock,
    ADD COLUMN related_type VARCHAR(50) COMMENT '关联单据类型' AFTER batch_no,
    ADD COLUMN related_id BIGINT COMMENT '关联单据 ID' AFTER related_type,
    ADD COLUMN related_no VARCHAR(64) COMMENT '关联单据号' AFTER related_id;

-- 修改 type 字段注释
ALTER TABLE stock_record
    MODIFY COLUMN type TINYINT NOT NULL COMMENT '变动类型：1-采购入库，2-手动入库，3-销售出库，4-手动出库，5-盘点调整，6-退货入库，7-领用出库';

-- 2. 创建 goods_change_log 表
-- （执行上面的 CREATE TABLE 语句）

-- 3. goods_sku 表增加 version 字段
ALTER TABLE goods_sku ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER update_user;
```

---

## 5. 核心功能详细设计

### 5.1 SKU 增量更新方案

#### 5.1.1 设计思路

废弃"全删全插"策略，改为"差异对比更新"：

1. **按唯一标识匹配 SKU**：前端传入 SKU 时，有 ID 的是更新，无 ID 的是新增
2. **删除不再存在的 SKU**：数据库中有但前端未传的 SKU，标记为删除（逻辑删除）
3. **记录变更日志**：所有变更（新增/修改/删除）都记录到 `goods_change_log`

#### 5.1.2 SKU 匹配规则

| 前端传入 SKU | 数据库 SKU | 操作 |
|-------------|-----------|------|
| 有 id | 存在 | 更新字段（只更新变化的字段） |
| 有 id | 不存在 | 报错（SKU 不存在） |
| 无 id | - | 新增 SKU |
| - | 存在且前端未传 | 逻辑删除 |

#### 5.1.3 变更日志记录策略

对于每个 SKU 的变更，记录字段级别的变更：

```
示例：修改 SKU 价格
- data_type: 2 (goods_sku)
- data_id: 100
- change_type: 2 (修改)
- field_name: "price"
- before_value: "99.00"
- after_value: "129.00"
```

#### 5.1.4 核心代码流程

```java
@Transactional(rollbackFor = Exception.class)
public void saveGoods(GoodsSaveReq req, Long tenantId, Long userId) {
    // 1. 获取分布式锁（同现有逻辑）
    RLock lock = getGoodsEditLock(tenantId, req.getId());

    try {
        // 2. 生成变更批次号
        String batchNo = generateBatchNo();

        // 3. 保存商品基础信息（同现有逻辑）
        Goods goods = saveGoodsBaseInfo(req, tenantId, userId, batchNo);

        // 4. 增量更新 SKU（新逻辑）
        saveSkuIncremental(goods.getId(), req.getSkuList(), tenantId, userId, batchNo);

    } finally {
        unlock(lock);
    }
}

/**
 * 增量更新 SKU
 */
private void saveSkuIncremental(Long goodsId, List<GoodsSkuSaveReq> skuReqList,
                                  Long tenantId, Long userId, String batchNo) {
    // 1. 查询数据库现有 SKU
    List<GoodsSku> existSkuList = goodsSkuService.listByGoodsId(goodsId);
    Map<Long, GoodsSku> existSkuMap = existSkuList.stream()
        .collect(Collectors.toMap(GoodsSku::getId, Function.identity()));

    // 2. 处理前端传入的 SKU（更新或新增）
    Set<Long> frontSkuIds = new HashSet<>();
    for (GoodsSkuSaveReq skuReq : skuReqList) {
        if (skuReq.getId() != null) {
            // 更新现有 SKU
            frontSkuIds.add(skuReq.getId());
            GoodsSku existSku = existSkuMap.get(skuReq.getId());
            if (existSku == null) {
                throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
            }
            updateSkuIfChanged(existSku, skuReq, userId, batchNo);
        } else {
            // 新增 SKU
            GoodsSku newSku = createSku(skuReq, goodsId, tenantId, userId, batchNo);
            frontSkuIds.add(newSku.getId());
        }
    }

    // 3. 删除数据库中有但前端未传的 SKU
    for (GoodsSku existSku : existSkuList) {
        if (!frontSkuIds.contains(existSku.getId())) {
            deleteSku(existSku, userId, batchNo);
        }
    }
}

/**
 * 对比并更新 SKU（只更新变化的字段）
 */
private void updateSkuIfChanged(GoodsSku existSku, GoodsSkuSaveReq skuReq,
                                  Long userId, String batchNo) {
    List<FieldChange> changes = new ArrayList<>();

    // 对比每个字段
    if (!Objects.equals(existSku.getSpecName(), skuReq.getSpecName())) {
        changes.add(new FieldChange("specName", existSku.getSpecName(), skuReq.getSpecName()));
        existSku.setSpecName(skuReq.getSpecName());
    }
    if (!Objects.equals(existSku.getSpecValue(), skuReq.getSpecValue())) {
        changes.add(new FieldChange("specValue", existSku.getSpecValue(), skuReq.getSpecValue()));
        existSku.setSpecValue(skuReq.getSpecValue());
    }
    if (!Objects.equals(existSku.getPrice(), skuReq.getPrice())) {
        changes.add(new FieldChange("price", existSku.getPrice(), skuReq.getPrice()));
        existSku.setPrice(skuReq.getPrice());
    }
    // ... 其他字段对比

    // 库存字段不允许直接修改，忽略！

    if (!changes.isEmpty()) {
        // 有变化才更新
        existSku.setUpdateUser(userId);
        existSku.setVersion(existSku.getVersion() + 1);
        goodsSkuService.updateById(existSku);

        // 记录变更日志
        for (FieldChange change : changes) {
            goodsChangeLogService.logChange(2, existSku.getId(), 2,
                change.fieldName, change.beforeValue, change.afterValue, batchNo, userId);
        }
    }
}
```

### 5.2 库存管理方案

#### 5.2.1 核心原则

**❌ 禁止：**
- 直接通过 `goods_sku.stock` 字段修改库存
- 在商品编辑页面修改库存
- 任何绕过库存流水的库存变动

**✅ 必须：**
- 所有库存变动通过 `StockService` 的统一入口
- 记录完整的库存快照（变动前、变动后）
- 指定库存变动类型
- 获取分布式锁保证并发安全

#### 5.2.2 库存服务接口设计

```java
public interface StockService {

    /**
     * 入库操作
     *
     * @param req 入库请求
     * @return 库存流水记录
     */
    StockRecord inStock(StockInReq req);

    /**
     * 出库操作
     *
     * @param req 出库请求
     * @return 库存流水记录
     */
    StockRecord outStock(StockOutReq req);

    /**
     * 批量入库（同一批次）
     *
     * @param reqList 入库请求列表
     * @return 库存流水记录列表
     */
    List<StockRecord> batchInStock(List<StockInReq> reqList);

    /**
     * 批量出库（同一批次）
     *
     * @param reqList 出库请求列表
     * @return 库存流水记录列表
     */
    List<StockRecord> batchOutStock(List<StockOutReq> req);

    /**
     * 盘点调整
     *
     * @param req 盘点调整请求
     * @return 库存流水记录
     */
    StockRecord adjustStock(StockAdjustReq req);

    /**
     * 查询 SKU 库存流水
     *
     * @param query 查询条件
     * @return 库存流水分页
     */
    Page<StockRecord> listStockRecord(StockRecordQuery query);
}
```

#### 5.2.3 DTO 设计

```java
@Data
public class StockInReq {
    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    @NotNull(message = "变动类型不能为空")
    private Integer type; // 1-采购入库，2-手动入库，6-退货入库

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "入库数量必须大于0")
    private Integer num;

    private String relatedType; // 关联单据类型
    private Long relatedId;     // 关联单据 ID
    private String relatedNo;   // 关联单据号
    private String remark;      // 备注
}

@Data
public class StockOutReq {
    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    @NotNull(message = "变动类型不能为空")
    private Integer type; // 3-销售出库，4-手动出库，7-领用出库

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "出库数量必须大于0")
    private Integer num;

    private String relatedType;
    private Long relatedId;
    private String relatedNo;
    private String remark;
}

@Data
public class StockAdjustReq {
    @NotNull(message = "SKU ID 不能为空")
    private Long skuId;

    @NotNull(message = "盘点后库存不能为空")
    private Integer targetStock; // 盘点后的实际库存

    private String remark;
}
```

#### 5.2.4 库存变动核心实现

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final GoodsSkuService goodsSkuService;
    private final StockRecordMapper stockRecordMapper;
    private final RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord inStock(StockInReq req, Long tenantId, Long userId) {
        return doStockChange(req.getSkuId(), req.getType(), req.getNum(),
            req.getRelatedType(), req.getRelatedId(), req.getRelatedNo(),
            req.getRemark(), tenantId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord outStock(StockOutReq req, Long tenantId, Long userId) {
        // 出库数量取负数
        return doStockChange(req.getSkuId(), req.getType(), -req.getNum(),
            req.getRelatedType(), req.getRelatedId(), req.getRelatedNo(),
            req.getRemark(), tenantId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord adjustStock(StockAdjustReq req, Long tenantId, Long userId) {
        GoodsSku sku = goodsSkuService.getSku(req.getSkuId());
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }
        // 计算调整数量
        int changeNum = req.getTargetStock() - sku.getStock();
        if (changeNum == 0) {
            throw new BusinessException(ErrorCode.STOCK_NO_CHANGE);
        }
        return doStockChange(req.getSkuId(), 5, changeNum,
            null, null, null, req.getRemark(), tenantId, userId);
    }

    /**
     * 统一库存变动处理
     */
    private StockRecord doStockChange(Long skuId, Integer type, int changeNum,
                                        String relatedType, Long relatedId, String relatedNo,
                                        String remark, Long tenantId, Long userId) {
        // 1. 获取 SKU 级别的分布式锁
        String lockKey = String.format(RedisKeyConstants.STOCK_CHANGE_LOCK_KEY, tenantId, skuId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.STOCK_BEING_CHANGED);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }

        try {
            // 2. 查询 SKU（加锁后重新查询，确保数据最新）
            GoodsSku sku = goodsSkuService.getSku(skuId);
            if (sku == null) {
                throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
            }
            if (sku.getStock() == -1) {
                throw new BusinessException(ErrorCode.SERVICE_GOODS_NO_STOCK);
            }

            // 3. 校验出库库存是否足够
            int beforeStock = sku.getStock();
            if (changeNum < 0 && beforeStock + changeNum < 0) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
            }

            // 4. 计算变动后库存
            int afterStock = beforeStock + changeNum;

            // 5. 更新 SKU 库存（使用 CAS + 乐观锁）
            boolean updated = goodsSkuService.updateStock(skuId, beforeStock, afterStock, sku.getVersion());
            if (!updated) {
                throw new BusinessException(ErrorCode.STOCK_VERSION_MISMATCH);
            }

            // 6. 生成批次号
            String batchNo = generateBatchNo();

            // 7. 记录库存流水
            StockRecord record = new StockRecord();
            record.setTenantId(tenantId);
            record.setSkuId(skuId);
            record.setType(type);
            record.setChangeNum(changeNum);
            record.setBeforeStock(beforeStock);
            record.setAfterStock(afterStock);
            record.setBatchNo(batchNo);
            record.setRelatedType(relatedType);
            record.setRelatedId(relatedId);
            record.setRelatedNo(relatedNo);
            record.setRemark(remark);
            record.setCreateUser(userId);
            record.setUpdateUser(userId);
            stockRecordMapper.insert(record);

            log.info("库存变动成功：skuId={}, type={}, before={}, change={}, after={}",
                skuId, type, beforeStock, changeNum, afterStock);

            return record;

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
```

#### 5.2.5 GoodsSkuService.updateStock 实现

```java
/**
 * 使用 CAS 更新库存（配合乐观锁）
 */
@Update("UPDATE goods_sku SET stock = #{afterStock}, version = version + 1 " +
        "WHERE id = #{skuId} AND stock = #{beforeStock} AND version = #{version} AND is_deleted = 0")
int updateStock(@Param("skuId") Long skuId,
                @Param("beforeStock") int beforeStock,
                @Param("afterStock") int afterStock,
                @Param("version") Integer version);
```

### 5.3 并发控制方案

### 5.3.1 锁的粒度设计

| 操作 | 锁粒度 | 锁 Key 格式 | 说明 |
|-----|-------|------------|------|
| 商品编辑 | 商品级 | `goods:edit:lock:{tenantId}:{goodsId}` | 防止并发编辑同一商品 |
| 库存变动 | SKU 级 | `stock:change:lock:{tenantId}:{skuId}` | 防止并发修改同一 SKU 库存 |

### 5.3.2 双层锁策略

对于同时涉及商品编辑和库存变动的场景，采用**分层加锁**：

```
先获取商品级锁
  ↓
再获取 SKU 级锁（如果需要）
  ↓
执行业务逻辑
  ↓
释放 SKU 级锁
  ↓
释放商品级锁
```

### 5.3.3 Redis Key 设计

在 `RedisKeyConstants` 中新增：

```java
/**
 * 库存变动锁
 * 参数：tenantId, skuId
 */
String STOCK_CHANGE_LOCK_KEY = "stock:change:lock:%d:%d";
```

### 5.4 变更日志服务设计

### 5.4.1 服务接口

```java
public interface GoodsChangeLogService {

    /**
     * 记录单字段变更
     */
    void logChange(Integer dataType, Long dataId, Integer changeType,
                   String fieldName, Object beforeValue, Object afterValue,
                   String batchNo, Long userId);

    /**
     * 记录新增（记录所有字段）
     */
    void logCreate(Integer dataType, Long dataId, Object entity,
                   String batchNo, Long userId);

    /**
     * 记录删除（记录所有字段）
     */
    void logDelete(Integer dataType, Long dataId, Object entity,
                   String batchNo, Long userId);

    /**
     * 查询变更历史
     */
    List<GoodsChangeLog> listByBatchNo(String batchNo);

    /**
     * 查询数据变更历史
     */
    List<GoodsChangeLog> listByData(Integer dataType, Long dataId);
}
```

---

## 6. API 接口设计

### 6.1 库存操作接口

```java
@Tag(name = "库存管理")
@RestController
@RequestMapping("/pc/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @Operation(summary = "手动入库")
    @PostMapping("/in")
    public R<StockRecordVO> manualInStock(@Valid @RequestBody StockManualInReq req) {
        Long tenantId = StpKit.SHOP.getTenantId();
        Long userId = StpKit.SHOP.getUserId();

        StockInReq inReq = new StockInReq();
        inReq.setSkuId(req.getSkuId());
        inReq.setType(2); // 手动入库
        inReq.setNum(req.getNum());
        inReq.setRemark(req.getRemark());

        StockRecord record = stockService.inStock(inReq, tenantId, userId);
        return R.ok(BeanConverter.convert(record, StockRecordVO.class));
    }

    @Operation(summary = "手动出库")
    @PostMapping("/out")
    public R<StockRecordVO> manualOutStock(@Valid @RequestBody StockManualOutReq req) {
        Long tenantId = StpKit.SHOP.getTenantId();
        Long userId = StpKit.SHOP.getUserId();

        StockOutReq outReq = new StockOutReq();
        outReq.setSkuId(req.getSkuId());
        outReq.setType(4); // 手动出库
        outReq.setNum(req.getNum());
        outReq.setRemark(req.getRemark());

        StockRecord record = stockService.outStock(outReq, tenantId, userId);
        return R.ok(BeanConverter.convert(record, StockRecordVO.class));
    }

    @Operation(summary = "盘点调整")
    @PostMapping("/adjust")
    public R<StockRecordVO> adjustStock(@Valid @RequestBody StockAdjustReq req) {
        Long tenantId = StpKit.SHOP.getTenantId();
        Long userId = StpKit.SHOP.getUserId();

        StockRecord record = stockService.adjustStock(req, tenantId, userId);
        return R.ok(BeanConverter.convert(record, StockRecordVO.class));
    }

    @Operation(summary = "库存流水列表")
    @GetMapping("/records")
    public R<PageResult<StockRecordVO>> listStockRecords(@Valid StockRecordQuery query) {
        Long tenantId = StpKit.SHOP.getTenantId();
        Page<StockRecord> page = stockService.listStockRecord(query, tenantId);
        return R.ok(PageResult.of(page, StockRecordVO.class));
    }

    @Operation(summary = "商品变更历史")
    @GetMapping("/change-log/{dataType}/{dataId}")
    public R<List<GoodsChangeLogVO>> listChangeLog(
            @PathVariable Integer dataType,
            @PathVariable Long dataId) {
        List<GoodsChangeLog> logs = goodsChangeLogService.listByData(dataType, dataId);
        return R.ok(BeanConverter.convertList(logs, GoodsChangeLogVO.class));
    }
}
```

### 6.2 前端适配说明

#### 6.2.1 商品编辑页面改动

**移除：**
- ❌ 库存输入框（禁止在商品编辑时修改库存）

**新增：**
- ✅ 变更历史查看入口

#### 6.2.2 新增库存管理页面

功能：
1. 库存列表展示（含当前库存、预警状态）
2. 手动入库按钮
3. 手动出库按钮
4. 盘点调整按钮
5. 库存流水记录查看

---

## 7. 错误码设计

在 `ErrorCode.java` 中新增：

```java
// SKU 相关错误 (40020-40039)
SKU_NOT_FOUND(40020, "商品规格不存在"),
SERVICE_GOODS_NO_STOCK(40021, "服务类商品无库存概念"),

// 库存相关错误 (40040-40059)
STOCK_NOT_ENOUGH(40040, "库存不足"),
STOCK_BEING_CHANGED(40041, "库存正在变动中，请稍后再试"),
STOCK_VERSION_MISMATCH(40042, "库存已变动，请刷新后重试"),
STOCK_NO_CHANGE(40043, "库存未发生变化"),
```

---

## 8. 实施计划

### 8.1 实施步骤

| 阶段 | 任务 | 预计工作量 |
|-----|------|-----------|
| 阶段一 | 数据库表结构变更 | 0.5d |
| 阶段二 | SKU 增量更新改造 | 1d |
| 阶段三 | 库存服务开发 | 1.5d |
| 阶段四 | 变更日志服务开发 | 0.5d |
| 阶段五 | API 接口开发 | 0.5d |
| 阶段六 | 单元测试 | 1d |
| 阶段七 | 联调测试 | 0.5d |
| **合计** | | **6d** |

### 8.2 发布顺序

1. 执行数据库变更脚本
2. 发布后端代码
3. 前端适配（移除商品编辑页的库存输入，新增库存管理页）

---

## 9. 测试方案

### 9.1 单元测试

| 测试场景 | 预期结果 |
|---------|---------|
| SKU 增量更新-修改价格 | 只更新 price 字段，记录变更日志，SKU ID 不变 |
| SKU 增量更新-新增 SKU | 插入新 SKU，记录新增日志 |
| SKU 增量更新-删除 SKU | 逻辑删除 SKU，记录删除日志 |
| 手动入库 | 库存增加，记录流水，快照正确 |
| 手动出库-库存足够 | 库存减少，记录流水，快照正确 |
| 手动出库-库存不足 | 报错，库存不变 |
| 盘点调整 | 库存调整为目标值，记录流水 |
| 并发库存变动 | 只有一个成功，其他提示"库存正在变动" |

### 9.2 并发测试

使用 JMeter 模拟 100 个线程同时出库同一 SKU（库存 100，每次出库 1）：

**预期结果：**
- 100 次出库全部成功
- 最终库存为 0
- 流水记录 100 条
- 无超卖现象

---

## 10. 后续优化方向

### 10.1 采购管理模块

- 采购单申请、审批流程
- 采购入库与采购单关联
- 供应商管理

### 10.2 库存预警通知

- 库存低于预警值时推送通知
- 支持微信、站内信等多种通知方式

### 10.3 库存报表

- 库存周转率分析
- 出入库汇总报表
- 库存盘点报表

---

## 11. 参考资料

- [MyBatis-Plus 乐观锁插件](https://baomidou.com/guides/interceptor/)
- [Redisson 分布式锁](https://github.com/redisson/redisson/wiki/8.-%E5%88%86%E5%B8%83%E5%BC%8F%E5%BC%8F)
- 项目现有实现：`ActivityServiceImpl.createActivityOrder()`

---

**文档版本**：V1.0
**创建日期**：2026-04-01
**作者**：Claude Code
