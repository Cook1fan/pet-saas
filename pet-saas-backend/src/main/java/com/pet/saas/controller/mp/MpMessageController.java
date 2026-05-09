package com.pet.saas.controller.mp;

import com.pet.saas.dto.query.MpMessageReceiveQuery;
import com.pet.saas.dto.query.MpMessageVerifyQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpMessageRouter;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

/**
 * 微信公众号消息处理控制器
 * <p>
 * 用于微信服务器验证和接收消息推送
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/mp/wx")
@RequiredArgsConstructor
@Tag(name = "微信公众号消息接口", description = "微信服务器验证、消息接收等接口")
public class MpMessageController {

    private final WxMpService wxMpService;
    private final WxMpMessageRouter wxMpMessageRouter;

    /**
     * 微信服务器验证接口（GET请求）
     * <p>
     * 开发者通过检验 signature 对请求进行校验
     * 若确认此次 GET 请求来自微信服务器，请原样返回 echostr 参数内容
     * </p>
     */
    @Operation(summary = "微信服务器验证", description = "用于微信公众号服务器配置验证")
    @GetMapping("/message")
    public String verifyServer(@ParameterObject MpMessageVerifyQuery query) {

        log.info("收到微信服务器验证请求: signature={}, timestamp={}, nonce={}, echostr={}",
                query.getSignature(), query.getTimestamp(), query.getNonce(), query.getEchostr());

        // 使用 WxJava 提供的签名验证
        if (wxMpService.checkSignature(query.getTimestamp(), query.getNonce(), query.getSignature())) {
            log.info("微信服务器验证成功，返回 echostr: {}", query.getEchostr());
            return query.getEchostr();
        } else {
            log.warn("微信服务器验证失败");
            return "error";
        }
    }

    /**
     * 接收微信公众号消息推送（POST请求）
     * <p>
     * 接收用户发送的消息、事件推送等
     * </p>
     */
    @Operation(summary = "接收微信消息", description = "接收微信公众号的消息和事件推送")
    @PostMapping(value = "/message", produces = "application/xml; charset=UTF-8")
    public String handleMessage(@RequestBody String requestBody,
                                 @ParameterObject MpMessageReceiveQuery query) {

        log.info("收到微信消息推送: openid={}, encryptType={}", query.getOpenid(), query.getEncryptType());
        log.debug("消息内容: {}", requestBody);

        // 校验签名
        if (!wxMpService.checkSignature(query.getTimestamp(), query.getNonce(), query.getSignature())) {
            log.warn("消息签名验证失败");
            return "error";
        }

        // 解析消息
        WxMpXmlMessage inMessage;
        boolean isEncrypted = "aes".equals(query.getEncryptType());

        if (isEncrypted) {
            // AES 加密传输
            inMessage = WxMpXmlMessage.fromEncryptedXml(requestBody, wxMpService.getWxMpConfigStorage(),
                    query.getTimestamp(), query.getNonce(), query.getMsgSignature());
        } else {
            // 明文传输
            inMessage = WxMpXmlMessage.fromXml(requestBody);
        }

        // 使用消息路由器处理消息
        WxMpXmlOutMessage outMessage = wxMpMessageRouter.route(inMessage);

        if (outMessage == null) {
            return "";
        }

        // 返回响应消息
        if (isEncrypted) {
            return outMessage.toEncryptedXml(wxMpService.getWxMpConfigStorage());
        } else {
            return outMessage.toXml();
        }
    }
}