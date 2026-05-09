package com.pet.saas.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.saas.common.enums.StockChangeTypeEnum;
import com.pet.saas.entity.StockRecord;
import com.pet.saas.mapper.StockRecordMapper;
import com.pet.saas.service.StockRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 库存变更记录服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockRecordServiceImpl extends ServiceImpl<StockRecordMapper, StockRecord> implements StockRecordService {

    private final StockRecordMapper stockRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord createStockRecord(
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
    ) {
        String batchNo = IdUtil.fastSimpleUUID().substring(0, 16);

        StockRecord record = new StockRecord();
        record.setTenantId(tenantId);
        record.setSkuId(skuId);
        record.setType(type);
        record.setChangeNum(changeNum);
        record.setBeforeStock(beforeStock);
        record.setAfterStock(afterStock);
        record.setBatchNo(batchNo);
        record.setRelatedType(relatedType);
        record.setRelatedId(relatedId);
        record.setRelatedNo(relatedNo);
        record.setRemark(remark);

        stockRecordMapper.insert(record);
        log.info("创建库存变更记录成功，SKU ID：{}，变更类型：{}，数量：{}", skuId, type, changeNum);

        return stockRecordMapper.selectById(record.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateStockRecord(List<StockRecord> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        String batchNo = IdUtil.fastSimpleUUID().substring(0, 16);
        for (StockRecord record : records) {
            record.setBatchNo(batchNo);
            stockRecordMapper.insert(record);
        }

        log.info("批量创建库存变更记录成功，数量：{}，批次号：{}", records.size(), batchNo);
    }

    @Override
    public List<StockRecord> listBySkuId(Long skuId, Long tenantId) {
        LambdaQueryWrapper<StockRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StockRecord::getTenantId, tenantId)
                .eq(StockRecord::getSkuId, skuId)
                .orderByDesc(StockRecord::getCreateTime);
        return stockRecordMapper.selectList(wrapper);
    }
}
