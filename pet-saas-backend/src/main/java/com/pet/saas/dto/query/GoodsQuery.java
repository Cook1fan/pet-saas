package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "商品列表查询条件")
public class GoodsQuery extends BasePageQuery {

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "商品名称（模糊搜索）")
    private String keyword;

    @Schema(description = "状态：0-下架，1-上架")
    private Integer status;
}
