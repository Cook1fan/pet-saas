package com.pet.saas.service;

import com.pet.saas.dto.resp.CardVerifyResultVO;
import com.pet.saas.dto.resp.MerchantDashboardVO;
import com.pet.saas.dto.resp.OrderInfoVO;

import java.util.List;

public interface MerchantService {

    MerchantDashboardVO getDashboardOverview();

    List<OrderInfoVO> getTodayOrderList();

    CardVerifyResultVO verifyMemberCard(String verifyCode);
}
