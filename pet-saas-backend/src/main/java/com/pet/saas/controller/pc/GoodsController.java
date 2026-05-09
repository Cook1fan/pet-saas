package com.pet.saas.controller.pc;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.common.R;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.common.util.BeanConverter;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.query.GoodsChangeLogQuery;
import com.pet.saas.dto.query.GoodsQuery;
import com.pet.saas.dto.req.GoodsSaveReq;
import com.pet.saas.dto.resp.*;
import com.pet.saas.entity.Goods;
import com.pet.saas.entity.GoodsChangeLog;
import com.pet.saas.entity.GoodsSku;
import com.pet.saas.service.*;
import com.pet.saas.service.impl.GoodsChangeLogFormatter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "门店PC端-商品管理")
@RestController
@RequestMapping("/api/pc/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;
    private final GoodsSkuService goodsSkuService;
    private final GoodsCategoryService goodsCategoryService;
    private final FileUploadService fileUploadService;
    private final GoodsChangeLogService goodsChangeLogService;
    private final GoodsChangeLogFormatter goodsChangeLogFormatter;

    @Operation(summary = "商品列表")
    @GetMapping("/list")
    public R<PageResult<GoodsVO>> listGoods(@ParameterObject @Valid GoodsQuery query) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Page<Goods> page = goodsService.listGoods(query, tenantId);

        List<Goods> goodsList = page.getRecords();
        List<GoodsVO> goodsVOList = BeanConverter.convertList(goodsList, GoodsVO.class);

        if (!goodsList.isEmpty()) {
            List<Long> goodsIds = goodsList.stream().map(Goods::getId).collect(Collectors.toList());
            List<GoodsSku> allSkus = goodsSkuService.listByGoodsIds(goodsIds);

            Map<Long, List<GoodsSku>> skuGroupByGoodsId = allSkus.stream()
                    .collect(Collectors.groupingBy(GoodsSku::getGoodsId));

            Map<Long, String> categoryNameMap = buildCategoryNameMap();

            for (GoodsVO vo : goodsVOList) {
                List<GoodsSku> skus = skuGroupByGoodsId.getOrDefault(vo.getId(), List.of());
                List<GoodsSkuVO> skuVOList = BeanConverter.convertList(skus, GoodsSkuVO.class);
                // 填充可销售库存
                for (int i = 0; i < skus.size(); i++) {
                    skuVOList.get(i).setAvailableStock(goodsSkuService.calculateAvailableStock(skus.get(i)));
                }
                vo.setSkuList(skuVOList);
                vo.setCategoryName(categoryNameMap.get(vo.getCategoryId()));
                // 转换商品主图为签名URL
                if (StringUtils.hasText(vo.getMainImage())) {
                    vo.setMainImage(fileUploadService.generatePresignedUrlFromFullUrl(vo.getMainImage()));
                }
            }
        }

        PageResult<GoodsVO> result = new PageResult<>();
        result.setRecords(goodsVOList);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        return R.ok(result);
    }

    private Map<Long, String> buildCategoryNameMap() {
        List<GoodsCategoryVO> categoryTree = goodsCategoryService.getCategoryTree();
        List<GoodsCategoryVO> flatList = this.flattenCategoryTree(categoryTree);
        return flatList.stream()
                .collect(Collectors.toMap(GoodsCategoryVO::getId, GoodsCategoryVO::getCategoryName));
    }

    private List<GoodsCategoryVO> flattenCategoryTree(List<GoodsCategoryVO> tree) {
        List<GoodsCategoryVO> result = new ArrayList<>();
        if (tree == null) {
            return result;
        }
        for (GoodsCategoryVO node : tree) {
            result.add(node);
            if (node.getChildren() != null) {
                result.addAll(this.flattenCategoryTree(node.getChildren()));
            }
        }
        return result;
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{goodsId}")
    public R<GoodsVO> getGoodsDetail(@PathVariable Long goodsId) {
        Goods goods = goodsService.getGoods(goodsId);
        if (goods == null) {
            return R.ok(null);
        }
        GoodsVO vo = BeanConverter.convert(goods, GoodsVO.class);
        List<GoodsSku> skus = goodsSkuService.listByGoodsId(goodsId);
        List<GoodsSkuVO> skuVOList = BeanConverter.convertList(skus, GoodsSkuVO.class);
        // 填充可销售库存
        for (int i = 0; i < skus.size(); i++) {
            skuVOList.get(i).setAvailableStock(goodsSkuService.calculateAvailableStock(skus.get(i)));
        }
        vo.setSkuList(skuVOList);
        // 转换商品主图为签名URL
        if (StringUtils.hasText(vo.getMainImage())) {
            vo.setMainImage(fileUploadService.generatePresignedUrlFromFullUrl(vo.getMainImage()));
        }
        return R.ok(vo);
    }

    @Operation(summary = "商品录入")
    @PostMapping("/save")
    public R<Void> saveGoods(@Valid @RequestBody GoodsSaveReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long userId = StpKit.SHOP.getLoginIdAsLong();
        goodsService.saveGoods(req, tenantId, userId);
        return R.ok();
    }

    @Operation(summary = "商品上架/下架")
    @PutMapping("/{goodsId}/status")
    public R<Void> updateGoodsStatus(@PathVariable Long goodsId, @RequestParam Integer status) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long userId = StpKit.SHOP.getLoginIdAsLong();
        goodsService.updateGoodsStatus(goodsId, status, tenantId, userId);
        return R.ok();
    }

    @Operation(summary = "库存预警列表")
    @GetMapping("/warnList")
    public R<List<GoodsSkuVO>> listWarnGoods() {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        List<GoodsSku> skus = goodsService.listWarnGoods(tenantId);
        return R.ok(BeanConverter.convertList(skus, GoodsSkuVO.class));
    }

    @Operation(summary = "商品变更历史")
    @GetMapping("/change-log")
    public R<PageResult<GoodsChangeLogVO>> listChangeLog(@ParameterObject @Valid GoodsChangeLogQuery query) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Page<GoodsChangeLog> page = goodsChangeLogService.pageByData(query, tenantId);

        List<GoodsChangeLogVO> voList = BeanConverter.convertList(page.getRecords(), GoodsChangeLogVO.class);

        // 格式化为二维结构：goodsChanges + skuChanges
        for (int i = 0; i < page.getRecords().size(); i++) {
            goodsChangeLogFormatter.formatToVo(page.getRecords().get(i), voList.get(i));
        }

        PageResult<GoodsChangeLogVO> result = new PageResult<>();
        result.setRecords(voList);
        result.setTotal(page.getTotal());
        result.setCurrent(page.getCurrent());
        result.setSize(page.getSize());
        result.setPages(page.getPages());
        return R.ok(result);
    }

    @Operation(summary = "扫码查询商品SKU")
    @GetMapping("/sku/scan/{code}")
    public R<GoodsSkuVO> scanSku(@PathVariable String code) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        GoodsSku sku = goodsSkuService.getByCode(code, tenantId);
        if (sku == null) {
            throw new BusinessException(ErrorCode.SKU_NOT_FOUND);
        }
        GoodsSkuVO vo = BeanConverter.convert(sku, GoodsSkuVO.class);
        vo.setAvailableStock(goodsSkuService.calculateAvailableStock(sku));
        return R.ok(vo);
    }

    @Operation(summary = "根据条码查找商品SKU")
    @GetMapping("/sku/by-barcode")
    public R<GoodsSkuVO> getSkuByBarcode(@RequestParam String code) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        GoodsSku sku = goodsSkuService.getByBarcodeOrCode(code, tenantId);
        if (sku == null) {
            return R.error("未找到商品");
        }
        GoodsSkuVO vo = BeanConverter.convert(sku, GoodsSkuVO.class);
        vo.setAvailableStock(goodsSkuService.calculateAvailableStock(sku));
        return R.ok(vo);
    }

    @Operation(summary = "预留库存转实际库存")
    @PostMapping("/sku/transfer-reserved")
    public R<Void> transferReservedToActual(
            @Valid @RequestBody GoodsSkuTransferReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        goodsSkuService.transferReservedToActual(req.getSkuId(), req.getNum());
        return R.ok();
    }

    @Data
    @Schema(description = "预留库存转实际库存请求")
    public static class GoodsSkuTransferReq {
        @Schema(description = "SKU ID", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "SKU ID不能为空")
        private Long skuId;

        @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须大于0")
        private Integer num;
    }
}
