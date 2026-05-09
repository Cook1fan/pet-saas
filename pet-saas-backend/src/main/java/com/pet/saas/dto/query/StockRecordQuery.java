package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 库存流水查询条件
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "库存流水查询条件")
public class StockRecordQuery extends BasePageQuery {

    @Schema(description = "SKU ID")
    private Long skuId;

    @Schema(description = "商品条码")
    private String barcode;

    @Schema(description = "变动类型")
    private Integer type;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
