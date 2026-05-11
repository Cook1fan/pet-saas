package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会员账户查询条件")
public class MemberAccountQuery extends BasePageQuery {

    @Schema(description = "会员姓名/手机号（模糊搜索）")
    private String keyword;
}
