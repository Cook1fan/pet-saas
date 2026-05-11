package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "我的记录查询条件")
public class MyRecordQuery extends BasePageQuery {

    @Schema(description = "类型：1-储值/购买，2-消费/核销，3-退款")
    private Integer type;
}
