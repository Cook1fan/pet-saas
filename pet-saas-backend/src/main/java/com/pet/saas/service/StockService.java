package com.pet.saas.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.dto.query.StockRecordQuery;
import com.pet.saas.dto.req.StockAdjustReq;
import com.pet.saas.dto.req.StockInReq;
import com.pet.saas.dto.req.StockOutReq;
import com.pet.saas.entity.StockRecord;

import java.util.List;

/**
 * 库存服务接口
 */
public interface StockService {

    /**
     * 入库操作
     *
     * @param req      入库请求
     * @param tenantId 租户ID
     * @param userId   操作人ID
     * @return 库存流水记录
     */
    StockRecord inStock(StockInReq req, Long tenantId, Long userId);

    /**
     * 出库操作
     *
     * @param req      出库请求
     * @param tenantId 租户ID
     * @param userId   操作人ID
     * @return 库存流水记录
     */
    StockRecord outStock(StockOutReq req, Long tenantId, Long userId);

    /**
     * 批量入库（同一批次）
     *
     * @param reqList  入库请求列表
     * @param tenantId 租户ID
     * @param userId   操作人ID
     * @return 库存流水记录列表
     */
    List<StockRecord> batchInStock(List<StockInReq> reqList, Long tenantId, Long userId);

    /**
     * 批量出库（同一批次）
     *
     * @param reqList  出库请求列表
     * @param tenantId 租户ID
     * @param userId   操作人ID
     * @return 库存流水记录列表
     */
    List<StockRecord> batchOutStock(List<StockOutReq> reqList, Long tenantId, Long userId);

    /**
     * 盘点调整
     *
     * @param req      盘点调整请求
     * @param tenantId 租户ID
     * @param userId   操作人ID
     * @return 库存流水记录
     */
    StockRecord adjustStock(StockAdjustReq req, Long tenantId, Long userId);

    /**
     * 查询 SKU 库存流水
     *
     * @param query    查询条件
     * @param tenantId 租户ID
     * @return 库存流水分页
     */
    Page<StockRecord> listStockRecord(StockRecordQuery query, Long tenantId);

    /**
     * 预占库存（仅增加 reservedStock，不生成 stock_record）
     * 微信支付下单时调用
     *
     * @param skuId     SKU ID
     * @param num       数量
     * @param tenantId  租户 ID
     * @param userId    操作人 ID
     * @param relatedNo 关联单号（订单号）
     */
    void reserveStock(Long skuId, int num, Long tenantId, Long userId, String relatedNo);

    /**
     * 释放预占库存（仅减少 reservedStock，不生成 stock_record）
     * 订单取消/超时时调用
     *
     * @param skuId     SKU ID
     * @param num       数量
     * @param tenantId  租户 ID
     * @param userId    操作人 ID
     * @param relatedNo 关联单号（订单号）
     */
    void releaseReservedStock(Long skuId, int num, Long tenantId, Long userId, String relatedNo);

    /**
     * 确认扣减库存（扣减 stock，减少 reservedStock，生成 stock_record）
     * 微信支付成功回调时调用
     *
     * @param skuId     SKU ID
     * @param num       数量
     * @param tenantId  租户 ID
     * @param userId    操作人 ID
     * @param relatedNo 关联单号（订单号）
     */
    void confirmDeductStock(Long skuId, int num, Long tenantId, Long userId, String relatedNo);

    /**
     * 直接扣减库存（扣减 stock，生成 stock_record）
     * 现金/储值/次卡支付时调用
     *
     * @param skuId     SKU ID
     * @param num       数量
     * @param tenantId  租户 ID
     * @param userId    操作人 ID
     * @param relatedNo 关联单号（订单号）
     */
    void deductStockDirect(Long skuId, int num, Long tenantId, Long userId, String relatedNo);
}
