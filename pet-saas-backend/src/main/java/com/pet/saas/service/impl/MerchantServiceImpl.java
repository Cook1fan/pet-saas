package com.pet.saas.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.saas.common.enums.VerifyCodeStatusEnum;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.common.util.BeanConverter;
import com.pet.saas.common.util.VerifyCodeUtil;
import com.pet.saas.dto.resp.CardVerifyResultVO;
import com.pet.saas.dto.resp.MerchantDashboardVO;
import com.pet.saas.dto.resp.OrderInfoVO;
import com.pet.saas.entity.*;
import com.pet.saas.mapper.*;
import com.pet.saas.service.MerchantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantServiceImpl implements MerchantService {

    private final OrderInfoMapper orderInfoMapper;
    private final VerifyCodeRecordMapper verifyCodeRecordMapper;
    private final MemberCardMapper memberCardMapper;
    private final CardRuleMapper cardRuleMapper;
    private final MemberMapper memberMapper;

    @Override
    public MerchantDashboardVO getDashboardOverview() {
        Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDateTime monthStart = firstDayOfMonth.atStartOfDay();

        LambdaQueryWrapper<OrderInfo> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(OrderInfo::getTenantId, tenantId)
                .eq(OrderInfo::getPayStatus, 1)
                .between(OrderInfo::getPayTime, todayStart, todayEnd);
        List<OrderInfo> todayOrders = orderInfoMapper.selectList(todayWrapper);

        Integer todayOrderCount = todayOrders.size();
        BigDecimal todayGmv = todayOrders.stream()
                .map(OrderInfo::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LambdaQueryWrapper<OrderInfo> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.eq(OrderInfo::getTenantId, tenantId)
                .eq(OrderInfo::getPayStatus, 1)
                .between(OrderInfo::getPayTime, monthStart, todayEnd);
        List<OrderInfo> monthOrders = orderInfoMapper.selectList(monthWrapper);

        BigDecimal monthlyGmv = monthOrders.stream()
                .map(OrderInfo::getPayAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return MerchantDashboardVO.builder()
                .todayOrderCount(todayOrderCount)
                .todayGmv(todayGmv)
                .monthlyGmv(monthlyGmv)
                .build();
    }

    @Override
    public List<OrderInfoVO> getTodayOrderList() {
        Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getTenantId, tenantId)
                .between(OrderInfo::getCreateTime, todayStart, todayEnd)
                .orderByDesc(OrderInfo::getCreateTime);
        List<OrderInfo> orders = orderInfoMapper.selectList(wrapper);

        return BeanConverter.convertList(orders, OrderInfoVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CardVerifyResultVO verifyMemberCard(String verifyCode) {
        Long tenantId = (Long) StpUtil.getSession().get("tenant_id");
        Long adminId = (Long) StpUtil.getSession().get("admin_id");

        LambdaQueryWrapper<VerifyCodeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VerifyCodeRecord::getTenantId, tenantId)
                .eq(VerifyCodeRecord::getVerifyCode, verifyCode);
        VerifyCodeRecord record = verifyCodeRecordMapper.selectOne(wrapper);

        if (record == null) {
            throw new BusinessException(ErrorCode.VERIFY_CODE_INVALID);
        }
        if (record.getStatus().equals(VerifyCodeStatusEnum.USED.getCode())) {
            throw new BusinessException(ErrorCode.VERIFY_CODE_USED);
        }
        if (record.getStatus().equals(VerifyCodeStatusEnum.INVALID.getCode())) {
            throw new BusinessException(ErrorCode.VERIFY_CODE_INVALID);
        }
        if (VerifyCodeUtil.isExpired(record.getExpireTime())) {
            record.setStatus(VerifyCodeStatusEnum.INVALID.getCode());
            verifyCodeRecordMapper.updateById(record);
            throw new BusinessException(ErrorCode.VERIFY_CODE_INVALID);
        }

        MemberCard memberCard = memberCardMapper.selectById(record.getMemberCardId());
        if (memberCard == null) {
            throw new BusinessException(ErrorCode.MEMBER_CARD_NOT_FOUND);
        }
        if (memberCard.getRemainTimes() <= 0) {
            throw new BusinessException(ErrorCode.CARD_INSUFFICIENT_OR_EXPIRED);
        }
        if (LocalDateTime.now().isAfter(memberCard.getExpireTime())) {
            throw new BusinessException(ErrorCode.CARD_INSUFFICIENT_OR_EXPIRED);
        }

        memberCard.setRemainTimes(memberCard.getRemainTimes() - 1);
        memberCardMapper.updateById(memberCard);

        record.setStatus(VerifyCodeStatusEnum.USED.getCode());
        record.setVerifyTime(LocalDateTime.now());
        record.setVerifyUserId(adminId);
        verifyCodeRecordMapper.updateById(record);

        CardRule cardRule = cardRuleMapper.selectById(memberCard.getCardRuleId());
        Member member = memberMapper.selectById(memberCard.getMemberId());

        return CardVerifyResultVO.builder()
                .cardName(cardRule != null ? cardRule.getName() : "次卡")
                .memberName(member != null ? member.getName() : "")
                .remainTimes(memberCard.getRemainTimes())
                .expireTime(memberCard.getExpireTime())
                .build();
    }
}
