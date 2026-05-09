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
@Schema(description = "次卡列表响应")
public class UserMemberCardVO {

    @Schema(description = "会员次卡 ID")
    private Long id;

    @Schema(description = "次卡规则 ID")
    private Long cardRuleId;

    @Schema(description = "次卡名称")
    private String cardName;

    @Schema(description = "剩余次数")
    private Integer remainTimes;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "状态：0-已用完，1-有效，2-已过期")
    private Integer status;
}
