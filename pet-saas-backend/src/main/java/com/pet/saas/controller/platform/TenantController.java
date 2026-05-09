package com.pet.saas.controller.platform;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.common.R;
import com.pet.saas.common.util.BeanConverter;
import com.pet.saas.dto.query.TenantQuery;
import com.pet.saas.dto.req.CreateTenantReq;
import com.pet.saas.dto.req.UpdateTenantStatusReq;
import com.pet.saas.dto.resp.CreateTenantRespVO;
import com.pet.saas.dto.resp.PageResult;
import com.pet.saas.dto.resp.SysTenantVO;
import com.pet.saas.entity.SysTenant;
import com.pet.saas.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

@Tag(name = "平台管理端-租户管理")
@RestController
@RequestMapping("/api/platform/tenant")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @Operation(summary = "创建租户")
    @PostMapping("/create")
    public R<CreateTenantRespVO> createTenant(@Valid @RequestBody CreateTenantReq req) {
        CreateTenantRespVO result = tenantService.createTenant(req);
        return R.ok(result);
    }

    @Operation(summary = "租户列表")
    @GetMapping("/list")
    public R<PageResult<SysTenantVO>> listTenants(@ParameterObject @Valid TenantQuery query) {
        Page<SysTenant> page = tenantService.listTenants(query);
        return R.ok(BeanConverter.convertToPageResult(page, SysTenantVO.class));
    }

    @Operation(summary = "租户状态管理")
    @PostMapping("/updateStatus")
    public R<Void> updateTenantStatus(@Valid @RequestBody UpdateTenantStatusReq req) {
        tenantService.updateTenantStatus(req);
        return R.ok();
    }
}
