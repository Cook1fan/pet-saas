package com.pet.saas.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.saas.entity.OrderOperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface OrderOperationLogMapper extends BaseMapper<OrderOperationLog> {

    /**
     * 插入订单操作日志（忽略租户插件）
     */
    @InterceptorIgnore(tenantLine = "true")
    @Insert("INSERT INTO order_operation_log (tenant_id, order_id, order_no, before_status, after_status, " +
            "operation_type, operation_desc, operator_id, operator_name, create_time, update_time) " +
            "VALUES (#{tenantId}, #{orderId}, #{orderNo}, #{beforeStatus}, #{afterStatus}, " +
            "#{operationType}, #{operationDesc}, #{operatorId}, #{operatorName}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertIgnoreTenant(OrderOperationLog log);
}
