package com.pet.saas.service;

import com.pet.saas.dto.req.MerchantLoginReq;
import com.pet.saas.dto.req.PcLoginReq;
import com.pet.saas.dto.req.PlatformLoginReq;
import com.pet.saas.dto.req.WxLoginReq;
import com.pet.saas.dto.resp.LoginRespVO;
import com.pet.saas.dto.resp.WxLoginRespVO;

public interface AuthService {

    LoginRespVO pcLogin(PcLoginReq req);

    LoginRespVO platformLogin(PlatformLoginReq req);

    LoginRespVO merchantLogin(MerchantLoginReq req);

    WxLoginRespVO wxLogin(WxLoginReq req);

    void pcLogout();

    void platformLogout();

    void merchantLogout();

    void userLogout();
}
