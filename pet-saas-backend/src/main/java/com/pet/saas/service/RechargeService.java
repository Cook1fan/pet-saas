package com.pet.saas.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.dto.req.RechargeRuleSaveReq;
import com.pet.saas.dto.resp.*;
import com.pet.saas.dto.query.RechargeRuleQuery;
import com.pet.saas.dto.query.MemberAccountQuery;
import com.pet.saas.dto.query.RechargeRecordQuery;
import com.pet.saas.dto.query.MyRecordQuery;
import com.pet.saas.entity.RechargeRule;
import com.pet.saas.entity.MemberAccount;

import java.math.BigDecimal;
import java.util.List;

public interface RechargeService {

    // ========== 商家端 - 储值规则管理 ==========

    /**
     * 创建储值规则
     */
    Long createRechargeRule(RechargeRuleSaveReq req, Long tenantId, Long adminId);

    /**
     * 更新储值规则
     */
    void updateRechargeRule(Long id, RechargeRuleSaveReq req, Long tenantId);

    /**
     * 更新储值规则状态
     */
    void updateRechargeRuleStatus(Long id, Integer status, Long tenantId);

    /**
     * 删除储值规则
     */
    void deleteRechargeRule(Long id, Long tenantId);

    /**
     * 获取储值规则详情
     */
    RechargeRule getRechargeRuleDetail(Long id, Long tenantId);

    /**
     * 获取储值规则列表
     */
    Page<RechargeRuleVO> getRechargeRuleList(RechargeRuleQuery query, Long tenantId);

    // ========== 商家端 - 会员账户查看 ==========

    /**
     * 获取会员储值账户列表
     */
    Page<MemberAccountVO> getMemberAccountList(MemberAccountQuery query, Long tenantId);

    /**
     * 获取会员储值账户详情
     */
    MemberAccountVO getMemberAccountDetail(Long memberId, Long tenantId);

    /**
     * 获取储值记录列表
     */
    Page<RechargeRecordVO> getRechargeRecordList(RechargeRecordQuery query, Long tenantId);

    // ========== C端 - 购买储值 ==========

    /**
     * 获取可用的储值规则列表
     */
    List<RechargeRuleVO> getAvailableRechargeRules(Long tenantId);

    /**
     * 购买储值
     */
    CreateRechargeOrderResp purchaseRecharge(Long rechargeRuleId, Long tenantId, Long memberId);

    /**
     * 获取我的储值账户
     */
    UserRechargeAccountVO getMyAccountInfo(Long tenantId, Long memberId);

    /**
     * 获取我的储值记录
     */
    Page<RechargeRecordVO> getMyRechargeRecords(MyRecordQuery query, Long tenantId, Long memberId);

    // ========== 支付回调处理 ==========

    /**
     * 处理储值支付成功
     */
    void handleRechargePaySuccess(Long orderId);

    // ========== 储值消费 ==========

    /**
     * 消费储值余额
     */
    void consumeBalance(Long memberId, BigDecimal amount, Long orderId, String remark, Long tenantId, Long operatorId);
}
