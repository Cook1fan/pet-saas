package com.pet.saas.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "PC端开单收银请求")
public class PcOrderCreateReq {

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "商品/服务列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "商品列表不能为空")
    @Valid
    private List<PcOrderItemRequest> items;

    @Schema(description = "支付方式：1-微信，2-现金，3-储值，4-次卡", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "支付方式不能为空")
    private Integer payType;

    @Schema(description = "次卡ID（次卡支付时必填）")
    private Long cardId;
}
