package com.pet.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.util.BeanConverter;
import com.pet.saas.dto.resp.GoodsCategoryVO;
import com.pet.saas.entity.GoodsCategory;
import com.pet.saas.mapper.GoodsCategoryMapper;
import com.pet.saas.service.GoodsCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoodsCategoryServiceImpl extends ServiceImpl<GoodsCategoryMapper, GoodsCategory> implements GoodsCategoryService {

    private final GoodsCategoryMapper goodsCategoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<GoodsCategoryVO> getCategoryTree() {
        List<GoodsCategoryVO> cacheResult = getCategoryTreeFromCache();
        if (cacheResult != null) {
            return cacheResult;
        }
        refreshCategoryTreeCache();
        return getCategoryTreeFromCache();
    }

    @Override
    public void refreshCategoryTreeCache() {
        RLock lock = redissonClient.getLock(RedisKeyConstants.GOODS_CATEGORY_LOCK_KEY);
        try {
            if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
                try {
                    log.info("开始刷新商品分类树缓存");
                    List<GoodsCategoryVO> categoryTree = buildCategoryTreeFromDB();
                    redisTemplate.opsForValue().set(RedisKeyConstants.GOODS_CATEGORY_TREE_KEY, categoryTree);
                    log.info("商品分类树缓存刷新完成，共{}个一级分类", categoryTree.size());
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("刷新商品分类树缓存被中断", e);
        }
    }

    private List<GoodsCategoryVO> getCategoryTreeFromCache() {
        Object cacheData = redisTemplate.opsForValue().get(RedisKeyConstants.GOODS_CATEGORY_TREE_KEY);
        if (cacheData == null) {
            return null;
        }
        return objectMapper.convertValue(cacheData, new TypeReference<List<GoodsCategoryVO>>() {});
    }

    private List<GoodsCategoryVO> buildCategoryTreeFromDB() {
        List<GoodsCategory> allCategories = goodsCategoryMapper.selectList(
                new LambdaQueryWrapper<GoodsCategory>()
                        .orderByAsc(GoodsCategory::getSort)
        );

        List<GoodsCategoryVO> allVOs = BeanConverter.convertList(allCategories, GoodsCategoryVO.class);

        Map<Long, List<GoodsCategoryVO>> groupByParent = allVOs.stream()
                .collect(Collectors.groupingBy(GoodsCategoryVO::getParentId));

        allVOs.forEach(vo -> vo.setChildren(groupByParent.getOrDefault(vo.getId(), new ArrayList<>())));

        return groupByParent.getOrDefault(0L, new ArrayList<>());
    }
}
