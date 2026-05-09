package com.pet.saas.config;

import com.pet.saas.config.properties.WxMpProperties;
import com.pet.saas.handler.mp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static me.chanjar.weixin.common.api.WxConsts.EventType;
import static me.chanjar.weixin.common.api.WxConsts.EventType.SUBSCRIBE;
import static me.chanjar.weixin.common.api.WxConsts.EventType.UNSUBSCRIBE;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType;
import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType.EVENT;

/**
 * 微信公众号配置类
 * <p>
 * 配置 WxMpService 和消息路由器
 * </p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WxMpConfig {

    private final WxMpProperties wxMpProperties;

    /**
     * 配置 WxMpService
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "wx.mp", name = "app-id")
    public WxMpService wxMpService() {
        String appId = wxMpProperties.getEffectiveAppId();
        String secret = wxMpProperties.getSecret();
        String token = wxMpProperties.getToken();
        String aesKey = wxMpProperties.getEffectiveAesKey();

        log.info("初始化 WxMpService, appId={}", appId);

        WxMpDefaultConfigImpl configStorage = new WxMpDefaultConfigImpl();
        configStorage.setAppId(appId);
        configStorage.setSecret(secret);
        configStorage.setToken(token);
        configStorage.setAesKey(aesKey);

        WxMpService service = new WxMpServiceImpl();
        service.setWxMpConfigStorage(configStorage);

        log.info("WxMpService 初始化完成");
        return service;
    }

    /**
     * 配置消息路由器
     */
    @Bean
    @ConditionalOnMissingBean
    public WxMpMessageRouter wxMpMessageRouter(WxMpService wxMpService,
                                                  MpLogHandler mpLogHandler,
                                                  MpNullHandler mpNullHandler,
                                                  MpSubscribeHandler mpSubscribeHandler,
                                                  MpUnsubscribeHandler mpUnsubscribeHandler,
                                                  MpMenuHandler mpMenuHandler,
                                                  MpMsgHandler mpMsgHandler) {
        log.info("初始化 WxMpMessageRouter");

        WxMpMessageRouter router = new WxMpMessageRouter(wxMpService);

        // 记录所有消息的日志
        router.rule().handler(mpLogHandler).next();

        // 关注事件
        router.rule().async(false).msgType(EVENT).event(SUBSCRIBE)
                .handler(mpSubscribeHandler).end();

        // 取消关注事件
        router.rule().async(false).msgType(EVENT).event(UNSUBSCRIBE)
                .handler(mpUnsubscribeHandler).end();

        // 点击菜单事件
        router.rule().async(false).msgType(EVENT).event(EventType.CLICK)
                .handler(mpMenuHandler).end();

        // 普通文本消息
        router.rule().async(false).msgType(XmlMsgType.TEXT)
                .handler(mpMsgHandler).end();

        // 默认处理器
        router.rule().async(false).handler(mpNullHandler).end();

        log.info("WxMpMessageRouter 初始化完成");
        return router;
    }
}