package com.pet.saas.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 创建拼团订单响应 DTO
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupOrderResp {

    private Long orderId;

    private String orderNo;

    private BigDecimal payAmount;

    private Map<String, String> wechatPayParams;
}
