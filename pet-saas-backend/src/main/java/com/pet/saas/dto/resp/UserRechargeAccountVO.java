package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "用户储值账户响应")
public class UserRechargeAccountVO {

    @Schema(description = "账户余额")
    private BigDecimal balance;

    @Schema(description = "累计储值")
    private BigDecimal totalRecharge;

    @Schema(description = "累计消费")
    private BigDecimal totalConsume;
}
