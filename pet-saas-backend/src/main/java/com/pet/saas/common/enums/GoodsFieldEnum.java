package com.pet.saas.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 商品字段枚举
 * 用于字段名到中文标签的映射，以及值格式化
 */
@Getter
public enum GoodsFieldEnum {

    // ==================== Goods 字段 ====================
    GOODS_NAME("goodsName", "商品名称"),
    MAIN_IMAGE("mainImage", "商品主图"),
    CATEGORY_ID("categoryId", "商品分类"),
    IS_SERVICE("isService", "商品类型", value -> value.equals(1) ? "服务商品" : "实物商品"),
    STATUS("status", "商品状态", value -> value.equals(1) ? "上架" : "下架"),

    // ==================== GoodsSku 字段 ====================
    SPEC_NAME("specName", "规格名称"),
    SPEC_VALUE("specValue", "规格值"),
    PRICE("price", "售价(元)"),
    COST_PRICE("costPrice", "成本价(元)"),
    STOCK("stock", "库存"),
    WARN_STOCK("warnStock", "预警库存"),
    BARCODE("barcode", "商品条码"),
    IS_UNLIMITED_STOCK("isUnlimitedStock", "无限库存", value -> value.equals(1) ? "是" : "否"),
    RESERVED_STOCK("reservedStock", "预留库存"),
    SKU_STATUS("status", "SKU状态", value -> value.equals(1) ? "启用" : "禁用");

    /**
     * 字段名（英文）
     */
    private final String fieldName;

    /**
     * 字段标签（中文）
     */
    private final String label;

    /**
     * 值格式化函数
     */
    private final Function<Object, String> valueFormatter;

    GoodsFieldEnum(String fieldName, String label) {
        this(fieldName, label, null);
    }

    GoodsFieldEnum(String fieldName, String label, Function<Object, String> valueFormatter) {
        this.fieldName = fieldName;
        this.label = label;
        this.valueFormatter = valueFormatter;
    }

    /**
     * 根据字段名获取枚举（商品字段）
     */
    public static GoodsFieldEnum ofGoodsField(String fieldName) {
        return GOODS_FIELD_MAP.get(fieldName);
    }

    /**
     * 根据字段名获取枚举（SKU字段）
     */
    public static GoodsFieldEnum ofSkuField(String fieldName) {
        return SKU_FIELD_MAP.get(fieldName);
    }

    /**
     * 格式化值
     */
    public String formatValue(Object value) {
        if (value == null) {
            return null;
        }
        if (valueFormatter != null) {
            return valueFormatter.apply(value);
        }
        return String.valueOf(value);
    }

    // 商品字段缓存
    private static final Map<String, GoodsFieldEnum> GOODS_FIELD_MAP = Arrays.stream(values())
            .filter(e -> !e.name().startsWith("SKU_"))
            .collect(Collectors.toMap(GoodsFieldEnum::getFieldName, Function.identity()));

    // SKU字段缓存（优先使用 SKU_ 开头的枚举）
    private static final Map<String, GoodsFieldEnum> SKU_FIELD_MAP = Arrays.stream(values())
            .collect(Collectors.toMap(
                    GoodsFieldEnum::getFieldName,
                    Function.identity(),
                    (existing, replacement) -> replacement.name().startsWith("SKU_") ? replacement : existing
            ));
}
