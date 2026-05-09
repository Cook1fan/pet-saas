package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "拼团活动查询条件")
public class GroupActivityQuery extends BasePageQuery {

    @Schema(description = "活动状态")
    private Integer status;

    @Schema(description = "活动标题（模糊搜索）")
    private String keyword;
}
