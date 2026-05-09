package com.pet.saas.handler.mp;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微信公众号普通消息处理器
 */
@Slf4j
@Component
public class MpMsgHandler extends AbstractMpMessageHandler {

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                      Map<String, Object> context,
                                      WxMpService wxMpService,
                                      WxSessionManager sessionManager) {
        String content = wxMessage.getContent();
        String openid = wxMessage.getFromUser();
        log.info("收到普通消息: content={}, openid={}", content, openid);

        // 简单的回复逻辑
        String reply = "收到您的消息啦！如需帮助，请点击菜单进行门店注册~";

        return WxMpXmlOutMessage.TEXT()
                .content(reply)
                .fromUser(wxMessage.getToUser())
                .toUser(wxMessage.getFromUser())
                .build();
    }
}