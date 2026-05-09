package com.pet.saas.controller.pc;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.common.R;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.enums.StockChangeTypeEnum;
import com.pet.saas.common.util.BeanConverter;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.query.StockRecordQuery;
import com.pet.saas.dto.req.StockAdjustReq;
import com.pet.saas.dto.req.StockManualInReq;
import com.pet.saas.dto.req.StockManualOutReq;
import com.pet.saas.dto.req.StockInReq;
import com.pet.saas.dto.req.StockOutReq;
import com.pet.saas.dto.resp.PageResult;
import com.pet.saas.dto.resp.StockRecordVO;
import com.pet.saas.entity.Goods;
import com.pet.saas.entity.GoodsSku;
import com.pet.saas.entity.StockRecord;
import com.pet.saas.service.FileUploadService;
import com.pet.saas.service.GoodsService;
import com.pet.saas.service.GoodsSkuService;
import com.pet.saas.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 库存管理控制器
 */
@Tag(name = "门店PC端-库存管理")
@RestController
@RequestMapping("/api/pc/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final GoodsSkuService goodsSkuService;
    private final GoodsService goodsService;
    private final FileUploadService fileUploadService;

    @Operation(summary = "手动入库")
    @PostMapping("/in")
    public R<StockRecordVO> manualInStock(@Valid @RequestBody StockManualInReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long userId = StpKit.SHOP.getLoginIdAsLong();

        StockInReq inReq = new StockInReq();
        inReq.setSkuId(req.getSkuId());
        inReq.setType(StockChangeTypeEnum.MANUAL_IN.getCode());
        inReq.setNum(req.getNum());
        inReq.setRemark(req.getRemark());

        StockRecord record = stockService.inStock(inReq, tenantId, userId);
        return R.ok(BeanConverter.convert(record, StockRecordVO.class));
    }

    @Operation(summary = "手动出库")
    @PostMapping("/out")
    public R<StockRecordVO> manualOutStock(@Valid @RequestBody StockManualOutReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long userId = StpKit.SHOP.getLoginIdAsLong();

        StockOutReq outReq = new StockOutReq();
        outReq.setSkuId(req.getSkuId());
        outReq.setType(StockChangeTypeEnum.MANUAL_OUT.getCode());
        outReq.setNum(req.getNum());
        outReq.setRemark(req.getRemark());

        StockRecord record = stockService.outStock(outReq, tenantId, userId);
        return R.ok(BeanConverter.convert(record, StockRecordVO.class));
    }

    @Operation(summary = "盘点调整")
    @PostMapping("/adjust")
    public R<StockRecordVO> adjustStock(@Valid @RequestBody StockAdjustReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long userId = StpKit.SHOP.getLoginIdAsLong();

        StockRecord record = stockService.adjustStock(req, tenantId, userId);
        return R.ok(BeanConverter.convert(record, StockRecordVO.class));
    }

    @Operation(summary = "库存流水列表")
    @GetMapping("/records")
    public R<PageResult<StockRecordVO>> listStockRecords(@ParameterObject @Valid StockRecordQuery query) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Page<StockRecord> page = stockService.listStockRecord(query, tenantId);

        List<StockRecordVO> voList = this.assembleStockRecordVOs(page.getRecords());

        PageResult<StockRecordVO> result = new PageResult<>();
        result.setRecords(voList);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        return R.ok(result);
    }

    /**
     * 组装库存流水 VO，填充商品规格信息
     */
    private List<StockRecordVO> assembleStockRecordVOs(List<StockRecord> records) {
        if (CollectionUtils.isEmpty(records)) {
            return List.of();
        }

        // 1. 提取所有 skuId
        List<Long> skuIds = records.stream()
                .map(StockRecord::getSkuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 2. 批量查询 SKU 信息
        final Map<Long, GoodsSku> skuMap;
        if (!CollectionUtils.isEmpty(skuIds)) {
            List<GoodsSku> skuList = goodsSkuService.listBySkuIds(skuIds);
            skuMap = skuList.stream()
                    .collect(Collectors.toMap(GoodsSku::getId, Function.identity()));
        } else {
            skuMap = new HashMap<>();
        }

        // 3. 提取所有 goodsId
        List<Long> goodsIds = skuMap.values().stream()
                .map(GoodsSku::getGoodsId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 4. 批量查询商品信息
        final Map<Long, Goods> goodsMap;
        if (!CollectionUtils.isEmpty(goodsIds)) {
            List<Goods> goodsList = goodsService.listByGoodsIds(goodsIds);
            goodsMap = goodsList.stream()
                    .collect(Collectors.toMap(Goods::getId, Function.identity()));
        } else {
            goodsMap = new HashMap<>();
        }

        // 5. 组装 VO
        return records.stream().map(record -> {
            StockRecordVO vo = BeanConverter.convert(record, StockRecordVO.class);

            GoodsSku sku = skuMap.get(record.getSkuId());
            if (sku != null) {
                vo.setBarcode(sku.getBarcode());
                vo.setSpecName(sku.getSpecName());
                vo.setSpecValue(sku.getSpecValue());

                Goods goods = goodsMap.get(sku.getGoodsId());
                if (goods != null) {
                    vo.setGoodsName(goods.getGoodsName());
                    if (StringUtils.hasText(goods.getMainImage())) {
                        vo.setMainImage(fileUploadService.generatePresignedUrlFromFullUrl(goods.getMainImage()));
                    }
                }
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
