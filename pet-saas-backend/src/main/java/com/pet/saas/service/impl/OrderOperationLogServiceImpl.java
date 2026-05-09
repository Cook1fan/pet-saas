package com.pet.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.saas.entity.OrderInfo;
import com.pet.saas.entity.OrderOperationLog;
import com.pet.saas.mapper.OrderOperationLogMapper;
import com.pet.saas.service.OrderOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单操作日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderOperationLogServiceImpl extends ServiceImpl<OrderOperationLogMapper, OrderOperationLog> implements OrderOperationLogService {

    private final OrderOperationLogMapper orderOperationLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderOperationLog saveLog(
            OrderInfo order,
            Integer beforeStatus,
            Integer afterStatus,
            Integer operationType,
            String operationDesc,
            Long operatorId,
            String operatorName
    ) {
        OrderOperationLog logRecord = new OrderOperationLog();
        logRecord.setTenantId(order.getTenantId());
        logRecord.setOrderId(order.getId());
        logRecord.setOrderNo(order.getOrderNo());
        logRecord.setBeforeStatus(beforeStatus);
        logRecord.setAfterStatus(afterStatus);
        logRecord.setOperationType(operationType);
        logRecord.setOperationDesc(operationDesc);
        logRecord.setOperatorId(operatorId);
        logRecord.setOperatorName(operatorName);

        orderOperationLogMapper.insert(logRecord);
        log.info("保存订单操作日志成功，订单号：{}，操作类型：{}", order.getOrderNo(), operationType);

        return orderOperationLogMapper.selectById(logRecord.getId());
    }

    @Override
    public List<OrderOperationLog> listByOrderId(Long orderId, Long tenantId) {
        LambdaQueryWrapper<OrderOperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderOperationLog::getTenantId, tenantId)
                .eq(OrderOperationLog::getOrderId, orderId)
                .orderByDesc(OrderOperationLog::getCreateTime);
        return orderOperationLogMapper.selectList(wrapper);
    }
}
