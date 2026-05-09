package com.pet.saas.handler;

import cn.dev33.satoken.stp.StpLogic;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.common.util.TenantContext;
import com.pet.saas.common.constant.RedisKeyConstants;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
public class CustomTenantLineHandler implements TenantLineHandler {

    private static final List<String> IGNORE_TABLES = Arrays.asList(
            "sys_platform_admin",
            "sys_tenant",
            "sys_ai_package",
            "sys_config",
            "member_shop_bind",
            "shop_wechat_bind",
            "shop_admin",
            "shop_config",
            "goods_category"
    );

    @Override
    public Expression getTenantId() {
        // 按优先级尝试获取 tenant_id：PLATFORM -> SHOP -> MEMBER
        Long tenantId = getTenantIdFromStpLogic(StpKit.PLATFORM);
        if (tenantId != null) {
            return new LongValue(tenantId);
        }
        tenantId = getTenantIdFromStpLogic(StpKit.SHOP);
        if (tenantId != null) {
            return new LongValue(tenantId);
        }
        tenantId = getTenantIdFromStpLogic(StpKit.MEMBER);
        return new LongValue(Objects.requireNonNullElse(tenantId, 0L));
    }

    private Long getTenantIdFromStpLogic(StpLogic stpLogic) {
        try {
            Object tenantId = stpLogic.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
            if (tenantId != null) {
                return (Long) tenantId;
            }
        } catch (Exception ignored) {
            // 当前 StpLogic 未登录，忽略
        }
        return null;
    }

    @Override
    public boolean ignoreTable(String tableName) {
        // 检查是否通过 ThreadLocal 临时忽略多租户
        if (TenantContext.isIgnore()) {
            return true;
        }
        if (IGNORE_TABLES.contains(tableName)) {
            return true;
        }
        // 检查是否是平台管理员
        try {
            return StpKit.PLATFORM.hasRole("platform_admin");
        } catch (Exception ignored) {
            return false;
        }
    }
}