package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "次卡规则响应")
public class CardRuleVO {

    @Schema(description = "规则ID")
    private Long id;

    @Schema(description = "规则名称")
    private String name;

    @Schema(description = "包含次数")
    private Integer times;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "有效期天数")
    private Integer validDays;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
