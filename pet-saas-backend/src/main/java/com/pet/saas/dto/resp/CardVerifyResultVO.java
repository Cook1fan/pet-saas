package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "次卡核销结果响应")
public class CardVerifyResultVO {

    @Schema(description = "次卡名称")
    private String cardName;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "核销后剩余次数")
    private Integer remainTimes;

    @Schema(description = "次卡过期时间")
    private LocalDateTime expireTime;
}
