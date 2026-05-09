package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "会员列表查询条件")
public class MemberQuery extends BasePageQuery {

    @Schema(description = "手机号/姓名（模糊搜索）")
    private String keyword;

    @Schema(description = "标签")
    private String tag;
}
