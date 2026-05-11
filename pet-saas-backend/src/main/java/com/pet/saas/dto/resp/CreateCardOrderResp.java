package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Schema(description = "创建次卡订单响应")
public class CreateCardOrderResp {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "支付金额")
    private BigDecimal payAmount;

    @Schema(description = "微信支付参数")
    private Map<String, String> wechatPayParams;
}
