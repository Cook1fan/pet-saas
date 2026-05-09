package com.pet.saas.common.constant;

public interface RedisKeyConstants {

    String ACTIVITY_STOCK_KEY = "activity:stock:%d";
    String AI_LIMIT_KEY = "ai:limit:%d:%s";
    String ORDER_USER_LOCK_KEY = "order:user:%d:activity:%d";
    String TENANT_ID_KEY = "tenant_id";
    String MP_TOKEN_KEY = "mp:token:%s";

    String GOODS_CATEGORY_TREE_KEY = "goods:category:tree";
    String GOODS_CATEGORY_LOCK_KEY = "goods:category:lock";

    /**
     * 商品编辑锁
     * 参数：tenantId, goodsId
     */
    String GOODS_EDIT_LOCK_KEY = "goods:edit:lock:%d:%d";

    /**
     * 库存变动锁
     * 参数：tenantId, skuId
     */
    String STOCK_CHANGE_LOCK_KEY = "stock:change:lock:%d:%d";

    /**
     * 订单支付回调幂等性 Key
     * 参数：orderNo
     */
    String ORDER_PAY_CALLBACK_KEY = "order:pay:callback:%s";
}
