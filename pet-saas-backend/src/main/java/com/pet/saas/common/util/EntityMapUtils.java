package com.pet.saas.common.util;

import cn.hutool.core.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实体与 Map 转换工具类
 */
@Slf4j
public class EntityMapUtils {

    private EntityMapUtils() {
    }

    /**
     * 默认忽略的字段
     */
    private static final List<String> DEFAULT_IGNORE_FIELDS = Arrays.asList(
            "createTime", "createUser", "updateTime", "updateUser",
            "isDeleted", "serialVersionUID", "tenantId"
    );

    /**
     * 将实体转换为 Map（过滤默认忽略字段和 null 值）
     *
     * @param entity 实体对象
     * @return Map
     */
    public static Map<String, Object> entityToMap(Object entity) {
        return entityToMap(entity, DEFAULT_IGNORE_FIELDS);
    }

    /**
     * 将实体转换为 Map（过滤指定忽略字段和 null 值）
     *
     * @param entity        实体对象
     * @param ignoreFields  忽略字段列表
     * @return Map
     */
    public static Map<String, Object> entityToMap(Object entity, List<String> ignoreFields) {
        if (entity == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> result = new HashMap<>();
        Field[] fields = ReflectUtil.getFields(entity.getClass());

        List<String> ignoreList = ignoreFields != null ? ignoreFields : DEFAULT_IGNORE_FIELDS;

        for (Field field : fields) {
            if (ignoreList.contains(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            try {
                Object value = field.get(entity);
                if (value != null) {
                    result.put(field.getName(), value);
                }
            } catch (IllegalAccessException e) {
                log.warn("Failed to get field value: {}", field.getName(), e);
            }
        }
        return result;
    }
}
