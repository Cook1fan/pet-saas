package com.pet.saas.common;

/**
 * Redis Key 枚举
 *
 * @author Pet SaaS Team
 * @since 2026-05-09
 */
public enum RedisKey {

    // 活动库存
    ACTIVITY_STOCK("activity:stock:%d", "活动库存"),
    // 订单用户活动锁
    ORDER_USER_ACTIVITY("order:user:%d:activity:%d", "订单用户活动锁"),
    // 拼团组进度
    GROUP_PROGRESS("group:progress:%d", "拼团组进度"),
    // 活动热门拼团列表
    ACTIVITY_HOT_GROUPS("activity:hot_groups:%d", "活动热门拼团列表");

    private final String pattern;
    private final String description;

    RedisKey(String pattern, String description) {
        this.pattern = pattern;
        this.description = description;
    }

    public String getKey(Object... args) {
        return String.format(pattern, args);
    }

    public String getPattern() {
        return pattern;
    }

    public String getDescription() {
        return description;
    }
}
