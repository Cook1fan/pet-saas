package com.pet.saas.controller.pc;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.common.R;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.req.CardRuleSaveReq;
import com.pet.saas.dto.resp.*;
import com.pet.saas.dto.query.CardRuleQuery;
import com.pet.saas.dto.query.CardRecordQuery;
import com.pet.saas.entity.CardRule;
import com.pet.saas.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "门店PC端-次卡管理")
@RestController
@RequestMapping("/api/pc/card")
@RequiredArgsConstructor
public class PcCardController {

    private final CardService cardService;

    // ========== 次卡规则管理 ==========

    @Operation(summary = "创建次卡规则")
    @PostMapping("/rule/create")
    public R<Long> createCardRule(@Valid @RequestBody CardRuleSaveReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Long adminId = StpKit.SHOP.getLoginIdAsLong();
        return R.ok(cardService.createCardRule(req, tenantId, adminId));
    }

    @Operation(summary = "更新次卡规则")
    @PutMapping("/rule/update/{id}")
    public R<Void> updateCardRule(
            @Parameter(description = "规则ID", required = true) @PathVariable @NotNull Long id,
            @Valid @RequestBody CardRuleSaveReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        cardService.updateCardRule(id, req, tenantId);
        return R.ok();
    }

    @Operation(summary = "更新次卡规则状态")
    @PutMapping("/rule/status/{id}")
    public R<Void> updateCardRuleStatus(
            @Parameter(description = "规则ID", required = true) @PathVariable @NotNull Long id,
            @Parameter(description = "状态：0-禁用，1-启用", required = true) @RequestParam @NotNull Integer status) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        cardService.updateCardRuleStatus(id, status, tenantId);
        return R.ok();
    }

    @Operation(summary = "删除次卡规则")
    @DeleteMapping("/rule/delete/{id}")
    public R<Void> deleteCardRule(
            @Parameter(description = "规则ID", required = true) @PathVariable @NotNull Long id) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        cardService.deleteCardRule(id, tenantId);
        return R.ok();
    }

    @Operation(summary = "次卡规则详情")
    @GetMapping("/rule/detail/{id}")
    public R<CardRuleVO> getCardRuleDetail(
            @Parameter(description = "规则ID", required = true) @PathVariable @NotNull Long id) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        CardRule rule = cardService.getCardRuleDetail(id, tenantId);
        CardRuleVO vo = new CardRuleVO();
        vo.setId(rule.getId());
        vo.setName(rule.getName());
        vo.setTimes(rule.getTimes());
        vo.setPrice(rule.getPrice());
        vo.setValidDays(rule.getValidDays());
        vo.setStatus(rule.getStatus());
        vo.setStatusDesc(rule.getStatus() == 1 ? "启用" : "禁用");
        vo.setCreateTime(rule.getCreateTime());
        return R.ok(vo);
    }

    @Operation(summary = "次卡规则列表")
    @GetMapping("/rule/list")
    public R<PageResult<CardRuleVO>> getCardRuleList(@ParameterObject @Valid CardRuleQuery query) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Page<CardRuleVO> page = cardService.getCardRuleList(query, tenantId);
        PageResult<CardRuleVO> pageResult = new PageResult<>();
        pageResult.setRecords(page.getRecords());
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setSize(page.getSize());
        pageResult.setPages(page.getPages());
        return R.ok(pageResult);
    }

    // ========== 会员次卡查看 ==========

    @Operation(summary = "会员次卡列表")
    @GetMapping("/member/{memberId}/cards")
    public R<List<MemberCardVO>> getMemberCardList(
            @Parameter(description = "会员ID", required = true) @PathVariable @NotNull Long memberId) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        return R.ok(cardService.getMemberCardList(memberId, tenantId));
    }

    @Operation(summary = "次卡核销记录列表")
    @GetMapping("/record/list")
    public R<PageResult<CardRecordVO>> getCardRecordList(@ParameterObject @Valid CardRecordQuery query) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Page<CardRecordVO> page = cardService.getCardRecordList(query, tenantId);
        PageResult<CardRecordVO> pageResult = new PageResult<>();
        pageResult.setRecords(page.getRecords());
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setSize(page.getSize());
        pageResult.setPages(page.getPages());
        return R.ok(pageResult);
    }
}
