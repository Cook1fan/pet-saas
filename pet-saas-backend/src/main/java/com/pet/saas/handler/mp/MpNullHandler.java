package com.pet.saas.handler.mp;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微信公众号默认处理器
 * <p>
 * 处理未匹配到其他处理器的消息
 * </p>
 */
@Slf4j
@Component
public class MpNullHandler extends AbstractMpMessageHandler {

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                      Map<String, Object> context,
                                      WxMpService wxMpService,
                                      WxSessionManager sessionManager) {
        log.info("未匹配到具体处理器的消息: msgType={}, event={}, fromUser={}",
                wxMessage.getMsgType(), wxMessage.getEvent(), wxMessage.getFromUser());
        return null;
    }
}