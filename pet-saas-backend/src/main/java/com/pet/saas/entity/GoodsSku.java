package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.Version;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods_sku")
public class GoodsSku extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 系统内部SKU编码
     */
    private String skuCode;

    /**
     * 规格名称（比如「规格」「重量」「体型」）
     */
    private String specName;

    /**
     * 规格值（比如「1.5kg」「5kg」「小型犬」）
     */
    private String specValue;

    /**
     * 售价
     */
    private BigDecimal price;

    /**
     * 成本价（预留）
     */
    private BigDecimal costPrice;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 是否无限库存：0-否，1-是
     */
    private Integer isUnlimitedStock;

    /**
     * 预留库存（在途库存）
     */
    private Integer reservedStock;

    /**
     * 库存预警值（实物商品用）
     */
    private Integer warnStock;

    /**
     * 商品条码
     */
    private String barcode;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
