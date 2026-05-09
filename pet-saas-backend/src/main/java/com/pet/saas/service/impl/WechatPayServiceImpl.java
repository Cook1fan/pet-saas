package com.pet.saas.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.enums.PayStatusEnum;
import com.pet.saas.config.properties.WechatPayProperties;
import com.pet.saas.entity.OrderInfo;
import com.pet.saas.mapper.OrderInfoMapper;
import com.pet.saas.service.FlowRecordService;
import com.pet.saas.service.OrderService;
import com.pet.saas.service.WechatPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信支付服务实现
 * <p>
 * 注：此为 MVP 版本，实际微信支付集成需要引入官方 SDK
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatPayServiceImpl implements WechatPayService {

    private final WechatPayProperties wechatPayProperties;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderService orderService;
    private final FlowRecordService flowRecordService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public Map<String, String> createMiniProgramPayOrder(OrderInfo order) {
        Map<String, String> result = new HashMap<>();

        // TODO: 实际项目中需要接入微信支付 SDK
        // 这里生成模拟的支付参数供前端联调
        String timestamp = String.valueOf(Instant.now().getEpochSecond());
        String nonceStr = IdUtil.fastSimpleUUID().substring(0, 32);
        String prepayId = "wx" + IdUtil.fastSimpleUUID().substring(0, 28);

        result.put("timeStamp", timestamp);
        result.put("nonceStr", nonceStr);
        result.put("package", "prepay_id=" + prepayId);
        result.put("signType", "RSA");
        result.put("paySign", generateMockSign(timestamp, nonceStr, prepayId));

        log.info("生成模拟微信支付参数，订单号：{}", order.getOrderNo());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String handlePayCallback(String xmlData) {
        // TODO: 实际项目中需要解析微信回调 XML 并验证签名
        // 这里模拟处理回调逻辑
        log.info("收到微信支付回调：{}", xmlData);

        // 模拟解析回调数据
        String orderNo = extractOrderNoFromXml(xmlData);
        String transactionId = extractTransactionIdFromXml(xmlData);

        // 幂等性检查（Redis）
        String idempotentKey = String.format(RedisKeyConstants.ORDER_PAY_CALLBACK_KEY, orderNo);
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(idempotentKey))) {
            log.info("订单支付回调已处理，直接返回成功，订单号：{}", orderNo);
            return buildSuccessResponse();
        }

        // 查询订单
        OrderInfo order = orderInfoMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderInfo>()
                .eq(OrderInfo::getOrderNo, orderNo));

        if (order == null) {
            log.warn("回调订单不存在：{}", orderNo);
            return buildFailResponse("订单不存在");
        }

        // 幂等性检查（数据库状态）
        if (PayStatusEnum.PAID.getCode() == order.getPayStatus()) {
            log.info("订单已支付，直接返回成功：{}", orderNo);
            stringRedisTemplate.opsForValue().set(idempotentKey, "1", 24, TimeUnit.HOURS);
            return buildSuccessResponse();
        }

        // 调用 OrderService 处理支付成功逻辑
        orderService.handlePaySuccess(orderNo, transactionId);

        // 记录幂等性
        stringRedisTemplate.opsForValue().set(idempotentKey, "1", 24, TimeUnit.HOURS);

        log.info("订单支付成功处理完成，订单号：{}", orderNo);
        return buildSuccessResponse();
    }

    @Override
    public boolean refundOrder(OrderInfo order, BigDecimal refundAmount, String reason) {
        // TODO: 实际项目中需要调用微信退款 API
        log.info("模拟微信退款，订单号：{}，退款金额：{}", order.getOrderNo(), refundAmount);
        return true;
    }

    private String generateMockSign(String timestamp, String nonceStr, String prepayId) {
        // 模拟签名生成，实际使用微信支付 SDK
        String signStr = wechatPayProperties.getAppId() + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + "prepay_id=" + prepayId + "\n";
        return SecureUtil.sha256(signStr);
    }

    private String extractOrderNoFromXml(String xml) {
        // 模拟解析，实际使用 XML 解析库
        return "ORD" + System.currentTimeMillis();
    }

    private String extractTransactionIdFromXml(String xml) {
        // 模拟解析
        return "420000" + System.currentTimeMillis();
    }

    private String buildSuccessResponse() {
        return """
                <xml>
                  <return_code><![CDATA[SUCCESS]]></return_code>
                  <return_msg><![CDATA[OK]]></return_msg>
                </xml>
                """;
    }

    private String buildFailResponse(String msg) {
        return """
                <xml>
                  <return_code><![CDATA[FAIL]]></return_code>
                  <return_msg><![CDATA[%s]]></return_msg>
                </xml>
                """.formatted(msg);
    }
}
