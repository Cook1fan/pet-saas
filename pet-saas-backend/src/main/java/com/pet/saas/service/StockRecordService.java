package com.pet.saas.service;

import com.pet.saas.entity.StockRecord;
import java.util.List;

/**
 * 库存变更记录服务
 */
public interface StockRecordService {

    /**
     * 创建库存变更记录
     *
     * @param tenantId    租户ID
     * @param skuId      SKU ID
     * @param type       变更类型
     * @param changeNum  变更数量（正数入库，负数出库）
     * @param beforeStock 变更前库存
     * @param afterStock  变更后库存
     * @param relatedType  关联单据类型
     * @param relatedId    关联单据ID
     * @param relatedNo    关联单据号
     * @param remark      备注
     * @return 库存变更记录
     */
    StockRecord createStockRecord(
            Long tenantId,
            Long skuId,
            Integer type,
            Integer changeNum,
            Integer beforeStock,
            Integer afterStock,
            String relatedType,
            Long relatedId,
            String relatedNo,
            String remark
    );

    /**
     * 批量创建库存变更记录（同一批次）
     *
     * @param records 库存变更记录列表
     */
    void batchCreateStockRecord(List<StockRecord> records);

    /**
     * 查询SKU的库存变更记录
     *
     * @param skuId SKU ID
     * @param tenantId 租户 ID
     * @return 库存变更记录列表
     */
    List<StockRecord> listBySkuId(Long skuId, Long tenantId);
}
