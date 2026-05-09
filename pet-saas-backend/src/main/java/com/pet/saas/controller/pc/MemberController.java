package com.pet.saas.controller.pc;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.common.R;
import com.pet.saas.common.constant.RedisKeyConstants;
import com.pet.saas.common.util.BeanConverter;
import com.pet.saas.common.util.StpKit;
import com.pet.saas.dto.query.MemberQuery;
import com.pet.saas.dto.req.PcMemberCreateReq;
import com.pet.saas.dto.req.PcMemberUpdateReq;
import com.pet.saas.dto.resp.MemberAccountVO;
import com.pet.saas.dto.resp.MemberVO;
import com.pet.saas.dto.resp.PageResult;
import com.pet.saas.dto.resp.PetInfoVO;
import com.pet.saas.dto.resp.UserMemberCardVO;
import com.pet.saas.entity.CardRule;
import com.pet.saas.entity.Member;
import com.pet.saas.entity.MemberAccount;
import com.pet.saas.entity.MemberCard;
import com.pet.saas.entity.PetInfo;
import com.pet.saas.mapper.CardRuleMapper;
import com.pet.saas.mapper.MemberCardMapper;
import com.pet.saas.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "门店PC端-会员管理")
@RestController
@RequestMapping("/api/pc/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberCardMapper memberCardMapper;
    private final CardRuleMapper cardRuleMapper;

    @Operation(summary = "会员列表")
    @GetMapping("/list")
    public R<PageResult<MemberVO>> listMembers(@ParameterObject @Valid MemberQuery query) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Page<Member> page = memberService.listMembers(query, tenantId);

        List<MemberVO> memberVOList = page.getRecords().stream().map(member -> {
            MemberVO memberVO = BeanConverter.convert(member, MemberVO.class);
            List<PetInfo> pets = memberService.listPets(tenantId, member.getId());
            memberVO.setPets(BeanConverter.convertList(pets, PetInfoVO.class));
            return memberVO;
        }).collect(Collectors.toList());

        PageResult<MemberVO> pageResult = new PageResult<>();
        pageResult.setRecords(memberVOList);
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrent(page.getCurrent());
        pageResult.setSize(page.getSize());
        pageResult.setPages(page.getPages());

        return R.ok(pageResult);
    }

    @Operation(summary = "会员录入")
    @PostMapping("/create")
    public R<MemberVO> createMember(@Valid @RequestBody PcMemberCreateReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Member member = memberService.createMember(req, tenantId);

        MemberVO memberVO = BeanConverter.convert(member, MemberVO.class);
        List<PetInfo> pets = memberService.listPets(tenantId, member.getId());
        memberVO.setPets(BeanConverter.convertList(pets, PetInfoVO.class));

        return R.ok(memberVO);
    }

    @Operation(summary = "会员编辑")
    @PutMapping("/update")
    public R<MemberVO> updateMember(@Valid @RequestBody PcMemberUpdateReq req) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        Member member = memberService.updateMember(req, tenantId);

        MemberVO memberVO = BeanConverter.convert(member, MemberVO.class);
        List<PetInfo> pets = memberService.listPets(tenantId, member.getId());
        memberVO.setPets(BeanConverter.convertList(pets, PetInfoVO.class));

        return R.ok(memberVO);
    }

    @Operation(summary = "会员储值账户")
    @GetMapping("/account/{memberId}")
    public R<MemberAccountVO> getMemberAccount(
            @Parameter(description = "会员ID", required = true) @PathVariable @NotNull Long memberId) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        MemberAccount account = memberService.getMemberAccount(tenantId, memberId);
        return R.ok(BeanConverter.convert(account, MemberAccountVO.class));
    }

    @Operation(summary = "会员次卡列表")
    @GetMapping("/{memberId}/cards")
    public R<List<UserMemberCardVO>> getMemberCards(
            @Parameter(description = "会员ID", required = true) @PathVariable @NotNull Long memberId) {
        Long tenantId = (Long) StpKit.SHOP.getSession().get(RedisKeyConstants.TENANT_ID_KEY);
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<MemberCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCard::getTenantId, tenantId)
                .eq(MemberCard::getMemberId, memberId)
                .orderByDesc(MemberCard::getCreateTime);
        List<MemberCard> cards = memberCardMapper.selectList(wrapper);

        List<Long> cardRuleIds = cards.stream()
                .map(MemberCard::getCardRuleId)
                .collect(Collectors.toList());
        Map<Long, CardRule> cardRuleMap = cardRuleIds.isEmpty() ? Map.of() :
                cardRuleMapper.selectBatchIds(cardRuleIds).stream()
                        .collect(Collectors.toMap(CardRule::getId, rule -> rule));

        List<UserMemberCardVO> result = cards.stream().map(card -> {
            CardRule rule = cardRuleMap.get(card.getCardRuleId());
            int status;
            if (card.getRemainTimes() <= 0) {
                status = 0;
            } else if (now.isAfter(card.getExpireTime())) {
                status = 2;
            } else {
                status = 1;
            }
            return UserMemberCardVO.builder()
                    .id(card.getId())
                    .cardRuleId(card.getCardRuleId())
                    .cardName(rule != null ? rule.getName() : "次卡")
                    .remainTimes(card.getRemainTimes())
                    .expireTime(card.getExpireTime())
                    .status(status)
                    .build();
        }).collect(Collectors.toList());

        return R.ok(result);
    }
}
