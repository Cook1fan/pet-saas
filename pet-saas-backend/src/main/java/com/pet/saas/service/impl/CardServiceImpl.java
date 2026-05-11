package com.pet.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.common.util.OrderNoUtil;
import com.pet.saas.common.util.VerifyCodeUtil;
import com.pet.saas.dto.req.CardRuleSaveReq;
import com.pet.saas.dto.resp.*;
import com.pet.saas.dto.query.CardRuleQuery;
import com.pet.saas.dto.query.CardRecordQuery;
import com.pet.saas.dto.query.MyRecordQuery;
import com.pet.saas.entity.*;
import com.pet.saas.mapper.*;
import com.pet.saas.service.CardService;
import com.pet.saas.service.FlowRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl extends ServiceImpl<CardRuleMapper, CardRule> implements CardService {

    private final CardRuleMapper cardRuleMapper;
    private final MemberCardMapper memberCardMapper;
    private final CardOrderMapper cardOrderMapper;
    private final CardVerifyRecordMapper cardVerifyRecordMapper;
    private final VerifyCodeRecordMapper verifyCodeRecordMapper;
    private final FlowRecordService flowRecordService;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final MemberMapper memberMapper;

    // ========== 次卡规则管理 ==========

    @Override
    public Long createCardRule(CardRuleSaveReq req, Long tenantId, Long adminId) {
        CardRule rule = new CardRule();
        rule.setTenantId(tenantId);
        rule.setName(req.getName());
        rule.setTimes(req.getTimes());
        rule.setPrice(req.getPrice());
        rule.setValidDays(req.getValidDays());
        rule.setStatus(1);
        rule.setCreateUser(adminId);
        cardRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    public void updateCardRule(Long id, CardRuleSaveReq req, Long tenantId) {
        CardRule rule = getCardRuleDetail(id, tenantId);
        rule.setName(req.getName());
        rule.setTimes(req.getTimes());
        rule.setPrice(req.getPrice());
        rule.setValidDays(req.getValidDays());
        cardRuleMapper.updateById(rule);
    }

    @Override
    public void updateCardRuleStatus(Long id, Integer status, Long tenantId) {
        CardRule rule = getCardRuleDetail(id, tenantId);
        rule.setStatus(status);
        cardRuleMapper.updateById(rule);
    }

    @Override
    public void deleteCardRule(Long id, Long tenantId) {
        CardRule rule = getCardRuleDetail(id, tenantId);
        cardRuleMapper.deleteById(rule.getId());
    }

    @Override
    public CardRule getCardRuleDetail(Long id, Long tenantId) {
        CardRule rule = cardRuleMapper.selectById(id);
        if (rule == null || !rule.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "次卡规则不存在");
        }
        return rule;
    }

    @Override
    public Page<CardRuleVO> getCardRuleList(CardRuleQuery query, Long tenantId) {
        LambdaQueryWrapper<CardRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CardRule::getTenantId, tenantId);
        if (query.getStatus() != null) {
            wrapper.eq(CardRule::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(CardRule::getName, query.getKeyword());
        }
        wrapper.orderByDesc(CardRule::getCreateTime);

        Page<CardRule> page = cardRuleMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return convertToCardRuleVOPage(page);
    }

    private Page<CardRuleVO> convertToCardRuleVOPage(Page<CardRule> page) {
        Page<CardRuleVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::convertToCardRuleVO).collect(Collectors.toList()));
        return result;
    }

    private CardRuleVO convertToCardRuleVO(CardRule rule) {
        CardRuleVO vo = new CardRuleVO();
        vo.setId(rule.getId());
        vo.setName(rule.getName());
        vo.setTimes(rule.getTimes());
        vo.setPrice(rule.getPrice());
        vo.setValidDays(rule.getValidDays());
        vo.setStatus(rule.getStatus());
        vo.setStatusDesc(rule.getStatus() == 1 ? "启用" : "禁用");
        vo.setCreateTime(rule.getCreateTime());
        return vo;
    }

    // ========== 商家端 - 会员次卡查看 ==========

    @Override
    public List<MemberCardVO> getMemberCardList(Long memberId, Long tenantId) {
        LambdaQueryWrapper<MemberCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCard::getTenantId, tenantId)
                .eq(MemberCard::getMemberId, memberId);

        return memberCardMapper.selectList(wrapper).stream()
                .map(this::convertToMemberCardVO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CardRecordVO> getCardRecordList(CardRecordQuery query, Long tenantId) {
        LambdaQueryWrapper<CardVerifyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CardVerifyRecord::getTenantId, tenantId);
        if (query.getMemberId() != null) {
            wrapper.eq(CardVerifyRecord::getMemberId, query.getMemberId());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(CardVerifyRecord::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(CardVerifyRecord::getCreateTime, query.getEndTime());
        }
        wrapper.orderByDesc(CardVerifyRecord::getCreateTime);

        Page<CardVerifyRecord> page = cardVerifyRecordMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return convertToCardRecordVOPage(page);
    }

    private Page<CardRecordVO> convertToCardRecordVOPage(Page<CardVerifyRecord> page) {
        Page<CardRecordVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::convertToCardRecordVO).collect(Collectors.toList()));
        return result;
    }

    private CardRecordVO convertToCardRecordVO(CardVerifyRecord record) {
        CardRecordVO vo = new CardRecordVO();
        vo.setId(record.getId());
        vo.setMemberId(record.getMemberId());
        vo.setCardName(record.getCardName());
        vo.setType(2); // 2-核销
        vo.setTypeDesc("核销");
        vo.setVerifyTimes(record.getVerifyTimes());
        vo.setRemainTimesBefore(record.getRemainTimesBefore());
        vo.setRemainTimesAfter(record.getRemainTimesAfter());
        vo.setOrderId(record.getOrderId());
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime());

        Member member = memberMapper.selectById(record.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getName());
            vo.setMemberPhone(member.getPhone());
        }

        return vo;
    }

    // ========== 商家端 - 核销 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CardVerifyResultVO verifyByCode(String verifyCode, Long tenantId, Long operatorId) {
        // 1. 查询核销码
        VerifyCodeRecord codeRecord = verifyCodeRecordMapper.selectOne(
                new LambdaQueryWrapper<VerifyCodeRecord>()
                        .eq(VerifyCodeRecord::getTenantId, tenantId)
                        .eq(VerifyCodeRecord::getVerifyCode, verifyCode)
        );
        if (codeRecord == null) {
            throw new BusinessException(ErrorCode.VERIFY_CODE_INVALID);
        }
        if (codeRecord.getStatus() != 0) {
            throw new BusinessException(ErrorCode.VERIFY_CODE_USED);
        }
        if (LocalDateTime.now().isAfter(codeRecord.getExpireTime())) {
            codeRecord.setStatus(2);
            verifyCodeRecordMapper.updateById(codeRecord);
            throw new BusinessException(ErrorCode.VERIFY_CODE_INVALID);
        }

        // 2. 校验会员次卡
        MemberCard memberCard = memberCardMapper.selectById(codeRecord.getMemberCardId());
        if (memberCard == null) {
            throw new BusinessException(ErrorCode.MEMBER_CARD_NOT_FOUND);
        }
        if (memberCard.getRemainTimes() <= 0) {
            throw new BusinessException(ErrorCode.CARD_INSUFFICIENT_OR_EXPIRED);
        }
        if (LocalDateTime.now().isAfter(memberCard.getExpireTime())) {
            throw new BusinessException(ErrorCode.CARD_INSUFFICIENT_OR_EXPIRED);
        }

        // 3. 查询次卡规则
        CardRule cardRule = cardRuleMapper.selectById(memberCard.getCardRuleId());

        // 4. 扣除次数
        int beforeTimes = memberCard.getRemainTimes();
        int afterTimes = beforeTimes - 1;
        memberCard.setRemainTimes(afterTimes);
        memberCardMapper.updateById(memberCard);

        // 5. 更新核销码状态
        codeRecord.setStatus(1);
        codeRecord.setVerifyTime(LocalDateTime.now());
        codeRecord.setVerifyUserId(operatorId);
        verifyCodeRecordMapper.updateById(codeRecord);

        // 6. 记录核销记录
        CardVerifyRecord verifyRecord = new CardVerifyRecord();
        verifyRecord.setTenantId(tenantId);
        verifyRecord.setMemberId(memberCard.getMemberId());
        verifyRecord.setMemberCardId(memberCard.getId());
        verifyRecord.setCardRuleId(memberCard.getCardRuleId());
        verifyRecord.setVerifyCodeRecordId(codeRecord.getId());
        verifyRecord.setCardName(cardRule != null ? cardRule.getName() : "次卡");
        verifyRecord.setVerifyTimes(1);
        verifyRecord.setRemainTimesBefore(beforeTimes);
        verifyRecord.setRemainTimesAfter(afterTimes);
        verifyRecord.setCreateUser(operatorId);
        cardVerifyRecordMapper.insert(verifyRecord);

        // 7. 查询会员信息
        Member member = memberMapper.selectById(memberCard.getMemberId());

        // 8. 返回结果
        return CardVerifyResultVO.builder()
                .cardName(cardRule != null ? cardRule.getName() : "次卡")
                .memberName(member != null ? member.getName() : "")
                .remainTimes(afterTimes)
                .expireTime(memberCard.getExpireTime())
                .build();
    }

    // ========== C端 - 购买次卡 ==========

    @Override
    public List<CardRuleVO> getAvailableCardRules(Long tenantId) {
        LambdaQueryWrapper<CardRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CardRule::getTenantId, tenantId)
                .eq(CardRule::getStatus, 1)
                .orderByDesc(CardRule::getCreateTime);

        return cardRuleMapper.selectList(wrapper).stream()
                .map(this::convertToCardRuleVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateCardOrderResp purchaseCard(Long cardRuleId, Long tenantId, Long memberId) {
        // 1. 校验规则
        CardRule rule = getCardRuleDetail(cardRuleId, tenantId);
        if (rule.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "该次卡规则已禁用");
        }

        // 2. 创建次卡订单
        String orderNo = OrderNoUtil.generateCardOrderNo();
        CardOrder cardOrder = new CardOrder();
        cardOrder.setTenantId(tenantId);
        cardOrder.setMemberId(memberId);
        cardOrder.setCardRuleId(cardRuleId);
        cardOrder.setOrderNo(orderNo);
        cardOrder.setCardName(rule.getName());
        cardOrder.setTimes(rule.getTimes());
        cardOrder.setPrice(rule.getPrice());
        cardOrder.setValidDays(rule.getValidDays());
        cardOrder.setPayAmount(rule.getPrice());
        cardOrder.setPayType(1);
        cardOrder.setPayStatus(0);
        cardOrderMapper.insert(cardOrder);

        // 3. 创建订单记录（用于支付和对账）
        OrderInfo order = new OrderInfo();
        order.setTenantId(tenantId);
        order.setOrderNo(orderNo);
        order.setMemberId(memberId);
        order.setTotalAmount(rule.getPrice());
        order.setPayAmount(rule.getPrice());
        order.setPayType(1);
        order.setPayStatus(0);
        orderInfoMapper.insert(order);

        // 4. 创建会员次卡（待激活状态）
        MemberCard memberCard = new MemberCard();
        memberCard.setTenantId(tenantId);
        memberCard.setMemberId(memberId);
        memberCard.setCardRuleId(cardRuleId);
        memberCard.setRemainTimes(0); // 支付成功后再设置次数
        memberCard.setExpireTime(LocalDateTime.now()); // 临时设置，支付成功后更新
        memberCardMapper.insert(memberCard);

        // 5. 更新次卡订单的会员次卡ID
        cardOrder.setMemberCardId(memberCard.getId());
        cardOrderMapper.updateById(cardOrder);

        // 6. 返回结果
        CreateCardOrderResp resp = new CreateCardOrderResp();
        resp.setOrderId(order.getId());
        resp.setOrderNo(orderNo);
        resp.setPayAmount(rule.getPrice());
        return resp;
    }

    @Override
    public List<MemberCardVO> getMyCards(Long tenantId, Long memberId) {
        LambdaQueryWrapper<MemberCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberCard::getTenantId, tenantId)
                .eq(MemberCard::getMemberId, memberId);

        return memberCardMapper.selectList(wrapper).stream()
                .map(this::convertToMemberCardVO)
                .collect(Collectors.toList());
    }

    private MemberCardVO convertToMemberCardVO(MemberCard memberCard) {
        MemberCardVO vo = new MemberCardVO();
        vo.setId(memberCard.getId());
        vo.setCardName(getCardRuleDetail(memberCard.getCardRuleId(), memberCard.getTenantId()).getName());
        vo.setRemainTimes(memberCard.getRemainTimes());
        vo.setTotalTimes(getCardRuleDetail(memberCard.getCardRuleId(), memberCard.getTenantId()).getTimes());
        vo.setExpireTime(memberCard.getExpireTime());

        long daysRemain = ChronoUnit.DAYS.between(LocalDateTime.now(), memberCard.getExpireTime());
        vo.setDaysRemain((int) daysRemain);

        if (LocalDateTime.now().isAfter(memberCard.getExpireTime()) || memberCard.getRemainTimes() <= 0) {
            vo.setStatus(0);
            vo.setStatusDesc("已失效");
        } else {
            vo.setStatus(1);
            vo.setStatusDesc("有效");
        }

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerifyCodeVO generateVerifyCode(Long memberCardId, Long tenantId, Long memberId) {
        // 1. 校验会员次卡
        MemberCard memberCard = memberCardMapper.selectById(memberCardId);
        if (memberCard == null || !memberCard.getTenantId().equals(tenantId) || !memberCard.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_CARD_NOT_FOUND);
        }
        if (memberCard.getRemainTimes() <= 0) {
            throw new BusinessException(ErrorCode.CARD_INSUFFICIENT_OR_EXPIRED);
        }
        if (LocalDateTime.now().isAfter(memberCard.getExpireTime())) {
            throw new BusinessException(ErrorCode.CARD_INSUFFICIENT_OR_EXPIRED);
        }

        // 2. 检查是否存在未使用的核销码
        VerifyCodeRecord existing = verifyCodeRecordMapper.selectOne(
                new LambdaQueryWrapper<VerifyCodeRecord>()
                        .eq(VerifyCodeRecord::getTenantId, tenantId)
                        .eq(VerifyCodeRecord::getMemberCardId, memberCardId)
                        .eq(VerifyCodeRecord::getStatus, 0)
                        .gt(VerifyCodeRecord::getExpireTime, LocalDateTime.now())
        );
        if (existing != null) {
            // 返回现有核销码
            long remainSeconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), existing.getExpireTime());
            return VerifyCodeVO.builder()
                    .verifyCode(existing.getVerifyCode())
                    .expireTime(existing.getExpireTime())
                    .cardName(getCardRuleDetail(memberCard.getCardRuleId(), tenantId).getName())
                    .remainTimes(memberCard.getRemainTimes())
                    .remainSeconds(remainSeconds)
                    .build();
        }

        // 3. 生成新核销码
        String verifyCode = VerifyCodeUtil.generateCode();
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(VerifyCodeUtil.EXPIRATION_MINUTES);

        VerifyCodeRecord record = new VerifyCodeRecord();
        record.setTenantId(tenantId);
        record.setVerifyCode(verifyCode);
        record.setMemberCardId(memberCardId);
        record.setMemberId(memberId);
        record.setExpireTime(expireTime);
        record.setStatus(0);
        verifyCodeRecordMapper.insert(record);

        return VerifyCodeVO.builder()
                .verifyCode(verifyCode)
                .expireTime(expireTime)
                .cardName(getCardRuleDetail(memberCard.getCardRuleId(), tenantId).getName())
                .remainTimes(memberCard.getRemainTimes())
                .remainSeconds((long) VerifyCodeUtil.EXPIRATION_MINUTES * 60)
                .build();
    }

    @Override
    public Page<CardRecordVO> getMyCardRecords(MyRecordQuery query, Long tenantId, Long memberId) {
        LambdaQueryWrapper<CardVerifyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CardVerifyRecord::getTenantId, tenantId)
                .eq(CardVerifyRecord::getMemberId, memberId);
        if (query.getType() != null) {
            // 类型：1-购买，2-核销
            if (query.getType() == 2) {
                // 查询核销记录
            } else {
                // 查询购买记录，需要关联 card_order 表，这里简化处理
                return new Page<>();
            }
        }
        wrapper.orderByDesc(CardVerifyRecord::getCreateTime);

        Page<CardVerifyRecord> page = cardVerifyRecordMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return convertToCardRecordVOPage(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleCardPaySuccess(Long orderId) {
        // 1. 查询订单
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在，orderId={}", orderId);
            return;
        }

        // 查询次卡订单
        CardOrder cardOrder = cardOrderMapper.selectOne(
                new LambdaQueryWrapper<CardOrder>()
                        .eq(CardOrder::getOrderNo, order.getOrderNo()));

        if (cardOrder == null || cardOrder.getPayStatus() != 0) {
            log.warn("次卡订单不存在或已支付，orderId={}", orderId);
            return;
        }

        // 2. 更新次卡订单状态
        cardOrder.setPayStatus(1);
        cardOrder.setPayTime(LocalDateTime.now());
        cardOrderMapper.updateById(cardOrder);

        // 3. 激活会员次卡
        MemberCard memberCard = memberCardMapper.selectById(cardOrder.getMemberCardId());
        if (memberCard != null) {
            CardRule rule = getCardRuleDetail(cardOrder.getCardRuleId(), cardOrder.getTenantId());
            memberCard.setRemainTimes(cardOrder.getTimes());
            memberCard.setExpireTime(LocalDateTime.now().plusDays(cardOrder.getValidDays()));
            memberCardMapper.updateById(memberCard);
        }

        // 4. 更新订单状态并创建流水
        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);
        flowRecordService.createFlowRecord(order, 2, null); // 2-消费
    }
}
