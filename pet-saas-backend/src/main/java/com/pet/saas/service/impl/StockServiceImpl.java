package com.pet.saas.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.enums.StockChangeTypeEnum;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.dto.query.StockRecordQuery;
import com.pet.saas.dto.req.StockAdjustReq;
import com.pet.saas.dto.req.StockChangeContext;
import com.pet.saas.dto.req.StockInReq;
import com.pet.saas.dto.req.StockOutReq;
import com.pet.saas.entity.GoodsSku;
import com.pet.saas.entity.StockRecord;
import com.pet.saas.mapper.GoodsSkuMapper;
import com.pet.saas.mapper.StockRecordMapper;
import com.pet.saas.service.GoodsSkuService;
import com.pet.saas.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 库存服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final GoodsSkuService goodsSkuService;
    private final GoodsSkuMapper goodsSkuMapper;
    private final StockRecordMapper stockRecordMapper;
    private final RedissonClient redissonClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord inStock(StockInReq req, Long tenantId, Long userId) {
        StockChangeContext context = StockChangeContext.builder()
                .skuId(req.getSkuId())
                .type(req.getType())
                .changeNum(req.getNum())
                .relatedType(req.getRelatedType())
                .relatedId(req.getRelatedId())
                .relatedNo(req.getRelatedNo())
                .remark(req.getRemark())
                .tenantId(tenantId)
                .userId(userId)
                .build();
        return this.doStockChange(context);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord outStock(StockOutReq req, Long tenantId, Long userId) {
        StockChangeContext context = StockChangeContext.builder()
                .skuId(req.getSkuId())
                .type(req.getType())
                .changeNum(-req.getNum())
                .relatedType(req.getRelatedType())
                .relatedId(req.getRelatedId())
                .relatedNo(req.getRelatedNo())
                .remark(req.getRemark())
                .tenantId(tenantId)
                .userId(userId)
                .build();
        return this.doStockChange(context);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StockRecord> batchInStock(List<StockInReq> reqList, Long tenantId, Long userId) {
        if (CollectionUtils.isEmpty(reqList)) {
            return new ArrayList<>();
        }
        String batchNo = this.generateBatchNo();
        List<StockRecord> result = new ArrayList<>();
        for (StockInReq req : reqList) {
            StockChangeContext context = StockChangeContext.builder()
                    .skuId(req.getSkuId())
                    .type(req.getType())
                    .changeNum(req.getNum())
                    .relatedType(req.getRelatedType())
                    .relatedId(req.getRelatedId())
                    .relatedNo(req.getRelatedNo())
                    .remark(req.getRemark())
                    .batchNo(batchNo)
                    .tenantId(tenantId)
                    .userId(userId)
                    .build();
            StockRecord record = this.doStockChange(context);
            result.add(record);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<StockRecord> batchOutStock(List<StockOutReq> reqList, Long tenantId, Long userId) {
        if (CollectionUtils.isEmpty(reqList)) {
            return new ArrayList<>();
        }
        String batchNo = this.generateBatchNo();
        List<StockRecord> result = new ArrayList<>();
        for (StockOutReq req : reqList) {
            StockChangeContext context = StockChangeContext.builder()
                    .skuId(req.getSkuId())
                    .type(req.getType())
                    .changeNum(-req.getNum())
                    .relatedType(req.getRelatedType())
                    .relatedId(req.getRelatedId())
                    .relatedNo(req.getRelatedNo())
                    .remark(req.getRemark())
                    .batchNo(batchNo)
                    .tenantId(tenantId)
                    .userId(userId)
                    .build();
            StockRecord record = this.doStockChange(context);
            result.add(record);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord adjustStock(StockAdjustReq req, Long tenantId, Long userId) {
        GoodsSku sku = goodsSkuService.getSku(req.getSkuId());
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }
        int changeNum = req.getTargetStock() - sku.getStock();
        if (changeNum == 0) {
            throw new BusinessException(ErrorCode.STOCK_NO_CHANGE);
        }
        StockChangeContext context = StockChangeContext.builder()
                .skuId(req.getSkuId())
                .type(StockChangeTypeEnum.ADJUST.getCode())
                .changeNum(changeNum)
                .remark(req.getRemark())
                .tenantId(tenantId)
                .userId(userId)
                .build();
        return this.doStockChange(context);
    }

    @Override
    public Page<StockRecord> listStockRecord(StockRecordQuery query, Long tenantId) {
        LambdaQueryWrapper<StockRecord> wrapper = new LambdaQueryWrapper<>();

        // 如果有 barcode 参数，先根据 barcode 查询 skuId
        if (cn.hutool.core.util.StrUtil.isNotBlank(query.getBarcode())) {
            GoodsSku sku = goodsSkuService.getByBarcodeOrCode(query.getBarcode(), tenantId);
            if (sku != null) {
                wrapper.eq(StockRecord::getSkuId, sku.getId());
            } else {
                // barcode 不存在，返回空结果
                return new Page<>(query.getPageNum(), query.getPageSize());
            }
        } else if (query.getSkuId() != null) {
            wrapper.eq(StockRecord::getSkuId, query.getSkuId());
        }

        if (query.getType() != null) {
            wrapper.eq(StockRecord::getType, query.getType());
        }
        if (cn.hutool.core.util.StrUtil.isNotBlank(query.getBatchNo())) {
            wrapper.eq(StockRecord::getBatchNo, query.getBatchNo());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(StockRecord::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(StockRecord::getCreateTime, query.getEndTime());
        }
        wrapper.orderByDesc(StockRecord::getCreateTime);
        return stockRecordMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserveStock(Long skuId, int num, Long tenantId, Long userId, String relatedNo) {
        this.executeWithLock(skuId, tenantId, (sku, stock, reservedStock) -> {
            if (stock - reservedStock < num) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
            }
            sku.setReservedStock(reservedStock + num);
            return StockOperationResult.builder()
                    .newStock(stock)
                    .newReservedStock(reservedStock + num)
                    .logMessage(String.format("预占库存成功：skuId=%d, num=%d, stock=%d, reserved=%d→%d",
                            skuId, num, stock, reservedStock, reservedStock + num))
                    .build();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseReservedStock(Long skuId, int num, Long tenantId, Long userId, String relatedNo) {
        this.executeWithLock(skuId, tenantId, (sku, stock, reservedStock) -> {
            int actualNum = num;
            if (reservedStock < actualNum) {
                log.warn("预留库存不足，skuId={}, reservedStock={}, num={}", skuId, reservedStock, actualNum);
                actualNum = reservedStock;
            }
            sku.setReservedStock(reservedStock - actualNum);
            return StockOperationResult.builder()
                    .newStock(stock)
                    .newReservedStock(reservedStock - actualNum)
                    .logMessage(String.format("释放预占库存成功：skuId=%d, num=%d, stock=%d, reserved=%d→%d",
                            skuId, actualNum, stock, reservedStock, reservedStock - actualNum))
                    .build();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmDeductStock(Long skuId, int num, Long tenantId, Long userId, String relatedNo) {
        this.executeWithLock(skuId, tenantId, (sku, stock, reservedStock) -> {
            if (reservedStock < num) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH, "预留库存不足");
            }
            sku.setStock(stock - num);
            sku.setReservedStock(reservedStock - num);

            StockRecord record = new StockRecord();
            record.setTenantId(tenantId);
            record.setSkuId(skuId);
            record.setType(StockChangeTypeEnum.SALE_OUT.getCode());
            record.setChangeNum(-num);
            record.setBeforeStock(stock);
            record.setAfterStock(stock - num);
            record.setBatchNo(this.generateBatchNo());
            record.setRelatedType("order");
            record.setRelatedNo(relatedNo);
            record.setRemark("订单支付确认扣减");
            record.setCreateUser(userId);
            record.setUpdateUser(userId);

            return StockOperationResult.builder()
                    .newStock(stock - num)
                    .newReservedStock(reservedStock - num)
                    .stockRecord(record)
                    .logMessage(String.format("确认扣减库存成功：skuId=%d, num=%d, beforeStock=%d, afterStock=%d, reserved=%d→%d",
                            skuId, num, stock, stock - num, reservedStock, reservedStock - num))
                    .build();
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductStockDirect(Long skuId, int num, Long tenantId, Long userId, String relatedNo) {
        this.executeWithLock(skuId, tenantId, (sku, stock, reservedStock) -> {
            if (stock - reservedStock < num) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
            }
            sku.setStock(stock - num);

            StockRecord record = new StockRecord();
            record.setTenantId(tenantId);
            record.setSkuId(skuId);
            record.setType(StockChangeTypeEnum.SALE_OUT.getCode());
            record.setChangeNum(-num);
            record.setBeforeStock(stock);
            record.setAfterStock(stock - num);
            record.setBatchNo(this.generateBatchNo());
            record.setRelatedType("order");
            record.setRelatedNo(relatedNo);
            record.setRemark("开单销售");
            record.setCreateUser(userId);
            record.setUpdateUser(userId);

            return StockOperationResult.builder()
                    .newStock(stock - num)
                    .newReservedStock(reservedStock)
                    .stockRecord(record)
                    .logMessage(String.format("直接扣减库存成功：skuId=%d, num=%d, beforeStock=%d, afterStock=%d",
                            skuId, num, stock, stock - num))
                    .build();
        });
    }

    /**
     * 股票操作回调函数
     */
    @FunctionalInterface
    private interface StockOperationCallback {
        StockOperationResult apply(GoodsSku sku, int stock, int reservedStock);
    }

    /**
     * 股票操作结果
     */
    @lombok.Builder
    @lombok.Data
    private static class StockOperationResult {
        private int newStock;
        private int newReservedStock;
        private StockRecord stockRecord;
        private String logMessage;
    }

    /**
     * 带分布式锁的库存操作模板方法
     */
    private void executeWithLock(Long skuId, Long tenantId, StockOperationCallback callback) {
        String lockKey = String.format(RedisKeyConstants.STOCK_CHANGE_LOCK_KEY, tenantId, skuId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.STOCK_BEING_CHANGED);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }

        try {
            GoodsSku sku = goodsSkuService.getSku(skuId);
            if (sku == null) {
                throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
            }
            if (Integer.valueOf(1).equals(sku.getIsUnlimitedStock())) {
                return;
            }

            int stock = sku.getStock() != null ? sku.getStock() : 0;
            int reservedStock = sku.getReservedStock() != null ? sku.getReservedStock() : 0;

            // 执行回调，进行具体的库存操作
            StockOperationResult result = callback.apply(sku, stock, reservedStock);

            // 更新 SKU
            int updated = goodsSkuMapper.updateById(sku);
            if (updated == 0) {
                throw new BusinessException(ErrorCode.STOCK_VERSION_MISMATCH);
            }

            // 插入库存记录（如果有）
            if (result.getStockRecord() != null) {
                stockRecordMapper.insert(result.getStockRecord());
            }

            // 记录日志
            if (result.getLogMessage() != null) {
                log.info(result.getLogMessage());
            }

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 统一库存变动处理（旧方法，保留兼容性）
     */
    private StockRecord doStockChange(StockChangeContext context) {
        String batchNo = context.getBatchNo();
        if (batchNo == null) {
            batchNo = this.generateBatchNo();
        }

        Long skuId = context.getSkuId();
        Long tenantId = context.getTenantId();

        String lockKey = String.format(RedisKeyConstants.STOCK_CHANGE_LOCK_KEY, tenantId, skuId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                throw new BusinessException(ErrorCode.STOCK_BEING_CHANGED);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }

        try {
            GoodsSku sku = goodsSkuService.getSku(skuId);
            if (sku == null) {
                throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
            }
            if (sku.getStock() == null || sku.getStock() == -1) {
                throw new BusinessException(ErrorCode.SERVICE_GOODS_NO_STOCK);
            }

            int beforeStock = sku.getStock();
            int changeNum = context.getChangeNum();
            if (changeNum < 0 && beforeStock + changeNum < 0) {
                throw new BusinessException(ErrorCode.STOCK_NOT_ENOUGH);
            }

            int afterStock = beforeStock + changeNum;

            boolean updated = goodsSkuMapper.updateStock(skuId, beforeStock, afterStock, sku.getVersion()) > 0;
            if (!updated) {
                throw new BusinessException(ErrorCode.STOCK_VERSION_MISMATCH);
            }

            StockRecord record = new StockRecord();
            record.setTenantId(tenantId);
            record.setSkuId(skuId);
            record.setType(context.getType());
            record.setChangeNum(changeNum);
            record.setBeforeStock(beforeStock);
            record.setAfterStock(afterStock);
            record.setBatchNo(batchNo);
            record.setRelatedType(context.getRelatedType());
            record.setRelatedId(context.getRelatedId());
            record.setRelatedNo(context.getRelatedNo());
            record.setRemark(context.getRemark());
            record.setCreateUser(context.getUserId());
            record.setUpdateUser(context.getUserId());
            stockRecordMapper.insert(record);

            log.info("库存变动成功：skuId={}, type={}, before={}, change={}, after={}",
                    skuId, context.getType(), beforeStock, changeNum, afterStock);

            return record;

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 生成批次号
     */
    private String generateBatchNo() {
        return IdUtil.fastSimpleUUID();
    }
}
