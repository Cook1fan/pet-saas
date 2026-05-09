package com.pet.saas.service.impl;

import cn.hutool.core.util.StrUtil;
import com.pet.saas.config.properties.WxMpProperties;
import com.pet.saas.service.MpWechatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.kefu.WxMpKefuMessage;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateMessage;
import org.springframework.stereotype.Service;

/**
 * 微信公众号业务服务实现
 * <p>
 * 使用 WxJava SDK 实现微信公众号业务功能
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MpWechatServiceImpl implements MpWechatService {

    private final WxMpProperties wxMpProperties;
    private final me.chanjar.weixin.mp.api.WxMpService wxMpService;

    @Override
    public MpWechatUserInfo getUserInfo(String code) {
        log.info("通过 code 获取微信用户信息, code={}", code);

        try {
            // 1. 通过 code 获取 access_token 和 openid
            var accessToken = wxMpService.getOAuth2Service().getAccessToken(code);
            String openid = accessToken.getOpenId();
            String unionid = accessToken.getUnionId();
            String accessTokenStr = accessToken.getAccessToken();

            log.info("获取 access_token 成功, openid={}, unionid={}", openid, unionid);

            // 2. 通过 access_token 获取用户详细信息
            var wxMpUser = wxMpService.getOAuth2Service().getUserInfo(accessToken, null);

            String nickname = wxMpUser.getNickname();
            String avatar = wxMpUser.getHeadImgUrl();

            log.info("获取用户信息成功, nickname={}, avatar={}", nickname, avatar);

            return MpWechatUserInfo.builder()
                    .openid(openid)
                    .unionid(unionid)
                    .nickname(nickname)
                    .avatar(avatar)
                    .build();

        } catch (WxErrorException e) {
            log.error("获取微信用户信息失败", e);
            throw new RuntimeException("获取微信用户信息失败: " + e.getError().getErrorMsg(), e);
        }
    }

    @Override
    public void sendTemplateMessage(String openid, String templateId, Object data) {
        log.info("发送模板消息, openid={}, templateId={}", openid, templateId);

        try {
            WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                    .toUser(openid)
                    .templateId(templateId)
                    .build();

            // 如果 data 是 List<WxMpTemplateData>，则直接添加
            if (data instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<WxMpTemplateData> dataList = (java.util.List<WxMpTemplateData>) data;
                for (WxMpTemplateData datum : dataList) {
                    templateMessage.addData(datum);
                }
            }

            String msgId = wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            log.info("模板消息发送成功, msgId={}", msgId);

        } catch (WxErrorException e) {
            log.error("发送模板消息失败", e);
            throw new RuntimeException("发送模板消息失败: " + e.getError().getErrorMsg(), e);
        }
    }

    @Override
    public void sendRegisterSuccessNotification(String openid, String shopName, String username, String password) {
        log.info("发送注册成功通知, openid={}, shopName={}", openid, shopName);

        String templateId = wxMpProperties.getRegisterSuccessTemplateId();
        if (StrUtil.isBlank(templateId)) {
            log.warn("注册成功模板消息未配置 (wx.mp.register-success-template-id)，尝试使用客服消息");
            sendRegisterSuccessKefuMessage(openid, shopName, username, password);
            return;
        }

        try {
            WxMpTemplateMessage templateMessage = WxMpTemplateMessage.builder()
                    .toUser(openid)
                    .templateId(templateId)
                    .build();

            // 填充模板参数（根据实际模板内容调整）
            templateMessage.addData(new WxMpTemplateData("first", "您好，您的宠物门店账号已创建成功！"));
            templateMessage.addData(new WxMpTemplateData("keyword1", shopName));
            templateMessage.addData(new WxMpTemplateData("keyword2", username));
            templateMessage.addData(new WxMpTemplateData("keyword3", password));
            templateMessage.addData(new WxMpTemplateData("remark", "请妥善保管您的账号信息。"));

            wxMpService.getTemplateMsgService().sendTemplateMsg(templateMessage);
            log.info("注册成功模板消息发送成功");

        } catch (WxErrorException e) {
            log.error("发送模板消息失败，尝试使用客服消息", e);
            sendRegisterSuccessKefuMessage(openid, shopName, username, password);
        }
    }

    /**
     * 备选：发送客服消息通知注册成功
     */
    private void sendRegisterSuccessKefuMessage(String openid, String shopName, String username, String password) {
        String content = String.format(
                "【宠物门店SaaS】\n您好，您的门店账号已创建成功！\n\n门店名称：%s\n管理员账号：%s\n初始密码：%s\n\n请妥善保管您的账号信息。",
                shopName, username, password
        );

        sendTextMessage(openid, content);
    }

    /**
     * 发送客服消息
     */
    public void sendTextMessage(String openid, String content) {
        log.info("发送客服消息, openid={}", openid);

        try {
            WxMpKefuMessage kefuMessage = WxMpKefuMessage.TEXT()
                    .toUser(openid)
                    .content(content)
                    .build();

            wxMpService.getKefuService().sendKefuMessage(kefuMessage);
            log.info("客服消息发送成功");

        } catch (WxErrorException e) {
            log.error("发送客服消息失败", e);
            // 客服消息发送失败也不影响主流程，只记录日志
        }
    }
}