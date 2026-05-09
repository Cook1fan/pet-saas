package com.pet.saas.service.dto;

import com.pet.saas.entity.Goods;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 商品变更结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsChangeResult {
    /**
     * 变更前的商品（修改时有值）
     */
    private Goods beforeGoods;
    /**
     * 变更后的商品
     */
    private Goods afterGoods;
    /**
     * 商品基础信息变更（修改时有值）
     */
    private Map<String, Object> beforeChanges;
    private Map<String, Object> afterChanges;
    /**
     * SKU 变更列表
     */
    private List<SkuChange> skuChanges;
}
