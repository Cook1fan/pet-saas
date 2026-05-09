package com.pet.saas.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 商品变更日志记录请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsChangeLogReq {

    /**
     * 数据类型：1-goods商品，2-goods_sku商品规格
     */
    private Integer dataType;

    /**
     * 数据ID
     */
    private Long dataId;

    /**
     * 变更类型：1-新增，2-修改，3-删除
     */
    private Integer changeType;

    /**
     * 变更前字段（Map格式，key=字段名，value=字段值）
     */
    private Map<String, Object> beforeChanges;

    /**
     * 变更后字段（Map格式，key=字段名，value=字段值）
     */
    private Map<String, Object> afterChanges;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 操作人ID
     */
    private Long userId;

    /**
     * 实体对象（用于新增/删除时记录所有字段）
     */
    private Object entity;
}
