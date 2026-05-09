package com.pet.saas.handler.mp;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微信公众号菜单事件处理器
 */
@Slf4j
@Component
public class MpMenuHandler extends AbstractMpMessageHandler {

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                      Map<String, Object> context,
                                      WxMpService wxMpService,
                                      WxSessionManager sessionManager) {
        String eventKey = wxMessage.getEventKey();
        String openid = wxMessage.getFromUser();
        log.info("菜单事件: eventKey={}, openid={}", eventKey, openid);

        // TODO: 根据不同的 eventKey 处理不同的菜单事件

        return null;
    }
}