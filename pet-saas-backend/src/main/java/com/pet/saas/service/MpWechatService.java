package com.pet.saas.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public interface MpWechatService {

    /**
     * 通过code获取微信用户信息（openid, unionid, nickname, avatar）
     *
     * @param code 微信授权code
     * @return 微信用户信息（包含openid, unionid, nickname, avatar等）
     */
    MpWechatUserInfo getUserInfo(String code);

    /**
     * 发送公众号模板消息
     *
     * @param openid 接收用户openid
     * @param templateId 模板消息ID
     * @param data 模板数据
     */
    void sendTemplateMessage(String openid, String templateId, Object data);

    /**
     * 发送注册成功通知
     *
     * @param openid 接收用户openid
     * @param shopName 门店名称
     * @param username 管理员账号
     * @param password 初始密码
     */
    void sendRegisterSuccessNotification(String openid, String shopName, String username, String password);

    /**
     * 微信公众号用户信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class MpWechatUserInfo {
        private String openid;
        private String unionid;
        private String nickname;
        private String avatar;
    }
}
