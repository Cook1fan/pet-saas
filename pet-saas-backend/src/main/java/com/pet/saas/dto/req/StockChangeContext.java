package com.pet.saas.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存变动上下文DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockChangeContext {

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 变动类型
     */
    private Integer type;

    /**
     * 变动数量（正数入库，负数出库）
     */
    private Integer changeNum;

    /**
     * 关联业务类型
     */
    private String relatedType;

    /**
     * 关联业务ID
     */
    private Long relatedId;

    /**
     * 关联业务单号
     */
    private String relatedNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 批次号（可选，不传则自动生成）
     */
    private String batchNo;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 操作人ID
     */
    private Long userId;
}
