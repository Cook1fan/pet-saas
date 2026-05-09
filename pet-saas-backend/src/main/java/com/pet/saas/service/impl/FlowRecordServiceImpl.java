package com.pet.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.saas.common.util.OrderNoUtil;
import com.pet.saas.dto.query.FlowQuery;
import com.pet.saas.entity.FlowRecord;
import com.pet.saas.entity.OrderInfo;
import com.pet.saas.mapper.FlowRecordMapper;
import com.pet.saas.service.FlowRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlowRecordServiceImpl extends ServiceImpl<FlowRecordMapper, FlowRecord> implements FlowRecordService {

    private final FlowRecordMapper flowRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlowRecord createFlowRecord(OrderInfo order, Integer type, String transactionId) {
        FlowRecord flow = new FlowRecord();
        flow.setTenantId(order.getTenantId());
        flow.setFlowNo(OrderNoUtil.generateFlowNo());
        flow.setOrderId(order.getId());
        flow.setAmount(order.getPayAmount());
        flow.setType(type);
        flow.setPayStatus(order.getPayStatus());
        flow.setTransactionId(transactionId);
        flow.setReconcileStatus(0);
        flowRecordMapper.insert(flow);
        return flow;
    }

    @Override
    public Page<FlowRecord> listFlows(FlowQuery query, Long tenantId) {
        LambdaQueryWrapper<FlowRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FlowRecord::getTenantId, tenantId);
        if (query.getType() != null) {
            wrapper.eq(FlowRecord::getType, query.getType());
        }
        if (query.getPayStatus() != null) {
            wrapper.eq(FlowRecord::getPayStatus, query.getPayStatus());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(FlowRecord::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(FlowRecord::getCreateTime, query.getEndTime());
        }
        wrapper.orderByDesc(FlowRecord::getCreateTime);
        return flowRecordMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }
}
