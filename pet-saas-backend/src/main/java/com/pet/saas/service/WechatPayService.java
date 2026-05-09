package com.pet.saas.service;

import com.pet.saas.entity.OrderInfo;
import java.util.Map;

/**
 * 微信支付服务
 */
public interface WechatPayService {

    /**
     * 小程序支付统一下单
     *
     * @param order 订单信息
     * @return 微信支付参数
     */
    Map<String, String> createMiniProgramPayOrder(OrderInfo order);

    /**
     * 处理微信支付回调
     *
     * @param xmlData 回调 XML 数据
     * @return 响应给微信的 XML
     */
    String handlePayCallback(String xmlData);

    /**
     * 订单退款
     *
     * @param order 订单信息
     * @param refundAmount 退款金额
     * @param reason 退款原因
     * @return 是否成功
     */
    boolean refundOrder(OrderInfo order, java.math.BigDecimal refundAmount, String reason);
}
