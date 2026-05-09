package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "拼团组查询条件")
public class ActivityGroupQuery extends BasePageQuery {

    @Schema(description = "团状态：1-拼团中，2-拼团成功，3-拼团失败")
    private Integer status;
}
