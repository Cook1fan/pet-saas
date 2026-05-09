package com.pet.saas.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 我的拼团订单查询条件
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "我的拼团订单查询条件")
public class MyGroupOrderQuery extends BasePageQuery {

    @Schema(description = "订单状态：0-待支付，1-已支付，2-拼团成功，3-拼团失败")
    private Integer status;
}
