package com.pet.saas.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 token 失效"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    ACCOUNT_OR_PASSWORD_ERROR(1001, "账号或密码错误"),
    TENANT_DISABLED(1002, "租户已被禁用"),
    STOCK_INSUFFICIENT(1003, "库存不足"),
    BALANCE_INSUFFICIENT(1004, "储值余额不足"),
    CARD_INSUFFICIENT_OR_EXPIRED(1005, "次卡次数不足或已过期"),
    AI_LIMIT_EXCEEDED(1006, "AI 调用额度已用完"),
    DUPLICATE_ORDER(1007, "请勿重复下单"),
    RECONCILIATION_ERROR(1008, "对账异常"),
    VERIFY_CODE_INVALID(1009, "核销码无效或已过期"),
    VERIFY_CODE_USED(1010, "核销码已使用"),
    MEMBER_CARD_NOT_FOUND(1011, "会员次卡不存在"),
    PHONE_ALREADY_EXISTS(1012, "该手机号已创建过租户"),
    MP_TOKEN_INVALID(1020, "微信授权token无效或已过期"),
    WECHAT_ALREADY_REGISTERED(1021, "该微信已注册过门店"),
    TENANT_INFO_INCOMPLETE(1022, "门店信息尚未完善"),
    WECHAT_DECRYPT_FAILED(1023, "微信数据解密失败"),

    GOODS_BEING_EDITED(40010, "商品正在被编辑，请稍后再试"),
    GOODS_VERSION_MISMATCH(40011, "商品已被修改，请刷新后重新编辑"),
    GOODS_NOT_FOUND(40012, "商品不存在"),

    // SKU 相关错误 (40020-40039)
    SKU_NOT_FOUND(40020, "商品规格不存在"),
    SERVICE_GOODS_NO_STOCK(40021, "服务类商品无库存概念"),

    // 活动相关错误 (40030-40039)
    ACTIVITY_NOT_FOUND(40030, "活动不存在"),

    // 库存相关错误 (40040-40059)
    STOCK_NOT_ENOUGH(40040, "库存不足"),
    STOCK_BEING_CHANGED(40041, "库存正在变动中，请稍后再试"),
    STOCK_VERSION_MISMATCH(40042, "库存已变动，请刷新后重试"),
    STOCK_NO_CHANGE(40043, "库存未发生变化");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
