package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "储值余额响应")
public class UserAccountBalanceVO {

    @Schema(description = "储值余额")
    private BigDecimal balance;

    @Schema(description = "累计储值")
    private BigDecimal totalRecharge;
}
