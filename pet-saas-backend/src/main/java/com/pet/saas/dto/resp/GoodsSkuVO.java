package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "商品SKU响应")
public class GoodsSkuVO {

    @Schema(description = "SKU ID")
    private Long id;

    @Schema(description = "商品ID")
    private Long goodsId;

    @Schema(description = "系统内部SKU编码")
    private String skuCode;

    @Schema(description = "规格名称")
    private String specName;

    @Schema(description = "规格值")
    private String specValue;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "成本价")
    private BigDecimal costPrice;

    @Schema(description = "实际库存")
    private Integer stock;

    @Schema(description = "是否无限库存：0-否，1-是")
    private Integer isUnlimitedStock;

    @Schema(description = "占用库存（在途库存）")
    private Integer reservedStock;

    @Schema(description = "可用库存（计算字段：stock - reservedStock）")
    private Integer availableStock;

    @Schema(description = "商品条码")
    private String barcode;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
