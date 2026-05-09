package com.pet.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.entity.GoodsSku;
import com.pet.saas.mapper.GoodsSkuMapper;
import com.pet.saas.service.GoodsSkuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GoodsSkuServiceImpl extends ServiceImpl<GoodsSkuMapper, GoodsSku> implements GoodsSkuService {

    private final GoodsSkuMapper goodsSkuMapper;

    @Override
    public List<GoodsSku> listByGoodsId(Long goodsId) {
        return goodsSkuMapper.selectList(
                new LambdaQueryWrapper<GoodsSku>()
                        .eq(GoodsSku::getGoodsId, goodsId)
                        .orderByAsc(GoodsSku::getId)
        );
    }

    @Override
    public List<GoodsSku> listByGoodsIds(List<Long> goodsIds) {
        return goodsSkuMapper.selectList(
                new LambdaQueryWrapper<GoodsSku>()
                        .in(GoodsSku::getGoodsId, goodsIds)
                        .orderByAsc(GoodsSku::getId)
        );
    }

    @Override
    public List<GoodsSku> listBySkuIds(List<Long> skuIds) {
        return goodsSkuMapper.selectList(
                new LambdaQueryWrapper<GoodsSku>()
                        .in(GoodsSku::getId, skuIds)
        );
    }

    @Override
    public GoodsSku getSku(Long skuId) {
        return goodsSkuMapper.selectById(skuId);
    }

    @Override
    public GoodsSku getByCode(String code, Long tenantId) {
        return this.getByBarcodeOrCode(code, tenantId);
    }

    @Override
    public GoodsSku getByBarcodeOrCode(String code, Long tenantId) {
        // 1. 先按 barcode 查询
        GoodsSku sku = goodsSkuMapper.selectOne(
                new LambdaQueryWrapper<GoodsSku>()
                        .eq(GoodsSku::getTenantId, tenantId)
                        .eq(GoodsSku::getBarcode, code)
                        .eq(GoodsSku::getStatus, 1)
                        .last("LIMIT 1")
        );

        if (sku != null) {
            return sku;
        }

        // 2. 再按 sku_code 查询
        return goodsSkuMapper.selectOne(
                new LambdaQueryWrapper<GoodsSku>()
                        .eq(GoodsSku::getTenantId, tenantId)
                        .eq(GoodsSku::getSkuCode, code)
                        .eq(GoodsSku::getStatus, 1)
                        .last("LIMIT 1")
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSku(GoodsSku sku) {
        if (sku.getId() == null) {
            goodsSkuMapper.insert(sku);
        } else {
            goodsSkuMapper.updateById(sku);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSku(GoodsSku sku) {
        goodsSkuMapper.updateById(sku);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSku(Long skuId) {
        goodsSkuMapper.deleteById(skuId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByGoodsId(Long goodsId) {
        goodsSkuMapper.delete(
                new LambdaQueryWrapper<GoodsSku>()
                        .eq(GoodsSku::getGoodsId, goodsId)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStock(Long skuId, int num) {
        // 旧方法，保持兼容性，内部调用新方法
        return this.deductStockWithReserve(skuId, num);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addStock(Long skuId, int num) {
        // 旧方法，保持兼容性，默认增加实际库存
        return this.addStockWithType(skuId, num, 1);
    }

    @Override
    public List<GoodsSku> listWarnSku(Long tenantId) {
        return goodsSkuMapper.selectList(
                new LambdaQueryWrapper<GoodsSku>()
                        .eq(GoodsSku::getStatus, 1)
                        .eq(GoodsSku::getIsUnlimitedStock, 0)
                        .apply("(COALESCE(stock, 0) + COALESCE(reserved_stock, 0)) <= COALESCE(warn_stock, 0)")
        );
    }

    @Override
    public Integer calculateAvailableStock(GoodsSku sku) {
        if (sku == null) {
            return 0;
        }
        // 无限库存
        if (Integer.valueOf(1).equals(sku.getIsUnlimitedStock())) {
            return Integer.MAX_VALUE;
        }
        int stock = sku.getStock() != null ? sku.getStock() : 0;
        int reservedStock = sku.getReservedStock() != null ? sku.getReservedStock() : 0;
        // 可用库存 = 实际库存 - 占用库存
        return stock - reservedStock;
    }

    @Override
    public boolean checkStockAvailable(Long skuId, int num) {
        GoodsSku sku = this.getById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }
        // 无限库存直接返回足够
        if (Integer.valueOf(1).equals(sku.getIsUnlimitedStock())) {
            return true;
        }
        int availableStock = this.calculateAvailableStock(sku);
        return availableStock >= num;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStockWithReserve(Long skuId, int num) {
        GoodsSku sku = this.getById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }

        // 1. 无限库存直接成功
        if (Integer.valueOf(1).equals(sku.getIsUnlimitedStock())) {
            return true;
        }

        int stock = sku.getStock() != null ? sku.getStock() : 0;
        int reservedStock = sku.getReservedStock() != null ? sku.getReservedStock() : 0;
        int totalStock = stock + reservedStock;

        // 2. 检查库存是否足够
        if (totalStock < num) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        // 3. 计算扣减后的库存
        int remainNum = num;
        int newReserved = reservedStock;
        int newStock = stock;

        // 先扣预留库存
        if (newReserved > 0 && remainNum > 0) {
            if (newReserved >= remainNum) {
                newReserved -= remainNum;
                remainNum = 0;
            } else {
                remainNum -= newReserved;
                newReserved = 0;
            }
        }

        // 再扣实际库存
        if (remainNum > 0) {
            newStock -= remainNum;
        }

        // 4. 更新库存（使用 CAS 乐观锁）
        return goodsSkuMapper.updateStockWithReserve(
                skuId,
                newStock,
                newReserved,
                sku.getVersion()
        ) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addStockWithType(Long skuId, int num, Integer type) {
        GoodsSku sku = this.getById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }

        // 无限库存不需要操作
        if (Integer.valueOf(1).equals(sku.getIsUnlimitedStock())) {
            return true;
        }

        int stock = sku.getStock() != null ? sku.getStock() : 0;
        int reservedStock = sku.getReservedStock() != null ? sku.getReservedStock() : 0;

        int newStock = stock;
        int newReserved = reservedStock;

        // type=1: 实际库存入库, type=2: 预留库存入库
        if (Integer.valueOf(2).equals(type)) {
            newReserved += num;
        } else {
            newStock += num;
        }

        return goodsSkuMapper.updateStockWithReserve(
                skuId,
                newStock,
                newReserved,
                sku.getVersion()
        ) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean transferReservedToActual(Long skuId, int num) {
        GoodsSku sku = this.getById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }

        int reservedStock = sku.getReservedStock() != null ? sku.getReservedStock() : 0;
        if (reservedStock < num) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "预留库存不足");
        }

        int stock = sku.getStock() != null ? sku.getStock() : 0;
        int newStock = stock + num;
        int newReserved = reservedStock - num;

        return goodsSkuMapper.updateStockWithReserve(
                skuId,
                newStock,
                newReserved,
                sku.getVersion()
        ) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reserveStock(Long skuId, int num) {
        GoodsSku sku = this.getById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }

        // 无限库存直接成功
        if (Integer.valueOf(1).equals(sku.getIsUnlimitedStock())) {
            return true;
        }

        int stock = sku.getStock() != null ? sku.getStock() : 0;
        int reservedStock = sku.getReservedStock() != null ? sku.getReservedStock() : 0;

        // 检查可用库存是否足够
        if (stock < num) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        int newStock = stock - num;
        int newReserved = reservedStock + num;

        return goodsSkuMapper.updateStockWithReserve(
                skuId,
                newStock,
                newReserved,
                sku.getVersion()
        ) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean releaseReservedStock(Long skuId, int num) {
        GoodsSku sku = this.getById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }

        // 无限库存不需要操作
        if (Integer.valueOf(1).equals(sku.getIsUnlimitedStock())) {
            return true;
        }

        int stock = sku.getStock() != null ? sku.getStock() : 0;
        int reservedStock = sku.getReservedStock() != null ? sku.getReservedStock() : 0;

        // 检查预留库存是否足够
        if (reservedStock < num) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "预留库存不足");
        }

        int newStock = stock + num;
        int newReserved = reservedStock - num;

        return goodsSkuMapper.updateStockWithReserve(
                skuId,
                newStock,
                newReserved,
                sku.getVersion()
        ) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmDeductReservedStock(Long skuId, int num) {
        GoodsSku sku = this.getById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }

        // 无限库存不需要操作
        if (Integer.valueOf(1).equals(sku.getIsUnlimitedStock())) {
            return true;
        }

        int reservedStock = sku.getReservedStock() != null ? sku.getReservedStock() : 0;

        // 检查预留库存是否足够
        if (reservedStock < num) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "预留库存不足");
        }

        int newReserved = reservedStock - num;

        return goodsSkuMapper.updateStockWithReserve(
                skuId,
                sku.getStock(),
                newReserved,
                sku.getVersion()
        ) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStockDirect(Long skuId, int num) {
        GoodsSku sku = this.getById(skuId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }

        // 无限库存直接成功
        if (Integer.valueOf(1).equals(sku.getIsUnlimitedStock())) {
            return true;
        }

        int stock = sku.getStock() != null ? sku.getStock() : 0;

        // 检查库存是否足够
        if (stock < num) {
            throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
        }

        int newStock = stock - num;

        return goodsSkuMapper.updateStockWithReserve(
                skuId,
                newStock,
                sku.getReservedStock(),
                sku.getVersion()
        ) > 0;
    }
}
