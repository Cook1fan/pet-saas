package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "订单项响应")
public class OrderItemVO {

    @Schema(description = "订单项ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "商品/服务ID")
    private Long goodsId;

    @Schema(description = "商品/服务名称")
    private String goodsName;

    @Schema(description = "数量")
    private Integer num;

    @Schema(description = "单价")
    private BigDecimal price;
}
