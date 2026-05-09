package com.pet.saas.service;

import com.pet.saas.dto.resp.GoodsCategoryVO;

import java.util.List;

public interface GoodsCategoryService {

    List<GoodsCategoryVO> getCategoryTree();

    void refreshCategoryTreeCache();
}
