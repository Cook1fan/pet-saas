package com.pet.saas.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pet.saas.dto.req.CardRuleSaveReq;
import com.pet.saas.dto.resp.*;
import com.pet.saas.dto.query.CardRuleQuery;
import com.pet.saas.dto.query.CardRecordQuery;
import com.pet.saas.dto.query.MyRecordQuery;
import com.pet.saas.entity.CardRule;

import java.util.List;

public interface CardService {

    // ========== 商家端 - 次卡规则管理 ==========

    /**
     * 创建次卡规则
     */
    Long createCardRule(CardRuleSaveReq req, Long tenantId, Long adminId);

    /**
     * 更新次卡规则
     */
    void updateCardRule(Long id, CardRuleSaveReq req, Long tenantId);

    /**
     * 更新次卡规则状态
     */
    void updateCardRuleStatus(Long id, Integer status, Long tenantId);

    /**
     * 删除次卡规则
     */
    void deleteCardRule(Long id, Long tenantId);

    /**
     * 获取次卡规则详情
     */
    CardRule getCardRuleDetail(Long id, Long tenantId);

    /**
     * 获取次卡规则列表
     */
    Page<CardRuleVO> getCardRuleList(CardRuleQuery query, Long tenantId);

    // ========== 商家端 - 会员次卡查看 ==========

    /**
     * 获取会员次卡列表
     */
    List<MemberCardVO> getMemberCardList(Long memberId, Long tenantId);

    /**
     * 获取次卡记录列表
     */
    Page<CardRecordVO> getCardRecordList(CardRecordQuery query, Long tenantId);

    // ========== 商家端 - 核销 ==========

    /**
     * 通过核销码核销次卡
     */
    CardVerifyResultVO verifyByCode(String verifyCode, Long tenantId, Long operatorId);

    // ========== C端 - 购买次卡 ==========

    /**
     * 获取可用的次卡规则列表
     */
    List<CardRuleVO> getAvailableCardRules(Long tenantId);

    /**
     * 购买次卡
     */
    CreateCardOrderResp purchaseCard(Long cardRuleId, Long tenantId, Long memberId);

    /**
     * 获取我的次卡列表
     */
    List<MemberCardVO> getMyCards(Long tenantId, Long memberId);

    /**
     * 生成核销码
     */
    VerifyCodeVO generateVerifyCode(Long memberCardId, Long tenantId, Long memberId);

    /**
     * 获取我的次卡记录
     */
    Page<CardRecordVO> getMyCardRecords(MyRecordQuery query, Long tenantId, Long memberId);

    // ========== 支付回调处理 ==========

    /**
     * 处理次卡支付成功
     */
    void handleCardPaySuccess(Long orderId);
}
