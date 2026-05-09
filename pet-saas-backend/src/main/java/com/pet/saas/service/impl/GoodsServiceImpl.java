package com.pet.saas.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.enums.ChangeTypeEnum;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.common.util.BeanConverter;
import com.pet.saas.common.util.CompareUtils;
import com.pet.saas.dto.query.GoodsQuery;
import com.pet.saas.dto.req.GoodsSaveReq;
import com.pet.saas.dto.req.GoodsSkuSaveReq;
import com.pet.saas.entity.Goods;
import com.pet.saas.entity.GoodsSku;
import com.pet.saas.mapper.GoodsMapper;
import com.pet.saas.service.GoodsService;
import com.pet.saas.service.GoodsSkuService;
import com.pet.saas.service.dto.GoodsChangeResult;
import com.pet.saas.service.dto.SkuChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    private final GoodsMapper goodsMapper;
    private final GoodsSkuService goodsSkuService;
    private final RedissonClient redissonClient;
    private final GoodsChangeLogHelper goodsChangeLogHelper;

    /**
     * 分页查询商品列表
     */
    @Override
    public Page<Goods> listGoods(GoodsQuery query, Long tenantId) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (query.getCategoryId() != null) {
            wrapper.eq(Goods::getCategoryId, query.getCategoryId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Goods::getStatus, query.getStatus());
        }
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.like(Goods::getGoodsName, query.getKeyword());
        }
        wrapper.orderByDesc(Goods::getCreateTime);
        return goodsMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    /**
     * 查询库存预警商品列表
     */
    @Override
    public List<GoodsSku> listWarnGoods(Long tenantId) {
        return goodsSkuService.listWarnSku(tenantId);
    }

    /**
     * 保存商品（新增或更新）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveGoods(GoodsSaveReq req, Long tenantId, Long userId) {
        RLock lock = null;
        if (req.getId() != null) {
            String lockKey = String.format(RedisKeyConstants.GOODS_EDIT_LOCK_KEY, tenantId, req.getId());
            lock = redissonClient.getLock(lockKey);
            try {
                if (!lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                    throw new BusinessException(ErrorCode.GOODS_BEING_EDITED);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(ErrorCode.INTERNAL_ERROR.getCode(), "系统异常");
            }
        }

        try {
            String batchNo = IdUtil.fastSimpleUUID();
            GoodsChangeResult changeResult;

            if (req.getId() == null) {
                changeResult = this.createGoods(req, tenantId, userId);
            } else {
                changeResult = this.updateGoods(req, tenantId, userId);
            }

            goodsChangeLogHelper.logCompleteChange(changeResult, batchNo, tenantId, userId);

        } finally {
            if (lock != null && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 根据ID查询商品详情
     */
    @Override
    public Goods getGoods(Long goodsId) {
        return goodsMapper.selectById(goodsId);
    }

    /**
     * 根据ID列表批量查询商品
     */
    @Override
    public List<Goods> listByGoodsIds(List<Long> goodsIds) {
        return goodsMapper.selectList(
                new LambdaQueryWrapper<Goods>()
                        .in(Goods::getId, goodsIds)
        );
    }

    /**
     * 更新商品上架/下架状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoodsStatus(Long goodsId, Integer status, Long tenantId, Long userId) {
        Goods beforeGoods = goodsMapper.selectById(goodsId);
        if (beforeGoods == null) {
            throw new BusinessException(ErrorCode.GOODS_NOT_FOUND);
        }

        if (CompareUtils.valuesEqual(beforeGoods.getStatus(), status)) {
            return;
        }

        Goods goods = new Goods();
        goods.setId(goodsId);
        goods.setStatus(status);
        goods.setUpdateUser(userId);
        goodsMapper.updateById(goods);

        Goods afterGoods = BeanConverter.convert(beforeGoods, Goods.class);
        afterGoods.setStatus(status);

        Map<String, Object> beforeChanges = new HashMap<>();
        Map<String, Object> afterChanges = new HashMap<>();
        beforeChanges.put("status", beforeGoods.getStatus());
        afterChanges.put("status", status);

        String batchNo = IdUtil.fastSimpleUUID();
        GoodsChangeResult changeResult = GoodsChangeResult.builder()
                .beforeGoods(beforeGoods)
                .afterGoods(afterGoods)
                .beforeChanges(beforeChanges)
                .afterChanges(afterChanges)
                .skuChanges(Collections.emptyList())
                .build();

        goodsChangeLogHelper.logCompleteChange(changeResult, batchNo, tenantId, userId);
    }

    private GoodsChangeResult createGoods(GoodsSaveReq req, Long tenantId, Long userId) {
        Goods goods = BeanConverter.convert(req, Goods.class);
        goods.setTenantId(tenantId);
        goods.setCreateUser(userId);
        goods.setUpdateUser(userId);
        goods.setVersion(0);
        if (goods.getStatus() == null) {
            goods.setStatus(1);
        }
        goodsMapper.insert(goods);

        List<SkuChange> skuChanges = this.saveSkuIncremental(goods.getId(), req.getSkuList(), tenantId, userId);

        return GoodsChangeResult.builder()
                .beforeGoods(null)
                .afterGoods(goods)
                .beforeChanges(null)
                .afterChanges(null)
                .skuChanges(skuChanges)
                .build();
    }

    private GoodsChangeResult updateGoods(GoodsSaveReq req, Long tenantId, Long userId) {
        Goods beforeGoods = goodsMapper.selectById(req.getId());
        if (beforeGoods == null) {
            throw new BusinessException(ErrorCode.GOODS_NOT_FOUND);
        }

        if (req.getVersion() == null || !req.getVersion().equals(beforeGoods.getVersion())) {
            throw new BusinessException(ErrorCode.GOODS_VERSION_MISMATCH);
        }

        GoodsChangeResult.GoodsChangeResultBuilder resultBuilder = GoodsChangeResult.builder();
        resultBuilder.beforeGoods(beforeGoods);

        Goods afterGoods = BeanConverter.convert(beforeGoods, Goods.class);
        this.updateGoodsIfChanged(afterGoods, req, userId, resultBuilder);

        List<SkuChange> skuChanges = this.saveSkuIncremental(afterGoods.getId(), req.getSkuList(), tenantId, userId);

        resultBuilder.afterGoods(afterGoods);
        resultBuilder.skuChanges(skuChanges);

        return resultBuilder.build();
    }

    private void updateGoodsIfChanged(Goods goods, GoodsSaveReq req, Long userId,
                                       GoodsChangeResult.GoodsChangeResultBuilder resultBuilder) {
        Goods temp = new Goods();
        temp.setId(goods.getId());
        temp.setVersion(goods.getVersion());
        boolean hasChange = false;

        Map<String, Object> beforeChanges = new HashMap<>();
        Map<String, Object> afterChanges = new HashMap<>();

        if (!CompareUtils.valuesEqual(goods.getCategoryId(), req.getCategoryId())) {
            temp.setCategoryId(req.getCategoryId());
            beforeChanges.put("categoryId", goods.getCategoryId());
            afterChanges.put("categoryId", req.getCategoryId());
            goods.setCategoryId(req.getCategoryId());
            hasChange = true;
        }
        if (!Objects.equals(goods.getGoodsName(), req.getGoodsName())) {
            temp.setGoodsName(req.getGoodsName());
            beforeChanges.put("goodsName", goods.getGoodsName());
            afterChanges.put("goodsName", req.getGoodsName());
            goods.setGoodsName(req.getGoodsName());
            hasChange = true;
        }
        if (!Objects.equals(goods.getMainImage(), req.getMainImage())) {
            temp.setMainImage(req.getMainImage());
            beforeChanges.put("mainImage", goods.getMainImage());
            afterChanges.put("mainImage", req.getMainImage());
            goods.setMainImage(req.getMainImage());
            hasChange = true;
        }
        if (!CompareUtils.valuesEqual(goods.getIsService(), req.getIsService())) {
            temp.setIsService(req.getIsService());
            beforeChanges.put("isService", goods.getIsService());
            afterChanges.put("isService", req.getIsService());
            goods.setIsService(req.getIsService());
            hasChange = true;
        }
        if (req.getStatus() != null && !CompareUtils.valuesEqual(goods.getStatus(), req.getStatus())) {
            temp.setStatus(req.getStatus());
            beforeChanges.put("status", goods.getStatus());
            afterChanges.put("status", req.getStatus());
            goods.setStatus(req.getStatus());
            hasChange = true;
        }

        if (hasChange) {
            temp.setUpdateUser(userId);
            int updated = goodsMapper.updateById(temp);
            if (updated == 0) {
                throw new BusinessException(ErrorCode.GOODS_VERSION_MISMATCH);
            }
            goods.setVersion(goods.getVersion() + 1);
            resultBuilder.beforeChanges(beforeChanges);
            resultBuilder.afterChanges(afterChanges);
        }
    }

    private List<SkuChange> saveSkuIncremental(Long goodsId, List<GoodsSkuSaveReq> skuReqList,
                                                  Long tenantId, Long userId) {
        List<SkuChange> skuChanges = new ArrayList<>();

        List<GoodsSku> existSkuList = goodsSkuService.listByGoodsId(goodsId);
        Map<Long, GoodsSku> existSkuMap = existSkuList.stream()
                .collect(Collectors.toMap(GoodsSku::getId, Function.identity()));

        Set<Long> frontSkuIds = new HashSet<>();
        if (!CollectionUtils.isEmpty(skuReqList)) {
            for (GoodsSkuSaveReq skuReq : skuReqList) {
                if (skuReq.getId() != null) {
                    frontSkuIds.add(skuReq.getId());
                    GoodsSku existSku = existSkuMap.get(skuReq.getId());
                    if (existSku == null) {
                        throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
                    }
                    SkuChange change = this.updateSkuIfChanged(existSku, skuReq, userId);
                    if (change != null) {
                        skuChanges.add(change);
                    }
                } else {
                    GoodsSku newSku = this.createSku(skuReq, goodsId, tenantId, userId);
                    frontSkuIds.add(newSku.getId());
                    skuChanges.add(SkuChange.builder()
                            .changeType(ChangeTypeEnum.CREATE.getCode())
                            .beforeSku(null)
                            .afterSku(newSku)
                            .build());
                }
            }
        }

        for (GoodsSku existSku : existSkuList) {
            if (!frontSkuIds.contains(existSku.getId())) {
                this.deleteSku(existSku);
                skuChanges.add(SkuChange.builder()
                        .changeType(ChangeTypeEnum.DELETE.getCode())
                        .beforeSku(existSku)
                        .afterSku(null)
                        .build());
            }
        }

        return skuChanges;
    }

    private SkuChange updateSkuIfChanged(GoodsSku existSku, GoodsSkuSaveReq skuReq, Long userId) {
        GoodsSku temp = new GoodsSku();
        temp.setId(existSku.getId());
        temp.setVersion(existSku.getVersion());
        boolean hasChange = false;

        GoodsSku afterSku = BeanConverter.convert(existSku, GoodsSku.class);

        if (!Objects.equals(existSku.getSpecName(), skuReq.getSpecName())) {
            temp.setSpecName(skuReq.getSpecName());
            afterSku.setSpecName(skuReq.getSpecName());
            hasChange = true;
        }
        if (!Objects.equals(existSku.getSpecValue(), skuReq.getSpecValue())) {
            temp.setSpecValue(skuReq.getSpecValue());
            afterSku.setSpecValue(skuReq.getSpecValue());
            hasChange = true;
        }
        if (!CompareUtils.valuesEqual(existSku.getPrice(), skuReq.getPrice())) {
            temp.setPrice(skuReq.getPrice());
            afterSku.setPrice(skuReq.getPrice());
            hasChange = true;
        }
        if (!CompareUtils.valuesEqual(existSku.getCostPrice(), skuReq.getCostPrice())) {
            temp.setCostPrice(skuReq.getCostPrice());
            afterSku.setCostPrice(skuReq.getCostPrice());
            hasChange = true;
        }
        Integer newIsUnlimited = skuReq.getIsUnlimitedStock();
        if (!CompareUtils.valuesEqual(existSku.getIsUnlimitedStock(), newIsUnlimited)) {
            temp.setIsUnlimitedStock(newIsUnlimited);
            afterSku.setIsUnlimitedStock(newIsUnlimited);
            hasChange = true;
        }
        if (!Objects.equals(existSku.getBarcode(), skuReq.getBarcode())) {
            temp.setBarcode(skuReq.getBarcode());
            afterSku.setBarcode(skuReq.getBarcode());
            hasChange = true;
        }
        if (skuReq.getStatus() != null && !CompareUtils.valuesEqual(existSku.getStatus(), skuReq.getStatus())) {
            temp.setStatus(skuReq.getStatus());
            afterSku.setStatus(skuReq.getStatus());
            hasChange = true;
        }

        if (hasChange) {
            temp.setUpdateUser(userId);
            goodsSkuService.updateSku(temp);
            afterSku.setVersion(afterSku.getVersion() + 1);
            return SkuChange.builder()
                    .changeType(ChangeTypeEnum.UPDATE.getCode())
                    .beforeSku(existSku)
                    .afterSku(afterSku)
                    .build();
        }
        return null;
    }

    private GoodsSku createSku(GoodsSkuSaveReq skuReq, Long goodsId, Long tenantId, Long userId) {
        GoodsSku sku = BeanConverter.convert(skuReq, GoodsSku.class);
        sku.setTenantId(tenantId);
        sku.setGoodsId(goodsId);
        sku.setCreateUser(userId);
        sku.setUpdateUser(userId);
        sku.setSkuCode(String.valueOf(IdUtil.getSnowflakeNextId()));
        if (sku.getStatus() == null) {
            sku.setStatus(1);
        }
        if (sku.getIsUnlimitedStock() == null) {
            sku.setIsUnlimitedStock(0);
        }
        sku.setStock(0);
        sku.setReservedStock(0);
        sku.setWarnStock(0);
        sku.setVersion(0);
        goodsSkuService.saveSku(sku);
        return sku;
    }

    private void deleteSku(GoodsSku existSku) {
        goodsSkuService.deleteSku(existSku.getId());
    }
}
