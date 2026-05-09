package com.pet.saas.service;

import com.pet.saas.dto.req.MpRegisterSubmitReq;
import com.pet.saas.dto.req.MpWxLoginReq;
import com.pet.saas.dto.resp.*;

public interface MpShopRegisterService {

    /**
     * 处理微信公众号授权回调，生成临时token
     *
     * @param code 微信授权code
     * @param state 状态参数
     * @return 临时token（用于后续接口调用）
     */
    String handleWxCallback(String code, String state);

    /**
     * 微信注册预检查
     *
     * @param mpToken 微信授权临时token
     * @return 检查结果
     */
    MpRegisterCheckRespVO checkRegister(String mpToken);

    /**
     * 提交门店注册信息
     *
     * @param req 请求参数
     * @return 注册结果
     */
    MpRegisterSubmitRespVO submitRegister(MpRegisterSubmitReq req);

    /**
     * 微信公众号商家登录（已注册用户）
     *
     * @param req 请求参数
     * @return 登录结果
     */
    MpShopLoginRespVO login(MpWxLoginReq req);
}
