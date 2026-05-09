package com.pet.saas.service;

import com.pet.saas.entity.GoodsSku;

import java.util.List;

public interface GoodsSkuService {

    List<GoodsSku> listByGoodsId(Long goodsId);

    List<GoodsSku> listByGoodsIds(List<Long> goodsIds);

    List<GoodsSku> listBySkuIds(List<Long> skuIds);

    GoodsSku getSku(Long skuId);

    /**
     * 根据条码查询 SKU（优先查 barcode，查不到再查 sku_code）
     *
     * @param code     条码或 SKU 编码
     * @param tenantId 租户 ID
     * @return SKU 实体，不存在返回 null
     */
    GoodsSku getByCode(String code, Long tenantId);

    void saveSku(GoodsSku sku);

    void updateSku(GoodsSku sku);

    void deleteSku(Long skuId);

    void deleteByGoodsId(Long goodsId);

    boolean deductStock(Long skuId, int num);

    boolean addStock(Long skuId, int num);

    List<GoodsSku> listWarnSku(Long tenantId);

    /**
     * 根据条码或SKU编码查找SKU（同 getByCode，保留兼容性）
     */
    GoodsSku getByBarcodeOrCode(String code, Long tenantId);

    /**
     * 计算可销售库存
     */
    Integer calculateAvailableStock(GoodsSku sku);

    /**
     * 检查库存是否充足
     */
    boolean checkStockAvailable(Long skuId, int num);

    /**
     * 扣减库存（支持预留库存和负库存）
     */
    boolean deductStockWithReserve(Long skuId, int num);

    /**
     * 入库（支持实际库存入库和预留库存入库）
     */
    boolean addStockWithType(Long skuId, int num, Integer type);

    /**
     * 预留库存转为实际库存（货到了）
     */
    boolean transferReservedToActual(Long skuId, int num);

    /**
     * 预占库存：从可用库存(stock)移到预留库存(reservedStock)
     * 下单时调用（微信支付场景）
     *
     * @param skuId SKU ID
     * @param num   数量
     * @return 是否成功
     */
    boolean reserveStock(Long skuId, int num);

    /**
     * 释放预占库存：从预留库存移回可用库存
     * 订单取消/超时时调用
     *
     * @param skuId SKU ID
     * @param num   数量
     * @return 是否成功
     */
    boolean releaseReservedStock(Long skuId, int num);

    /**
     * 确认扣减：扣减预留库存
     * 支付成功后调用（PC端开单收银）或发货时调用（小程序下单）
     *
     * @param skuId SKU ID
     * @param num   数量
     * @return 是否成功
     */
    boolean confirmDeductReservedStock(Long skuId, int num);

    /**
     * 直接扣减可用库存
     * 即时交付场景用（现金/储值/次卡支付）
     *
     * @param skuId SKU ID
     * @param num   数量
     * @return 是否成功
     */
    boolean deductStockDirect(Long skuId, int num);
}
