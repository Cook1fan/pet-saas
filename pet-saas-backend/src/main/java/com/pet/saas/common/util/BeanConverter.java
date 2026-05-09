package com.pet.saas.common.util;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.dto.resp.PageResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean 转换工具类
 */
public class BeanConverter {

    private BeanConverter() {
    }

    /**
     * 转换单个对象
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        return BeanUtil.copyProperties(source, targetClass);
    }

    /**
     * 转换列表
     */
    public static <T> List<T> convertList(List<?> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return List.of();
        }
        return sourceList.stream()
                .map(source -> convert(source, targetClass))
                .collect(Collectors.toList());
    }

    /**
     * 转换分页对象
     */
    public static <T> Page<T> convertPage(Page<?> sourcePage, Class<T> targetClass) {
        if (sourcePage == null) {
            return new Page<>();
        }
        Page<T> targetPage = new Page<>(sourcePage.getCurrent(), sourcePage.getSize(), sourcePage.getTotal());
        targetPage.setRecords(convertList(sourcePage.getRecords(), targetClass));
        return targetPage;
    }

    /**
     * 转换为 PageResult 分页响应对象
     */
    public static <T> PageResult<T> convertToPageResult(Page<?> sourcePage, Class<T> targetClass) {
        if (sourcePage == null) {
            return new PageResult<>();
        }
        PageResult<T> result = new PageResult<>();
        result.setRecords(convertList(sourcePage.getRecords(), targetClass));
        result.setTotal(sourcePage.getTotal());
        result.setCurrent(sourcePage.getCurrent());
        result.setSize(sourcePage.getSize());
        result.setPages(sourcePage.getPages());
        return result;
    }
}
