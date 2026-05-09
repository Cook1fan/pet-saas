package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品变更日志查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品变更日志查询条件")
public class GoodsChangeLogQuery extends BasePageQuery {

    @Schema(description = "数据类型：1-goods商品，2-goods_sku商品规格")
    private Integer dataType;

    @Schema(description = "数据ID")
    private Long dataId;

    @Schema(description = "批次号")
    private String batchNo;
}
