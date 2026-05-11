package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "次卡记录查询条件")
public class CardRecordQuery extends BasePageQuery {

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "类型：1-购买，2-核销")
    private Integer type;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
