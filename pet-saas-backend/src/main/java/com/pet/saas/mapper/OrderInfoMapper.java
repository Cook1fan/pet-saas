package com.pet.saas.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.saas.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.ResultHandler;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * 流式查询（配合 QueryWrapper 使用，ResultHandler 回调方式）
     * 忽略租户插件，查询所有租户的超时订单
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM order_info ${ew.customSqlSegment}")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000)
    @ResultType(OrderInfo.class)
    void selectCursor(@Param("ew") Wrapper<OrderInfo> wrapper, ResultHandler<OrderInfo> handler);

    /**
     * 根据订单ID查询订单（忽略租户插件）
     */
    @InterceptorIgnore(tenantLine = "true")
    @Select("SELECT * FROM order_info WHERE id = #{orderId}")
    OrderInfo selectByIdIgnoreTenant(@Param("orderId") Long orderId);

    /**
     * 更新订单（忽略租户插件）
     */
    @InterceptorIgnore(tenantLine = "true")
    @Update("UPDATE order_info SET order_status = #{orderStatus}, cancel_time = #{cancelTime}, " +
            "cancel_reason = #{cancelReason}, update_time = NOW() WHERE id = #{id}")
    int updateByIdIgnoreTenant(OrderInfo order);
}
