package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品变更日志表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_change_log")
public class GoodsChangeLog extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 数据类型：1-goods商品，2-goods_sku商品规格
     */
    private Integer dataType;

    /**
     * 数据ID（goods.id 或 goods_sku.id）
     */
    private Long dataId;

    /**
     * 变更类型：1-新增，2-修改，3-删除
     */
    private Integer changeType;

    /**
     * 变更字段名（修改时有值，已废弃，使用 beforeValue/afterValue 的 JSON）
     * @deprecated
     */
    @Deprecated
    private String fieldName;

    /**
     * 变更前值（JSON格式，仅包含变更字段：{"field1": "value1", "field2": "value2"}）
     */
    private String beforeValue;

    /**
     * 变更后值（JSON格式，仅包含变更字段：{"field1": "value1", "field2": "value2"}）
     */
    private String afterValue;

    /**
     * 批次号（同一次保存操作共享）
     */
    private String batchNo;

    /**
     * 备注
     */
    private String remark;
}
