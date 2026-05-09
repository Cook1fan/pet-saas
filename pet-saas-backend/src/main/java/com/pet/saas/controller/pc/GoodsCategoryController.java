package com.pet.saas.controller.pc;

import com.pet.saas.common.R;
import com.pet.saas.dto.resp.GoodsCategoryVO;
import com.pet.saas.service.GoodsCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "门店PC端-商品分类")
@RestController
@RequestMapping("/api/pc/goods-category")
@RequiredArgsConstructor
public class GoodsCategoryController {

    private final GoodsCategoryService goodsCategoryService;

    @Operation(summary = "获取商品分类树")
    @GetMapping("/tree")
    public R<List<GoodsCategoryVO>> getCategoryTree() {
        return R.ok(goodsCategoryService.getCategoryTree());
    }
}
