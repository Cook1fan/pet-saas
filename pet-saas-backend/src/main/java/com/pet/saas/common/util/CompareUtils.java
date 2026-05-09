package com.pet.saas.common.util;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 通用比较工具类
 */
public class CompareUtils {

    private CompareUtils() {
    }

    /**
     * 比较两个值是否相等，处理数值类型兼容问题（如 3.0 和 3 视为相等）
     *
     * @param value1 值1
     * @param value2 值2
     * @return 是否相等
     */
    public static boolean valuesEqual(Object value1, Object value2) {
        if (Objects.equals(value1, value2)) {
            return true;
        }
        // 如果都是数值类型，转换为 BigDecimal 比较
        if (value1 instanceof Number && value2 instanceof Number) {
            BigDecimal bd1 = new BigDecimal(String.valueOf(value1));
            BigDecimal bd2 = new BigDecimal(String.valueOf(value2));
            return bd1.compareTo(bd2) == 0;
        }
        return false;
    }
}
