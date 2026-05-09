package com.pet.saas.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.dto.query.GoodsQuery;
import com.pet.saas.dto.req.GoodsSaveReq;
import com.pet.saas.entity.Goods;
import com.pet.saas.entity.GoodsSku;

import java.util.List;

public interface GoodsService {

    Page<Goods> listGoods(GoodsQuery query, Long tenantId);

    List<GoodsSku> listWarnGoods(Long tenantId);

    void saveGoods(GoodsSaveReq req, Long tenantId, Long userId);

    Goods getGoods(Long goodsId);

    List<Goods> listByGoodsIds(List<Long> goodsIds);

    void updateGoodsStatus(Long goodsId, Integer status, Long tenantId, Long userId);
}
