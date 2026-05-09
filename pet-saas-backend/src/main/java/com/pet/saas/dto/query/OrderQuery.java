package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单列表查询条件")
public class OrderQuery extends BasePageQuery {

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "支付方式：1-微信，2-现金，3-储值，4-次卡")
    private Integer payType;

    @Schema(description = "支付状态：0-待支付，1-已支付，2-已退款")
    private Integer payStatus;

    @Schema(description = "订单状态：0-待支付，1-已支付，2-已发货，3-已完成，4-已取消，5-已退款")
    private Integer orderStatus;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
