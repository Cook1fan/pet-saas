package com.pet.saas.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.common.R;
import com.pet.saas.common.constant.RedisKeyConstants;
import cn.dev33.satoken.stp.StpUtil;
import com.pet.saas.dto.req.RechargePurchaseReq;
import com.pet.saas.dto.resp.*;
import com.pet.saas.dto.query.MyRecordQuery;
import com.pet.saas.service.RechargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "C端小程序-储值管理")
@RestController
@RequestMapping("/api/user/recharge")
@RequiredArgsConstructor
public class UserRechargeController {

    private final RechargeService rechargeService;

    @Operation(summary = "可用储值规则列表")
    @GetMapping("/rules")
    public R<List<RechargeRuleVO>> getAvailableRechargeRules() {
        Long tenantId = (Long) StpUtil.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        return R.ok(rechargeService.getAvailableRechargeRules(tenantId));
    }

    @Operation(summary = "购买储值")
    @PostMapping("/purchase")
    public R<CreateRechargeOrderResp> purchaseRecharge(@Valid @RequestBody RechargePurchaseReq req) {
        Long tenantId = (Long) StpUtil.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long memberId = (Long) StpUtil.getSession().get("member_id");
        return R.ok(rechargeService.purchaseRecharge(req.getRechargeRuleId(), tenantId, memberId));
    }

    @Operation(summary = "我的储值账户")
    @GetMapping("/account")
    public R<UserRechargeAccountVO> getMyAccount() {
        Long tenantId = (Long) StpUtil.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long memberId = (Long) StpUtil.getSession().get("member_id");
        return R.ok(rechargeService.getMyAccountInfo(tenantId, memberId));
    }

    @Operation(summary = "我的储值记录")
    @GetMapping("/records")
    public R<PageResult<RechargeRecordVO>> getMyRechargeRecords(@ParameterObject @Valid MyRecordQuery query) {
        Long tenantId = (Long) StpUtil.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long memberId = (Long) StpUtil.getSession().get("member_id");
        Page<RechargeRecordVO> page = rechargeService.getMyRechargeRecords(query, tenantId, memberId);
        PageResult<RechargeRecordVO> pageResult = new PageResult<>();
        pageResult.setRecords(page.getRecords());
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setSize(page.getSize());
        pageResult.setPages(page.getPages());
        return R.ok(pageResult);
    }
}
