package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存流水记录响应
 */
@Data
@Schema(description = "库存流水记录响应")
public class StockRecordVO {

    @Schema(description = "主键 ID")
    private Long id;

    @Schema(description = "商品SKU ID")
    private Long skuId;

    @Schema(description = "变动类型")
    private Integer type;

    @Schema(description = "变动数量（正数入库，负数出库）")
    private Integer changeNum;

    @Schema(description = "变动前库存")
    private Integer beforeStock;

    @Schema(description = "变动后库存")
    private Integer afterStock;

    @Schema(description = "批次号")
    private String batchNo;

    @Schema(description = "关联单据类型")
    private String relatedType;

    @Schema(description = "关联单据 ID")
    private Long relatedId;

    @Schema(description = "关联单据号")
    private String relatedNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "操作人 ID")
    private Long createUser;

    @Schema(description = "商品条码")
    private String barcode;

    @Schema(description = "规格名称")
    private String specName;

    @Schema(description = "规格值")
    private String specValue;

    @Schema(description = "商品名称")
    private String goodsName;

    @Schema(description = "商品主图URL")
    private String mainImage;
}
