package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "储值记录查询条件")
public class RechargeRecordQuery extends BasePageQuery {

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "类型：1-储值，2-消费，3-退款")
    private Integer type;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
