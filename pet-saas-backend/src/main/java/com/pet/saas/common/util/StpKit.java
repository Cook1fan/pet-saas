package com.pet.saas.common.util;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;

public class StpKit {

    // 默认原生会话对象（保留，兼容旧代码）
    public static final StpLogic DEFAULT = StpUtil.stpLogic;

    // SHOP 会话对象：门店端管理员
    public static final StpLogic SHOP = new StpLogic("shop");

    // PLATFORM 会话对象：平台端管理员
    public static final StpLogic PLATFORM = new StpLogic("platform");

    // MEMBER 会话对象：小程序用户
    public static final StpLogic MEMBER = new StpLogic("member");
}
