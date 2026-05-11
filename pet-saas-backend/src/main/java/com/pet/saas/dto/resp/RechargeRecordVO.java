package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "储值记录响应")
public class RechargeRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "类型：1-储值，2-消费，3-退款")
    private Integer type;

    @Schema(description = "类型描述")
    private String typeDesc;

    @Schema(description = "变动金额")
    private BigDecimal amount;

    @Schema(description = "变动前余额")
    private BigDecimal balanceBefore;

    @Schema(description = "变动后余额")
    private BigDecimal balanceAfter;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
