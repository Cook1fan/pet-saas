package com.pet.saas.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.dto.req.CreateTenantReq;
import com.pet.saas.dto.req.UpdateTenantStatusReq;
import com.pet.saas.dto.resp.CreateTenantRespVO;
import com.pet.saas.dto.query.TenantQuery;
import com.pet.saas.entity.SysTenant;

public interface TenantService {

    CreateTenantRespVO createTenant(CreateTenantReq req);

    Page<SysTenant> listTenants(TenantQuery query);

    void updateTenantStatus(UpdateTenantStatusReq req);
}
