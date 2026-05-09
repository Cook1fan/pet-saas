package com.pet.saas.service.dto;

import com.pet.saas.entity.GoodsSku;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SKU 变更信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuChange {
    /**
     * 变更类型：1-新增，2-修改，3-删除
     */
    private Integer changeType;
    /**
     * 变更前的 SKU（修改/删除时有值）
     */
    private GoodsSku beforeSku;
    /**
     * 变更后的 SKU（新增/修改时有值）
     */
    private GoodsSku afterSku;
}
