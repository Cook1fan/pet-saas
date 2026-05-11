package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "储值规则查询条件")
public class RechargeRuleQuery extends BasePageQuery {

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "规则名称（模糊搜索）")
    private String keyword;
}
