package com.pet.saas.config.init;

import com.pet.saas.service.GoodsCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsCategoryCacheInitializer implements ApplicationRunner {

    private final GoodsCategoryService goodsCategoryService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始初始化商品分类树缓存");
        try {
            goodsCategoryService.refreshCategoryTreeCache();
            log.info("商品分类树缓存初始化完成");
        } catch (Exception e) {
            log.error("商品分类树缓存初始化失败，不影响系统启动", e);
        }
    }
}
