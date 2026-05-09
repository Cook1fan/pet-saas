package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 库存流水表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("stock_record")
public class StockRecord extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 商品SKU ID
     */
    private Long skuId;

    /**
     * 变动类型：1-采购入库，2-手动入库，3-销售出库，4-手动出库，5-盘点调整，6-退货入库，7-领用出库
     */
    private Integer type;

    /**
     * 变动数量（正数入库，负数出库）
     */
    private Integer changeNum;

    /**
     * 变动前库存
     */
    private Integer beforeStock;

    /**
     * 变动后库存
     */
    private Integer afterStock;

    /**
     * 批次号（同一批次操作共享）
     */
    private String batchNo;

    /**
     * 关联单据类型：order-订单，purchase-采购单，check-盘点单
     */
    private String relatedType;

    /**
     * 关联单据 ID
     */
    private Long relatedId;

    /**
     * 关联单据号
     */
    private String relatedNo;

    /**
     * 备注
     */
    private String remark;
}
