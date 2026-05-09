package com.pet.saas.service;

import com.pet.saas.entity.OrderInfo;
import com.pet.saas.entity.OrderOperationLog;

import java.util.List;

/**
 * 订单操作日志服务
 */
public interface OrderOperationLogService {

    /**
     * 保存订单操作日志
     *
     * @param order           订单信息
     * @param beforeStatus   变更前状态
     * @param afterStatus    变更后状态
     * @param operationType  操作类型
     * @param operationDesc  操作描述
     * @param operatorId     操作人ID（系统操作为null）
     * @param operatorName   操作人姓名（系统操作为null）
     * @return 操作日志
     */
    OrderOperationLog saveLog(
            OrderInfo order,
            Integer beforeStatus,
            Integer afterStatus,
            Integer operationType,
            String operationDesc,
            Long operatorId,
            String operatorName
    );

    /**
     * 查询订单的操作日志
     *
     * @param orderId  订单ID
     * @param tenantId 租户ID
     * @return 操作日志列表
     */
    List<OrderOperationLog> listByOrderId(Long orderId, Long tenantId);
}
