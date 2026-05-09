package com.pet.saas.service.impl;

import com.pet.saas.common.enums.ChangeTypeEnum;
import com.pet.saas.common.util.EntityMapUtils;
import com.pet.saas.dto.req.GoodsChangeLogReq;
import com.pet.saas.entity.Goods;
import com.pet.saas.entity.GoodsSku;
import com.pet.saas.service.GoodsChangeLogService;
import com.pet.saas.service.GoodsSkuService;
import com.pet.saas.service.dto.GoodsChangeResult;
import com.pet.saas.service.dto.SkuChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品变更日志辅助类
 * 负责商品变更日志的记录逻辑
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsChangeLogHelper {

    private static final int DATA_TYPE_GOODS = 1;

    private static final List<String> SKU_IGNORE_FIELDS = Arrays.asList(
            "createTime", "createUser", "updateTime", "updateUser",
            "isDeleted", "serialVersionUID", "tenantId",
            "reservedStock", "warnStock"
    );

    private final GoodsSkuService goodsSkuService;
    private final GoodsChangeLogService goodsChangeLogService;

    /**
     * 统一记录商品+SKU的变更日志（合并为一条记录）
     */
    public void logCompleteChange(GoodsChangeResult changeResult, String batchNo, Long tenantId, Long userId) {
        GoodsChangeLogReq req = GoodsChangeLogReq.builder()
                .dataType(DATA_TYPE_GOODS)
                .dataId(changeResult.getAfterGoods().getId())
                .batchNo(batchNo)
                .tenantId(tenantId)
                .userId(userId)
                .build();

        if (changeResult.getBeforeGoods() == null) {
            this.logCreate(changeResult, req);
        } else {
            this.logUpdate(changeResult, req);
        }
    }

    /**
     * 记录新增商品日志
     */
    private void logCreate(GoodsChangeResult changeResult, GoodsChangeLogReq req) {
        Map<String, Object> afterMap = new HashMap<>();
        Goods goods = changeResult.getAfterGoods();
        afterMap.put("id", goods.getId());
        afterMap.put("categoryId", goods.getCategoryId());
        afterMap.put("goodsName", goods.getGoodsName());
        afterMap.put("mainImage", goods.getMainImage());
        afterMap.put("isService", goods.getIsService());
        afterMap.put("status", goods.getStatus());
        afterMap.put("version", goods.getVersion());

        List<GoodsSku> skuList = goodsSkuService.listByGoodsId(goods.getId());
        if (!CollectionUtils.isEmpty(skuList)) {
            afterMap.put("skuList", skuList.stream()
                    .map(sku -> EntityMapUtils.entityToMap(sku, SKU_IGNORE_FIELDS))
                    .collect(Collectors.toList()));
        }

        req.setChangeType(ChangeTypeEnum.CREATE.getCode());
        req.setBeforeChanges(null);
        req.setAfterChanges(afterMap);
        goodsChangeLogService.logChange(req);
    }

    /**
     * 记录修改商品日志
     */
    private void logUpdate(GoodsChangeResult changeResult, GoodsChangeLogReq req) {
        Map<String, Object> beforeMap = new HashMap<>();
        Map<String, Object> afterMap = new HashMap<>();

        if (changeResult.getBeforeChanges() != null && !changeResult.getBeforeChanges().isEmpty()) {
            beforeMap.putAll(changeResult.getBeforeChanges());
        }
        if (changeResult.getAfterChanges() != null && !changeResult.getAfterChanges().isEmpty()) {
            afterMap.putAll(changeResult.getAfterChanges());
        }

        if (!CollectionUtils.isEmpty(changeResult.getSkuChanges())) {
            List<Map<String, Object>> beforeSkuList = new ArrayList<>();
            List<Map<String, Object>> afterSkuList = new ArrayList<>();

            for (SkuChange skuChange : changeResult.getSkuChanges()) {
                if (skuChange.getBeforeSku() != null) {
                    beforeSkuList.add(EntityMapUtils.entityToMap(skuChange.getBeforeSku(), SKU_IGNORE_FIELDS));
                }
                if (skuChange.getAfterSku() != null) {
                    afterSkuList.add(EntityMapUtils.entityToMap(skuChange.getAfterSku(), SKU_IGNORE_FIELDS));
                }
            }

            if (!beforeSkuList.isEmpty()) {
                beforeMap.put("skuList", beforeSkuList);
            }
            if (!afterSkuList.isEmpty()) {
                afterMap.put("skuList", afterSkuList);
            }
        }

        if (beforeMap.isEmpty() && afterMap.isEmpty()) {
            return;
        }

        req.setChangeType(ChangeTypeEnum.UPDATE.getCode());
        req.setBeforeChanges(beforeMap);
        req.setAfterChanges(afterMap);
        goodsChangeLogService.logChange(req);
    }
}
