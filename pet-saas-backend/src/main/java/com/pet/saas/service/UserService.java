package com.pet.saas.service;

import com.pet.saas.dto.req.ActivityOrderCreateReq;
import com.pet.saas.dto.req.AiChatReq;
import com.pet.saas.dto.req.GenerateVerifyCodeReq;
import com.pet.saas.dto.req.ShopSwitchReq;
import com.pet.saas.dto.resp.*;

import java.util.List;
import java.util.Map;

public interface UserService {

    List<UserShopVO> getBoundShopList();

    void switchShop(ShopSwitchReq req);

    List<UserShopVO> getAvailableShopList();

    void bindShop(ShopSwitchReq req);

    List<ActivityInfoVO> getActivityList(Integer type);

    ActivityInfoVO getActivityDetail(Long activityId);

    Map<String, Object> createActivityOrder(ActivityOrderCreateReq req);

    UserAccountBalanceVO getAccountBalance();

    List<UserMemberCardVO> getMemberCardList();

    List<OrderInfoVO> getOrderList();

    List<PetInfoVO> getPetList();

    VerifyCodeVO generateVerifyCode(GenerateVerifyCodeReq req);

    AiChatRespVO aiChat(AiChatReq req);

    List<AiChatHistoryVO> getAiChatHistory();
}
