package com.pet.saas.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.dto.query.OrderQuery;
import com.pet.saas.dto.req.PcOrderCreateReq;
import com.pet.saas.dto.req.PcOrderRefundReq;
import com.pet.saas.entity.OrderInfo;
import com.pet.saas.entity.OrderItem;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;

public interface OrderService {

    OrderInfo createOrder(PcOrderCreateReq req, Long tenantId);

    void completeOrder(OrderInfo order, String transactionId);

    Page<OrderInfo> listOrders(OrderQuery query, Long tenantId);

    OrderInfo getOrderDetail(Long orderId);

    List<OrderItem> getOrderItems(Long orderId);

    void refundOrder(PcOrderRefundReq req);

    /**
     * 取消订单
     *
     * @param orderId    订单ID
     * @param reason     取消原因
     * @param isSystem   是否系统取消
     */
    void cancelOrder(Long orderId, String reason, boolean isSystem);

    /**
     * 订单发货
     *
     * @param orderId    订单ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void shipOrder(Long orderId, Long operatorId, String operatorName);

    /**
     * 订单完成
     *
     * @param orderId    订单ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人姓名
     */
    void completeOrder(Long orderId, Long operatorId, String operatorName);

    /**
     * 流式查询超时未支付订单（ResultHandler 回调方式）
     *
     * @param handler 结果处理器
     */
    void selectTimeoutUnpaidOrders(ResultHandler<OrderInfo> handler);

    /**
     * 处理微信支付成功回调
     *
     * @param orderNo       订单号
     * @param transactionId 微信支付单号
     */
    void handlePaySuccess(String orderNo, String transactionId);
}
