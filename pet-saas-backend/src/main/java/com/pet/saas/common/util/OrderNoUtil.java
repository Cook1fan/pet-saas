package com.pet.saas.common.util;

import cn.hutool.core.util.RandomUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 订单号生成工具类
 */
public class OrderNoUtil {

    private static final String ORDER_PREFIX = "ORD";
    private static final String FLOW_PREFIX = "FLW";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNoUtil() {
    }

    /**
     * 生成订单号
     * 格式：ORD{yyyyMMddHHmmss}{6位随机数}
     *
     * @return 订单号
     */
    public static String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String random = RandomUtil.randomNumbers(6);
        return ORDER_PREFIX + timestamp + random;
    }

    /**
     * 生成流水号
     * 格式：FLW{yyyyMMddHHmmss}{6位随机数}
     *
     * @return 流水号
     */
    public static String generateFlowNo() {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String random = RandomUtil.randomNumbers(6);
        return FLOW_PREFIX + timestamp + random;
    }
}
