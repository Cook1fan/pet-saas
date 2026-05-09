package com.pet.saas.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pet.saas.entity.GoodsSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface GoodsSkuMapper extends BaseMapper<GoodsSku> {

    @Update("UPDATE goods_sku SET stock = stock - #{num} WHERE id = #{skuId} AND (stock = -1 OR stock >= #{num})")
    int deductStock(@Param("skuId") Long skuId, @Param("num") int num);

    @Update("UPDATE goods_sku SET stock = stock + #{num} WHERE id = #{skuId} AND stock != -1")
    int addStock(@Param("skuId") Long skuId, @Param("num") int num);

    /**
     * 使用 CAS 更新库存（配合乐观锁）
     */
    @Update("UPDATE goods_sku SET stock = #{afterStock}, version = version + 1 " +
            "WHERE id = #{skuId} AND stock = #{beforeStock} AND version = #{version} AND is_deleted = 0")
    int updateStock(@Param("skuId") Long skuId,
                    @Param("beforeStock") int beforeStock,
                    @Param("afterStock") int afterStock,
                    @Param("version") Integer version);

    /**
     * 更新库存和预留库存（配合乐观锁）
     */
    @Update("UPDATE goods_sku " +
            "SET stock = #{newStock}, reserved_stock = #{newReserved}, version = version + 1 " +
            "WHERE id = #{skuId} AND version = #{version} AND is_deleted = 0")
    int updateStockWithReserve(@Param("skuId") Long skuId,
                               @Param("newStock") int newStock,
                               @Param("newReserved") int newReserved,
                               @Param("version") Integer version);
}
