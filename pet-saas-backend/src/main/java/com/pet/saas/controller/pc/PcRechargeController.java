package com.pet.saas.controller.pc;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.common.R;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.req.RechargeRuleSaveReq;
import com.pet.saas.dto.resp.*;
import com.pet.saas.dto.query.RechargeRuleQuery;
import com.pet.saas.dto.query.MemberAccountQuery;
import com.pet.saas.dto.query.RechargeRecordQuery;
import com.pet.saas.entity.RechargeRule;
import com.pet.saas.service.RechargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

@Tag(name = "门店PC端-储值管理")
@RestController
@RequestMapping("/api/pc/recharge")
@RequiredArgsConstructor
public class PcRechargeController {

    private final RechargeService rechargeService;

    // ========== 储值规则管理 ==========

    @Operation(summary = "创建储值规则")
    @PostMapping("/rule/create")
    public R<Long> createRechargeRule(@Valid @RequestBody RechargeRuleSaveReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long adminId = StpKit.SHOP.getLoginIdAsLong();
        return R.ok(rechargeService.createRechargeRule(req, tenantId, adminId));
    }

    @Operation(summary = "更新储值规则")
    @PutMapping("/rule/update/{id}")
    public R<Void> updateRechargeRule(
            @Parameter(description = "规则ID", required = true) @PathVariable @NotNull Long id,
            @Valid @RequestBody RechargeRuleSaveReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        rechargeService.updateRechargeRule(id, req, tenantId);
        return R.ok();
    }

    @Operation(summary = "更新储值规则状态")
    @PutMapping("/rule/status/{id}")
    public R<Void> updateRechargeRuleStatus(
            @Parameter(description = "规则ID", required = true) @PathVariable @NotNull Long id,
            @Parameter(description = "状态：0-禁用，1-启用", required = true) @RequestParam @NotNull Integer status) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        rechargeService.updateRechargeRuleStatus(id, status, tenantId);
        return R.ok();
    }

    @Operation(summary = "删除储值规则")
    @DeleteMapping("/rule/delete/{id}")
    public R<Void> deleteRechargeRule(
            @Parameter(description = "规则ID", required = true) @PathVariable @NotNull Long id) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        rechargeService.deleteRechargeRule(id, tenantId);
        return R.ok();
    }

    @Operation(summary = "储值规则详情")
    @GetMapping("/rule/detail/{id}")
    public R<RechargeRuleVO> getRechargeRuleDetail(
            @Parameter(description = "规则ID", required = true) @PathVariable @NotNull Long id) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        RechargeRule rule = rechargeService.getRechargeRuleDetail(id, tenantId);
        RechargeRuleVO vo = new RechargeRuleVO();
        vo.setId(rule.getId());
        vo.setName(rule.getName());
        vo.setRechargeAmount(rule.getRechargeAmount());
        vo.setGiveAmount(rule.getGiveAmount());
        vo.setStatus(rule.getStatus());
        vo.setStatusDesc(rule.getStatus() == 1 ? "启用" : "禁用");
        vo.setCreateTime(rule.getCreateTime());
        return R.ok(vo);
    }

    @Operation(summary = "储值规则列表")
    @GetMapping("/rule/list")
    public R<PageResult<RechargeRuleVO>> getRechargeRuleList(@ParameterObject @Valid RechargeRuleQuery query) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Page<RechargeRuleVO> page = rechargeService.getRechargeRuleList(query, tenantId);
        PageResult<RechargeRuleVO> pageResult = new PageResult<>();
        pageResult.setRecords(page.getRecords());
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setSize(page.getSize());
        pageResult.setPages(page.getPages());
        return R.ok(pageResult);
    }

    // ========== 会员账户查看 ==========

    @Operation(summary = "会员储值账户列表")
    @GetMapping("/account/list")
    public R<PageResult<MemberAccountVO>> getMemberAccountList(@ParameterObject @Valid MemberAccountQuery query) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Page<MemberAccountVO> page = rechargeService.getMemberAccountList(query, tenantId);
        PageResult<MemberAccountVO> pageResult = new PageResult<>();
        pageResult.setRecords(page.getRecords());
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setSize(page.getSize());
        pageResult.setPages(page.getPages());
        return R.ok(pageResult);
    }

    @Operation(summary = "会员储值账户详情")
    @GetMapping("/account/detail/{memberId}")
    public R<MemberAccountVO> getMemberAccountDetail(
            @Parameter(description = "会员ID", required = true) @PathVariable @NotNull Long memberId) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        return R.ok(rechargeService.getMemberAccountDetail(memberId, tenantId));
    }

    @Operation(summary = "储值记录列表")
    @GetMapping("/record/list")
    public R<PageResult<RechargeRecordVO>> getRechargeRecordList(@ParameterObject @Valid RechargeRecordQuery query) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Page<RechargeRecordVO> page = rechargeService.getRechargeRecordList(query, tenantId);
        PageResult<RechargeRecordVO> pageResult = new PageResult<>();
        pageResult.setRecords(page.getRecords());
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setSize(page.getSize());
        pageResult.setPages(page.getPages());
        return R.ok(pageResult);
    }
}
