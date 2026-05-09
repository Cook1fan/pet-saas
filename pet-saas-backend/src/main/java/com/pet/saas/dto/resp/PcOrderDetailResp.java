package com.pet.saas.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "PC端订单详情响应")
public class PcOrderDetailResp {

    @Schema(description = "订单信息")
    private OrderInfoVO order;

    @Schema(description = "订单项列表")
    private List<OrderItemVO> items;
}
