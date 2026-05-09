package com.pet.saas.handler.mp;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 微信公众号取消关注事件处理器
 */
@Slf4j
@Component
public class MpUnsubscribeHandler extends AbstractMpMessageHandler {

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                      Map<String, Object> context,
                                      WxMpService wxMpService,
                                      WxSessionManager sessionManager) {
        String openid = wxMessage.getFromUser();
        log.info("用户取消关注公众号: openid={}", openid);

        // TODO: 可以在这里处理用户取消关注逻辑
        // 例如: 更新用户状态, 记录取消关注事件等

        return null;
    }
}