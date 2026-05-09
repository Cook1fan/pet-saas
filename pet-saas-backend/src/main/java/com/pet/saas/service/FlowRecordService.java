package com.pet.saas.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.dto.query.FlowQuery;
import com.pet.saas.entity.FlowRecord;
import com.pet.saas.entity.OrderInfo;

public interface FlowRecordService {

    FlowRecord createFlowRecord(OrderInfo order, Integer type, String transactionId);

    Page<FlowRecord> listFlows(FlowQuery query, Long tenantId);
}
