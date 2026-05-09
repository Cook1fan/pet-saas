package com.pet.saas.handler.mp;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微信公众号日志处理器
 * <p>
 * 记录所有收到的微信消息
 * </p>
 */
@Slf4j
@Component
public class MpLogHandler extends AbstractMpMessageHandler {

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                      Map<String, Object> context,
                                      WxMpService wxMpService,
                                      WxSessionManager sessionManager) {
        log.info("收到微信消息: msgType={}, event={}, fromUser={}, toUser={}, content={}",
                wxMessage.getMsgType(), wxMessage.getEvent(),
                wxMessage.getFromUser(), wxMessage.getToUser(),
                wxMessage.getContent());
        return null;
    }
}