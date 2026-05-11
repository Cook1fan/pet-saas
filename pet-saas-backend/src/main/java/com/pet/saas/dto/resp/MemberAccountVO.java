package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "会员储值账户响应")
public class MemberAccountVO {

    @Schema(description = "账户ID")
    private Long id;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员姓名")
    private String memberName;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "账户余额")
    private BigDecimal balance;

    @Schema(description = "累计储值")
    private BigDecimal totalRecharge;
}
