package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单操作日志实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("order_operation_log")
public class OrderOperationLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 租户 ID
     */
    private Long tenantId;

    /**
     * 订单 ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 变更前状态
     */
    private Integer beforeStatus;

    /**
     * 变更后状态
     */
    private Integer afterStatus;

    /**
     * 操作类型：1-创建，2-支付，3-发货，4-完成，5-取消，6-退款
     */
    private Integer operationType;

    /**
     * 操作描述
     */
    private String operationDesc;

    /**
     * 操作人 ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;
}
