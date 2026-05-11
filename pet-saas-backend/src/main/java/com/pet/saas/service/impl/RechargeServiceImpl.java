package com.pet.saas.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pet.saas.common.exception.BusinessException;
import com.pet.saas.common.exception.ErrorCode;
import com.pet.saas.common.util.OrderNoUtil;
import com.pet.saas.dto.req.RechargeRuleSaveReq;
import com.pet.saas.dto.resp.*;
import com.pet.saas.dto.query.RechargeRuleQuery;
import com.pet.saas.dto.query.MemberAccountQuery;
import com.pet.saas.dto.query.RechargeRecordQuery;
import com.pet.saas.dto.query.MyRecordQuery;
import com.pet.saas.entity.*;
import com.pet.saas.mapper.*;
import com.pet.saas.service.RechargeService;
import com.pet.saas.service.FlowRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RechargeServiceImpl extends ServiceImpl<RechargeRuleMapper, RechargeRule> implements RechargeService {

    private final RechargeRuleMapper rechargeRuleMapper;
    private final MemberAccountMapper memberAccountMapper;
    private final RechargeOrderMapper rechargeOrderMapper;
    private final RechargeConsumeRecordMapper rechargeConsumeRecordMapper;
    private final FlowRecordService flowRecordService;
    private final OrderInfoMapper orderInfoMapper;
    private final OrderItemMapper orderItemMapper;
    private final MemberMapper memberMapper;

    // ========== 储值规则管理 ==========

    @Override
    public Long createRechargeRule(RechargeRuleSaveReq req, Long tenantId, Long adminId) {
        RechargeRule rule = new RechargeRule();
        rule.setTenantId(tenantId);
        rule.setName(req.getName());
        rule.setRechargeAmount(req.getRechargeAmount());
        rule.setGiveAmount(req.getGiveAmount());
        rule.setStatus(1);
        rule.setCreateUser(adminId);
        rechargeRuleMapper.insert(rule);
        return rule.getId();
    }

    @Override
    public void updateRechargeRule(Long id, RechargeRuleSaveReq req, Long tenantId) {
        RechargeRule rule = getRechargeRuleDetail(id, tenantId);
        rule.setName(req.getName());
        rule.setRechargeAmount(req.getRechargeAmount());
        rule.setGiveAmount(req.getGiveAmount());
        rechargeRuleMapper.updateById(rule);
    }

    @Override
    public void updateRechargeRuleStatus(Long id, Integer status, Long tenantId) {
        RechargeRule rule = getRechargeRuleDetail(id, tenantId);
        rule.setStatus(status);
        rechargeRuleMapper.updateById(rule);
    }

    @Override
    public void deleteRechargeRule(Long id, Long tenantId) {
        RechargeRule rule = getRechargeRuleDetail(id, tenantId);
        rechargeRuleMapper.deleteById(rule.getId());
    }

    @Override
    public RechargeRule getRechargeRuleDetail(Long id, Long tenantId) {
        RechargeRule rule = rechargeRuleMapper.selectById(id);
        if (rule == null || !rule.getTenantId().equals(tenantId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "储值规则不存在");
        }
        return rule;
    }

    @Override
    public Page<RechargeRuleVO> getRechargeRuleList(RechargeRuleQuery query, Long tenantId) {
        LambdaQueryWrapper<RechargeRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeRule::getTenantId, tenantId);
        if (query.getStatus() != null) {
            wrapper.eq(RechargeRule::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(RechargeRule::getName, query.getKeyword());
        }
        wrapper.orderByDesc(RechargeRule::getCreateTime);

        Page<RechargeRule> page = rechargeRuleMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return convertToRechargeRuleVOPage(page);
    }

    private Page<RechargeRuleVO> convertToRechargeRuleVOPage(Page<RechargeRule> page) {
        Page<RechargeRuleVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::convertToRechargeRuleVO).collect(Collectors.toList()));
        return result;
    }

    private RechargeRuleVO convertToRechargeRuleVO(RechargeRule rule) {
        RechargeRuleVO vo = new RechargeRuleVO();
        vo.setId(rule.getId());
        vo.setName(rule.getName());
        vo.setRechargeAmount(rule.getRechargeAmount());
        vo.setGiveAmount(rule.getGiveAmount());
        vo.setStatus(rule.getStatus());
        vo.setStatusDesc(rule.getStatus() == 1 ? "启用" : "禁用");
        vo.setCreateTime(rule.getCreateTime());
        return vo;
    }

    // ========== 商家端 - 会员账户查看 ==========

    @Override
    public Page<MemberAccountVO> getMemberAccountList(MemberAccountQuery query, Long tenantId) {
        LambdaQueryWrapper<MemberAccount> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberAccount::getTenantId, tenantId);

        Page<MemberAccount> page = memberAccountMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return convertToMemberAccountVOPage(page);
    }

    private Page<MemberAccountVO> convertToMemberAccountVOPage(Page<MemberAccount> page) {
        Page<MemberAccountVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(account -> {
            MemberAccountVO vo = new MemberAccountVO();
            vo.setId(account.getId());
            vo.setMemberId(account.getMemberId());
            vo.setBalance(account.getBalance());
            vo.setTotalRecharge(account.getTotalRecharge());

            Member member = memberMapper.selectById(account.getMemberId());
            if (member != null) {
                vo.setMemberName(member.getName());
                vo.setMemberPhone(member.getPhone());
            }

            return vo;
        }).collect(Collectors.toList()));
        return result;
    }

    @Override
    public MemberAccountVO getMemberAccountDetail(Long memberId, Long tenantId) {
        MemberAccount account = memberAccountMapper.selectOne(
                new LambdaQueryWrapper<MemberAccount>()
                        .eq(MemberAccount::getTenantId, tenantId)
                        .eq(MemberAccount::getMemberId, memberId));
        if (account == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "会员储值账户不存在");
        }
        MemberAccountVO vo = new MemberAccountVO();
        vo.setId(account.getId());
        vo.setMemberId(account.getMemberId());
        vo.setBalance(account.getBalance());
        vo.setTotalRecharge(account.getTotalRecharge());

        Member member = memberMapper.selectById(memberId);
        if (member != null) {
            vo.setMemberName(member.getName());
            vo.setMemberPhone(member.getPhone());
        }

        return vo;
    }

    @Override
    public Page<RechargeRecordVO> getRechargeRecordList(RechargeRecordQuery query, Long tenantId) {
        LambdaQueryWrapper<RechargeConsumeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeConsumeRecord::getTenantId, tenantId);
        if (query.getMemberId() != null) {
            wrapper.eq(RechargeConsumeRecord::getMemberId, query.getMemberId());
        }
        if (query.getType() != null) {
            wrapper.eq(RechargeConsumeRecord::getType, query.getType());
        }
        if (query.getStartTime() != null) {
            wrapper.ge(RechargeConsumeRecord::getCreateTime, query.getStartTime());
        }
        if (query.getEndTime() != null) {
            wrapper.le(RechargeConsumeRecord::getCreateTime, query.getEndTime());
        }
        wrapper.orderByDesc(RechargeConsumeRecord::getCreateTime);

        Page<RechargeConsumeRecord> page = rechargeConsumeRecordMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return convertToRechargeRecordVOPage(page);
    }

    private Page<RechargeRecordVO> convertToRechargeRecordVOPage(Page<RechargeConsumeRecord> page) {
        Page<RechargeRecordVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::convertToRechargeRecordVO).collect(Collectors.toList()));
        return result;
    }

    private RechargeRecordVO convertToRechargeRecordVO(RechargeConsumeRecord record) {
        RechargeRecordVO vo = new RechargeRecordVO();
        vo.setId(record.getId());
        vo.setMemberId(record.getMemberId());
        vo.setType(record.getType());
        vo.setAmount(record.getAmount());
        vo.setBalanceBefore(record.getBalanceBefore());
        vo.setBalanceAfter(record.getBalanceAfter());
        vo.setOrderId(record.getOrderId());
        vo.setRemark(record.getRemark());
        vo.setCreateTime(record.getCreateTime());

        vo.setTypeDesc(switch (record.getType()) {
            case 1 -> "储值";
            case 2 -> "消费";
            case 3 -> "退款";
            default -> "未知";
        });

        Member member = memberMapper.selectById(record.getMemberId());
        if (member != null) {
            vo.setMemberName(member.getName());
            vo.setMemberPhone(member.getPhone());
        }

        return vo;
    }

    // ========== C端 - 购买储值 ==========

    @Override
    public List<RechargeRuleVO> getAvailableRechargeRules(Long tenantId) {
        LambdaQueryWrapper<RechargeRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeRule::getTenantId, tenantId)
                .eq(RechargeRule::getStatus, 1)
                .orderByDesc(RechargeRule::getCreateTime);

        return rechargeRuleMapper.selectList(wrapper).stream()
                .map(this::convertToRechargeRuleVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CreateRechargeOrderResp purchaseRecharge(Long rechargeRuleId, Long tenantId, Long memberId) {
        // 1. 校验规则
        RechargeRule rule = getRechargeRuleDetail(rechargeRuleId, tenantId);
        if (rule.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "该储值规则已禁用");
        }

        // 2. 查询或创建会员储值账户
        MemberAccount account = getOrCreateMemberAccount(memberId, tenantId);

        // 3. 创建储值订单
        String orderNo = OrderNoUtil.generateRechargeOrderNo();
        RechargeOrder rechargeOrder = new RechargeOrder();
        rechargeOrder.setTenantId(tenantId);
        rechargeOrder.setMemberId(memberId);
        rechargeOrder.setRechargeRuleId(rechargeRuleId);
        rechargeOrder.setOrderNo(orderNo);
        rechargeOrder.setRechargeAmount(rule.getRechargeAmount());
        rechargeOrder.setGiveAmount(rule.getGiveAmount());
        rechargeOrder.setPayAmount(rule.getRechargeAmount());
        rechargeOrder.setPayType(1); // 微信支付
        rechargeOrder.setPayStatus(0); // 待支付
        rechargeOrderMapper.insert(rechargeOrder);

        // 4. 创建订单记录（用于支付和对账）
        OrderInfo order = new OrderInfo();
        order.setTenantId(tenantId);
        order.setOrderNo(orderNo);
        order.setMemberId(memberId);
        order.setTotalAmount(rule.getRechargeAmount());
        order.setPayAmount(rule.getRechargeAmount());
        order.setPayType(1);
        order.setPayStatus(0);
        orderInfoMapper.insert(order);

        // 6. 返回结果
        CreateRechargeOrderResp resp = new CreateRechargeOrderResp();
        resp.setOrderId(order.getId());
        resp.setOrderNo(orderNo);
        resp.setPayAmount(rule.getRechargeAmount());
        return resp;
    }

    @Override
    public UserRechargeAccountVO getMyAccountInfo(Long tenantId, Long memberId) {
        MemberAccount account = getOrCreateMemberAccount(memberId, tenantId);

        UserRechargeAccountVO vo = new UserRechargeAccountVO();
        vo.setBalance(account.getBalance());
        vo.setTotalRecharge(account.getTotalRecharge());

        // 计算累计消费
        LambdaQueryWrapper<RechargeConsumeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeConsumeRecord::getTenantId, tenantId)
                .eq(RechargeConsumeRecord::getMemberId, memberId)
                .eq(RechargeConsumeRecord::getType, 2);

        BigDecimal totalConsume = rechargeConsumeRecordMapper.selectList(wrapper).stream()
                .map(RechargeConsumeRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs();

        vo.setTotalConsume(totalConsume);

        return vo;
    }

    @Override
    public Page<RechargeRecordVO> getMyRechargeRecords(MyRecordQuery query, Long tenantId, Long memberId) {
        LambdaQueryWrapper<RechargeConsumeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeConsumeRecord::getTenantId, tenantId)
                .eq(RechargeConsumeRecord::getMemberId, memberId);

        if (query.getType() != null) {
            wrapper.eq(RechargeConsumeRecord::getType, query.getType());
        }

        wrapper.orderByDesc(RechargeConsumeRecord::getCreateTime);

        Page<RechargeConsumeRecord> page = rechargeConsumeRecordMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()), wrapper);

        return convertToRechargeRecordVOPage(page);
    }

    // ========== 支付回调处理 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRechargePaySuccess(Long orderId) {
        // 1. 查询订单
        OrderInfo order = orderInfoMapper.selectById(orderId);
        if (order == null) {
            log.warn("订单不存在，orderId={}", orderId);
            return;
        }

        // 查询储值订单
        RechargeOrder rechargeOrder = rechargeOrderMapper.selectOne(
                new LambdaQueryWrapper<RechargeOrder>()
                        .eq(RechargeOrder::getOrderNo, order.getOrderNo()));

        if (rechargeOrder == null || rechargeOrder.getPayStatus() != 0) {
            log.warn("储值订单不存在或已支付，orderId={}", orderId);
            return;
        }

        // 2. 更新储值订单状态
        rechargeOrder.setPayStatus(1);
        rechargeOrder.setPayTime(LocalDateTime.now());
        rechargeOrderMapper.updateById(rechargeOrder);

        // 3. 查询或创建会员储值账户
        MemberAccount account = getOrCreateMemberAccount(rechargeOrder.getMemberId(), rechargeOrder.getTenantId());
        BigDecimal totalAmount = rechargeOrder.getRechargeAmount().add(rechargeOrder.getGiveAmount());
        BigDecimal beforeBalance = account.getBalance();
        BigDecimal afterBalance = beforeBalance.add(totalAmount);

        // 4. 更新账户余额
        account.setBalance(afterBalance);
        account.setTotalRecharge(account.getTotalRecharge().add(rechargeOrder.getRechargeAmount()));
        memberAccountMapper.updateById(account);

        // 5. 记录储值消费记录
        RechargeConsumeRecord record = new RechargeConsumeRecord();
        record.setTenantId(account.getTenantId());
        record.setMemberId(account.getMemberId());
        record.setMemberAccountId(account.getId());
        record.setType(1); // 1-储值
        record.setAmount(totalAmount);
        record.setBalanceBefore(beforeBalance);
        record.setBalanceAfter(afterBalance);
        record.setRemark("储值" + rechargeOrder.getRechargeAmount() + "，赠送" + rechargeOrder.getGiveAmount());
        rechargeConsumeRecordMapper.insert(record);

        // 6. 更新订单状态并创建流水
        order.setPayStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);
        flowRecordService.createFlowRecord(order, 1, null); // 1-充值
    }

    // ========== 储值消费 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeBalance(Long memberId, BigDecimal amount, Long orderId, String remark, Long tenantId, Long operatorId) {
        // 1. 查询会员储值账户
        MemberAccount account = memberAccountMapper.selectOne(
                new LambdaQueryWrapper<MemberAccount>()
                        .eq(MemberAccount::getTenantId, tenantId)
                        .eq(MemberAccount::getMemberId, memberId));
        if (account == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "会员储值账户不存在");
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.BALANCE_INSUFFICIENT);
        }

        // 2. 扣除余额
        BigDecimal beforeBalance = account.getBalance();
        BigDecimal afterBalance = beforeBalance.subtract(amount);
        account.setBalance(afterBalance);
        memberAccountMapper.updateById(account);

        // 3. 记录消费记录
        RechargeConsumeRecord record = new RechargeConsumeRecord();
        record.setTenantId(tenantId);
        record.setMemberId(memberId);
        record.setMemberAccountId(account.getId());
        record.setOrderId(orderId);
        record.setType(2); // 2-消费
        record.setAmount(amount.negate());
        record.setBalanceBefore(beforeBalance);
        record.setBalanceAfter(afterBalance);
        record.setRemark(remark);
        record.setCreateUser(operatorId);
        rechargeConsumeRecordMapper.insert(record);
    }

    private MemberAccount getOrCreateMemberAccount(Long memberId, Long tenantId) {
        MemberAccount account = memberAccountMapper.selectOne(
                new LambdaQueryWrapper<MemberAccount>()
                        .eq(MemberAccount::getTenantId, tenantId)
                        .eq(MemberAccount::getMemberId, memberId));
        if (account == null) {
            account = new MemberAccount();
            account.setTenantId(tenantId);
            account.setMemberId(memberId);
            account.setBalance(BigDecimal.ZERO);
            account.setTotalRecharge(BigDecimal.ZERO);
            memberAccountMapper.insert(account);
        }
        return account;
    }
}
