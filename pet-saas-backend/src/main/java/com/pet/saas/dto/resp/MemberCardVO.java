package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "会员次卡响应")
public class MemberCardVO {

    @Schema(description = "次卡ID")
    private Long id;

    @Schema(description = "次卡名称")
    private String cardName;

    @Schema(description = "剩余次数")
    private Integer remainTimes;

    @Schema(description = "总次数")
    private Integer totalTimes;

    @Schema(description = "过期时间")
    private LocalDateTime expireTime;

    @Schema(description = "剩余天数")
    private Integer daysRemain;

    @Schema(description = "状态：0-已过期，1-有效")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;
}
