package com.pet.saas.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.dto.query.GoodsChangeLogQuery;
import com.pet.saas.dto.req.GoodsChangeLogReq;
import com.pet.saas.entity.GoodsChangeLog;

import java.util.List;

/**
 * 商品变更日志服务接口
 */
public interface GoodsChangeLogService {

    /**
     * 记录变更（一次性记录所有变更字段）
     * beforeChanges 和 afterChanges 仅包含变更的字段
     */
    void logChange(GoodsChangeLogReq req);

    /**
     * 记录新增（afterValue 存储所有字段的 JSON）
     */
    void logCreate(GoodsChangeLogReq req);

    /**
     * 记录删除（beforeValue 存储所有字段的 JSON）
     */
    void logDelete(GoodsChangeLogReq req);

    /**
     * 查询变更历史（按批次号）
     */
    List<GoodsChangeLog> listByBatchNo(String batchNo);

    /**
     * 查询数据变更历史
     */
    List<GoodsChangeLog> listByData(Integer dataType, Long dataId);

    /**
     * 分页查询数据变更历史
     */
    Page<GoodsChangeLog> pageByData(GoodsChangeLogQuery query, Long tenantId);
}
