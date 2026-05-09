package com.pet.saas.common.util;

/**
 * 多租户上下文工具类
 * 用于在特定业务流程中临时忽略多租户插件
 */
public final class TenantContext {

    private TenantContext() {
    }

    private static final ThreadLocal<Boolean> IGNORE_TENANT = new ThreadLocal<>();

    /**
     * 开启忽略多租户插件
     */
    public static void ignore() {
        IGNORE_TENANT.set(true);
    }

    /**
     * 恢复多租户插件
     */
    public static void restore() {
        IGNORE_TENANT.remove();
    }

    /**
     * 判断是否忽略多租户插件
     *
     * @return true-忽略，false-不忽略
     */
    public static boolean isIgnore() {
        return Boolean.TRUE.equals(IGNORE_TENANT.get());
    }
}
