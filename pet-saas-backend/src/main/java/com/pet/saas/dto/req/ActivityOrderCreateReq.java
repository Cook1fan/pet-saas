package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "活动下单请求")
public class ActivityOrderCreateReq {

    @Schema(description = "活动 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "活动 ID 不能为空")
    private Long activityId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    private Integer num;
}
