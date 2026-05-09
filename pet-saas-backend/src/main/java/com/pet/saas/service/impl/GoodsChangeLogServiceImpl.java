package com.pet.saas.service.impl;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.saas.dto.query.GoodsChangeLogQuery;
import com.pet.saas.dto.req.GoodsChangeLogReq;
import com.pet.saas.entity.GoodsChangeLog;
import com.pet.saas.mapper.GoodsChangeLogMapper;
import com.pet.saas.service.GoodsChangeLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品变更日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsChangeLogServiceImpl implements GoodsChangeLogService {

    private final GoodsChangeLogMapper goodsChangeLogMapper;
    private final ObjectMapper objectMapper;

    /**
     * 变更类型：新增
     */
    public static final int CHANGE_TYPE_CREATE = 1;

    /**
     * 变更类型：修改
     */
    public static final int CHANGE_TYPE_UPDATE = 2;

    /**
     * 变更类型：删除
     */
    public static final int CHANGE_TYPE_DELETE = 3;

    /**
     * 需要忽略的字段（不记录变更）
     */
    private static final List<String> IGNORE_FIELDS = Arrays.asList(
            "createTime", "createUser", "updateTime", "updateUser", "isDeleted", "serialVersionUID"
    );

    @Override
    public void logChange(GoodsChangeLogReq req) {
        this.doLogChange(req);
    }

    @Override
    public void logCreate(GoodsChangeLogReq req) {
        Map<String, Object> entityMap = this.entityToMap(req.getEntity());
        GoodsChangeLogReq logReq = GoodsChangeLogReq.builder()
                .dataType(req.getDataType())
                .dataId(req.getDataId())
                .changeType(CHANGE_TYPE_CREATE)
                .beforeChanges(null)
                .afterChanges(entityMap)
                .batchNo(req.getBatchNo())
                .tenantId(req.getTenantId())
                .userId(req.getUserId())
                .build();
        this.doLogChange(logReq);
    }

    @Override
    public void logDelete(GoodsChangeLogReq req) {
        Map<String, Object> entityMap = this.entityToMap(req.getEntity());
        GoodsChangeLogReq logReq = GoodsChangeLogReq.builder()
                .dataType(req.getDataType())
                .dataId(req.getDataId())
                .changeType(CHANGE_TYPE_DELETE)
                .beforeChanges(entityMap)
                .afterChanges(null)
                .batchNo(req.getBatchNo())
                .tenantId(req.getTenantId())
                .userId(req.getUserId())
                .build();
        this.doLogChange(logReq);
    }

    @Override
    public List<GoodsChangeLog> listByBatchNo(String batchNo) {
        if (cn.hutool.core.util.StrUtil.isBlank(batchNo)) {
            return Collections.emptyList();
        }
        return goodsChangeLogMapper.selectList(
                new LambdaQueryWrapper<GoodsChangeLog>()
                        .eq(GoodsChangeLog::getBatchNo, batchNo)
                        .orderByAsc(GoodsChangeLog::getCreateTime)
        );
    }

    @Override
    public List<GoodsChangeLog> listByData(Integer dataType, Long dataId) {
        return goodsChangeLogMapper.selectList(
                new LambdaQueryWrapper<GoodsChangeLog>()
                        .eq(GoodsChangeLog::getDataType, dataType)
                        .eq(GoodsChangeLog::getDataId, dataId)
                        .orderByDesc(GoodsChangeLog::getCreateTime)
        );
    }

    @Override
    public Page<GoodsChangeLog> pageByData(GoodsChangeLogQuery query, Long tenantId) {
        LambdaQueryWrapper<GoodsChangeLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsChangeLog::getTenantId, tenantId);
        if (query.getDataType() != null) {
            wrapper.eq(GoodsChangeLog::getDataType, query.getDataType());
        }
        if (query.getDataId() != null) {
            wrapper.eq(GoodsChangeLog::getDataId, query.getDataId());
        }
        if (cn.hutool.core.util.StrUtil.isNotBlank(query.getBatchNo())) {
            wrapper.eq(GoodsChangeLog::getBatchNo, query.getBatchNo());
        }
        wrapper.orderByDesc(GoodsChangeLog::getCreateTime);
        return goodsChangeLogMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
    }

    /**
     * 实际记录变更（一次操作一条记录）
     */
    private void doLogChange(GoodsChangeLogReq req) {
        GoodsChangeLog changeLog = new GoodsChangeLog();
        changeLog.setTenantId(req.getTenantId());
        changeLog.setDataType(req.getDataType());
        changeLog.setDataId(req.getDataId());
        changeLog.setChangeType(req.getChangeType());
        changeLog.setBeforeValue(this.mapToJson(req.getBeforeChanges()));
        changeLog.setAfterValue(this.mapToJson(req.getAfterChanges()));
        changeLog.setBatchNo(req.getBatchNo());
        changeLog.setCreateUser(req.getUserId());
        changeLog.setUpdateUser(req.getUserId());
        goodsChangeLogMapper.insert(changeLog);
    }

    /**
     * 将实体转换为 Map（过滤忽略字段和 null 值）
     */
    private Map<String, Object> entityToMap(Object entity) {
        Map<String, Object> result = new HashMap<>();
        Field[] fields = ReflectUtil.getFields(entity.getClass());
        for (Field field : fields) {
            if (IGNORE_FIELDS.contains(field.getName())) {
                continue;
            }
            Object value = ReflectUtil.getFieldValue(entity, field);
            if (value != null) {
                result.put(field.getName(), value);
            }
        }
        return result;
    }

    /**
     * 将 Map 转换为 JSON 字符串
     */
    private String mapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("Failed to convert map to json", e);
            return null;
        }
    }
}
