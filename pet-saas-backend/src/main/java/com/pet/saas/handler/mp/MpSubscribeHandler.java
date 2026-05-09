package com.pet.saas.handler.mp;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微信公众号关注事件处理器
 */
@Slf4j
@Component
public class MpSubscribeHandler extends AbstractMpMessageHandler {

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                      Map<String, Object> context,
                                      WxMpService wxMpService,
                                      WxSessionManager sessionManager) {
        String openid = wxMessage.getFromUser();
        log.info("用户关注公众号: openid={}", openid);

        // TODO: 可以在这里处理新用户关注逻辑
        // 例如: 发送欢迎消息, 记录用户关注事件等

        // 返回欢迎消息
        return WxMpXmlOutMessage.TEXT()
                .content("欢迎关注宠物门店SaaS服务！\n点击菜单开始注册您的门店吧~")
                .fromUser(wxMessage.getToUser())
                .toUser(wxMessage.getFromUser())
                .build();
    }
}