package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "活动列表查询条件")
public class ActivityQuery {

    @Schema(description = "活动类型：1-拼团，2-秒杀")
    private Integer type;

    @Schema(description = "状态：0-未开始，1-进行中，2-已结束")
    private Integer status;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页条数")
    private Integer pageSize = 10;
}
