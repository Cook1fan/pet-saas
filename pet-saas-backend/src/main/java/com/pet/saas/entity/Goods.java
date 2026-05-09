package com.pet.saas.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("goods")
public class Goods extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 分类ID，关联goods_category
     */
    private Long categoryId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品主图URL
     */
    private String mainImage;

    /**
     * 是否服务类商品：0-实物商品，1-服务商品
     */
    private Integer isService;

    /**
     * 状态：0-下架，1-上架
     */
    private Integer status;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;
}
