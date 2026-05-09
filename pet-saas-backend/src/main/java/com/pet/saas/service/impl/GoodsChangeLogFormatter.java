package com.pet.saas.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.saas.common.enums.GoodsFieldEnum;
import com.pet.saas.dto.resp.GoodsCategoryVO;
import com.pet.saas.dto.resp.GoodsChangeDetailVO;
import com.pet.saas.dto.resp.GoodsChangeLogVO;
import com.pet.saas.entity.GoodsChangeLog;
import com.pet.saas.service.FileUploadService;
import com.pet.saas.service.GoodsCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品变更日志格式化组件
 * 将 JSON 格式的变更数据转换为用户可读的变更详情
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsChangeLogFormatter {

    private final ObjectMapper objectMapper;
    private final FileUploadService fileUploadService;
    private final GoodsCategoryService goodsCategoryService;

    /**
     * 需要过滤的内部字段（不展示给用户）
     */
    private static final Set<String> INTERNAL_FIELDS = Set.of(
            "id", "tenantId", "goodsId", "version",
            "createTime", "createUser", "updateTime", "updateUser", "isDeleted"
    );

    /**
     * SKU列表字段名
     */
    private static final String SKU_LIST_FIELD = "skuList";

    /**
     * 商品主图字段名
     */
    private static final String MAIN_IMAGE_FIELD = "mainImage";

    /**
     * 分类ID字段名
     */
    private static final String CATEGORY_ID_FIELD = "categoryId";

    /**
     * 需要展示的 SKU 字段列表
     */
    private static final Set<String> SKU_FIELDS_TO_SHOW = Set.of(
            "specValue", "price", "costPrice", "barcode",
            "isUnlimitedStock", "status"
    );

    /**
     * 格式化单条变更日志
     *
     * @param changeLog 变更日志实体
     * @return 格式化后的变更详情列表
     */
    public List<GoodsChangeDetailVO> format(GoodsChangeLog changeLog) {
        List<GoodsChangeDetailVO> details = new ArrayList<>();

        try {
            Map<String, Object> beforeMap = this.parseJson(changeLog.getBeforeValue());
            Map<String, Object> afterMap = this.parseJson(changeLog.getAfterValue());

            Map<Long, String> categoryNameMap = this.buildCategoryNameMap();
            Map<String, Object> beforeGoodsFields = this.extractGoodsFields(beforeMap);
            Map<String, Object> afterGoodsFields = this.extractGoodsFields(afterMap);
            List<Map<String, Object>> beforeSkus = this.extractSkuList(beforeMap);
            List<Map<String, Object>> afterSkus = this.extractSkuList(afterMap);

            details.addAll(this.formatGoodsChanges(beforeGoodsFields, afterGoodsFields, categoryNameMap));
            details.addAll(this.formatSkuChanges(beforeSkus, afterSkus));

        } catch (Exception e) {
            log.warn("Failed to format change log, logId: {}, error: {}", changeLog.getId(), e.getMessage());
        }

        return details;
    }

    /**
     * 构建分类ID到名称的映射
     */
    private Map<Long, String> buildCategoryNameMap() {
        try {
            List<GoodsCategoryVO> categoryTree = goodsCategoryService.getCategoryTree();
            Map<Long, String> result = new HashMap<>();
            this.flattenCategoryMap(categoryTree, result);
            return result;
        } catch (Exception e) {
            log.warn("Failed to build category name map", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 扁平化分类树为 Map
     */
    private void flattenCategoryMap(List<GoodsCategoryVO> tree, Map<Long, String> result) {
        if (tree == null || tree.isEmpty()) {
            return;
        }
        for (GoodsCategoryVO category : tree) {
            if (category.getId() != null && category.getCategoryName() != null) {
                result.put(category.getId(), category.getCategoryName());
            }
            if (category.getChildren() != null) {
                this.flattenCategoryMap(category.getChildren(), result);
            }
        }
    }

    /**
     * 解析 JSON 字符串为 Map
     */
    private Map<String, Object> parseJson(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.warn("Failed to parse json: {}", json, e);
            return Collections.emptyMap();
        }
    }

    /**
     * 提取商品基本字段（排除 skuList）
     */
    private Map<String, Object> extractGoodsFields(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }
        return map.entrySet().stream()
                .filter(e -> !SKU_LIST_FIELD.equals(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * 提取 SKU 列表
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractSkuList(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return Collections.emptyList();
        }
        Object skuList = map.get(SKU_LIST_FIELD);
        if (skuList instanceof List) {
            return (List<Map<String, Object>>) skuList;
        }
        return Collections.emptyList();
    }

    /**
     * 对比并格式化商品基本信息变更
     */
    private List<GoodsChangeDetailVO> formatGoodsChanges(Map<String, Object> before, Map<String, Object> after,
                                                         Map<Long, String> categoryNameMap) {
        List<GoodsChangeDetailVO> details = new ArrayList<>();

        Set<String> allFields = new HashSet<>();
        allFields.addAll(before.keySet());
        allFields.addAll(after.keySet());

        for (String fieldName : allFields) {
            if (INTERNAL_FIELDS.contains(fieldName)) {
                continue;
            }

            Object beforeValue = before.get(fieldName);
            Object afterValue = after.get(fieldName);

            if (this.valuesEqual(beforeValue, afterValue)) {
                continue;
            }

            GoodsFieldEnum fieldEnum = GoodsFieldEnum.ofGoodsField(fieldName);
            if (fieldEnum == null) {
                fieldEnum = GoodsFieldEnum.GOODS_NAME;
            }

            GoodsChangeDetailVO detail = new GoodsChangeDetailVO();
            detail.setFieldName(fieldName);
            detail.setFieldLabel(fieldEnum.getLabel());

            if (MAIN_IMAGE_FIELD.equals(fieldName)) {
                detail.setBeforeValue(this.formatImageUrl(beforeValue));
                detail.setAfterValue(this.formatImageUrl(afterValue));
            } else if (CATEGORY_ID_FIELD.equals(fieldName)) {
                detail.setBeforeValue(this.formatCategoryId(beforeValue, categoryNameMap));
                detail.setAfterValue(this.formatCategoryId(afterValue, categoryNameMap));
            } else {
                detail.setBeforeValue(this.formatValue(fieldEnum, beforeValue));
                detail.setAfterValue(this.formatValue(fieldEnum, afterValue));
            }

            details.add(detail);
        }

        return details;
    }

    /**
     * 格式化图片 URL（添加签名）
     */
    private String formatImageUrl(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return fileUploadService.generatePresignedUrlFromFullUrl(String.valueOf(value));
        } catch (Exception e) {
            log.warn("Failed to generate presigned url: {}", value, e);
            return String.valueOf(value);
        }
    }

    /**
     * 格式化分类 ID（转换为分类名称）
     */
    private String formatCategoryId(Object value, Map<Long, String> categoryNameMap) {
        if (value == null) {
            return null;
        }
        try {
            Long categoryId = Long.valueOf(String.valueOf(value));
            String categoryName = categoryNameMap.get(categoryId);
            if (categoryName != null) {
                return categoryName;
            }
            return String.valueOf(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    /**
     * 对比并格式化 SKU 列表变更
     */
    private List<GoodsChangeDetailVO> formatSkuChanges(List<Map<String, Object>> beforeSkus, List<Map<String, Object>> afterSkus) {
        List<GoodsChangeDetailVO> details = new ArrayList<>();

        Set<Object> processedAfterIds = new HashSet<>();
        Set<String> processedAfterKeys = new HashSet<>();

        Map<Object, Map<String, Object>> beforeSkuById = this.groupSkusById(beforeSkus);
        Map<Object, Map<String, Object>> afterSkuById = this.groupSkusById(afterSkus);

        for (Map.Entry<Object, Map<String, Object>> entry : beforeSkuById.entrySet()) {
            Object id = entry.getKey();
            Map<String, Object> beforeSku = entry.getValue();
            Map<String, Object> afterSku = afterSkuById.get(id);

            if (afterSku != null) {
                details.addAll(this.formatSingleSkuChanges(beforeSku, afterSku));
                processedAfterIds.add(id);
            } else {
                details.add(this.createSkuDeleteDetail(beforeSku));
            }
        }

        for (Map.Entry<Object, Map<String, Object>> entry : afterSkuById.entrySet()) {
            if (!processedAfterIds.contains(entry.getKey())) {
                details.add(this.createSkuCreateDetail(entry.getValue()));
            }
        }

        List<Map<String, Object>> remainingBeforeSkus = beforeSkus.stream()
                .filter(sku -> sku.get("id") == null)
                .collect(Collectors.toList());
        List<Map<String, Object>> remainingAfterSkus = afterSkus.stream()
                .filter(sku -> sku.get("id") == null)
                .collect(Collectors.toList());

        if (!remainingBeforeSkus.isEmpty() || !remainingAfterSkus.isEmpty()) {
            this.matchSkusBySpec(remainingBeforeSkus, remainingAfterSkus, details, processedAfterKeys);
        }

        return details;
    }

    /**
     * 按规格名称+规格值匹配 SKU（降级策略）
     */
    private void matchSkusBySpec(List<Map<String, Object>> beforeSkus, List<Map<String, Object>> afterSkus,
                                 List<GoodsChangeDetailVO> details, Set<String> processedKeys) {
        Map<String, Map<String, Object>> beforeByKey = new HashMap<>();
        Map<String, Map<String, Object>> afterByKey = new HashMap<>();

        for (Map<String, Object> sku : beforeSkus) {
            String key = this.getSkuSpecKey(sku);
            if (key != null) {
                beforeByKey.put(key, sku);
            }
        }
        for (Map<String, Object> sku : afterSkus) {
            String key = this.getSkuSpecKey(sku);
            if (key != null) {
                afterByKey.put(key, sku);
            }
        }

        for (Map.Entry<String, Map<String, Object>> entry : beforeByKey.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> beforeSku = entry.getValue();
            Map<String, Object> afterSku = afterByKey.get(key);

            if (afterSku != null) {
                details.addAll(this.formatSingleSkuChanges(beforeSku, afterSku));
                processedKeys.add(key);
            } else {
                details.add(this.createSkuDeleteDetail(beforeSku));
            }
        }

        for (Map.Entry<String, Map<String, Object>> entry : afterByKey.entrySet()) {
            if (!processedKeys.contains(entry.getKey())) {
                details.add(this.createSkuCreateDetail(entry.getValue()));
            }
        }
    }

    /**
     * 获取 SKU 的规格 Key（specName-specValue）
     */
    private String getSkuSpecKey(Map<String, Object> sku) {
        Object specName = sku.get("specName");
        Object specValue = sku.get("specValue");
        if (specName != null && specValue != null) {
            return specName + "-" + specValue;
        }
        return null;
    }

    /**
     * 按 ID 分组 SKU
     */
    private Map<Object, Map<String, Object>> groupSkusById(List<Map<String, Object>> skus) {
        if (skus == null || skus.isEmpty()) {
            return Collections.emptyMap();
        }
        return skus.stream()
                .filter(sku -> sku.get("id") != null)
                .collect(Collectors.toMap(sku -> sku.get("id"), sku -> sku));
    }

    /**
     * 格式化单个 SKU 的字段变更
     */
    private List<GoodsChangeDetailVO> formatSingleSkuChanges(Map<String, Object> beforeSku, Map<String, Object> afterSku) {
        List<GoodsChangeDetailVO> details = new ArrayList<>();
        String skuIdentifier = this.getSkuIdentifier(beforeSku);

        Set<String> allFields = new HashSet<>();
        allFields.addAll(beforeSku.keySet());
        allFields.addAll(afterSku.keySet());

        for (String fieldName : allFields) {
            if (INTERNAL_FIELDS.contains(fieldName)) {
                continue;
            }

            Object beforeValue = beforeSku.get(fieldName);
            Object afterValue = afterSku.get(fieldName);

            if (Objects.equals(beforeValue, afterValue)) {
                continue;
            }

            GoodsFieldEnum fieldEnum = GoodsFieldEnum.ofSkuField(fieldName);
            if (fieldEnum == null) {
                continue;
            }

            GoodsChangeDetailVO detail = new GoodsChangeDetailVO();
            detail.setFieldName(fieldName);
            detail.setFieldLabel(fieldEnum.getLabel());
            detail.setBeforeValue(this.formatValue(fieldEnum, beforeValue));
            detail.setAfterValue(this.formatValue(fieldEnum, afterValue));
            detail.setSkuIdentifier(skuIdentifier);
            details.add(detail);
        }

        return details;
    }

    /**
     * 创建 SKU 新增的变更详情
     */
    private GoodsChangeDetailVO createSkuCreateDetail(Map<String, Object> sku) {
        GoodsChangeDetailVO detail = new GoodsChangeDetailVO();
        detail.setFieldName(SKU_LIST_FIELD);
        detail.setFieldLabel("SKU");
        detail.setBeforeValue(null);
        detail.setAfterValue("新增");
        detail.setSkuIdentifier(this.getSkuIdentifier(sku));
        return detail;
    }

    /**
     * 创建 SKU 删除的变更详情
     */
    private GoodsChangeDetailVO createSkuDeleteDetail(Map<String, Object> sku) {
        GoodsChangeDetailVO detail = new GoodsChangeDetailVO();
        detail.setFieldName(SKU_LIST_FIELD);
        detail.setFieldLabel("SKU");
        detail.setBeforeValue("删除");
        detail.setAfterValue(null);
        detail.setSkuIdentifier(this.getSkuIdentifier(sku));
        return detail;
    }

    /**
     * 获取 SKU 标识（用于展示）
     * 格式：规格名-规格值
     */
    private String getSkuIdentifier(Map<String, Object> sku) {
        if (sku == null) {
            return "未知SKU";
        }
        Object specName = sku.get("specName");
        Object specValue = sku.get("specValue");
        if (specName != null && specValue != null) {
            return specName + "-" + specValue;
        }
        if (specValue != null) {
            return String.valueOf(specValue);
        }
        Object id = sku.get("id");
        if (id != null) {
            return "SKU-" + id;
        }
        return "未知SKU";
    }

    /**
     * 比较两个值是否相等，处理数值类型兼容问题（如 3.0 和 3 视为相等）
     */
    private boolean valuesEqual(Object value1, Object value2) {
        if (Objects.equals(value1, value2)) {
            return true;
        }
        if (value1 instanceof Number && value2 instanceof Number) {
            java.math.BigDecimal bd1 = new java.math.BigDecimal(String.valueOf(value1));
            java.math.BigDecimal bd2 = new java.math.BigDecimal(String.valueOf(value2));
            return bd1.compareTo(bd2) == 0;
        }
        return false;
    }

    /**
     * 格式化单个字段值
     */
    private String formatValue(GoodsFieldEnum fieldEnum, Object value) {
        if (value == null) {
            return null;
        }
        return fieldEnum.formatValue(value);
    }

    /**
     * 格式化变更日志为二维结构（商品属性 + SKU属性分组）
     * 直接填充到 GoodsChangeLogVO 的 goodsChanges 和 skuChanges 字段
     *
     * @param changeLog 变更日志实体
     * @param vo        要填充的VO对象
     */
    public void formatToVo(GoodsChangeLog changeLog, GoodsChangeLogVO vo) {
        try {
            Map<String, Object> beforeMap = this.parseJson(changeLog.getBeforeValue());
            Map<String, Object> afterMap = this.parseJson(changeLog.getAfterValue());

            Map<Long, String> categoryNameMap = this.buildCategoryNameMap();
            Map<String, Object> beforeGoodsFields = this.extractGoodsFields(beforeMap);
            Map<String, Object> afterGoodsFields = this.extractGoodsFields(afterMap);
            List<Map<String, Object>> beforeSkus = this.extractSkuList(beforeMap);
            List<Map<String, Object>> afterSkus = this.extractSkuList(afterMap);

            vo.setGoodsChanges(this.formatGoodsChanges(beforeGoodsFields, afterGoodsFields, categoryNameMap));
            vo.setSkuChanges(this.formatSkuChangesGrouped(beforeSkus, afterSkus));

        } catch (Exception e) {
            log.warn("Failed to format change log to vo, logId: {}, error: {}", changeLog.getId(), e.getMessage());
        }
    }

    /**
     * 格式化SKU列表变更，按SKU ID分组
     */
    private Map<Long, List<GoodsChangeDetailVO>> formatSkuChangesGrouped(
            List<Map<String, Object>> beforeSkus,
            List<Map<String, Object>> afterSkus) {

        Map<Long, List<GoodsChangeDetailVO>> result = new LinkedHashMap<>();

        Map<Object, Map<String, Object>> beforeSkuById = this.groupSkusById(beforeSkus);
        Map<Object, Map<String, Object>> afterSkuById = this.groupSkusById(afterSkus);

        Set<Object> allSkuIds = new HashSet<>();
        allSkuIds.addAll(beforeSkuById.keySet());
        allSkuIds.addAll(afterSkuById.keySet());

        for (Object skuIdObj : allSkuIds) {
            Long skuId = Long.valueOf(String.valueOf(skuIdObj));
            Map<String, Object> beforeSku = beforeSkuById.get(skuIdObj);
            Map<String, Object> afterSku = afterSkuById.get(skuIdObj);

            List<GoodsChangeDetailVO> skuDetails = new ArrayList<>();

            if (beforeSku != null && afterSku != null) {
                this.buildSkuDetailsForUpdate(beforeSku, afterSku, skuDetails);
            } else if (beforeSku != null) {
                this.buildSkuDetailsForCreateOrDelete(beforeSku, skuDetails, true);
            } else {
                this.buildSkuDetailsForCreateOrDelete(afterSku, skuDetails, false);
            }

            if (!skuDetails.isEmpty()) {
                result.put(skuId, skuDetails);
            }
        }

        return result;
    }

    /**
     * 构建 SKU 修改的变更详情（抽取公共方法消除重复）
     */
    private void buildSkuDetailsForUpdate(Map<String, Object> beforeSku, Map<String, Object> afterSku,
                                          List<GoodsChangeDetailVO> skuDetails) {
        for (String fieldName : SKU_FIELDS_TO_SHOW) {
            Object beforeValue = beforeSku.get(fieldName);
            Object afterValue = afterSku.get(fieldName);

            if (this.valuesEqual(beforeValue, afterValue)) {
                continue;
            }

            GoodsFieldEnum fieldEnum = GoodsFieldEnum.ofSkuField(fieldName);
            if (fieldEnum == null) {
                continue;
            }

            GoodsChangeDetailVO detail = new GoodsChangeDetailVO();
            detail.setFieldName(fieldName);
            detail.setFieldLabel(fieldEnum.getLabel());
            detail.setBeforeValue(this.formatValue(fieldEnum, beforeValue));
            detail.setAfterValue(this.formatValue(fieldEnum, afterValue));
            skuDetails.add(detail);
        }
    }

    /**
     * 构建 SKU 新增或删除的变更详情（抽取公共方法消除重复）
     *
     * @param isDelete true=删除（展示原值）, false=新增（展示新值）
     */
    private void buildSkuDetailsForCreateOrDelete(Map<String, Object> sku,
                                                  List<GoodsChangeDetailVO> skuDetails,
                                                  boolean isDelete) {
        for (String fieldName : SKU_FIELDS_TO_SHOW) {
            Object value = sku.get(fieldName);

            GoodsFieldEnum fieldEnum = GoodsFieldEnum.ofSkuField(fieldName);
            if (fieldEnum == null) {
                continue;
            }

            GoodsChangeDetailVO detail = new GoodsChangeDetailVO();
            detail.setFieldName(fieldName);
            detail.setFieldLabel(fieldEnum.getLabel());
            if (isDelete) {
                detail.setBeforeValue(this.formatValue(fieldEnum, value));
                detail.setAfterValue(null);
            } else {
                detail.setBeforeValue(null);
                detail.setAfterValue(this.formatValue(fieldEnum, value));
            }
            skuDetails.add(detail);
        }
    }
}
