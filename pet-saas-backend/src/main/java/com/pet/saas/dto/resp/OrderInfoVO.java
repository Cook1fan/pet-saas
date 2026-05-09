package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "订单信息响应")
public class OrderInfoVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "支付方式：1-微信，2-现金，3-储值，4-次卡")
    private Integer payType;

    @Schema(description = "支付状态：0-待支付，1-已支付，2-已退款")
    private Integer payStatus;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "第三方交易流水号")
    private String transactionId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
