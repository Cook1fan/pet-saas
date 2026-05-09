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
@Schema(description = "商家版数据概览响应")
public class MerchantDashboardVO {

    @Schema(description = "今日订单数")
    private Integer todayOrderCount;

    @Schema(description = "今日GMV")
    private BigDecimal todayGmv;

    @Schema(description = "本月GMV")
    private BigDecimal monthlyGmv;
}
